# Adicionar Botão de Sair do Aplicativo

Este plano descreve as alterações necessárias para adicionar um botão que permite ao usuário fechar o aplicativo e retornar à tela inicial (Home) do Android.

## Decisões de Design

- **Localização:** Adicionaremos um ícone de "Sair" na `TopAppBar` (barra superior) da `MainActivity`.
- **Comportamento:** Utilizaremos `activity.finishAffinity()` para fechar completamente o aplicativo, conforme solicitado ("fechar o app").
- **Ícone:** Usaremos `Icons.AutoMirrored.Filled.ExitToApp` para representar a ação de saída.

## Alterações Propostas

### UI da MainActivity

#### [MainActivity.kt](file:///home/Activa/Área de trabalho/Projetos Android_Studio/app/src/main/java/com/paulo/controle_gastos/MainActivity.kt)

- Adicionar um `actions` no `TopAppBar` contendo um `IconButton` com o ícone de saída.
- Obter a referência da `Activity` atual usando `LocalContext.current` para chamar `finishAffinity()`.

```kotlin
// No escopo da TopAppBar
actions = {
    val activity = LocalContext.current as? Activity
    IconButton(onClick = { activity?.finishAffinity() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Sair"
        )
    }
}
```

## Plano de Verificação

### Verificação Manual
1. **Compilar e Executar:** Rodar o aplicativo no emulador ou dispositivo físico.
2. **Localizar o Botão:** Verificar se o ícone de saída aparece no canto superior direito da barra de título.
3. **Testar Funcionalidade:** Clicar no botão e verificar se o aplicativo fecha e o usuário retorna à tela inicial do Android.
4. **Persistência:** Reabrir o app e garantir que ele inicia normalmente.

---
**Nota:** Como sou uma IA, não posso clicar fisicamente no botão, mas verificarei a sintaxe e a integração no código.
