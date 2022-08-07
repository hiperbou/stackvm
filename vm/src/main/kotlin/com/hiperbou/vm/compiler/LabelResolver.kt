package com.hiperbou.vm.compiler

import com.hiperbou.vm.InvalidProgramException

class LabelResolver {
    private data class UnresolvedAddress(val label: String, val position: Int, val inputLine:()->String)
    private val labelsToResolve = mutableListOf<UnresolvedAddress>()
    private val labelsAddresses = mutableMapOf<String, Int>()
    val UNRESOLVED_JUMP_ADDRESS = Int.MIN_VALUE

    fun resolveLabels(program:MutableList<Int>) {
        labelsToResolve.forEach {
            val destination = labelsAddresses[it.label]
                ?: throw InvalidProgramException("Unresolved label ${it.label} ${it.inputLine()}")

            if (destination == UNRESOLVED_JUMP_ADDRESS) throw InvalidProgramException("Label self referencing ${it.label} as its own value ${it.inputLine()}")
            if (program[it.position] != UNRESOLVED_JUMP_ADDRESS) throw InvalidProgramException("Undefined label ${it.label} used in expression ${it.inputLine()}")
            program[it.position] = destination
        }
        cleanLabels()
    }

    fun cleanLabels() {
        labelsToResolve.clear()
        labelsAddresses.clear()
    }

    fun addLabel(label:String, value:Int, inputLine: ()->String) {
        labelsAddresses.putIfAbsent(label, value)?.let {
            throw InvalidProgramException("Duplicated label '$label' ${inputLine()}")
        }
    }

    fun addUnresolvedLabel(label:String, position:Int, inputLine: () -> String) {
        labelsToResolve.add(UnresolvedAddress(label, position, inputLine))
    }

    fun getLabelAddress(label:String):Int? {
        return labelsAddresses[label]
    }
}
