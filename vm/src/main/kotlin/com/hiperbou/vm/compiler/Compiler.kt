package com.hiperbou.vm.compiler

import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformation
import java.io.InputStreamReader

class Compiler(private val opcodeInformation: OpcodeInformation = CoreOpcodeInformation()) {
    private data class UnresolvedAddress(val label: String, val position: Int)
    private val labelsToResolve = mutableListOf<UnresolvedAddress>()
    private val labelsAddresses = mutableMapOf<String, Int>()
    private val UNRESOLVED_JUMP_ADDRESS = -1

    private fun resolveLabels(program:MutableList<Int>) {
        labelsToResolve.forEach {
            val destination = labelsAddresses[it.label]
                ?: throw InvalidProgramException("Unresolved label " + it.label)
            assert(program[it.position] == UNRESOLVED_JUMP_ADDRESS)
            program[it.position] = destination
        }
        cleanLabels()
    }

    private fun cleanLabels() {
        labelsToResolve.clear()
        labelsAddresses.clear()
    }

    fun generateProgram(inputStream: InputStreamReader,
                        programWriter:ProgramWriter = DefaultProgramWriter()
    ):IntArray {
        cleanLabels()

        var currentLine = 1
        programWriter.setCurrentLineNumberProvider { currentLine }

        inputStream.use {
            it.forEachLine {
                parseLine(programWriter, it, currentLine)
                currentLine++
            }
        }
        resolveLabels(programWriter.program)
        return programWriter.program.toIntArray()
    }

    private val IDENTIFIER = """[a-zA-Z][a-zA-Z0-9_]*""".toRegex()
    private val LABEL = """[a-zA-Z][a-zA-Z0-9_]*:""".toRegex()
    private val NUMBER = """-*\d+""".toRegex()

    private val WHITESPACE = """\s+""".toRegex()

    private val COMMENT = """//.*""".toRegex()
    private val COMMENT_BLOCK = """(?s)/\*.*?\*/""".toRegex()

    private fun tokenize(inputLine:String):List<String> {
        val line = inputLine.replace(COMMENT_BLOCK, "").replace(COMMENT, "").trim()
        if (line.isBlank()) return emptyList()

        return line.split(WHITESPACE)
    }

    private fun parseLine(programWriter:ProgramWriter, inputLine:String, currentLine:Int) = with(programWriter) {
        fun String.isNumber() = NUMBER.matches(this)
        fun String.isLabel() = LABEL.matches(this)
        fun String.isIdentifier() = IDENTIFIER.matches(this)

        fun String.tryProcessOpcode():Boolean {
            val opcode = opcodeInformation.tryGetOpcode(this)
            if (opcode != null) addInstruction(opcode)
            return opcode != null
        }

        fun String.removeLabelColon() = dropLast(1)

        tokenize(inputLine).forEach {
            when {
                it.isNumber() -> addLiteral(it.toInt())
                it.tryProcessOpcode() -> {}
                it.isLabel() -> {
                    labelsAddresses[it.removeLabelColon()] = currentAddress()
                }
                it.isIdentifier() -> {
                    labelsToResolve.add(UnresolvedAddress(it, currentAddress()))
                    addLiteral(UNRESOLVED_JUMP_ADDRESS)
                }
                else -> throw InvalidProgramException("Unknown token: '$it' in line: $currentLine\n'$inputLine'")
            }
        }
    }
}
