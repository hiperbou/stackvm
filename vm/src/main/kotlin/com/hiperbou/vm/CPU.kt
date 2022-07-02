package com.hiperbou.vm

import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.BasicDecoder

class CPU(vararg instructions:Int) {

    private val program: IntArray = instructions

    var instructionAddress = 0
    private var halted = false

    private val stack = CPUStack<Int>()
    private val frames = CPUFrames<Frame>()

    private val decoder = BasicDecoder(this, stack, frames)
    
    init {
        assert(program.isNotEmpty()) { "A program should have at least an instruction" }
        frames.push(Frame(0))
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

    fun getCurrentFrame(): Frame {
        return frames.peek()
    }

    fun appendDecoder(decoder: Decoder) {
        this.decoder.setNextDecoder(decoder)
    }
}

