package com.hiperbou.vm

import com.hiperbou.vm.collection.*

class CPUStack<T>(private val impl:StackImpl<T> = StackImpl()): Stack<T> by impl  {
    fun checkIsNotEmpty(lazyInstructionName: String) {
        if (impl.isEmpty()) throw InvalidProgramException("There should be at least one item on the stack to execute an {$lazyInstructionName()} instruction")
    }

    fun checkAtLeast2Items() {
        if (impl.size < 2) {
            throw InvalidProgramException("There should be at least two items on the stack to execute a binary instruction")
        }
    }

    override fun toString(): String {
        return impl.toString()
    }
}

class CPUFrames<T>(private val impl:StackImpl<T> = StackImpl()): Stack<T> by impl  {
    fun checkThereIsAReturnAddress(instructionAddress:Int) {
        if (impl.size == 1) throw InvalidProgramException("Invalid RET instruction: no current function call $instructionAddress")
    }

    override fun toString(): String {
        return impl.toString()
    }
}
