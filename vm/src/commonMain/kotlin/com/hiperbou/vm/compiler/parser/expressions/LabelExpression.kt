package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.Parser

class LabelExpression(val label: String, private val currentLine:Int, private val parser: Parser) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append("LABEL(")
        builder.append(label)
        builder.append(")")
    }

    override fun solveExpression(): Int {
        parser.labelResolver.addLabel(label, parser.programWriter.currentAddress()) { parser.debugLine(currentLine) }
        return Int.MIN_VALUE
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        solveExpression()
    }
}

class LabelWithValueExpression(val label: String, private val right:Expression, private val parser: Parser) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append("LABEL(")
        builder.append(label)
        builder.append(": ")
        right.print(builder)
        builder.append(")")
    }

    override fun solveExpression(): Int {
        throw UnsupportedOperationException()
        //return -1
    }

    override fun compileExpression(programWriter: ProgramWriter) { }
}