@echo off
:: Run the vminterp on a compiled .vmb bytecode file
:: Usage: vminterp.bat <file.vmb> [options]
::   -d, --disassemble  Disassemble and print the bytecode (instead of running)
::
:: Example: vminterp.bat example.vmb
::          vminterp.bat example.vmb -d
::
:: Tip: compile a .asm file first:
::   asmcompiler.bat example.asm -o example.vmb
::   vminterp.bat example.vmb

setlocal
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%vminterp.jar

if not exist "%JAR%" (
    echo Error: vminterp.jar not found in %SCRIPT_DIR%
    echo Build it first with: gradlew :vminterp:shadowJar
    exit /b 1
)

java -jar "%JAR%" %*
