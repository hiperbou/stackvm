# C-like Compiler for StackVM — Architecture Plan

## Project Overview

The VM is a **stack-based virtual machine** written in Kotlin (multiplatform). It has:
- A rich instruction set: arithmetic, logic, comparison, jumps, local/global variables, memory, function calls
- An existing **assembly language compiler** that parses assembly text → `IntArray` bytecode
- Plugin system for extra opcodes (e.g., `PrintPlugin` adds `PRINT`)
- A `CALL`/`RET` mechanism with stack frames for local variables

The new **C-like compiler** will be a new Gradle subproject (`ccompiler`) that depends on `vm`, and will compile C-like source code directly to `IntArray` bytecode.

---

## Answered Design Decisions

1. **New subproject**: `ccompiler` is a new Gradle subproject depending on `vm`
2. **`print(expr)` pops**: Compiles to evaluate expr + `PRINT` + `POP` (note: the VM's `PRINT` uses `peek()`, so `POP` must be emitted explicitly)
3. **`main` parameters ignored for now**: `argc`/`argv` are parsed but not loaded — documented as a missing feature below
4. **Integer-only**: The VM only works with `Int`; the language is integer-only — string printing is a future feature documented below
5. **Multiplatform target**: Start with JVM-only implementation, but **do not use Java classes or libraries directly** — encapsulate all platform-specific code so JS implementations can be substituted later
6. **Phase 1 comparison operators**: Not needed for Phase 1 (only needed from Phase 4 onward)

---

## Parser Strategy: Hybrid Recursive Descent + Pratt

The existing codebase uses a **Pratt parser** in `vm/src/commonMain/kotlin/com/hiperbou/vm/compiler/parser/` for the assembly language. However, that parser is tightly coupled to assembly concepts (`TokenType.OPCODE`, `TokenType.LABEL`, `TokenType.EOL`, `OpcodeInformation`) and produces `Expression` nodes that are immediately evaluated — it does not build a separate AST.

For the C-like compiler, we use a **hybrid approach** (the standard technique used by production compilers like Clang and Go):

```
Statement-level:  Recursive Descent
                  (function decls, blocks, if/else, while, for, return, var decls)
                        |
                        v
Expression-level: Pratt Parsing
                  (arithmetic, comparisons, function calls, unary ops)
```

**Why not reuse the existing Pratt parser directly?**
- Different token types (`CTokenType` vs `TokenType`)
- Different AST nodes (sealed `AstNode` hierarchy vs `Expression` interface)
- The existing parser is statement-oriented (one opcode per line); the C parser needs nested blocks

**What we borrow from the existing Pratt parser:**
- The same **Pratt algorithm pattern**: `PrefixParselet` / `InfixParselet` interfaces with `precedence`
- The same **`Precedence` object** structure (ASSIGNMENT, SUM, PRODUCT, PREFIX, POSTFIX, CALL)
- The `parseExpression(minPrecedence)` loop pattern

We implement a **new, self-contained** `CExpressionParser` using the Pratt technique, with `CPrefixParselet` / `CInfixParselet` interfaces operating on `CToken` and producing `AstNode.Expression` subtypes.

---

## Known Missing Features (Future Work)

> **`main` parameters (`argc`, `argv`)**: Currently ignored. The compiler parses them syntactically but does not load them from memory. A future phase will define a calling convention for passing program arguments via global memory.

> **String/ASCII printing**: The VM is integer-only. Printing ASCII strings (e.g., `print("hello")`) is not supported. A future phase could encode strings as sequences of integer character codes and emit a loop of `PRINT` calls, or add a dedicated `PRINTS` opcode to the VM.

---

## Target Language Design

The language is C99-inspired but simplified for scripting use. All values are `int`.

### Phase 1 — Minimal viable program:
```c
int main() {
    print(42);
    return 0;
}
```

### Phase 2 — Local variables:
```c
int main() {
    int x = 10;
    print(x);
    return 0;
}
```

### Phase 3 — Arithmetic:
```c
int main() {
    int x = 3 + 4 * 2;
    print(x);
    return 0;
}
```

### Phase 4 — if/else:
```c
int main() {
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
int main() {
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

int main() {
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
   CParser         (recursive descent for statements/declarations)
      |  uses
      +---------> CExpressionParser  (Pratt parser for expressions)
      |
      v
  AstNode          (sealed class hierarchy)
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
        CLexer.kt               # Tokenizer (pure Kotlin, no Java APIs)
        CToken.kt               # Token data class
        CTokenType.kt           # Token type enum (keywords, operators, punctuation)
      ast/
        AstNode.kt              # Sealed class hierarchy for AST nodes
      parser/
        CParser.kt              # Recursive descent parser (statements/declarations)
        CExpressionParser.kt    # Pratt parser for expressions
        CPrefixParselet.kt      # Interface for prefix parselets
        CInfixParselet.kt       # Interface for infix parselets
        CPrecedence.kt          # Precedence constants
        parselets/
          NumberParselet.kt     # Parses integer literals
          IdentifierParselet.kt # Parses variable references
          GroupParselet.kt      # Parses (expr)
          UnaryMinusParselet.kt # Parses -expr
          BinaryOpParselet.kt   # Parses left op right
          CallParselet.kt       # Parses func(args...)  [Phase 6]
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

4. **`print(expr)`**: Compiles to: evaluate expr (leaves value on stack) → `PRINT` → `POP`. The VM's `PRINT` instruction uses `peek()` so it does not consume the value; `POP` must be emitted explicitly.

5. **`main` entry point**: The program starts executing at address 0. If `main` is not the first function, a `JMP main` is emitted at the top.

6. **`return` statement**: Emits `RET`. For `main`, emits `HALT` (or `POP` + `HALT` if the return value is on the stack).

7. **Integer-only**: The VM only works with `Int`, so the language is integer-only.

8. **Multiplatform-safe**: All code in `commonMain`. No direct use of `java.*` or `kotlin.io.*` APIs. Platform-specific I/O (e.g., `println`) is encapsulated behind an interface so JS targets can provide their own implementation.

9. **Phase 1 scope**: No comparison operators needed. Phase 1 only requires `PUSH`, `PRINT`, `POP`, `HALT`.

10. **Pratt parser for expressions**: A new `CExpressionParser` uses the Pratt technique (same pattern as the existing `vm` parser) but with `CToken`/`AstNode` types. Statement-level parsing uses recursive descent in `CParser`.

---

## Pratt Expression Parser Design

The `CExpressionParser` follows the same pattern as the existing [`Parser`](vm/src/commonMain/kotlin/com/hiperbou/vm/compiler/parser/Parser.kt):

```kotlin
// CPrefixParselet.kt
interface CPrefixParselet {
    fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression
}

// CInfixParselet.kt
interface CInfixParselet {
    val precedence: Int
    fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression
}

// CPrecedence.kt
object CPrecedence {
    const val ASSIGNMENT = 1
    const val CONDITIONAL = 2
    const val SUM = 3
    const val PRODUCT = 4
    const val PREFIX = 5
    const val POSTFIX = 6
    const val CALL = 7
}
```

The `CExpressionParser.parseExpression(minPrecedence)` loop:
1. Consume next token
2. Look up prefix parselet → get `left` node
3. While next token's infix precedence > `minPrecedence`: consume, look up infix parselet, update `left`
4. Return `left`

---

## Incremental Feature Roadmap

| Phase | Feature | VM Instructions Used |
|-------|---------|---------------------|
| 1 | `main()` + `print(int_literal)` | `PUSH`, `PRINT`, `POP`, `HALT` |
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
| 12 | `main` parameters | `GLOAD` for `argc`/`argv` from reserved global slots |
| 13 | ASCII string printing | Future: encode as int arrays or new `PRINTS` opcode |

---

## `build.gradle` for `ccompiler`

```groovy
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.7.10'
}

