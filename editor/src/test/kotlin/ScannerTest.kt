import org.junit.jupiter.api.Test


class ScannerTest {

    enum class TokenType{
        // Single-character tokens.
        TOKEN_LEFT_PAREN, TOKEN_RIGHT_PAREN,
        TOKEN_LEFT_BRACE, TOKEN_RIGHT_BRACE,
        TOKEN_COMMA, TOKEN_DOT, TOKEN_MINUS, TOKEN_PLUS,
        TOKEN_SEMICOLON, TOKEN_SLASH, TOKEN_STAR,
        // One or two character tokens.
        TOKEN_BANG, TOKEN_BANG_EQUAL,
        TOKEN_EQUAL, TOKEN_EQUAL_EQUAL,
        TOKEN_GREATER, TOKEN_GREATER_EQUAL,
        TOKEN_LESS, TOKEN_LESS_EQUAL,
        // Literals.
        TOKEN_IDENTIFIER, TOKEN_STRING, TOKEN_NUMBER,
        // Keywords.
        TOKEN_AND, TOKEN_CLASS, TOKEN_ELSE, TOKEN_FALSE,
        TOKEN_FOR, TOKEN_FUN, TOKEN_IF, TOKEN_NIL, TOKEN_OR,
        TOKEN_PRINT, TOKEN_RETURN, TOKEN_SUPER, TOKEN_THIS,
        TOKEN_TRUE, TOKEN_VAR, TOKEN_WHILE,

        TOKEN_ERROR, TOKEN_EOF
    }
    data class Token(
        val type:TokenType,
        val start:Int, //was a pointer
        val length:Int,
        val line:Int
    )

    class Scanner(private val source: CharArray) {

        private val EOF = '\u0000'

        private val start: Char
            get() = source[_start]
        private val current: Char
            get() = source.getOrElse(_current) { EOF }
        private val previous: Char
            get() = source[_current - 1]
        private val next: Char
            get() = source[_current + 1]
        private val startNext: Char
            get() = source[_start + 1]

        private var _start = 0
        private var _current = 0
        private var line = 0

        private fun isAlpha(c: Char): Boolean {
            return c.code >= 'a'.code && c.code <= 'z'.code || c.code >= 'A'.code && c.code <= 'Z'.code || c.code == '_'.code
        }

        private fun isDigit(c: Char): Boolean {
            return c.code >= '0'.code && c.code <= '9'.code
        }

        private fun isAtEnd(): Boolean {
            return current == EOF
            //return _current >= source.lastIndex
        }

        private fun advance(): Char {
            _current++
            return previous
        }

        private fun peek(): Char {
            return current
        }

        private fun peekNext(): Char {
            return if (isAtEnd()) EOF else next
        }

        private fun match(expected: Char): Boolean {
            if (isAtEnd()) return false
            if (current != expected) return false;
            _current++
            return true
        }

        private fun makeToken(type: TokenType): Token {
            return Token(
                type,
                _start,
                (_current - _start),
                line
            )
        }

        private fun errorToken(message: String): Token {
            return Token(
                TokenType.TOKEN_ERROR,
                message.length, //this was a pointer to the string
                message.length,
                line
            )
        }

        private fun skipWhitespace() {
            while (!isAtEnd()) {
                val c = peek()
                when (c) {
                    ' ', '\r', '\t' -> advance()
                    '\n' -> {
                        line++
                        advance()
                    }
                    '/' -> if (peekNext() == '/') {
                        // A comment goes until the end of the line.
                        while (peek() != '\n' && !isAtEnd()) advance()
                    } else {
                        return
                    }
                    else -> return
                }
            }
        }

        private fun checkKeyword(start: Int, length: Int, rest: String, type: TokenType): TokenType {
            if (_current - _start == start + length &&
                source.slice(_start + start.._start + start + length - 1).joinToString("") == rest
            ) {
                return type
            }
            return TokenType.TOKEN_IDENTIFIER
        }

