package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.lexer.CToken

/**
 * Parses a prefix position token (the first token of an expression).
 * Examples: number literals, identifiers, unary minus, grouped expressions.
 */
interface CPrefixParselet {
    fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression
}
