@echo off
set ADB_PATH=C:\Users\Rodolfo\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo [1/4] Limpando e validando assets...
call .\gradlew.bat clean

echo [2/4] Removendo versao anterior para resetar Banco de Dados...
"%ADB_PATH%" uninstall com.gurps.ficha.visual

echo [3/4] Compilando e Instalando versao Visual...
call .\gradlew.bat :app:installVisualDebug

if %ERRORLEVEL% EQU 0 (
    echo [4/4] Abrindo o app no emulador...
    "%ADB_PATH%" shell am start -n com.gurps.ficha.visual/com.gurps.ficha.MainActivity
    echo TUDO PRONTO! App atualizado com as novas regras.
) else (
    echo ERRO na compilacao. Verifique o log acima.
)
pause
