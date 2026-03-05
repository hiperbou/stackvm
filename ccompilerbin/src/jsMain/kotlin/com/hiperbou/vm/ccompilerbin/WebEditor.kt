package com.hiperbou.vm.ccompilerbin

import ace
import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.Frame
import com.hiperbou.vm.ccompiler.CCompiler
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.plugin.print.PrintInstructions
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import com.hiperbou.vm.state.CPUState
import com.hiperbou.vm.state.saveState
import jsObject
import kotlinx.browser.document
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLOptionElement
import setValue

class WebEditor(private val examples: List<CExample>) {
    init {
        document.addEventListener("DOMContentLoaded", { _ ->
            val sourceEditor = initializeEditor("source")
            val compiledEditor = initializeEditor("compiled", true)
            val resultEditor = initializeEditor("result", true)
            val exampleSelect = document.getElementById("example-select") as? HTMLSelectElement

            fun loadExample(index: Int) {
                val safeIndex = index.coerceIn(0, examples.lastIndex)
                sourceEditor.setValue(examples[safeIndex].source, -1)
                compiledEditor.setValue("", -1)
                resultEditor.setValue("", -1)
            }

            exampleSelect?.let { select ->
                examples.forEachIndexed { index, example ->
                    val option = document.createElement("option") as HTMLOptionElement
                    option.value = index.toString()
                    option.text = example.name
                    select.add(option)
                }
                select.selectedIndex = 0
                select.addEventListener("change", { _ ->
                    loadExample(select.selectedIndex)
                })
            }

            loadExample(0)

            fun buildAndRun(runProgram: Boolean) {
                try {
                    val bytecode = CCompiler.compile(sourceEditor.getValue())
                    val decompiled = ProgramDecompiler(
                        OpcodeInformationChain(
                            CoreOpcodeInformation(),
                            PrintOpcodeInformation()
                        )
                    ).decompile(bytecode)
                    val disassembled = Disassembler().disassemble(decompiled)
                    compiledEditor.setValue(disassembled, -1)

                    if (!runProgram) {
                        resultEditor.setValue("", -1)
                        return
                    }

                    val runResult = run(bytecode)
                    resultEditor.setValue(runResult.format(), -1)
                } catch (e: Throwable) {
                    val message = e.message ?: e.toString()
                    compiledEditor.setValue(message, -1)
                    resultEditor.setValue("", -1)
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

    private data class RunResult(
        val state: CPUState<Int, Frame>,
        val output: List<Int>
    )

    private class CapturingPrintDecoder(
        private val stack: CPUStack<Int>,
        private var nextDecoder: Decoder = ExceptionDecoder.instance
    ) : Decoder {
        val output = mutableListOf<Int>()

        override fun decodeInstruction(instruction: Int) {
            when (instruction) {
                PrintInstructions.PRINT -> {
                    val value = stack.peek()
                    output.add(value)
                    println(value)
                }
                PrintInstructions.DEBUG_PRINT -> {
                    val value = stack.peek()
                    output.add(value)
                    println("<$value>")
                }
                else -> nextDecoder.decodeInstruction(instruction)
            }
        }

        override fun setNextDecoder(decoder: Decoder) {
            nextDecoder = decoder
        }
    }

    private fun run(instructions: IntArray): RunResult {
        val cpu = CPU(instructions)
        val printDecoder = CapturingPrintDecoder(cpu.getStack())
        cpu.appendDecoder(printDecoder)
        cpu.run()
        return RunResult(cpu.saveState(), printDecoder.output)
    }

    private fun RunResult.format(): String {
        return with(StringBuilder()) {
            append("Halted: ")
            append(state.halted)
            append("\ninstructionAddress: ")
            append(state.instructionAddress)
            append("\nstack: ")
            append(state.stack.toString())
            append("\nframe: ")
            append(state.frames.peek())
            append("\nglobals: ")
            append(state.globals)
            append("\noutput: ")
            append(if (output.isEmpty()) "[]" else output.joinToString(prefix = "[", postfix = "]"))
        }.toString()
    }

    private fun initializeEditor(name: String, readonly: Boolean = false) =
        ace.edit(name).apply {
            setReadOnly(readonly)
            setOptions(
                jsObject(
                    "enableBasicAutocompletion" to true,
                    "enableLiveAutocompletion" to true
                )
            )
        }
}
