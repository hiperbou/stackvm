package com.hiperbou.conversation.compiler

import com.hiperbou.vm.compiler.Compiler

fun interface AsmConversationCompiler {
    fun compile(source:String):IntArray
}

class DefaultAsmConversationCompiler:AsmConversationCompiler {
    private val compiler = Compiler()
    override fun compile(source: String): IntArray {
        return compiler.generateProgram(source)
    }
}
