package com.hiperbou.vm.state

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
import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.assertProgramRunsToHaltAndInstructionAddressIs
import com.hiperbou.vm.assertStackContains
import kotlin.test.*

class CPUStateTest {
    private fun instructions(vararg instructions:Int) = instructions

    @Test
    fun restoreCPUStateTest() {
        val program = instructions(
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

        val cpu = CPU(program)
        cpu.step()
        cpu.step()
        assertEquals(4, cpu.instructionAddress)

        val state = cpu.saveState()
        val newCPU = state.restoreCPU(program)

        assertEquals(4, newCPU.instructionAddress)
        cpu.step()
        cpu.step()
        assertEquals(2, cpu.getFrames().size)
        assertEquals(1, newCPU.getFrames().size)
        assertEquals(9, cpu.instructionAddress)
        assertEquals(4, newCPU.instructionAddress)
        newCPU.step()
        assertEquals(7, newCPU.instructionAddress)
        assertEquals(2, newCPU.getFrames().size)

        assertProgramRunsToHaltAndInstructionAddressIs(newCPU, 7)
        assertStackContains(newCPU, 6)

        assertEquals(2, cpu.getFrames().size)
        assertEquals(1, newCPU.getFrames().size)
    }
}