package com.hiperbou.vm.asmcompiler

import com.hiperbou.vm.CPU
import com.hiperbou.vm.bytecode.BytecodeFile
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.plugin.print.PrintDecoder
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import java.io.File
import kotlin.system.exitProcess

class AsmTool {

    fun run(options: ToolOptions) {
        try {
            // 1. Read source file
            val sourceText = File(options.inputFile).readText()

            // 2. Compile to bytecode
            val opcodeInfo = OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation())
            val bytecode = Compiler(opcodeInfo).generateProgram(sourceText)

            // 3. Optionally write bytecode to file
            options.outputFile?.let { outPath ->
                BytecodeFile().writeTo(File(outPath), bytecode)
            }

            // 4. Optionally disassemble
            if (options.disassemble) {
                val decompiled = ProgramDecompiler(opcodeInfo).decompile(bytecode)
                val asm = Disassembler().disassemble(decompiled)
                println(asm)
            }

            // 5. Optionally execute
            if (options.run) {
                val cpu = CPU(bytecode)
                cpu.appendDecoder(PrintDecoder(cpu.getStack()))
                cpu.run()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            exitProcess(1)
        }
    }
}
