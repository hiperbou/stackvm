import com.hiperbou.conversation.compiler.AsmConversationCompiler
import com.hiperbou.vm.CPU
import com.hiperbou.vm.Frame
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.compiler.DebugProgramWriter
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.state.CPUState
import com.hiperbou.vm.state.saveState

class LittleCompiler: AsmConversationCompiler {
    private val compiler = Compiler()

    private val decompiler = ProgramDecompiler()
    private val disassembler = Disassembler()

    private var lineOpcodeMap = mutableMapOf<Int,Int>()

    override fun compile(source: String): IntArray {
        lineOpcodeMap.clear()
        return compiler.generateProgram(source, DebugProgramWriter(
            lineOpcodeMap = lineOpcodeMap
        ))
    }

    fun getDebugInfo() = lineOpcodeMap

    fun disassembly(program: IntArray): String {
        val decompilation = decompiler.decompile(program)
        return disassembler.disassemble(decompilation)
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

    data class ProgramResult (val text:String, val memory:IntArray)

    fun runProgram(program: IntArray):ProgramResult? {
        try {
            val cpu = CPU(program)
            cpu.run()
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun runProgram(program: IntArray, memory: Memory):ProgramResult? {
        try {
            val cpu = CPU(instructions = program, memory = memory)
            cpu.run()
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun stepProgram(program: IntArray): CPU? {
        try {
            return CPU(program)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun stepProgram(program: IntArray, memory: Memory): CPU? {
        try {
            return CPU(instructions = program, memory = memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun step(cpu: CPU):ProgramResult? {
        try {
            if (!cpu.isHalted()) cpu.step()
            return state(cpu)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun state(cpu: CPU):ProgramResult? {
        try {
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }
}