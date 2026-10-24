import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.ScopedArtifacts
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
}

// MAJOR.MINOR.PATCH from version.properties; the code is MAJOR * 1_000_000 + MINOR * 1_000 + PATCH.
val appVersionName: String = Properties().apply {
    load(providers.fileContents(layout.projectDirectory.file("version.properties")).asText.get().reader())
}.getProperty("versionName").orEmpty()
val appVersionCode: Int = run {
    val (major, minor, patch) = Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(appVersionName)
        ?.destructured?.toList()?.map(String::toInt)
        ?: error("version.properties: versionName must be MAJOR.MINOR.PATCH, was '$appVersionName'")
    require(minor < 1000 && patch < 1000) { "version.properties: MINOR and PATCH must be below 1000, was '$appVersionName'" }
    major * 1_000_000 + minor * 1_000 + patch
}

android {
    namespace = "com.frafio.myfinance"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.frafio.myfinance"
        minSdk = 29
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "com.frafio.myfinance.testing.MyFinanceTestRunner"
    }

    // Release builds are signed only when the key comes from the environment (the release workflow);
    // without it they stay unsigned, as built locally before.
    signingConfigs {
        providers.environmentVariable("RELEASE_KEYSTORE_FILE").orNull?.let { keystore ->
            fun requireEnv(name: String): String = providers.environmentVariable(name).orNull
                ?: error("$name must be set when RELEASE_KEYSTORE_FILE is")
            create("release") {
                storeFile = file(keystore)
                storePassword = requireEnv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = requireEnv("RELEASE_KEY_ALIAS")
                keyPassword = requireEnv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        // Coverage for both suites; only debug, so release stays non-debuggable.
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }

    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }

    // New errors fail lintDebug (docs/testing.md#lint).
    lint {
        abortOnError = true
    }

    // Fakes and Hilt test modules used by both the JVM and the instrumented suite.
    sourceSets {
        getByName("test") { kotlin.directories.add("src/sharedTest/java") }
        getByName("androidTest") { kotlin.directories.add("src/sharedTest/java") }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

baselineProfile {
    mergeIntoMain = true
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

dependencies {
    lintChecks(project(":lint"))
    implementation(project(":charts"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)

    // Navigation 3 and Adaptive
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation3)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)

    // Firebase & Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity)

    // Hilt
    implementation(libs.hilt.android)
    "baselineProfile"(project(":baselineProfile"))
    ksp(libs.hilt.compiler)
    ksp(libs.metadata.jvm)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.accessibility.check)
    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.compose.ui.testManifest)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.truth)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
}

// The adapter tests reach the Firebase emulators on this PC through 127.0.0.1 on the device.
val firebaseEmulatorPortReverses = listOf(8080, 9099).map { port ->
    tasks.register<Exec>("firebaseEmulatorReverse$port") {
        description = "Forwards port $port on the connected device to the Firebase emulator on this PC (adb reverse)."
        executable = androidComponents.sdkComponents.adb.get().asFile.absolutePath
        args("reverse", "tcp:$port", "tcp:$port")
    }
}
val firebaseEmulatorReverse = tasks.register("firebaseEmulatorReverse") {
    description = "Forwards the Firestore (8080) and Auth (9099) emulator ports to the device; runs before connectedDebugAndroidTest."
    dependsOn(firebaseEmulatorPortReverses)
}
tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
    dependsOn(firebaseEmulatorReverse)
}

// AuthCode/FinanceCode resolve their messages from Locale.getDefault() in static init,
// so the test JVM must start with a fixed locale.
tasks.withType<Test>().configureEach {
    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
    defaultCharacterEncoding = "UTF-8"
    // Robolectric loads app classes through its own class loader; JaCoCo must follow them.
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

sonar {
    properties {
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/createDebugCombinedCoverageReport/createDebugCombinedCoverageReport.xml").get().asFile.path,
        )
        property("sonar.androidLint.reportPaths", layout.buildDirectory.file("reports/lint-results-debug.xml").get().asFile.path)
    }
}

// Unit-test and device-test coverage merged into one report for SonarCloud
// Run it after testDebugUnitTest and connectedDebugAndroidTest.
androidComponents.onVariants(androidComponents.selector().withBuildType("debug")) { variant ->
    // Locals only: the configuration cache cannot store lambdas that reach back into this script.
    val exclusions = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*_Hilt*.class", "**/Hilt_*.class", "**/hilt_aggregated_deps/**", "**/dagger/**",
        "**/*_Factory*.class", "**/*_MembersInjector.class", "**/*Module_*.class",
        "**/*_Impl*.class", "**/ComposableSingletons*.class",
    )
    val objectFactory = objects
    val classJars = objectFactory.listProperty(RegularFile::class.java)
    val classDirs = objectFactory.listProperty(Directory::class.java)
    val buildDir = layout.buildDirectory
    val report = tasks.register<JacocoReport>("create${variant.name.replaceFirstChar(Char::titlecase)}CombinedCoverageReport") {
        description = "Merges the unit-test and device-test JaCoCo data of ${variant.name} into one XML/HTML report."
        group = "verification"
        // Reads their coverage files when they run in the same build, without forcing them to run.
        val variantName = variant.name.replaceFirstChar(Char::titlecase)
        mustRunAfter("test${variantName}UnitTest", "connected${variantName}AndroidTest")
        classDirectories.setFrom(
            classJars,
            classDirs.map { dirs -> dirs.map { dir -> objectFactory.fileTree().setDir(dir).exclude(exclusions) } },
        )
        sourceDirectories.setFrom(
            files(
                variant.sources.java?.all?.map { dirs -> dirs.map { it.asFile.path } } ?: provider { emptyList<String>() },
                variant.sources.kotlin?.all?.map { dirs -> dirs.map { it.asFile.path } } ?: provider { emptyList<String>() },
            ),
        )
        executionData.setFrom(
            objectFactory.fileTree().from(buildDir.dir("outputs/unit_test_code_coverage/${variant.name}UnitTest")).include("**/*.exec"),
            objectFactory.fileTree().from(buildDir.dir("outputs/code_coverage/${variant.name}AndroidTest")).include("**/*.ec"),
        )
        reports {
            xml.required = true
            html.required = true
        }
    }
    variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
        .use(report)
        .toGet(ScopedArtifact.CLASSES, { _ -> classJars }, { _ -> classDirs })
}
