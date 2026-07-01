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
import com.paulo.controle_gastos.model.Ganho
import com.paulo.controle_gastos.util.FormatUtils
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGanhoScreen(
    nav: NavController,
    vm: FinanceViewModel,
    editId: String? = null
) {
    // --- 1. Obter dados do ViewModel ---
    val uiState by vm.uiState.collectAsState()
    val contas = uiState.contas

    // --- 2. Estados do formulário ---
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var dataOriginal by remember { mutableStateOf(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }
    var contaSelecionada by remember { mutableStateOf<Conta?>(null) }

    LaunchedEffect(editId, uiState.ganhos) {
        if (editId != null) {
            val ganhoParaEditar = uiState.ganhos.find { it.id == editId }
            ganhoParaEditar?.let {
                descricao = it.descricao
                valor = it.valor.toString()
                dataOriginal = it.data
                contaSelecionada = contas.find { c -> c.id == it.contaId }
            }
        } else if (contaSelecionada == null && contas.isNotEmpty()) {
            contaSelecionada = contas.first()
        }
    }

    // Validação: habilita botão somente se valor parseável e descrição + conta válidas
    val parsedValor = FormatUtils.parseUserDecimal(valor)
    val salvarHabilitado = descricao.isNotBlank() && !valor.isBlank() && parsedValor != null && contaSelecionada != null

    // --- 3. Construir a UI (SEM SCAFFOLD) ---
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- Campo Descrição ---
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição (ex: Salário)") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Campo Valor ---
        OutlinedTextField(
            value = valor,
            onValueChange = { input ->
                // Aceita entrada livre; fazemos parsing robusto ao salvar
                valor = input
            },
            label = { Text("Valor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("R$ ") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Dropdown de Contas ---
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = contaSelecionada?.nome ?: "Selecione uma conta",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Conta de Destino") }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (contas.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Cadastre uma conta primeiro") },
                        onClick = { expanded = false }
                    )
                }
                contas.forEach { conta ->
                    DropdownMenuItem(
                        text = { Text(conta.nome) },
                        onClick = {
                            contaSelecionada = conta
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Botão Salvar ---
        Button(
            onClick = {
                val amount = FormatUtils.parseUserDecimal(valor) ?: 0.0
                val ganho = Ganho(
                    id = editId ?: UUID.randomUUID().toString(),
                    data = dataOriginal,
                    descricao = descricao.trim(),
                    valor = amount,
                    contaId = contaSelecionada!!.id
                )

                vm.addGanho(ganho)
                nav.popBackStack() // Volta para a tela anterior
            },
            enabled = salvarHabilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editId == null) "Salvar Ganho" else "Atualizar Ganho")
        }
    }
}