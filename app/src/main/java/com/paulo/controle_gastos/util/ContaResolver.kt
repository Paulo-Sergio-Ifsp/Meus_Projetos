package com.paulo.controle_gastos.util

import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.TipoConta

object ContaResolver {

    private val BANK_ALIASES = mapOf(
        "nubank" to listOf("nubank", "nu "),
        "itau" to listOf("itau", "itauc", "itaú"),
        "bradesco" to listOf("bradesco"),
        "santander" to listOf("santander"),
        "inter" to listOf("inter", "bancointer"),
        "c6" to listOf("c6", "c6bank"),
        "bb" to listOf("banco do brasil", "brasil"),
        "caixa" to listOf("caixa"),
        "picpay" to listOf("picpay"),
        "mercado" to listOf("mercado pago", "mercadopago"),
    )

    fun resolve(
        contas: List<Conta>,
        titulo: String,
        texto: String,
        packageName: String?,
        metodoPagamento: String
    ): String? {
        if (contas.isEmpty()) return null

        val source = "$titulo $texto ${packageName.orEmpty()}".lowercase()
        val matched = contas.filter { conta -> matchesConta(conta, source) }
        val pool = matched.ifEmpty { contas }

        val preferCartao = metodoPagamento in listOf("Crédito", "Cartão")
        if (preferCartao) {
            pool.firstOrNull { it.tipo == TipoConta.CARTAO_CREDITO }?.let { return it.id }
        }
        pool.firstOrNull { it.tipo != TipoConta.CARTAO_CREDITO }?.let { return it.id }
        return pool.first().id
    }

    private fun matchesConta(conta: Conta, source: String): Boolean {
        val nome = conta.nome.lowercase()
        if (nome.isNotBlank() && source.contains(nome)) return true

        return BANK_ALIASES.any { (bank, aliases) ->
            val sourceHasBank = source.contains(bank) || aliases.any { source.contains(it) }
            val contaHasBank = nome.contains(bank) || aliases.any { nome.contains(it) }
            sourceHasBank && contaHasBank
        }
    }
}
