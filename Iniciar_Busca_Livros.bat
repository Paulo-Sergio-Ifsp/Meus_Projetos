@echo off
chcp 65001 >nul
title meu_Busca_Livros - Iniciando...

color 0A
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                  meu_Busca_Livros                          ║
echo ║          Busca de livros gratuitos em fontes públicas      ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo [%TIME%] Verificando instalação do Python...

REM Verificar se Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo.
    echo [ERRO] Python não encontrado!
    echo.
    echo Por favor, instale o Python 3.8+ de: https://www.python.org/downloads/
    echo Certifique-se de marcar "Add Python to PATH" durante a instalação.
    echo.
    pause
    exit /b 1
)

echo [%TIME%] Python encontrado! ✓
echo [%TIME%] Verificando dependências...

REM Verificar se as dependências estão instaladas
python -c "import flask" 2>nul
if errorlevel 1 (
    echo [%TIME%] Instalando dependências necessárias...
    echo.
    pip install -r requirements.txt
    if errorlevel 1 (
        color 0C
        echo.
        echo [ERRO] Falha ao instalar dependências!
        echo.
        pause
        exit /b 1
    )
    echo.
    echo [%TIME%] Dependências instaladas com sucesso! ✓
) else (
    echo [%TIME%] Dependências já instaladas ✓
)

echo.
echo [%TIME%] Iniciando servidor...
echo.
echo ════════════════════════════════════════════════════════════
echo.
echo   O navegador abrirá automaticamente em alguns segundos.
echo   Esta janela será MINIMIZADA automaticamente.
echo.
echo   Se não abrir, acesse manualmente:
echo   http://127.0.0.1:5000
echo.
echo   Para ENCERRAR o servidor, restaure esta janela e pressione CTRL+C
echo   ou feche-a pelo ícone na barra de tarefas.
echo.
echo ════════════════════════════════════════════════════════════
echo.

REM Criar VBScript temporário para minimizar a janela
set "VBS_FILE=%TEMP%\minimize_window.vbs"
echo Set objShell = CreateObject("WScript.Shell") > "%VBS_FILE%"
echo WScript.Sleep 2000 >> "%VBS_FILE%"
echo objShell.SendKeys "%%{SPACE}n" >> "%VBS_FILE%"

REM Executar VBScript em background para minimizar após 2 segundos
start /min cscript //nologo "%VBS_FILE%"

REM Iniciar o servidor
python app_search.py

REM Limpar arquivo temporário
del "%VBS_FILE%" 2>nul

pause
