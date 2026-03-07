package com.hiperbou.vm.vminterp

import com.hiperbou.vm.CPU
import com.hiperbou.vm.bytecode.BytecodeFile
import com.hiperbou.vm.bytecode.InvalidBytecodeFileException
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.plugin.print.PrintDecoder
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import java.io.File
import kotlin.system.exitProcess

class InterpTool {

    fun run(options: InterpOptions) {
        try {
            // 1. Read bytecode from file
            val bytecode = BytecodeFile().readFrom(File(options.inputFile))

            val opcodeInfo = OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation())

            if (options.disassemble) {
                // 2a. Disassemble and print
                val decompiled = ProgramDecompiler(opcodeInfo).decompile(bytecode)
                val asm = Disassembler().disassemble(decompiled)
                println(asm)
            } else {
                // 2b. Execute
                val cpu = CPU(bytecode)
                cpu.appendDecoder(PrintDecoder(cpu.getStack()))
                cpu.run()
            }
        } catch (e: InvalidBytecodeFileException) {
            System.err.println("Error: Invalid bytecode file — ${e.message}")
            exitProcess(1)
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            exitProcess(1)
        }
    }
}
