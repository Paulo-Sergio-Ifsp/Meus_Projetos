package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.data.entity.DespesaEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface DespesaDao {

    @Query("SELECT * FROM despesas")
    fun getAll(): Flow<List<DespesaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(despesa: DespesaEntity)

    @Delete
    suspend fun delete(despesa: DespesaEntity)
}

