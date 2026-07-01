package com.paulo.controle_gastos.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.paulo.controle_gastos.ui.navigation.Dest

@Composable
fun LoginScreen(nav: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }
    
    // Senha salva (se não houver, o usuário define na primeira vez)
    var savedPassword by remember { mutableStateOf(sharedPrefs.getString("app_password", "")) }
    var passwordInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (savedPassword.isNullOrBlank()) "Defina sua Senha" else "Acesso Restrito",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { 
                if (it.length <= 6) {
                    passwordInput = it 
                    isError = false
                }
            },
            label = { Text("Senha (numérica)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = isError,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        if (isError) {
            Text("Senha incorreta", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (savedPassword.isNullOrBlank()) {
                    // Primeira vez: Salva a senha
                    if (passwordInput.length >= 4) {
                        sharedPrefs.edit().putString("app_password", passwordInput).apply()
                        nav.navigate(Dest.Home.route) {
                            popUpTo(Dest.Login.route) { inclusive = true }
                        }
                    } else {
                        isError = true
                    }
                } else {
                    // Verificação
                    if (passwordInput == savedPassword) {
                        nav.navigate(Dest.Home.route) {
                            popUpTo(Dest.Login.route) { inclusive = true }
                        }
                    } else {
                        isError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(if (savedPassword.isNullOrBlank()) "Salvar e Entrar" else "Entrar")
        }
    }
}
