@echo off
echo Starting Online Food Ordering - Frontend...
cd /d "%~dp0frontend"
set PATH=C:\Program Files\nodejs;%PATH%
npm run dev
pause
