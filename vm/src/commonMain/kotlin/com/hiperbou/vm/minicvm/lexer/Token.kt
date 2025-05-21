package com.hiperbou.vm.minicvm.lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int
)
