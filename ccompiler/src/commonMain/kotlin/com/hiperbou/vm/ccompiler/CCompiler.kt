package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.ccompiler.codegen.CodeGenerator
import com.hiperbou.vm.ccompiler.lexer.CLexer
import com.hiperbou.vm.ccompiler.parser.CParser

/**
 * Entry point for the C-like compiler.
 *
 * Compiles C-like source code to VM bytecode ([IntArray]).
 *
 * Usage:
 * ```kotlin
 * val bytecode = CCompiler().compile("""
 *     int main() {
 *         print(42);
 *         return 0;
 *     }
 * """)
 * val cpu = CPU(bytecode)
 * cpu.appendDecoder(PrintDecoder(cpu.getStack()))
 * cpu.run()
 * ```
 */
class CCompiler {

    /**
     * Compile [source] code to VM bytecode.
     *
     * @param source The C-like source code string.
     * @return An [IntArray] of VM instructions ready to be loaded into [com.hiperbou.vm.CPU].
     * @throws com.hiperbou.vm.ccompiler.lexer.LexerException on tokenization errors.
     * @throws com.hiperbou.vm.ccompiler.parser.ParseException on syntax errors.
     * @throws com.hiperbou.vm.ccompiler.codegen.CodeGenException on semantic errors.
     */
    fun compile(source: String): IntArray {
        val tokens = CLexer(source).tokenize()
        val ast = CParser(tokens).parse()
        return CodeGenerator().generate(ast)
    }
}
