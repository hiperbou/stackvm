#!/usr/bin/env sh
# Run the ccompiler on a given .c file
# Usage: ./ccompiler.sh <file.c> [options]
#   -r, --run          Execute the program (default)
#   -d, --disassemble  Disassemble and print the program
#   -o, --output FILE  Write compiled bytecode to FILE
#
# Example: ./ccompiler.sh example.c
#          ./ccompiler.sh example.c -d
#          ./ccompiler.sh example.c -o out.vmb

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/ccompiler.jar"

if [ ! -f "$JAR" ]; then
    echo "Error: ccompiler.jar not found in $SCRIPT_DIR"
    echo "Build it first with: ./gradlew :ccompilerbin:shadowJar"
    exit 1
fi

java -jar "$JAR" "$@"
