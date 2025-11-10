package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete // <-- Precisa deste import
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.model.Conta
import kotlinx.coroutines.flow.Flow

@Dao
interface ContaDao {

    @Query("SELECT * FROM contas ORDER BY nome ASC")
    fun getAll(): Flow<List<Conta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conta: Conta)

    @Delete // <-- Precisa da anotação
    suspend fun delete(conta: Conta) // A função que estava causando o erro
}