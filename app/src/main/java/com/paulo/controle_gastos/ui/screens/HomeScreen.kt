package com.paulo.controle_gastos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi // Para stickyHeader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Despesa //
import com.paulo.controle_gastos.model.Ganho //
import com.paulo.controle_gastos.viewmodel.FinanceViewModel //
import java.text.SimpleDateFormat // Para formatarData
import java.util.Date // Para formatarData
import java.util.Locale // Para formatarData

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.filled.Add

import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import com.paulo.controle_gastos.ui.navigation.Dest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    val state = vm.uiState.collectAsState().value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nav.navigate(Dest.AddAccount.route) // ✅ Navega para adicionar conta
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            item {
                Text("Ganhos Recentes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(state.ganhos) { ganho ->
                GanhoItem(
                    ganho = ganho,
                    onDelete = { vm.deleteGanho(ganho) }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Despesas Recentes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(state.despesas) { despesa ->
                DespesaItem(
                    despesa = despesa,
                    onDelete = { vm.deleteDespesa(despesa) }
                )
            }
        }
    }
}

@Composable
fun GanhoItem(ganho: Ganho, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ganho.descricao, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("+R$ %.2f".format(ganho.valor))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
fun DespesaItem(despesa: Despesa, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(despesa.local, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("-R$ %.2f".format(despesa.valor))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

// [No seu arquivo HomeScreen.kt ou em um novo]

// Helper para formatar a data (Long) para "dd/MM/yyyy"
@Composable
private fun formatarData(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

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
                text = ganho.descricao, //
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatarData(ganho.data), //
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = "+ R$ ${"%.2f".format(ganho.valor)}", //
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

// [No seu arquivo HomeScreen.kt ou em um novo]

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
                text = despesa.local, //
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatarData(despesa.data), //
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = "- R$ ${"%.2f".format(despesa.valor)}", //
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