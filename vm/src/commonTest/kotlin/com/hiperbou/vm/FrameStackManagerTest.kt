@file:Suppress("DEPRECATION")

package com.hiperbou.vm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrameStackManagerTest {

    @Test
    fun poppedFrameLocalsDoNotLeakIntoReusedFrameBaseTest() {
        val manager = FrameStackManager()

        val frame1 = manager.pushFrame(returnAddress = 10)
        frame1.setVariable(2, 22)

        val frame2 = manager.pushFrame(returnAddress = 20)
        frame2.setVariable(0, 99)

        manager.popFrame(instructionAddress = 0)

        val frame3 = manager.pushFrame(returnAddress = 30)
        assertEquals(0, frame3.getVariable(0))

        manager.popFrame(instructionAddress = 0)
        assertEquals(22, manager.currentFrame().getVariable(2))
    }

    @Test
    fun cannotPopSentinelFrameTest() {
        val manager = FrameStackManager()
        assertFailsWith<InvalidProgramException> {
            manager.popFrame(instructionAddress = 123)
        }
    }

    @Test
    fun managerNormalizesPreexistingMapFramesIntoSliceBackedStorageTest() {
        val frames = CPUFrames<Frame>()
        val root = Frame.mapBacked(0)
        root.setVariable(0, 10)
        frames.push(root)

        val callee = Frame.mapBacked(42)
        callee.setVariable(0, 20)
        frames.push(callee)

        val manager = FrameStackManager(frames)
        assertEquals(20, manager.currentFrame().getVariable(0))

        val nested = manager.pushFrame(returnAddress = 77)
        nested.setVariable(0, 30)
        manager.popFrame(instructionAddress = 0)

        assertEquals(20, manager.currentFrame().getVariable(0))
        manager.popFrame(instructionAddress = 0)
        assertEquals(10, manager.currentFrame().getVariable(0))
    }
}




