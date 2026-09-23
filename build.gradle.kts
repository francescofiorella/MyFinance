// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.sonarqube)
}

// SonarCloud analysis, run by CI (docs/testing.md#continuous-integration); the token comes from SONAR_TOKEN.
sonar {
    properties {
        property("sonar.projectKey", "francescofiorella_MyFinance")
        property("sonar.organization", "francescofiorella")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
