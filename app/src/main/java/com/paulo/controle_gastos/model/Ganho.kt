package com.paulo.controle_gastos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ganhos",
    // Esta parte é nova:
    foreignKeys = [
        ForeignKey(
            entity = Conta::class,
            parentColumns = ["id"],
            childColumns = ["contaId"],
            onDelete = ForeignKey.CASCADE // Deleta ganhos se a conta for deletada
        )
    ]
)
data class Ganho(
    @PrimaryKey
    val id: String,
    val data: Long,
    val descricao: String,
    val valor: Double,

    @ColumnInfo(index = true) // Melhora a performance de buscas por contaId
    val contaId: String
)