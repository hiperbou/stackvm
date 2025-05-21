package com.hiperbou.vm.minicvm

import com.hiperbou.vm.VirtualMachine
import com.hiperbou.vm.RAM_SIZE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

// For capturing print output (basic version)
class TestOutputCapture {
    private val stringBuilder = StringBuilder()

    fun print(value: Any?) {
        stringBuilder.append(value.toString())
    }

    fun println(value: Any?) {
        stringBuilder.append(value.toString()).append('\n')
    }

    fun getOutput(): String = stringBuilder.toString()
    fun clear() = stringBuilder.clear()
}

class MiniCvmNewFeaturesTest {

    private fun runCode(
        code: String,
        stackSize: Int = 1024,
        initialGlobals: Map<Int, Int> = emptyMap(),
        outputCapture: TestOutputCapture? = null
    ): VirtualMachine {
        val program = MiniCvmCompiler.compile(code)
        val vm = VirtualMachine(program, RAM_SIZE, stackSize) // program ROM, RAM, stack size for program

        initialGlobals.forEach { (address, value) ->
            // Assuming global variables are stored in the lower part of RAM by convention
            // For these tests, we'll use direct memory access.
            // A more robust solution might involve a symbol table from the compiler.
            vm.memory.set(address, value)
        }

        // Basic print redirection if capture is provided
        // This is a simplified approach. A full solution would involve deeper VM integration.
        if (outputCapture != null) {
            // This is where we would ideally redirect the VM's print operations.
            // For now, since the VM's `PRINT` opcode uses `kotlin.io.println`,
            // true output capture without modifying the VM or using a test framework's
            // system-level capture is hard.
            // The tests for print will check for execution without error and argument consumption.
        }

        vm.run()
        return vm
    }

    // Helper to assert global variable values
    private fun assertGlobal(vm: VirtualMachine, address: Int, expectedValue: Int, message: String) {
        assertEquals(expectedValue, vm.memory.get(address), message)
    }

    // --- Test Cases Start Here ---

    // 1. Do-While Loop
    @Test
    fun testDoWhileLoopSimple() {
        val code = """
            int main() {
                int x = 0;
                int i = 0;
                do {
                    x = x + 1; // x becomes 1, 2, 3, 4, 5
                    i = i + 1; // i becomes 1, 2, 3, 4, 5
                } while (i < 5); // continues if i is 0,1,2,3,4. Stops when i is 5.
                return x;
            }
        """
        val vm = runCode(code)
        assertEquals(5, vm.returnValue, "Do-while loop should increment x to 5")
    }

    @Test
    fun testDoWhileLoopExecutesOnce() {
        val code = """
            int main() {
                int x = 0;
                int i = 5; // Condition i < 5 is initially false
                do {
                    x = x + 1; // x becomes 1
                    i = i + 1; // i becomes 6
                } while (i < 5); // 6 < 5 is false, loop terminates
                return x;
            }
        """
        val vm = runCode(code)
        assertEquals(1, vm.returnValue, "Do-while loop should execute once even if condition is initially false")
    }
    
    @Test
    fun testDoWhileLoopConditionInitiallyFalse() {
         val code = """
            int main() {
                int x = 10;
                do {
                    x = 20;
                } while (0); // Condition is false (0 means false)
                return x;
            }
        """
        val vm = runCode(code)
        assertEquals(20, vm.returnValue, "Do-while with initially false condition should execute once.")
    }

    // 2. For Loop
    @Test
    fun testForLoopBasic() {
        val code = """
            int main() {
                int sum = 0;
                int i = 0; // Declared outside for this version
                for (i = 0; i < 5; i = i + 1) {
                    sum = sum + i; // 0 + 1 + 2 + 3 + 4 = 10
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "Basic for loop sum should be 10")
    }

    @Test
    fun testForLoopDeclareInside() {
        val code = """
            int main() {
                int sum = 0;
                for (int i = 0; i < 5; i = i + 1) { // i declared inside
                    sum = sum + i;
                }
                // i should not be accessible here
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "For loop with variable declared inside sum should be 10")
    }

