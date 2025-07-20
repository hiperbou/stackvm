package com.hiperbou.vm.plugin.cfunctioncall

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUFrames
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Frame
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.OpcodeInformation
import com.hiperbou.vm.plugin.cfunctioncall.FunctionCallInstructions.ALLOCA
import com.hiperbou.vm.plugin.cfunctioncall.FunctionCallInstructions.CALL_C
import com.hiperbou.vm.plugin.cfunctioncall.FunctionCallInstructions.FRAME_PTR

object FunctionCallInstructions {
    const val CALL_C = 0x98     // C calling convention call
    const val ALLOCA = 0x99     // Stack allocation for local arrays
    const val FRAME_PTR = 0x9A  // Get current frame pointer
}

enum class FunctionCallInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    CALL_C(FunctionCallInstructions.CALL_C),
    ALLOCA(FunctionCallInstructions.ALLOCA),
    FRAME_PTR(FunctionCallInstructions.FRAME_PTR),
}

class FunctionOpcodeInformation: OpcodeInformation {
    private val values = FunctionCallInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = FunctionCallInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

//TODO: WIP
class FunctionCallDecoder(
    private val cpu: CPU,
    private val stack: CPUStack<Int>,
    private val frames: CPUFrames<Frame>,
    private var nextDecoder: Decoder = ExceptionDecoder.instance
): Decoder {

    override fun decodeInstruction(instruction: Int) {
        with(stack) {
            with(cpu) {
                when (instruction) {
                    CALL_C -> {
                        val address = getNextWordFromProgram("Should have address after CALL_C")
                        val paramCount = getNextWordFromProgram("Should have parameter count after CALL_C")
                        checkJumpAddress(address)

                        // Create new frame with parameter space
                        val newFrame = Frame(instructionAddress)

                        // Copy parameters from stack to frame variables in reverse order
                        // (C calling convention: rightmost parameter pushed first)
                        for (i in paramCount - 1 downTo 0) {
                            checkIsNotEmpty("CALL_C parameter")
                            newFrame.setVariable(i, pop())
                        }

                        frames.push(newFrame)
                        instructionAddress = address
                    }
                    //TODO:
                    ALLOCA -> {
                        println("Warning: This plugin is not fully implemented yet. ALLOCA instruction will be failing.")
                        checkIsNotEmpty("ALLOCA")
                        val size = pop()
                        val currentFrame = getCurrentFrame() as EnhancedFrame //TODO: This cast is not valid

                        // Allocate space in current frame for local array
                        // Return the starting variable index for the allocated space
                        val startIndex = currentFrame.allocateSpace(size)
                        push(startIndex)
                    }
                    //TODO:
                    FRAME_PTR -> {
                        println("Warning: This plugin is not fully implemented yet. ALLOCA instruction will be ignored.")
                        // Push a reference to the current frame base
                        // This would need frame-relative addressing support
                        val frameBase = frames.size - 1 // Simplified frame pointer
                        push(frameBase)
                    }
                    else -> nextDecoder.decodeInstruction(instruction)
                }
            }
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }
}

class EnhancedFrame(returnAddress: Int) : Frame(returnAddress) {
    private var nextVariableIndex = 0

    fun allocateSpace(size: Int): Int {
        val startIndex = nextVariableIndex
        nextVariableIndex += size

        // Initialize allocated space to zero
        for (i in 0 until size) {
            setVariable(startIndex + i, 0)
        }

        return startIndex
    }
}