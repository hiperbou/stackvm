package com.hiperbou.vm

import com.hiperbou.vm.compiler.Compiler

fun main() {
    println("KotlinJs VM")
    val program = """
        PUSH 1
        PUSH 2
        ADD
        DUP
        MUL
        HALT
    """.trimMargin()

    val compiler = Compiler()
    val instructions = compiler.generateProgram(program)
    val cpu = CPU(instructions)
    cpu.run()

    println("stack: ${cpu.getStack()}")
}