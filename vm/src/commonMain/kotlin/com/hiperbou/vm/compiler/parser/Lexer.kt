package com.hiperbou.vm.compiler.parser

import com.hiperbou.vm.InvalidProgramException

class Lexer(private val text: String) : MutableIterator<Token> {

    private val punctuators = TokenType.values().filter { it.punctuator != null }.associateBy { it.punctuator!! }
    private var index = 0
    var currentLine = 1

    private val NEWLINE = "(\n|\r|\n\r)".toRegex()
    private fun getLineText(lineNumber:Int) = text.split(NEWLINE).get(lineNumber - 1)
    private fun getCurrentLineText() = getLineText(currentLine)

    fun debugLine() = debugLine(currentLine)
    fun debugLine(lineNumber:Int) = "at line: ${lineNumber}\n${getLineText(lineNumber)}"

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): Token {
        fun isNewLine(c:Char):Boolean {
            return c == '\n'
        }

        fun isNewLineCarriageReturn(r:Char, n:Char):Boolean {
            if(r == '\r' && n == '\n') return true.also { index++ }
            return r == '\r'
        }

        fun ignoreSingleLineComment() {
            do {
                index++
            } while (index < text.length && !isNewLine(text[index]))
        }

        fun ignoreBlockComment() {
            do {
                index++
                if (isNewLine(text[index])) currentLine++
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
                    return Token(punctuators.getValue(c), c.toString(), currentLine)
                }
                Character.isDigit(c) -> {
                    val start = index - 1
                    while (index < text.length) {
                        if (!Character.isDigit(text[index])) break
                        index++
                    }
                    val number = text.substring(start, index)
                    return Token(TokenType.NUMBER, number, currentLine)
                }
                Character.isLetter(c) -> {
                    val start = index - 1
                    while (index < text.length) {
                        if (!Character.isLetterOrDigit(text[index]) && text[index]!='_') break
                        index++
                    }
                    val name = text.substring(start, index)
                    return Token(TokenType.IDENTIFIER, name, currentLine)
                }

                isNewLine(c) || (index < text.length && isNewLineCarriageReturn(c, text[index])) -> {
                    return Token(TokenType.EOL, TokenType.EOL.name, currentLine).also {
                        currentLine++
                    }
                }
                Character.isWhitespace(c) -> { }
                else -> throw InvalidProgramException("Invalid token $c in line: $currentLine\n${getCurrentLineText()}")
            }
        }

        return Token(TokenType.EOF, "EOF", currentLine)
    }

    override fun remove() {
        throw UnsupportedOperationException()
    }


}

object Character{
    fun isDigit(c: Char): Boolean {
        return c.isDigit()
    }

    private fun isUnderscore(c:Char) = c.code == '_'.code

    fun isLetter(c: Char): Boolean {
        return c.isLetter() || isUnderscore(c)
    }

    fun isLetterOrDigit(c:Char) = c.isLetterOrDigit() || isUnderscore(c)

    fun isWhitespace(c:Char) = c.isWhitespace()
}