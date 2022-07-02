package com.hiperbou.vm.plugin.print

import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.decoder.*

object PrintInstructions {
    const val PRINT = 0xF0
}

class PrintDecoder(private val stack: CPUStack<Int>, private var nextDecoder: Decoder = ExceptionDecoder.instance):Decoder {
    override fun decodeInstruction(instruction: Int) = with(stack) {
        when (instruction) {
            PrintInstructions.PRINT -> {
                checkIsNotEmpty("PRINT" )
                val n = peek()
                println(n)
            }
            else -> nextDecoder.decodeInstruction(instruction)
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }
}