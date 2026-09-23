import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// MyFinance's own lint rules, run on :app through lintChecks (docs/testing.md#lint).
plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.lint.api)
    testImplementation(libs.junit)
    testImplementation(libs.lint.checks)
    testImplementation(libs.lint.tests)
}
