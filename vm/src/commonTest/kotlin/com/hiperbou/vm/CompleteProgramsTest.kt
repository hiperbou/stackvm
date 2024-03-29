package com.hiperbou.vm

import com.hiperbou.vm.Instructions.ABS
import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.B_AND
import com.hiperbou.vm.Instructions.B_NOT
import com.hiperbou.vm.Instructions.B_OR
import com.hiperbou.vm.Instructions.B_XOR
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.DIV
import com.hiperbou.vm.Instructions.DUP
import com.hiperbou.vm.Instructions.EQ
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.LT
import com.hiperbou.vm.Instructions.LTE
import com.hiperbou.vm.Instructions.MAX
import com.hiperbou.vm.Instructions.MIN
import com.hiperbou.vm.Instructions.MOD
import com.hiperbou.vm.Instructions.MUL
import com.hiperbou.vm.Instructions.NE
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import kotlin.test.*


class CompleteProgramsTest {
    
    @Test
    fun testIfInstruction() {
        /**
         * The code is:
         * if (a > b) {
         * c = a;
         * } else {
         * c = b;
         * }
         *
         * We're going to use variable 0 as "a", variable 1 as "b", variable 2 as "c".
         */
        val cpu = CPU( // Init a with "6"
            PUSH, 6,
            STORE, 0,  // Init b with "4"
            PUSH, 4,
            STORE, 1,  // Load a and b into the stack
            LOAD, 0,  // Stack contains a
            LOAD, 1,  // Stack contains a, b
            GT,  // Stack contains a > b
            JIF, 21,  // This is the "else" path
            LOAD, 1,  // Stack contains b
            STORE, 2,  // Set c to the stack head, meaning c = b
            JMP, 25,  // This is the "if" path, and this is the address 21
            LOAD, 0,  // Stack contains a
            STORE, 2,  // Set c to the stack head, meaning c = a
            // Done; this is address 25
            HALT
        )
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 26)
        assertStackIsEmpty(cpu)
        assertVariableValues(cpu, 6, 4, 6)
    }

    @Test
    fun testMultiplication() {
        /**
         * We're going to multiply two numbers (a, b) without using the MUL instruction.
         *
         * The algorithm is:
         *
         * int total = 0;
         * while (b >= 1) {
         * total += a;
         * --b;
         * }
         *
         * We're going to use variable 0 as "a", variable 1 as "b", variable 2 as total.
         */
        val cpu = CPU( // Init a with "6"
            PUSH, 6,
            STORE, 0,  // Init b with "4"
            PUSH, 4,
            STORE, 1,  // Init total to 0
            PUSH, 0,
            STORE, 2,  // While part
            // Here is address 12
            LOAD, 1,  // Stack contains b
            PUSH, 1,  // Stack contains b, 1
            GTE,  // Stack contains b >= 1
            NOT,  // Stack contains b < 1
            JIF, 36,  // 36 is the address of the HALT label
            // Inner loop part
            LOAD, 0,  // Stack contains a
            LOAD, 2,  // Stack contains a, total
            ADD,  // Stack contains a + total
            STORE, 2,  // Save in total, meaning total = a + total
            LOAD, 1,  // Stack contains b
            PUSH, 1,  // Stack contains b, 1
            SUB,  // Stack contains b - 1
            STORE, 1,  // Save in b, meaning b = b - 1
            JMP, 12,  // Go back to the start of the loop
            HALT
        )
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 37)
        assertStackIsEmpty(cpu)
        assertVariableValues(cpu, 6, 0, 24)
    }

    @Test
    @Throws(Exception::class)
    fun testMaxAB() {
        /**
         * We're going to create a function that returns the maximum of its two arguments.
         *
         * The algorithm is obviously:
         *
         * int max(int a, int b) {
         * if (a > b) {
         * return a;
         * } else {
         * return b;
         * }
         * }
         *
         *
         */
        val cpu = CPU(
            PUSH, 6,  // Push the first argument
            PUSH, 4,  // Push the second argument
            CALL, 7,  // Call "max"
            HALT,  // Here is address 7, the start of "max" function
            STORE, 1,  // Store b in local variable 1; the stack now contains [a]
            STORE, 0,  // Store a in local variable 0; the stack is now empty
            LOAD, 0,  // The stack now contains [a]
            LOAD, 1,  // The stack now contains [a, b]
            GTE,  // The stack now contains [a > b]
            JIF, 21,  // If the top of the stack is true (a > b), jump to the "if" path
            LOAD, 1,  // "else" path: load b on the stack
            RET,  // Here is address 23
            LOAD, 0,  // "if" path: load a on the stack
            RET
        )
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 7)
        assertStackContains(cpu, 6)
    }

    @Test
    @Throws(Exception::class)
    fun testDivProgram() {
        /*
i:3
iterations:2
playerX:0
enemyX:1

MEM_num_processes:0
MEM_process:10

PUSH 30
STORE iterations

//process_main:
 PUSH process_player
 CALL new_process

 PUSH process_enemy
 CALL new_process

 // i = 0
 process_main_loop:
  LOAD i
  LOAD iterations
  LT
  JIF if
 //ELSE
  //PRINT MEMORY STATE
  READ MEM_num_processes
  READ MEM_process
  READ MEM_process + 1
  READ MEM_process + 2
  READ MEM_process + 3
  READ MEM_process + 4
  JMP halt
 //IF
  if:
  CALL run_processes
  LOAD i
  PUSH 1
  ADD
  STORE i
  JMP process_main_loop

  //LOAD iterations
  //PUSH 1
  //SUB
  //STORE iterations

  //LOAD iterations
  //PUSH 0
  //GT
  //JIF process_main_loop

 //PRINT MEMORY STATE
 //READ MEM_num_processes
 //READ MEM_process
 //READ MEM_process + 1
 //READ MEM_process + 2
 //READ MEM_process + 3
 //READ MEM_process + 4
halt:
HALT

new_process:
 READ MEM_num_processes
 PUSH MEM_process
 ADD
 WRITEI
 CALL incr_num_processes
RET

incr_num_processes:
 READ MEM_num_processes
 PUSH 1
 ADD
 WRITE MEM_num_processes
RET

run_processes:
 READ MEM_num_processes
 STORE 0

 //WHILE a>0
 p_loop:

 LOAD 0 //DECREMENT VARIABLE
 PUSH 1
 SUB
 STORE 0

 LOAD 0
 PUSH 0
 GTE
 NOT
 JIF p_break
 PUSH MEM_process
 LOAD 0
 ADD
 DUP
 READI
 CALLI

 JMP p_loop
 p_break:
RET


process_player:
STORE 0
 GLOAD playerX
 PUSH 1
 ADD
 GSTORE playerX
 PUSH frame
 WRITE MEM_process
 RET
 frame:
 STORE 0
 PUSH 666
 GSTORE 100
RET

process_enemy:

STORE 0
 GLOAD enemyX
 PUSH 2
 ADD
 GSTORE enemyX
RET


HALT

*/
    }

}