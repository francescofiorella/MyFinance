package com.frafio.myfinance.baselineProfile

import android.content.Intent
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-start timing, plain launch and via the ADD_EXPENSE shortcut, with and without the
 * Baseline Profile. Compare median `timeToInitialDisplayMs` between the two compilation modes.
 * See README.md for the command and reference numbers.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() =
        benchmark(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfiles() =
        benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

    // Short names on purpose: the output path was hitting Windows' 260-char limit.
    @Test
    fun shortcutCompilationNone() =
        benchmark(CompilationMode.None(), shortcutAction = "com.frafio.myfinance.ADD_EXPENSE")

    @Test
    fun shortcutCompilationBaselineProfiles() =
        benchmark(CompilationMode.Partial(BaselineProfileMode.Require), shortcutAction = "com.frafio.myfinance.ADD_EXPENSE")

    private fun benchmark(
        compilationMode: CompilationMode,
        shortcutAction: String? = null
    ) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                // For COLD the harness kills the process after this block, so the launch here
                // never reaches the measured one.
                pressHome()
                startActivityAndWait()
                signIn()
                pressHome()
            },
            measureBlock = {
                if (shortcutAction != null) {
                    startActivityAndWait(
                        Intent().apply {
                            action = shortcutAction
                            `package` = PACKAGE_NAME
                        }
                    )
                } else {
                    startActivityAndWait()
                }
            }
        )
    }
}
