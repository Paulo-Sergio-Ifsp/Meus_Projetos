package com.paulo.controle_gastos.data.repository

import com.paulo.controle_gastos.data.dao.ContaDao
import com.paulo.controle_gastos.data.dao.DespesaDao
import com.paulo.controle_gastos.data.dao.GanhoDao
import com.paulo.controle_gastos.data.toEntity
import com.paulo.controle_gastos.data.toModel
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val contaDao: ContaDao,
    private val despesaDao: DespesaDao,
    private val ganhoDao: GanhoDao
) {

    val contas: Flow<List<Conta>> =
        contaDao.getAll().map { list -> list.map { it.toModel() } }

    val despesas: Flow<List<Despesa>> =
        despesaDao.getAll().map { list -> list.map { it.toModel() } }

    val ganhos: Flow<List<Ganho>> =
        ganhoDao.getAll().map { list -> list.map { it.toModel() } }

    // ---- INSERT ----

    suspend fun addConta(conta: Conta) =
        contaDao.insert(conta.toEntity())

    suspend fun addDespesa(despesa: Despesa) =
        despesaDao.insert(despesa.toEntity())

    suspend fun addGanho(ganho: Ganho) =
        ganhoDao.insert(ganho.toEntity())

    // ---- DELETE ----

    suspend fun deleteConta(conta: Conta) =
        contaDao.delete(conta.toEntity())

    suspend fun deleteDespesa(d: Despesa) =
        despesaDao.delete(d.toEntity())

    suspend fun deleteGanho(g: Ganho) =
        ganhoDao.delete(g.toEntity())
}
