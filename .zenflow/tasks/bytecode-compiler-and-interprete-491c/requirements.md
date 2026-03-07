# Product Requirements Document (PRD)

## Feature: Standalone Bytecode Compiler and Interpreter Toolchain

---

## 1. Overview

This feature adds a standalone command-line toolchain for the stackvm project, consisting of:

1. **`asmcompiler.jar`** – Standalone assembly language compiler (`.asm` → `.vmb` binary, and/or run directly)
2. **`ccompiler.jar`** – Standalone C-like language compiler (`.c` → `.vmb` binary, and/or run directly)
3. **`vminterp.jar`** – Standalone bytecode interpreter (executes `.vmb` binary files)

Inspiration is drawn from tools like `gcc`, `clang`, `kotlinc`, and `node` — which support compiling to output files, running directly, or both via flags.

---

## 2. Codebase Context

### Existing Modules
| Module | Purpose |
|--------|---------|
| `vm` | Kotlin Multiplatform core: CPU, asm compiler (text → IntArray), decompiler, instructions |
| `assembler` | Old ANTLR-based assembler (deprecated, JVM-only) |
| `vmbin` | Kotlin Multiplatform: compiles and **runs** asm source; has JVM + JS targets |
| `ccompiler` | Kotlin Multiplatform: C-like language compiler (source → IntArray bytecode) — **already implemented** |
| `ccompilerbin` | Kotlin Multiplatform: web editor for the C-like compiler; has JVM + JS targets |
| `editor` | Swing IDE with `LittleCompiler` |
| `conversation` | DSL for conversation flows built on top of the VM |

### Key Classes
- `vm/compiler/Compiler.kt` – Compiles assembly text → `IntArray` bytecode
- `vm/CPU.kt` – Executes `IntArray` bytecode
- `ccompiler/CCompiler.kt` – Compiles C-like source text → `IntArray` bytecode (entry point)
- `ccompiler/codegen/CodeGenerator.kt` – AST → VM instructions
- `ccompiler/lexer/CLexer.kt` – C-like language tokenizer
- `ccompiler/parser/CParser.kt` – C-like language recursive-descent parser
- `vmbin/jvmMain/.../Main.kt` – Reads asm file, compiles, runs (no binary output today)
- `ccompilerbin/jsMain/.../WebEditor.kt` – Web editor for C-like language (JS target)

### What's Missing (the work to be done)
Neither `vmbin` nor `ccompilerbin` currently:
- Outputs a binary `.vmb` file
- Accepts command-line flags (`-o`, `--run`, `--disassemble`, etc.)
- Has a JVM `main` for `ccompilerbin`

There is also **no standalone bytecode interpreter** that can read and execute a pre-compiled binary file.

---

## 3. Binary File Format

A simple custom binary format (`.vmb` — VM Bytecode):

| Field | Size | Value |
|-------|------|-------|
| Magic | 4 bytes | `0x56 0x4D 0x42 0x43` ("VMBC") |
| Version | 1 byte | `0x01` |
| Instruction count | 4 bytes | Big-endian `Int` |
| Instructions | N × 4 bytes | Each opcode/literal as big-endian `Int` |

A shared `BytecodeFile` utility (in `vm` module or a new `vmformat` module) handles read/write of this format using only Kotlin multiplatform APIs.

---

## 4. CLI Design

Inspired by `gcc`, `kotlinc`, `node`, and `clang`:

```
# Default (no flags) — compile and run, like `python script.py`
java -jar asmcompiler.jar input.asm
java -jar ccompiler.jar input.c

# Compile to binary only
java -jar asmcompiler.jar input.asm -o output.vmb
java -jar ccompiler.jar input.c -o output.vmb

# Compile to binary AND run
java -jar asmcompiler.jar input.asm -o output.vmb --run
java -jar ccompiler.jar input.c -o output.vmb --run

# Run only (compile + run, discard binary)
java -jar asmcompiler.jar input.asm --run

# Interpreter: run a pre-compiled binary
java -jar vminterp.jar program.vmb

# Disassemble
java -jar asmcompiler.jar input.asm --disassemble
java -jar ccompiler.jar input.c --disassemble
java -jar vminterp.jar program.vmb --disassemble
```

### Flags
| Flag | Short | Description |
|------|-------|-------------|
| `--output <file>` | `-o <file>` | Write compiled binary to file (default: input filename with `.vmb` extension) |
| `--run` | `-r` | Execute the program after compilation |
| `--disassemble` | `-d` | Print disassembled bytecode to stdout instead of running |
| `--help` | `-h` | Show usage |
| `--version` | `-v` | Show version |

