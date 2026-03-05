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
            "Feature: print literal",
            """
            int main() {
                print(42);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: arithmetic precedence",
            """
            int main() {
                int x = 3 + 4 * 2;
                print(x);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: if/else branch",
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
            "Feature: while countdown",
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
            "Feature: for loop sum",
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
            "Feature: Compound assignment",
            """
            int main() {
                int x = 5;
                x += 3;
                print(x);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: do-while",
            """
            int main() {
                int i = 0;
                do {
                    print(i);
                    i = i + 1;
                } while (i < 3);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: break",
            """
            int main() {
                int i = 0;
                while (1) {
                    if (i == 3) {
                        break;
                    }
                    print(i);
                    i = i + 1;
                }
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: continue",
            """
            int main() {
                for (int i = 0; i < 6; i++) {
                    if (i == 2 || i == 4) {
                        continue;
                    }
                    print(i);
                }
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: switch/case/default",
            """
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
                return 0;
            }
            """.trimIndent()
        ),        CExample(
            "Feature: bitwise ops",
            """
            int main() {
                print(5 & 3);
                print(5 | 3);
                print(5 ^ 3);
                print(~5);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: print vs debugPrint",
            """
            int main() {
                int x = 7;
                print(x);
                debugPrint(x + 1);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: ternary operator",
            """
            int main() {
                int a = 10;
                int b = 4;
                int m = (a > b) ? a : b;
                print(m);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: array indexing",
            """
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
            """.trimIndent()
        ),
        CExample(
            "Feature: array inc/dec",
            """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[1] = 20;
                arr[2] = 30;

                int idx = 1;
                int a = ++arr[idx];
                idx = 2;
                int b = arr[idx]++;

                print(a);
                print(b);
                print(arr[2]);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Feature: multi declaration",
            """
            int main() {
                int a = 1, b = 2, c;
                c = a + b;
                print(c);
                return 0;
            }
            """.trimIndent()
        ),
        CExample(
            "Stress: Calls + do scopes",
            """
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
        )
    )
}


