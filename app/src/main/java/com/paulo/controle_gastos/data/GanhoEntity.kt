package com.paulo.controle_gastos.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ganhos")
data class GanhoEntity(
    @PrimaryKey
    val id: String,
    val data: Long,
    val descricao: String,
    val valor: Double,
    @ColumnInfo(index = true) // Boa prática
    val contaId: String
)