**Default behavior** (no flags): compile and run — like `python script.py` or `node script.js`.

---

## 5. Functional Requirements

### 5.1 Assembly Compiler (`asmcompiler.jar`)
- New standalone JVM module (separate from `vmbin`, which targets both JVM + JS)
- Reads a `.asm` source file
- Uses the existing `vm/compiler/Compiler.kt` to compile to `IntArray`
- Serializes bytecode to `.vmb` format when `-o` is specified
- Executes the program using `CPU` when `--run` is specified (or by default)
- Prints disassembly when `--disassemble` is specified
- Prints meaningful errors with line information on compilation failure
- Exits with code `0` on success, non-zero on error

### 5.2 C-like Language Compiler (`ccompiler.jar`)
- New standalone JVM entry point (the `ccompilerbin` module currently has no JVM `main`)
- Reads a `.c` source file
- Uses the existing `ccompiler/CCompiler.kt` to compile to `IntArray`
- Same CLI interface and behavior as the assembly compiler
- Prints meaningful errors with line information on compilation failure

### 5.3 Bytecode Interpreter (`vminterp.jar`)
- New standalone JVM module
- Reads a `.vmb` binary file
- Validates magic header and version
- Deserializes the `IntArray` bytecode
- Executes the program using `CPU`
- Supports `--disassemble` flag to print instructions without executing
- Reports runtime errors with meaningful messages
- Exits with code `0` on success, non-zero on error

### 5.4 Bytecode File Format Utility
- Shared read/write utility (added to `vm` module's `commonMain`)
- `BytecodeWriter`: serializes `IntArray` → `.vmb` bytes
- `BytecodeReader`: deserializes `.vmb` bytes → `IntArray`, validates header/version
- Pure Kotlin multiplatform (no `java.io` directly in `commonMain`)
- JVM platform implementations use `java.io.File`

---

## 6. New Modules / Changes

| Module | Change |
|--------|--------|
| `vm` | Add `BytecodeWriter` / `BytecodeReader` in `commonMain`; JVM-specific `File` I/O in `jvmMain` |
| `asmcompiler` | **New** JVM-only Gradle module with Shadow plugin; fat JAR |
| `ccompilerbin` | Add `jvmMain` entry point with full CLI support |
| `vminterp` | **New** JVM-only Gradle module with Shadow plugin; fat JAR |
| `settings.gradle` | Add `asmcompiler`, `vminterp` |

> **Note**: `ccompilerbin` already exists and will receive a JVM `main`. A shadow JAR task will be added to it. Alternatively, a separate `ccompiler-standalone` module may be cleaner — decision deferred to Technical Spec.

---

## 7. Non-Functional Requirements

- **Language**: Kotlin, consistent with the existing codebase
- **Build**: Gradle with Shadow plugin (`com.github.johnrengelman.shadow`) for fat JAR generation
- **Self-contained JARs**: Each tool bundles all dependencies (no classpath setup needed)
- **Error handling**: User-friendly error messages; no raw stack traces printed to the user
- **Exit codes**: `0` for success, `1` for user errors (bad args, compile errors, runtime errors)
- **Multiplatform safety**: `BytecodeWriter`/`BytecodeReader` in `commonMain` must use only Kotlin multiplatform APIs

---

## 8. Out of Scope

- REPL / interactive mode
- Debugger integration
- Optimizing compiler / IR passes
- Full C standard library support
- IDE integration (editor module) changes
- JS or Native targets for the new standalone tools (JVM only)

---

## 9. Acceptance Criteria

1. `java -jar asmcompiler.jar hello.asm` compiles and runs `hello.asm`
2. `java -jar asmcompiler.jar hello.asm -o hello.vmb` produces a valid `.vmb` file
3. `java -jar vminterp.jar hello.vmb` executes the compiled binary correctly
4. `java -jar ccompiler.jar hello.c` compiles and runs a C-like source file
5. `java -jar ccompiler.jar hello.c -o hello.vmb` produces a valid `.vmb` file
6. `java -jar vminterp.jar hello.vmb` also executes binaries compiled by `ccompiler.jar`
7. All three JARs print usage when invoked with `--help`
8. All three JARs print disassembly when invoked with `--disassemble`
9. Invalid input produces user-friendly error messages and non-zero exit codes
10. All new/modified modules build successfully with `gradlew shadowJar` (or equivalent)
