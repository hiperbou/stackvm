package com.hiperbou.vm.plugin.bitwise

import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.USHR
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.ROL
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.ROR
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.SHL
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.SHR
import kotlin.test.Test
import kotlin.test.assertEquals

class BitwiseDecoderTest {

    private val opcodeInformation = OpcodeInformationChain(CoreOpcodeInformation(), BitwiseOpcodeInformation())
    private val compiler = Compiler(opcodeInformation)

    private fun parseProgram(source: String): IntArray {
        return compiler.generateProgram(source)
    }

    @Test
    fun testBitwiseShiftLeft() {
        val program = parseProgram("""
            PUSH 8
            PUSH 2
            SHL
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(32, cpu.getStack().peek())
    }

    @Test
    fun testBitwiseShiftRight() {
        val program = parseProgram("""
            PUSH 8
            PUSH 2
            SHR
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(2, cpu.getStack().peek())
    }

    @Test
    fun testBitwiseShiftRight2() {
        val program = parseProgram("""
            PUSH -8
            PUSH 2
            SHR
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(-2, cpu.getStack().peek())
    }

    @Test
    fun testBitwiseUnsignedShiftRight() {
        val program = parseProgram("""
            PUSH -8
            PUSH 2
            USHR
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(1073741822, cpu.getStack().peek())
    }

    @Test
    fun testBitwiseRotateLeft() {
        val program = parseProgram("""
            PUSH 8
            PUSH 2
            ROL
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(32, cpu.getStack().peek())
    }

    @Test
    fun testBitwiseRotateRight() {
        val program = parseProgram("""
            PUSH 8
            PUSH 2
            ROR
            HALT
        """.trimIndent())
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(2, cpu.getStack().peek())
    }

    private fun assertStackTop(program: IntArray, expected: Int, message: String) {
        val cpu = CPU(program)
        cpu.appendDecoder(BitwiseDecoder(cpu, cpu.getStack()))
        cpu.run()
        assertEquals(expected, cpu.getStack().peek(), message)
    }

    @Test
    fun testSHL() {
        assertStackTop(
            intArrayOf(
                Instructions.PUSH, 5,
                Instructions.PUSH, 2,
                SHL,
                Instructions.HALT
            ),
            20,
            "5 shl 2 should be 20"
        )
    }

    @Test
    fun testSHR() {
        assertStackTop(
            intArrayOf(
                Instructions.PUSH, 5,
                Instructions.PUSH, 1,
                SHR,
                Instructions.HALT
            ),
            2,
            "5 shr 1 should be 2"
        )
    }

    @Test
    fun testASHR() {
        assertStackTop(
            intArrayOf(
                Instructions.PUSH, -5,
                Instructions.PUSH, 1,
                USHR,
                Instructions.HALT
            ),
            2147483645,
            "-5 ashr 1 should be 2147483645"
        )
    }

    @Test
    fun testROL() {
        assertStackTop(
            intArrayOf(
                Instructions.PUSH, 5,
                Instructions.PUSH, 2,
                ROL,
                Instructions.HALT
            ),
            20,
            "5 rol 2 should be 20"
        )
    }

    @Test
    fun testROR() {
        assertStackTop(
            intArrayOf(
                Instructions.PUSH, 5,
                Instructions.PUSH, 1,
                ROR,
                Instructions.HALT
            ),
            -2147483646,
            "5 ror 1 should be -2147483646"
        )
    }
}
