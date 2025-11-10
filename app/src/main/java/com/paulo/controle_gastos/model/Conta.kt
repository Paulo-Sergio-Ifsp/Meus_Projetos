package com.paulo.controle_gastos.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contas")
data class Conta(
    @PrimaryKey
    val id: String,
    val nome: String,
    val tipo: TipoConta,
    val saldoInicial: Double = 0.0
)