        private fun identifierType(): TokenType {
//> keywords
            when (start) {
                'a' -> return checkKeyword(1, 2, "nd", TokenType.TOKEN_AND)
                'c' -> return checkKeyword(1, 4, "lass", TokenType.TOKEN_CLASS)
                'e' -> return checkKeyword(1, 3, "lse", TokenType.TOKEN_ELSE)
                'f' -> if (_current - _start > 1) {
                    when (startNext) {
                        'a' -> return checkKeyword(2, 3, "lse", TokenType.TOKEN_FALSE)
                        'o' -> return checkKeyword(2, 1, "r", TokenType.TOKEN_FOR)
                        'u' -> return checkKeyword(2, 1, "n", TokenType.TOKEN_FUN)
                    }
                }
                'i' -> return checkKeyword(1, 1, "f", TokenType.TOKEN_IF)
                'n' -> return checkKeyword(1, 2, "il", TokenType.TOKEN_NIL)
                'o' -> return checkKeyword(1, 1, "r", TokenType.TOKEN_OR)
                'p' -> return checkKeyword(1, 4, "rint", TokenType.TOKEN_PRINT)
                'r' -> return checkKeyword(1, 5, "eturn", TokenType.TOKEN_RETURN)
                's' -> return checkKeyword(1, 4, "uper", TokenType.TOKEN_SUPER)
                't' -> if (_current - _start > 1) {
                    when (startNext) {
                        'h' -> return checkKeyword(2, 2, "is", TokenType.TOKEN_THIS)
                        'r' -> return checkKeyword(2, 2, "ue", TokenType.TOKEN_TRUE)
                    }
                }
                'v' -> return checkKeyword(1, 2, "ar", TokenType.TOKEN_VAR)
                'w' -> return checkKeyword(1, 4, "hile", TokenType.TOKEN_WHILE)
            }

//< keywords
            return TokenType.TOKEN_IDENTIFIER
        }

        private fun identifier(): Token {
            while ((isAlpha(peek()) || isDigit(peek()))) advance()
            return makeToken(identifierType())
        }

        private fun number(): Token {
            while (isDigit(peek())) advance()

            // Look for a fractional part.
            if (peek() == '.' && isDigit(peekNext())) {
                // Consume the ".".
                advance()
                while (isDigit(peek())) advance()
            }
            return makeToken(TokenType.TOKEN_NUMBER)
        }

        private fun string(): Token {
            while (peek() != '"' && !isAtEnd()) {
                if (peek() == '\n') line++
                advance()
            }
            if (isAtEnd()) return errorToken("Unterminated string.")

            // The closing quote.
            advance()
            return makeToken(TokenType.TOKEN_STRING)
        }

        fun scanToken(): Token {
//> call-skip-whitespace
            skipWhitespace()
            //< call-skip-whitespace
            _start = _current
            if (isAtEnd()) return makeToken(TokenType.TOKEN_EOF)
            //> scan-char
            val c = advance()
            //> scan-identifier
            if (isAlpha(c)) return identifier()
            //< scan-identifier
//> scan-number
            if (isDigit(c)) return number()
            when (c) {
                '(' -> return makeToken(TokenType.TOKEN_LEFT_PAREN)
                ')' -> return makeToken(TokenType.TOKEN_RIGHT_PAREN)
                '{' -> return makeToken(TokenType.TOKEN_LEFT_BRACE)
                '}' -> return makeToken(TokenType.TOKEN_RIGHT_BRACE)
                ';' -> return makeToken(TokenType.TOKEN_SEMICOLON)
                ',' -> return makeToken(TokenType.TOKEN_COMMA)
                '.' -> return makeToken(TokenType.TOKEN_DOT)
                '-' -> return makeToken(TokenType.TOKEN_MINUS)
                '+' -> return makeToken(TokenType.TOKEN_PLUS)
                '/' -> return makeToken(TokenType.TOKEN_SLASH)
                '*' -> return makeToken(TokenType.TOKEN_STAR)
                '!' -> return makeToken(
                    if (match('=')) TokenType.TOKEN_BANG_EQUAL else TokenType.TOKEN_BANG
                )
                '=' -> return makeToken(
                    if (match('=')) TokenType.TOKEN_EQUAL_EQUAL else TokenType.TOKEN_EQUAL
                )
                '<' -> return makeToken(
                    if (match('=')) TokenType.TOKEN_LESS_EQUAL else TokenType.TOKEN_LESS
                )
                '>' -> return makeToken(
                    if (match('=')) TokenType.TOKEN_GREATER_EQUAL else TokenType.TOKEN_GREATER
                )
                '"' -> return string()
            }
            //< scan-char
            return errorToken("Unexpected character.")
        }
    }

