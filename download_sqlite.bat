@echo off
echo ========================================
echo Downloading SQLite JDBC Driver
echo ========================================
echo.

echo This script will download the SQLite JDBC driver needed for database connectivity.
echo.

set /p download="Do you want to download the SQLite JDBC driver? (y/n): "

if /i "%download%"=="y" (
    echo.
    echo Downloading SQLite JDBC driver...
    
    powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/xerial/sqlite-jdbc/releases/download/3.42.0.0/sqlite-jdbc-3.42.0.0.jar' -OutFile 'sqlite-jdbc.jar'}"
    
    if exist "sqlite-jdbc.jar" (
        echo.
        echo Download successful! SQLite JDBC driver saved as 'sqlite-jdbc.jar'
        echo.
        echo To use the database functionality:
        echo 1. Place 'sqlite-jdbc.jar' in the same directory as your Java files
        echo 2. Compile with: javac -cp ".;sqlite-jdbc.jar" com/wipro/studentgrade/*/*.java
        echo 3. Run with: java -cp ".;sqlite-jdbc.jar" com.wipro.studentgrade.service.GradeProcessor
        echo.
    ) else (
        echo.
        echo Download failed! Please check your internet connection.
        echo.
    )
) else (
    echo.
    echo Download cancelled.
    echo.
    echo Note: You need the SQLite JDBC driver to use database functionality.
    echo You can download it manually from: https://github.com/xerial/sqlite-jdbc/releases
)

echo Press any key to exit...
pause >nul
