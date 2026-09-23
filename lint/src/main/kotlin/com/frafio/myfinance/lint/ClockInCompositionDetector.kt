package com.frafio.myfinance.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod

/**
 * Reading the clock while composing makes the UI depend on when it is drawn, so screenshots and
 * previews drift day by day. Composables take `today` from their caller instead.
 */
class ClockInCompositionDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("now", "currentTimeMillis", "getInstance")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val owner = method.containingClass?.qualifiedName ?: return
        val readsClock = when (method.name) {
            "now" -> owner in CLOCK_CLASSES
            "currentTimeMillis" -> owner == "java.lang.System"
            "getInstance" -> owner == "java.util.Calendar"
            else -> false
        }
        if (!readsClock || !node.isEvaluatedDuringComposition()) return
        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Composables must not read the clock; take the date as a parameter",
        )
    }

    /**
     * Walks out to the nearest function: a composable body or default argument counts; so does a
     * `@Composable` content lambda. An effect or callback lambda stops the walk, since it runs later.
     */
    private fun UElement.isEvaluatedDuringComposition(): Boolean {
        var element: UElement? = uastParent
        while (element != null) {
            when (element) {
                is ULambdaExpression -> when (element.kind()) {
                    LambdaKind.COMPOSABLE -> return !element.isInsidePreview()
                    LambdaKind.INLINE -> Unit
                    LambdaKind.DEFERRED -> return false
                }
                is UMethod -> return element.isComposable() && !element.isPreview()
            }
            element = element.uastParent
        }
        return false
    }

    companion object {
        private val CLOCK_CLASSES = setOf(
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.YearMonth",
            "java.time.Year",
            "java.time.Instant",
            "java.time.ZonedDateTime",
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "ClockInComposition",
            briefDescription = "Clock read during composition",
            explanation = "A composable that calls `LocalDate.now()` (or another clock) renders differently " +
                "every day, which breaks screenshot tests and previews. Read the clock in the ViewModel or " +
                "the caller and pass the value in. Effects and callbacks, which run later, are fine.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(ClockInCompositionDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
