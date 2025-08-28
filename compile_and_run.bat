@echo off
echo ========================================
echo Student Grade Generation System
echo ========================================
echo.

echo Checking for SQLite JDBC driver...
if exist "sqlite-jdbc.jar" (
    echo SQLite JDBC driver found! Database functionality available.
    set DB_AVAILABLE=1
) else (
    echo SQLite JDBC driver not found. Database functionality unavailable.
    echo Run 'download_sqlite.bat' to download the driver.
    set DB_AVAILABLE=0
)

echo.
echo Compiling Java files...

if %DB_AVAILABLE%==1 (
    echo Compiling with database support...
    cd src
    javac -cp ".;../sqlite-jdbc.jar" com\wipro\studentgrade\bean\*.java
    javac -cp ".;../sqlite-jdbc.jar" com\wipro\studentgrade\util\*.java
    javac -cp ".;../sqlite-jdbc.jar" com\wipro\studentgrade\dao\*.java
    javac -cp ".;../sqlite-jdbc.jar" com\wipro\studentgrade\service\*.java
    javac -cp ".;../sqlite-jdbc.jar" com\wipro\studentgrade\test\*.java
) else (
    echo Compiling without database support...
    cd src
    javac com\wipro\studentgrade\bean\*.java
    javac com\wipro\studentgrade\util\*.java
    javac com\wipro\studentgrade\dao\*.java
    javac com\wipro\studentgrade\service\*.java
    javac com\wipro\studentgrade\test\*.java
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    
    if %DB_AVAILABLE%==1 (
        echo Choose an option:
        echo 1. Run interactive grade processor with DATABASE (Add/View/Delete Students)
        echo 2. Run test cases demonstration
        echo 3. Exit
        echo.
        set /p choice="Enter your choice (1-3): "
        
        if "%choice%"=="1" (
            echo.
            echo Running GradeProcessor with Database Support...
            echo Features:
            echo - Add new students with marks
            echo - View all stored students from database
            echo - Delete individual students
            echo - Clear all students
            echo - Data persists permanently in SQLite database
            echo.
            java -cp ".;../sqlite-jdbc.jar" com.wipro.studentgrade.service.GradeProcessor
        ) else if "%choice%"=="2" (
            echo.
            echo Running TestGradeSystem...
            java -cp ".;../sqlite-jdbc.jar" com.wipro.studentgrade.test.TestGradeSystem
        ) else if "%choice%"=="3" (
            echo Exiting...
            exit /b 0
        ) else (
            echo Invalid choice. Exiting...
            exit /b 1
        )
    ) else (
        echo Choose an option:
        echo 1. Run interactive grade processor (In-Memory Storage Only)
        echo 2. Run test cases demonstration
        echo 3. Exit
        echo.
        echo Note: Database functionality unavailable. Run 'download_sqlite.bat' first.
        echo.
        set /p choice="Enter your choice (1-3): "
        
        if "%choice%"=="1" (
            echo.
            echo Running GradeProcessor (In-Memory Only)...
            echo Note: Data will not persist between sessions.
            echo.
            java com.wipro.studentgrade.service.GradeProcessor
        ) else if "%choice%"=="2" (
            echo.
            echo Running TestGradeSystem...
            java com.wipro.studentgrade.test.TestGradeSystem
        ) else if "%choice%"=="3" (
            echo Exiting...
            exit /b 0
        ) else (
            echo Invalid choice. Exiting...
            exit /b 1
        )
    )
) else (
    echo.
    echo Compilation failed! Please check for errors.
    pause
    exit /b 1
)

echo.
echo Press any key to exit...
pause >nul
