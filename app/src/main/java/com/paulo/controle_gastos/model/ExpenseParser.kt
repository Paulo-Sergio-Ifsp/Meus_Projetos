package com.paulo.controle_gastos.model

import java.util.UUID

object ExpenseParser {

    // ✅ Método novo que o NotificationReaderService usa
    fun parseMessage(titulo: String, texto: String, contaIdPadrao: String): Despesa? {
        val mensagem = "$titulo $texto".lowercase()

        // Caso a notificação não parece ser compra
        if (!mensagem.contains("compra") && !mensagem.contains("valor")) {
            return null
        }

        // Extrai valor
        val valorRegex = Regex("""r\$?\s*([\d.,]+)""")
        val valor = valorRegex.find(mensagem)
            ?.groupValues?.get(1)
            ?.replace(",", ".")
            ?.toDoubleOrNull() ?: return null

        val local = when {
            mensagem.contains("no estabelecimento") ->
                mensagem.substringAfter("no estabelecimento").trim().take(40)
            mensagem.contains("em ") ->
                mensagem.substringAfter("em ").trim().take(40)
            else -> "Local desconhecido"
        }

        return Despesa(
            id = UUID.randomUUID().toString(),
            data = System.currentTimeMillis(),
            local = local,
            valor = valor,
            metodoPagamento = "Cartão",
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
}
