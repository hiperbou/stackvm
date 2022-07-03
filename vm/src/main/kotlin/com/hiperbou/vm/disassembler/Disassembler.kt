package com.hiperbou.vm.disassembler

import com.hiperbou.vm.Instruction
import com.hiperbou.vm.Opcode
import com.hiperbou.vm.decompiler.Label
import com.hiperbou.vm.decompiler.Literal

class Disassembler {
    fun disassemble(instructions:List<Instruction>):String {
        val labels = findLabels(instructions)
        val labelsByAddress = labels.associateBy { it.address }
        val labelsByUsage = labels.associateBy { it.usageAddress }

        var address = 0
        val sb = StringBuilder()
        writeLabel(labelsByAddress, address, sb, false)
        sb.append(instructions.first().toString())

        instructions.drop(1).forEach {
            address++
            when{
                it is Literal -> {
                    sb.append(" ")
                    sb.append(it.toString())
                }
                it is Label -> {
                    sb.append(" ")
                    sb.append(getLabelName(labelsByUsage, address))
                }
                else -> {
                    writeLabel(labelsByAddress, address, sb, true)
                    sb.append("\n")
                    sb.append(it.toString())
                }
            }
        }
        sb.append("\n")
        return sb.toString()
    }

    private fun writeLabel(labelsByAddress: Map<Int, LabelAddressPair>, address:Int, sb:StringBuilder, prependEndline:Boolean) {
        labelsByAddress.get(address)?.let {
            if (prependEndline) sb.append("\n")
            if (address > 0) sb.append("\n")
            sb.append(formatLabel(it.address))
            sb.append(":")
            if (!prependEndline) sb.append("\n")
        }
    }

    private fun getLabelName(labelsByUsage: Map<Int, LabelAddressPair>, addresss:Int):String {
        return formatLabel(labelsByUsage.getValue(addresss - 1).address)
    }

    private fun formatLabel(address:Int) = "label_$address"

    data class LabelAddressPair(val opcode: Opcode, val address:Int, val usageAddress:Int)

    private fun findLabels(instructions: List<Instruction>):List<LabelAddressPair> {
        return instructions.mapIndexedNotNull { index, it ->
            if (it is Opcode && it.label) {
                val address = instructions[index + 1] as Label
                LabelAddressPair(it, address.value, index)
            } else {
                null
            }
        }
    }
}