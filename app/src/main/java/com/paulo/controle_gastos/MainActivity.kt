package com.paulo.controle_gastos

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.paulo.controle_gastos.data.AppDatabase
import com.paulo.controle_gastos.data.repository.FinanceRepository
import com.paulo.controle_gastos.model.*
import com.paulo.controle_gastos.ui.theme.Controle_GastosTheme
import com.paulo.controle_gastos.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete

// --- Telas ---
sealed class Screen(
  val route: String,
  val label: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  object Home : Screen("home", "Início", Icons.Default.Home)
  object Contas : Screen("contas", "Contas", Icons.AutoMirrored.Filled.List)
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Controle_GastosTheme {
        AppPrincipal()
      }
    }
  }
}

// ===================================================================
//  FUNÇÃO PRINCIPAL DO APP (COM AS MUDANÇAS)
// ===================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal() {
  val context = LocalContext.current

  val db = remember {
    Room.databaseBuilder(
      context,
      AppDatabase::class.java,
      "controle_gastos_db"
    ).build()
  }

  val repository = remember {
    FinanceRepository(
      db.despesaDao(),
      db.ganhoDao(),
      db.contaDao()
    )
  }

  val viewModel = remember { FinanceViewModel(repository) }
  val uiState by viewModel.uiState.collectAsState()

  var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
  var showAddTransactionSheet by remember { mutableStateOf(false) }
  var showAddAccountSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(currentScreen.label) },
        actions = {
          IconButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
          }) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Permissões")
          }
        }
      )
    },
    bottomBar = {
      NavigationBar {
        val items = listOf(Screen.Home, Screen.Contas)
        items.forEach { screen ->
          NavigationBarItem(
            icon = { Icon(screen.icon, contentDescription = screen.label) },
            label = { Text(screen.label) },
            selected = currentScreen == screen,
            onClick = { currentScreen = screen }
          )
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(onClick = {
        when (currentScreen) {
          Screen.Home -> showAddTransactionSheet = true
          Screen.Contas -> showAddAccountSheet = true
        }
      }) {
        Icon(Icons.Default.Add, contentDescription = "Adicionar")
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      when (currentScreen) {
        // MUDANÇA AQUI: Passando as funções de delete
        Screen.Home -> HomeScreen(
          despesas = uiState.despesas,
          ganhos = uiState.ganhos,
          onDeleteDespesa = viewModel::deleteDespesa,
          onDeleteGanho = viewModel::deleteGanho
        )
        // MUDANÇA AQUI: Passando a função de delete
        Screen.Contas -> ContasScreen(
          contas = uiState.contas,
          onDeleteConta = viewModel::deleteConta
        )
      }
    }
  }

  if (showAddTransactionSheet) {
    ModalBottomSheet(
      onDismissRequest = { showAddTransactionSheet = false },
      sheetState = sheetState
    ) {
      AddTransactionSheet(
        contas = uiState.contas,
        onAddDespesa = { despesa ->
          scope.launch { viewModel.addDespesa(despesa) }
          showAddTransactionSheet = false
        },
        onAddGanho = { ganho ->
          scope.launch { viewModel.addGanho(ganho) }
          showAddTransactionSheet = false
        }
      )
    }
  }

  if (showAddAccountSheet) {
    ModalBottomSheet(
      onDismissRequest = { showAddAccountSheet = false },
      sheetState = sheetState
    ) {
      AddAccountSheet(
        onAddConta = { conta ->
          scope.launch {
            viewModel.addConta(conta)
          }
          showAddAccountSheet = false
        }
      )
    }
  }
}

// ===================================================================
//  NOVO FORMULÁRIO DE ADICIONAR CONTA
// ===================================================================

@OptIn(ExperimentalMaterial3Api::class) // <-- CORRIGIDO AQUI
@Composable
fun AddAccountSheet(
  onAddConta: (Conta) -> Unit
) {
  val tiposDeConta = listOf(
    TipoConta.CONTA_CORRENTE,
    TipoConta.POUPANCA,
    TipoConta.CARTAO_CREDITO
  )

  var nome by remember { mutableStateOf("") }
  var saldoInicial by remember { mutableStateOf("") }
  var tipoExpanded by remember { mutableStateOf(false) }
  var selectedTipo by remember { mutableStateOf(tiposDeConta.first()) }

  Column(
    Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text("Adicionar Nova Conta", style = MaterialTheme.typography.titleLarge)

    OutlinedTextField(
      value = nome,
      onValueChange = { nome = it },
      label = { Text("Nome da Conta (Ex: Nubank, Itaú)") },
      modifier = Modifier.fillMaxWidth()
    )

    ExposedDropdownMenuBox(
      expanded = tipoExpanded,
      onExpandedChange = { tipoExpanded = !tipoExpanded }
    ) {
      OutlinedTextField(
        value = selectedTipo.name.replace("_", " "),
        onValueChange = {},
        readOnly = true,
        label = { Text("Tipo de Conta") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded) },
        modifier = Modifier.menuAnchor().fillMaxWidth()
      )

      ExposedDropdownMenu(
        expanded = tipoExpanded,
        onDismissRequest = { tipoExpanded = false }
      ) {
        tiposDeConta.forEach { tipo ->
          DropdownMenuItem(
            text = { Text(tipo.name.replace("_", " ")) },
            onClick = {
              selectedTipo = tipo
              tipoExpanded = false
            }
          )
        }
      }
    }

    OutlinedTextField(
      value = saldoInicial,
      onValueChange = { saldoInicial = it },
      label = { Text("Saldo Inicial (Ex: 150.00)") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth(),
      enabled = selectedTipo != TipoConta.CARTAO_CREDITO,
      placeholder = {
        if (selectedTipo == TipoConta.CARTAO_CREDITO) {
          Text("Não aplicável para cartão")
        }
      }
    )

    Button(
      onClick = {
        val valorCorrigido = saldoInicial.replace(",", ".")
        val saldoDouble = if (selectedTipo == TipoConta.CARTAO_CREDITO) {
          0.0
        } else {
          saldoInicial.toDoubleOrNull() ?: 0.0
        }

        if (nome.isNotBlank()) {
          onAddConta(
            Conta(
              id = UUID.randomUUID().toString(),
              nome = nome,
              saldoInicial = saldoDouble,
              tipo = selectedTipo
            )
          )
        }
      },
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
      Text("Salvar Conta")
    }
  }
}

// ===================================================================
//  FUNÇÕES QUE JÁ EXISTIAM (DO SEU CÓDIGO ORIGINAL)
// ===================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
  contas: List<Conta>,
  onAddDespesa: (Despesa) -> Unit,
  onAddGanho: (Ganho) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Despesa", "Ganho")

  Column(Modifier.fillMaxWidth().padding(16.dp)) {
    TabRow(selectedTabIndex = selectedTab) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title) }
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    when (selectedTab) {
      0 -> AddExpenseForm(contas, onAddDespesa)
      1 -> AddEarningForm(contas, onAddGanho)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseForm(contas: List<Conta>, onAddDespesa: (Despesa) -> Unit) {
  var valor by remember { mutableStateOf("") }
  var local by remember { mutableStateOf("") }
  var contaExpanded by remember { mutableStateOf(false) }
  var selectedConta by remember { mutableStateOf(contas.firstOrNull()) }

  var metodoExpanded by remember { mutableStateOf(false) }
  val metodos = listOf("Pix", "Débito", "Crédito", "Dinheiro")
  var selectedMetodo by remember { mutableStateOf(metodos.first()) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("Adicionar Nova Despesa", style = MaterialTheme.typography.titleLarge)

    OutlinedTextField(
      value = valor,
      onValueChange = { valor = it },
      label = { Text("Valor (Ex: 50.00)") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = local,
      onValueChange = { local = it },
      label = { Text("Local (Ex: Mercado, iFood)") },
      modifier = Modifier.fillMaxWidth()
    )

    ExposedDropdownMenuBox(
      expanded = contaExpanded,
      onExpandedChange = { contaExpanded = !contaExpanded }
    ) {
      OutlinedTextField(
        value = selectedConta?.nome ?: "Selecione a Conta",
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contaExpanded) },
        modifier = Modifier.menuAnchor().fillMaxWidth()
      )

      ExposedDropdownMenu(
        expanded = contaExpanded,
        onDismissRequest = { contaExpanded = false }
      ) {
        contas.forEach { conta ->
          DropdownMenuItem(
            text = { Text(conta.nome) },
            onClick = {
              selectedConta = conta
              contaExpanded = false
            }
          )
        }
      }
    }

    ExposedDropdownMenuBox(
      expanded = metodoExpanded,
      onExpandedChange = { metodoExpanded = !metodoExpanded }
    ) {
      OutlinedTextField(
        value = selectedMetodo,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metodoExpanded) },
        modifier = Modifier.menuAnchor().fillMaxWidth()
      )

      ExposedDropdownMenu(
        expanded = metodoExpanded,
        onDismissRequest = { metodoExpanded = false }
      ) {
        metodos.forEach { metodo ->
          DropdownMenuItem(
            text = { Text(metodo) },
            onClick = {
              selectedMetodo = metodo
              metodoExpanded = false
            }
          )
        }
      }
    }

    Button(
      onClick = {
        val valorDouble = valor.toDoubleOrNull()
        if (valorDouble != null && local.isNotBlank() && selectedConta != null) {
          onAddDespesa(
            Despesa(
              id = UUID.randomUUID().toString(),
              data = System.currentTimeMillis(),
              local = local,
              valor = valorDouble,
              contaId = selectedConta!!.id,
              metodoPagamento = selectedMetodo
            )
          )
        }
      },
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
      Text("Salvar Despesa")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEarningForm(contas: List<Conta>, onAddGanho: (Ganho) -> Unit) {
  var valor by remember { mutableStateOf("") }
  var descricao by remember { mutableStateOf("") }
  var contaExpanded by remember { mutableStateOf(false) }
  var selectedConta by remember { mutableStateOf(contas.firstOrNull { it.tipo != TipoConta.CARTAO_CREDITO }) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("Adicionar Novo Ganho", style = MaterialTheme.typography.titleLarge)

    OutlinedTextField(
      value = valor,
      onValueChange = { valor = it },
      label = { Text("Valor (Ex: 1000.00)") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = descricao,
      onValueChange = { descricao = it },
      label = { Text("Descrição (Ex: Salário, Freelance)") },
      modifier = Modifier.fillMaxWidth()
    )

    ExposedDropdownMenuBox(
      expanded = contaExpanded,
      onExpandedChange = { contaExpanded = !contaExpanded }
    ) {
      OutlinedTextField(
        value = selectedConta?.nome ?: "Selecione a Conta",
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contaExpanded) },
        modifier = Modifier.menuAnchor().fillMaxWidth()
      )

      ExposedDropdownMenu(
        expanded = contaExpanded,
        onDismissRequest = { contaExpanded = false }
      ) {
        contas.filter { it.tipo != TipoConta.CARTAO_CREDITO }.forEach { conta ->
          DropdownMenuItem(
            text = { Text(conta.nome) },
            onClick = {
              selectedConta = conta
              contaExpanded = false
            }
          )
        }
      }
    }

    Button(
      onClick = {
        val valorDouble = valor.toDoubleOrNull()
        if (valorDouble != null && descricao.isNotBlank() && selectedConta != null) {
          onAddGanho(
            Ganho(
              id = UUID.randomUUID().toString(),
              data = System.currentTimeMillis(),
              descricao = descricao,
              valor = valorDouble,
              contaId = selectedConta!!.id
            )
          )
        }
      },
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
      Text("Salvar Ganho")
    }
  }
}

