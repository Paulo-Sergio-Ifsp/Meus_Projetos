package com.paulo.controle_gastos.model

import android.util.Log
import java.util.UUID

object ExpenseParser {

    private val NUBANK_REGEX = Regex("Compra aprovada no seu NUBANK R\\$ (\\d+,\\d{2}) em (.+)\\.")
    private val ITAU_REGEX = Regex("Compra de R\\$(\\d+,\\d{2}) aprovada em .+ na (.+)\\.")

    fun parseMessage(remetente: String, mensagem: String, contaIdPadrao: String): Despesa? {
        var match: MatchResult? = null
        var metodoPagamento = "Crédito"

        if (remetente.contains("NUBANK", true) || mensagem.contains("NUBANK", true)) {
            match = NUBANK_REGEX.find(mensagem)
            if (mensagem.contains("Débito", true)) metodoPagamento = "Débito"
            if (mensagem.contains("Pix", true)) metodoPagamento = "Pix"

        } else if (remetente.contains("ITAUCARD", true) || mensagem.contains("ITAUCARD", true)) {
            match = ITAU_REGEX.find(mensagem)
        }

        if (match != null && match.groupValues.size >= 3) {
            try {
                val valorString = match.groupValues[1].replace(",", ".")
                val valorDouble = valorString.toDouble()
                val local = match.groupValues[2].trimEnd('.')

                return Despesa(
                    id = UUID.randomUUID().toString(),
                    data = System.currentTimeMillis(),
                    local = local,
                    valor = valorDouble,
                    contaId = contaIdPadrao,
                    metodoPagamento = metodoPagamento
                )
            } catch (e: Exception) {
                Log.e("ExpenseParser", "Erro ao converter valor", e)
            }
        }
        return null
    }
}
