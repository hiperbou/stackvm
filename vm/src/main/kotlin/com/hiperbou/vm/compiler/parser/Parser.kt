package com.hiperbou.vm.compiler.parser

import com.hiperbou.vm.compiler.LabelResolver
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.parselets.InfixParselet
import com.hiperbou.vm.compiler.parser.parselets.PrefixParselet
import com.hiperbou.vm.decompiler.OpcodeInformation

open class Parser(private val tokens: Iterator<Token>,
                  val opcodeInformation: OpcodeInformation,
                  val programWriter: ProgramWriter,
                  val labelResolver: LabelResolver
            ) {

    fun register(token: TokenType, parselet: PrefixParselet) {
        prefixParselets[token] = parselet
    }

    fun register(token: TokenType, parselet: InfixParselet) {
        infixParselets[token] = parselet
    }

    var currentLine = 1

    private fun consumeEOL() {
        while(match(TokenType.EOL)) { currentLine++ }
    }

    private fun isEOL():Boolean {
        return if (match(TokenType.EOL) || match(TokenType.EOF)) {
            currentLine++
            true
        } else false
    }

    fun parse():List<Expression> {
        val list = mutableListOf<Expression>()
        consumeEOL()
        while(!match(TokenType.EOF)) {
            parseLine(list)
            consumeEOL()
        }
        return list
    }

    private fun parseLine(list:MutableList<Expression>):List<Expression> {
        list.add(parseOpcodeOrLabel())
        if (!isEOL()) {
            list.add(parseExpression())
        }
        return list
    }

    private fun parseOpcodeOrLabel():Expression {
        val token = consume()

        return if(match(TokenType.COLON)) {
            parseLabel(Token(TokenType.LABEL, token.text))
        } else {
            parseOpcode(Token(TokenType.OPCODE, token.text))
        }
    }

    private fun parseLabel(token:Token) = if(peek(TokenType.EOL)) {
        val prefixParselet = prefixParselets[token.type]
        ?: throw ParseException("""Could not parse label "${token.text}"""")

        prefixParselet.parse(this, token)
    } else {
        parseLabelWithExpression(Token(TokenType.LABEL_WITH_VALUE, token.text))
    }

    private fun parseLabelWithExpression(token: Token): Expression {
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("""Could not parse label with expression "${token.text}"""")

        return prefixParselet.parse(this, token)
    }

    private fun parseOpcode(token:Token):Expression {
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("""Could not parse opcode "${token.text}"""")

        return prefixParselet.parse(this, token)
    }

    fun parseExpression(precedence: Int = 0): Expression {
        var token = consume()
        val prefixParselet = prefixParselets[token.type]
            ?: throw ParseException("""Could not parse expression "${token.text}"""")

        var left = prefixParselet.parse(this, token)
        while (precedence < this.precedence) {
            token = consume()
            val infixParselet = infixParselets[token.type]
                ?: throw ParseException("""Could not parse "${token.text}"""")

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

    fun consume(expected: TokenType): Token {
        val token = lookAhead(0)
        if (token.type != expected) {
            throw RuntimeException("Expected token $expected and found ${token.type}")
        }
        return consume()
    }

    private fun consume(): Token {
        lookAhead(0)
        return tokensRead.removeAt(0)
    }

    private fun lookAhead(distance: Int): Token {
        while (distance >= tokensRead.size) {
            tokensRead.add(tokens.next())
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