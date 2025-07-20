package com.hiperbou.vm.plugin.conditional

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.OpcodeInformation
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions
import com.hiperbou.vm.plugin.conditional.ConditionalInstructions.BRANCH_TABLE
import com.hiperbou.vm.plugin.conditional.ConditionalInstructions.SELECT

object ConditionalInstructions {
    const val SELECT = 0x89      // Conditional select (condition ? a : b)
    const val BRANCH_TABLE = 0x8A // Switch statement optimization
}

enum class ConditionaInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    SELECT(ConditionalInstructions.SELECT, 3),
    BRANCH_TABLE(ConditionalInstructions.BRANCH_TABLE, 1, true),
}

class ConditionaOpcodeInformation: OpcodeInformation {
    private val values = ConditionaInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = ConditionaInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

class ConditionalDecoder(
    private val cpu: CPU,
    private val stack: CPUStack<Int>,
    private var nextDecoder: Decoder = ExceptionDecoder.instance
): Decoder {

    override fun decodeInstruction(instruction: Int) {
        with(stack) {
            with(cpu) {
                when (instruction) {
                    SELECT -> {
                        checkAtLeast3Items() // Need condition, value_if_true, value_if_false
                        val valueIfFalse = pop()
                        val valueIfTrue = pop()
                        val condition = pop()
                        push(if (condition.toBool()) valueIfTrue else valueIfFalse)
                    }
                    BRANCH_TABLE -> {
                        checkIsNotEmpty("BRANCH_TABLE") // Need index
                        val tableSize = getNextWordFromProgram("Should have table size after BRANCH_TABLE")
                        val index = pop()

                        // Bounds check
                        if (index < 0 || index >= tableSize) {
                            // Skip the jump table
                            for (i in 0 until tableSize) {
                                getNextWordFromProgram("Jump table entry")
                            }
                        } else {
                            // Skip to the correct entry in the jump table
                            for (i in 0 until index) {
                                getNextWordFromProgram("Jump table entry")
                            }
                            val jumpAddress = getNextWordFromProgram("Jump table target")
                            checkJumpAddress(jumpAddress)

                            // Skip remaining entries
                            for (i in index + 1 until tableSize) {
                                getNextWordFromProgram("Jump table entry")
                            }
                            instructionAddress = jumpAddress
                        }
                    }
                    else -> nextDecoder.decodeInstruction(instruction)
                }
            }
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }

    private fun Int.toBool() = this != 0
}