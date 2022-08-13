package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.NumberExpression

class NumberParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        return NumberExpression(token.text.toInt())
    }
}