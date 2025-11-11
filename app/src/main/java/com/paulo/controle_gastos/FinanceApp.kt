package com.paulo.controle_gastos

import android.app.Application
import androidx.room.Room
import com.paulo.controle_gastos.data.AppDatabase
import com.paulo.controle_gastos.data.repository.FinanceRepository

class FinanceApp : Application() {
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "controle_gastos_db").build()
    }
    val repository by lazy {
        FinanceRepository(
            database.contaDao(),
            database.despesaDao(),
            database.ganhoDao()
        )
    }
}