    private fun Token.toString(source: CharArray):String {
        val tokenString = source.slice(start..start+length-1).joinToString("")
        return "${type} $tokenString"
    }

    @Test
    fun identifiersTest() {
        /*
        andy formless fo _ _123 _abc ab123
abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_

// expect: IDENTIFIER andy null
// expect: IDENTIFIER formless null
// expect: IDENTIFIER fo null
// expect: IDENTIFIER _ null
// expect: IDENTIFIER _123 null
// expect: IDENTIFIER _abc null
// expect: IDENTIFIER ab123 null
// expect: IDENTIFIER abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_ null
// expect: EOF  null
         */
        val source = """
                    andy formless fo _ _123 _abc ab123
abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_

        """.trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }

    @Test
    fun keywordsTest() {
        /*
and class else false for fun if nil or return super this true var while

// expect: AND and null
// expect: CLASS class null
// expect: ELSE else null
// expect: FALSE false null
// expect: FOR for null
// expect: FUN fun null
// expect: IF if null
// expect: NIL nil null
// expect: OR or null
// expect: RETURN return null
// expect: SUPER super null
// expect: THIS this null
// expect: TRUE true null
// expect: VAR var null
// expect: WHILE while null
// expect: EOF  null
         */
        val source = """
                    and class else false for fun if nil or return super this true var while

        """.trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }


    @Test
    fun numbersTest() {
        /*
        123
    123.456
    .456
    123.

    // expect: NUMBER 123 123.0
    // expect: NUMBER 123.456 123.456
    // expect: DOT . null
    // expect: NUMBER 456 456.0
    // expect: NUMBER 123 123.0
    // expect: DOT . null
    // expect: EOF  null
         */
        val source = """
            123
            123.456
            .456
            123.

        """.trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }

    @Test
    fun punctuatorsTest() {
/*
(){};,+-*!===<=>=!=<>/.

// expect: LEFT_PAREN ( null
// expect: RIGHT_PAREN ) null
// expect: LEFT_BRACE { null
// expect: RIGHT_BRACE } null
// expect: SEMICOLON ; null
// expect: COMMA , null
// expect: PLUS + null
// expect: MINUS - null
// expect: STAR * null
// expect: BANG_EQUAL != null
// expect: EQUAL_EQUAL == null
// expect: LESS_EQUAL <= null
// expect: GREATER_EQUAL >= null
// expect: BANG_EQUAL != null
// expect: LESS < null
// expect: GREATER > null
// expect: SLASH / null
// expect: DOT . null
// expect: EOF  null
 */
        val source = """
            (){};,+-*!===<=>=!=<>/.

        """.trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }

    @Test
    fun stringsTest() {
        /*
    ""
    "string"

    // expect: STRING ""
    // expect: STRING "string" string
    // expect: EOF  null
         */
        val source = """
            ""
            "string"

        """.trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }

    @Test
    fun whitespaceTest() {
        /*
space    tabs				newlines




end

// expect: IDENTIFIER space null
// expect: IDENTIFIER tabs null
// expect: IDENTIFIER newlines null
// expect: IDENTIFIER end null
// expect: EOF  null
         */
        val source = """
space    tabs				newlines




end
""".trimIndent().toCharArray()
        val scanner = Scanner(source)
        var t = scanner.scanToken()
        while(t.type != TokenType.TOKEN_EOF) {
            t = scanner.scanToken()
            println(t.toString(source))
        }
    }
}