@Composable
fun HomeScreen(
  despesas: List<Despesa>,
  ganhos: List<Ganho>,
  onDeleteDespesa: (Despesa) -> Unit, // <-- Parâmetro adicionado
  onDeleteGanho: (Ganho) -> Unit      // <-- Parâmetro adicionado
) {
  // Estado para controlar o que será deletado
  var itemParaDeletar by remember { mutableStateOf<Any?>(null) }

  LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    item {
      Text("Ganhos Recentes", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
    }
    items(ganhos) { ganho ->
      GanhoItem(ganho, onDeleteClick = { itemParaDeletar = ganho }) // Passa o item
    }

    item {
      Spacer(Modifier.height(16.dp))
      Text("Despesas Recentes", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
    }
    items(despesas) { despesa ->
      ExpenseItem(despesa, onDeleteClick = { itemParaDeletar = despesa }) // Passa o item
    }
  }

  // Lógica do Dialog
  itemParaDeletar?.let { item ->
    DeleteConfirmationDialog(
      itemDescricao = when (item) {
        is Despesa -> item.local
        is Ganho -> item.descricao
        else -> "item"
      },
      onDismiss = { itemParaDeletar = null },
      onConfirm = {
        when (item) {
          is Despesa -> onDeleteDespesa(item)
          is Ganho -> onDeleteGanho(item)
        }
      }
    )
  }
}
@Composable
fun ContasScreen(
  contas: List<Conta>,
  onDeleteConta: (Conta) -> Unit // <-- Parâmetro adicionado
) {
  // Estado para controlar o que será deletado
  var contaParaDeletar by remember { mutableStateOf<Conta?>(null) }

  LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    items(contas) { conta ->
      ContaItem(conta, onDeleteClick = { contaParaDeletar = conta }) // Passa a conta
    }
  }

  // Lógica do Dialog
  contaParaDeletar?.let { conta ->
    DeleteConfirmationDialog(
      itemDescricao = conta.nome,
      onDismiss = { contaParaDeletar = null },
      onConfirm = { onDeleteConta(conta) }
    )
  }
}

