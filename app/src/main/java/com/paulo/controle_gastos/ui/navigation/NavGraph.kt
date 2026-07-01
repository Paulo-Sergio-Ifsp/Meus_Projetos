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
import com.paulo.controle_gastos.ui.screens.LoginScreen
import androidx.compose.ui.Modifier // ✅ ADICIONE ESTE IMPORT
import androidx.navigation.NavType
import androidx.navigation.navArgument

// ✅ Todas as rotas do app
sealed class Dest(val route: String) {
    data object Login : Dest("login")
    data object Home : Dest("home")
    data object Contas : Dest("contas")
    data object AddAccount : Dest("add_account")
    data object AddDespesa : Dest("add_despesa") // ✅ NOVA ROTA
    data object AddGanho : Dest("add_ganho")     // ✅ NOVA ROTA
    data object EditDespesa : Dest("edit_despesa")
    data object EditGanho : Dest("edit_ganho")
    data object EditAccount : Dest("edit_account")
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
        startDestination = Dest.Login.route,
        modifier = modifier // ✅ 2. PASSE O MODIFIER PARA O NavHost
    ) {

        // ✅ Tela Login
        composable(Dest.Login.route) {
            LoginScreen(nav = navController)
        }

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

        // ✅ Tela AddDespesa
        composable(Dest.AddDespesa.route) {
            AddDespesaScreen(nav = navController, vm = vm)
        }

        // ✅ Tela AddGanho
        composable(Dest.AddGanho.route) {
            AddGanhoScreen(nav = navController, vm = vm)
        }

        // ✅ Tela EditDespesa
        composable(
            route = "${Dest.EditDespesa.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddDespesaScreen(nav = navController, vm = vm, editId = id)
        }

        // ✅ Tela EditGanho
        composable(
            route = "${Dest.EditGanho.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddGanhoScreen(nav = navController, vm = vm, editId = id)
        }

        // ✅ Tela EditAccount
        composable(
            route = "${Dest.EditAccount.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddAccountScreen(nav = navController, vm = vm, editId = id)
        }
    }
}
