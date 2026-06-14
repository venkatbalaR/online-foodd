@echo off
echo Starting Online Food Ordering - Backend (Spring Boot on port 8085)...
cd /d "%~dp0OnlineFoodOrderingSystem\backend"
mvn spring-boot:run
pause
