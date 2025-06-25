# Assembly Language Documentation

This document describes the assembly language used by the virtual machine.

## Program Structure

An assembly program consists of a sequence of lines. Each line can contain one of the following:

*   **Label:** A label assigns a name to a specific point in the code. Labels are defined by an identifier followed by a colon (e.g., `my_label:`). They can be used as targets for jump and call instructions.
*   **Instruction:** An instruction performs a specific operation. Instructions may have operands, which can be numbers or labels.
*   **Comment:** Comments are ignored by the assembler. They start with `//` and continue to the end of the line.
*   **Empty Line:** Empty lines are ignored.

Identifiers (for labels and instruction parameters) must start with a letter and can be followed by letters, numbers, or underscores (e.g., `loop_start`, `var_1`).
Numbers are integers.

## Core Instructions

The virtual machine supports the following core instructions.

### Control Flow

*   **NOP**
    *   Syntax: `NOP`
    *   Description: No operation. Does nothing.
*   **HALT**
    *   Syntax: `HALT`
    *   Description: Halts the execution of the program.
*   **JMP `address`**
    *   Syntax: `JMP label` or `JMP immediate_address`
    *   Description: Unconditionally jumps to the specified `address`. The address can be a label or an immediate integer value.
*   **JIF `address`**
    *   Syntax: `JIF label` or `JIF immediate_address`
    *   Description: Pops a value from the stack. If the value is non-zero (true), jumps to the specified `address`. Otherwise, continues to the next instruction. The address can be a label or an immediate integer value.
*   **CALL `address`**
    *   Syntax: `CALL label` or `CALL immediate_address`
    *   Description: Pushes the address of the next instruction (return address) onto the call frame stack and then jumps to the specified `address`. Used for function calls. The address can be a label or an immediate integer value.
*   **CALLI**
    *   Syntax: `CALLI`
    *   Description: Pops an `address` from the stack. Pushes the address of the next instruction (return address) onto the call frame stack and then jumps to the popped `address`. Indirect version of `CALL`.
*   **RET**
    *   Syntax: `RET`
    *   Description: Pops a return address from the call frame stack and jumps to that address. Used to return from a function call.

### Stack Manipulation

*   **PUSH `value`**
    *   Syntax: `PUSH immediate_value`
    *   Description: Pushes the `immediate_value` onto the stack.
*   **POP**
    *   Syntax: `POP`
    *   Description: Pops the top value from the stack and discards it.
*   **DUP**
    *   Syntax: `DUP`
    *   Description: Duplicates the top value on the stack. (Pops the top value, then pushes it twice).

### Arithmetic Operations

Binary arithmetic operations (ADD, SUB, MUL, DIV, MOD, MIN, MAX) pop two operands from the stack (n1, then n2), perform the operation, and push the result back onto the stack.
Unary arithmetic operations (ABS) pop one operand, perform the operation, and push the result.

*   **ADD**
    *   Syntax: `ADD`
    *   Operation: `n1 + n2`
*   **SUB**
    *   Syntax: `SUB`
    *   Operation: `n1 - n2`
*   **MUL**
    *   Syntax: `MUL`
    *   Operation: `n1 * n2`
*   **DIV**
    *   Syntax: `DIV`
    *   Operation: `n1 / n2` (integer division)
*   **MOD**
    *   Syntax: `MOD`
    *   Operation: `n1 % n2` (modulo operation, result has the same sign as `n2` or is zero)
*   **MIN**
    *   Syntax: `MIN`
    *   Operation: `min(n1, n2)`
*   **MAX**
    *   Syntax: `MAX`
    *   Operation: `max(n1, n2)`
*   **ABS**
    *   Syntax: `ABS`
    *   Description: Pops a value from the stack, computes its absolute value, and pushes the result back.

### Logical and Bitwise Operations

For logical operations (`NOT`, `AND`, `OR`), operands are treated as booleans (0 is false, non-zero is true). The result is pushed as an integer (0 for false, 1 for true).
Binary logical and bitwise operations pop two operands from the stack (n1, then n2), perform the operation, and push the result back.

*   **NOT**
    *   Syntax: `NOT`
    *   Description: Pops a value, performs a logical NOT, and pushes the result.
*   **AND**
    *   Syntax: `AND`
    *   Operation: `n1 AND n2` (logical AND)
*   **OR**
    *   Syntax: `OR`
    *   Operation: `n1 OR n2` (logical OR)
