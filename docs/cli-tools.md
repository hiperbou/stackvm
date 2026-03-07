# CLI Tools

This document covers the three standalone command-line tools produced by this project. Each tool is packaged as a self-contained fat JAR via the Shadow plugin and is also available as a launch script in the `bin/` directory.

---

## Building All Tools

```bash
# Build all three JARs (they are automatically copied to bin/)
./gradlew :asmcompiler:shadowJar :ccompilerbin:shadowJar :vminterp:shadowJar
```

Output JARs are placed in `bin/` alongside the launch scripts.

---

## `asmcompiler` — Assembly Language Compiler

**Module:** `:asmcompiler`  
**JAR:** `bin/asmcompiler.jar`  
**Launcher:** `bin/asmcompiler.bat` / `bin/asmcompiler.sh`

Compiles `.asm` files written in the VM assembly language into VM bytecode.

### Usage

```
asmcompiler [options] <input.asm>

Options:
  -o, --output <file>   Write compiled bytecode to <file> (.vmb)
  -r, --run             Execute the compiled program (default when no flags given)
  -d, --disassemble     Disassemble and print the compiled program
  -h, --help            Show help and exit
  -v, --version         Show version and exit
```

### Examples

```bash
# Run the program immediately (default)
asmcompiler.bat example.asm

# Compile to a binary file without running
asmcompiler.bat example.asm -o example.vmb

# Compile to binary AND run in the same invocation
asmcompiler.bat example.asm -o example.vmb -r

# Disassemble (pretty-print the compiled instructions)
asmcompiler.bat example.asm -d

# Compile to binary AND disassemble in one pass
asmcompiler.bat example.asm -o example.vmb -d
```

### Assembly Language Syntax

```asm
// Constants and immediate labels
n:     5        // symbolic constant
i_slot: 0       // local variable slot index

// Push a literal value
PUSH 42

// Arithmetic
PUSH 10
PUSH 3
ADD             // → 13 on stack

// Local variables
PUSH 0
STORE i_slot    // store 0 in slot 0
LOAD  i_slot    // load slot 0

// Conditional jump
LOAD  i_slot
PUSH  n
GT
JIF   loop_done // jump if top-of-stack is non-zero

// Labels
loop_start:
    ...
    JMP loop_start

loop_done:
HALT

// Print plugin instruction
PUSH 99
PRINT           // prints 99

// Comments (// and /* ... */)
// This is a comment
/* This is also a comment */
```

See `bin/example.asm` for a complete working program (sum of 1..5 with PRINT output).

---

## `ccompiler` — C-like Language Compiler

**Module:** `:ccompilerbin`  
**JAR:** `bin/ccompiler.jar`  
**Launcher:** `bin/ccompiler.bat` / `bin/ccompiler.sh`

Compiles `.cvm` files written in a C-like language into VM bytecode.  
The language supports functions, local/global variables, control flow, arrays, and standard operators.

### Usage

```
ccompiler [options] <input.cvm>

Options:
  -o, --output <file>   Write compiled bytecode to <file> (.vmb)
  -r, --run             Execute the compiled program (default when no flags given)
  -d, --disassemble     Disassemble and print the compiled program
  -h, --help            Show help and exit
  -v, --version         Show version and exit
```

### Examples

```bash
# Run a .cvm source file immediately (default)
ccompiler.bat example.cvm

# Compile to binary
ccompiler.bat example.cvm -o example.vmb

# Compile to binary AND run in the same invocation
ccompiler.bat example.cvm -o example.vmb -r

# Disassemble to see the generated assembly
ccompiler.bat example.cvm -d

# Compile to binary AND disassemble in one pass
ccompiler.bat example.cvm -o example.vmb -d
```

### C-like Language Features

```c
// Functions and recursion
int fib(int n) {
    if (n <= 1) { return n; }
    return fib(n - 1) + fib(n - 2);
}

// Global variables
int counter = 0;

int main() {
    // Local variables and arithmetic
    int x = 10, y = 3;
    print(x + y);       // → 13
    print(x % y);       // → 1

    // For loop
    for (int i = 0; i < 5; i++) {
        print(fib(i));  // → 0 1 1 2 3
    }

    // While loop
    int n = 3;
    while (n > 0) {
        print(n--);     // → 3 2 1
    }

    // Do-while
    do {
        print(0);
    } while (0);        // executes once

    // Arrays
    int arr[4];
    for (int j = 0; j < 4; j++) {
        arr[j] = j * j;
        print(arr[j]);  // → 0 1 4 9
    }

    // Switch
    int v = 2;
    switch (v) {
        case 1: print(10); break;
        case 2: print(20); break;
        default: print(30);
    }

    // Operators: + - * / % == != < > <= >= && || ! & | ^ ~ ?: += -=  ++ --
    return 0;
}
```

See `bin/example.cvm` for a complete working program.

---

## `vminterp` — Bytecode Interpreter

**Module:** `:vminterp`  
**JAR:** `bin/vminterp.jar`  
**Launcher:** `bin/vminterp.bat` / `bin/vminterp.sh`

Executes pre-compiled `.vmb` bytecode files directly, without needing the source.  
Useful for distributing programs that should run on the VM without exposing source code.

### Usage

```
vminterp [options] <bytecode.vmb>

Options:
  -d, --disassemble     Disassemble and print the bytecode (instead of running)
  -h, --help            Show help and exit
  -v, --version         Show version and exit
```

### Examples

```bash
# Execute a compiled binary
vminterp.bat example.vmb

# Disassemble a binary back to readable assembly
vminterp.bat example.vmb -d
```

---

## Full Pipeline Walkthrough

### ASM → binary → interpreter

```bash
cd bin

# 1. Write or edit your assembly program
#    (edit example.asm)

# 2. Compile ASM source to a .vmb binary
asmcompiler.bat example.asm -o example.vmb

# 3. Run it directly via the interpreter
vminterp.bat example.vmb

# 4. Or disassemble it to inspect the instructions
vminterp.bat example.vmb -d
```

### C → binary → interpreter

```bash
cd bin

# 1. Write or edit your C-like program
#    (edit example.cvm)

# 2. Compile to a .vmb binary
ccompiler.bat example.cvm -o example.vmb

# 3. Run via the interpreter
vminterp.bat example.vmb

# 4. Or disassemble to see the generated assembly
vminterp.bat example.vmb -d

# 5. Or compile and run in one step (no binary saved)
ccompiler.bat example.cvm
```

---

## Module Overview

| Module | Description | Output JAR |
|--------|-------------|------------|
| `:vm` | Core VM runtime (CPU, compiler, decompiler, disassembler, bytecode format) | *(library)* |
| `:asmcompiler` | Assembly language compiler CLI | `asmcompiler.jar` |
| `:ccompilerbin` | C-like language compiler CLI | `ccompiler.jar` |
| `:vminterp` | Bytecode interpreter CLI | `vminterp.jar` |
| `:vmbin` | Web/Node VM runner | *(browser/node)* |
| `:assembler` | Legacy ANTLR-based assembler | `assembler.jar` |
| `:editor` | Swing-based IDE for the VM | *(desktop app)* |

---

## Bytecode File Format (`.vmb`)

| Field | Size | Value |
|-------|------|-------|
| Magic | 4 bytes | `0x56 0x4D 0x42 0x43` (`VMBC`) |
| Version | 1 byte | `0x01` |
| Instructions | 4 bytes each | Big-endian 32-bit signed integers |

An `InvalidBytecodeFileException` is thrown by `vminterp` if the magic bytes or version are wrong.
