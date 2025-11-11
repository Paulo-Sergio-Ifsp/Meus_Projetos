package com.paulo.controle_gastos.model

data class Conta(
    val id: String,
    val nome: String,
    val tipo: TipoConta,
    val saldoInicial: Double
)
