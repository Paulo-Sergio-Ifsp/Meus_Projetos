package com.paulo.controle_gastos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.paulo.controle_gastos.data.converters.TipoContaConverter
import com.paulo.controle_gastos.data.dao.ContaDao
import com.paulo.controle_gastos.data.dao.DespesaDao
import com.paulo.controle_gastos.data.dao.GanhoDao
import com.paulo.controle_gastos.data.entity.ContaEntity
import com.paulo.controle_gastos.data.entity.DespesaEntity
import com.paulo.controle_gastos.data.entity.GanhoEntity

@Database(
    entities = [
        ContaEntity::class,
        DespesaEntity::class,
        GanhoEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(TipoContaConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contaDao(): ContaDao
    abstract fun despesaDao(): DespesaDao
    abstract fun ganhoDao(): GanhoDao
}
