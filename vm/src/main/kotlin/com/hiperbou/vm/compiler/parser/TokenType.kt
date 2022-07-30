package com.hiperbou.vm.compiler.parser

enum class TokenType(val punctuator:Char? = null) {
    LEFT_PAREN('('),
    RIGHT_PAREN(')'),
    PLUS('+'),
    MINUS('-'),
    ASTERISK('*'),
    SLASH('/'),
    COLON(':'),
    NUMBER,
    IDENTIFIER,
    LABEL,
    LABEL_WITH_VALUE,
    OPCODE,
    EOL('\n'),
    EOF;
}