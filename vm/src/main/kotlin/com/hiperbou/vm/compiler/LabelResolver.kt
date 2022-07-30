package com.hiperbou.vm.compiler

import com.hiperbou.vm.InvalidProgramException

class LabelResolver {
    private data class UnresolvedAddress(val label: String, val position: Int)
    private val labelsToResolve = mutableListOf<UnresolvedAddress>()
    private val labelsAddresses = mutableMapOf<String, Int>()
    val UNRESOLVED_JUMP_ADDRESS = -1

    fun resolveLabels(program:MutableList<Int>) {
        labelsToResolve.forEach {
            val destination = labelsAddresses[it.label]
                ?: throw InvalidProgramException("Unresolved label ${it.label}")
            val currentLine = 0 //TODO:
            val inputLine = "" //TODO:
            if (destination == UNRESOLVED_JUMP_ADDRESS) throw InvalidProgramException("Label self referencing $it as its own value in line: $currentLine\n'$inputLine'")
            assert(program[it.position] == UNRESOLVED_JUMP_ADDRESS)
            program[it.position] = destination
        }
        cleanLabels()
    }

    fun cleanLabels() {
        labelsToResolve.clear()
        labelsAddresses.clear()
    }

    fun addLabel(label:String, value:Int, currentLine:Int, inputLine: String) {
        //TODO this can be deprecated with the new parser
        fun String.removeLabelColon():String {
            return if(last()==':') dropLast(1) else this
        }

        labelsAddresses.putIfAbsent(label.removeLabelColon(), value)?.let {
            throw InvalidProgramException("Duplicated label $label in line: $currentLine\n'$inputLine'")
        }
    }

    fun addUnresolvedLabel(label:String, position:Int) {
        labelsToResolve.add(UnresolvedAddress(label, position))
    }

    fun getLabelAddress(label:String):Int? {
        return labelsAddresses[label]
    }
}
