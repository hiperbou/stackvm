package com.hiperbou.vm.compiler.parser

class Token(val type: TokenType, val text: String, val currentLine:Int) {
    override fun toString(): String {
        return "$type $text $currentLine"
    }

    fun withType(type: TokenType) = Token(type, text, currentLine)
}