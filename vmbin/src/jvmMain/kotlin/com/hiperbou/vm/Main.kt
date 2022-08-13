package com.hiperbou.vm

import com.hiperbou.vm.compiler.Compiler
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Please specify a file to compile and run.")
        exitProcess(-1)
    }
    runProgram(args[0])
}

private fun runProgram(fileName: String) {
    val program = File(fileName).readText(Charsets.UTF_8)
    val instructions = Compiler().generateProgram(program)
    val cpu = CPU(instructions)
    cpu.run()
    println("CPU instructionAddress: " + cpu.instructionAddress)
    println("CPU stack: " + cpu.getStack())
    println("CPU frame: " + cpu.getCurrentFrame().getVariables())
    println("CPU globals: " + cpu.getGlobals().getVariables())
}
