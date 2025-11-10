package com.paulo.controle_gastos.model

/**
 * ATUALIZADO: Adicionamos um ID e campos para
 * contaId e método de pagamento (Pix, Débito, etc.)
 */
data class Despesa(
    val id: String = java.util.UUID.randomUUID().toString(),
    val data: Long,
    val local: String,
    val valor: Double,
    val contaId: String, // ID da Conta (banco ou cartão)
    val metodoPagamento: String // "Pix", "Débito", "Crédito"
)

