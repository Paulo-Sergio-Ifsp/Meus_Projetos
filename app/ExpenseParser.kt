package com.paulo.controle_gastos

import android.util.Log
import com.paulo.controle_gastos.model.Despesa
import java.util.UUID

object ExpenseParser {

    // Patterns mais tolerantes (aceitam pontos ou vírgulas e diferentes frases)
    private val NUBANK_REGEX = Regex("""Compra(?: aprovada)? no (?:seu )?NUBANK.*?R\$?\s*([\d\.,]+)\s*(?:em|no|na)\s+(.+?)(?:[.\n]|$)""", RegexOption.IGNORE_CASE)
    private val ITAU_REGEX = Regex("""Compra(?: de)?\s*R\$?\s*([\d\.,]+).*?(?:aprovad[ao].*?em.*?na|em)\s+(.+?)(?:[.\n]|$)""", RegexOption.IGNORE_CASE)

    /**
     * Parseia uma mensagem (remetente + corpo) tentando extrair uma Despesa.
     * @param remetente - remetente do SMS/push (ex: "NUBANK", "ITAUCARD")
     * @param mensagem - corpo da mensagem
     * @param contaIdPadrao - id da conta a ser usado se não for possível mapear
     * @return Despesa? - objeto Despesa (ou null se não reconhecido)
     */
    fun parseMessage(remetente: String, mensagem: String, contaIdPadrao: String): Despesa? {
        var match: MatchResult? = null
        var metodoPagamento = "Crédito"

        val from = (remetente + " " + mensagem).lowercase()

        if (from.contains("nubank")) {
            match = NUBANK_REGEX.find(mensagem)
            if (mensagem.contains("débito", ignoreCase = true) || mensagem.contains("debito", ignoreCase = true)) metodoPagamento = "Débito"
            if (mensagem.contains("pix", ignoreCase = true)) metodoPagamento = "Pix"

        } else if (from.contains("itau") || from.contains("itauc")) {
            match = ITAU_REGEX.find(mensagem)
            if (mensagem.contains("débito", ignoreCase = true) || mensagem.contains("debito", ignoreCase = true)) metodoPagamento = "Débito"
            if (mensagem.contains("pix", ignoreCase = true)) metodoPagamento = "Pix"
        }

        if (match != null && match.groupValues.size >= 3) {
            try {
                val rawValor = match.groupValues[1].trim()
                // normaliza: trocar '.' por '' quando usado como milhares e ',' por '.' para decimal
                val normalized = rawValor
                    .replace(".", "") // remove separador de milhares, se houver
                    .replace(",", ".") // transforma vírgula decimal em ponto
                    .trim()
                val valorDouble = normalized.toDoubleOrNull()
                val local = match.groupValues[2].trim().trimEnd('.')

                if (valorDouble == null || local.isBlank()) return null

                // Tente criar a Despesa com e sem id — adapte conforme sua data class:
                return try {
                    // Versão quando Despesa tem um campo `id: String` como primeiro parâmetro
                    Despesa(
                        id = UUID.randomUUID().toString(),
                        data = System.currentTimeMillis(),
                        local = local,
                        valor = valorDouble,
                        contaId = contaIdPadrao,
                        metodoPagamento = metodoPagamento
                    )
                } catch (e: NoSuchMethodError) {
                    // Se a versão acima falhar em tempo de execução/compilação, tenta a versão sem id
                    Despesa(
                        data = System.currentTimeMillis(),
                        local = local,
                        valor = valorDouble,
                        contaId = contaIdPadrao,
                        metodoPagamento = metodoPagamento
                    )
                } catch (e: Throwable) {
                    // Caso o compilador não permita o catch de NoSuchMethodError (em Kotlin/JVM pode ser diferente),
                    // tentamos criar sem id de forma segura:
                    try {
                        Despesa(
                            data = System.currentTimeMillis(),
                            local = local,
                            valor = valorDouble,
                            contaId = contaIdPadrao,
                            metodoPagamento = metodoPagamento
                        )
                    } catch (ex: Exception) {
                        Log.e("ExpenseParser", "Construtor Despesa não compatível", ex)
                        null
                    }
                }

            } catch (e: Exception) {
                Log.e("Parser", "Erro ao converter valor ou construir Despesa", e)
                return null
            }
        }
        return null
    }
}
