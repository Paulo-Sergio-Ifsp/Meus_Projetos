package com.paulo.controle_gastos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paulo.controle_gastos.data.repository.FinanceRepository
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import com.paulo.controle_gastos.model.TipoConta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID // ✅✅✅ A CORREÇÃO DO 'UUID' ESTÁ AQUI ✅✅✅

data class FinanceUiState(
    val despesas: List<Despesa> = emptyList(),
    val ganhos: List<Ganho> = emptyList(),
    val contas: List<Conta> = emptyList(),
    val saldoTotal: Double = 0.0,
    val ganhosTotais: Double = 0.0,
    val despesasTotais: Double = 0.0,
    val faturasTotais: Double = 0.0,
    val displayMonth: String = "" // ✅✅✅ A CORREÇÃO DO 'displayMonth' ESTÁ AQUI ✅✅✅
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())

    init {
        viewModelScope.launch {

            combine(
                repository.despesas,
                repository.ganhos,
                repository.contas,
                _selectedDate
            ) { despesasLista, ganhosLista, contasList, selectedDate ->

                val (startTime, endTime) = getMonthBoundaries(selectedDate)
                val displayMonth = formatDisplayMonth(selectedDate)

                val mapaTipoContas = contasList.associate { it.id to it.tipo }

                val ganhosDoMes = ganhosLista.filter { it.data in startTime..endTime }
                val despesasDoMes = despesasLista.filter { it.data in startTime..endTime }

                // Ganhos (Mês) = Apenas em contas que não são Cartão
                val ganhosDashboard = ganhosDoMes
                    .filter { mapaTipoContas[it.contaId] != TipoConta.CARTAO_CREDITO }
                    .sumOf { it.valor }

                // Despesas (Mês) = Apenas em contas que não são Cartão
                val despesasDashboard = despesasDoMes
                    .filter { mapaTipoContas[it.contaId] != TipoConta.CARTAO_CREDITO }
                    .sumOf { it.valor }

                // Faturas (Mês) = Apenas despesas em contas que SÃO Cartão
                val faturasDashboard = despesasDoMes
                    .filter { mapaTipoContas[it.contaId] == TipoConta.CARTAO_CREDITO }
                    .sumOf { it.valor }

                // --- CALCULAR SALDO VIVO (All-Time) ---
                val idsContasCartao = contasList
                    .filter { it.tipo == TipoConta.CARTAO_CREDITO }
                    .map { it.id }
                    .toSet()
                val totalSaldoInicial = contasList
                    .filter { it.tipo != TipoConta.CARTAO_CREDITO }
                    .sumOf { it.saldoInicial }
                val totalGanhos = ganhosLista
                    .filter { it.contaId !in idsContasCartao }
                    .sumOf { it.valor }
                val despesasDebitadas = despesasLista
                    .filter { it.contaId !in idsContasCartao }
                    .sumOf { it.valor }
                val saldoReal = totalSaldoInicial + totalGanhos - despesasDebitadas

                FinanceUiState(
                    despesas = despesasDoMes,
                    ganhos = ganhosDoMes,
                    contas = contasList,
                    saldoTotal = saldoReal,
                    ganhosTotais = ganhosDashboard,
                    despesasTotais = despesasDashboard,
                    faturasTotais = faturasDashboard,
                    displayMonth = displayMonth
                )
            }.collect { _uiState.value = it }
        }
    }

    // --- Funções de Escrita ---
    fun addDespesa(d: Despesa) = viewModelScope.launch { repository.addDespesa(d) }
    fun addGanho(g: Ganho)     = viewModelScope.launch { repository.addGanho(g) }
    fun addConta(c: Conta)     = viewModelScope.launch { repository.addConta(c) }
    fun deleteDespesa(d: Despesa) = viewModelScope.launch { repository.deleteDespesa(d) }
    fun deleteGanho(g: Ganho)     = viewModelScope.launch { repository.deleteGanho(g) }
    fun deleteConta(c: Conta)     = viewModelScope.launch { repository.deleteConta(c) }

    // --- Pagar Fatura ---
    fun pagarFatura(contaOrigem: Conta, contaCartao: Conta, valor: Double) {
        val dataPagamento = System.currentTimeMillis()

        val despesaPagamento = Despesa(
            id = UUID.randomUUID().toString(), // ✅ AGORA FUNCIONA
            data = dataPagamento,
            local = "Pagamento Fatura ${contaCartao.nome}",
            valor = valor,
            metodoPagamento = "Transferência",
            contaId = contaOrigem.id
        )

        val ganhoPagamento = Ganho(
            id = UUID.randomUUID().toString(), // ✅ AGORA FUNCIONA
            data = dataPagamento,
            descricao = "Pagamento Fatura",
            valor = valor,
            contaId = contaCartao.id
        )

        viewModelScope.launch {
            repository.addDespesa(despesaPagamento)
            repository.addGanho(ganhoPagamento)
        }
    }

    // --- Funções para Mudar o Mês ---
    fun nextMonth() {
        _selectedDate.value = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
    }
    fun previousMonth() {
        _selectedDate.value = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
    }

    // --- Funções Auxiliares de Data ---
    private fun getMonthBoundaries(date: Calendar): Pair<Long, Long> {
        val start = (date.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return Pair(start.timeInMillis, end.timeInMillis)
    }
    private fun formatDisplayMonth(date: Calendar): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        return sdf.format(date.time).replaceFirstChar { it.titlecase() }
    }

    // --- Companion object ---
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