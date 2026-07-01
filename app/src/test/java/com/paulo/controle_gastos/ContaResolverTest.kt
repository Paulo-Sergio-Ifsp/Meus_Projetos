package com.paulo.controle_gastos

import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.TipoConta
import com.paulo.controle_gastos.util.ContaResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class ContaResolverTest {

    private val contas = listOf(
        Conta("1", "Nubank", TipoConta.CARTAO_CREDITO, 0.0),
        Conta("2", "Itaú Corrente", TipoConta.CONTA_CORRENTE, 1000.0),
        Conta("3", "Carteira", TipoConta.CARTEIRA, 50.0),
    )

    @Test
    fun resolveNubankCreditToCardAccount() {
        val id = ContaResolver.resolve(
            contas = contas,
            titulo = "NUBANK",
            texto = "Compra aprovada R$ 45,90 em LOJA.",
            packageName = "com.nu.production",
            metodoPagamento = "Crédito"
        )
        assertEquals("1", id)
    }

    @Test
    fun resolveItauPixToCheckingAccount() {
        val id = ContaResolver.resolve(
            contas = contas,
            titulo = "ITAÚ",
            texto = "Compra pix valor 20,00 em PADARIA.",
            packageName = "com.itau",
            metodoPagamento = "Pix"
        )
        assertEquals("2", id)
    }

    @Test
    fun resolveUnknownBankFallsBackToFirstNonCard() {
        val id = ContaResolver.resolve(
            contas = contas,
            titulo = "Banco",
            texto = "Compra valor 10,00 em MERCADO.",
            packageName = null,
            metodoPagamento = "Débito"
        )
        assertEquals("2", id)
    }
}
