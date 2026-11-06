@echo off
title Cargando Proyecto Java + MySQL...
cls
echo ===================================================
echo   CONFIGURANDO E INICIANDO EL PROYECTO (ECLIPSE)   
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

echo [3/3] Compilando y ejecutando la aplicacion Java...
:: Eclipse compila por defecto en la carpeta 'bin'. 
:: Buscamos todos los archivos .java de src, los compilamos en bin y ejecutamos la clase principal.

if not exist bin mkdir bin
javac -d bin -sourcepath src src/*.java

:: NOTA: Reemplaza "Principal" por el nombre de tu clase que tiene el metodo main (ej: Login, Menu, etc.)
:: Si tu clase principal esta dentro de un paquete, usa: java -cp bin paquete.ClasePrincipal
java -cp bin;lib/* Principal

echo.
echo Proceso finalizado.
pause
