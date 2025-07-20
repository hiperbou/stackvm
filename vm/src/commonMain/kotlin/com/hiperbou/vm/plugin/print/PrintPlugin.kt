package com.hiperbou.vm.plugin.print

import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.*
import com.hiperbou.vm.decompiler.OpcodeInformation

object PrintInstructions {
    const val PRINT = 0xF0
    const val DEBUG_PRINT = 0xF1
}

enum class PrintInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    PRINT(PrintInstructions.PRINT),
    DEBUG_PRINT(PrintInstructions.DEBUG_PRINT)
}

class PrintOpcodeInformation: OpcodeInformation {
    private val values = PrintInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = PrintInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

class PrintDecoder(private val stack: CPUStack<Int>, private var nextDecoder: Decoder = ExceptionDecoder.instance):Decoder {
    override fun decodeInstruction(instruction: Int) = with(stack) {
        when (instruction) {
            PrintInstructions.PRINT -> {
                checkIsNotEmpty("PRINT" )
                val n = peek()
                println(n)
            }
            PrintInstructions.DEBUG_PRINT -> {
                checkIsNotEmpty("DEBUG_PRINT" )
                val n = peek()
                println("<$n>")
            }
            else -> nextDecoder.decodeInstruction(instruction)
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }
}