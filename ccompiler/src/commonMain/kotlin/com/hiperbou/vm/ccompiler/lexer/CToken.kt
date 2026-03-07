package com.hiperbou.vm.ccompiler.lexer

data class CToken(
    val type: CTokenType,
    val text: String,
    val line: Int,
    val column: Int
) {
    override fun toString(): String = "CToken($type, \"$text\", line=$line, col=$column)"
}
