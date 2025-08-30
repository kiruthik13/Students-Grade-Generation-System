@echo off
echo ========================================
echo Student Grade Generation System (JDBC)
echo ========================================
echo.

set CLASSPATH_EXTRA=
set DBJAR=

if exist "mysql-connector-j*.jar" (
    for %%f in (mysql-connector-j*.jar) do set DBJAR=%%f
)

if "%DBJAR%"=="" (
    if exist "sqlite-jdbc.jar" (
        set DBJAR=sqlite-jdbc.jar
    )
)

if not "%DBJAR%"=="" (
    echo Using JDBC driver: %DBJAR%
    set CLASSPATH_EXTRA=../%DBJAR%
) else (
    echo No JDBC driver JAR found.
    echo - For SQLite: run download_sqlite.bat
    echo - For MySQL : place mysql-connector-j-<version>.jar in project root
)

echo.
echo Compiling Java files (with JDBC)...
cd src
if not "%CLASSPATH_EXTRA%"=="" (
    javac -cp ".;%CLASSPATH_EXTRA%" com\wipro\studentgrade\bean\*.java
    javac -cp ".;%CLASSPATH_EXTRA%" com\wipro\studentgrade\util\*.java
    javac -cp ".;%CLASSPATH_EXTRA%" com\wipro\studentgrade\dao\*.java
    javac -cp ".;%CLASSPATH_EXTRA%" com\wipro\studentgrade\service\*.java
    javac -cp ".;%CLASSPATH_EXTRA%" com\wipro\studentgrade\test\*.java
) else (
    javac com\wipro\studentgrade\bean\*.java
    javac com\wipro\studentgrade\util\*.java
    javac com\wipro\studentgrade\dao\*.java
    javac com\wipro\studentgrade\service\*.java
    javac com\wipro\studentgrade\test\*.java
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed.
    pause
    exit /b 1
)

echo.
echo Reading JDBC configuration from ..\db.properties (if present)...
for /f "tokens=1,2 delims==" %%a in (..\db.properties) do (
    if "%%a"=="jdbc.url" echo JDBC URL: %%b
)

echo.
echo Choose an option:
echo 1. Run interactive grade processor (JDBC)
echo 2. Run test cases
set /p choice="Enter your choice (1-2): "

if "%choice%"=="1" (
    if not "%CLASSPATH_EXTRA%"=="" (
        java -cp ".;%CLASSPATH_EXTRA%" com.wipro.studentgrade.service.GradeProcessor
    ) else (
        java com.wipro.studentgrade.service.GradeProcessor
    )
) else if "%choice%"=="2" (
    if not "%CLASSPATH_EXTRA%"=="" (
        java -cp ".;%CLASSPATH_EXTRA%" com.wipro.studentgrade.test.TestGradeSystem
    ) else (
        java com.wipro.studentgrade.test.TestGradeSystem
    )
) else (
    echo Invalid choice.
)

cd ..
