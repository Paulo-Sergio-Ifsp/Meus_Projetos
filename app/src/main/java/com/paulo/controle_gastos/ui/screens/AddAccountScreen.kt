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
// Imports do Scaffold e TopAppBar removidos
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalFocusManager (não estava a ser usado)
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Conta //
import com.paulo.controle_gastos.model.TipoConta
import com.paulo.controle_gastos.viewmodel.FinanceViewModel //
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    // val focus = LocalFocusManager.current // Não estava a ser usado

    var nome by remember { mutableStateOf("") }
    var saldoInicial by remember { mutableStateOf("") }
    var tipoSelecionado by remember { mutableStateOf(TipoConta.CONTA_CORRENTE) }
    var expanded by remember { mutableStateOf(false) }

    // --- O Scaffold foi REMOVIDO daqui ---

    // A Column agora é o Composable principal
    Column(
        modifier = Modifier
            // .padding(padding) // Removido, pois 'padding' vinha do Scaffold
            .padding(16.dp)     // Mantivemos o padding de 16.dp
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top //
    ) {

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome da Conta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = saldoInicial,
            onValueChange = { saldoInicial = it },
            label = { Text("Saldo Inicial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = tipoSelecionado.name.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Tipo da Conta") }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TipoConta.values().forEach { tipo ->
                    DropdownMenuItem(
                        onClick = {
                            tipoSelecionado = tipo
                            expanded = false
                        },
                        text = { Text(tipo.name.replace("_", " ")) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val novaConta = Conta(
                    id = UUID.randomUUID().toString(),
                    nome = nome,
                    saldoInicial = saldoInicial.toDoubleOrNull() ?: 0.0,
                    tipo = tipoSelecionado
                ) //
                vm.addConta(novaConta) //
                nav.popBackStack() // Adicionado para voltar após salvar
            },
            enabled = nome.isNotBlank() && saldoInicial.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Salvar")
        }
    }
}