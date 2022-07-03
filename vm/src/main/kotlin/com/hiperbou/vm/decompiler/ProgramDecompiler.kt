package com.hiperbou.vm.decompiler

import com.hiperbou.vm.Instruction

data class Literal(val value:Int):Instruction{
    override fun toString() = value.toString()
}

data class Label(val value:Int):Instruction{
    override fun toString() = value.toString()
}


class ProgramDecompiler(private val opcodeInformation: OpcodeInformation = CoreOpcodeInformation()) {
    fun decompile(program:IntArray):List<Instruction> {
        var params = 0
        var label = false
        return program.map {
            if (params > 0) {
                params--
                if(label) Label(it) else Literal(it)
            } else {
                opcodeInformation.getOpcode(it).also { opcode ->
                    params = opcode.params
                    label = opcode.label
                }
            }
        }
    }
}