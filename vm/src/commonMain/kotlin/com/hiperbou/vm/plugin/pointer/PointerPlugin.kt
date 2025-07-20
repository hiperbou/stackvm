package com.hiperbou.vm.plugin.pointer

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.OpcodeInformation
import com.hiperbou.vm.plugin.pointer.PointerInstructions.ADDR_OF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.DEREF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_ADD
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_DIFF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_SUB

object PointerInstructions {
    const val PTR_ADD = 0x8B    // Add offset to pointer (optimized pointer + integer)
    const val PTR_SUB = 0x8C    // Subtract offset from pointer
    const val PTR_DIFF = 0x8D   // Calculate difference between two pointers
    const val DEREF = 0x8E      // Dereference pointer (load value at address)
    const val ADDR_OF = 0x8F    // Get address of variable
}

enum class PointerInstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false): Opcode {
    PTR_ADD(PointerInstructions.PTR_ADD),
    PTR_SUB(PointerInstructions.PTR_SUB),
    PTR_DIFF(PointerInstructions.PTR_DIFF),
    DEREF(PointerInstructions.DEREF),
    ADDR_OF(PointerInstructions.ADDR_OF),
}

class PointerOpcodeInformation: OpcodeInformation {
    private val values = PointerInstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = PointerInstructionsEnum.values().associateBy { it.name }
    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}

class PointerDecoder(
    private val cpu: CPU,
    private val stack: CPUStack<Int>,
    private var nextDecoder: Decoder = ExceptionDecoder.instance
): Decoder {

    override fun decodeInstruction(instruction: Int) {
        with(stack) {
            with(cpu) {
                when (instruction) {
                    PTR_ADD -> {
                        checkAtLeast2Items() // Need pointer, offset
                        val offset = pop()
                        val pointer = pop()
                        push(pointer + offset)
                    }
                    PTR_SUB -> {
                        checkAtLeast2Items() // Need pointer, offset
                        val offset = pop()
                        val pointer = pop()
                        push(pointer - offset)
                    }
                    PTR_DIFF -> {
                        checkAtLeast2Items() // Need ptr1, ptr2
                        val ptr2 = pop()
                        val ptr1 = pop()
                        push(ptr1 - ptr2)
                    }
                    DEREF -> {
                        checkIsNotEmpty("DEREF") // Need pointer address
                        val address = pop()
                        push(getMemory().get(address))
                    }
                    ADDR_OF -> {
                        val varNumber = getNextWordFromProgram("Should have variable number after ADDR_OF")
                        // For local variables, we'd need to calculate frame-relative addresses
                        // This is a simplified implementation
                        push(varNumber) // In a real implementation, this would be the actual memory address
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