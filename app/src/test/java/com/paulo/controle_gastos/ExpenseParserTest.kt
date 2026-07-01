package com.paulo.controle_gastos

import com.paulo.controle_gastos.model.ExpenseParser
import org.junit.Test
import org.junit.Assert.*

class ExpenseParserTest {

    @Test
    fun parseNubankNotification() {
        val titulo = "NUBANK"
        val texto = "Compra aprovada no NUBANK R$ 45,90 em SUPERMERCADO XYZ."
        val contaId = "conta1"
        val despesa = ExpenseParser.parseMessage(titulo, texto, contaId)
        assertNotNull(despesa)
        assertTrue(despesa!!.local.lowercase().contains("supermercado xyz"))
        assertEquals(45.90, despesa.valor, 0.001)
        assertEquals(contaId, despesa.contaId)
    }

    @Test
    fun parseItauNotification() {
        val titulo = "ITAUCARD"
        val texto = "Compra de R$ 120,00 aprovada em LOJA ABC débito."
        val contaId = "conta2"
        val despesa = ExpenseParser.parseMessage(titulo, texto, contaId)
        assertNotNull(despesa)
        assertTrue(despesa!!.local.lowercase().contains("loja abc"))
        assertEquals(120.00, despesa.valor, 0.001)
        assertEquals(contaId, despesa.contaId)
    }

    @Test
    fun parseNonMatchingReturnsNull() {
        val titulo = "SISTEMA"
        val texto = "Bem-vindo ao serviço de alerta." // não contém 'compra' nem 'valor'
        val despesa = ExpenseParser.parseMessage(titulo, texto, "c")
        assertNull(despesa)
    }

    @Test
    fun parsePixWithThousandSeparator() {
        val titulo = "Banco"
        val texto = "Compra no valor 1.234,56 via pix em MERCADO CENTRAL."
        val despesa = ExpenseParser.parseMessage(titulo, texto, "conta3")

        assertNotNull(despesa)
        assertEquals(1234.56, despesa!!.valor, 0.001)
        assertEquals("Pix", despesa.metodoPagamento)
        assertTrue(despesa.local.lowercase().contains("mercado central"))
    }

    @Test
    fun parseWithoutCurrencyPrefixUsingValorKeyword() {
        val titulo = "Alerta"
        val texto = "Compra aprovada valor 89,90 em PADARIA SOL."
        val despesa = ExpenseParser.parseMessage(titulo, texto, "conta4")

        assertNotNull(despesa)
        assertEquals(89.90, despesa!!.valor, 0.001)
        assertTrue(despesa.local.lowercase().contains("padaria sol"))
    }
}
