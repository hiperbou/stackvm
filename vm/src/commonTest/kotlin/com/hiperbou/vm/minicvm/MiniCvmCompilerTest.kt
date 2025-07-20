package com.hiperbou.vm.minicvm

import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.InvalidProgramException

import kotlin.test.*

class MiniCvmCompilerTest {

    private val compiler = MiniCvmCompiler()

    // --- Utility Assertion Functions (Adapted from CPUAssertions.kt and CompleteProgramsTest.kt) ---

    private fun assertProgramRunsToHaltAndInstructionAddressIs(cpu: CPU, expectedAddress: Int, message: String? = null) {
        try {
            cpu.run()
        } catch (e: Exception) {
            fail("Program failed to run to HALT: ${e.message}", e)
        }
        assertTrue(cpu.isHalted(), "CPU should be halted. ${message ?: ""}")
        assertEquals(expectedAddress, cpu.instructionAddress, "Instruction address after HALT is not as expected. ${message ?: ""}")
    }
    
    private fun assertProgramResultIs(source: String, expectedValue: Int, message: String? = null) {
        val bytecode = compiler.compile(source)
        val cpu = CPU(bytecode)
        cpu.appendDecoder(com.hiperbou.vm.plugin.print.PrintDecoder(cpu.getStack())) // Add PrintDecoder
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, bytecode.size) 
        assertEquals(1, cpu.getStack().size, "Stack should contain one value (the result). ${message ?: ""}")
        assertEquals(expectedValue, cpu.getStack().pop(), "Program result on top of stack is not as expected. ${message ?: ""}")
    }

    private fun assertStackIsEmpty(cpu: CPU, message: String? = null) {
        assertEquals(0, cpu.getStack().pop(), "Stack should be empty. ${message ?: ""}")
    }
    
    private fun assertProgramRunsToHalt(source: String, message: String? = null): CPU {
        val bytecode = compiler.compile(source)
        val cpu = CPU(bytecode)
        cpu.appendDecoder(com.hiperbou.vm.plugin.print.PrintDecoder(cpu.getStack())) // Add PrintDecoder
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, bytecode.size, message)
        return cpu
    }


    // --- Test Cases ---

    @Test
    fun testVariableDeclarationAndReturn() {
        val source = """
            int main() {
                int x = 42;
                return x;
            }
        """
        assertProgramResultIs(source, 42, "Test Variable Declaration and Return Failed")
    }

    // ... (keep existing tests for arithmetic, functions, conditionals, loops, arrays, logical ops, scope, etc.) ...
    // For brevity, I'm omitting the full list of existing tests, but they should be retained.

    @Test
    fun testFactorialIterative() {
        val source = """
            int main() {
                int n = 5;
                int result = 1;
                int i = 1;
                while (i <= n) {
                    result = result * i;
                    i = i + 1;
                }
                return result; // 120
            }
        """
        assertProgramResultIs(source, 120, "Test Factorial (Iterative) Failed")
    }
    
    // --- Increment/Decrement Operator Tests ---
    @Test
    fun testLocalPrefixIncrement_ExpressionValue() {
        val source = "int main() { int x = 5; return ++x; }" // x becomes 6, returns 6
        assertProgramResultIs(source, 6, "Local Prefix Increment Expression Value Failed")
    }

    @Test
    fun testLocalPrefixIncrement_VariableValue() {
        val source = "int main() { int x = 5; ++x; return x; }" // x becomes 6, returns 6
        assertProgramResultIs(source, 6, "Local Prefix Increment Variable Value Failed")
    }

    @Test
    fun testLocalPostfixIncrement_ExpressionValue() {
        val source = "int main() { int x = 5; int y = x++; return y; }" // y becomes 5, x becomes 6
        assertProgramResultIs(source, 5, "Local Postfix Increment Expression Value Failed")
    }
    
    @Test
    fun testLocalPostfixIncrement_VariableValue_AfterExpression() {
         val source = "int main() { int x = 5; int y = x++; return x; }" // y gets 5, x becomes 6. Return x (6).
        assertProgramResultIs(source, 6, "Local Postfix Increment Variable Value (after expr) Failed")
    }


    @Test
    fun testLocalPostfixIncrement_VariableValue() {
        val source = "int main() { int x = 5; x++; return x; }" // x becomes 6
        assertProgramResultIs(source, 6, "Local Postfix Increment Variable Value Failed")
    }

    @Test
    fun testLocalPrefixDecrement_ExpressionValue() {
        val source = "int main() { int x = 5; return --x; }" // x becomes 4, returns 4
        assertProgramResultIs(source, 4, "Local Prefix Decrement Expression Value Failed")
    }

    @Test
    fun testLocalPrefixDecrement_VariableValue() {
        val source = "int main() { int x = 5; --x; return x; }" // x becomes 4
        assertProgramResultIs(source, 4, "Local Prefix Decrement Variable Value Failed")
    }

    @Test
    fun testLocalPostfixDecrement_ExpressionValue() {
        val source = "int main() { int x = 5; int y = x--; return y; }" // y becomes 5, x becomes 4
        assertProgramResultIs(source, 5, "Local Postfix Decrement Expression Value Failed")
    }
    
    @Test
    fun testLocalPostfixDecrement_VariableValue_AfterExpression() {
        val source = "int main() { int x = 5; int y = x--; return x; }" // y gets 5, x becomes 4. Return x (4).
        assertProgramResultIs(source, 4, "Local Postfix Decrement Variable Value (after expr) Failed")
    }

    @Test
    fun testLocalPostfixDecrement_VariableValue() {
        val source = "int main() { int x = 5; x--; return x; }" // x becomes 4
        assertProgramResultIs(source, 4, "Local Postfix Decrement Variable Value Failed")
    }

    // --- Global Simple Variables ---
    @Test
    fun testGlobalPrefixIncrement_ExpressionValue() {
        val source = "int g = 10; int main() { return ++g; }" // g becomes 11, returns 11
        assertProgramResultIs(source, 11, "Global Prefix Increment Expression Value Failed")
    }

    @Test
    fun testGlobalPrefixIncrement_VariableValue() {
        val source = "int g = 10; int main() { ++g; return g; }" // g becomes 11
        assertProgramResultIs(source, 11, "Global Prefix Increment Variable Value Failed")
    }

    @Test
    fun testGlobalPostfixIncrement_ExpressionValue() {
        val source = "int g = 10; int main() { int y = g++; return y; }" // y gets 10, g becomes 11
        assertProgramResultIs(source, 10, "Global Postfix Increment Expression Value Failed")
    }
    
    @Test
    fun testGlobalPostfixIncrement_VariableValue_AfterExpression() {
        val source = "int g = 10; int main() { int y = g++; return g; }" // y gets 10, g becomes 11. Return g (11).
        assertProgramResultIs(source, 11, "Global Postfix Increment Variable Value (after expr) Failed")
    }

    @Test
    fun testGlobalPostfixIncrement_VariableValue() {
        val source = "int g = 10; int main() { g++; return g; }" // g becomes 11
        assertProgramResultIs(source, 11, "Global Postfix Increment Variable Value Failed")
    }
    
    @Test
    fun testGlobalPrefixDecrement_ExpressionValue() {
        val source = "int g = 10; int main() { return --g; }" // g becomes 9, returns 9
        assertProgramResultIs(source, 9, "Global Prefix Decrement Expression Value Failed")
    }

    @Test
    fun testGlobalPrefixDecrement_VariableValue() {
        val source = "int g = 10; int main() { --g; return g; }" // g becomes 9
        assertProgramResultIs(source, 9, "Global Prefix Decrement Variable Value Failed")
    }

    @Test
    fun testGlobalPostfixDecrement_ExpressionValue() {
        val source = "int g = 10; int main() { int y = g--; return y; }" // y gets 10, g becomes 9
        assertProgramResultIs(source, 10, "Global Postfix Decrement Expression Value Failed")
    }

    @Test
    fun testGlobalPostfixDecrement_VariableValue_AfterExpression() {
        val source = "int g = 10; int main() { int y = g--; return g; }" // y gets 10, g becomes 9. Return g (9).
        assertProgramResultIs(source, 9, "Global Postfix Decrement Variable Value (after expr) Failed")
    }
    
    @Test
    fun testGlobalPostfixDecrement_VariableValue() {
        val source = "int g = 10; int main() { g--; return g; }" // g becomes 9
        assertProgramResultIs(source, 9, "Global Postfix Decrement Variable Value Failed")
    }

    // --- Local Array Elements ---
    @Test
    fun testLocalArrayPrefixIncrement_ExpressionValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; return ++arr[0]; }" // arr[0] becomes 6, returns 6
        assertProgramResultIs(source, 6, "Local Array Prefix Increment Expression Value Failed")
    }
    
    @Test
    fun testLocalArrayPrefixIncrement_ElementValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; ++arr[0]; return arr[0]; }" // arr[0] becomes 6
        assertProgramResultIs(source, 6, "Local Array Prefix Increment Element Value Failed")
    }

    @Test
    fun testLocalArrayPostfixIncrement_ExpressionValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; int y = arr[0]++; return y; }" // y gets 5, arr[0] becomes 6
        assertProgramResultIs(source, 5, "Local Array Postfix Increment Expression Value Failed")
    }
    
    @Test
    fun testLocalArrayPostfixIncrement_ElementValue_AfterExpression() {
        val source = "int main() { int arr[1]; arr[0] = 5; int y = arr[0]++; return arr[0]; }" // y gets 5, arr[0] becomes 6. Return arr[0] (6).
        assertProgramResultIs(source, 6, "Local Array Postfix Increment Element Value (after expr) Failed")
    }

    @Test
    fun testLocalArrayPostfixIncrement_ElementValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; arr[0]++; return arr[0]; }" // arr[0] becomes 6
        assertProgramResultIs(source, 6, "Local Array Postfix Increment Element Value Failed")
    }

    @Test
    fun testLocalArrayPrefixDecrement_ExpressionValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; return --arr[0]; }"
        assertProgramResultIs(source, 4, "Local Array Prefix Decrement Expression Value Failed")
    }

    @Test
    fun testLocalArrayPostfixDecrement_ElementValue() {
        val source = "int main() { int arr[1]; arr[0] = 5; arr[0]--; return arr[0]; }"
        assertProgramResultIs(source, 4, "Local Array Postfix Decrement Element Value Failed")
    }


    // --- Global Array Elements ---
     @Test
    fun testGlobalArrayPrefixIncrement_ExpressionValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; return ++gArr[0]; }" // gArr[0] becomes 21, returns 21
        assertProgramResultIs(source, 21, "Global Array Prefix Increment Expression Value Failed")
    }
    
    @Test
    fun testGlobalArrayPrefixIncrement_ElementValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; ++gArr[0]; return gArr[0]; }" // gArr[0] becomes 21
        assertProgramResultIs(source, 21, "Global Array Prefix Increment Element Value Failed")
    }

    @Test
    fun testGlobalArrayPostfixIncrement_ExpressionValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; int y = gArr[0]++; return y; }" // y gets 20, gArr[0] becomes 21
        assertProgramResultIs(source, 20, "Global Array Postfix Increment Expression Value Failed")
    }
    
    @Test
    fun testGlobalArrayPostfixIncrement_ElementValue_AfterExpression() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; int y = gArr[0]++; return gArr[0]; }" // y gets 20, gArr[0] becomes 21. Return gArr[0] (21).
        assertProgramResultIs(source, 21, "Global Array Postfix Increment Element Value (after expr) Failed")
    }

    @Test
    fun testGlobalArrayPostfixIncrement_ElementValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; gArr[0]++; return gArr[0]; }" // gArr[0] becomes 21
        assertProgramResultIs(source, 21, "Global Array Postfix Increment Element Value Failed")
    }

    @Test
    fun testGlobalArrayPrefixDecrement_ExpressionValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; return --gArr[0]; }"
        assertProgramResultIs(source, 19, "Global Array Prefix Decrement Expression Value Failed")
    }

    @Test
    fun testGlobalArrayPostfixDecrement_ElementValue() {
        val source = "int gArr[1]; int main() { gArr[0] = 20; gArr[0]--; return gArr[0]; }"
        assertProgramResultIs(source, 19, "Global Array Postfix Decrement Element Value Failed")
    }

    // --- Usage as Standalone Statements ---
    @Test
    fun testStandaloneLocalVar_PrefixIncrement() {
        val source = "int main() { int x = 7; ++x; return x; }"
        assertProgramResultIs(source, 8, "Standalone Local Var Prefix Increment Failed")
    }
    @Test
    fun testStandaloneLocalVar_PostfixIncrement() {
        val source = "int main() { int x = 7; x++; return x; }"
        assertProgramResultIs(source, 8, "Standalone Local Var Postfix Increment Failed")
    }
    @Test
    fun testStandaloneGlobalVar_PrefixDecrement() {
        val source = "int g = 15; int main() { --g; return g; }"
        assertProgramResultIs(source, 14, "Standalone Global Var Prefix Decrement Failed")
    }
    @Test
    fun testStandaloneGlobalVar_PostfixDecrement() {
        val source = "int g = 15; int main() { g--; return g; }"
        assertProgramResultIs(source, 14, "Standalone Global Var Postfix Decrement Failed")
    }
    @Test
    fun testStandaloneLocalArray_PrefixIncrement() {
        val source = "int main() { int a[1]; a[0] = 3; ++a[0]; return a[0]; }"
        assertProgramResultIs(source, 4, "Standalone Local Array Prefix Increment Failed")
    }
     @Test
    fun testStandaloneGlobalArray_PostfixDecrement() {
        val source = "int ga[1]; int main() { ga[0] = 7; ga[0]--; return ga[0]; }"
        assertProgramResultIs(source, 6, "Standalone Global Array Postfix Decrement Failed")
    }

    // --- Complex Expressions ---
    @Test
    fun testComplexExpression_SimpleIncDec() {
        val source = """
            int main() {
                int a = 5;
                int b = 10;
                // ++a (a=6, expr=6), b++ (b=11, expr=10)
                return ++a + b++; // 6 + 10 = 16
            }
        """
        assertProgramResultIs(source, 16, "Complex Expression (Simple Inc/Dec) Failed")
    }
    
    @Test
    fun testComplexExpression_VerifySideEffects_IncDec() {
        val source = """
            int main() {
                int a = 5;
                int b = 10;
                int result = ++a + b++; // a=6, b=11, result=16
                return a * 1000 + b * 100 + result; // 6000 + 1100 + 16 = 7116
            }
        """
        assertProgramResultIs(source, 7116, "Complex Expression (Verify Side Effects Inc/Dec) Failed")
    }

    @Test
    fun testComplexExpression_WithArrayIndex() {
        val source = """
            int main() {
                int arr[3];
                arr[0] = 1; arr[1] = 2; arr[2] = 3;
                int i = 0;
                // ++arr[0] (arr[0]=2, expr=2)
                // arr[i++] (i=1, expr=arr[0]=2)
                // arr[1] (arr[1]=2)
                return ++arr[0] + arr[i++] + arr[1]; // Expected: 2 + 1 + 2 (after i++ used for arr[0]) = 5. arr[0] is 2. i is 1.
                                                    // Corrected: ++arr[0] makes arr[0]=2, expr=2.
                                                    // i++ makes i=1, expr uses old i=0. So arr[i++] is arr[0] which is now 2.
                                                    // arr[1] is 2.
                                                    // So, 2 + 2 + 2 = 6.
            }
        """
        assertProgramResultIs(source, 6, "Complex Expression with Array Index Failed")
    }
    
    // --- Parser Error Tests for Inc/Dec ---
    @Test
    fun testIncrementNonLValue_Literal_ParserError() {
        val source = "int main() { return ++5; }"
        assertFailsWith<com.hiperbou.vm.minicvm.parser.ParserException> {
            compiler.compile(source)
        }
    }

    @Test
    fun testPostfixIncrementNonLValue_Literal_ParserError() {
        val source = "int main() { return 5++; }"
        assertFailsWith<com.hiperbou.vm.minicvm.parser.ParserException> {
            compiler.compile(source)
        }
    }
    
    @Test
    fun testIncrementNonLValue_Expression_ParserError() {
        val source = "int main() { int x=1; int y=2; return ++(x+y); }"
        assertFailsWith<com.hiperbou.vm.minicvm.parser.ParserException> {
            compiler.compile(source)
        }
    }

    // --- Bitwise Operator Tests ---
    // ... (keep existing bitwise tests) ...

    // --- Array Operation Tests ---
    // ... (keep existing array tests) ...

    // --- Logical Operator Tests ---
    // ... (keep existing logical op tests) ...

    // --- Scope Tests ---
    // ... (keep existing scope tests) ...

    // --- End-to-End Small Programs ---
    // ... (keep existing end-to-end tests, e.g. Factorial, Fibonacci, Comparisons) ...
    
    // --- Optional: Error Handling Tests (Example) ---
    // ... (keep existing error handling tests) ...

    // --- Built-in Function Tests ---
    @Test
    fun testPrintLiteral() {
        val source = "int main() { print(123); return 0; }"
        assertProgramResultIs(source, 0, "Test print(literal) Failed")
    }

    @Test
    fun testPrintVariable() {
        val source = "int main() { int x = 456; print(x); return x; }"
        assertProgramResultIs(source, 456, "Test print(variable) Failed")
    }

    @Test
    fun testPrintExpression() {
        val source = "int main() { print(10 + 20); return 30; }"
        assertProgramResultIs(source, 30, "Test print(expression) Failed")
    }

    @Test
    fun testDebugPrintLiteral() {
        val source = "int main() { debug_print(789); return 0; }"
        assertProgramResultIs(source, 0, "Test debug_print(literal) Failed")
    }

    @Test
    fun testDebugPrintVariable() {
        val source = "int main() { int y = 101; debug_print(y); return y; }"
        assertProgramResultIs(source, 101, "Test debug_print(variable) Failed")
    }

    @Test
    fun testPrintArgumentCountError_ZeroArgs() {
        val source = "int main() { print(); return 0; }"
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail: 'print' with 0 arguments"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testPrintArgumentCountError_TwoArgs() {
        val source = "int main() { print(1, 2); return 0; }"
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail: 'print' with 2 arguments"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testDebugPrintArgumentCountError_ZeroArgs() {
        val source = "int main() { debug_print(); return 0; }"
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail: 'debug_print' with 0 arguments"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testDebugPrintArgumentCountError_TwoArgs() {
        val source = "int main() { debug_print(1, 2); return 0; }"
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail: 'debug_print' with 2 arguments"
        ) {
            compiler.compile(source)
        }
    }
}

