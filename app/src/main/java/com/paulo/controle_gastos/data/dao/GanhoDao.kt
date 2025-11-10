package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete // <-- Precisa deste import
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.model.Ganho
import kotlinx.coroutines.flow.Flow

@Dao
interface GanhoDao {

    @Query("SELECT * FROM ganhos ORDER BY data DESC")
    fun getAll(): Flow<List<Ganho>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ganho: Ganho)

    @Delete // <-- Precisa da anotação
    suspend fun delete(ganho: Ganho) // Função que causou o erro
}