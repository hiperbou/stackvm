package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.IdentifierExpression

class IdentifierParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        return IdentifierExpression(token.text, parser)
    }
}