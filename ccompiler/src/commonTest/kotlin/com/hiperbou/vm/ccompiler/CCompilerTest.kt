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
        val bytecode = CCompiler.compile(source)
        val cpu = CPU(bytecode)
        val printDecoder = CapturingPrintDecoder(cpu.getStack())
        cpu.appendDecoder(printDecoder)
        cpu.run()
        return printDecoder.output
    }

    // -------------------------------------------------------------------------
    // 1) main + print literal
    // -------------------------------------------------------------------------

    @Test
    fun `phase1 - print integer literal`() {
        val output = compileAndRun("""
            int main() {
                print(42);
                return 0;
            }
        """)
        assertEquals(listOf(42), output)
    }

    // -------------------------------------------------------------------------
    // 2) local variables
    // -------------------------------------------------------------------------

    @Test
    fun `phase2 - local variable and default zero`() {
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

    // -------------------------------------------------------------------------
    // 3) arithmetic
    // -------------------------------------------------------------------------

    @Test
    fun `phase3 - arithmetic operators`() {
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

    // -------------------------------------------------------------------------
    // 4) if / else
    // -------------------------------------------------------------------------

    @Test
    fun `phase4 - if else branches`() {
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

    // -------------------------------------------------------------------------
    // 5) while
    // -------------------------------------------------------------------------

    @Test
    fun `phase5 - while loop`() {
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

    // -------------------------------------------------------------------------
    // 6) for
    // -------------------------------------------------------------------------

    @Test
    fun `phase6 - for loop`() {
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

    // -------------------------------------------------------------------------
    // 7) user functions + return
    // -------------------------------------------------------------------------

    @Test
    fun `phase7 - function call and return`() {
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

    // -------------------------------------------------------------------------
    // 8) global variables
    // -------------------------------------------------------------------------

    @Test
    fun `phase8 - global variable read and write`() {
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

    // -------------------------------------------------------------------------
    // 9) comparison + logical operators
    // -------------------------------------------------------------------------

    @Test
    fun `phase9 - comparison and logical operators`() {
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

    // -------------------------------------------------------------------------
    // 10) compound assignment
    // -------------------------------------------------------------------------

    @Test
    fun `phase10 - compound assignment plus and minus`() {
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

    // -------------------------------------------------------------------------
    // 11) ++ / --
    // -------------------------------------------------------------------------

    @Test
    fun `phase11 - pre and post inc dec`() {
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
    fun `do scope and shadowing`() {
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
    fun `stress - deep calls with do scopes and shadowing`() {
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
    fun `undefined variable throws CodeGenException`() {
        assertFailsWith<com.hiperbou.vm.ccompiler.codegen.CodeGenException> {
            CCompiler.compile("""
                int main() {
                    print(undeclared);
                    return 0;
                }
            """)
        }
    }
}
