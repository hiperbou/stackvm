import com.hiperbou.vm.Instructions.ABS
import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.B_AND
import com.hiperbou.vm.Instructions.B_NOT
import com.hiperbou.vm.Instructions.B_OR
import com.hiperbou.vm.Instructions.B_XOR
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.DIV
import com.hiperbou.vm.Instructions.DUP
import com.hiperbou.vm.Instructions.EQ
import com.hiperbou.vm.Instructions.GLOAD
import com.hiperbou.vm.Instructions.GSTORE
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.LT
import com.hiperbou.vm.Instructions.LTE
import com.hiperbou.vm.Instructions.MAX
import com.hiperbou.vm.Instructions.MIN
import com.hiperbou.vm.Instructions.MOD
import com.hiperbou.vm.Instructions.MUL
import com.hiperbou.vm.Instructions.NE
import com.hiperbou.vm.Instructions.NOP
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.ProgramVisitor
import org.antlr.v4.runtime.ANTLRInputStream
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertFailsWith


class ProgramVisitorTest {
    private fun parseProgram(source: String): IntArray {
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
              NOP
              POP
              DUP
              
              ADD
              SUB
              MUL
              DIV
              MOD
              MIN
              MAX
              
              NOT
              B_NOT
              ABS
              
              AND
              OR
              B_AND
              B_OR
              B_XOR
              
              EQ
              NE
              GTE
              LTE
              GT
              LT
              
              RET
              
              """.trimIndent()
        )
        assertArrayEquals(
            intArrayOf(
                NOP, POP, DUP,
                ADD, SUB, MUL, DIV, MOD, MIN, MAX,
                NOT, B_NOT, ABS,
                AND, OR, B_AND, B_OR, B_XOR,
                EQ, NE, GTE, LTE, GT, LT,
                RET),
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
    fun testGLoadAndGStore() {
        val program = parseProgram(
            """
              GLOAD  100
              GSTORE 101
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(GLOAD, 100, GSTORE, 101), program)
    }

    @Test
    fun testReadAndWrite() {
        val program = parseProgram(
            """
              READ  100
              WRITE 101
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(READ, 100, WRITE, 101), program)
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

    @Test
    fun labelTest() {
        val program = parseProgram(
            """
              label_0:
              PUSH 1
              CALL label_0
              HALT
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(PUSH,1,CALL,0,HALT), program)
    }

    @Ignore
    @Test
    fun negativeNumberTest() {
        val program = parseProgram(
            """
              PUSH -1
              HALT
              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(PUSH,-1,HALT), program)
    }
}