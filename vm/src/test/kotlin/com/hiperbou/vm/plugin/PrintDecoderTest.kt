package com.hiperbou.vm.plugin

import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.plugin.print.PrintInstructions.PRINT
import com.hiperbou.vm.assertStackContains
import com.hiperbou.vm.plugin.print.PrintDecoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrintDecoderTest {

    @Test
    fun printDecoderTest() {
        val cpu = CPU(PUSH, 2,
            PRINT,
            HALT
        )
        cpu.appendDecoder(PrintDecoder(cpu.getStack()))
        cpu.run()
        assertEquals(4, cpu.instructionAddress)
        assertTrue(cpu.isHalted())
        assertStackContains(cpu, 2)
    }
}