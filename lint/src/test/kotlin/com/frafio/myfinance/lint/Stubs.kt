package com.frafio.myfinance.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestFile

/** The slices of Compose, JUnit and Firebase the detectors look at, as source stubs. */
internal object Stubs {

    val COMPOSE_RUNTIME: TestFile = kotlin(
        """
        package androidx.compose.runtime

        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.PROPERTY_GETTER)
        annotation class Composable

        @Target(AnnotationTarget.TYPE)
        annotation class DisallowComposableCalls

        @Composable
        inline fun <T> remember(calculation: @DisallowComposableCalls () -> T): T = calculation()

        @Composable
        fun LaunchedEffect(key1: Any?, block: suspend () -> Unit) {}
        """,
    ).indented()

    val COMPOSE_UI: TestFile = kotlin(
        """
        package androidx.compose.material3

        import androidx.compose.runtime.Composable

        @Composable
        fun Icon(painter: Any, contentDescription: String?) {}

        @Composable
        fun Column(content: @Composable () -> Unit) {}

        @Composable
        fun Button(onClick: () -> Unit, content: @Composable () -> Unit) {}
        """,
    ).indented()

    val SEMANTICS: TestFile = kotlin(
        """
        package androidx.compose.ui.semantics

        class SemanticsPropertyReceiver

        var SemanticsPropertyReceiver.contentDescription: String
            get() = ""
            set(value) {}

        fun semantics(properties: SemanticsPropertyReceiver.() -> Unit) {}
        """,
    ).indented()

    val PREVIEW: TestFile = kotlin(
        """
        package androidx.compose.ui.tooling.preview

        annotation class Preview
        """,
    ).indented()

    val JUNIT: TestFile = kotlin(
        """
        package org.junit

        annotation class Test
        """,
    ).indented()

    val FIREBASE: Array<TestFile> = arrayOf(
        java(
            """
            package com.google.firebase.firestore;

            public class FirebaseFirestore {
                public static FirebaseFirestore getInstance() { throw new UnsupportedOperationException(); }
            }
            """,
        ).indented(),
        java(
            """
            package com.google.firebase.auth;

            public class FirebaseAuth {
                public static FirebaseAuth getInstance() { throw new UnsupportedOperationException(); }
            }
            """,
        ).indented(),
        kotlin(
            """
            package com.google.firebase

            object Firebase
            """,
        ).indented(),
        kotlin(
            "src/com/google/firebase/firestore/Firestore.kt",
            """
            package com.google.firebase.firestore

            import com.google.firebase.Firebase

            val Firebase.firestore: FirebaseFirestore
                get() = throw UnsupportedOperationException()
            """,
        ).indented(),
        kotlin(
            "src/com/google/firebase/auth/Auth.kt",
            """
            package com.google.firebase.auth

            import com.google.firebase.Firebase

            val Firebase.auth: FirebaseAuth
                get() = throw UnsupportedOperationException()
            """,
        ).indented(),
    )
}
