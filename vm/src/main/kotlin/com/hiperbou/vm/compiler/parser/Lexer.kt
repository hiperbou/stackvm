package com.hiperbou.vm.compiler.parser

import com.hiperbou.vm.InvalidProgramException
import java.lang.Character.LINE_SEPARATOR

class Lexer(private val text: String) : MutableIterator<Token> {
    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): Token {
        fun isNewLine(c:Char):Boolean {
            //TODO check (\r\n | \r)
            return c == '\n' || Character.getType(c).toByte() == LINE_SEPARATOR
        }

        fun ignoreSingleLineComment() {
            do {
                index++
            } while (index < text.length && !isNewLine(text[index]))
        }

        fun ignoreBlockComment() {
            do {
                index++
            } while (index + 1 < text.length && !(text[index] == '*' && text[index + 1] == '/'))
            index += 2
        }

        fun isSingleLineComment():Boolean{
            return index < text.length && text[index-1] == '/' && text[index] == '/'
        }
        fun isBlockComment():Boolean{
            return index < text.length && text[index-1] == '/' && text[index] == '*'
        }

        while (index < text.length) {
            val c = text[index++]
            when {
                isSingleLineComment() -> { ignoreSingleLineComment() }
                isBlockComment() -> { ignoreBlockComment() }

                punctuators.containsKey(c) -> {
                    return Token(punctuators.getValue(c), c.toString())
                }
                Character.isDigit(c) -> {
                    val start = index - 1
                    while (index < text.length) {
                        if (!Character.isDigit(text[index])) break
                        index++
                    }
                    val number = text.substring(start, index)
                    return Token(TokenType.NUMBER, number)
                }
                Character.isLetter(c) -> {
                    val start = index - 1
                    while (index < text.length) {
                        if (!Character.isLetterOrDigit(text[index]) && text[index]!='_') break
                        index++
                    }
                    val name = text.substring(start, index)
                    return Token(TokenType.IDENTIFIER, name)
                }
                Character.isWhitespace(c) -> { }
                else -> throw InvalidProgramException("Invalid token $c in expression $text")
            }
        }

        return Token(TokenType.EOF, "EOF")
    }

    override fun remove() {
        throw UnsupportedOperationException()
    }

    private val punctuators = TokenType.values().filter { it.punctuator != null }.associateBy { it.punctuator!! }
    private var index = 0
}