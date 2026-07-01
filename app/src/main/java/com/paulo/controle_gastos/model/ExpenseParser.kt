package com.paulo.controle_gastos.model

import java.util.UUID

object ExpenseParser {

    fun parseMessage(titulo: String, texto: String, contaIdPadrao: String): Despesa? {
        val mensagemOriginal = "$titulo $texto"
        val mensagem = mensagemOriginal.lowercase()

        // Caso a notificação não parece ser compra/transação
        if (!mensagem.contains("compra") && !mensagem.contains("valor") && !mensagem.contains("pix")) {
            return null
        }

        // Extrai valor depois de 'R$' ou da palavra 'valor'.
        val valorRegex = Regex("""(?:r\$\s*|valor\s*)([\d][\d.,]*)""", RegexOption.IGNORE_CASE)
        val rawValor = valorRegex.find(mensagem)?.groupValues?.get(1) ?: return null
        val valor = parseMonetaryValue(rawValor) ?: return null

        val local = Regex("""(?:no estabelecimento|\bem\b|\bno\b|\bna\b|\bpara\b|\bdestinado a\b)\s+([^\n\.;,]+)""", RegexOption.IGNORE_CASE)
            .findAll(mensagemOriginal)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.trim()
            ?.take(60)
            ?.ifBlank { "Local desconhecido" }
            ?: "Local desconhecido"

        val metodoPagamento = detectPaymentMethod(mensagem)

        return Despesa(
            id = UUID.randomUUID().toString(),
            data = System.currentTimeMillis(),
            local = local,
            valor = valor,
            metodoPagamento = metodoPagamento,
            contaId = contaIdPadrao
        )
    }

    // ✅ Você pode manter este se quiser, não atrapalha
    fun fromText(texto: String, contaId: String): Despesa {
        val id = UUID.randomUUID().toString()
        val data = System.currentTimeMillis()
        val local = "Desconhecido"
        val valor = 0.0
        val metodo = "Dinheiro"
        return Despesa(
            id = id,
            data = data,
            local = local,
            valor = valor,
            metodoPagamento = metodo,
            contaId = contaId
        )
    }

    private fun detectPaymentMethod(mensagem: String): String = when {
        mensagem.contains("pix") -> "Pix"
        mensagem.contains("débito") || mensagem.contains("debito") -> "Débito"
        mensagem.contains("crédito") || mensagem.contains("credito") -> "Crédito"
        else -> "Cartão"
    }

    private fun parseMonetaryValue(raw: String): Double? {
        val clean = raw.trim().replace(" ", "")

        return when {
            clean.contains(",") && clean.contains(".") -> {
                // Formato BR com milhar e decimal (ex: 1.234,56)
                clean.replace(".", "").replace(",", ".").toDoubleOrNull()
            }

            clean.contains(",") -> {
                // Formato decimal com vírgula (ex: 45,90)
                clean.replace(",", ".").toDoubleOrNull()
            }

            clean.count { it == '.' } == 1 && clean.substringAfter('.').length == 3 -> {
                // Formato com milhar sem decimal (ex: 1.234)
                clean.replace(".", "").toDoubleOrNull()
            }

            clean.count { it == '.' } > 1 -> {
                // Vários pontos são tratados como separador de milhar.
                clean.replace(".", "").toDoubleOrNull()
            }

            else -> clean.toDoubleOrNull()
        }
    }
}
