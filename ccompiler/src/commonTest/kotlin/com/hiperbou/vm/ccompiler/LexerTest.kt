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
    fun emptySourceProducesOnlyEofTest() {
        val tokens = CLexer("").tokenize()
        assertEquals(1, tokens.size)
        assertEquals(CTokenType.EOF, tokens[0].type)
    }

    @Test
    fun integerLiteralTest() {
        val types = tokenTypes("42")
        assertEquals(listOf(CTokenType.NUMBER, CTokenType.EOF), types)
        assertEquals("42", CLexer("42").tokenize()[0].text)
    }

    @Test
    fun keywordsAreRecognizedTest() {
        val types = tokenTypes("int return if else while for do switch case default break continue print debugPrint")
        assertEquals(
            listOf(
                CTokenType.INT, CTokenType.RETURN, CTokenType.IF, CTokenType.ELSE,
                CTokenType.WHILE, CTokenType.FOR, CTokenType.DO, CTokenType.SWITCH, CTokenType.CASE, CTokenType.DEFAULT, CTokenType.BREAK, CTokenType.CONTINUE, CTokenType.PRINT, CTokenType.DEBUG_PRINT, CTokenType.EOF
            ),
            types
        )
    }

    @Test
    fun identifierIsNotAKeywordTest() {
        val types = tokenTypes("myVar")
        assertEquals(listOf(CTokenType.IDENTIFIER, CTokenType.EOF), types)
        assertEquals("myVar", CLexer("myVar").tokenize()[0].text)
    }

    @Test
    fun arithmeticOperatorsTest() {
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
    fun comparisonOperatorsTest() {
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
    fun assignmentOperatorsTest() {
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
    fun incrementAndDecrementTest() {
        val types = tokenTypes("++ --")
        assertEquals(listOf(CTokenType.PLUS_PLUS, CTokenType.MINUS_MINUS, CTokenType.EOF), types)
    }

    @Test
    fun Test() {
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
    fun logicalOperatorsTest() {
        val types = tokenTypes("&& || !")
        assertEquals(
            listOf(CTokenType.AMP_AMP, CTokenType.PIPE_PIPE, CTokenType.BANG, CTokenType.EOF),
            types
        )
    }

    @Test
    fun whitespaceAndNewlinesAreSkippedTest() {
        val types = tokenTypes("  int  \n  x  \t  ;  ")
        assertEquals(listOf(CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.SEMICOLON, CTokenType.EOF), types)
    }

    @Test
    fun lineCommentIsSkippedTest() {
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
    fun blockCommentIsSkippedTest() {
        val types = tokenTypes("int /* comment */ x;")
        assertEquals(listOf(CTokenType.INT, CTokenType.IDENTIFIER, CTokenType.SEMICOLON, CTokenType.EOF), types)
    }

    @Test
    fun lineAndColumnTrackingTest() {
        val tokens = CLexer("int\nx").tokenize()
        assertEquals(1, tokens[0].line)
        assertEquals(1, tokens[0].column)
        assertEquals(2, tokens[1].line)
        assertEquals(1, tokens[1].column)
    }

    @Test
    fun minimalMainFunctionTokensTest() {
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
    fun unexpectedCharacterThrowsLexerexceptionTest() {
        assertFailsWith<com.hiperbou.vm.ccompiler.lexer.LexerException> {
            CLexer("int x = @;").tokenize()
        }
    }
    @Test
    fun ternaryTokensTest() {
        val types = tokenTypes("? :")
        assertEquals(listOf(CTokenType.QUESTION, CTokenType.COLON, CTokenType.EOF), types)
    }    @Test
    fun arrayTokensTest() {
        val types = tokenTypes("[ ]")
        assertEquals(listOf(CTokenType.LBRACKET, CTokenType.RBRACKET, CTokenType.EOF), types)
    }

}

