package com.paulo.controle_gastos.ui.screens

// Imports necessários
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.viewmodel.FinanceViewModel

/**
 * A Tela principal que mostra a lista de contas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContasScreen(
    nav: NavController,
    vm: FinanceViewModel // Você já está recebendo o ViewModel
) {
    // 1. Coletar o ESTADO COMPLETO do ViewModel
    val uiState by vm.uiState.collectAsState()

    // 2. Pegar TODAS as listas
    val contas = uiState.contas // Pega a lista de contas
    val ganhos = uiState.ganhos // Pega a lista de TODOS os ganhos
    val despesas = uiState.despesas // Pega a lista de TODAS as despesas

    Scaffold(
        topBar = {
            // (A TopBar "Contas" é gerenciada pelo MainActivity agora)
        }
    ) { padding ->

        // 3. Verificar se a lista está vazia
        if (contas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma conta cadastrada.")
            }
        } else {
            // 4. Se NÃO estiver vazia, mostre a LazyColumn (lista)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // items() é a forma do Compose de criar a lista
                items(contas) { conta ->

                    // --- AQUI ESTÁ A NOVA LÓGICA ---
                    // Para CADA conta na lista, calculamos seu saldo individual

                    // 5. Soma todos os ganhos ONDE o contaId == conta.id
                    val ganhosDaConta = ganhos
                        .filter { it.contaId == conta.id }
                        .sumOf { it.valor }

                    // 6. Soma todas as despesas ONDE o contaId == conta.id
                    //    (e que não sejam "Cartão")
                    val despesasDaConta = despesas
                        .filter { it.contaId == conta.id && it.metodoPagamento != "Cartão" }
                        .sumOf { it.valor }

                    // 7. Calcula o Saldo Atual VIVO
                    val saldoAtual = conta.saldoInicial + ganhosDaConta - despesasDaConta

                    // 8. Passamos o saldoAtual para o Composable da linha
                    ContaItemRow(
                        conta = conta,
                        saldoAtual = saldoAtual, // Passando o saldo vivo
                        onDeleteClick = {
                            vm.deleteConta(conta)
                        }
                    )
                    HorizontalDivider() // Adiciona uma linha divisória
                }
            }
        }
    }
}


/**
 * Um Composable reutilizável para mostrar uma linha de conta
 * (Modificado para aceitar 'saldoAtual')
 */
@Composable
fun ContaItemRow(
    conta: Conta,
    saldoAtual: Double, // <-- MUDANÇA AQUI
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
                text = conta.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // --- MUDANÇA AQUI ---
            Text(
                // Mostra o saldoAtual, não o saldoInicial
                text = "Saldo: R$ ${"%.2f".format(saldoAtual)}",
                style = MaterialTheme.typography.bodyMedium,
                // Muda a cor se o saldo for negativo
                color = if (saldoAtual < 0) MaterialTheme.colorScheme.error else Color.Gray
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