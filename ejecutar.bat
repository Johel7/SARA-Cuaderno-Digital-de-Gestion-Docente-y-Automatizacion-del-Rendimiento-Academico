@echo off
title Cargando Sistema SARA...
cls
echo ===================================================
echo   CONFIGURANDO E INICIANDO EL PROYECTO (MAVEN)   
echo ===================================================
echo.

:: 1. Solicitar credenciales de MySQL Workbench a la profesora
set /p MYSQL_USER="Introduce tu usuario de MySQL (por defecto root): "
set /p DB_PASS="Introduce tu contrasena de MySQL Workbench: "
set /p DB_NAME="Introduce el nombre que deseas para la Base de Datos: "

echo.
echo [1/3] Creando Base de Datos si no existe...
mysql -u %MYSQL_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME%;"

echo [2/3] Importando tablas y datos desde schema.sql...
mysql -u %MYSQL_USER% -p%DB_PASS% %DB_NAME% < schema.sql

echo [3/3] Compilando y ejecutando la aplicacion con Maven...
:: Al ser un proyecto Maven, usamos el comando oficial exec:java apuntando a tu clase sara.Main
call mvn clean compile exec:java -Dexec.mainClass="sara.Main"

echo.
echo Proceso finalizado.
pause
