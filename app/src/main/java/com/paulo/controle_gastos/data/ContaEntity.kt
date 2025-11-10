package com.paulo.controle_gastos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.paulo.controle_gastos.model.TipoConta

@Entity(tableName = "contas")
data class ContaEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val tipo: TipoConta,
    val saldoInicial: Double = 0.0
)
