package com.frafio.myfinance.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class FirebaseOutsideAdapterDetectorTest {

    private fun check(vararg files: TestFile) = lint()
        .allowMissingSdk()
        .issues(FirebaseOutsideAdapterDetector.ISSUE)
        .files(*Stubs.FIREBASE, *files)
        .run()

    @Test
    fun singletonsAndKtxOutsideTheAdapters_areReported() {
        check(
            kotlin(
                """
                package com.frafio.myfinance.features.budget

                import com.google.firebase.Firebase
                import com.google.firebase.auth.FirebaseAuth
                import com.google.firebase.auth.auth
                import com.google.firebase.firestore.FirebaseFirestore
                import com.google.firebase.firestore.firestore

                class BudgetRepository {
                    val store = FirebaseFirestore.getInstance()
                    val identity = FirebaseAuth.getInstance()
                    val ktxStore = Firebase.firestore
                    val ktxIdentity = Firebase.auth
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/frafio/myfinance/features/budget/BudgetRepository.kt:10: Error: Reach Firebase through RemoteDataSource / AuthDataSource, not the SDK singleton [FirebaseOutsideAdapter]
                val store = FirebaseFirestore.getInstance()
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/frafio/myfinance/features/budget/BudgetRepository.kt:11: Error: Reach Firebase through RemoteDataSource / AuthDataSource, not the SDK singleton [FirebaseOutsideAdapter]
                val identity = FirebaseAuth.getInstance()
                               ~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/frafio/myfinance/features/budget/BudgetRepository.kt:12: Error: Reach Firebase through RemoteDataSource / AuthDataSource, not the SDK singleton [FirebaseOutsideAdapter]
                val ktxStore = Firebase.firestore
                                        ~~~~~~~~~
            src/com/frafio/myfinance/features/budget/BudgetRepository.kt:13: Error: Reach Firebase through RemoteDataSource / AuthDataSource, not the SDK singleton [FirebaseOutsideAdapter]
                val ktxIdentity = Firebase.auth
                                           ~~~~
            4 errors
            """,
        )
    }

    @Test
    fun theTwoAdapters_areClean() {
        check(
            kotlin(
                """
                package com.frafio.myfinance.core.data.remote

                import com.google.firebase.firestore.FirebaseFirestore

                class FirestoreRemoteDataSource {
                    private val fStore: FirebaseFirestore
                        get() = FirebaseFirestore.getInstance()
                }
                """,
            ).indented(),
            kotlin(
                """
                package com.frafio.myfinance.core.data.remote

                import com.google.firebase.auth.FirebaseAuth

                class FirebaseAuthDataSource {
                    private val fAuth: FirebaseAuth
                        get() = FirebaseAuth.getInstance()
                }
                """,
            ).indented(),
        ).expectClean()
    }
}
