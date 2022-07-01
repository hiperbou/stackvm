import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.DIV
import com.hiperbou.vm.Instructions.DUP
import com.hiperbou.vm.Instructions.EQ
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.MUL
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.ProgramVisitor
import org.antlr.v4.runtime.ANTLRInputStream
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith


class ProgramVisitorTest {
    private fun parseProgram(source: String): IntArray? {
        return ProgramVisitor.generateProgram(ANTLRInputStream(source))
    }

    @Test
    fun testTrivialProgram() {
        val program = parseProgram("HALT\n")
        assertArrayEquals(intArrayOf(HALT), program)
    }

    @Test
    @Throws(Exception::class)
    fun testAllSimpleInstructions() {
        val program = parseProgram(
            """
              ADD
              SUB
              MUL
              DIV
              NOT
              AND
              OR
              POP
              DUP
              ISEQ
              ISGE
              ISGT
              RET
              
              """.trimIndent()
        )
        assertArrayEquals(
            intArrayOf(ADD, SUB, MUL, DIV, NOT, AND, OR, POP, DUP, EQ, GTE, GT, RET),
            program
        )
    }

    @Test
    fun testPushWithArgument() {
        val program = parseProgram("PUSH   123\n")
        assertArrayEquals(intArrayOf(PUSH, 123), program)
    }

    @Test
    fun testLoadAndStore() {
        val program = parseProgram(
            """
              LOAD  100
              STORE 101
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(LOAD, 100, STORE, 101), program)
    }

    @Test
    fun testJmpWithLabel() {
        val program = parseProgram(
            """
              JMP afterEnd
              HALT
              afterEnd:
              PUSH 42
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(JMP, 3, HALT, PUSH, 42), program)
    }


    @Test
    fun testJifCallWithLabel() {
        val program = parseProgram(
            """
              JIF aLabel
              CALL anotherLabel
              HALT
              aLabel:
              anotherLabel:
              PUSH 43
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(JIF, 5, CALL, 5, HALT, PUSH, 43), program)
    }

    @Test
    fun testLabelNotFound() {
        assertFailsWith(InvalidProgramException::class) {
            parseProgram("JMP noLabel\n")
        }
    }

    @Test
    fun testCommentsAreIgnored() {
        val program = parseProgram(
            """
              // I am a comment!
              HALT // Comment inline
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(HALT), program)
    }
}