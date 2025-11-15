package com.paulo.controle_gastos.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun formatCurrency(value: Double): String {
        return currencyFormatter.format(value)
    }

    /**
     * Parse robusto para entrada do usuário:
     * aceita formatos como:
     * - "1.234,56" (pt-BR with thousands dot and comma decimal)
     * - "1234,56"
     * - "1234.56" (en-US)
     * - with or without "R$" and spaces
     *
     * Retorna null se não for possível parsear.
     */
    fun parseUserDecimal(input: String?): Double? {
        if (input == null) return null
        val s = input.replace("[R$\\s]".toRegex(), "")
        if (s.isEmpty()) return null

        return when {
            s.contains(',') -> {
                // assume comma is decimal separator, dots are thousand separators
                val noDots = s.replace(".", "")
                val withDot = noDots.replace(",", ".")
                withDot.toDoubleOrNull()
            }
            s.contains('.') -> {
                // assume dot is decimal separator (en-US)
                s.toDoubleOrNull()
            }
            else -> s.toDoubleOrNull()
        }
    }
}