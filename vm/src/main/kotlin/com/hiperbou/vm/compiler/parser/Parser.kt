package com.hiperbou.vm.compiler.parser

import com.hiperbou.vm.compiler.LabelResolver
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.parselets.InfixParselet
import com.hiperbou.vm.compiler.parser.parselets.PrefixParselet
import com.hiperbou.vm.decompiler.OpcodeInformation

open class Parser(private val lexer: Lexer,
                  val opcodeInformation: OpcodeInformation,
                  val programWriter: ProgramWriter,
                  val labelResolver: LabelResolver
            ):MutableIterator<Expression> {

    private val expressionsRead = mutableListOf<Expression>()

    fun debugLine(lineNumber:Int):String {
        return lexer.debugLine(lineNumber)
    }

    override fun hasNext(): Boolean {
        return expressionsRead.isNotEmpty() || !match(TokenType.EOF)
    }

    override fun next(): Expression {
        if(expressionsRead.isNotEmpty()) return expressionsRead.removeAt(0)

        consumeEOL()
        parseStatement(expressionsRead)
        consumeEOL()
        return expressionsRead.removeAt(0)
    }

    override fun remove() {
        throw UnsupportedOperationException()
    }

    fun register(token: TokenType, parselet: PrefixParselet) {
        prefixParselets[token] = parselet
    }

    fun register(token: TokenType, parselet: InfixParselet) {
        infixParselets[token] = parselet
    }

    private fun consumeEOL() {
        while(match(TokenType.EOL)) { /**/ }
    }

    private fun isEOL():Boolean {
        return match(TokenType.EOL) || match(TokenType.EOF)
    }

    private fun parseStatement(list:MutableList<Expression>):List<Expression> {
        list.add(parseOpcodeOrLabel())
        if (!isEOL()) {
            list.add(parseExpression())
        }
        return list
    }

    private fun parseOpcodeOrLabel():Expression {
        val token = consume()

        return if(match(TokenType.COLON)) {
            parseLabel(token.withType(TokenType.LABEL))
        } else {
            parseOpcode(token.withType(TokenType.OPCODE))
        }
    }

    private fun parseLabel(token:Token) = if(peek(TokenType.EOL)) {
        val prefixParselet = prefixParselets[token.type]
        ?: throw ParseException("Could not parse label '${token.text}' ${lexer.debugLine()}")

        prefixParselet.parse(this, token)
    } else {
        parseLabelWithExpression(token.withType(TokenType.LABEL_WITH_VALUE))
    }

    private fun parseLabelWithExpression(token: Token): Expression {
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("Could not parse label with expression '${token.text}' ${lexer.debugLine()}")

        return prefixParselet.parse(this, token)
    }

    private fun parseOpcode(token:Token):Expression {
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("Could not parse opcode '${token.text}' ${lexer.debugLine()}")

        return prefixParselet.parse(this, token)
    }

    fun parseExpression(precedence: Int = 0): Expression {
        var token = consume()
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("Could not parse expression '${token.text}' ${lexer.debugLine()}")

        var left = prefixParselet.parse(this, token)
        while (precedence < this.precedence) {
            token = consume()
            val infixParselet = infixParselets[token.type]
                ?: throw ParseException("Could not parse '${token.text}' ${lexer.debugLine()}")

            left = infixParselet.parse(this, left, token)
        }
        return left
    }

    private fun match(expected: TokenType): Boolean {
        val token = lookAhead(0)
        if (token.type != expected) {
            return false
        }
        consume()
        return true
    }

    private fun peek(expected: TokenType):Boolean {
        val token = lookAhead(0)
        return token.type == expected
    }

    private val expectedParams  = setOf(TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.MINUS, TokenType.LEFT_PAREN)
    fun expectParameters():Boolean {
        val token = lookAhead(0)
        return token.type in expectedParams
    }

    fun consume(expected: TokenType): Token {
        val token = lookAhead(0)
        if (token.type != expected) {
            throw RuntimeException("Expected token $expected and found ${token.type} ${lexer.debugLine()}")
        }
        return consume()
    }

    private fun consume(): Token {
        lookAhead(0)
        return tokensRead.removeAt(0)
    }

    private fun lookAhead(distance: Int): Token {
        while (distance >= tokensRead.size) {
            tokensRead.add(lexer.next())
        }

        return tokensRead[distance]
    }

    private val precedence: Int
        get() {
            val parser = infixParselets[lookAhead(0).type]
            return parser?.precedence ?: 0
        }
    private val tokensRead = mutableListOf<Token>()
    private val prefixParselets = mutableMapOf<TokenType, PrefixParselet>()
    private val infixParselets = mutableMapOf<TokenType, InfixParselet>()
}