package com.paulo.controle_gastos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.paulo.controle_gastos.data.dao.ContaDao
import com.paulo.controle_gastos.data.dao.DespesaDao
import com.paulo.controle_gastos.data.dao.GanhoDao

@Database(
    // ❗ USAR AS CLASSES DE ENTIDADE AQUI:
    entities = [ContaEntity::class, DespesaEntity::class, GanhoEntity::class],
    version = 1
    // Se você já tinha uma versão do app instalada, mude para version = 2
)
@TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contaDao(): ContaDao
    abstract fun despesaDao(): DespesaDao
    abstract fun ganhoDao(): GanhoDao
}