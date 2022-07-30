package com.hiperbou.vm.compiler.parser

import com.hiperbou.vm.compiler.LabelResolver
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.parselets.*
import com.hiperbou.vm.decompiler.OpcodeInformation

class AsmProgramParser(lexer: Lexer,
                       opcodeInformation: OpcodeInformation,
                       programWriter: ProgramWriter,
                       labelResolver:LabelResolver
) : Parser(lexer, opcodeInformation,programWriter, labelResolver) {
    init {
        register(TokenType.NUMBER, NumberParselet())
        register(TokenType.IDENTIFIER, IdentifierParselet())
        register(TokenType.OPCODE, OpcodeParselet())
        register(TokenType.LABEL, LabelParselet())
        register(TokenType.LABEL_WITH_VALUE, LabelWithValueParselet())
        register(TokenType.LEFT_PAREN, GroupParselet())

        prefix(TokenType.MINUS, Precedence.PREFIX)

        infixLeft(TokenType.PLUS, Precedence.SUM)
        infixLeft(TokenType.MINUS, Precedence.SUM)
        infixLeft(TokenType.ASTERISK, Precedence.PRODUCT)
        infixLeft(TokenType.SLASH, Precedence.PRODUCT)
    }

    private fun postfix(token: TokenType, precedence: Int) {
        register(token, PostfixOperatorParselet(precedence))
    }

    private fun prefix(token: TokenType, precedence: Int) {
        register(token, PrefixOperatorParselet(precedence))
    }

    private fun infixLeft(token: TokenType, precedence: Int) {
        register(token, BinaryOperatorParselet(precedence, false))
    }

    private fun infixRight(token: TokenType, precedence: Int) {
        register(token, BinaryOperatorParselet(precedence, true))
    }
}