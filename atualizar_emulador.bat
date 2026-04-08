@echo off
set ADB_PATH=C:\Users\Rodolfo\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo [1/3] Limpando e validando assets...
call .\gradlew.bat clean validateActiveJsonAssets

echo [2/3] Compilando e Instalando versao DeepSeek-Visual...
call .\gradlew.bat :app:installDeepseekVisualDebug

if %ERRORLEVEL% EQU 0 (
    echo [3/3] Abrindo o app no emulador...
    "%ADB_PATH%" shell am start -n com.gurps.ficha.visual/com.gurps.ficha.MainActivity
    echo TUDO PRONTO! App atualizado.
) else (
    echo ERRO na compilacao. Verifique o log acima.
)
pause
