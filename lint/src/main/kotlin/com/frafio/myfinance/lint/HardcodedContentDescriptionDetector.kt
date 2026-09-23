package com.frafio.myfinance.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.skipParenthesizedExprDown

/** A string literal as a content description: English in every locale, and untranslatable. */
class HardcodedContentDescriptionDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UCallExpression::class.java, UBinaryExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
            val method = node.resolve() ?: return
            context.evaluator.computeArgumentMapping(node, method).forEach { (argument, parameter) ->
                if (parameter.name == CONTENT_DESCRIPTION) check(argument)
            }
        }

        // `Modifier.semantics { contentDescription = "…" }`
        override fun visitBinaryExpression(node: UBinaryExpression) {
            if (node.operator != UastBinaryOperator.ASSIGN) return
            val target = when (val left = node.leftOperand.skipParenthesizedExprDown()) {
                is USimpleNameReferenceExpression -> left.identifier
                is UQualifiedReferenceExpression -> (left.selector as? USimpleNameReferenceExpression)?.identifier
                else -> null
            }
            if (target == CONTENT_DESCRIPTION) check(node.rightOperand)
        }

        private fun check(expression: UExpression) {
            val value = expression.skipParenthesizedExprDown()
            if (value !is UInjectionHost) return
            if (value.isInsidePreview()) return
            context.report(
                ISSUE,
                value,
                context.getLocation(value),
                "Use a string resource for `contentDescription`, translated in `values-it`",
            )
        }
    }

    companion object {
        private const val CONTENT_DESCRIPTION = "contentDescription"

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "HardcodedContentDescription",
            briefDescription = "Hard-coded content description",
            explanation = "Content descriptions are what TalkBack reads. A string literal stays English in " +
                "every locale; use `stringResource(R.string.…)` with an Italian translation, or `null` for a " +
                "decorative icon. Previews are exempt.",
            category = Category.A11Y,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(HardcodedContentDescriptionDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
