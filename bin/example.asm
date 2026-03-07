// ============================================================
// example.asm — asmcompiler demo program
//
// Computes the sum 1+2+3+4+5 using a counted loop,
// prints each running total, then prints the final result.
//
// Run:         asmcompiler.bat example.asm
// Disassemble: asmcompiler.bat example.asm -d
// Save binary: asmcompiler.bat example.asm -o example.vmb
// ============================================================

// Constants
n:      5   // how many numbers to sum
i_slot: 0   // local variable index: loop counter  (i)
s_slot: 1   // local variable index: running sum   (sum)

// ----- initialise i = 1, sum = 0 -----
PUSH 1
STORE i_slot

PUSH 0
STORE s_slot

// ----- loop: while i <= n -----
loop_start:
    // if i > n → jump to done
    LOAD  i_slot
    PUSH  n
    GT              // (i > n) → 1 or 0
    JIF   loop_done

    // sum = sum + i
    LOAD  s_slot
    LOAD  i_slot
    ADD
    STORE s_slot

    // print running sum
    LOAD  s_slot
    PRINT

    // i++
    LOAD  i_slot
    PUSH  1
    ADD
    STORE i_slot

    JMP loop_start

// ----- done -----
loop_done:
HALT
