package com.hiperbou.vm.ccompilerbin

data class CExample(
    val name: String,
    val source: String
)

object CExamples {
    fun all(): List<CExample> = listOf(
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
