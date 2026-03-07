# Product Requirements Document (PRD)

## Feature: Standalone Bytecode Compiler and Interpreter Toolchain

---

## 1. Overview

This feature adds a standalone command-line toolchain for the stackvm project, consisting of:

1. **`asmcompiler.jar`** – Standalone assembly language compiler (`.asm` → `.bin` or run directly)
2. **`ccompiler.jar`** – Standalone C-like language compiler (`.c`/`.lox` → `.bin` or run directly)
3. **`vminterp.jar`** – Standalone bytecode interpreter (executes `.bin` files)

Inspiration is drawn from tools like `gcc`, `clang`, `kotlinc`, and `node` – which support compiling to output files, running directly, or both via flags.

---

## 2. Codebase Context

### Existing Modules
| Module | Purpose |
|--------|---------|
| `vm` | Kotlin Multiplatform core: CPU, compiler (asm→IntArray), decompiler, instructions |
| `assembler` | Old ANTLR-based assembler (deprecated, JVM-only) |
| `vmbin` | Kotlin Multiplatform entry point: compiles and runs asm on JVM/JS |
| `editor` | Swing IDE with `LittleCompiler`; contains exploratory C-like scanner in tests |
| `conversation` | DSL for conversation flows built on top of the VM |

### Key Classes
- `vm/compiler/Compiler.kt` – Compiles assembly text → `IntArray` bytecode
- `vm/CPU.kt` – Executes `IntArray` bytecode
- `vm/decompiler/ProgramDecompiler.kt` – Converts `IntArray` to structured `Instruction` list
- `vmbin/jvmMain/.../Main.kt` – Reads asm file, compiles, runs (no binary output today)
- `editor/ScannerTest.kt` – Exploratory Lox/C-like language scanner (keyword set: `class`, `fun`, `for`, `if`, `var`, `while`, `print`, `return`, etc.)

---

## 3. Assumptions and Decisions

### A. C-like Language Compiler
There is **no production C-like compiler** in the codebase today; `editor/ScannerTest.kt` contains an exploratory Lox-inspired scanner. 

**Decision**: The `ccompiler.jar` will implement a minimal Lox-style language (C-inspired syntax) that compiles to the same stackvm bytecode. The scanner from `ScannerTest.kt` serves as the starting point. The language supports: variables (`var`), arithmetic expressions, if/else, while loops, print, functions (`fun`), and return.

### B. Binary File Format
The compiled bytecode must be serialized to a binary file. 

**Decision**: Use a simple custom binary format:
- **Magic bytes**: `0x56 0x4D 0x42 0x43` ("VMBC")
- **Version** (1 byte): `0x01`
- **Instruction count** (4 bytes, big-endian int)
- **Instructions** (N × 4 bytes, each opcode/literal as big-endian int)

File extension: `.vmb` (VM Bytecode)

### C. Toolchain Structure
New Gradle subprojects will be created as JVM-only modules (not multiplatform), each producing a fat JAR via the Shadow plugin.

**Decision**: Three new modules:
- `asmcompiler` – Assembly → `.vmb` (and/or run)
- `ccompiler` – C-like language → `.vmb` (and/or run)
- `vminterp` – Runs `.vmb` files

### D. CLI Design
Inspired by `gcc`, `kotlinc`, `node`, and `clang`:

```
# Compile to binary
java -jar asmcompiler.jar input.asm -o output.vmb

# Compile and run (like script mode)
java -jar asmcompiler.jar input.asm --run

# Compile, output binary AND run
java -jar asmcompiler.jar input.asm -o output.vmb --run

# Default (no -o, no --run): compile and run (REPL-friendly default)
java -jar asmcompiler.jar input.asm

# Interpreter
java -jar vminterp.jar program.vmb

# C-like compiler (same interface)
java -jar ccompiler.jar input.c -o output.vmb
java -jar ccompiler.jar input.c --run
java -jar ccompiler.jar input.c
```

