# Implementação do Botão de Sair

Concluí a adição do botão para fechar o aplicativo e retornar à tela inicial do Android.

## Alterações Realizadas

### [MainActivity.kt](file:///home/Activa/Área de trabalho/Projetos Android_Studio/app/src/main/java/com/paulo/controle_gastos/MainActivity.kt)

- **Novos Imports:** Adicionados `android.app.Activity`, `androidx.compose.ui.platform.LocalContext` e `Icons.AutoMirrored.Filled.ExitToApp`.
- **TopAppBar:** Adicionada a seção `actions` ao componente `TopAppBar`.
- **Lógica de Saída:** Implementado o comando `activity?.finishAffinity()`, que encerra todas as atividades do aplicativo de uma vez, garantindo o retorno à Home do sistema.

## Como Testar

1. Abra o aplicativo.
2. Observe o novo ícone de "porta de saída" no canto superior direito da barra de título.
3. Clique no ícone.
4. O aplicativo deve fechar imediatamente e você será levado para a tela inicial do seu celular.

## Resumo Técnico
O uso de `finishAffinity()` é a forma mais eficaz de garantir que o app seja "fechado" completamente do ponto de vista do usuário, limpando a pilha de telas (back stack) e encerrando o processo principal.
