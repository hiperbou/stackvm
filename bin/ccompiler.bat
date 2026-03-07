@echo off
:: Run the ccompiler on a given .c file
:: Usage: ccompiler.bat <file.c> [options]
::   -r, --run          Execute the program (default)
::   -d, --disassemble  Disassemble and print the program
::   -o, --output FILE  Write compiled bytecode to FILE
::
:: Example: ccompiler.bat example.c
::          ccompiler.bat example.c -d
::          ccompiler.bat example.c -o out.vmb

setlocal
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%ccompiler.jar

if not exist "%JAR%" (
    echo Error: ccompiler.jar not found in %SCRIPT_DIR%
    echo Build it first with: gradlew :ccompilerbin:shadowJar
    exit /b 1
)

java -jar "%JAR%" %*
