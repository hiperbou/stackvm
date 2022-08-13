package com.hiperbou.vm.decompiler

import com.hiperbou.vm.InstructionsEnum
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.Opcode

interface OpcodeInformation {
    fun getOpcode(opcode:Int):Opcode
    fun tryGetOpcode(opcode: Int):Opcode?
    fun tryGetOpcode(opcode: String):Opcode?
}

class OpcodeInformationChain(vararg opcodeInfoProviders: OpcodeInformation):OpcodeInformation {
    private val list = opcodeInfoProviders

    override fun getOpcode(opcode: Int):Opcode {
        return tryGetOpcode(opcode) ?:
        throw InvalidProgramException("Opcode $opcode not found in any of the providers.")
    }

    override fun tryGetOpcode(opcode: Int): Opcode? {
        list.forEach {
            it.tryGetOpcode(opcode)?.let { return it }
        }
        return null
    }

    override fun tryGetOpcode(opcode: String): Opcode? {
        list.forEach {
            it.tryGetOpcode(opcode)?.let { return it }
        }
        return null
    }
}

class CoreOpcodeInformation:OpcodeInformation {
    private val values = InstructionsEnum.values().associateBy { it.opcode }
    private val valuesByName = InstructionsEnum.values().associateBy { it.name }

    override fun getOpcode(opcode:Int) = tryGetOpcode(opcode)!!
    override fun tryGetOpcode(opcode: Int) = values[opcode]
    override fun tryGetOpcode(opcode: String) = valuesByName[opcode]
}