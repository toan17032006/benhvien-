@echo off
chcp 65001 >nul
title Frontend - Next.js (cong 3000)
cd /d "%~dp0frontend"

echo ============================================
echo   FRONTEND - Next.js (cong 3000)
echo   Lan dau chay se tai node_modules (vai phut)
echo ============================================
echo.

if not exist "node_modules" (
    echo Dang cai dat dependencies...
    call npm install
)

call npm run dev

pause
