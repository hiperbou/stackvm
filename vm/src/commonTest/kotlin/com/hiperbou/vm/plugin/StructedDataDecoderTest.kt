package com.hiperbou.vm.plugin.struct

import com.hiperbou.vm.CPU
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import kotlin.test.Test
import kotlin.test.assertEquals

class StructuredDataDecoderTest {

    private val opcodeInformation = OpcodeInformationChain(CoreOpcodeInformation(), StructuredDataOpcodeInformation())
    private val compiler = Compiler(opcodeInformation)

    private fun parseProgram(source: String): IntArray {
        return compiler.generateProgram(source)
    }

    private fun runCpu(program: IntArray): CPU {
        val cpu = CPU(program)
        cpu.appendDecoder(StructuredDataDecoder(cpu, cpu.getStack()))
        cpu.run()
        return cpu
    }

    @Test
    fun testArrayAddr() {
        val program = parseProgram("""
            PUSH 100 // base address
            PUSH 2   // index
            PUSH 4   // element size
            ARRAY_ADDR
            HALT
        """.trimIndent())
        val cpu = runCpu(program)
        assertEquals(108, cpu.getStack().peek())
    }

    @Test
    fun testFieldAddr() {
        val program = parseProgram("""
            PUSH 100 // base address
            PUSH 8   // field offset
            FIELD_ADDR
            HALT
        """.trimIndent())
        val cpu = runCpu(program)
        assertEquals(108, cpu.getStack().peek())
    }

    @Test
    fun testMemcpy() {
        val program = parseProgram("""
            PUSH 10 // dest
            PUSH 20 // src
            PUSH 5  // size
            MEMCPY
            HALT
        """.trimIndent())

        val cpu = CPU(program)
        cpu.appendDecoder(StructuredDataDecoder(cpu, cpu.getStack()))

        for (i in 0 until 5) {
            cpu.getMemory().set(20 + i, 100 + i)
        }

        cpu.run()

        for (i in 0 until 5) {
            assertEquals(100 + i, cpu.getMemory().get(10 + i))
        }
    }

    @Test
    fun testMemset() {
        val program = parseProgram("""
            PUSH 10  // dest
            PUSH 99  // value
            PUSH 5   // size
            MEMSET
            HALT
        """.trimIndent())

        val cpu = CPU(program)
        cpu.appendDecoder(StructuredDataDecoder(cpu, cpu.getStack()))

        cpu.run()

        for (i in 0 until 5) {
            assertEquals(99, cpu.getMemory().get(10 + i))
        }
    }
}
