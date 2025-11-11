package com.paulo.controle_gastos.model

data class Despesa(
    val id: String,
    val data: Long,
    val local: String,
    val valor: Double,
    val metodoPagamento: String,
    val contaId: String
)
