package com.hiperbou.vm.compiler

import com.hiperbou.vm.compiler.parser.AsmProgramParser
import com.hiperbou.vm.compiler.parser.Lexer
import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformation

class Compiler(private val opcodeInformation: OpcodeInformation = CoreOpcodeInformation()) {

    private val labelResolver = LabelResolver()

    fun generateProgram(inputText: String,
                        programWriter:ProgramWriter = DefaultProgramWriter()
    ):IntArray {
        labelResolver.cleanLabels()

        val lexer = Lexer(inputText)
        val parser: Parser = AsmProgramParser(lexer, opcodeInformation, programWriter, labelResolver)

        programWriter.setCurrentLineNumberProvider { parser.currentLine }

        parser.parse().forEach {
            it.compileExpression(programWriter)
        }

        labelResolver.resolveLabels(programWriter.program)
        return programWriter.program.toIntArray()
    }
}
