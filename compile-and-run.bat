@echo off
echo ============================================
echo   Kompilacja i uruchomienie LabInc (Maven)
echo ============================================
echo.

cd /d "%~dp0"

echo [0/2] Zamykanie poprzednich instancji Java...
taskkill /f /im java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

echo [1/2] Budowanie projektu Maven (clean package)...
call mvn clean package -DskipTests
if errorlevel 1 goto error

echo [2/2] Uruchamianie gry...
echo.
java -jar target\labinc-game-1.0.0-jar-with-dependencies.jar

goto end

:error
echo.
echo [ERROR] Budowanie Maven nie powiodlo sie!
echo.
pause
exit /b 1

:end
echo.
echo Gra zakonczona.
pause
