package com.hiperbou.vm.plugin.struct

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.OpcodeInformation
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.ARRAY_ADDR
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.FIELD_ADDR
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.MEMCPY
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.MEMSET

object StructuredDataInstructions {
    const val ARRAY_ADDR = 0x85  // Calculate array element address (base + index * size)
    const val FIELD_ADDR = 0x86  // Calculate field address (base + offset)
    const val MEMCPY = 0x87  // Copy memory block (dest, src, size)
    const val MEMSET = 0x88  // Set memory block (dest, value, size)
}

enum class StructuredDataInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    ARRAY_ADDR(StructuredDataInstructions.ARRAY_ADDR),
    FIELD_ADDR(StructuredDataInstructions.FIELD_ADDR),
    MEMCPY(StructuredDataInstructions.MEMCPY),
    MEMSET(StructuredDataInstructions.MEMSET),
}

class StructuredDataOpcodeInformation: OpcodeInformation {
    private val values = StructuredDataInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = StructuredDataInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

class StructuredDataDecoder(
    private val cpu: CPU,
    private val stack: CPUStack<Int>,
    private var nextDecoder: Decoder = ExceptionDecoder.instance
): Decoder {

    override fun decodeInstruction(instruction: Int) {
        with(stack) {
            with(cpu) {
                when (instruction) {
                    ARRAY_ADDR -> {
                        checkAtLeast3Items() // Need base, index, element_size
                        val elementSize = pop()
                        val index = pop()
                        val baseAddr = pop()
                        push(baseAddr + (index * elementSize))
                    }
                    FIELD_ADDR -> {
                        checkAtLeast2Items() // Need base_address, field_offset
                        val fieldOffset = pop()
                        val baseAddr = pop()
                        push(baseAddr + fieldOffset)
                    }
                    MEMCPY -> {
                        checkAtLeast3Items() // Need dest, src, size
                        val size = pop()
                        val src = pop()
                        val dest = pop()
                        performMemcpy(dest, src, size)
                    }
                    MEMSET -> {
                        checkAtLeast3Items() // Need dest, value, size
                        val size = pop()
                        val value = pop()
                        val dest = pop()
                        performMemset(dest, value, size)
                    }
                    else -> nextDecoder.decodeInstruction(instruction)
                }
            }
        }
    }

    private fun performMemcpy(dest: Int, src: Int, size: Int) {
        val memory = cpu.getMemory()
        for (i in 0 until size) {
            val value = memory.get(src + i)
            memory.set(dest + i, value)
        }
    }

    private fun performMemset(dest: Int, value: Int, size: Int) {
        val memory = cpu.getMemory()
        for (i in 0 until size) {
            memory.set(dest + i, value)
        }
    }

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
    }
}