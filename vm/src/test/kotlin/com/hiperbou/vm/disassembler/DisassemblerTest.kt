package com.hiperbou.vm.disassembler

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
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.decompiler.ProgramDecompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DisassemblerTest {
    private fun instructions(vararg instructions:Int) = instructions

    @Test
    fun disassembleLabelTest() {
        val program = instructions (
            CALL, 0,
            HALT
        )
        val decompiler = ProgramDecompiler()
        val decompilation = decompiler.decompile(program)

        val disassembler = Disassembler()
        val assembly = disassembler.disassemble(decompilation)
        assertEquals("""
            label_0:
            CALL label_0
            HALT
            
        """.trimIndent(), assembly)
    }

    @Test
    fun disassembleLabel2Test() {
        val program = instructions (
            PUSH, 1,
            PUSH, 3,
            CALL, 12,
            PUSH, 7,
            PUSH, 9,
            PUSH, 11,
            HALT
        )
        val decompiler = ProgramDecompiler()
        val decompilation = decompiler.decompile(program)

        val disassembler = Disassembler()
        val assembly = disassembler.disassemble(decompilation)
        assertEquals("""
            PUSH 1
            PUSH 3
            CALL label_12
            PUSH 7
            PUSH 9
            PUSH 11
            
            label_12:
            HALT
            
        """.trimIndent(), assembly)
    }

    @Test
    fun disassembleTest() {
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

        val disassembler = Disassembler()
        val assembly = disassembler.disassemble(decompilation)
        assertEquals("""
            PUSH 6
            PUSH 4
            CALL label_7
            HALT
            
            label_7:
            STORE 1
            STORE 0
            LOAD 0
            LOAD 1
            GTE
            JIF label_21
            LOAD 1
            RET
            
            label_21:
            LOAD 0
            RET
            
        """.trimIndent(), assembly)
    }
}