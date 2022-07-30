package com.hiperbou.vm.compiler

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
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.NOP
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.compiler.parser.*
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.plugin.print.PrintInstructions.PRINT
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class CompilerTest {
    private val compiler = Compiler()

    private fun parseProgram(source: String): IntArray {
        //return compiler.generateProgram(source.byteInputStream().reader())
        return compiler.generateProgram(source)
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
    fun testSingleLineCStyleCommentsAreIgnored() {
        val program = parseProgram(
            """
              /* I am a comment!*/
              /*NOP*/ HALT /* Comment inline*/
              /**/
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(HALT), program)
    }

    @Test
    fun testSingleLineCStyleCommentsWithNestedCommentAreIgnored() {
        val program = parseProgram(
            """
              /* I am //a comment!*/
              HALT
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

    @Test
    fun testImmediateValueLabel() {
        val program = parseProgram("""
            keyboard: 32
            PUSH keyboard
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testAssignImmediateValueLabel() {
        val program = parseProgram("""
            memory: 16
            reserved: memory
            PUSH reserved
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,16), program)
    }

    @Test
    fun testArithmeticValueLabel() {
        val program = parseProgram("""
            value: 16 + 16
            PUSH value
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testPrecedenceArithmetic() {
        val program = parseProgram("""
            value: 16 + 16 * 16 
            PUSH value
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,272), program)
    }

    @Test
    fun testPrecedenceArithmetic2() {
        val program = parseProgram("""
            value: (16 + 16) * 16 
            PUSH value
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,512), program)
    }

    @Test
    fun testArithmeticImmediateValue() {
        val program = parseProgram("""
            memory: 8
            keyboard: memory + 8
            PUSH keyboard
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,16), program)
    }

    @Test
    fun testArithmeticImmediateValueAlphanumericLabel() {
        val program = parseProgram("""
            memory8: 8
            keyboard: memory8 + 8
            PUSH keyboard
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,16), program)
    }

    @Test
    fun testArithmeticImmediateValueProgram2() {
        val program = parseProgram("""
            memory: 8
            reserved: memory
            keyboard: memory + 8
            PUSH reserved
            PUSH keyboard
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,8,PUSH,16), program)
    }

    @Test
    fun testPrecedenceArithmeticImmediateValuesProgram() {
        val program = parseProgram("""
            a: 8
            b: 16
            c: 32
            result: (a + b) * c
            PUSH result
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,768), program)
    }

    @Test
    fun testPrecedenceArithmeticImmediateValuesProgramAsExpression() {
        val program = parseProgram("""
            a: 8
            b: 16
            c: 32
            PUSH (a + b) * c
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,768), program)
    }

    @Test
    fun testArithmetic() {
        val program = parseProgram("""
            PUSH 16 + 16
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testArithmeticNoWhitespaces() {
        val program = parseProgram("""
            PUSH 16+16
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testArithmeticMinus() {
        val program = parseProgram("""
            PUSH 16 - 8
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,8), program)
    }

    @Test
    fun testArithmeticMultiply() {
        val program = parseProgram("""
            PUSH 8 * 8
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,64), program)
    }

    @Test
    fun testArithmeticDivision() {
        val program = parseProgram("""
            PUSH 64 / 16
        """.trimIndent())
        assertArrayEquals(intArrayOf(PUSH,4), program)
    }

    @Test
    fun testUnknownOperatorException() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
            PUSH 2 ^ 2
        """.trimIndent()
            )
            assertArrayEquals(intArrayOf(PUSH, 2), program)
        }
    }

    @Test
    fun testDuplicatedLabelException() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
                a: 1
                PUSH a
                a: 2
        """.trimIndent()
            )
            assertArrayEquals(intArrayOf(PUSH, 2), program)
        }
    }

    @Test
    fun testCyclicLabelException() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
                a: a
                PUSH a
        """.trimIndent()
            )
            assertArrayEquals(intArrayOf(PUSH, 2), program)
        }
    }

    @Test
    fun noNewLineAtEOFTest() {
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

    @Test
    fun compilerPluginFailTest() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
              label_0:
              PUSH 1
              CALL label_0
              PRINT
              HALT
              """.trimIndent()
            )
            assertArrayEquals(intArrayOf(PUSH, 1, CALL, 0, PRINT, HALT), program)
        }
    }

    @Test
    fun compilerPluginTest() {
        val opcodeInformation = OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation())
        val compiler = Compiler(opcodeInformation)
        fun parseProgram(source: String): IntArray {
            return compiler.generateProgram(source)
        }
        val program = parseProgram(
            """
              label_0:
              PUSH 1
              CALL label_0
              PRINT
              HALT
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(PUSH,1,CALL,0,PRINT,HALT), program)
    }

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

    @Test
    fun negativeNumberExpressionTest() {
        val program = parseProgram(
            """
              PUSH - 1              
              """.trimIndent()
        )
        assertArrayEquals(intArrayOf(PUSH,-1), program)
    }


    @Test
    fun newParserTest() {
        //val source = """HALT"""
        //val source = """PUSH 1"""
        //val source = """PUSH 1+3"""
        //val source = "start:"
        //val source = """start: 10"""
        /*val source = """
        HALT
         """*/
        val source = """
        memory: 128    
        PUSH memory + memory
         """
        /*val source = """
        memory: 128
        //memory: 32
        PUSH memory + memory
         """*/
        /*val source = """
        memory: 128
        /*memory: 32*/
        PUSH memory + memory
         """*/
        /*val source = """
        memory: 128
        /*memory: 32
            memory: 64
        */
        PUSH memory + memory
         """*/
        /*val source = """
        memory: 128
        /*memory: 32
            memory: 64
        
        PUSH memory + memory*/"""*/
        val programWriter = DefaultProgramWriter()
        val lexer = Lexer(source)

        val parser: Parser = AsmProgramParser(lexer, CoreOpcodeInformation(), programWriter)

        try {
            val result = parser.parse()
            val builder = StringBuilder()
            result.forEach {
                it.print(builder)
            }
            val actual = builder.toString()
            println(actual)
            result.forEach {
                println("Solve: " + it.solveExpression())
                it.compileExpression(programWriter)
            }
            println(programWriter.program)
        } catch (ex: ParseException) {
            println(ex)
            //throw InvalidProgramException("Error parsing expression in line: $currentLine\n'$inputLine'\n" + ex.message)
        }
    }
}