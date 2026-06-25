@echo off
echo ========================================
echo    INICIANDO CLIENTE DE MENSAGENS
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

java -cp "bin;lib\activemq-all-5.16.7.jar" cliente.ClienteMensagens

pause