    @Test
    fun testForLoopEmptyInitializer() {
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                for (; i < 5; i = i + 1) {
                    sum = sum + i;
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "For loop with empty initializer sum should be 10")
    }

    @Test
    fun testForLoopEmptyCondition() { // Effectively an infinite loop, needs break
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                for (i = 0; ; i = i + 1) {
                    if (i >= 5) {
                        break;
                    }
                    sum = sum + i;
                }
                return sum; // Should be 0+1+2+3+4 = 10
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "For loop with empty condition (using break) sum should be 10")
    }

    @Test
    fun testForLoopEmptyIncrementer() {
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                for (i = 0; i < 5; ) {
                    sum = sum + i;
                    i = i + 1; // Manual increment
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "For loop with empty incrementer sum should be 10")
    }

    @Test
    fun testForLoopAllEmpty() { // Infinite loop, needs break
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                for (;;) {
                    if (i >= 5) {
                        break;
                    }
                    sum = sum + i;
                    i = i + 1;
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "For loop with all parts empty (using break) sum should be 10")
    }

    // 3. Break Statement
    @Test
    fun testBreakFromWhileLoop() {
        val code = """
            int main() {
                int x = 0;
                int i = 0;
                while (i < 10) {
                    x = x + 1;
                    if (i == 4) { // x will be 1,2,3,4,5
                        break;
                    }
                    i = i + 1;
                }
                return x; // Should be 5
            }
        """
        val vm = runCode(code)
        assertEquals(5, vm.returnValue, "Break from while loop should result in x = 5")
    }

    @Test
    fun testBreakFromDoWhileLoop() {
        val code = """
            int main() {
                int x = 0;
                int i = 0;
                do {
                    x = x + 1;
                    if (i == 2) { // x will be 1,2,3
                        break;
                    }
                    i = i + 1;
                } while (i < 5);
                return x; // Should be 3
            }
        """
        val vm = runCode(code)
        assertEquals(3, vm.returnValue, "Break from do-while loop should result in x = 3")
    }

    @Test
    fun testBreakFromForLoop() {
        val code = """
            int main() {
                int sum = 0;
                for (int i = 0; i < 10; i = i + 1) {
                    if (i == 5) {
                        break;
                    }
                    sum = sum + i; // 0+1+2+3+4 = 10
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "Break from for loop sum should be 10")
    }

    @Test
    fun testBreakNestedLoopInner() {
        // Using globals to check outer loop continuation
        // global 0: outer_sum, global 1: inner_sum_total
        val code = """
            int main() {
                int outer_val = 0; // Store in global 0
                int inner_sum_total = 0; // Store in global 1

                int i = 0;
                while (i < 3) { // Outer loop: 0, 1, 2
                    outer_val = outer_val + 10; // 10, 20, 30
                    
                    int j = 0;
                    int current_inner_sum = 0;
                    while (j < 5) { // Inner loop
                        if (j == 2) {
                            break; // Breaks inner loop
                        }
                        current_inner_sum = current_inner_sum + 1; // 1, 2
                        j = j + 1;
                    }
                    inner_sum_total = inner_sum_total + current_inner_sum; // Iter 0: 2, Iter 1: 2, Iter 2: 2 => Total 6
                    i = i + 1;
                }
                
                // For returning multiple values, store them in memory and return one, or test memory directly.
                // Here, we'll return outer_val and check inner_sum_total from memory.
                // Let's use specific memory addresses for globals if runCode supports it or modify runCode.
                // For now, let's assume globals are not easily testable this way without modifying runCode for globals.
                // So, let's change the test to return a combined value or focus on one.
                // For simplicity, return outer_val * 100 + inner_sum_total
                return outer_val * 100 + inner_sum_total; // 30 * 100 + 6 = 3006
            }
        """
        val vm = runCode(code)
        val expectedOuter = 30
        val expectedInnerTotal = 6
        assertEquals(expectedOuter * 100 + expectedInnerTotal, vm.returnValue, "Break in nested loop should only break inner loop.")
    }


    // 4. Continue Statement
    @Test
    fun testContinueInWhileLoop() {
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                while (i < 5) { // 0, 1, 2, 3, 4
                    i = i + 1; // i becomes 1, 2, 3, 4, 5
                    if (i == 2 || i == 4) {
                        continue;
                    }
                    sum = sum + i; // sum += 1 (i=1), sum += 3 (i=3), sum += 5 (i=5) => 1+3+5 = 9
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(9, vm.returnValue, "Continue in while loop sum should be 9")
    }

    @Test
    fun testContinueInDoWhileLoop() {
        val code = """
            int main() {
                int sum = 0;
                int i = 0;
                do {
                    i = i + 1; // i becomes 1, 2, 3, 4, 5
                    if (i == 2 || i == 4) {
                        continue;
                    }
                    sum = sum + i; // sum += 1 (i=1), sum += 3 (i=3), sum += 5 (i=5) => 1+3+5 = 9
                } while (i < 5);
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(9, vm.returnValue, "Continue in do-while loop sum should be 9")
    }
    
    @Test
    fun testContinueInForLoop() {
        val code = """
            int main() {
                int sum = 0;
                // i goes 0, 1, 2, 3, 4
                for (int i = 0; i < 5; i = i + 1) {
                    if (i == 1 || i == 3) { // Skip when i is 1 or 3
                        continue;
                    }
                    sum = sum + i; // sum += 0 (i=0), sum += 2 (i=2), sum += 4 (i=4) => 0+2+4 = 6
                }
                return sum;
            }
        """
        val vm = runCode(code)
        assertEquals(6, vm.returnValue, "Continue in for loop sum should be 6")
    }

    @Test
    fun testContinueNestedLoopInner() {
        // Similar to break, testing side effects or combined return.
        val code = """
            int main() {
                int outer_iterations = 0;
                int total_inner_adds = 0;

                int i = 0;
                while (i < 3) { // Outer: 0, 1, 2
                    outer_iterations = outer_iterations + 1; // 1, 2, 3
                    
                    int j = 0;
                    for (j = 0; j < 4; j = j + 1) { // Inner: 0, 1, 2, 3
                        if (j == 1 || j == 3) { // Skip when j is 1 or 3
                            continue;
                        }
                        total_inner_adds = total_inner_adds + 1; // Inner adds for j=0, j=2. So 2 adds per outer loop.
                                                                 // Total: 2*3 = 6
                    }
                    i = i + 1;
                }
                return outer_iterations * 100 + total_inner_adds; // 3 * 100 + 6 = 306
            }
        """
        val vm = runCode(code)
        assertEquals(306, vm.returnValue, "Continue in nested loop affects inner loop correctly.")
    }

    // 5. Bitwise Operators
    @Test
    fun testBitwiseAnd() {
        val code = """
            int main() {
                int a = 5;  // 0101
                int b = 3;  // 0011
                return a & b; // 0001 => 1
            }
        """
        val vm = runCode(code)
        assertEquals(1, vm.returnValue, "5 & 3 should be 1")
    }

    @Test
    fun testBitwiseOr() {
        val code = """
            int main() {
                int a = 5;  // 0101
                int b = 3;  // 0011
                return a | b; // 0111 => 7
            }
        """
        val vm = runCode(code)
        assertEquals(7, vm.returnValue, "5 | 3 should be 7")
    }

    @Test
    fun testBitwiseXor() {
        val code = """
            int main() {
                int a = 5;  // 0101
                int b = 3;  // 0011
                return a ^ b; // 0110 => 6
            }
        """
        val vm = runCode(code)
        assertEquals(6, vm.returnValue, "5 ^ 3 should be 6")
    }

    @Test
    fun testBitwiseNot() {
        val code = """
            int main() {
                int a = 5; // 0...0101
                return ~a; // 1...1010 (two's complement for -6 if int is 32-bit)
                           // In a simple VM, bitwise not might just flip bits.
                           // Assuming standard integer representation where ~x = -x-1
            }
        """
        val vm = runCode(code)
        assertEquals(-6, vm.returnValue, "~5 should be -6 (assuming standard two's complement)")
    }
    
    @Test
    fun testBitwiseNotZero() {
        val code = """
            int main() {
                int a = 0;
                return ~a; // ~0 is -1 (all bits set to 1)
            }
        """
        val vm = runCode(code)
        assertEquals(-1, vm.returnValue, "~0 should be -1")
    }

    @Test
    fun testBitwiseCombined() {
        val code = """
            int main() {
                int a = 10; // 1010
                int b = 12; // 1100
                int c = 5;  // 0101
                // (1010 & 1100) | 0101 = 1000 | 0101 = 1101 (13)
                // ~13 = -14
                return ~( (a & b) | c );
            }
        """
        val vm = runCode(code)
        assertEquals(-14, vm.returnValue, "Combined bitwise operation ~( (10 & 12) | 5 ) should be -14")
    }

    // 6. Increment/Decrement Operators
    @Test
    fun testPrefixIncrement() {
        val code = """
            int main() {
                int x = 5;
                int y = ++x; // x becomes 6, y becomes 6
                return y * 10 + x; // 6 * 10 + 6 = 66
            }
        """
        val vm = runCode(code)
        assertEquals(66, vm.returnValue, "Prefix increment ++x failed.")
    }

    @Test
    fun testPostfixIncrement() {
        val code = """
            int main() {
                int x = 5;
                int y = x++; // y becomes 5, x becomes 6
                return y * 10 + x; // 5 * 10 + 6 = 56
            }
        """
        val vm = runCode(code)
        assertEquals(56, vm.returnValue, "Postfix increment x++ failed.")
    }

    @Test
    fun testPrefixDecrement() {
        val code = """
            int main() {
                int x = 5;
                int y = --x; // x becomes 4, y becomes 4
                return y * 10 + x; // 4 * 10 + 4 = 44
            }
        """
        val vm = runCode(code)
        assertEquals(44, vm.returnValue, "Prefix decrement --x failed.")
    }

    @Test
    fun testPostfixDecrement() {
        val code = """
            int main() {
                int x = 5;
                int y = x--; // y becomes 5, x becomes 4
                return y * 10 + x; // 5 * 10 + 4 = 54
            }
        """
        val vm = runCode(code)
        assertEquals(54, vm.returnValue, "Postfix decrement x-- failed.")
    }

    @Test
    fun testIncrementInExpression() {
        val code = """
            int main() {
                int x = 3;
                int y = (++x) + (x++); // x=4 (y=4) + (x=4, then x=5) => 4 + 4 = 8. x becomes 5.
                return y * 10 + x;      // 8 * 10 + 5 = 85
            }
        """
        val vm = runCode(code)
        assertEquals(85, vm.returnValue, "Increment operators in expression failed.")
    }
    
    @Test
    fun testDecrementInExpression() {
        val code = """
            int main() {
                int x = 3;
                int y = (--x) + (x--); // x=2 (y=2) + (x=2, then x=1) => 2 + 2 = 4. x becomes 1.
                return y * 10 + x;      // 4 * 10 + 1 = 41
            }
        """
        val vm = runCode(code)
        assertEquals(41, vm.returnValue, "Decrement operators in expression failed.")
    }

    // Array tests for increment/decrement need a way to initialize arrays or use globals.
    // For simplicity, let's assume array operations are complex to set up without direct memory write access in tests
    // or global array declarations if not fully supported.
    // We will test a simple case assuming arrays are zero-initialized or can be set up.
    // The `runCode` helper does not easily support array initialization.
    // Let's modify the test to declare and initialize the array inside main.

    @Test
    fun testIncrementArrayElementPrefix() {
        val code = """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                int y = ++arr[1]; // arr[1] becomes 21, y is 21
                return y * 100 + arr[0] * 10 + arr[1]; // 21 * 100 + 10 * 10 + 21 = 2100 + 100 + 21 = 2221
            }
        """
        val vm = runCode(code)
        assertEquals(2221, vm.returnValue, "Prefix increment on array element failed.")
    }

    @Test
    fun testIncrementArrayElementPostfix() {
        val code = """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                int y = arr[1]++; // y is 20, arr[1] becomes 21
                return y * 100 + arr[0] * 10 + arr[1]; // 20 * 100 + 10 * 10 + 21 = 2000 + 100 + 21 = 2121
            }
        """
        val vm = runCode(code)
        assertEquals(2121, vm.returnValue, "Postfix increment on array element failed.")
    }
    
    @Test
    fun testIncrementArrayElementWithVariableIndex() {
        val code = """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                arr[2] = 30;
                int idx = 1;
                arr[idx]++; // arr[1] (20) becomes 21. Value of expression is 20, but not used.
                idx++;      // idx (1) becomes 2. Value of expression is 1, but not used.
                ++arr[idx]; // arr[2] (30) becomes 31. Value of expression is 31.
                            // arr[0]=10, arr[1]=21, arr[2]=31
                return arr[0] * 10000 + arr[1] * 100 + arr[2]; // 100000 + 2100 + 31 = 102131
            }
        """
        val vm = runCode(code)
        assertEquals(102131, vm.returnValue, "Increment on array element with variable index failed.")
    }

    // 7. Ternary Operator (?:)
    @Test
    fun testTernaryOperatorSimpleTrue() {
        val code = """
            int main() {
                int a = 10;
                int b = 5;
                int result = (a > b) ? a : b; // 10 > 5 is true, result = a (10)
                return result;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "Ternary operator (true case) failed.")
    }

    @Test
    fun testTernaryOperatorSimpleFalse() {
        val code = """
            int main() {
                int a = 5;
                int b = 10;
                int result = (a > b) ? a : b; // 5 > 10 is false, result = b (10)
                return result;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "Ternary operator (false case) failed.")
    }

    @Test
    fun testTernaryOperatorWithExpressions() {
        val code = """
            int main() {
                int a = 3;
                int b = 7;
                // (3 > 7) is false. result = b + 5 = 7 + 5 = 12
                int result = (a > b) ? (a * 2) : (b + 5); 
                return result;
            }
        """
        val vm = runCode(code)
        assertEquals(12, vm.returnValue, "Ternary operator with expressions failed.")
    }
    
    @Test
    fun testTernaryOperatorSideEffectsTrueBranch() {
        // Test that only one branch is evaluated. Use globals for side effects.
        // global 0: side_effect_true
        // global 1: side_effect_false
        // We need to initialize globals to 0.
        val code = """
            int main() {
                // side_effect_true at mem[0], side_effect_false at mem[1]
                // In this test, they are local variables for simplicity as global setup is tricky.
                int side_effect_true = 0;
                int side_effect_false = 0;
                int condition = 1; // true
                
                int result = condition ? (side_effect_true = 10) : (side_effect_false = 20);
                
                // result should be 10
                // side_effect_true should be 10
                // side_effect_false should be 0
                return result * 1000 + side_effect_true * 100 + side_effect_false; // 10 * 1000 + 10 * 100 + 0 = 10000 + 1000 = 11000
            }
        """
        val vm = runCode(code)
        assertEquals(11000, vm.returnValue, "Ternary operator true branch side effect failed.")
    }

    @Test
    fun testTernaryOperatorSideEffectsFalseBranch() {
        val code = """
            int main() {
                int side_effect_true = 0;
                int side_effect_false = 0;
                int condition = 0; // false
                
                int result = condition ? (side_effect_true = 10) : (side_effect_false = 20);
                
                // result should be 20
                // side_effect_true should be 0
                // side_effect_false should be 20
                return result * 1000 + side_effect_true * 100 + side_effect_false; // 20 * 1000 + 0 * 100 + 20 = 20000 + 20 = 20020
            }
        """
        val vm = runCode(code)
        assertEquals(20020, vm.returnValue, "Ternary operator false branch side effect failed.")
    }

    @Test
    fun testNestedTernaryOperator() {
        val code = """
            int main() {
                int a = 1, b = 0, c = 1, d = 0, e = 1, f = 0;
                int result = a > b ? (c > d ? 10 : 20) : (e > f ? 30 : 40);
                // a > b is true (1 > 0)
                //   c > d is true (1 > 0)
                //     result = 10
                return result;
            }
        """
        val vm = runCode(code)
        assertEquals(10, vm.returnValue, "Nested ternary (true-true) failed.")

        val code2 = """
            int main() {
                int a = 1, b = 0, c = 0, d = 1, e = 1, f = 0;
                int result = a > b ? (c > d ? 10 : 20) : (e > f ? 30 : 40);
                // a > b is true (1 > 0)
                //   c > d is false (0 > 1)
                //     result = 20
                return result;
            }
        """
        val vm2 = runCode(code2)
        assertEquals(20, vm2.returnValue, "Nested ternary (true-false) failed.")
        
        val code3 = """
            int main() {
                int a = 0, b = 1, c = 1, d = 0, e = 1, f = 0;
                int result = a > b ? (c > d ? 10 : 20) : (e > f ? 30 : 40);
                // a > b is false (0 > 1)
                //   e > f is true (1 > 0)
                //     result = 30
                return result;
            }
        """
        val vm3 = runCode(code3)
        assertEquals(30, vm3.returnValue, "Nested ternary (false-true) failed.")

        val code4 = """
            int main() {
                int a = 0, b = 1, c = 1, d = 0, e = 0, f = 1;
                int result = a > b ? (c > d ? 10 : 20) : (e > f ? 30 : 40);
                // a > b is false (0 > 1)
                //   e > f is false (0 > 1)
                //     result = 40
                return result;
            }
        """
        val vm4 = runCode(code4)
        assertEquals(40, vm4.returnValue, "Nested ternary (false-false) failed.")
    }

    // 8. Print Functions (Basic execution tests)
    // Full output capture is complex here, so we test if they run and consume args.
    @Test
    fun testPrintFunctionSimpleLiteral() {
        val code = """
            int main() {
                print(123);
                return 0; // Ensure it runs
            }
        """
        // No direct output assertion, just checking for successful execution.
        // The `runCode` helper would need modification for output capture.
        // This test implicitly checks if `PRINT` opcode works and stack is managed.
        val vm = runCode(code)
        assertEquals(0, vm.returnValue, "print(123) failed to execute.")
    }

    @Test
    fun testPrintFunctionExpression() {
        val code = """
            int main() {
                int x = 10;
                int y = 20;
                print(x + y); // Should print 30
                return x + y; // Return 30 to verify expression was evaluated
            }
        """
        val vm = runCode(code)
        assertEquals(30, vm.returnValue, "print(x+y) failed or expression evaluation incorrect.")
        // Stack check: after print, stack should be clean if only argument was pushed.
        // The return value itself uses the stack, so direct stack empty check isn't trivial here.
    }

    @Test
    fun testDebugPrintFunctionSimpleLiteral() {
        val code = """
            int main() {
                debugPrint(-456);
                return 0; 
            }
        """
        val vm = runCode(code)
        assertEquals(0, vm.returnValue, "debugPrint(-456) failed to execute.")
    }

    @Test
    fun testDebugPrintFunctionVariable() {
        val code = """
            int main() {
                int someVar = 789;
                debugPrint(someVar);
                return someVar;
            }
        """
        val vm = runCode(code)
        assertEquals(789, vm.returnValue, "debugPrint(someVar) failed or var access incorrect.")
    }
}
