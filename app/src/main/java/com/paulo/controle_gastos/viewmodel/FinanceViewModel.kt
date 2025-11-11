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

data class FinanceUiState(
    val despesas: List<Despesa> = emptyList(),
    val ganhos: List<Ganho> = emptyList(),
    val contas: List<Conta> = emptyList()
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.despesas,
                repository.ganhos,
                repository.contas
            ) { d, g, c ->
                FinanceUiState(despesas = d, ganhos = g, contas = c)
            }.collect { _uiState.value = it }
        }
    }

    // Writes
    fun addDespesa(d: Despesa) = viewModelScope.launch { repository.addDespesa(d) }
    fun addGanho(g: Ganho)     = viewModelScope.launch { repository.addGanho(g) }
    fun addConta(c: Conta)     = viewModelScope.launch { repository.addConta(c) }

    fun deleteDespesa(d: Despesa) = viewModelScope.launch { repository.deleteDespesa(d) }
    fun deleteGanho(g: Ganho)     = viewModelScope.launch { repository.deleteGanho(g) }
    fun deleteConta(c: Conta)     = viewModelScope.launch { repository.deleteConta(c) }

    companion object {
        fun provideFactory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FinanceViewModel(repository) as T
                }
            }
    }
}
