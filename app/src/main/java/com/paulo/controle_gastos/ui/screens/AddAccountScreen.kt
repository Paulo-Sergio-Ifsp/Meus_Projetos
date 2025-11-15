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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.TipoConta // ✅ IMPORT CORRETO
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    nav: NavController,
    vm: FinanceViewModel
) {
    var nome by remember { mutableStateOf("") }
    var saldoInicial by remember { mutableStateOf("") }
    var tipoSelecionado by remember { mutableStateOf(TipoConta.CONTA_CORRENTE) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
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
                // ✅ Lê o Enum TipoConta.kt
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
                    // Se for cartão, o saldo inicial deve ser 0 (ou o valor da fatura)
                    // Vamos forçar 0 por enquanto para simplificar
                    saldoInicial = if(tipoSelecionado == TipoConta.CARTAO_CREDITO) 0.0 else saldoInicial.toDoubleOrNull() ?: 0.0,
                    tipo = tipoSelecionado
                )
                vm.addConta(novaConta)
                nav.popBackStack() // Volta para a tela anterior
            },
            enabled = nome.isNotBlank() && (saldoInicial.isNotBlank() || tipoSelecionado == TipoConta.CARTAO_CREDITO),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Salvar")
        }
    }
}