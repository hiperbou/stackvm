package com.hiperbou.vm.minicvm

import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.OutOfMemoryException
import com.hiperbou.vm.ProgramTooLargeException
import kotlin.test.*

class MiniCvmCompilerTest {

    private val compiler = MiniCvmCompiler()

    // --- Utility Assertion Functions (Adapted from CPUAssertions.kt and CompleteProgramsTest.kt) ---

    private fun assertProgramRunsToHaltAndInstructionAddressIs(cpu: CPU, expectedAddress: Int, message: String? = null) {
        try {
            cpu.run()
        } catch (e: Exception) {
            fail("Program failed to run to HALT: ${e.message} \n${cpu.dumpState()}", e)
        }
        assertTrue(cpu.isHalted, "CPU should be halted. ${message ?: ""} \n${cpu.dumpState()}")
        assertEquals(expectedAddress, cpu.instructionAddress, "Instruction address after HALT is not as expected. ${message ?: ""} \n${cpu.dumpState()}")
    }
    
    private fun assertProgramResultIs(source: String, expectedValue: Int, message: String? = null) {
        val bytecode = compiler.compile(source)
        val cpu = CPU(bytecode, 0, 100, 30) // program, entryPoint, programStackSize, operandStackSize
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, bytecode.size) // Assumes HALT is last instruction
        assertEquals(1, cpu.stackPointer, "Stack should contain one value (the result). ${message ?: ""} \n${cpu.dumpState()}")
        assertEquals(expectedValue, cpu.stack[0], "Program result on top of stack is not as expected. ${message ?: ""} \n${cpu.dumpState()}")
    }

    private fun assertStackIsEmpty(cpu: CPU, message: String? = null) {
        assertEquals(0, cpu.stackPointer, "Stack should be empty. ${message ?: ""} \n${cpu.dumpState()}")
    }

    private fun assertVariableValue(cpu: CPU, variableAddress: Int, expectedValue: Int, message: String? = null) {
         // In our MiniCVM, local variables are mapped to memory addresses by the CodeGenerator.
         // We'd need to know these addresses. For now, testing via return values is easier.
         // This function might be more useful if we have a fixed global variable section or debug symbols.
        assertEquals(expectedValue, cpu.readMemory(variableAddress), "Memory at address $variableAddress is not as expected. ${message ?: ""} \n${cpu.dumpState()}")
    }
    
    private fun assertProgramRunsToHalt(source: String, message: String? = null): CPU {
        val bytecode = compiler.compile(source)
        val cpu = CPU(bytecode, 0, 100, 30)
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

    @Test
    fun testSimpleArithmetic() {
        val source = """
            int main() {
                return (10 + 5) * 2 - 30 / 2; 
            }
        """
        // (10 + 5) * 2 - 30 / 2 = 15 * 2 - 15 = 30 - 15 = 15
        assertProgramResultIs(source, 15, "Test Simple Arithmetic Failed")
    }
    
    @Test
    fun testArithmeticWithVariables() {
        val source = """
            int main() {
                int a = 10;
                int b = 5;
                int c = 2;
                int d = 30;
                return (a + b) * c - d / c; 
            }
        """
        assertProgramResultIs(source, 15, "Test Arithmetic With Variables Failed")
    }

    @Test
    fun testSubtraction() {
        val source = """
            int main() {
                return 100 - 40 - 5;
            }
        """
        assertProgramResultIs(source, 55, "Test Subtraction Failed")
    }

    @Test
    fun testMultiplication() {
        val source = """
            int main() {
                return 3 * 7 * 2;
            }
        """
        assertProgramResultIs(source, 42, "Test Multiplication Failed")
    }

    @Test
    fun testDivision() {
        val source = """
            int main() {
                return 100 / 10 / 2;
            }
        """
        assertProgramResultIs(source, 5, "Test Division Failed")
    }
    
    @Test
    fun testModulo() {
        val source = """
            int main() {
                return 10 % 3;
            }
        """
        assertProgramResultIs(source, 1, "Test Modulo Failed")
    }

    @Test
    fun testModuloComplex() {
        val source = """
            int main() {
                int a = 23;
                int b = 5;
                return a % b; // 23 % 5 = 3
            }
        """
        assertProgramResultIs(source, 3, "Test Modulo Complex Failed")
    }

    @Test
    fun testOperatorPrecedence() {
        val source = """
            int main() {
                return 2 + 3 * 4; // 2 + 12 = 14
            }
        """
        assertProgramResultIs(source, 14, "Test Operator Precedence (Multiply before Add) Failed")
    }

    @Test
    fun testOperatorPrecedenceWithParentheses() {
        val source = """
            int main() {
                return (2 + 3) * 4; // 5 * 4 = 20
            }
        """
        assertProgramResultIs(source, 20, "Test Operator Precedence with Parentheses Failed")
    }

    // --- Function Call Tests ---

    @Test
    fun testSimpleFunctionCall() {
        val source = """
            int add(int a, int b) {
                return a + b;
            }
            int main() {
                return add(7, 8);
            }
        """
        assertProgramResultIs(source, 15, "Test Simple Function Call Failed")
    }

    @Test
    fun testVoidFunctionCall() {
        // Void functions don't return a value to be used in an expression,
        // but they execute. We can test their side effects if they modify globals or state,
        // or just ensure the program runs. Here, we'll ensure main still returns correctly.
        // The `noop` function doesn't do anything, but it's a valid void function.
        val source = """
            void noop() {
                int x = 5; // This is local, does nothing to main's return
            }
            int main() {
                noop();
                return 123;
            }
        """
        assertProgramResultIs(source, 123, "Test Void Function Call Failed")
    }
    
    @Test
    fun testFunctionWithMultipleParameters() {
         val source = """
            int calculate(int a, int b, int c) {
                return a * b - c;
            }
            int main() {
                return calculate(5, 4, 3); // 5*4 - 3 = 20 - 3 = 17
            }
        """
        assertProgramResultIs(source, 17, "Test Function With Multiple Parameters Failed")
    }

    @Test
    fun testNestedFunctionCalls() {
        val source = """
            int triple(int x) {
                return x * 3;
            }
            int addFive(int y) {
                return y + 5;
            }
            int main() {
                return addFive(triple(10)); // triple(10) = 30; addFive(30) = 35
            }
        """
        assertProgramResultIs(source, 35, "Test Nested Function Calls Failed")
    }
    
    @Test
    fun testFunctionCallWithExpressionAsArgument() {
        val source = """
            int doubleIt(int val) {
                return val * 2;
            }
            int main() {
                int x = 5;
                return doubleIt(x + 3); // doubleIt(8) = 16
            }
        """
        assertProgramResultIs(source, 16, "Test Function Call With Expression As Argument Failed")
    }

    // --- Conditional Statement Tests (`if/else`) ---

    @Test
    fun testIfStatementTrueCondition() {
        val source = """
            int main() {
                int x = 10;
                if (x > 5) {
                    x = 20;
                }
                return x;
            }
        """
        assertProgramResultIs(source, 20, "Test If Statement (True Condition) Failed")
    }

    @Test
    fun testIfStatementFalseCondition() {
        val source = """
            int main() {
                int x = 3;
                if (x > 5) {
                    x = 20; 
                }
                return x; 
            }
        """
        assertProgramResultIs(source, 3, "Test If Statement (False Condition) Failed")
    }

    @Test
    fun testIfElseStatementTrueCondition() {
        val source = """
            int main() {
                int x = 10;
                if (x > 5) {
                    x = 20;
                } else {
                    x = 30;
                }
                return x;
            }
        """
        assertProgramResultIs(source, 20, "Test If/Else Statement (True Condition) Failed")
    }

    @Test
    fun testIfElseStatementFalseCondition() {
        val source = """
            int main() {
                int x = 3;
                if (x > 5) {
                    x = 20;
                } else {
                    x = 30;
                }
                return x;
            }
        """
        assertProgramResultIs(source, 30, "Test If/Else Statement (False Condition) Failed")
    }

    @Test
    fun testIfWithTrueLiteral() {
        val source = """
            int main() {
                int x = 1;
                if (true) {
                    x = 100;
                }
                return x;
            }
        """
        assertProgramResultIs(source, 100, "Test If with true literal failed")
    }

    @Test
    fun testIfWithFalseLiteral() {
        val source = """
            int main() {
                int x = 1;
                if (false) {
                    x = 100;
                }
                return x;
            }
        """
        assertProgramResultIs(source, 1, "Test If with false literal failed")
    }
    
    @Test
    fun testIfElseWithComplexCondition() {
        val source = """
            int main() {
                int a = 10;
                int b = 20;
                int result = 0;
                if ((a * 2) > (b - 5)) { // (20) > (15) -> true
                    result = 1;
                } else {
                    result = -1;
                }
                return result;
            }
        """
        assertProgramResultIs(source, 1, "Test If/Else with complex condition failed")
    }

    // --- Loop Tests (`while`) ---

    @Test
    fun testWhileLoopSimpleSum() {
        val source = """
            int main() {
                int sum = 0;
                int i = 1;
                while (i <= 5) {
                    sum = sum + i;
                    i = i + 1;
                }
                return sum; // 1+2+3+4+5 = 15
            }
        """
        assertProgramResultIs(source, 15, "Test While Loop (Simple Sum) Failed")
    }
    
    @Test
    fun testWhileLoopConditionFalseInitially() {
        val source = """
            int main() {
                int x = 10;
                while (x < 5) {
                    x = x + 1; // This block should not execute
                }
                return x; 
            }
        """
        assertProgramResultIs(source, 10, "Test While Loop (Condition False Initially) Failed")
    }

    @Test
    fun testWhileLoopWithVariableInCondition() {
        val source = """
            int main() {
                int count = 0;
                int limit = 3;
                while (count < limit) {
                    count = count + 1;
                }
                return count; 
            }
        """
        assertProgramResultIs(source, 3, "Test While Loop with variable in condition failed")
    }


    // --- Array Operation Tests ---
    @Test
    fun testArrayDeclarationAssignmentAccess() {
        val source = """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                arr[2] = arr[0] + arr[1];
                return arr[2];
            }
        """
        assertProgramResultIs(source, 30, "Test Array Declaration, Assignment, Access Failed")
    }

    @Test
    fun testArrayAccessWithVariableIndex() {
        val source = """
            int main() {
                int arr[5];
                arr[0] = 7;
                arr[1] = 8;
                arr[2] = 9;
                int index = 1;
                return arr[index] + arr[index+1]; // arr[1] + arr[2] = 8 + 9 = 17
            }
        """
        assertProgramResultIs(source, 17, "Test Array Access with Variable Index Failed")
    }
    
    @Test
    fun testArrayModificationAndReturn() {
        val source = """
            int main() {
                int data[2];
                data[0] = 5;
                data[1] = data[0] * 3; // data[1] = 15
                return data[1];
            }
        """
        assertProgramResultIs(source, 15, "Test Array Modification and Return Failed")
    }


    // --- Logical Operator Tests ---
    @Test
    fun testLogicalAndTrue() {
        val source = """
            int main() {
                int x = 10;
                int y = 0;
                if (x > 5 && y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 1, "Test Logical AND (True) Failed")
    }

    @Test
    fun testLogicalAndFalseLeft() {
        val source = """
            int main() {
                int x = 3;
                int y = 0;
                if (x > 5 && y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 0, "Test Logical AND (False Left) Failed")
    }

    @Test
    fun testLogicalAndFalseRight() {
        val source = """
            int main() {
                int x = 10;
                int y = 1;
                if (x > 5 && y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 0, "Test Logical AND (False Right) Failed")
    }

    @Test
    fun testLogicalOrTrueLeft() {
        val source = """
            int main() {
                int x = 10;
                int y = 1;
                if (x > 5 || y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 1, "Test Logical OR (True Left) Failed")
    }

    @Test
    fun testLogicalOrTrueRight() {
        val source = """
            int main() {
                int x = 3;
                int y = 0;
                if (x > 5 || y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 1, "Test Logical OR (True Right) Failed")
    }

    @Test
    fun testLogicalOrFalse() {
        val source = """
            int main() {
                int x = 3;
                int y = 1;
                if (x > 5 || y == 0) { return 1; } else { return 0; }
            }
        """
        assertProgramResultIs(source, 0, "Test Logical OR (False) Failed")
    }

    @Test
    fun testLogicalNotTrue() {
        val source = """
            int main() {
                int x = 0; // false in C-like bool context
                if (!x) { return 55; } return 0;
            }
        """
        assertProgramResultIs(source, 55, "Test Logical NOT (True from !0) Failed")
    }

    @Test
    fun testLogicalNotFalse() {
        val source = """
            int main() {
                int x = 10; // true in C-like bool context
                if (!x) { return 55; } return 0;
            }
        """
        assertProgramResultIs(source, 0, "Test Logical NOT (False from !10) Failed")
    }
    
    @Test
    fun testComplexLogicalExpression() {
        val source = """
            int main() {
                int a = 5;
                int b = 10;
                int c = 0;
                // ( (5 > 3 && 10 < 15) || 0 ) -> ( (true && true) || false ) -> ( true || false ) -> true
                if ( (a > 3 && b < 15) || c ) { 
                    return 1;
                } else {
                    return 0;
                }
            }
        """
        assertProgramResultIs(source, 1, "Test Complex Logical Expression Failed")
    }

    // --- Scope Tests ---
    @Test
    fun testBasicScopeFunctionLocals() {
        val source = """
            int func1() { 
                int a = 10; 
                return a;
            }
            int func2() { 
                int a = 20; // Different 'a'
                return a;
            }
            int main() { 
                return func1() + func2(); 
            }
        """
        assertProgramResultIs(source, 30, "Test Basic Scope (Function Locals) Failed")
    }

    @Test
    fun testParameterScope() {
        val source = """
            int modify(int val) {
                val = val * 2;
                return val;
            }
            int main() {
                int x = 5;
                return modify(x) + x; // modify(5) returns 10. Result should be 10 + 5 = 15.
            }
        """
        assertProgramResultIs(source, 15, "Test Parameter Scope (pass-by-value) Failed")
    }
    
    // --- End-to-End Small Programs ---
    @Test
    fun testFactorialRecursive() {
        val source = """
            int factorial(int n) {
                if (n <= 1) {
                    return 1;
                }
                return n * factorial(n - 1);
            }
            int main() {
                return factorial(5); // 5*4*3*2*1 = 120
            }
        """
        assertProgramResultIs(source, 120, "Test Factorial (Recursive) Failed")
    }

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

    @Test
    fun testFibonacciRecursive() {
        // Note: Recursive Fibonacci is very inefficient and can easily blow the call stack
        // or take too long for larger numbers. Testing with a small number.
        val source = """
            int fib(int n) {
                if (n <= 0) { return 0; }
                if (n == 1) { return 1; }
                return fib(n-1) + fib(n-2);
            }
            int main() {
                return fib(7); // 0,1,1,2,3,5,8,13 -> fib(7) = 13
            }
        """
        assertProgramResultIs(source, 13, "Test Fibonacci (Recursive) Failed")
    }
    
    @Test
    fun testFibonacciIterative() {
        val source = """
            int main() {
                int n = 7;
                if (n <= 0) { return 0; }
                if (n == 1) { return 1; }
                
                int a = 0;
                int b = 1;
                int i = 2;
                int temp = 0;
                
                while (i <= n) {
                    temp = a + b;
                    a = b;
                    b = temp;
                    i = i + 1;
                }
                return b; // fib(7) = 13
            }
        """
        assertProgramResultIs(source, 13, "Test Fibonacci (Iterative) Failed")
    }

    @Test
    fun testComparisons() {
        val source = """
            int main() {
                if (5 > 3) { } else { return 0; }
                if (3 < 5) { } else { return 0; }
                if (5 == 5) { } else { return 0; }
                if (5 != 3) { } else { return 0; }
                if (5 >= 5) { } else { return 0; }
                if (3 <= 5) { } else { return 0; }
                if (5 >= 3) { } else { return 0; }
                if (3 <= 3) { } else { return 0; }

                if (!(5 < 3)) { } else { return 0; } // !(false) -> true
                if (!(5 == 3)) { } else { return 0; } // !(false) -> true
                
                if (2 > 5) { return 0; } // Should not enter
                if (5 < 2) { return 0; } // Should not enter
                if (5 == 2) { return 0; } // Should not enter
                if (5 != 5) { return 0; } // Should not enter
                if (2 >= 5) { return 0; } // Should not enter
                if (5 <= 2) { return 0; } // Should not enter

                return 1; // All good
            }
        """
        assertProgramResultIs(source, 1, "Test Comparisons Failed")
    }

    @Test
    fun testUnaryMinus() {
        val source = """
            int main() {
                int x = 10;
                return -x + (-5); // -10 + -5 = -15
            }
        """
        assertProgramResultIs(source, -15, "Test Unary Minus Failed")
    }
    
    @Test
    fun testMultipleVariableDeclarations() {
        val source = """
            int main() {
                int a = 1;
                int b = 2;
                int c = a + b; // 3
                int d = c * 2; // 6
                int e = d - 1; // 5
                return e;
            }
        """
        assertProgramResultIs(source, 5, "Test Multiple Variable Declarations Failed")
    }

    // --- Optional: Error Handling Tests (Example) ---
    // These would require the compiler to throw specific, catchable exceptions.
    // For now, the MiniCvmCompiler lets exceptions from Lexer/Parser/CodeGen propagate.

    @Test
    fun testSyntaxErrorMissingSemicolon() {
        val source = """
            int main() {
                int x = 5 // Missing semicolon
                return x;
            }
        """
        // Assuming ParserException is thrown for syntax errors
        assertFailsWith<com.hiperbou.vm.minicvm.parser.ParserException>(
            message = "Compilation should fail due to missing semicolon"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testSemanticErrorUndefinedVariable() {
        val source = """
            int main() {
                return x; // x is not defined
            }
        """
         // Assuming CodeGenException for undefined variable
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail due to undefined variable"
        ) {
            compiler.compile(source)
        }
    }
    
    @Test
    fun testSemanticErrorUndefinedFunction() {
        val source = """
            int main() {
                return undefinedFunc();
            }
        """
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail due to undefined function"
        ) {
            compiler.compile(source)
        }
    }
    
    @Test
    fun testSemanticErrorFunctionArgumentMismatchTooFew() {
        val source = """
            int add(int a, int b) { return a + b; }
            int main() {
                return add(5); // Too few arguments
            }
        """
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail due to too few function arguments"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testSemanticErrorFunctionArgumentMismatchTooMany() {
        val source = """
            int add(int a, int b) { return a + b; }
            int main() {
                return add(5, 6, 7); // Too many arguments
            }
        """
        assertFailsWith<com.hiperbou.vm.minicvm.codegen.CodeGenException>(
            message = "Compilation should fail due to too many function arguments"
        ) {
            compiler.compile(source)
        }
    }

    @Test
    fun testInvalidAssignmentTarget() {
        val source = """
            int main() {
                5 = 10; // Cannot assign to a literal
                return 0;
            }
        """
        assertFailsWith<com.hiperbou.vm.minicvm.parser.ParserException>(
            message = "Compilation should fail due to invalid assignment target"
        ) {
            compiler.compile(source)
        }
    }
}

// Helper to get the CPU state for debugging messages
private fun CPU.dumpState(): String {
    val sb = StringBuilder()
    sb.append("CPU State:\n")
    sb.append("  IP: $instructionAddress, SP: $stackPointer, FP: $framePointer, Halted: $isHalted\n")
    sb.append("  Stack (top $stackPointer elements): ${stack.take(stackPointer).joinToString(", ")}\n")
    // Add memory dump if relevant, e.g., first N locations or specific ranges
    // sb.append("  Memory (first 20): ${memory.take(20).joinToString(", ")}\n")
    return sb.toString()
}
