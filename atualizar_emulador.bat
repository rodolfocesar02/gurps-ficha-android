@echo off
:: Forçar a execução no diretório onde o script está localizado
cd /d "%~dp0"

set ADB_PATH=C:\Users\Rodolfo\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo ==========================================
echo   MESTRE IA - ATUALIZADOR DE EMULADOR
echo ==========================================

:: Verificar se o gradlew existe nesta pasta
if not exist "gradlew.bat" (
    echo [ERRO] Nao encontrei o arquivo gradlew.bat!
    echo Por favor, rode este script de dentro da pasta:
    echo c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\
    pause
    exit /b
)

echo [1/4] Limpando e validando assets...
call .\gradlew.bat clean
if %ERRORLEVEL% NEQ 0 goto :erro

echo [2/4] Removendo versao anterior para resetar Banco de Dados...
"%ADB_PATH%" uninstall com.gurps.ficha.visual

echo [3/4] Compilando e Instalando versao Visual...
call .\gradlew.bat :app:installVisualDebug
if %ERRORLEVEL% NEQ 0 goto :erro

echo [4/4] Abrindo o app no emulador...
"%ADB_PATH%" shell am start -n com.gurps.ficha.visual/com.gurps.ficha.MainActivity

echo ------------------------------------------
echo TUDO PRONTO! App atualizado com as novas regras.
echo ------------------------------------------
pause
exit /b

:erro
echo.
echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
echo   ERRO na compilacao ou instalacao.
echo   Verifique as mensagens acima.
echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
pause
exit /b
