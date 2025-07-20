package com.hiperbou.vm.plugin.bitwise

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.OpcodeInformation

object BitwiseInstructions {
    const val SHL = 0x80   // Shift left
    const val SHR = 0x81   // Logical shift right
    const val USHR = 0x82  // Arithmetic shift right
    const val ROL = 0x83   // Rotate left
    const val ROR = 0x84   // Rotate right
}

enum class BitwiseInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    SHL(BitwiseInstructions.SHL),
    SHR(BitwiseInstructions.SHR),
    USHR(BitwiseInstructions.USHR),
    ROL(BitwiseInstructions.ROL),
    ROR(BitwiseInstructions.ROR),
}

class BitwiseOpcodeInformation: OpcodeInformation {
    private val values = BitwiseInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = BitwiseInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

class BitwiseDecoder(
    private val cpu: CPU,
    private val stack: CPUStack<Int>,
    private var nextDecoder: Decoder = ExceptionDecoder.instance
): Decoder {

    override fun decodeInstruction(instruction: Int) {
        with(stack) {
            with(cpu) {
                when (instruction) {
                    BitwiseInstructions.SHL, BitwiseInstructions.SHR, BitwiseInstructions.USHR, BitwiseInstructions.ROL, BitwiseInstructions.ROR -> {
                        checkAtLeast2Items()
                        val n2 = pop() // shift amount
                        val n1 = pop() // value to shift
                        push(evaluateBitwiseOperation(instruction, n1, n2))
                    }
                    else -> nextDecoder.decodeInstruction(instruction)
                }
            }
        }
    }

    private fun evaluateBitwiseOperation(instruction: Int, value: Int, amount: Int): Int {
        return when (instruction) {
            BitwiseInstructions.SHL -> value shl amount
            BitwiseInstructions.SHR -> value shr amount
            BitwiseInstructions.USHR -> value ushr amount
            BitwiseInstructions.ROL -> value.rotateLeft(amount)
            BitwiseInstructions.ROR -> value.rotateRight(amount)
            else -> throw AssertionError()
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }
}
