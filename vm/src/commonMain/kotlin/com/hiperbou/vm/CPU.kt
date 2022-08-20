package com.hiperbou.vm

import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.CoreDecoder
import com.hiperbou.vm.memory.DefaultMemory
import com.hiperbou.vm.memory.Memory

fun CPU(vararg instructions:Int) = CPU(instructions)
class CPU(instructions:IntArray,
          private val stack:CPUStack<Int> = CPUStack(),
          private val frames:CPUFrames<Frame> = CPUFrames<Frame>().also { it.push(Frame(0)) },
          private val globals:Frame = Frame(),
          private val memory: Memory = DefaultMemory(),
          var instructionAddress:Int = 0,
          private var halted:Boolean = false
) {
    private var program: IntArray = instructions

    private val decoder = CoreDecoder(this, stack, frames)
    
    init {
        assert(program.isNotEmpty()) { "A program should have at least an instruction" }
    }
    fun isHalted() = halted
    fun haltCPU() { halted = true }
    fun getStack() = stack

    fun run() {
        while (!halted) {
            step()
        }
    }

    fun step() {
        assert(!halted) { "A halted CPU cannot execute the program" }
        val nextInstruction = getNextWordFromProgram("Should have a next instruction")
        decoder.decodeInstruction(nextInstruction)
    }

    fun reset(instructions:IntArray?) {
        program = instructions ?: program
        instructionAddress = 0
        halted = false
    }

    fun checkJumpAddress(address: Int) {
        if (address < 0 || address >= program.size) {
            throw InvalidProgramException("Invalid jump address $address at $instructionAddress")
        }
    }

    fun getNextWordFromProgram(errorMessage: String): Int {
        if (instructionAddress >= program.size) {
            throw InvalidProgramException(errorMessage)
        }
        return program[instructionAddress++]
    }

    fun getCurrentFrame() = frames.peek()
    fun getFrames() = frames
    fun getGlobals() = globals
    fun getMemory() = memory

    fun appendDecoder(decoder: Decoder) {
        this.decoder.setNextDecoder(decoder)
    }
}

