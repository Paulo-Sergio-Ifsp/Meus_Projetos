@echo off
chcp 65001 >nul
title Criando Executável - meu_Busca_Livros

set "ROOT_DIR=%~dp0"
pushd "%ROOT_DIR%"

color 0B
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║         Criando Executável do meu_Busca_Livros               ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Verificar Python
python --version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo [ERRO] Python não encontrado!
    pause
    exit /b 1
)

echo [1/4] Instalando PyInstaller...
pip install pyinstaller >nul 2>&1
echo      ✓ PyInstaller instalado

echo.
echo [2/4] Verificando dependências...
pip install -r requirements.txt >nul 2>&1
echo      ✓ Dependências verificadas

echo.
echo [3/4] Criando executável... (isso pode demorar alguns minutos)
echo.

REM Criar pasta temporária para build (evita problemas com OneDrive)
set "BUILD_DIR=%TEMP%\meu_Busca_Livros_build"
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

REM Copiar arquivos necessários para pasta temporária
xcopy /E /I /Q "frontend" "%BUILD_DIR%\frontend\" >nul
copy "app_exe.py" "%BUILD_DIR%\" >nul
copy "book_search.py" "%BUILD_DIR%\" >nul
copy "version_info.txt" "%BUILD_DIR%\" >nul

REM Mudar para pasta temporária e criar executável
pushd "%BUILD_DIR%"

pyinstaller --noconfirm ^
    --name=BuscaLivros ^
    --onefile ^
    --console ^
    --icon=NONE ^
    --version-file=version_info.txt ^
    --add-data="frontend;frontend" ^
    --hidden-import=flask ^
    --hidden-import=flask_cors ^
    --hidden-import=requests ^
    --hidden-import=bs4 ^
    --hidden-import=lxml ^
    --hidden-import=urllib.parse ^
    --collect-all=flask ^
    --collect-all=jinja2 ^
    app_exe.py

set BUILD_ERROR=%ERRORLEVEL%
popd

if %BUILD_ERROR% neq 0 (
    color 0C
    echo.
    echo [ERRO] Falha ao criar executável!
    rmdir /s /q "%BUILD_DIR%"
    pause
    exit /b 1
)

echo.
echo [4/4] Finalizando...

REM Criar pasta de distribuição
if not exist "Distribuir" mkdir "Distribuir"

REM Copiar executável da pasta temporária
if not exist "%BUILD_DIR%\dist\BuscaLivros.exe" (
    color 0C
    echo.
    echo [ERRO] Executável não encontrado em: %BUILD_DIR%\dist\BuscaLivros.exe
    echo Verifique os logs do PyInstaller em:
    echo   %BUILD_DIR%\build\BuscaLivros\warn-BuscaLivros.txt
    echo.
    pause
    exit /b 1
)

copy /Y "%BUILD_DIR%\dist\BuscaLivros.exe" "Distribuir\meu_Busca_Livros.exe" >nul

if not exist "Distribuir\meu_Busca_Livros.exe" (
    color 0C
    echo.
    echo [ERRO] Falha ao copiar o executável para Distribuir\meu_Busca_Livros.exe
    pause
    exit /b 1
)

REM Limpar pasta temporária
rmdir /s /q "%BUILD_DIR%"

REM Copiar README de distribuição
if exist "README_DISTRIBUICAO.txt" (
    copy /Y "README_DISTRIBUICAO.txt" "Distribuir\LEIA-ME.txt" >nul
) else (
    echo [AVISO] README_DISTRIBUICAO.txt não encontrado. Ignorando cópia.
)

REM Criar instruções rápidas
(
echo ═══════════════════════════════════════════════════════════════
echo   INSTRUÇÕES RÁPIDAS - meu_Busca_Livros
echo ═══════════════════════════════════════════════════════════════
echo.
echo   1. Duplo clique em "meu_Busca_Livros.exe"
echo   2. Aguarde o navegador abrir
echo   3. Digite o livro e clique em "Buscar"
echo   4. Pronto! 🎉
echo.
echo   Para instruções completas, leia "LEIA-ME.txt"
echo.
echo ═══════════════════════════════════════════════════════════════
) > "Distribuir\INICIO_RAPIDO.txt"

REM Limpar arquivos temporários locais
rmdir /s /q build >nul 2>&1
rmdir /s /q __pycache__ >nul 2>&1
del /q *.spec >nul 2>&1

color 0A
echo.
echo ════════════════════════════════════════════════════════════
echo.
echo   ✓ EXECUTÁVEL CRIADO COM SUCESSO!
echo.
echo   Localização: Distribuir\meu_Busca_Livros.exe
echo.
echo   Informações incluídas:
echo     • Empresa: Activaware
echo     • Produto: meu_Busca_Livros v2.0
echo     • Copyright: © 2026 Activaware
echo.
echo   Você pode distribuir toda a pasta "Distribuir" para
echo   outras pessoas. Elas só precisam clicar no .exe!
echo.
echo ════════════════════════════════════════════════════════════
echo.

REM Perguntar sobre assinatura digital
echo.
choice /C SN /M "Deseja assinar digitalmente o executável agora (requer Windows SDK)"

if errorlevel 2 goto skip_sign
if errorlevel 1 goto do_sign

:do_sign
echo.
echo Iniciando processo de assinatura...
call Assinar_Executavel.bat
goto end_script

:skip_sign
echo.
echo Assinatura digital ignorada.
echo.
echo NOTA: O executável já contém metadados da empresa Activaware
echo Para assinar depois, execute: Assinar_Executavel.bat
echo.

:end_script
REM Abrir pasta
explorer "Distribuir"

popd

pause
