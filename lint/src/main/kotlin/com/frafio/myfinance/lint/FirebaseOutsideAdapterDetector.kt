package com.frafio.myfinance.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.getParentOfType

/**
 * Firestore and Firebase Auth are reached only through `RemoteDataSource` and `AuthDataSource`, so
 * everything else runs over the fakes in tests. Only the two adapters may take the SDK singletons.
 */
class FirebaseOutsideAdapterDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("getInstance", "getFirestore", "getAuth")

    // Kotlin reads the KTX `Firebase.firestore` / `Firebase.auth` as property references.
    override fun getApplicableReferenceNames(): List<String> = listOf("firestore", "auth")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.isFirebaseEntryPoint()) report(context, node)
    }

    override fun visitReference(context: JavaContext, reference: UReferenceExpression, referenced: PsiElement) {
        val isEntryPoint = when (referenced) {
            is PsiMethod -> referenced.isFirebaseEntryPoint()
            // Resolved to Kotlin source rather than to the compiled getter.
            is KtProperty -> referenced.fqName?.asString() in KTX_PROPERTIES
            else -> false
        }
        if (isEntryPoint) report(context, reference)
    }

    private fun PsiMethod.isFirebaseEntryPoint(): Boolean {
        val owner = containingClass?.qualifiedName ?: return false
        return when (name) {
            "getInstance" -> owner in SINGLETONS
            "getFirestore", "getAuth" -> owner.startsWith("com.google.firebase.")
            else -> false
        }
    }

    private fun report(context: JavaContext, element: UElement) {
        val owner = element.getParentOfType(UClass::class.java, true)?.qualifiedName
        if (owner in ADAPTERS) return
        context.report(
            ISSUE,
            element,
            context.getLocation(element),
            "Reach Firebase through `RemoteDataSource` / `AuthDataSource`, not the SDK singleton",
        )
    }

    companion object {
        private val SINGLETONS = setOf(
            "com.google.firebase.firestore.FirebaseFirestore",
            "com.google.firebase.auth.FirebaseAuth",
        )
        private val KTX_PROPERTIES = setOf("com.google.firebase.firestore.firestore", "com.google.firebase.auth.auth")
        private val ADAPTERS = setOf(
            "com.frafio.myfinance.core.data.remote.FirestoreRemoteDataSource",
            "com.frafio.myfinance.core.data.remote.FirebaseAuthDataSource",
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "FirebaseOutsideAdapter",
            briefDescription = "Firebase used outside its adapter",
            explanation = "Only `FirestoreRemoteDataSource` and `FirebaseAuthDataSource` may call " +
                "`FirebaseFirestore.getInstance()`, `FirebaseAuth.getInstance()` or the KTX `Firebase.firestore` / " +
                "`Firebase.auth`. Everything else depends on the `RemoteDataSource` / `AuthDataSource` " +
                "interfaces, which the tests replace with fakes.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(FirebaseOutsideAdapterDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
