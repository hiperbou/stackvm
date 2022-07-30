package com.hiperbou.vm.compiler.parser

class Token(val type: TokenType, val text: String) {
    override fun toString(): String {
        return "$type $text"
    }
}