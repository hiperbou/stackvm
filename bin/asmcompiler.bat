@echo off
:: Run the asmcompiler on a given .asm file
:: Usage: asmcompiler.bat <file.asm> [options]
::   -r, --run          Execute the program (default)
::   -d, --disassemble  Disassemble and print the program
::   -o, --output FILE  Write compiled bytecode to FILE
::
:: Example: asmcompiler.bat example.asm
::          asmcompiler.bat example.asm -d
::          asmcompiler.bat example.asm -o out.vmb

setlocal
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%asmcompiler.jar

if not exist "%JAR%" (
    echo Error: asmcompiler.jar not found in %SCRIPT_DIR%
    echo Build it first with: gradlew :asmcompiler:shadowJar
    exit /b 1
)

java -jar "%JAR%" %*
