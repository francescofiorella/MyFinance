package com.frafio.myfinance.baselineProfile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame timing for the expenses list, with and without the Baseline Profile. Compare P95 / P99
 * of `frameDurationCpuMs` between the two compilation modes; the median barely moves.
 * See README.md for the command and reference numbers.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun expensesScrollCompilationNone() =
        expensesScroll(CompilationMode.None())

    @Test
    fun expensesScrollCompilationBaselineProfiles() =
        expensesScroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun expensesScroll(compilationMode: CompilationMode) = scrollBenchmark(
        compilationMode = compilationMode,
        setUp = { openTab("tab_expenses", "expenses_list") },
        scroll = {
            await("expenses_list", 10_000)?.let { list ->
                list.setGestureMargin(displayWidth / 10)
                list.fling(Direction.DOWN)
                settle()
                list.fling(Direction.UP)
            }
        }
    )

    private fun scrollBenchmark(
        compilationMode: CompilationMode,
        setUp: UiDevice.() -> Unit,
        scroll: UiDevice.() -> Unit
    ) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                signIn()
                device.setUp()
                device.settle()
            },
            measureBlock = {
                device.scroll()
            }
        )
    }
}
