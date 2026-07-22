@echo off
title Screenshot Tool
cd /d "%~dp0"

echo ========================================
echo   HTML Screenshot Tool
echo ========================================
echo.
echo Starting server...
echo.

C:\Users\QDM\AppData\Local\Programs\Python\Python310\python.exe screenshot_server.py

pause
