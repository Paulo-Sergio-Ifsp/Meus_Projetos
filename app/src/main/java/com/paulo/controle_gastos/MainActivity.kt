package com.paulo.controle_gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding // ✅ IMPORTADO
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // ✅ IMPORTADO
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // ✅ IMPORTADO
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier // ✅ IMPORTADO
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState // ✅ IMPORTADO
import androidx.navigation.compose.rememberNavController
import com.paulo.controle_gastos.ui.components.MultiActionFab
import com.paulo.controle_gastos.ui.navigation.AppNavHost
import com.paulo.controle_gastos.ui.navigation.Dest
import com.paulo.controle_gastos.ui.theme.Controle_GastosTheme
import com.paulo.controle_gastos.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

      Controle_GastosTheme {

        // ✅ ViewModel e Repository (Seu código estava OK)
        val app = application as FinanceApp
        val vm: FinanceViewModel =
          viewModel(factory = FinanceViewModel.provideFactory(app.repository))

        // --- CORREÇÕES AQUI ---

        // 1. Definir o NavController
        val nav = rememberNavController()

        // 2. Ler a rota atual (como você tentou fazer)
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // (A variável 'var current' foi removida, não precisamos dela)

        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                // Define o título baseado na rota atual
                Text(
                  when (currentRoute) {
                    Dest.Home.route -> "Início"
                    Dest.Contas.route -> "Contas"
                    Dest.AddAccount.route -> "Adicionar Conta"
                    Dest.AddGanho.route -> "Adicionar Ganho"
                    Dest.AddDespesa.route -> "Adicionar Despesa"
                    else -> "Controle de Gastos"
                  }
                )
              },
              // Adiciona um botão "Voltar" nas telas internas
              navigationIcon = {
                if (currentRoute != Dest.Home.route && currentRoute != Dest.Contas.route) {
                  IconButton(onClick = { nav.popBackStack() }) { // Usa 'nav'
                    Icon(Icons.Default.ArrowBack, "Voltar")
                  }
                }
              }
            )
          },
          bottomBar = {
            // Esconde a BottomBar se não estivermos na Home ou Contas
            if (currentRoute == Dest.Home.route || currentRoute == Dest.Contas.route) {
              NavigationBar {

                NavigationBarItem(
                  selected = currentRoute == Dest.Home.route, // Usa 'currentRoute'
                  onClick = {
                    nav.navigate(Dest.Home.route) { // Usa 'nav'
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.Home, null) },
                  label = { Text("Início") }
                )

                NavigationBarItem(
                  selected = currentRoute == Dest.Contas.route, // Usa 'currentRoute'
                  onClick = {
                    nav.navigate(Dest.Contas.route) { // Usa 'nav'
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.List, null) },
                  label = { Text("Contas") }
                )
              }
            }
          },
          floatingActionButton = {
            // Esconde o FAB se não estivermos na Home ou Contas
            if (currentRoute == Dest.Home.route || currentRoute == Dest.Contas.route) {
              MultiActionFab(
                onAddContaClick = {
                  nav.navigate(Dest.AddAccount.route) // Usa 'nav'
                },
                onAddGanhoClick = {
                  nav.navigate(Dest.AddGanho.route) // Usa 'nav'
                },
                onAddDespesaClick = {
                  nav.navigate(Dest.AddDespesa.route) // Usa 'nav'
                }
              )
            }
          }
        ) { padding ->
          AppNavHost(
            navController = nav, // Passa 'nav'
            vm = vm,
            // 3. Aplica o padding ao conteúdo da tela
            modifier = Modifier.padding(padding) //
          )
        }
      }
    }
  }
}