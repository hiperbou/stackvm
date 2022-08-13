package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter

interface Expression {
    fun print(builder: StringBuilder)
    fun solveExpression():Int
    fun compileExpression(programWriter: ProgramWriter)
}