package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.ccompiler.lexer.CLexer
import com.hiperbou.vm.ccompiler.lexer.CTokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexerTest {

    private fun tokenTypes(source: String): List<CTokenType> {
        return CLexer(source).tokenize().map { it.type }
    }

    private fun tokenTexts(source: String): List<String> {
        return CLexer(source).tokenize().dropLast(1).map { it.text } // drop EOF
    }

    @Test
    fun `empty source produces only EOF`() {
        val tokens = CLexer("").tokenize()
        assertEquals(1, tokens.size)
        assertEquals(CTokenType.EOF, tokens[0].type)
    }

    @Test
    fun `integer literal`() {
        val types = tokenTypes("42")
        assertEquals(listOf(CTokenType.NUMBER, CTokenType.EOF), types)
        assertEquals("42", CLexer("42").tokenize()[0].text)
    }

    @Test
    fun `keywords are recognized`() {
        val types = tokenTypes("int return if else while for do break continue print debugPrint")
        assertEquals(
            listOf(
                CTokenType.INT, CTokenType.RETURN, CTokenType.IF, CTokenType.ELSE,
                CTokenType.WHILE, CTokenType.FOR, CTokenType.DO, CTokenType.BREAK, CTokenType.CONTINUE, CTokenType.PRINT, CTokenType.DEBUG_PRINT, CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `identifier is not a keyword`() {
        val types = tokenTypes("myVar")
        assertEquals(listOf(CTokenType.IDENTIFIER, CTokenType.EOF), types)
        assertEquals("myVar", CLexer("myVar").tokenize()[0].text)
    }

    @Test
    fun `arithmetic operators`() {
        val types = tokenTypes("+ - * / %")
        assertEquals(
            listOf(
                CTokenType.PLUS, CTokenType.MINUS, CTokenType.STAR,
                CTokenType.SLASH, CTokenType.PERCENT, CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `comparison operators`() {
        val types = tokenTypes("== != < > <= >=")
        assertEquals(
            listOf(
                CTokenType.EQ_EQ, CTokenType.BANG_EQ,
                CTokenType.LT, CTokenType.GT,
                CTokenType.LT_EQ, CTokenType.GT_EQ,
                CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `assignment operators`() {
        val types = tokenTypes("= += -= *= /=")
        assertEquals(
            listOf(
                CTokenType.EQ, CTokenType.PLUS_EQ, CTokenType.MINUS_EQ,
                CTokenType.STAR_EQ, CTokenType.SLASH_EQ, CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `increment and decrement`() {
        val types = tokenTypes("++ --")
        assertEquals(listOf(CTokenType.PLUS_PLUS, CTokenType.MINUS_MINUS, CTokenType.EOF), types)
    }

    @Test
    fun `punctuation`() {
        val types = tokenTypes("( ) { } ; ,")
        assertEquals(
            listOf(
                CTokenType.LPAREN, CTokenType.RPAREN,
                CTokenType.LBRACE, CTokenType.RBRACE,
                CTokenType.SEMICOLON, CTokenType.COMMA,
                CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `logical operators`() {
        val types = tokenTypes("&& || !")
        assertEquals(
            listOf(CTokenType.AMP_AMP, CTokenType.PIPE_PIPE, CTokenType.BANG, CTokenType.EOF),
            types
        )
    }

    @Test
    fun `whitespace and newlines are skipped`() {
        val types = tokenTypes("  int  \n  x  \t  ;  ")
        assertEquals(listOf(CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.SEMICOLON, CTokenType.EOF), types)
    }

    @Test
    fun `line comment is skipped`() {
        val types = tokenTypes("int x; // this is a comment\nreturn 0;")
        assertEquals(
            listOf(
                CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.SEMICOLON,
                CTokenType.RETURN, CTokenType.NUMBER, CTokenType.SEMICOLON,
                CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `block comment is skipped`() {
        val types = tokenTypes("int /* comment */ x;")
        assertEquals(listOf(CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.SEMICOLON, CTokenType.EOF), types)
    }

    @Test
    fun `line and column tracking`() {
        val tokens = CLexer("int\nx").tokenize()
        assertEquals(1, tokens[0].line)
        assertEquals(1, tokens[0].column)
        assertEquals(2, tokens[1].line)
        assertEquals(1, tokens[1].column)
    }

    @Test
    fun `minimal main function tokens`() {
        val source = "int main() { print(42); return 0; }"
        val types = tokenTypes(source)
        assertEquals(
            listOf(
                CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.LPAREN, CTokenType.RPAREN,
                CTokenType.LBRACE,
                CTokenType.PRINT, CTokenType.LPAREN, CTokenType.NUMBER, CTokenType.RPAREN, CTokenType.SEMICOLON,
                CTokenType.RETURN, CTokenType.NUMBER, CTokenType.SEMICOLON,
                CTokenType.RBRACE,
                CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun `unexpected character throws LexerException`() {
        assertFailsWith<com.hiperbou.vm.ccompiler.lexer.LexerException> {
            CLexer("int x = @;").tokenize()
        }
    }
}




