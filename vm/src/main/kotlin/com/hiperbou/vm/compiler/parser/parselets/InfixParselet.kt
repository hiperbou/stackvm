package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression

interface InfixParselet {
    fun parse(parser: Parser, left: Expression, token: Token): Expression
    val precedence: Int
}