package com.frafio.myfinance.lint

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import java.util.EnumSet

/**
 * `@Test` functions do not start with `test` (docs/testing.md, Naming). Ported from Now in Android
 * (Apache 2.0, `lint/.../TestMethodNameDetector.kt`, its prefix half); fixture builders such as
 * `testExpense()` are not `@Test` functions and are left alone.
 */
class TestMethodPrefixDetector : Detector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> = listOf("org.junit.Test")

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {
        val method = usageInfo.referenced as? PsiMethod ?: return
        if (!method.name.startsWith("test")) return
        context.report(
            issue = ISSUE,
            scope = usageInfo.usage,
            location = context.getNameLocation(method),
            message = "Test method starts with `test`",
            quickfixData = LintFix.create()
                .name("Remove prefix")
                .replace().pattern("""test[\s_]*""")
                .with("")
                .autoFix()
                .build(),
        )
    }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "TestMethodPrefix",
            briefDescription = "Test method starts with `test`",
            explanation = "`@Test` already marks a test; name it `subject_condition_expectation` or as a short sentence.",
            category = Category.TESTING,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                TestMethodPrefixDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
            ),
        )
    }
}
