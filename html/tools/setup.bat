@echo off
cd /d "%~dp0"

:: Find Python 3
set PY=
for %%d in (
    "%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python311\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python310\python.exe"
    "C:\Python312\python.exe" "C:\Python311\python.exe" "C:\Python310\python.exe"
    "C:\Program Files\Python312\python.exe" "C:\Program Files\Python311\python.exe" "C:\Program Files\Python310\python.exe"
) do if exist "%%~d" set PY=%%~d

if "%PY%"=="" (
    for %%p in (python3 python) do (
        where %%p >nul 2>&1 && %%p --version 2>&1 | findstr "3\." >nul && set PY=%%p
    )
)

if "%PY%"=="" (
    echo Python 3 not found. Install from https://www.python.org/downloads/
    pause & exit /b 1
)

:: Install playwright if missing
%PY% -c "import playwright" 2>nul || %PY% -m pip install playwright -q

:: Kill any old server on port 8765, then start fresh
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8765 ^| findstr LISTENING') do taskkill //F //PID %%a 2>nul
start "" /MIN %PY% screenshot_server.py
