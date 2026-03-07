package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.plugin.print.PrintInstructions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CCompilerTest {

    private class CapturingPrintDecoder(
        private val stack: CPUStack<Int>,
        private var nextDecoder: Decoder = ExceptionDecoder.instance
    ) : Decoder {
        val output = mutableListOf<Int>()

        override fun decodeInstruction(instruction: Int) {
            when (instruction) {
                PrintInstructions.PRINT,
                PrintInstructions.DEBUG_PRINT -> output.add(stack.peek())
                else -> nextDecoder.decodeInstruction(instruction)
            }
        }

        override fun setNextDecoder(decoder: Decoder) {
            nextDecoder = decoder
        }
    }

    private fun compileAndRun(source: String): List<Int> {
        val bytecode = CCompiler().compile(source)
        val cpu = CPU(bytecode)
        val printDecoder = CapturingPrintDecoder(cpu.getStack())
        cpu.appendDecoder(printDecoder)
        cpu.run()
        return printDecoder.output
    }

    @Test
    fun phase1PrintIntegerLiteralTest() {
        val output = compileAndRun("""
            int main() {
                print(42);
                return 0;
            }
        """)
        assertEquals(listOf(42), output)
    }

    @Test
    fun phase2LocalVariableAndDefaultZeroTest() {
        val output = compileAndRun("""
            int main() {
                int x = 10;
                int y;
                print(x);
                print(y);
                return 0;
            }
        """)
        assertEquals(listOf(10, 0), output)
    }

    @Test
    fun phase3ArithmeticOperatorsTest() {
        val output = compileAndRun("""
            int main() {
                print(3 + 4 * 2);
                print((3 + 4) * 2);
                print(10 - 3);
                print(10 / 2);
                print(10 % 3);
                print(-5);
                return 0;
            }
        """)
        assertEquals(listOf(11, 14, 7, 5, 1, -5), output)
    }

    @Test
    fun phase4IfElseBranchesTest() {
        val output = compileAndRun("""
            int main() {
                int x = 5;
                if (x > 3) {
                    print(1);
                } else {
                    print(0);
                }

                x = 1;
                if (x > 3) {
                    print(1);
                } else {
                    print(0);
                }
                return 0;
            }
        """)
        assertEquals(listOf(1, 0), output)
    }

    @Test
    fun phase5WhileLoopTest() {
        val output = compileAndRun("""
            int main() {
                int i = 3;
                while (i > 0) {
                    print(i);
                    i = i - 1;
                }
                return 0;
            }
        """)
        assertEquals(listOf(3, 2, 1), output)
    }

    @Test
    fun phase6ForLoopTest() {
        val output = compileAndRun("""
            int main() {
                int sum = 0;
                for (int i = 0; i < 5; i++) {
                    sum = sum + i;
                }
                print(sum);
                return 0;
            }
        """)
        assertEquals(listOf(10), output)
    }

    @Test
    fun phase7FunctionCallAndReturnTest() {
        val output = compileAndRun("""
            int max(int a, int b) {
                if (a > b) {
                    return a;
                } else {
                    return b;
                }
            }

            int main() {
                int x = 6;
                int y = 4;
                print(max(x, y));
                return 0;
            }
        """)
        assertEquals(listOf(6), output)
    }

    @Test
    fun phase8GlobalVariableReadAndWriteTest() {
        val output = compileAndRun("""
            int counter = 10;

            int bump() {
                counter = counter + 1;
                return counter;
            }

            int main() {
                print(counter);
                print(bump());
                print(counter);
                return 0;
            }
        """)
        assertEquals(listOf(10, 11, 11), output)
    }

    @Test
    fun phase9ComparisonAndLogicalOperatorsTest() {
        val output = compileAndRun("""
            int main() {
                print(3 == 3);
                print(3 != 4);
                print(2 < 3);
                print(3 > 2);
                print(2 <= 2);
                print(3 >= 3);
                print(1 && 0);
                print(1 || 0);
                print(!0);
                print(!1);
                return 0;
            }
        """)
        assertEquals(listOf(1, 1, 1, 1, 1, 1, 0, 1, 1, 0), output)
    }

    @Test
    fun phase10CompoundAssignmentPlusAndMinusTest() {
        val output = compileAndRun("""
            int main() {
                int x = 10;
                x += 3;
                x -= 5;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(8), output)
    }

    @Test
    fun phase11PreAndPostIncDecTest() {
        val output = compileAndRun("""
            int main() {
                int x = 1;
                print(++x);
                print(x);
                print(x++);
                print(x);
                print(--x);
                print(x--);
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(2, 2, 2, 3, 2, 2, 1), output)
    }

    @Test
    fun doScopeAndShadowingTest() {
        val output = compileAndRun("""
            int main() {
                int x = 5;
                do {
                    int x = 9;
                    print(x);
                }
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(9, 5), output)
    }

    @Test
    fun doWhileLoopExecutesAtLeastOnceTest() {
        val output = compileAndRun("""
            int main() {
                int x = 0;
                do {
                    print(1);
                    x = x + 1;
                } while (0);
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(1, 1), output)
    }

    @Test
    fun doWhileLoopRepeatsWhileConditionIsTrueTest() {
        val output = compileAndRun("""
            int main() {
                int i = 0;
                do {
                    print(i);
                    i = i + 1;
                } while (i < 3);
                return 0;
            }
        """)
        assertEquals(listOf(0, 1, 2), output)
    }

    @Test
    fun continueWorksInWhileForAndDoWhileLoopsTest() {
        val output = compileAndRun("""
            int main() {
                int i = 0;
                while (i < 4) {
                    i = i + 1;
                    if (i == 2) {
                        continue;
                    }
                    print(i);
                }

                for (int j = 0; j < 4; j++) {
                    if (j == 1) {
                        continue;
                    }
                    print(j);
                }

                int k = 0;
                do {
                    k = k + 1;
                    if (k == 2) {
                        continue;
                    }
                    print(k);
                } while (k < 3);

                return 0;
            }
        """)
        assertEquals(listOf(1, 3, 4, 0, 2, 3, 1, 3), output)
    }    @Test
    fun forLoopSupportsEmptyInitializerConditionAndUpdateTest() {
        val output = compileAndRun("""
            int main() {
                int sum = 0;
                int i = 0;

                for (; i < 5; i = i + 1) {
                    sum = sum + i;
                }

                for (i = 0; ; i = i + 1) {
                    if (i >= 5) {
                        break;
                    }
                    sum = sum + i;
                }

                i = 0;
                for (i = 0; i < 3; ) {
                    sum = sum + i;
                    i = i + 1;
                }

                i = 0;
                for (;;) {
                    if (i >= 2) {
                        break;
                    }
                    sum = sum + i;
                    i = i + 1;
                }

                print(sum);
                return 0;
            }
        """)
        assertEquals(listOf(24), output)
    }

    @Test
    fun bitwiseOperatorsOnIntegersTest() {
        val output = compileAndRun("""
            int main() {
                print(5 & 3);
                print(5 | 3);
                print(5 ^ 3);
                print(~5);
                print(~((10 & 12) | 5));
                return 0;
            }
        """)
        assertEquals(listOf(1, 7, 6, -6, -14), output)
    }    @Test
    fun multiVariableDeclarationInitializesEachVariableTest() {
        val output = compileAndRun("""
            int main() {
                int a = 1, b = 2, c;
                c = a + b;
                print(c);
                return 0;
            }
        """)
        assertEquals(listOf(3), output)
    }

    @Test
    fun stressDeepCallsWithDoScopesAndShadowingTest() {
        val output = compileAndRun("""
            int fib(int n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
            }

            int blend(int a, int b) {
                int r = a + b;
                do {
                    int a = r * 2;
                    if (a > 10) {
                        int b = a - 3;
                        r = r + b;
                    } else {
                        int b = a + 1;
                        r = r + b;
                    }
                }
                return r;
            }

            int walk(int seed) {
                int total = 0;
                for (int i = 0; i < 4; i++) {
                    int f = fib(i + 3);
                    int m = blend(seed + i, f);
                    do {
                        int total = m % 5;
                        total = total + 1;
                        print(total);
                    }
                    total = total + m;
                }
                return total;
            }

            int main() {
                int x = 6;
                int y = 4;
                int a = walk(x);
                int b = walk(y);
                print(a);
                print(b);
                print(a - b);
                return 0;
            }
        """)
        assertEquals(listOf(2, 3, 2, 4, 1, 2, 1, 3, 132, 108, 24), output)
    }
    @Test
    fun debugprintEmitsOutputAndKeepsStackBalancedTest() {
        val output = compileAndRun("""
            int main() {
                int x = 5;
                debugPrint(x);
                debugPrint(x + 7);
                return 0;
            }
        """)
        assertEquals(listOf(5, 12), output)
    }
    @Test
    fun ternaryOperatorEvaluatesOnlySelectedBranchTest() {
        val output = compileAndRun("""
            int main() {
                int t = 0;
                int f = 0;
                int a = 1 ? (t = 10) : (f = 20);
                int b = 0 ? (t = t + 1) : (f = f + 3);
                print(a);
                print(b);
                print(t);
                print(f);
                return 0;
            }
        """)
        assertEquals(listOf(10, 3, 10, 3), output)
    }
    @Test
    fun arrayDeclarationAndIndexedReadWriteTest() {
        val output = compileAndRun("""
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                print(arr[0]);
                print(arr[1]);
                return 0;
            }
        """)
        assertEquals(listOf(10, 20), output)
    }

    @Test
    fun arrayIndexingWithVariableIndexTest() {
        val output = compileAndRun("""
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                arr[2] = 30;
                int idx = 1;
                print(arr[idx]);
                idx = 2;
                print(arr[idx]);
                return 0;
            }
        """)
        assertEquals(listOf(20, 30), output)
    }    @Test
    fun arrayElementPrefixAndPostfixIncrementTest() {
        val output = compileAndRun("""
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                int y = ++arr[1];
                int z = arr[1]++;
                print(y);
                print(z);
                print(arr[1]);
                return 0;
            }
        """)
        assertEquals(listOf(21, 21, 22), output)
    }

    @Test
    fun arrayIncrementWithVariableIndexTest() {
        val output = compileAndRun("""
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                arr[2] = 30;
                int idx = 1;
                arr[idx]++;
                idx++;
                ++arr[idx];
                print(arr[0]);
                print(arr[1]);
                print(arr[2]);
                return 0;
            }
        """)
        assertEquals(listOf(10, 21, 31), output)
    }
    @Test
    fun switchSelectsMatchingCaseAndDefaultTest() {
        val output = compileAndRun("""
            int main() {
                int x = 2;
                switch (x) {
                    case 1:
                        print(10);
                        break;
                    case 2:
                        print(20);
                        break;
                    default:
                        print(30);
                }
                x = 9;
                switch (x) {
                    case 1:
                        print(100);
                        break;
                    default:
                        print(300);
                }
                return 0;
            }
        """)
        assertEquals(listOf(20, 300), output)
    }

    @Test
    fun switchSupportsFallthroughAndBreakOnlyExitsSwitchTest() {
        val output = compileAndRun("""
            int main() {
                int x = 1;
                switch (x) {
                    case 1:
                        print(1);
                    case 2:
                        print(2);
                        break;
                    default:
                        print(9);
                }

                int i = 0;
                while (i < 4) {
                    switch (i) {
                        case 1:
                            print(100);
                            break;
                        case 2:
                            print(200);
                            i = i + 1;
                            break;
                        default:
                            print(i);
                    }
                    i = i + 1;
                }
                return 0;
            }
        """)
        assertEquals(listOf(1, 2, 0, 100, 200), output)
    }
    @Test
    fun undefinedVariableThrowsCodegenexceptionTest() {
        assertFailsWith<com.hiperbou.vm.ccompiler.codegen.CodeGenException> {
            CCompiler().compile("""
                int main() {
                    print(undeclared);
                    return 0;
                }
            """)
        }
    }
}