**Flags**:
| Flag | Description |
|------|------------|
| `-o <file>` | Output binary to `<file>` (default: derived from input name, e.g. `input.vmb`) |
| `--run` / `-r` | Execute the program after compilation |
| `--disassemble` / `-d` | Print disassembled bytecode to stdout |
| `--help` / `-h` | Show usage |
| `--version` / `-v` | Show version |

**Default behavior** (no flags): compile and run (useful for script-like usage, similar to how `node script.js` or `python script.py` works).

---

## 4. Functional Requirements

### 4.1 Assembly Compiler (`asmcompiler.jar`)
- Accepts a `.asm` source file as input
- Uses the existing `vm/compiler/Compiler.kt` to compile to `IntArray`
- Serializes the `IntArray` to `.vmb` binary format when `-o` is specified
- Executes the program using `CPU` when `--run` is specified
- Default behavior (no flags): compile and run
- Prints meaningful errors with line information on compilation failure
- Exits with code 0 on success, non-zero on error

### 4.2 C-like Language Compiler (`ccompiler.jar`)
- Accepts a `.c` source file as input (or any text file)
- Implements a Lox-inspired language subset:
  - Variable declarations: `var x = 10;`
  - Assignment: `x = 5;`
  - Arithmetic: `+`, `-`, `*`, `/`, `%`
  - Comparison: `==`, `!=`, `<`, `<=`, `>`, `>=`
  - Logical: `&&`, `||`, `!`
  - Control flow: `if`/`else`, `while`
  - Functions: `fun name(params) { ... }` and `return`
  - Print: `print expr;`
- Compiles to the same stackvm `IntArray` bytecode
- Same CLI interface as `asmcompiler.jar`
- Prints meaningful errors with line information on compilation failure

### 4.3 Bytecode Interpreter (`vminterp.jar`)
- Accepts a `.vmb` binary file as input
- Reads and validates the magic header and version
- Deserializes the `IntArray` bytecode
- Executes the program using `CPU`
- Prints runtime output
- Reports runtime errors with meaningful messages
- Exits with code 0 on success, non-zero on error

### 4.4 Binary Format Reader/Writer
A shared utility (in the `vm` module or a new shared module) for:
- `BytecodeWriter`: serializes `IntArray` → `.vmb` file
- `BytecodeReader`: deserializes `.vmb` file → `IntArray`, validates header/version

---

## 5. Non-Functional Requirements

- **Language**: Kotlin (JVM), consistent with the existing codebase
- **Build**: Gradle with Shadow plugin for fat JAR generation
- **Fat JAR**: Each tool produces a self-contained executable JAR (`shadowJar` task)
- **Error handling**: All tools must print user-friendly error messages; never print stack traces to the user
- **Exit codes**: `0` for success, `1` for compilation/runtime errors
- **No external runtime deps**: Fat JARs must be self-contained (all deps bundled)

---

## 6. Out of Scope

- REPL / interactive mode
- Debugger integration
- Optimizing compiler / IR passes
- Full Lox or C standard library
- JS or Native targets for the new standalone tools (JVM only)
- IDE integration (editor module) changes

---

## 7. Acceptance Criteria

1. `java -jar asmcompiler.jar hello.asm` compiles and executes `hello.asm`
2. `java -jar asmcompiler.jar hello.asm -o hello.vmb` produces a valid `hello.vmb` binary
3. `java -jar vminterp.jar hello.vmb` executes the compiled `hello.vmb`
4. `java -jar ccompiler.jar hello.c` compiles and executes a C-like source file
5. `java -jar ccompiler.jar hello.c -o hello.vmb` produces a valid binary
6. `java -jar vminterp.jar hello.vmb` also executes the C-compiled binary (same format)
7. All three jars print help when invoked with `--help`
8. Invalid input files produce user-friendly error messages and non-zero exit codes
9. All new modules build successfully with `./gradlew shadowJar`
