' ════════════════════════════════════════════════════════════════
' meu_Busca_Livros - Inicialização Invisível
' Este script inicia o servidor em segundo plano sem mostrar janelas
' ════════════════════════════════════════════════════════════════

Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Obter o diretório atual do script
strScriptPath = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Mudar para o diretório do projeto
objShell.CurrentDirectory = strScriptPath

' Verificar se Python está instalado
On Error Resume Next
objShell.Run "python --version", 0, True
If Err.Number <> 0 Then
    MsgBox "Python não encontrado!" & vbCrLf & vbCrLf & _
           "Por favor, instale o Python de:" & vbCrLf & _
           "https://www.python.org/downloads/" & vbCrLf & vbCrLf & _
           "Lembre-se de marcar 'Add Python to PATH'", _
           vbCritical, "meu_Busca_Livros - Erro"
    WScript.Quit 1
End If
On Error GoTo 0

' Mostrar mensagem de inicialização
MsgBox "Iniciando meu_Busca_Livros..." & vbCrLf & vbCrLf & _
       "O navegador abrirá em alguns segundos." & vbCrLf & vbCrLf & _
       "Para encerrar, abra o Gerenciador de Tarefas" & vbCrLf & _
       "e finalize o processo 'python.exe'", _
       vbInformation, "meu_Busca_Livros"

' Iniciar o servidor Python em segundo plano (janela oculta)
objShell.Run "python app_search.py", 0, False

' Aguardar 3 segundos para o servidor inicializar
WScript.Sleep 3000

' Abrir o navegador
objShell.Run "http://127.0.0.1:5000", 1, False

' Script finaliza, mas o servidor continua rodando em background
WScript.Quit 0
