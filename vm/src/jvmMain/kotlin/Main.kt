import com.hiperbou.vm.CPU
import com.hiperbou.vm.compiler.Compiler
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Please give the file to parse as the only argument!")
        exitProcess(-1)
    }
    runProgram(args[0])
}

private fun runProgram(fileName: String) {
    val program = File(fileName).readText(Charsets.UTF_8)
    val instructions = Compiler().generateProgram(program)
    val cpu = CPU(instructions)
    cpu.run()
    println("After running, the cpu stack contains: " + cpu.getStack())
    println("After running, the cpu local frame contains: " + cpu.getCurrentFrame().getVariables())
}
