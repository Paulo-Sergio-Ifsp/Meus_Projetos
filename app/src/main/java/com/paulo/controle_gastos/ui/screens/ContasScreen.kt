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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.TipoConta
import com.paulo.controle_gastos.util.FormatUtils
import com.paulo.controle_gastos.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContasScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    val uiState by vm.uiState.collectAsState()

    val contas = uiState.contas
    val ganhos = uiState.ganhos
    val despesas = uiState.despesas

    var contaParaPagar by remember { mutableStateOf<Conta?>(null) }
    var valorAPagar by remember { mutableStateOf(0.0) }

    if (contaParaPagar != null && valorAPagar > 0) {
        PagarFaturaDialog(
            fatura = valorAPagar,
            contaCartao = contaParaPagar!!,
            contasDeDebito = contas.filter { it.tipo != TipoConta.CARTAO_CREDITO },
            onDismiss = { contaParaPagar = null },
            onConfirm = { contaOrigem ->
                vm.pagarFatura(contaOrigem, contaParaPagar!!, valorAPagar)
                contaParaPagar = null
            }
        )
    }

    Scaffold(
        topBar = {
            // (Gerenciado pelo MainActivity)
        }
    ) { padding ->

        if (contas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma conta cadastrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(contas) { conta ->

                    // --- ✅ LÓGICA CORRETA DE CÁLCULO ---

                    val ganhosDaConta = ganhos
                        .filter { it.contaId == conta.id }
                        .sumOf { it.valor }

                    val despesasDaConta = despesas
                        .filter { it.contaId == conta.id }
                        .sumOf { it.valor }

                    val saldoAtual = if (conta.tipo == TipoConta.CARTAO_CREDITO) {
                        // Fatura = (Saldo Inicial) + Despesas - Ganhos (Pagamentos)
                        conta.saldoInicial + despesasDaConta - ganhosDaConta
                    } else {
                        // Saldo = (Saldo Inicial) + Ganhos - Despesas
                        conta.saldoInicial + ganhosDaConta - despesasDaConta
                    }

                    ContaItemRow(
                        conta = conta,
                        saldoAtual = saldoAtual,
                        onEditClick = {
                            nav.navigate("edit_account/${conta.id}")
                        },
                        onDeleteClick = {
                            vm.deleteConta(conta)
                        },
                        onPayClick = {
                            contaParaPagar = conta
                            valorAPagar = saldoAtual
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}


@Composable
fun ContaItemRow(
    conta: Conta,
    saldoAtual: Double,
    onEditClick: () -> Unit,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conta.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // --- ✅ LÓGICA DE TEXTO E COR CORRETA (USANDO FormatUtils) ---

            val (textoSaldo, corSaldo) = if (conta.tipo == TipoConta.CARTAO_CREDITO) {
                // Para cartão, o saldo é o que você DEVE (Fatura)
                "Fatura: ${FormatUtils.formatCurrency(saldoAtual)}" to
                        if (saldoAtual > 0) MaterialTheme.colorScheme.error else Color.Gray
            } else {
                // Para contas normais, o saldo é o que você TEM
                "Saldo: ${FormatUtils.formatCurrency(saldoAtual)}" to
                        if (saldoAtual < 0) MaterialTheme.colorScheme.error else Color.Gray
            }

            Text(
                text = textoSaldo,
                style = MaterialTheme.typography.bodyMedium,
                color = corSaldo
            )
        }

        if (conta.tipo == TipoConta.CARTAO_CREDITO && saldoAtual > 0) {
            IconButton(onClick = onPayClick) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = "Pagar Fatura",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar Conta",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir Conta",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagarFaturaDialog(
    fatura: Double,
    contaCartao: Conta,
    contasDeDebito: List<Conta>,
    onDismiss: () -> Unit,
    onConfirm: (contaOrigem: Conta) -> Unit
) {
    var contaOrigemSelecionada by remember { mutableStateOf(contasDeDebito.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar Fatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pagar fatura de ${FormatUtils.formatCurrency(fatura)} do cartão ${contaCartao.nome}?")

                // Dropdown para selecionar a conta de origem
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = contaOrigemSelecionada?.nome ?: "Selecione a conta",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Pagar com:") }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        contasDeDebito.forEach { conta ->
                            DropdownMenuItem(
                                // Mostra o saldo da conta de origem para o usuário saber
                                text = {
                                    // Precisamos calcular o saldo vivo da conta de débito aqui
                                    // (Simplificado por agora, mostra apenas o nome)
                                    // TODO: Calcular o saldo vivo real da conta de débito
                                    Text(conta.nome)
                                },
                                onClick = {
                                    contaOrigemSelecionada = conta
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (contaOrigemSelecionada != null) {
                        onConfirm(contaOrigemSelecionada!!)
                    }
                },
                enabled = contaOrigemSelecionada != null
            ) {
                Text("Confirmar Pagamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}