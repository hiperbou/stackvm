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
import com.hiperbou.vm.Instructions.GLOADI
import com.hiperbou.vm.Instructions.GSTORE
import com.hiperbou.vm.Instructions.GSTOREI
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.LOADI
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
import com.hiperbou.vm.Instructions.STOREI
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.NEG
import com.hiperbou.vm.Instructions.NOP
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.READI
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.Instructions.WRITEI
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.compiler.parser.*
import com.hiperbou.vm.decompiler.CoreOpcodeInformation
import com.hiperbou.vm.decompiler.OpcodeInformationChain
import com.hiperbou.vm.plugin.print.PrintInstructions.PRINT
import com.hiperbou.vm.plugin.print.PrintOpcodeInformation
import com.hiperbou.vm.plugin.bitwise.BitwiseOpcodeInformation
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.SHL
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.SHR
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.USHR
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.ROL
import com.hiperbou.vm.plugin.bitwise.BitwiseInstructions.ROR
import com.hiperbou.vm.plugin.struct.StructuredDataOpcodeInformation
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.ARRAY_ADDR
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.FIELD_ADDR
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.MEMCPY
import com.hiperbou.vm.plugin.struct.StructuredDataInstructions.MEMSET

import kotlin.test.*

class CompilerTest {
    private val compiler = Compiler()

    private fun parseProgram(source: String): IntArray {
        return compiler.generateProgram(source)
    }
    @Test
    fun testTrivialProgram() {
        val program = parseProgram("HALT\n")
        assertContentEquals(intArrayOf(HALT), program)
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
              NEG
              
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
        assertContentEquals(
            intArrayOf(
                NOP, POP, DUP,
                ADD, SUB, MUL, DIV, MOD, MIN, MAX,
                NOT, B_NOT, ABS, NEG,
                AND, OR, B_AND, B_OR, B_XOR,
                EQ, NE, GTE, LTE, GT, LT,
                RET),
            program
        )
    }

    @Test
    fun testPushWithArgument() {
        val program = parseProgram("PUSH   123\n")
        assertContentEquals(intArrayOf(PUSH, 123), program)
    }

    @Test
    fun testLoadAndStore() {
        val program = parseProgram(
            """
              LOAD  100
              STORE 101
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(LOAD, 100, STORE, 101), program)
    }

    @Test
    fun testGLoadAndGStore() {
        val program = parseProgram(
            """
              GLOAD  100
              GSTORE 101
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(GLOAD, 100, GSTORE, 101), program)
    }

    @Test
    fun testReadAndWrite() {
        val program = parseProgram(
            """
              READ  100
              WRITE 101
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(READ, 100, WRITE, 101), program)
    }


    @Test
    fun testLoadAndStoreIndirect() {
        val program = parseProgram(
            """  
              LOADI
              STOREI
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(LOADI, STOREI), program)
    }

    @Test
    fun testGLoadAndGStoreIndirect() {
        val program = parseProgram(
            """
              GLOADI
              GSTOREI
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(GLOADI, GSTOREI), program)
    }

    @Test
    fun testReadAndWriteIndirect() {
        val program = parseProgram(
            """
              READI
              WRITEI
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(READI, WRITEI), program)
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
        assertContentEquals(intArrayOf(JMP, 3, HALT, PUSH, 42), program)
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
        assertContentEquals(intArrayOf(JIF, 5, CALL, 5, HALT, PUSH, 43), program)
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
        assertContentEquals(intArrayOf(HALT), program)
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
        assertContentEquals(intArrayOf(HALT), program)
    }

