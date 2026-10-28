@echo off
chcp 65001 >nul
echo Dang tat Backend (cong 8081) va Frontend (cong 3000)...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1

echo Da tat xong.
timeout /t 3 >nul
