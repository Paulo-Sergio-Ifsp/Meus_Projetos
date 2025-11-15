package com.paulo.controle_gastos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.TipoConta
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import java.util.UUID

// ✅ NOVO: Um mapa para ligar o Tipo de Conta ao Método de Pagamento
private val metodoParaTipoConta = mapOf(
    "Dinheiro" to listOf(TipoConta.CARTEIRA),
    "Débito" to listOf(TipoConta.CONTA_CORRENTE, TipoConta.POUPANCA),
    "Pix" to listOf(TipoConta.CONTA_CORRENTE, TipoConta.POUPANCA),
    "Cartão de Crédito" to listOf(TipoConta.CARTAO_CREDITO)
)
private val metodosDePagamento = listOf("Cartão de Crédito", "Débito", "Pix", "Dinheiro")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDespesaScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    // --- 1. Obter dados do ViewModel ---
    val uiState by vm.uiState.collectAsState()
    val todasAsContas = uiState.contas

    // --- 2. Estados do formulário ---
    var local by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    // --- ✅ 3. LÓGICA ATUALIZADA (O SEU DESIGN) ---

    // Dropdown 1: Método de Pagamento
    var metodoSelecionado by remember { mutableStateOf(metodosDePagamento.first()) }
    var metodoExpanded by remember { mutableStateOf(false) }

    // Dropdown 2: Conta
    var contasFiltradas by remember { mutableStateOf(emptyList<Conta>()) }
    var contaSelecionada by remember { mutableStateOf<Conta?>(null) }
    var contaExpanded by remember { mutableStateOf(false) }

    // --- Efeito que liga os dois dropdowns ---
    LaunchedEffect(metodoSelecionado, todasAsContas) {
        // 1. Filtra a lista de contas com base no método
        val tiposPermitidos = metodoParaTipoConta[metodoSelecionado] ?: emptyList()
        contasFiltradas = todasAsContas.filter { it.tipo in tiposPermitidos }

        // 2. Auto-seleciona a primeira conta da nova lista
        contaSelecionada = contasFiltradas.firstOrNull()
    }

    // --- 4. UI (SEM SCAFFOLD) ---
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- Campo Local ---
        OutlinedTextField(
            value = local,
            onValueChange = { local = it },
            label = { Text("Local (ex: Supermercado)") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Campo Valor ---
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("R$ ") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Dropdown 1: Método de Pagamento (O Controlador) ---
        ExposedDropdownMenuBox(
            expanded = metodoExpanded,
            onExpandedChange = { metodoExpanded = !metodoExpanded }
        ) {
            OutlinedTextField(
                value = metodoSelecionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = metodoExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Método de Pagamento") }
            )

            ExposedDropdownMenu(
                expanded = metodoExpanded,
                onDismissRequest = { metodoExpanded = false }
            ) {
                metodosDePagamento.forEach { metodo ->
                    DropdownMenuItem(
                        text = { Text(metodo) },
                        onClick = {
                            metodoSelecionado = metodo
                            metodoExpanded = false
                        }
                    )
                }
            }
        }

        // --- Dropdown 2: Contas (O Controlado) ---
        ExposedDropdownMenuBox(
            expanded = contaExpanded,
            onExpandedChange = { contaExpanded = !contaExpanded }
        ) {
            OutlinedTextField(
                // Mostra a conta selecionada ou um aviso se a lista estiver vazia
                value = contaSelecionada?.nome ?: "Nenhuma conta para este método",
                onValueChange = {},
                readOnly = true,
                // Desativa o dropdown se não houver contas
                enabled = contasFiltradas.isNotEmpty(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = contaExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Conta (Origem)") }
            )

            ExposedDropdownMenu(
                expanded = contaExpanded,
                onDismissRequest = { contaExpanded = false }
            ) {
                contasFiltradas.forEach { conta ->
                    DropdownMenuItem(
                        text = { Text(conta.nome) },
                        onClick = {
                            contaSelecionada = conta
                            contaExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Botão Salvar ---
        Button(
            onClick = {
                val novaDespesa = Despesa(
                    id = UUID.randomUUID().toString(),
                    data = System.currentTimeMillis(),
                    local = local,
                    valor = valor.toDoubleOrNull() ?: 0.0,
                    metodoPagamento = metodoSelecionado, // ✅ Usa o método correto
                    contaId = contaSelecionada!!.id // ✅ Usa a conta correta
                )

                vm.addDespesa(novaDespesa)
                nav.popBackStack()
            },
            // Só ativa se todos os campos estiverem preenchidos E uma conta válida selecionada
            enabled = local.isNotBlank()
                    && valor.isNotBlank()
                    && contaSelecionada != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Despesa")
        }
    }
}