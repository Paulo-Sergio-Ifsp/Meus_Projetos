package com.paulo.controle_gastos.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class DespesaEntity(
    @PrimaryKey val id: String,
    val data: Long,
    val local: String,
    val valor: Double,
    val contaId: String,
    val metodoPagamento: String
)