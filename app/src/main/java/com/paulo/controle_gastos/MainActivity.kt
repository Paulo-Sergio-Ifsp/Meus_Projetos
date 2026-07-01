package com.paulo.controle_gastos

// ✅ VERIFIQUE ESTES IMPORTS
// ✅ ESTE É O IMPORT MAIS IMPORTANTE
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
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

        // ✅ ViewModel e Repository
        val app = application as FinanceApp

        // ✅ LINHA 40: A chamada que estava falhando
        // Ela usa o 'viewModel' importado e o 'FinanceViewModel' importado
        val vm: FinanceViewModel =
          viewModel(factory = FinanceViewModel.provideFactory(app.repository))

        val nav = rememberNavController()
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
          topBar = {
            if (currentRoute != Dest.Login.route) {
              TopAppBar(
                title = {
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
                navigationIcon = {
                  if (currentRoute != Dest.Home.route && currentRoute != Dest.Contas.route) {
                    IconButton(onClick = { nav.popBackStack() }) {
                      Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                  }
                },
                actions = {
                  val activity = LocalContext.current as? Activity
                  IconButton(onClick = { activity?.finishAffinity() }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                      contentDescription = "Sair do App"
                    )
                  }
                }
              )
            }
          },
          bottomBar = {
            if (currentRoute == Dest.Home.route || currentRoute == Dest.Contas.route) {
              NavigationBar {
                NavigationBarItem(
                  selected = currentRoute == Dest.Home.route,
                  onClick = {
                    nav.navigate(Dest.Home.route) {
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.Home, null) },
                  label = { Text("Início") }
                )
                NavigationBarItem(
                  selected = currentRoute == Dest.Contas.route,
                  onClick = {
                    nav.navigate(Dest.Contas.route) {
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                  label = { Text("Contas") }
                )
              }
            }
          },
          floatingActionButton = {
            if (currentRoute == Dest.Home.route || currentRoute == Dest.Contas.route) {
              MultiActionFab(
                onAddContaClick = { nav.navigate(Dest.AddAccount.route) },
                onAddGanhoClick = { nav.navigate(Dest.AddGanho.route) },
                onAddDespesaClick = { nav.navigate(Dest.AddDespesa.route) }
              )
            }
          }
        ) { padding ->
          AppNavHost(
            navController = nav,
            vm = vm,
            modifier = Modifier.padding(padding)
          )
        }
      }
    }
  }
}