package com.hiperbou.vm.decoder

import com.hiperbou.vm.InvalidProgramException

class ExceptionDecoder:Decoder {
    override fun decodeInstruction(instruction: Int) {
        throw InvalidProgramException("Unknown instruction: $instruction")
    }

    override fun setNextDecoder(decoder: Decoder) {
        throw Exception("ExceptionDecoder doesn't accept next decoder")
    }

    companion object {
        val instance = ExceptionDecoder()
    }
}