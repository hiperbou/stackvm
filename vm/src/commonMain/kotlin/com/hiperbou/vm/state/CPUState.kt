package com.hiperbou.vm.state

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUFrames
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Frame
import com.hiperbou.vm.FrameSlice
import com.hiperbou.vm.collection.StackImpl
import com.hiperbou.vm.memory.DefaultMemory
import com.hiperbou.vm.memory.Memory

data class FrameDebugInfo(
    val stackIndexFromTop: Int,
    val returnAddress: Int,
    val baseOffset: Int?,
    val backend: String
)

data class CPUState<T, R>(
    val stack: CPUStack<T>,
    val frames: CPUFrames<R>,
    val globals: R,
    val memory: IntArray,
    val instructionAddress: Int,
    val halted: Boolean,
    val frameDebugInfo: List<FrameDebugInfo> = emptyList()
)

fun CPU.saveState(): CPUState<Int, Frame> {
    fun CPUStack<Int>.clone(): CPUStack<Int> {
        return CPUStack(StackImpl(ArrayDeque(map { it })))
    }

    fun Frame.clone(): Frame {
        val cloned = Frame(returnAddress)
        getVariables().forEach { cloned.setVariable(it.key, it.value) }
        return cloned
    }

    fun CPUFrames<Frame>.clone(): CPUFrames<Frame> {
        return CPUFrames(StackImpl(ArrayDeque(map { it.clone() })))
    }

    fun Memory.clone(): IntArray {
        return getBackingArray().copyOf()
    }

    val frameDebugInfo = getFrames().mapIndexed { index, frame ->
        FrameDebugInfo(
            stackIndexFromTop = index,
            returnAddress = frame.returnAddress,
            baseOffset = (frame as? FrameSlice)?.baseOffset,
            backend = if (frame is FrameSlice) "slice" else "map"
        )
    }

    return CPUState(
        getStack().clone(),
        getFrames().clone(),
        getGlobals().clone(),
        getMemory().clone(),
        instructionAddress,
        isHalted(),
        frameDebugInfo
    )
}

fun CPUState<Int, Frame>.restoreCPU(program: IntArray, memory: Memory = DefaultMemory(this.memory)): CPU {
    return CPU(program, stack, frames, globals, memory, instructionAddress, halted)
}
