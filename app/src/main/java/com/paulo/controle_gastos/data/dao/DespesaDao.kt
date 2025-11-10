package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete // <-- Precisa deste import
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.model.Despesa
import kotlinx.coroutines.flow.Flow

@Dao
interface DespesaDao {

    @Query("SELECT * FROM despesas ORDER BY data DESC")
    fun getAll(): Flow<List<Despesa>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(despesa: Despesa)

    @Delete // <-- Precisa da anotação
    suspend fun delete(despesa: Despesa) // Função que causou o erro
}