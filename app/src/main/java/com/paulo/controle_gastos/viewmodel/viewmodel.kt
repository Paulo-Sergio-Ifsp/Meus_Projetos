package com.paulo.controle_gastos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paulo.controle_gastos.data.repository.FinanceRepository
import com.paulo.controle_gastos.model.*
import kotlinx.coroutines.flow.*
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
        // Atualiza a UI sempre que algum dado mudar
        viewModelScope.launch {
            combine(
                repository.despesas,
                repository.ganhos,
                repository.contas,
            ) { despesas, ganhos, contas ->
                FinanceUiState(despesas, ganhos, contas)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addDespesa(despesa: Despesa) {
        viewModelScope.launch { repository.addDespesa(despesa) }
    }

    fun addGanho(ganho: Ganho) {
        viewModelScope.launch { repository.addGanho(ganho) }
    }

    fun addConta(conta: Conta) {
        viewModelScope.launch { repository.addConta(conta) }
    }


    // ✅ Factory para o ViewModel (corrige o erro do MainActivity)
    companion object {
        fun Factory(repository: FinanceRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FinanceViewModel(repository) as T
                }
            }
        }
    }

    // ADICIONE ESTAS FUNÇÕES:
    fun deleteDespesa(despesa: Despesa) = viewModelScope.launch {
        repository.deleteDespesa(despesa)
    }

    fun deleteGanho(ganho: Ganho) = viewModelScope.launch {
        repository.deleteGanho(ganho)
    }

    fun deleteConta(conta: Conta) = viewModelScope.launch {
        repository.deleteConta(conta)
    }

}
