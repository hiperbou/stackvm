package com.hiperbou.vm.decompiler

import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.SWAP
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.plugin.print.PrintInstructions.PRINT
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import kotlin.test.*

class ProgramDecompilerTest {
    private fun instructions(vararg instructions:Int) = instructions

    @Test
    fun decompileSimple() {
        val program = instructions ( // Init a with "6"
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

        val decompiler = ProgramDecompiler()
        val decompilation = decompiler.decompile(program)
        assertEquals("[PUSH, 6, STORE, 0, PUSH, 4, STORE, 1, LOAD, 0, LOAD, 1, GT, JIF, 21, LOAD, 1, STORE, 2, JMP, 25, LOAD, 0, STORE, 2, HALT]",
            decompilation.toString())
    }

    @Test
    fun decompilerSimpleTest2() {
        val program = instructions ( // Init a with "6"
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
        val decompiler = ProgramDecompiler()
        val decompilation = decompiler.decompile(program)
        assertEquals("[PUSH, 6, STORE, 0, PUSH, 4, STORE, 1, PUSH, 0, STORE, 2, LOAD, 1, PUSH, 1, GTE, NOT, JIF, 36, LOAD, 0, LOAD, 2, ADD, STORE, 2, LOAD, 1, PUSH, 1, SUB, STORE, 1, JMP, 12, HALT]",
            decompilation.toString())

    }

    @Test
    fun decompilerSimpleTest3() {
        val program = instructions (
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
        val decompiler = ProgramDecompiler()
        val decompilation = decompiler.decompile(program)
        assertEquals("[PUSH, 6, PUSH, 4, CALL, 7, HALT, STORE, 1, STORE, 0, LOAD, 0, LOAD, 1, GTE, JIF, 21, LOAD, 1, RET, LOAD, 0, RET]",
            decompilation.toString())
    }

    @Test
    fun decompilePlugin() {
        val program = instructions (
            PUSH, 2,
            PRINT,
            HALT
        )

        val decompiler = ProgramDecompiler(OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation()))
        val decompilation = decompiler.decompile(program)
        assertEquals("[PUSH, 2, PRINT, HALT]",
            decompilation.toString())
    }

    @Test
    fun opcodeNotFoundOpcodeInformationChain() {
        assertFailsWith(InvalidProgramException::class) {
            val program = instructions(
                PUSH, 2,
                PRINT,
                0x29a,
                HALT
            )

            val decompiler =
                ProgramDecompiler(OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation()))
            decompiler.decompile(program)
        }
    }

    @Test
    fun negativeNumberTest() {
        val program = instructions(
            PUSH, -1,
            HALT
        )

        val decompiler =
            ProgramDecompiler(OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation()))
        decompiler.decompile(program)
    }

    @Test
    fun testDecompileSWAP() {
        val program = instructions(
            PUSH, 10,
            PUSH, 20,
            SWAP,
            HALT
        )
        val decompiler = ProgramDecompiler() // Uses CoreOpcodeInformation by default
        val decompilation = decompiler.decompile(program)
        assertEquals("[PUSH, 10, PUSH, 20, SWAP, HALT]", decompilation.toString())
    }
}