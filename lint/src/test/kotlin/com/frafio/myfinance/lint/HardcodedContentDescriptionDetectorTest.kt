package com.frafio.myfinance.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class HardcodedContentDescriptionDetectorTest {

    private fun check(source: String) = lint()
        .allowMissingSdk()
        .issues(HardcodedContentDescriptionDetector.ISSUE)
        .files(Stubs.COMPOSE_RUNTIME, Stubs.COMPOSE_UI, Stubs.SEMANTICS, Stubs.PREVIEW, kotlin(source).indented())
        .run()

    @Test
    fun literals_namedPositionalTemplateAndSemantics_areReported() {
        check(
            """
            package test

            import androidx.compose.material3.Icon
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.semantics.contentDescription
            import androidx.compose.ui.semantics.semantics

            @Composable
            fun Screen(name: String) {
                Icon(painter = 0, contentDescription = "Close")
                Icon(0, "Close")
                Icon(0, contentDescription = "Remove ${'$'}name")
                semantics { contentDescription = "Chart" }
            }
            """,
        ).expect(
            """
            src/test/test.kt:10: Error: Use a string resource for contentDescription, translated in values-it [HardcodedContentDescription]
                Icon(painter = 0, contentDescription = "Close")
                                                       ~~~~~~~
            src/test/test.kt:11: Error: Use a string resource for contentDescription, translated in values-it [HardcodedContentDescription]
                Icon(0, "Close")
                        ~~~~~~~
            src/test/test.kt:12: Error: Use a string resource for contentDescription, translated in values-it [HardcodedContentDescription]
                Icon(0, contentDescription = "Remove ＄name")
                                             ~~~~~~~~~~~~~~
            src/test/test.kt:13: Error: Use a string resource for contentDescription, translated in values-it [HardcodedContentDescription]
                semantics { contentDescription = "Chart" }
                                                 ~~~~~~~
            4 errors
            """,
        )
    }

    @Test
    fun resourcesVariablesNullAndPreviews_areClean() {
        check(
            """
            package test

            import androidx.compose.material3.Icon
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.tooling.preview.Preview

            @Composable
            fun Screen(label: String) {
                Icon(0, contentDescription = label)
                Icon(0, contentDescription = null)
            }

            @Preview
            @Composable
            fun ScreenPreview() {
                Icon(0, contentDescription = "Avatar")
            }
            """,
        ).expectClean()
    }
}
