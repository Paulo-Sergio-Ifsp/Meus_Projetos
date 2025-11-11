package com.paulo.controle_gastos.ui.components // (ou onde você preferir)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun MultiActionFab(
    onAddDespesaClick: () -> Unit,
    onAddGanhoClick: () -> Unit,
    onAddContaClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Menu expandido (só aparece se isExpanded = true)
        AnimatedVisibility(visible = isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabItem(text = "Conta",   icon = Icons.Default.Home) { onAddContaClick() }
                FabItem(text = "Ganho",   icon = Icons.Default.MonetizationOn) { onAddGanhoClick() }
                FabItem(text = "Despesa", icon = Icons.Default.Create) { onAddDespesaClick() }
            }
        }

        // Botão FAB Principal
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar"
            )
        }
    }
}

// Pequeno Composable auxiliar para os itens do menu
@Composable
private fun FabItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text)
        SmallFloatingActionButton(
            onClick = onClick,
        ) {
            Icon(imageVector = icon, contentDescription = text)
        }
    }
}