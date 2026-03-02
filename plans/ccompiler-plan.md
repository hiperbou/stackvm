# C-like Compiler for StackVM — Architecture Plan

## Project Overview

The VM is a **stack-based virtual machine** written in Kotlin (multiplatform). It has:
- A rich instruction set: arithmetic, logic, comparison, jumps, local/global variables, memory, function calls
- An existing **assembly language compiler** that parses assembly text → `IntArray` bytecode
- Plugin system for extra opcodes (e.g., `PrintPlugin` adds `PRINT`)
- A `CALL`/`RET` mechanism with stack frames for local variables

The new **C-like compiler** will be a new Gradle subproject (e.g., `ccompiler`) that depends on `vm`, and will compile C-like source code directly to `IntArray` bytecode.

---

## Target Language Design

The language will be C99-inspired but simplified for scripting use. Here's the progression:

### Phase 1 — Minimal viable program:
```c
int main(int argc, int argv) {
    print(42);
    return 0;
}
```

### Phase 2 — Local variables:
```c
int main(int argc, int argv) {
    int x = 10;
    print(x);
    return 0;
}
```

### Phase 3 — Arithmetic:
```c
int main(int argc, int argv) {
    int x = 3 + 4 * 2;
    print(x);
    return 0;
}
```

### Phase 4 — if/else:
```c
int main(int argc, int argv) {
    int x = 5;
    if (x > 3) {
        print(1);
    } else {
        print(0);
    }
    return 0;
}
```

### Phase 5 — while/for loops:
```c
int main(int argc, int argv) {
    int sum = 0;
    for (int i = 0; i < 10; i++) {
        sum += i;
    }
    print(sum);
    return 0;
}
```

### Phase 6 — User-defined functions:
```c
int add(int a, int b) {
    return a + b;
}

int main(int argc, int argv) {
    print(add(3, 4));
    return 0;
}
```

---

## Architecture

```
C-like Source Code
      |
      v
   CLexer          (tokenizes source into CToken stream)
      |
      v
   CParser         (recursive descent, produces AST)
      |
      v
  AST Nodes        (sealed class hierarchy)
      |
      v
 CodeGenerator     (walks AST, emits VM instructions via ProgramWriter)
      |
      v
  IntArray         (VM bytecode, ready to run in CPU)
```

## Module Structure

New subproject: `ccompiler/`

```
ccompiler/
  build.gradle
  src/
    commonMain/kotlin/com/hiperbou/vm/ccompiler/
      CCompiler.kt              # Entry point: source -> IntArray
      lexer/
        CLexer.kt               # Tokenizer
        CToken.kt               # Token data class
        CTokenType.kt           # Token type enum (keywords, operators, punctuation)
      ast/
        AstNode.kt              # Sealed class hierarchy for AST nodes
      parser/
        CParser.kt              # Recursive descent parser
      codegen/
        CodeGenerator.kt        # AST -> VM bytecode
        SymbolTable.kt          # Variable/function tracking
    commonTest/kotlin/com/hiperbou/vm/ccompiler/
      CCompilerTest.kt          # Integration tests (source -> run -> assert output)
      LexerTest.kt
      ParserTest.kt
      CodeGenTest.kt
```

---

## Key Design Decisions

1. **New module** (`ccompiler`) depends on `vm` module — reuses `ProgramWriter`, `LabelResolver`, and `Instructions`

2. **Variable mapping**: Local variables map to VM frame slots (0, 1, 2...) using `LOAD`/`STORE`. Global variables use `GLOAD`/`GSTORE`.

3. **Function calls**: Use VM's `CALL`/`RET` mechanism. Parameters are pushed onto the stack before `CALL`, then `STORE`d into local variables at function entry.

4. **`print(expr)`**: Compiles to evaluate expr (leaves value on stack) + `PRINT` + `POP`

5. **`main` entry point**: The program starts executing at address 0. If `main` is not the first function, a `JMP main` is emitted at the top.

6. **`return` statement**: Emits `RET`. For `main`, emits `HALT`.

7. **Integer-only**: The VM only works with `Int`, so the language is integer-only.

---

## Incremental Feature Roadmap

| Phase | Feature | VM Instructions Used |
|-------|---------|---------------------|
| 1 | `main()` + `print(int_literal)` | `PUSH`, `PRINT`, `HALT` |
| 2 | Local `int` variables | `PUSH`, `STORE`, `LOAD` |
| 3 | Arithmetic expressions | `ADD`, `SUB`, `MUL`, `DIV`, `MOD` |
| 4 | `if`/`else` | `JIF`, `JMP` + comparison ops |
| 5 | `while` loop | `JIF`, `JMP` |
| 6 | `for` loop | `JIF`, `JMP` |
| 7 | User functions + `return` | `CALL`, `RET`, `STORE`/`LOAD` for params |
| 8 | Global variables | `GLOAD`, `GSTORE` |
| 9 | Comparison/logical ops | `EQ`, `NE`, `LT`, `GT`, `LTE`, `GTE`, `AND`, `OR`, `NOT` |
| 10 | Compound assignment `+=`, `-=` | `LOAD`, `ADD`, `STORE` |
| 11 | `++`/`--` | `LOAD`, `PUSH 1`, `ADD`/`SUB`, `STORE` |

---

## Open Questions

1. Should the new compiler be a new Gradle subproject (`ccompiler`) or placed inside the existing `vm` module?
2. Should `print(x)` pop the value after printing, or leave it on the stack?
3. Should `main` parameters (`argc`, `argv`) be pre-loaded from global memory, or ignored initially?
4. Should the compiler be Kotlin Multiplatform (JVM + JS) or JVM-only?