@Composable
fun ExpenseItem(despesa: Despesa, onDeleteClick: () -> Unit) { // <-- Parâmetro adicionado
  Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(1f)) {
        Text(despesa.local, fontWeight = FontWeight.Bold)
        Text(despesa.metodoPagamento, style = MaterialTheme.typography.bodySmall)
      }
      Text(
        text = "-R$ ${"%.2f".format(despesa.valor)}",
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
      // BOTÃO DE DELETAR
      IconButton(onClick = onDeleteClick) {
        Icon(Icons.Default.Delete, contentDescription = "Excluir Despesa", tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
fun GanhoItem(ganho: Ganho, onDeleteClick: () -> Unit) { // <-- Parâmetro adicionado
  Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(ganho.descricao, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
      Text(
        text = "+R$ ${"%.2f".format(ganho.valor)}",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
      // BOTÃO DE DELETAR
      IconButton(onClick = onDeleteClick) {
        Icon(Icons.Default.Delete, contentDescription = "Excluir Ganho", tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
fun ContaItem(conta: Conta, onDeleteClick: () -> Unit) { // <-- Parâmetro adicionado
  Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(1f)) {
        Text(conta.nome, fontWeight = FontWeight.Bold)
        Text(conta.tipo.name.replace("_", " "), style = MaterialTheme.typography.bodySmall)
      }
      if (conta.tipo != TipoConta.CARTAO_CREDITO) {
        Text(
          "R$ ${"%.2f".format(conta.saldoInicial)}",
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }
      // BOTÃO DE DELETAR
      IconButton(onClick = onDeleteClick) {
        Icon(Icons.Default.Delete, contentDescription = "Excluir Conta", tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}

// Adicione esta função no seu MainActivity.kt
@Composable
fun DeleteConfirmationDialog(
  itemDescricao: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Confirmar Exclusão") },
    text = { Text("Você tem certeza que deseja excluir \"$itemDescricao\"? Esta ação não pode ser desfeita.") },
    confirmButton = {
      Button(
        onClick = {
          onConfirm()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
      ) {
        Text("Excluir")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}