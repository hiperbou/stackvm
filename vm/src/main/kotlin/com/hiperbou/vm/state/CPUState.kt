package com.hiperbou.vm.state

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUFrames
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Frame
import com.hiperbou.vm.collection.StackImpl

data class CPUState<T,R>(
    val stack:CPUStack<T>,
    val frames:CPUFrames<R>,
    val instructionAddress:Int,
    val halted:Boolean
)

fun CPU.saveState():CPUState<Int,Frame> {
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

    return CPUState(
            getStack().clone(),
            getFrames().clone(),
            instructionAddress,
            isHalted()
    )
}

fun CPUState<Int, Frame>.restoreCPU(program:IntArray):CPU {
    return CPU(program, stack, frames, instructionAddress, halted)
}