package com.hiperbou.vm.decoder

interface Decoder {
    fun decodeInstruction(instruction: Int)
    fun setNextDecoder(decoder: Decoder)
}

class PluggableDecoder(vararg decoders: Decoder):Decoder {
    private val last = decoders.last()
    private val first = decoders.first()
    init {
        for (index in decoders.indices) {
            decoders[index].setNextDecoder(decoders.getOrElse(index + 1) { ExceptionDecoder.instance })
        }
    }

    override fun decodeInstruction(instruction: Int) {
        first.decodeInstruction(instruction)
    }

    override fun setNextDecoder(decoder: Decoder) {
        last.setNextDecoder(decoder)
    }
}