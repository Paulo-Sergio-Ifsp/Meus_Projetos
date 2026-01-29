@echo off
chcp 65001 >nul
title meu_Busca_Livros - Parar Servidor

color 0E
echo.
echo ════════════════════════════════════════════════════════════
echo   meu_Busca_Livros - Parar Servidor
echo ════════════════════════════════════════════════════════════
echo.
echo Procurando servidores em execução...
echo.

REM Procurar processos Python rodando app_search.py
for /f "tokens=2" %%i in ('tasklist /FI "IMAGENAME eq python.exe" /FO CSV /NH ^| find "python.exe"') do (
    echo Finalizando processo Python %%i...
    taskkill /PID %%i /F >nul 2>&1
)

echo.
echo ✓ Servidor encerrado!
echo.
echo Você já pode fechar esta janela.
echo.
timeout /t 3 >nul
