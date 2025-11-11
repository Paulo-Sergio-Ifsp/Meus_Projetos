package com.paulo.controle_gastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.paulo.controle_gastos.ui.screens.AddAccountScreen
import com.paulo.controle_gastos.ui.screens.ContasScreen
import com.paulo.controle_gastos.ui.screens.HomeScreen
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import com.paulo.controle_gastos.ui.screens.AddDespesaScreen // ✅ ADICIONE ESTA LINHA
import com.paulo.controle_gastos.ui.screens.AddGanhoScreen   // ✅ ADICIONE ESTA LINHA
import androidx.compose.ui.Modifier // ✅ ADICIONE ESTE IMPORT

// ✅ Todas as rotas do app
sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Contas : Dest("contas")
    data object AddAccount : Dest("add_account")
    data object AddDespesa : Dest("add_despesa") // ✅ NOVA ROTA
    data object AddGanho : Dest("add_ganho")     // ✅ NOVA ROTA
}

// ✅ Aqui é o NavHost COMPLETO
@Composable
fun AppNavHost(
    navController: NavHostController,
    vm: FinanceViewModel,
    modifier: Modifier = Modifier // ✅ 1. ADICIONE O MODIFIER AQUI
) {
    NavHost(
        navController = navController,
        startDestination = Dest.Home.route,
        modifier = modifier // ✅ 2. PASSE O MODIFIER PARA O NavHost
    ) {

        // ✅ Tela Home
        composable(Dest.Home.route) {
            HomeScreen(nav = navController, vm = vm)
        }

        // ✅ Tela Contas
        composable(Dest.Contas.route) {
            ContasScreen(nav = navController, vm = vm)
        }

        // ✅ Tela AddAccount
        composable(Dest.AddAccount.route) {
            AddAccountScreen(nav = navController, vm = vm)

        }
        // ✅ Tela AddAccount
        composable(Dest.AddAccount.route) {
            AddAccountScreen(nav = navController, vm = vm)
        }

        // ✅ Tela AddDespesa
        composable(Dest.AddDespesa.route) {
            AddDespesaScreen(nav = navController, vm = vm)
        }

        // ✅ Tela AddGanho
        composable(Dest.AddGanho.route) {
            AddGanhoScreen(nav = navController, vm = vm)
        }
    }
}
