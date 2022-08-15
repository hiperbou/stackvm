package com.hiperbou.vm

import com.hiperbou.vm.compiler.Compiler

fun main() {
    println("KotlinJs VM")
    val program = """
        PUSH 6
        PUSH 4
        CALL max
        PUSH 6
        PUSH 4
        CALL maxOpcode
        HALT
        
        max:
        STORE 1
        STORE 0
        LOAD 0
        LOAD 1
        GTE
        JIF exit
        LOAD 1
        RET
        
        exit:
        LOAD 0
        RET
        
        maxOpcode:
        MAX
        RET
    """.trimIndent()

    val compiler = Compiler()
    val instructions = compiler.generateProgram(program)
    val cpu = CPU(instructions)
    cpu.run()

    println("stack: ${cpu.getStack()}")

    WebEditor(program)
}