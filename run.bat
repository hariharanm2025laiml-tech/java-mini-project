@echo off
echo ====================================================
echo Starting Delivery and Logistics Management System...
echo ====================================================

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    mvn exec:java
) else (
    if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
        "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" exec:java
    ) else (
        echo Maven executable not detected in PATH.
        echo Compiling and running directly with Java...
        java -cp "target/classes;target/dependency/*;%USERPROFILE%/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar;%USERPROFILE%/.m2/repository/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar" Main
    )
)
pause