group = 'com.hiperbou.vm'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

kotlin {
    jvm() {
        compilations.all {
            kotlinOptions.jvmTarget = '1.8'
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation project(':vm')
            }
        }
        commonTest {
            dependencies {
                implementation kotlin('test')
            }
        }
    }
}
```

`settings.gradle` must be updated to include `'ccompiler'` in the `include` list.

---

## Platform Encapsulation Strategy

To keep `commonMain` free of Java APIs:

- **String operations**: Use Kotlin stdlib only (`Char`, `String`, `CharArray`, etc.) — these are multiplatform-safe
- **I/O**: The `CodeGenerator` does not do any I/O. Tests use `kotlin-test` assertions only
- **`println` in tests**: Acceptable in `commonTest` since `kotlin.test` handles platform dispatch
- **No `java.util.regex`**: The lexer uses manual character-by-character scanning, not regex

---

## Implementation Plan (Phase 1 First)

### Step 1 — Gradle setup
- Create `ccompiler/build.gradle` (multiplatform, depends on `:vm`)
- Add `ccompiler` to root `settings.gradle`

### Step 2 — Lexer (`CLexer`, `CToken`, `CTokenType`)
- Token types: `INT` (keyword), `IDENTIFIER`, `NUMBER`, `LPAREN`, `RPAREN`, `LBRACE`, `RBRACE`, `SEMICOLON`, `COMMA`, `RETURN`, `PRINT` (builtin), `EOF`
- Pure Kotlin character scanning, no Java APIs

### Step 3 — AST nodes (`AstNode.kt`)
- `Program(functions: List<FunctionDecl>)`
- `FunctionDecl(name: String, params: List<Param>, body: Block)`
- `Block(statements: List<Statement>)`
- `ReturnStatement(expr: Expression)`
- `PrintStatement(expr: Expression)`
- `NumberLiteral(value: Int)`

### Step 4 — Pratt expression parser (`CPrecedence`, `CPrefixParselet`, `CInfixParselet`, `CExpressionParser`, parselets)
- Phase 1 only needs `NumberParselet` and `GroupParselet`
- Later phases add `IdentifierParselet`, `BinaryOpParselet`, `UnaryMinusParselet`, `CallParselet`

### Step 5 — Statement parser (`CParser`)
- Recursive descent
- Phase 1: parse `int name() { ... }` with `print(expr);` and `return expr;`
- Delegates expression parsing to `CExpressionParser`

### Step 6 — Code Generator (`CodeGenerator`, `SymbolTable`)
- Walk AST, emit instructions via `ProgramWriter`
- `print(expr)` → emit expr + `PRINT` + `POP`
- `return 0` in `main` → `PUSH 0` + `HALT`

### Step 7 — Entry point (`CCompiler.kt`)
- `fun compile(source: String): IntArray`

### Step 8 — Tests
- `LexerTest`: tokenize simple programs
- `ParserTest`: parse to AST
- `CCompilerTest`: compile + run in VM + assert output
