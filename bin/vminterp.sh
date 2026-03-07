#!/usr/bin/env sh
# Run the vminterp on a compiled .vmb bytecode file
# Usage: ./vminterp.sh <file.vmb> [options]
#   -d, --disassemble  Disassemble and print the bytecode (instead of running)
#
# Example: ./vminterp.sh example.vmb
#          ./vminterp.sh example.vmb -d
#
# Tip: compile a .asm file first:
#   ./asmcompiler.sh example.asm -o example.vmb
#   ./vminterp.sh example.vmb

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/vminterp.jar"

if [ ! -f "$JAR" ]; then
    echo "Error: vminterp.jar not found in $SCRIPT_DIR"
    echo "Build it first with: ./gradlew :vminterp:shadowJar"
    exit 1
fi

java -jar "$JAR" "$@"
