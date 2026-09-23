package com.frafio.myfinance.lint

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType

internal const val COMPOSABLE = "androidx.compose.runtime.Composable"
internal const val DISALLOW_COMPOSABLE_CALLS = "androidx.compose.runtime.DisallowComposableCalls"

internal fun UMethod.isComposable(): Boolean = findAnnotation(COMPOSABLE) != null

/** `@Preview` or a multipreview annotation (by convention named `…Preview`/`…Previews`). */
internal fun UMethod.isPreview(): Boolean =
    uAnnotations.any { annotation -> annotation.qualifiedName?.substringAfterLast('.')?.contains("Preview") == true }

internal fun UElement.isInsidePreview(): Boolean {
    var method = getParentOfType(UMethod::class.java, false)
    while (method != null) {
        if (method.isPreview()) return true
        method = method.getParentOfType(UMethod::class.java, true)
    }
    return false
}

internal enum class LambdaKind {
    /** Its body runs as part of composition: a `@Composable` content lambda. */
    COMPOSABLE,

    /** Runs inline wherever its caller runs, e.g. `remember { … }` (`@DisallowComposableCalls`). */
    INLINE,

    /** Runs later or elsewhere: effects, callbacks, stored lambdas. */
    DEFERRED,
}

/** Classifies a lambda by the annotations on the function type it is passed as or assigned to. */
internal fun ULambdaExpression.kind(): LambdaKind {
    val lambda = sourcePsi as? KtLambdaExpression ?: return LambdaKind.DEFERRED
    val annotations = analyze(lambda) {
        val type = lambda.expectedType ?: return@analyze emptyList()
        type.annotations.mapNotNull { it.classId?.asSingleFqName()?.asString() }
    }
    return when {
        COMPOSABLE in annotations -> LambdaKind.COMPOSABLE
        DISALLOW_COMPOSABLE_CALLS in annotations -> LambdaKind.INLINE
        else -> LambdaKind.DEFERRED
    }
}
