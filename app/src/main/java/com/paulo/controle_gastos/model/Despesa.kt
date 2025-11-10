package com.paulo.controle_gastos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "despesas",
    foreignKeys = [
        ForeignKey(
            entity = Conta::class,
            parentColumns = ["id"],
            childColumns = ["contaId"],
            onDelete = ForeignKey.CASCADE // Deleta despesas se a conta for deletada
        )
    ]
)
data class Despesa(
    @PrimaryKey // <-- Precisa desta anotação
    val id: String,
    val data: Long,
    val local: String,
    val valor: Double,
    val metodoPagamento: String,

    @ColumnInfo(index = true)
    val contaId: String
)