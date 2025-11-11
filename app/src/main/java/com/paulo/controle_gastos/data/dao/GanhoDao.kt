package com.paulo.controle_gastos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paulo.controle_gastos.data.entity.GanhoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GanhoDao {

    @Query("SELECT * FROM ganhos")
    fun getAll(): Flow<List<GanhoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ganho: GanhoEntity)

    @Delete
    suspend fun delete(ganho: GanhoEntity)
}
