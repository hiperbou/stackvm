package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.OpcodeExpression

class OpcodeParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        val opcode = parser.opcodeInformation.tryGetOpcode(token.text) ?: throw InvalidProgramException("Unresolved opcode '${token.text}' ${parser.debugLine(token.currentLine)}")
        if (opcode.params>0 && !parser.expectParameters()) throw InvalidProgramException("Expected parameters for ${token.text} ${parser.debugLine(token.currentLine)}")
        return OpcodeExpression(opcode, token.currentLine, parser)
    }
}

