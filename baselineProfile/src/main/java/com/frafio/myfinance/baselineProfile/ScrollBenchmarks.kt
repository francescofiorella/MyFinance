package com.frafio.myfinance.baselineProfile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
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
 * Measures scroll smoothness with and without the Baseline Profile.
 *
 * [StartupBenchmarks] only covers app launch, so it cannot tell you whether the journeys in
 * [BaselineProfileGenerator.journeysSignedIn] are worth their cost. This class can: it compares
 * frame durations under [CompilationMode.None] against [CompilationMode.Partial] with the profile
 * required, which is exactly the difference a Baseline Profile makes to scrolling.
 *
 * Run it after generating a profile, on a physical device:
 * ```
 * ./gradlew :baselineProfile:connectedBenchmarkReleaseAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.frafio.myfinance.baselineProfile.ScrollBenchmarks
 * ```
 *
 * Compare `frameDurationCpuMs` P50 and P95 between the two compilation modes. The
 * `CompilationBaselineProfiles` variants should show lower numbers, most visibly at P95 - first
 * scroll jank is what the profile removes.
 *
 * The device must already have a signed-in session; these benchmarks do not sign in, because
 * doing so inside the measured loop would drown the frame metrics in network latency. Run
 * [BaselineProfileGenerator] first, or sign in by hand.
 *
 * Everything here is read-only - it scrolls and nothing else.
 **/
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

    @Test
    fun dashboardScrollCompilationNone() =
        dashboardScroll(CompilationMode.None())

    @Test
    fun dashboardScrollCompilationBaselineProfiles() =
        dashboardScroll(CompilationMode.Partial(BaselineProfileMode.Require))

    /** Flings the expenses list, where item composition dominates the frame cost. */
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

    /** Scrolls the dashboard, where the chart Canvas draw calls dominate instead. */
    private fun dashboardScroll(compilationMode: CompilationMode) = scrollBenchmark(
        compilationMode = compilationMode,
        setUp = { awaitTag("dashboard_scroll", 15_000) },
        scroll = {
            await("dashboard_scroll", 10_000)?.let { content ->
                content.setGestureMargin(displayWidth / 10)
                content.scroll(Direction.DOWN, 1.0f)
                settle()
                content.scroll(Direction.UP, 1.0f)
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
            // WARM kills the process before each iteration, so setupBlock re-launches and
            // navigates while the measurement only covers the scroll itself.
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                requireSignedIn()
                device.setUp()
                device.settle()
            },
            measureBlock = {
                device.scroll()
            }
        )
    }

    private fun MacrobenchmarkScope.requireSignedIn() {
        check(device.awaitTag("tab_dashboard", 30_000)) {
            "No signed-in session on this device. Run BaselineProfileGenerator first, or sign " +
                "in by hand - these benchmarks deliberately do not sign in, because the network " +
                "round trip would distort the frame metrics."
        }
    }
}
