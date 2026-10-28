@echo off
chcp 65001 >nul
title Backend - Spring Boot (cong 8081)
cd /d "%~dp0backend"

echo ============================================
echo   BACKEND - Spring Boot (cong 8081)
echo   Lan dau chay se tai dependencies (vai phut)
echo ============================================
echo.

call mvnw.cmd spring-boot:run

pause
