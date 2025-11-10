package com.paulo.controle_gastos.data.repository

import com.paulo.controle_gastos.data.toEntity
import com.paulo.controle_gastos.data.toModel
import com.paulo.controle_gastos.data.dao.ContaDao
import com.paulo.controle_gastos.data.dao.DespesaDao
import com.paulo.controle_gastos.data.dao.GanhoDao
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val despesaDao: DespesaDao,
    private val ganhoDao: GanhoDao,
    private val contaDao: ContaDao
) {

    // --- LÓGICA DE GANHO ---
    val ganhos: Flow<List<Ganho>> = ganhoDao.getAll()
        .map { list -> list.map { it.toModel() } } // Converte Entity -> Model

    suspend fun addGanho(ganho: Ganho) {
        ganhoDao.insert(ganho.toEntity()) // Converte Model -> Entity
        // Remover a linha abaixo se 'atualizarSaldo' não existir
        // contaDao.atualizarSaldo(ganho.contaId, ganho.valor)
    }

    suspend fun deleteGanho(ganho: Ganho) {
        ganhoDao.delete(ganho.toEntity()) // Converte Model -> Entity
    }

    // --- LÓGICA DE DESPESA ---
    val despesas: Flow<List<Despesa>> = despesaDao.getAll()
        .map { list -> list.map { it.toModel() } } // Converte Entity -> Model

    suspend fun addDespesa(despesa: Despesa) {
        despesaDao.insert(despesa.toEntity()) // Converte Model -> Entity
        // Remover a linha abaixo se 'atualizarSaldo' não existir
        // contaDao.atualizarSaldo(despesa.contaId, -despesa.valor)
    }

    suspend fun deleteDespesa(despesa: Despesa) {
        despesaDao.delete(despesa.toEntity()) // Converte Model -> Entity
    }

    // --- LÓGICA DE CONTA ---
    val contas: Flow<List<Conta>> = contaDao.getAll()
        .map { list -> list.map { it.toModel() } } // Converte Entity -> Model

    suspend fun addConta(conta: Conta) {
        contaDao.insert(conta.toEntity()) // Converte Model -> Entity
    }

    suspend fun deleteConta(conta: Conta) {
        contaDao.delete(conta.toEntity()) // Converte Model -> Entity
    }
}