*   **B_NOT**
    *   Syntax: `B_NOT`
    *   Description: Pops a value, performs a bitwise NOT (inverts all bits), and pushes the result.
*   **B_AND**
    *   Syntax: `B_AND`
    *   Operation: `n1 & n2` (bitwise AND)
*   **B_OR**
    *   Syntax: `B_OR`
    *   Operation: `n1 | n2` (bitwise OR)
*   **B_XOR**
    *   Syntax: `B_XOR`
    *   Operation: `n1 ^ n2` (bitwise XOR)

### Comparison Operations

All comparison operations pop two operands from the stack (n1, then n2), perform the comparison, and push the result (0 for false, 1 for true) back onto the stack.

*   **EQ**
    *   Syntax: `EQ`
    *   Operation: `n1 == n2`
*   **NE**
    *   Syntax: `NE`
    *   Operation: `n1 != n2`
*   **GTE**
    *   Syntax: `GTE`
    *   Operation: `n1 >= n2`
*   **LTE**
    *   Syntax: `LTE`
    *   Operation: `n1 <= n2`
*   **GT**
    *   Syntax: `GT`
    *   Operation: `n1 > n2`
*   **LT**
    *   Syntax: `LT`
    *   Operation: `n1 < n2`

### Memory Access

These instructions interact with different memory regions:
*   **Local Memory:** Variables local to the current function call frame. Accessed by index.
*   **Global Memory:** Variables accessible from anywhere in the program. Accessed by index.
*   **Main Memory:** A general-purpose memory region. Accessed by address.

*   **LOAD `index`**
    *   Syntax: `LOAD immediate_index`
    *   Description: Pushes the value of the local variable at `immediate_index` onto the stack.
*   **STORE `index`**
    *   Syntax: `STORE immediate_index`
    *   Description: Pops a value from the stack and stores it in the local variable at `immediate_index`.
*   **GLOAD `index`**
    *   Syntax: `GLOAD immediate_index`
    *   Description: Pushes the value of the global variable at `immediate_index` onto the stack.
*   **GSTORE `index`**
    *   Syntax: `GSTORE immediate_index`
    *   Description: Pops a value from the stack and stores it in the global variable at `immediate_index`.
*   **READ `address`**
    *   Syntax: `READ immediate_address`
    *   Description: Pushes the value from main memory at `immediate_address` onto the stack.
*   **WRITE `address`**
    *   Syntax: `WRITE immediate_address`
    *   Description: Pops a value from the stack and writes it to main memory at `immediate_address`.
*   **LOADI**
    *   Syntax: `LOADI`
    *   Description: Pops an `index` from the stack. Pushes the value of the local variable at that `index` onto the stack. (Indirect LOAD)
*   **STOREI**
    *   Syntax: `STOREI`
    *   Description: Pops an `index` then a `value` from the stack. Stores the `value` in the local variable at `index`. (Indirect STORE)
*   **GLOADI**
    *   Syntax: `GLOADI`
    *   Description: Pops an `index` from the stack. Pushes the value of the global variable at that `index` onto the stack. (Indirect GLOAD)
*   **GSTOREI**
    *   Syntax: `GSTOREI`
    *   Description: Pops an `index` then a `value` from the stack. Stores the `value` in the global variable at `index`. (Indirect GSTORE)
*   **READI**
    *   Syntax: `READI`
    *   Description: Pops an `address` from the stack. Pushes the value from main memory at that `address` onto the stack. (Indirect READ)
*   **WRITEI**
    *   Syntax: `WRITEI`
    *   Description: Pops an `address` then a `value` from the stack. Writes the `value` to main memory at `address`. (Indirect WRITE)

## Plugin Instructions

The VM supports plugins that can add new instructions. Plugins are enabled by chaining their decoders using `cpu.appendDecoder()`.

### Print Plugin (`com.hiperbou.vm.plugin.print.PrintPlugin`)

This plugin provides instructions for printing values from the stack.

*   **PRINT**
    *   Opcode: `0xF0`
    *   Syntax: `PRINT`
    *   Description: Pops the top value from the stack and prints it to standard output, followed by a newline.
*   **DEBUG_PRINT**
    *   Opcode: `0xF1`
    *   Syntax: `DEBUG_PRINT`
    *   Description: Pops the top value from the stack and prints it to standard output, enclosed in angle brackets (e.g., `<value>`), followed by a newline.
