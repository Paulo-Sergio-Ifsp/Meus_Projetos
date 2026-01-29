@echo off
chcp 65001 >nul
title Assinar Executável - Activaware

set "ROOT_DIR=%~dp0"
pushd "%ROOT_DIR%"

color 0D
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║         Assinatura Digital - meu_Busca_Livros                ║
echo ║                  by Activaware                               ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Verificar se o executável existe
if not exist "Distribuir\meu_Busca_Livros.exe" (
    color 0C
    echo [ERRO] Executável não encontrado!
    echo.
    echo Execute primeiro "Criar_Executavel.bat" para gerar o .exe
    pause
    popd
    exit /b 1
)

echo [1/3] Verificando Windows SDK (SignTool)...
echo.

REM Procurar SignTool em locais comuns
set "SIGNTOOL="
for /f "delims=" %%i in ('dir /b /s "C:\Program Files (x86)\Windows Kits\10\bin\*\x64\signtool.exe" 2^>nul') do (
    set "SIGNTOOL=%%i"
    goto :found_signtool
)

:found_signtool
if "%SIGNTOOL%"=="" (
    color 0E
    echo [AVISO] SignTool não encontrado!
    echo.
    echo O Windows SDK não está instalado neste computador.
    echo.
    echo OPÇÃO 1: Baixar Windows SDK de:
    echo https://developer.microsoft.com/windows/downloads/windows-sdk/
    echo.
    echo OPÇÃO 2: Usar apenas metadados de versão (já incluídos)
    echo O executável já contém:
    echo   • Empresa: Activaware
    echo   • Versão: 2.0.0.0
    echo   • Copyright: © 2026 Activaware
    echo.
    echo Para verificar, clique com botão direito no .exe ^> Propriedades ^> Detalhes
    echo.
    pause
    popd
    exit /b 0
)

echo ✓ SignTool encontrado: %SIGNTOOL%
echo.

echo [2/3] Criando certificado autoassinado...
echo.

REM Criar certificado autoassinado (desenvolvimento/teste)
set "CERT_NAME=Activaware"
set "CERT_FILE=%TEMP%\activaware_cert.pfx"
set "CERT_PASSWORD=Activaware2026"

echo Gerando certificado de desenvolvimento...
echo (Este é um certificado de TESTE - não será reconhecido por antivírus)
echo.

REM Usar PowerShell para criar certificado
powershell -Command "& { ^
    $cert = New-SelfSignedCertificate ^
        -Type CodeSigningCert ^
        -Subject 'CN=Activaware' ^
        -KeyAlgorithm RSA ^
        -KeyLength 2048 ^
        -HashAlgorithm SHA256 ^
        -CertStoreLocation 'Cert:\CurrentUser\My' ^
        -NotAfter (Get-Date).AddYears(5); ^
    $pwd = ConvertTo-SecureString -String '%CERT_PASSWORD%' -Force -AsPlainText; ^
    Export-PfxCertificate -Cert $cert -FilePath '%CERT_FILE%' -Password $pwd ^
}" 2>nul

if not exist "%CERT_FILE%" (
    color 0C
    echo [ERRO] Falha ao criar certificado!
    echo.
    echo Isso pode acontecer se você não tiver permissões administrativas.
    echo Execute este script como Administrador.
    pause
    popd
    exit /b 1
)

echo ✓ Certificado criado: %CERT_FILE%
echo.

echo [3/3] Assinando executável...
echo.

REM Assinar o executável
"%SIGNTOOL%" sign ^
    /f "%CERT_FILE%" ^
    /p "%CERT_PASSWORD%" ^
    /fd SHA256 ^
    /tr http://timestamp.digicert.com ^
    /td SHA256 ^
    /d "meu_Busca_Livros by Activaware" ^
    "Distribuir\meu_Busca_Livros.exe"

if errorlevel 1 (
    color 0C
    echo.
    echo [ERRO] Falha ao assinar o executável!
    echo.
    echo Possíveis causas:
    echo   - Falta de permissões administrativas
    echo   - Servidor de timestamp indisponível
    echo.
    del "%CERT_FILE%" 2>nul
    pause
    popd
    exit /b 1
)

echo.
echo ✓ Executável assinado com sucesso!
echo.

REM Verificar assinatura
echo Verificando assinatura...
"%SIGNTOOL%" verify /pa "Distribuir\meu_Busca_Livros.exe"

echo.
color 0A
echo ════════════════════════════════════════════════════════════
echo.
echo   ✓ ASSINATURA COMPLETA!
echo.
echo   Informações do executável:
echo     • Empresa: Activaware
echo     • Produto: meu_Busca_Livros v2.0
echo     • Copyright: © 2026 Activaware
echo     • Assinatura Digital: Activaware (Autoassinado)
echo.
echo   IMPORTANTE: Certificado autoassinado para DESENVOLVIMENTO
echo   Para produção, obtenha certificado de autoridade certificadora
echo.
echo ════════════════════════════════════════════════════════════
echo.

REM Limpar certificado temporário
del "%CERT_FILE%" 2>nul

echo Pressione qualquer tecla para abrir a pasta do executável...
pause >nul
explorer "Distribuir"

popd
