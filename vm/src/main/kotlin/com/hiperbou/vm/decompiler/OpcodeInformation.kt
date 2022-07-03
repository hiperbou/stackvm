package com.hiperbou.vm.decompiler

import com.hiperbou.vm.InstructionsEnum
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.Opcode

interface OpcodeInformation {
    fun getOpcode(opcode:Int):Opcode
    fun tryGetOpcode(opcode: Int):Opcode?
}

class OpcodeInformationChain(vararg opcodeInfoProviders: OpcodeInformation):OpcodeInformation {
    private val list = opcodeInfoProviders

    override fun getOpcode(opcode: Int) = tryGetOpcode(opcode)

    override fun tryGetOpcode(opcode: Int): Opcode {
        list.forEach {
            it.tryGetOpcode(opcode)?.let { return it }
        }
        throw InvalidProgramException("Opcode $opcode not found in any of the providers.")
    }
}

class CoreOpcodeInformation:OpcodeInformation {
    private val values = InstructionsEnum.values().associateBy { it.opcode }

    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
}