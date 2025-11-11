package com.paulo.controle_gastos.ui.screens

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
import com.paulo.controle_gastos.model.Conta // Importa seu modelo de Conta
import com.paulo.controle_gastos.viewmodel.FinanceViewModel //

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContasScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    val uiState by vm.uiState.collectAsState()

    LazyColumn {
        items(uiState.contas) { conta ->
            ContaItem(
                conta = conta,
                onDelete = { vm.deleteConta(conta) }
            )
        }
    }

}

@Composable
fun ContaItem(conta: Conta, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(conta.nome, fontWeight = FontWeight.Bold)
                Text(conta.tipo.name.replace("_", " "))
            }
            Text("R$ %.2f".format(conta.saldoInicial))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

// Um Composable reutilizável para mostrar uma linha de conta
@Composable
fun ContaItemRow(
    conta: Conta,
    onDeleteClick: () -> Unit // Ação de clique para exclusão
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coluna para o Nome e o Saldo
        Column(
            modifier = Modifier.weight(1f), // Ocupa todo o espaço disponível
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conta.nome, //
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                // Formata o saldo (Double) para R$ 0,00
                text = "Saldo: R$ ${"%.2f".format(conta.saldoInicial)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Ícone de Lixeira (Botão de Excluir)
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir Conta",
                tint = MaterialTheme.colorScheme.error
            )
        }

    }
}