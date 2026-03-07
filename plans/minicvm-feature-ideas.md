# MiniCvm Ideas Extracted from `MiniCvmNewFeaturesTest.copy.kt.bak`

Source analyzed: `vm/src/commonTest/kotlin/com/hiperbou/vm/minicvm/MiniCvmNewFeaturesTest.copy.kt.bak`

## Feature ideas list (with difficulty)

1. `do { ... } while (cond);`
- What tests suggest:
  - Body executes at least once.
  - Condition checked after body.
- Difficulty: Medium
- Reason: parser + codegen control-flow labels.

2. `for` loop flexible forms
- What tests suggest:
  - Support empty initializer/condition/update.
  - `for (;;)` infinite loop patterns with `break`.
- Difficulty: Low/Medium
- Reason: parser mostly supports this already; depends on `break`/`continue` support.

3. `break` statement
- What tests suggest:
  - Works in `while`, `do-while`, and `for`.
  - In nested loops, breaks only the innermost loop.
- Difficulty: Medium
- Reason: loop context stack + end labels in codegen.

4. `continue` statement
- What tests suggest:
  - Works in `while`, `do-while`, and `for`.
  - In nested loops, continues only innermost loop.
- Difficulty: Medium
- Reason: loop continue-target labels differ by loop kind.

5. Bitwise operators: `&`, `|`, `^`, `~`
- What tests suggest:
  - Binary and unary bitwise semantics on ints.
  - Combined expressions with precedence.
- Difficulty: Medium
- Reason: lexer tokens + expression parser precedence + opcode mapping (`B_AND`, `B_OR`, `B_XOR`, `B_NOT`).

6. Increment/decrement robustness
- What tests suggest:
  - Prefix/postfix on variables in expressions.
  - Complex expression interactions.
- Difficulty: Low
- Reason: mostly implemented; needs additional validation tests.

7. Arrays (`int arr[n]`, indexing, read/write, inc/dec on elements)
- What tests suggest:
  - Local array declaration and indexed access.
  - Prefix/postfix inc/dec on `arr[idx]`.
- Difficulty: High
- Reason: new type of declaration + address model + indexed load/store codegen.

8. Ternary operator `cond ? a : b`
- What tests suggest:
  - Expression form with nested ternaries.
  - Only chosen branch should execute (side effects).
- Difficulty: Medium/High
- Reason: Pratt parselet + branching codegen that preserves expression value.

9. `debugPrint(expr);`
- What tests suggest:
  - Builtin analogous to `print` with different opcode.
- Difficulty: Low
- Reason: lexer/parser builtin + codegen to `DEBUG_PRINT`.

10. Multi-variable declaration syntax in one statement (`int a = 1, b = 2;`)
- What tests suggest:
  - Used in nested ternary tests.
- Difficulty: Medium
- Reason: parser extension + statement lowering into multiple decls.

## Proposed implementation order

1. `do-while`
2. `break`
3. `continue`
4. bitwise ops
5. `debugPrint`
6. ternary operator
7. array support (base indexing)
8. array inc/dec
9. multi-declaration `int a=..., b=...`

Each feature should be delivered with:
- parser/AST/codegen implementation
- focused tests in `ccompiler` test suite
- full `:ccompiler:jvmTest` green before commit
