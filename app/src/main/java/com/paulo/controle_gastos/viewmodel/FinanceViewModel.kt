package com.paulo.controle_gastos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paulo.controle_gastos.data.repository.FinanceRepository
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// 1. O UiState (sem mudanças na estrutura)
data class FinanceUiState(
    val despesas: List<Despesa> = emptyList(),
    val ganhos: List<Ganho> = emptyList(),
    val contas: List<Conta> = emptyList(),

    val saldoTotal: Double = 0.0,
    val ganhosTotais: Double = 0.0,
    val despesasTotais: Double = 0.0
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            // 2. O combine com a LÓGICA CORRIGIDA
            combine(
                repository.despesas,
                repository.ganhos,
                repository.contas
            ) { despesasLista, ganhosLista, contasList ->

                // --- A Lógica Antiga (simplificada) ---
                // val saldo = contasList.sumOf { it.saldoInicial }
                // val ganhos = ganhosLista.sumOf { it.valor }
                // val despesas = despesasLista.sumOf { it.valor }

                // --- ✅ A NOVA LÓGICA ---

                // Totais para o Dashboard (barra de progresso)
                val ganhosDashboard = ganhosLista.sumOf { it.valor }
                val despesasDashboard = despesasLista.sumOf { it.valor }

                // Totais para o Saldo Real (o "saldo vivo")
                val totalSaldoInicial = contasList.sumOf { it.saldoInicial }
                val totalGanhos = ganhosLista.sumOf { it.valor }

                // Soma apenas as despesas que saem do saldo (não-cartão)
                val despesasDebitadas = despesasLista
                    .filter { it.metodoPagamento.lowercase() != "cartão" }
                    .sumOf { it.valor }

                // O cálculo do saldo real que você sugeriu
                val saldoReal = totalSaldoInicial + totalGanhos - despesasDebitadas

                FinanceUiState(
                    despesas = despesasLista,
                    ganhos = ganhosLista,
                    contas = contasList,
                    saldoTotal = saldoReal, // ✅ Saldo "vivo"
                    ganhosTotais = ganhosDashboard, // Total para a barra
                    despesasTotais = despesasDashboard // Total para a barra
                )
            }.collect { _uiState.value = it }
        }
    }

    // --- Funções de Escrita (sem mudanças) ---
    fun addDespesa(d: Despesa) = viewModelScope.launch { repository.addDespesa(d) }
    fun addGanho(g: Ganho)     = viewModelScope.launch { repository.addGanho(g) }
    fun addConta(c: Conta)     = viewModelScope.launch { repository.addConta(c) }

    fun deleteDespesa(d: Despesa) = viewModelScope.launch { repository.deleteDespesa(d) }
    fun deleteGanho(g: Ganho)     = viewModelScope.launch { repository.deleteGanho(g) }
    fun deleteConta(c: Conta)     = viewModelScope.launch { repository.deleteConta(c) }

    // --- 3. O 'companion object' (sem mudanças) ---
    companion object {
        fun provideFactory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinanceViewModel(repository) as T
                }
            }
    }
}