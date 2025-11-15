package com.paulo.controle_gastos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import com.paulo.controle_gastos.model.TipoConta
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// O import do UUID não é necessário aqui

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    // 1. Obter o estado
    val uiState by vm.uiState.collectAsState()

    val ganhos = uiState.ganhos
    val despesas = uiState.despesas
    val saldoTotal = uiState.saldoTotal
    val ganhosTotais = uiState.ganhosTotais
    val despesasTotais = uiState.despesasTotais
    val faturasTotais = uiState.faturasTotais
    val displayMonth = uiState.displayMonth // ✅✅✅ A CORREÇÃO ESTÁ AQUI ✅✅✅

    val mapaContas = uiState.contas.associate { it.id to it.tipo }

    Scaffold(
        topBar = {
            // (Gerenciado pelo MainActivity)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // --- 3. O DASHBOARD HEADER ---
            item {
                DashboardHeader(
                    saldoTotal = saldoTotal,
                    ganhosTotais = ganhosTotais,
                    despesasTotais = despesasTotais,
                    faturasTotais = faturasTotais,

                    displayMonth = displayMonth, // ✅ AGORA FUNCIONA
                    onNextMonth = { vm.nextMonth() },
                    onPrevMonth = { vm.previousMonth() }
                )
            }

            // --- SEÇÃO DE GANHOS (APENAS DINHEIRO) ---
            val ganhosDoMes = ganhos.filter { (mapaContas[it.contaId] ?: TipoConta.CONTA_CORRENTE) != TipoConta.CARTAO_CREDITO }
            if (ganhosDoMes.isNotEmpty()) {
                stickyHeader {
                    ListHeader(text = "Ganhos do Mês (Dinheiro)")
                }
                items(ganhosDoMes) { ganho ->
                    GanhoItemRow(
                        ganho = ganho,
                        onDeleteClick = { vm.deleteGanho(ganho) }
                    )
                    HorizontalDivider()
                }
            }

            // --- SEÇÃO DE DESPESAS (APENAS DINHEIRO) ---
            val despesasDoMes = despesas.filter { (mapaContas[it.contaId] ?: TipoConta.CONTA_CORRENTE) != TipoConta.CARTAO_CREDITO }
            if (despesasDoMes.isNotEmpty()) {
                stickyHeader {
                    ListHeader(text = "Despesas do Mês (Dinheiro)")
                }
                items(despesasDoMes) { despesa ->
                    DespesaItemRow(
                        despesa = despesa,
                        onDeleteClick = { vm.deleteDespesa(despesa) }
                    )
                    HorizontalDivider()
                }
            }

            // --- NOVA SEÇÃO: DESPESAS DE CARTÃO ---
            val faturasDoMes = despesas.filter { (mapaContas[it.contaId] ?: TipoConta.CONTA_CORRENTE) == TipoConta.CARTAO_CREDITO }
            if (faturasDoMes.isNotEmpty()) {
                stickyHeader {
                    ListHeader(text = "Compras no Cartão (Mês)")
                }
                items(faturasDoMes) { despesa ->
                    DespesaItemRow(
                        despesa = despesa,
                        onDeleteClick = { vm.deleteDespesa(despesa) }
                    )
                    HorizontalDivider()
                }
            }

            // Mensagem de "Vazio"
            if (ganhos.isEmpty() && despesas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum ganho ou despesa neste mês.")
                    }
                }
            }
        } // Fim da LazyColumn
    } // Fim do Scaffold
} // Fim da HomeScreen

// --- TODOS OS COMPOSABLES AUXILIARES ESTÃO AQUI EMBAIXO ---

/**
 * O seletor de mês
 */
@Composable
fun MonthSelector(
    displayMonth: String,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Mês Anterior")
        }
        Text(
            text = displayMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Próximo Mês")
        }
    }
}


/**
 * O Header completo do Dashboard (Atualizado)
 */
@Composable
fun DashboardHeader(
    saldoTotal: Double,
    ganhosTotais: Double,
    despesasTotais: Double,
    faturasTotais: Double, // ✅ NOVO PARÂMETRO
    displayMonth: String,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Seletor de Mês ---
        MonthSelector(
            displayMonth = displayMonth,
            onNextMonth = onNextMonth,
            onPrevMonth = onPrevMonth
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Card do Saldo Total ---
        Text("Saldo Total (Geral)", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text(
            text = "R$ ${"%.2f".format(saldoTotal)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- 3. Barra de Progresso (Ganhos vs Despesas em DINHEIRO) ---
        CashFlowBar(ganhos = ganhosTotais, despesas = despesasTotais)

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Cards de Ganhos, Despesas, e Faturas (do Mês) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IncomeExpenseCard(
                title = "Ganhos (Mês)",
                value = ganhosTotais,
                color = Color(0xFF008000) // Verde
            )
            IncomeExpenseCard(
                title = "Despesas (Mês)",
                value = despesasTotais,
                color = MaterialTheme.colorScheme.error // Vermelho
            )
            IncomeExpenseCard(
                title = "Faturas (Mês)",
                value = faturasTotais,
                color = Color.Gray // Cor neutra para faturas
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

/**
 * A barra de progresso
 */
@Composable
fun CashFlowBar(
    ganhos: Double,
    despesas: Double
) {
    val total = ganhos + despesas

    if (total == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant) // Cinza (neutro)
        )
        return
    }

    val pesoGanhos = (ganhos / total).toFloat()
    val pesoDespesas = (despesas / total).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (pesoGanhos > 0f) {
            Box(
                modifier = Modifier
                    .weight(pesoGanhos)
                    .fillMaxHeight()
                    .background(Color(0xFF008000)) // Verde
            )
        }
        if (pesoDespesas > 0f) {
            Box(
                modifier = Modifier
                    .weight(pesoDespesas)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error) // Vermelho
            )
        }
    }
}

/**
 * Um Card pequeno para Ganhos ou Despesas
 */
@Composable
fun IncomeExpenseCard(title: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelMedium)
        Text(
            text = "R$ ${"%.2f".format(value)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Composable auxiliar para o cabeçalho das listas
 */
@Composable
fun ListHeader(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Helper para formatar a data (Long) para "dd/MM/yyyy"
 */
@Composable
private fun formatarData(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Um Composable para mostrar uma linha de Ganho
 */
@Composable
fun GanhoItemRow(
    ganho: Ganho,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ganho.descricao,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatarData(ganho.data),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = "+ R$ ${"%.2f".format(ganho.valor)}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF008000), // Verde
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir Ganho",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Um Composable para mostrar uma linha de Despesa
 */
@Composable
fun DespesaItemRow(
    despesa: Despesa,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = despesa.local,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatarData(despesa.data),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = "- R$ ${"%.2f".format(despesa.valor)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error, // Vermelho
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir Despesa",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}