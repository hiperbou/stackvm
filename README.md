# Multiplatform Stack based VM in Kotlin
![](https://github.com/hiperbou/stackvm/actions/workflows/gradle.yml/badge.svg)
![Top Language](https://img.shields.io/github/languages/top/hiperbou/stackvm.svg?style=flat)
![Code Size](https://img.shields.io/github/languages/code-size/hiperbou/stackvm.svg?style=flat)
![License](https://img.shields.io/github/license/hiperbou/stackvm.svg?style=flat&logo=gnu)

A stack-based VM implementation in Kotlin, targeting JVM, Browser, and Node.

Web editor: https://hiperbou-vm.pages.dev/

---

## Modules

| Module | Description |
|--------|-------------|
| [`vm`](vm/) | Core VM runtime: CPU, stack, frame management, compiler, decompiler, disassembler, and bytecode format |
| [`asmcompiler`](asmcompiler/) | Assembly language compiler CLI → `bin/asmcompiler.jar` |
| [`ccompilerbin`](ccompilerbin/) | C-like language compiler CLI → `bin/ccompiler.jar` |
| [`vminterp`](vminterp/) | Bytecode interpreter CLI → `bin/vminterp.jar` |
| [`vmbin`](vmbin/) | Browser/Node VM runner |
| [`ccompiler`](ccompiler/) | C-like language compiler library |
| [`assembler`](assembler/) | Legacy ANTLR-based assembler |
| [`editor`](editor/) | Swing-based IDE |

## CLI Tools Quick Start

After building, all JARs land in `bin/`. See **[docs/cli-tools.md](docs/cli-tools.md)** for full documentation.

```bash
# Build all CLI tools
./gradlew :asmcompiler:shadowJar :ccompilerbin:shadowJar :vminterp:shadowJar

# Compile and run an assembly program
bin/asmcompiler.bat bin/example.asm

# Compile and run a C-like program
bin/ccompiler.bat bin/example.cvm

# Compile ASM to binary, then run with the interpreter
bin/asmcompiler.bat bin/example.asm -o bin/example.vmb
bin/vminterp.bat bin/example.vmb
```
