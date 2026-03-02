package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.CPU
import com.hiperbou.vm.CPUStack
import com.hiperbou.vm.decoder.Decoder
import com.hiperbou.vm.decoder.ExceptionDecoder
import com.hiperbou.vm.plugin.print.PrintInstructions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Integration tests: compile C-like source → run in VM → assert printed output.
 *
 * Uses a [CapturingPrintDecoder] instead of the standard [PrintDecoder] so we can
 * assert on printed values without relying on stdout.
 */
class CCompilerTest {

    // -------------------------------------------------------------------------
    // Test helper: capturing print decoder
    // -------------------------------------------------------------------------

    /**
     * A [Decoder] that captures values passed to PRINT into a list,
     * instead of writing to stdout. Also pops the value (matching the compiler's
     * PRINT + POP pattern — the POP is emitted by the compiler, not this decoder).
     */
    private class CapturingPrintDecoder(
        private val stack: CPUStack<Int>,
        private var nextDecoder: Decoder = ExceptionDecoder.instance
    ) : Decoder {
        val output = mutableListOf<Int>()

        override fun decodeInstruction(instruction: Int) {
            when (instruction) {
                PrintInstructions.PRINT -> {
                    // VM's PRINT uses peek() — value stays on stack; compiler emits POP after
                    val n = stack.peek()
                    output.add(n)
                }
                PrintInstructions.DEBUG_PRINT -> {
                    val n = stack.peek()
                    output.add(n)
                }
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
    // Phase 1: minimal main + print + return
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

    @Test
    fun `phase1 - print multiple values`() {
        val output = compileAndRun("""
            int main() {
                print(1);
                print(2);
                print(3);
                return 0;
            }
        """)
        assertEquals(listOf(1, 2, 3), output)
    }

    @Test
    fun `phase1 - return 0 halts cleanly`() {
        val output = compileAndRun("""
            int main() {
                return 0;
            }
        """)
        assertEquals(emptyList(), output)
    }

    // -------------------------------------------------------------------------
    // Phase 2: local variables
    // -------------------------------------------------------------------------

    @Test
    fun `phase2 - local variable`() {
        val output = compileAndRun("""
            int main() {
                int x = 10;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(10), output)
    }

    @Test
    fun `phase2 - multiple local variables`() {
        val output = compileAndRun("""
            int main() {
                int a = 1;
                int b = 2;
                print(a);
                print(b);
                return 0;
            }
        """)
        assertEquals(listOf(1, 2), output)
    }

    @Test
    fun `phase2 - uninitialized variable defaults to 0`() {
        val output = compileAndRun("""
            int main() {
                int x;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(0), output)
    }

    // -------------------------------------------------------------------------
    // Phase 3: arithmetic
    // -------------------------------------------------------------------------

    @Test
    fun `phase3 - addition`() {
        val output = compileAndRun("""
            int main() {
                int x = 3 + 4;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(7), output)
    }

    @Test
    fun `phase3 - operator precedence`() {
        val output = compileAndRun("""
            int main() {
                int x = 3 + 4 * 2;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(11), output)
    }

    @Test
    fun `phase3 - parentheses override precedence`() {
        val output = compileAndRun("""
            int main() {
                int x = (3 + 4) * 2;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(14), output)
    }

    @Test
    fun `phase3 - subtraction`() {
        val output = compileAndRun("""
            int main() {
                int x = 10 - 3;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(7), output)
    }

    @Test
    fun `phase3 - division`() {
        val output = compileAndRun("""
            int main() {
                int x = 10 / 2;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(5), output)
    }

    @Test
    fun `phase3 - modulo`() {
        val output = compileAndRun("""
            int main() {
                int x = 10 % 3;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(1), output)
    }

    @Test
    fun `phase3 - unary minus`() {
        val output = compileAndRun("""
            int main() {
                int x = -5;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(-5), output)
    }

    // -------------------------------------------------------------------------
    // Phase 4: if/else
    // -------------------------------------------------------------------------

    @Test
    fun `phase4 - if true branch taken`() {
        val output = compileAndRun("""
            int main() {
                int x = 5;
                if (x > 3) {
                    print(1);
                } else {
                    print(0);
                }
                return 0;
            }
        """)
        assertEquals(listOf(1), output)
    }

    @Test
    fun `phase4 - if false branch taken`() {
        val output = compileAndRun("""
            int main() {
                int x = 1;
                if (x > 3) {
                    print(1);
                } else {
                    print(0);
                }
                return 0;
            }
        """)
        assertEquals(listOf(0), output)
    }

    @Test
    fun `phase4 - if without else, condition false`() {
        val output = compileAndRun("""
            int main() {
                int x = 1;
                if (x > 3) {
                    print(99);
                }
                print(0);
                return 0;
            }
        """)
        assertEquals(listOf(0), output)
    }

    // -------------------------------------------------------------------------
    // Phase 5: while loop
    // -------------------------------------------------------------------------

    @Test
    fun `phase5 - while loop counts down`() {
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
    fun `phase5 - while loop body not entered when condition false`() {
        val output = compileAndRun("""
            int main() {
                int i = 0;
                while (i > 0) {
                    print(99);
                }
                print(0);
                return 0;
            }
        """)
        assertEquals(listOf(0), output)
    }

    // -------------------------------------------------------------------------
    // Phase 6: for loop
    // -------------------------------------------------------------------------

    @Test
    fun `phase6 - for loop sum`() {
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
        assertEquals(listOf(10), output)  // 0+1+2+3+4 = 10
    }

    @Test
    fun `phase6 - for loop prints each iteration`() {
        val output = compileAndRun("""
            int main() {
                for (int i = 0; i < 3; i++) {
                    print(i);
                }
                return 0;
            }
        """)
        assertEquals(listOf(0, 1, 2), output)
    }

    // -------------------------------------------------------------------------
    // Compound assignment
    // -------------------------------------------------------------------------

    @Test
    fun `compound assignment plus-eq`() {
        val output = compileAndRun("""
            int main() {
                int x = 5;
                x += 3;
                print(x);
                return 0;
            }
        """)
        assertEquals(listOf(8), output)
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

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
