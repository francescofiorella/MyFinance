import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Generic Compose charts (bar chart, line chart, pie chart, share bar) with no dependency on the app.
plugins {
    jacoco
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.frafio.charts"
    compileSdk = 37

    defaultConfig {
        minSdk = 29
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }

    lint {
        abortOnError = true
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    lintChecks(project(":lint"))

    // Compose types (Modifier, Dp, Color, AnimationSpec) are part of the public API.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.testManifest)
}

// Robolectric loads the classes through its own class loader; JaCoCo must follow them.
tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// A plugin resets Gradle's JaCoCo to its bundled default in its own afterEvaluate; this one runs later.
afterEvaluate {
    jacoco {
        toolVersion = libs.versions.jacoco.get()
    }
}

// The module's unit-test coverage (createDebugUnitTestCoverageReport) and lint report for SonarCloud.
sonar {
    properties {
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/coverage/test/debug/report.xml").get().asFile.path,
        )
        property("sonar.androidLint.reportPaths", layout.buildDirectory.file("reports/lint-results-debug.xml").get().asFile.path)
    }
}
