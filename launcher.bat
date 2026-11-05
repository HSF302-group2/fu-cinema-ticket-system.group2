@echo off
:: Launcher script for FU Cinema Ticket System
:: Uses Maven Wrapper to start the Spring Boot application

setlocal

rem Ensure we are in the script directory
cd /d "%~dp0"

rem Run the application using Maven Wrapper
if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    echo Maven Wrapper not found. Please run "mvn spring-boot:run" manually.
)

endlocal
