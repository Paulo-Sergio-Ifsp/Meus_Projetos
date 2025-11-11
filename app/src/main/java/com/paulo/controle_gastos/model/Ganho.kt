package com.paulo.controle_gastos.model

data class Ganho(
    val id: String,
    val data: Long,
    val descricao: String,
    val valor: Double,
    val contaId: String
)
