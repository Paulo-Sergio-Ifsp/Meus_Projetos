package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.data.entity.ContaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContaDao {

    @Query("SELECT * FROM contas")
    fun getAll(): Flow<List<ContaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conta: ContaEntity)

    @Delete
    suspend fun delete(conta: ContaEntity)
}
