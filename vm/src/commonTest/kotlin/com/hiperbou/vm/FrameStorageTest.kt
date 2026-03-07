@file:Suppress("DEPRECATION")

package com.hiperbou.vm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrameStorageTest {

    @Test
    fun defaultFrameStorageReturnsZeroForMissingSlotsTest() {
        val frame = Frame(returnAddress = 10)
        assertEquals(0, frame.getVariable(0))
        frame.setVariable(3, 99)
        assertEquals(99, frame.getVariable(3))
    }

    @Test
    fun frameSlicesMapToIndependentWindowsOnSharedArrayStoreTest() {
        val store = ArrayFrameStore()
        val frameA = FrameSlice(returnAddress = 1, store = store, baseOffset = 0)
        val frameB = FrameSlice(returnAddress = 2, store = store, baseOffset = 100)

        frameA.setVariable(5, 42)
        frameB.setVariable(5, 7)

        assertEquals(42, frameA.getVariable(5))
        assertEquals(7, frameB.getVariable(5))
        assertEquals(0, frameA.getVariable(6))
        assertEquals(0, frameB.getVariable(6))
    }

    @Test
    fun arrayBackedFrameSupportsSparseSlotsTest() {
        val frame = Frame(0, ArrayFrameVariableStorage(ArrayFrameStore()))
        frame.setVariable(1024, 55)
        assertEquals(55, frame.getVariable(1024))
        assertEquals(0, frame.getVariable(7))
    }

    @Test
    fun mapBackedFactoryRemainsAvailableForCompatibilityTest() {
        val frame = Frame.mapBacked(returnAddress = 5)
        frame.setVariable(2, 123)
        assertEquals(123, frame.getVariable(2))
        assertEquals(5, frame.returnAddress)
    }

    @Test
    fun arrayFrameStorageRejectsNegativeSlotIndexesTest() {
        val store = ArrayFrameStore()
        val frame = FrameSlice(returnAddress = 0, store = store, baseOffset = 0)

        assertFailsWith<InvalidProgramException> {
            frame.setVariable(-1, 1)
        }
        assertFailsWith<InvalidProgramException> {
            frame.getVariable(-3)
        }
    }
}



