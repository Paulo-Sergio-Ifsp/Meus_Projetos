package com.paulo.controle_gastos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ganhos")
data class GanhoEntity(
    @PrimaryKey val id: String,
    val data: Long,
    val descricao: String,
    val valor: Double,
    val contaId: String
)
