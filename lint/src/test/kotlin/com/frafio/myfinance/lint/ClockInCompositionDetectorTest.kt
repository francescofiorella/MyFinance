package com.frafio.myfinance.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ClockInCompositionDetectorTest {

    private fun check(source: String) = lint()
        .allowMissingSdk()
        .issues(ClockInCompositionDetector.ISSUE)
        .files(Stubs.COMPOSE_RUNTIME, Stubs.COMPOSE_UI, Stubs.PREVIEW, kotlin(source).indented())
        .run()

    @Test
    fun clockReadDuringComposition_isReported() {
        check(
            """
            package test

            import androidx.compose.material3.Column
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import java.time.LocalDate

            @Composable
            fun Body() {
                val today = LocalDate.now()
            }

            @Composable
            fun DefaultArgument(today: LocalDate = LocalDate.now()) {}

            @Composable
            fun ContentLambda() {
                Column { val today = LocalDate.now() }
            }

            @Composable
            fun Remembered() {
                val today = remember { LocalDate.now() }
            }

            @Composable
            fun Millis() {
                val now = System.currentTimeMillis()
            }
            """,
        ).expect(
            """
            src/test/test.kt:10: Error: Composables must not read the clock; take the date as a parameter [ClockInComposition]
                val today = LocalDate.now()
                            ~~~~~~~~~~~~~~~
            src/test/test.kt:14: Error: Composables must not read the clock; take the date as a parameter [ClockInComposition]
            fun DefaultArgument(today: LocalDate = LocalDate.now()) {}
                                                   ~~~~~~~~~~~~~~~
            src/test/test.kt:18: Error: Composables must not read the clock; take the date as a parameter [ClockInComposition]
                Column { val today = LocalDate.now() }
                                     ~~~~~~~~~~~~~~~
            src/test/test.kt:23: Error: Composables must not read the clock; take the date as a parameter [ClockInComposition]
                val today = remember { LocalDate.now() }
                                       ~~~~~~~~~~~~~~~
            src/test/test.kt:28: Error: Composables must not read the clock; take the date as a parameter [ClockInComposition]
                val now = System.currentTimeMillis()
                          ~~~~~~~~~~~~~~~~~~~~~~~~~~
            5 errors
            """,
        )
    }

    @Test
    fun effectsCallbacksPlainCodeAndPreviews_areClean() {
        check(
            """
            package test

            import androidx.compose.material3.Button
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.ui.tooling.preview.Preview
            import java.time.LocalDate

            @Composable
            fun Effect() {
                LaunchedEffect(Unit) { val today = LocalDate.now() }
            }

            @Composable
            fun Callback() {
                Button(onClick = { val today = LocalDate.now() }) {}
            }

            @Composable
            fun FixedDate() {
                val date = LocalDate.of(2024, 5, 29)
            }

            fun notComposable(): LocalDate = LocalDate.now()

            class ViewModelLike {
                val today: LocalDate = LocalDate.now()
            }

            @Preview
            @Composable
            fun BodyPreview() {
                val today = LocalDate.now()
            }
            """,
        ).expectClean()
    }
}
