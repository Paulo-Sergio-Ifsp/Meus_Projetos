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
import com.paulo.controle_gastos.model.Conta //
import com.paulo.controle_gastos.model.Despesa //
import com.paulo.controle_gastos.viewmodel.FinanceViewModel //
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDespesaScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    // --- 1. Obter dados do ViewModel ---
    val uiState by vm.uiState.collectAsState()
    val contas = uiState.contas // Nossa lista de contas cadastradas

    // --- 2. Criar Estados para os campos do formulário ---
    var local by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    // Lista de métodos de pagamento (você pode alterar)
    val metodosPagamento = listOf("Cartão", "Dinheiro", "Pix", "Débito")
    var metodoSelecionado by remember { mutableStateOf(metodosPagamento.first()) }
    var metodoExpanded by remember { mutableStateOf(false) }

    // Estados para o Dropdown de Contas
    var contaExpanded by remember { mutableStateOf(false) }
    var contaSelecionada by remember { mutableStateOf<Conta?>(null) }

    LaunchedEffect(contas) {
        if (contaSelecionada == null && contas.isNotEmpty()) {
            contaSelecionada = contas.first()
        }
    }

    // --- 3. Construir a UI (SEM SCAFFOLD) ---
    Column(
        modifier = Modifier
            .padding(16.dp) // O padding vem do AppNavHost no MainActivity
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

        // --- Dropdown de Método de Pagamento ---
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
                metodosPagamento.forEach { metodo ->
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

        // --- Dropdown de Contas (de onde saiu o dinheiro) ---
        ExposedDropdownMenuBox(
            expanded = contaExpanded,
            onExpandedChange = { contaExpanded = !contaExpanded }
        ) {
            OutlinedTextField(
                value = contaSelecionada?.nome ?: "Selecione uma conta", //
                onValueChange = {},
                readOnly = true,
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
                contas.forEach { conta ->
                    DropdownMenuItem(
                        text = { Text(conta.nome) }, //
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
                // 4. Criar o objeto Despesa e salvar
                val novaDespesa = Despesa(
                    id = UUID.randomUUID().toString(),
                    data = System.currentTimeMillis(),
                    local = local,
                    valor = valor.toDoubleOrNull() ?: 0.0,
                    metodoPagamento = metodoSelecionado,
                    contaId = contaSelecionada!!.id
                ) //

                vm.addDespesa(novaDespesa) // ✅ Chamando a função do ViewModel
                nav.popBackStack() // Volta para a tela anterior
            },
            enabled = local.isNotBlank()
                    && valor.isNotBlank()
                    && contaSelecionada != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Despesa")
        }
    }
}