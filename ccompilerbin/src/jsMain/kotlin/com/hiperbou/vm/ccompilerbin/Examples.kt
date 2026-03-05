package com.hiperbou.vm.ccompilerbin

data class CExample(
    val name: String,
    val source: String
)

object CExamples {
    fun all(): List<CExample> = listOf(
        CExample(
            "Max function (x=6, y=4)",
            """
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
            """.trimIndent()
        ),
        CExample(
            "Classic: Fibonacci recursive (fib(8))",
            """
            int fib(int n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
            }

            int main() {
                print(fib(8));
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Classic: Fibonacci (first 8)",
            """
            int main() {
                int a = 0;
                int b = 1;
                int i = 0;
                while (i < 8) {
                    print(a);
                    int next = a + b;
                    a = b;
                    b = next;
                    i = i + 1;
                }
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Classic: Factorial (5!)",
            """
            int main() {
                int n = 5;
                int result = 1;
                while (n > 1) {
                    result = result * n;
                    n = n - 1;
                }
                print(result);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Classic: GCD Euclid (48,18)",
            """
            int gcd(int a, int b) {
                while (b != 0) {
                    int t = b;
                    b = a % b;
                    a = t;
                }
                return a;
            }

            int main() {
                print(gcd(48, 18));
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Classic: Power by loop (2^10)",
            """
            int powi(int base, int exp) {
                int result = 1;
                while (exp > 0) {
                    result = result * base;
                    exp = exp - 1;
                }
                return result;
            }

            int main() {
                print(powi(2, 10));
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Phase 1: Print literal",
            """
            int main() {
                print(42);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Phase 3: Arithmetic precedence",
            """
            int main() {
                int x = 3 + 4 * 2;
                print(x);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Phase 4: If/Else branch",
            """
            int main() {
                int x = 5;
                if (x > 3) {
                    print(1);
                } else {
                    print(0);
                }
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Phase 5: While countdown",
            """
            int main() {
                int i = 3;
                while (i > 0) {
                    print(i);
                    i = i - 1;
                }
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Phase 6: For loop sum",
            """
            int main() {
                int sum = 0;
                for (int i = 0; i < 5; i++) {
                    sum = sum + i;
                }
                print(sum);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Compound assignment",
            """
            int main() {
                int x = 5;
                x += 3;
                print(x);
                return 0;
            }
            """.trimIndent()
        )
    )
}
