@echo off
chcp 65001 >nul
title Criar Atalho - meu_Busca_Livros

color 0B
echo.
echo ════════════════════════════════════════════════════════════
echo   Criando atalho na Área de Trabalho...
echo ════════════════════════════════════════════════════════════
echo.

REM Obter o caminho atual
set "SCRIPT_DIR=%~dp0"
set "BATCH_FILE=%SCRIPT_DIR%Iniciar_Busca_Livros.bat"

REM Criar arquivo VBS temporário para criar o atalho
set "VBS_FILE=%TEMP%\create_shortcut.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") > "%VBS_FILE%"
echo sLinkFile = oWS.SpecialFolders("Desktop") ^& "\meu_Busca_Livros.lnk" >> "%VBS_FILE%"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%VBS_FILE%"
echo oLink.TargetPath = "%BATCH_FILE%" >> "%VBS_FILE%"
echo oLink.WorkingDirectory = "%SCRIPT_DIR%" >> "%VBS_FILE%"
echo oLink.Description = "Buscador de livros gratuitos" >> "%VBS_FILE%"
echo oLink.IconLocation = "%%SystemRoot%%\System32\imageres.dll,13" >> "%VBS_FILE%"
echo oLink.Save >> "%VBS_FILE%"

REM Executar o VBS
cscript //nologo "%VBS_FILE%"

REM Limpar arquivo temporário
del "%VBS_FILE%"

if exist "%USERPROFILE%\Desktop\meu_Busca_Livros.lnk" (
    color 0A
    echo.
    echo ✓ Atalho criado com sucesso na Área de Trabalho!
    echo.
    echo Agora você pode clicar no atalho para iniciar o programa.
) else (
    color 0C
    echo.
    echo ✗ Erro ao criar atalho.
    echo.
    echo Tente executar este arquivo como Administrador.
)

echo.
pause
