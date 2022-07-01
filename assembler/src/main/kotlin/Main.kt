
import com.hiperbou.vm.CPU
import com.hiperbou.vm.ProgramVisitor
import org.antlr.v4.runtime.ANTLRFileStream

import java.io.IOException
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Please give the file to parse as the only argument!")
        exitProcess(-1)
    }
    runProgram(args[0])
}

@Throws(IOException::class)
private fun runProgram(fileName: String) {
    val generatedProgram: IntArray = ProgramVisitor.generateProgram(ANTLRFileStream(fileName))
    val cpu = CPU(*generatedProgram)
    cpu.run()
    println("After running, the cpu stack contains: " + cpu.getStack())
    println("After running, the cpu local frame contains: " + cpu.getCurrentFrame().getVariables())
}