    @Test
    fun testSingleLineCStyleCommentsWithNestedCommentAreIgnored() {
        val program = parseProgram(
            """
              /* I am //a comment!*/
              HALT
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(HALT), program)
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
        assertContentEquals(intArrayOf(PUSH,1,CALL,0,HALT), program)
    }

    @Test
    fun testImmediateValueLabel() {
        val program = parseProgram("""
            keyboard: 32
            PUSH keyboard
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testAssignImmediateValueLabel() {
        val program = parseProgram("""
            memory: 16
            reserved: memory
            PUSH reserved
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,16), program)
    }

    @Test
    fun testArithmeticValueLabel() {
        val program = parseProgram("""
            value: 16 + 16
            PUSH value
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testPrecedenceArithmetic() {
        val program = parseProgram("""
            value: 16 + 16 * 16 
            PUSH value
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,272), program)
    }

    @Test
    fun testPrecedenceArithmetic2() {
        val program = parseProgram("""
            value: (16 + 16) * 16 
            PUSH value
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,512), program)
    }

    @Test
    fun testArithmeticImmediateValue() {
        val program = parseProgram("""
            memory: 8
            keyboard: memory + 8
            PUSH keyboard
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,16), program)
    }

    @Test
    fun testArithmeticImmediateValueAlphanumericLabel() {
        val program = parseProgram("""
            memory8: 8
            keyboard: memory8 + 8
            PUSH keyboard
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,16), program)
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
        assertContentEquals(intArrayOf(PUSH,8,PUSH,16), program)
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
        assertContentEquals(intArrayOf(PUSH,768), program)
    }

    @Test
    fun testPrecedenceArithmeticImmediateValuesProgramAsExpression() {
        val program = parseProgram("""
            a: 8
            b: 16
            c: 32
            PUSH (a + b) * c
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,768), program)
    }

    @Test
    fun testArithmetic() {
        val program = parseProgram("""
            PUSH 16 + 16
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testArithmeticNoWhitespaces() {
        val program = parseProgram("""
            PUSH 16+16
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,32), program)
    }

    @Test
    fun testArithmeticMinus() {
        val program = parseProgram("""
            PUSH 16 - 8
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,8), program)
    }

    @Test
    fun testArithmeticMultiply() {
        val program = parseProgram("""
            PUSH 8 * 8
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,64), program)
    }

    @Test
    fun testArithmeticDivision() {
        val program = parseProgram("""
            PUSH 64 / 16
        """.trimIndent())
        assertContentEquals(intArrayOf(PUSH,4), program)
    }

    @Test
    fun testUnknownOperatorException() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
            PUSH 2 ^ 2
        """.trimIndent()
            )
            assertContentEquals(intArrayOf(PUSH, 2), program)
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
            assertContentEquals(intArrayOf(PUSH, 2), program)
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
            assertContentEquals(intArrayOf(PUSH, 2), program)
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
        assertContentEquals(intArrayOf(PUSH,1,CALL,0,HALT), program)
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
            assertContentEquals(intArrayOf(PUSH, 1, CALL, 0, PRINT, HALT), program)
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
        assertContentEquals(intArrayOf(PUSH,1,CALL,0,PRINT,HALT), program)
    }

    @Test
    fun negativeNumberTest() {
        val program = parseProgram(
            """
              PUSH -1
              HALT
              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(PUSH,-1,HALT), program)
    }

    @Test
    fun negativeNumberExpressionTest() {
        val program = parseProgram(
            """
              PUSH - 1              
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(PUSH,-1), program)
    }

    @Test
    fun parameterExpectedTest() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """
              PUSH
              PUSH 2
              """.trimIndent()
            )
            assertContentEquals(intArrayOf(PUSH, PUSH, 2), program)
        }
    }

    @Test
    fun parseExceptionTest() {
        assertFailsWith(ParseException::class) {
            val program = parseProgram(
                """
              PUSH (
              PUSH 2
              """.trimIndent()
            )
            assertContentEquals(intArrayOf(PUSH, PUSH, 2), program)
        }
    }

    @Test
    fun parseExceptionTest2() {
        assertFailsWith(ParseException::class) {
            val program = parseProgram(
                """
              PUSH -
              PUSH 2
              """.trimIndent()
            )
            assertContentEquals(intArrayOf(PUSH, PUSH, 2), program)
        }
    }

    @Test
    fun undefinedLabelInExpressionTest() {
        assertFailsWith(InvalidProgramException::class) {
            val program = parseProgram(
                """   
                PUSH 1
                WRITE memoryAddressOptions + 0
                PUSH 1
                WRITE memoryAddressOptions + 1
                PUSH 0
                WRITE memoryAddressOptions + 2
                PUSH 1
                WRITE memoryAddressOptions + 3
                
                memoryAddressOptions: 2             
            """.trimIndent()
            )
            assertContentEquals(
                intArrayOf(PUSH, 1, WRITE, 2, PUSH, 1, WRITE, 3, PUSH, 0, WRITE, 4, PUSH, 1, WRITE, 5),
                program
            )
        }
    }

    @Test
    fun compilerBitwisePluginTest() {
        val opcodeInformation = OpcodeInformationChain(CoreOpcodeInformation(), BitwiseOpcodeInformation())
        val compiler = Compiler(opcodeInformation)
        fun parseProgram(source: String): IntArray {
            return compiler.generateProgram(source)
        }
        val program = parseProgram(
            """
              SHL
              SHR
              USHR
              ROL
              ROR
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(SHL, SHR, USHR, ROL, ROR), program)
    }

    @Test
    fun compilerStructuredDataPluginTest() {
        val opcodeInformation = OpcodeInformationChain(CoreOpcodeInformation(), StructuredDataOpcodeInformation())
        val compiler = Compiler(opcodeInformation)
        fun parseProgram(source: String): IntArray {
            return compiler.generateProgram(source)
        }
        val program = parseProgram(
            """
              ARRAY_ADDR
              FIELD_ADDR
              MEMCPY
              MEMSET
              """.trimIndent()
        )
        assertContentEquals(intArrayOf(ARRAY_ADDR, FIELD_ADDR, MEMCPY, MEMSET), program)
    }
}