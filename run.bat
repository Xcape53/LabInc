@echo off
echo ====================================
echo   LabInc - Chemical Tycoon Game
echo ====================================
echo.

REM Sprawdź czy Maven jest zainstalowany
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [1/3] Maven znaleziony - budowanie projektu...
    call mvn clean package -q
    if %ERRORLEVEL% EQU 0 (
        echo [2/3] Projekt zbudowany pomyslnie!
        echo [3/3] Uruchamianie gry...
        echo.
        java -jar target\labinc-game-1.0.0-jar-with-dependencies.jar
    ) else (
        echo [ERROR] Blad podczas budowania projektu!
        pause
        exit /b 1
    )
) else (
    echo [INFO] Maven nie znaleziony - probuje bezposrednia kompilacja...
    
    REM Sprawdź czy Java jest zainstalowana
    where java >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Java nie znaleziona! Zainstaluj JDK 11 lub nowszy.
        pause
        exit /b 1
    )
    
    echo [1/3] Kompilowanie z javac...
    if not exist "bin" mkdir bin
    
    javac -d bin -sourcepath src\main\java src\main\java\com\labinc\*.java src\main\java\com\labinc\model\*.java src\main\java\com\labinc\gui\*.java
    
    if %ERRORLEVEL% EQU 0 (
        echo [2/3] Kompilacja zakonczona!
        echo [3/3] Uruchamianie gry...
        echo.
        cd bin
        java com.labinc.LabIncGame
        cd ..
    ) else (
        echo [ERROR] Blad podczas kompilacji!
        pause
        exit /b 1
    )
)

pause
