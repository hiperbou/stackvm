package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.lexer.CToken

/**
 * Parses an infix position token (an operator that appears between two expressions).
 * Examples: binary arithmetic operators, comparison operators, function call `(`.
 */
interface CInfixParselet {
    /** The binding power of this operator. Higher = tighter binding. */
    val precedence: Int

    fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression
}
