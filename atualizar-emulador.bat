@echo off
setlocal

cd /d "%~dp0"
call .\gradlew.bat :app:installDebug --no-daemon

if errorlevel 1 (
  echo.
  echo Falha ao atualizar o app no emulador.
  exit /b 1
)

echo.
echo App atualizado no emulador com sucesso.
exit /b 0
