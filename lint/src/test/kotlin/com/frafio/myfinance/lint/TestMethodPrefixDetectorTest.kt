package com.frafio.myfinance.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class TestMethodPrefixDetectorTest {

    @Test
    fun testPrefixedTests_areReported_withAQuickFix() {
        lint()
            .allowMissingSdk()
            .issues(TestMethodPrefixDetector.ISSUE)
            .files(
                Stubs.JUNIT,
                kotlin(
                    "src/test/kotlin/test/SampleTest.kt",
                    """
                    package test

                    import org.junit.Test

                    class SampleTest {
                        @Test
                        fun foo() = Unit

                        @Test
                        fun test_foo() = Unit

                        @Test
                        fun testFoo() = Unit

                        // A fixture builder, not a test.
                        fun testExpense() = Unit
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/kotlin/test/SampleTest.kt:10: Warning: Test method starts with test [TestMethodPrefix]
                    fun test_foo() = Unit
                        ~~~~~~~~
                src/test/kotlin/test/SampleTest.kt:13: Warning: Test method starts with test [TestMethodPrefix]
                    fun testFoo() = Unit
                        ~~~~~~~
                0 errors, 2 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/test/kotlin/test/SampleTest.kt line 10: Remove prefix:
                @@ -10 +10
                -     fun test_foo() = Unit
                +     fun foo() = Unit
                Autofix for src/test/kotlin/test/SampleTest.kt line 13: Remove prefix:
                @@ -13 +13
                -     fun testFoo() = Unit
                +     fun Foo() = Unit
                """,
            )
    }
}
