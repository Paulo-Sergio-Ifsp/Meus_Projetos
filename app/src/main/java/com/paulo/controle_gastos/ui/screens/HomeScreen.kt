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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    // 1. Obter o estado e todos os valores do ViewModel
    val uiState by vm.uiState.collectAsState()

    val ganhos = uiState.ganhos
    val despesas = uiState.despesas
    val saldoTotal = uiState.saldoTotal
    val ganhosTotais = uiState.ganhosTotais
    val despesasTotais = uiState.despesasTotais

    Scaffold(
        topBar = {
            // (A TopBar "Início" é gerenciada pelo MainActivity agora)
        }
    ) { padding -> // O padding vem do MainActivity

        // 2. A Lista principal
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Aplicamos o padding aqui
        ) {

            // --- 3. O DASHBOARD HEADER ---
            item {
                DashboardHeader(
                    saldoTotal = saldoTotal,
                    ganhosTotais = ganhosTotais,
                    despesasTotais = despesasTotais
                )
            }

            // --- SEÇÃO DE GANHOS ---
            if (ganhos.isNotEmpty()) {
                stickyHeader {
                    ListHeader(text = "Ganhos Recentes")
                }
                items(ganhos) { ganho ->
                    GanhoItemRow(
                        ganho = ganho,
                        onDeleteClick = { vm.deleteGanho(ganho) }
                    )
                    HorizontalDivider()
                }
            }

            // --- SEÇÃO DE DESPESAS ---
            if (despesas.isNotEmpty()) {
                stickyHeader {
                    ListHeader(text = "Despesas Recentes")
                }
                items(despesas) { despesa ->
                    DespesaItemRow(
                        despesa = despesa,
                        onDeleteClick = { vm.deleteDespesa(despesa) }
                    )
                    HorizontalDivider()
                }
            }

            // Mensagem de "Vazio" se ambas as listas estiverem vazias
            if (ganhos.isEmpty() && despesas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize() // Ocupa o espaço da LazyColumn
                            .padding(top = 100.dp), // Empurra para baixo
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum ganho ou despesa registrada.")
                    }
                }
            }
        } // Fim da LazyColumn
    } // Fim do Scaffold
} // Fim da HomeScreen

// --- TODOS OS COMPOSABLES AUXILIARES ESTÃO AQUI EMBAIXO ---

/**
 * O Header completo do Dashboard
 */
@Composable
fun DashboardHeader(
    saldoTotal: Double,
    ganhosTotais: Double,
    despesasTotais: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Card do Saldo Total ---
        Text("Saldo Total", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text(
            text = "R$ ${"%.2f".format(saldoTotal)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- 2. Barra de Progresso Ganhos vs Despesas ---
        // (Passa 'saldoTotal' para o param 'ganhos' da barra)
        CashFlowBar(ganhos = saldoTotal, despesas = despesasTotais)

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Cards de Ganhos e Despesas ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IncomeExpenseCard(
                title = "Ganhos",
                value = ganhosTotais,
                color = Color(0xFF008000) // Verde
            )
            IncomeExpenseCard(
                title = "Despesas",
                value = despesasTotais,
                color = MaterialTheme.colorScheme.error // Vermelho
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

/**
 * A barra de progresso que muda de cor (COM A LÓGICA CORRIGIDA)
 */
@Composable
fun CashFlowBar(
    ganhos: Double,
    despesas: Double
) {
    val total = ganhos + despesas

    // Se o total for 0, desenha uma barra cinza simples e sai
    if (total == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant) // Cinza (neutro)
        )
        return // Sai da função
    }

    // Se o total for > 0, calcula os pesos
    val pesoGanhos = (ganhos / total).toFloat()
    val pesoDespesas = (despesas / total).toFloat()

    // Desenha um Row (linha) com duas caixas que competem pelo espaço
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Caixa de Ganhos (SÓ desenha se o peso for > 0)
        if (pesoGanhos > 0f) {
            Box(
                modifier = Modifier
                    .weight(pesoGanhos) // O peso define a largura
                    .fillMaxHeight()
                    .background(Color(0xFF008000)) // Verde
            )
        }
        // Caixa de Despesas (SÓ desenha se o peso for > 0)
        if (pesoDespesas > 0f) {
            Box(
                modifier = Modifier
                    .weight(pesoDespesas) // O peso define a largura
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