@echo off
setlocal
title SpaceWork - Sistema de Reservas

echo.
echo  ============================================
echo   SpaceWork - Sistema de Gestion de Reservas
echo  ============================================
echo.

cd /d "%~dp0"

:: Liberar el puerto 8080 si esta ocupado
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
        taskkill /PID %%a /F >nul 2>&1
    )
    timeout /t 2 >nul
)

:: Compilar el proyecto
echo  Compilando...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo  Error en compilacion.
    pause
    exit /b 1
)

:: Iniciar el servidor
echo  Iniciando en http://localhost:8080
echo  Presiona Ctrl+C para detener.
echo.

java -jar target\SistemaReservas-1.0-SNAPSHOT.jar

pause
