#!/usr/bin/env sh
# Run the asmcompiler on a given .asm file
# Usage: ./asmcompiler.sh <file.asm> [options]
#   -r, --run          Execute the program (default)
#   -d, --disassemble  Disassemble and print the program
#   -o, --output FILE  Write compiled bytecode to FILE
#
# Example: ./asmcompiler.sh example.asm
#          ./asmcompiler.sh example.asm -d
#          ./asmcompiler.sh example.asm -o out.vmb

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/asmcompiler.jar"

if [ ! -f "$JAR" ]; then
    echo "Error: asmcompiler.jar not found in $SCRIPT_DIR"
    echo "Build it first with: ./gradlew :asmcompiler:shadowJar"
    exit 1
fi

java -jar "$JAR" "$@"
