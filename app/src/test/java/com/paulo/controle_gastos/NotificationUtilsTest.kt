package com.paulo.controle_gastos

import android.os.Bundle
import com.paulo.controle_gastos.util.NotificationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationUtilsTest {

    @Test
    fun extractTextCombinesTitleBigTextAndLines() {
        val extras = Bundle().apply {
            putString("android.title", "NUBANK")
            putString("android.text", "Compra aprovada")
            putString("android.bigText", "Compra aprovada R$ 12,00 em LOJA ABC.")
        }

        val (titulo, texto) = NotificationUtils.extractText(extras)

        assertEquals("NUBANK", titulo)
        assertTrue(texto.contains("Compra aprovada"))
        assertTrue(texto.contains("R$ 12,00"))
        assertTrue(texto.contains("LOJA ABC"))
    }

    @Test
    fun isBankRelatedDetectsNubankPackage() {
        assertTrue(
            NotificationUtils.isBankRelated(
                packageName = "com.nu.production",
                titulo = "Compra",
                texto = "R$ 10,00"
            )
        )
    }

    @Test
    fun isBankRelatedDetectsPixTransactionText() {
        assertTrue(
            NotificationUtils.isBankRelated(
                packageName = "com.example.app",
                titulo = "Alerta",
                texto = "Pix enviado valor R$ 5,00"
            )
        )
    }
}
