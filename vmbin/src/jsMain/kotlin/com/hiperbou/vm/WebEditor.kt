package com.hiperbou.vm

import ace
import setValue
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.state.CPUState
import com.hiperbou.vm.state.saveState
import jsObject
import kotlinx.browser.document

class WebEditor(code:String) {
    init {
        document.addEventListener("DOMContentLoaded", { e ->
            val sourcesEditor = initializeEditor("source").apply {
                setValue(code, -1)
            }
            val compiledEditor = initializeEditor("compiled", true)
            val resultEditor = initializeEditor("result", true)

            fun buildAndRun(run:Boolean) {
                try {
                    val program = compile(sourcesEditor.getValue())
                    val instructions = ProgramDecompiler().decompile(program)
                    val disassembled = Disassembler().disassemble(instructions)
                    compiledEditor.setValue(disassembled, -1)

                    if (!run) return

                    val state = run(program)
                    resultEditor.setValue(state.format(), -1)
                } catch (e:Exception) {
                    compiledEditor.setValue(e.message!!)
                    resultEditor.setValue("")
                }
            }

            document.getElementById("compile")?.addEventListener("click", { _ ->
                buildAndRun(false)
            })

            document.getElementById("run")?.addEventListener("click", { _ ->
                buildAndRun(true)
            })
        })
    }

    private fun compile(program:String):IntArray {
        val compiler = Compiler()
        return compiler.generateProgram(program)
    }

    private fun run(instructions:IntArray):CPUState<Int, Frame> {
        val cpu = CPU(instructions)
        cpu.run()
        return cpu.saveState()
    }

    private fun CPUState<Int, Frame>.format():String {
        return with(StringBuilder()){
            append("Halted: ")
            append(halted)
            append("\ninstructionAddress: ")
            append(instructionAddress)
            append("\nstack: ")
            append(stack.toString())
            append("\nframe: ")
            append(frames.peek())
            append("\nglobals: ")
            append(globals)
        }.toString()
    }

    private fun initializeEditor(name:String, readonly:Boolean = false) =
        ace.edit(name).apply {
            setReadOnly(readonly)
            //setTheme("ace/theme/monokai")
            setOptions(jsObject(
                "enableBasicAutocompletion" to true,
                "enableLiveAutocompletion" to true
            ))
            //session.setMode("ace/mode/java")
        }
}