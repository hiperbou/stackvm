# Multiplatform Stack based VM in Kotlin
![](https://github.com/hiperbou/stackvm/actions/workflows/gradle.yml/badge.svg)
![Top Language](https://img.shields.io/github/languages/top/hiperbou/stackvm.svg?style=flat)
![Code Size](https://img.shields.io/github/languages/code-size/hiperbou/stackvm.svg?style=flat)
![License](https://img.shields.io/github/license/hiperbou/stackvm.svg?style=flat&logo=gnu)

A stack based VM implementation in Kotlin.
Working on JVM, Browser or Node


Web editor:
https://hiperbou-vm.pages.dev/

## MiniCVM Language and Compiler

MiniCVM is a simple, statically-typed, C-like programming language designed to compile to the assembly language of this stack-based Virtual Machine. It serves as a higher-level way to write programs for the VM.

### Features

The current implementation of MiniCVM supports the following features:

*   **Variables:** Declaration and assignment of `int` type variables.
*   **Functions:** Definition of functions with `int` parameters and `int` or `void` return types. A `main` function is required as the entry point.
*   **Control Flow:**
    *   `if/else` statements for conditional execution.
    *   `while` loops for iterative execution.
*   **Arrays:** Declaration and usage of one-dimensional `int` arrays.
*   **Operators:**
    *   Arithmetic: `+`, `-`, `*`, `/`, `%`
    *   Comparison: `==`, `!=`, `<`, `>`, `<=`, `>=`
    *   Logical: `&&`, `||`, `!` (unary not)
    *   Unary minus: `-`
*   **Literals:** Integer literals (e.g., `123`, `0`, `true`, `false`). `true` is treated as `1` and `false` as `0`.
*   **Comments:** Single-line (`// ...`) and multi-line (`/* ... */`) comments.

All variables and expressions are currently based on the `int` type.

### Compiler Usage (Kotlin)

The MiniCVM compiler is available as part of the Kotlin codebase. Developers can use the `com.hiperbou.vm.minicvm.MiniCvmCompiler` class to compile MiniCVM source code into bytecode, which can then be executed by the `com.hiperbou.vm.CPU`.

Here's a basic example of how to use the compiler:

```kotlin
import com.hiperbou.vm.minicvm.MiniCvmCompiler
import com.hiperbou.vm.CPU
// Import specific exception types if you want to catch them individually
// import com.hiperbou.vm.minicvm.lexer.LexerException
// import com.hiperbou.vm.minicvm.parser.ParserException
// import com.hiperbou.vm.minicvm.codegen.CodeGenException
// import com.hiperbou.vm.compiler.CompilerException // For assembler errors

fun main() {
    val miniCvmSource = """
      int main() {
        int a = 10;
        int b = 20;
        if (a < b) {
          return b - a; // Should return 10
        } else {
          return a - b;
        }
      }
    """

    val compiler = MiniCvmCompiler() // Instantiate the compiler

    try {
        println("Compiling MiniCVM source...")
        val bytecode = compiler.compile(miniCvmSource)
        println("Bytecode generated: ${bytecode.joinToString()}")

        // Now the bytecode can be loaded into the CPU
        println("Executing bytecode...")
        val cpu = CPU(bytecode, entryPoint = 0, programStackSize = 100, operandStackSize = 30)
        cpu.run() // Run until HALT

        if (cpu.isHalted && cpu.stackPointer > 0) {
            println("Execution finished. Result on stack: ${cpu.stack[0]}")
        } else if (cpu.isHalted) {
            println("Execution finished. Stack is empty.")
        } else {
            println("Execution did not complete normally.")
        }

    } catch (e: Exception) { // Catching generic Exception for simplicity
        // Specific exceptions like LexerException, ParserException, CodeGenException,
        // or com.hiperbou.vm.compiler.CompilerException can be caught for finer-grained error handling.
        System.err.println("Compilation or Execution Error: ${e.message}")
        e.printStackTrace()
    }
}
```

This demonstrates the basic workflow: write MiniCVM source, compile it to bytecode, and then execute the bytecode on the VM.