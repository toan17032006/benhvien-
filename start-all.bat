@echo off
chcp 65001 >nul
echo ============================================
echo   KHOI DONG UNG DUNG BENH VIEN
echo   Backend : http://localhost:8081
echo   Frontend: http://localhost:3000
echo ============================================
echo.

start "Backend - BenhVien (8081)" cmd /k "%~dp0start-backend.bat"
timeout /t 3 /nobreak >nul
start "Frontend - BenhVien (3000)" cmd /k "%~dp0start-frontend.bat"

echo.
echo Da mo 2 cua so: Backend va Frontend.
echo Cho khoang 30-60 giay (lan dau ton tai deps thi lau hon),
echo sau do mo trinh duyet tai: http://localhost:3000
echo.
echo De tat: dong 2 cua so, hoac chay stop-all.bat
timeout /t 10 >nul
