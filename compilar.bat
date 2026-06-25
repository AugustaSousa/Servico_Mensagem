@echo off
echo ========================================
echo    COMPILANDO SISTEMA DE MENSAGENS
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM Limpar compilação anterior
if exist bin rmdir /s /q bin
mkdir bin

set CP=lib\activemq-all-5.16.7.jar

echo [1/4] Compilando model e util...
javac -cp "%CP%" -d bin src\model\*.java src\util\*.java
if errorlevel 1 goto erro

echo [2/4] Compilando interfaces RMI...
javac -cp "%CP%;bin" -d bin src\cliente\IClienteCallback.java src\servidor\IServidorMensagens.java
if errorlevel 1 goto erro

echo [3/4] Compilando servidor...
javac -cp "%CP%;bin" -d bin src\servidor\*.java
if errorlevel 1 goto erro

echo [4/4] Compilando cliente e UI (juntos para resolver dependencias circulares)...
javac -cp "%CP%;bin" -d bin src\cliente\*.java src\ui\*.java
if errorlevel 1 goto erro

echo.
echo ========================================
echo    COMPILACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo Para executar o servidor:
echo   java -cp "bin;lib\activemq-all-5.16.7.jar" servidor.ServidorMensagens
echo.
echo Para executar o cliente:
echo   java -cp "bin;lib\activemq-all-5.16.7.jar" cliente.ClienteMensagens
echo.
pause
exit /b 0

:erro
echo.
echo ========================================
echo    ERRO NA COMPILACAO!
echo ========================================
echo.
pause
exit /b 1