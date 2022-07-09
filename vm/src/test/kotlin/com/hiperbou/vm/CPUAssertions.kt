package com.hiperbou.vm

import org.junit.jupiter.api.Assertions.*

fun assertProgramRunsToHaltAndInstructionAddressIs(cpu: CPU, expectedAddress: Int) {
    cpu.run()
    assertEquals(
        expectedAddress, cpu.instructionAddress,
        "The CPU should have finished at the expected address",
    )
    assertTrue(cpu.isHalted(), "The CPU should be halted")
}

fun assertStackIsEmpty(cpu: CPU) {
    assertTrue(cpu.getStack().isEmpty(), "The stack should be empty")
}

fun assertStackContains(cpu: CPU, vararg expectedContent: Int) {
    assertEquals(
        expectedContent.size, cpu.getStack().size,
        "The stack should have the expected length",
    )
    assertArrayEquals(
        expectedContent.toTypedArray(), cpu.getStack().toArray(),
        "The stack content should be as expected",
    )
}

fun assertVariableValues(cpu: CPU, vararg expectedVariableValues: Int) {
    val frame: Frame = cpu.getCurrentFrame()
    for (varNumber in expectedVariableValues.indices) {
        val expectedVariableValue = expectedVariableValues[varNumber]
        assertEquals(expectedVariableValue, frame.getVariable(varNumber), "Checking variable #$varNumber")
    }
}

fun assertGlobalVariableValues(cpu: CPU, vararg expectedVariableValues: Int) {
    val frame: Frame = cpu.getGlobals()
    for (varNumber in expectedVariableValues.indices) {
        val expectedVariableValue = expectedVariableValues[varNumber]
        assertEquals(expectedVariableValue, frame.getVariable(varNumber), "Checking variable #$varNumber")
    }
}

fun assertMemoryValues(cpu: CPU, vararg expectedMemoryValues: Int) {
    val memory = cpu.getMemory().getBackingArray()
    for (index in expectedMemoryValues.indices) {
        val expectedVariableValue = expectedMemoryValues[index]
        assertEquals(expectedVariableValue, memory[index], "Checking memory at #$index")
    }
}