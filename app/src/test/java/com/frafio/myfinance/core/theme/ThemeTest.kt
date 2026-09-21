package com.frafio.myfinance.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun darkThemeFalse_dynamicColorFalse_usesLightScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = false, dynamicColor = false) {
                assertColorSchemesEqual(lightScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    fun darkThemeTrue_dynamicColorFalse_usesDarkScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = true, dynamicColor = false) {
                assertColorSchemesEqual(darkScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    fun darkThemeFalse_dynamicColorTrue_usesDynamicLightScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = false, dynamicColor = true) {
                val expected = dynamicLightColorScheme(LocalContext.current)
                assertColorSchemesEqual(expected, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    fun darkThemeTrue_dynamicColorTrue_usesDynamicDarkScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = true, dynamicColor = true) {
                val expected = dynamicDarkColorScheme(LocalContext.current)
                assertColorSchemesEqual(expected, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    @Config(sdk = [30])
    fun dynamicColorTrue_belowApi31_fallsBackToLightScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = false, dynamicColor = true) {
                assertColorSchemesEqual(lightScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    @Config(sdk = [30])
    fun dynamicColorTrue_belowApi31_fallsBackToDarkScheme() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = true, dynamicColor = true) {
                assertColorSchemesEqual(darkScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    fun dynamicColor_defaultsToOff() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = false) {
                assertColorSchemesEqual(lightScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    @Config(qualifiers = "night")
    fun darkTheme_defaultsToDarkWhenTheSystemIsInNightMode() {
        composeTestRule.setContent {
            MyFinanceTheme {
                assertColorSchemesEqual(darkScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    @Config(qualifiers = "notnight")
    fun darkTheme_defaultsToLightWhenTheSystemIsNotInNightMode() {
        composeTestRule.setContent {
            MyFinanceTheme {
                assertColorSchemesEqual(lightScheme, MaterialTheme.colorScheme)
            }
        }
    }

    @Test
    fun typography_isAppTypography() {
        composeTestRule.setContent {
            MyFinanceTheme(darkTheme = false) {
                assertThat(MaterialTheme.typography).isEqualTo(AppTypography)
            }
        }
    }

    // ColorScheme has no equals(); compare every token Theme.kt sets.
    private fun assertColorSchemesEqual(expected: ColorScheme, actual: ColorScheme) {
        assertThat(actual.primary).isEqualTo(expected.primary)
        assertThat(actual.onPrimary).isEqualTo(expected.onPrimary)
        assertThat(actual.primaryContainer).isEqualTo(expected.primaryContainer)
        assertThat(actual.onPrimaryContainer).isEqualTo(expected.onPrimaryContainer)
        assertThat(actual.secondary).isEqualTo(expected.secondary)
        assertThat(actual.onSecondary).isEqualTo(expected.onSecondary)
        assertThat(actual.secondaryContainer).isEqualTo(expected.secondaryContainer)
        assertThat(actual.onSecondaryContainer).isEqualTo(expected.onSecondaryContainer)
        assertThat(actual.tertiary).isEqualTo(expected.tertiary)
        assertThat(actual.onTertiary).isEqualTo(expected.onTertiary)
        assertThat(actual.tertiaryContainer).isEqualTo(expected.tertiaryContainer)
        assertThat(actual.onTertiaryContainer).isEqualTo(expected.onTertiaryContainer)
        assertThat(actual.error).isEqualTo(expected.error)
        assertThat(actual.onError).isEqualTo(expected.onError)
        assertThat(actual.errorContainer).isEqualTo(expected.errorContainer)
        assertThat(actual.onErrorContainer).isEqualTo(expected.onErrorContainer)
        assertThat(actual.background).isEqualTo(expected.background)
        assertThat(actual.onBackground).isEqualTo(expected.onBackground)
        assertThat(actual.surface).isEqualTo(expected.surface)
        assertThat(actual.onSurface).isEqualTo(expected.onSurface)
        assertThat(actual.surfaceVariant).isEqualTo(expected.surfaceVariant)
        assertThat(actual.onSurfaceVariant).isEqualTo(expected.onSurfaceVariant)
        assertThat(actual.outline).isEqualTo(expected.outline)
        assertThat(actual.outlineVariant).isEqualTo(expected.outlineVariant)
        assertThat(actual.scrim).isEqualTo(expected.scrim)
        assertThat(actual.inverseSurface).isEqualTo(expected.inverseSurface)
        assertThat(actual.inverseOnSurface).isEqualTo(expected.inverseOnSurface)
        assertThat(actual.inversePrimary).isEqualTo(expected.inversePrimary)
        assertThat(actual.surfaceDim).isEqualTo(expected.surfaceDim)
        assertThat(actual.surfaceBright).isEqualTo(expected.surfaceBright)
        assertThat(actual.surfaceContainerLowest).isEqualTo(expected.surfaceContainerLowest)
        assertThat(actual.surfaceContainerLow).isEqualTo(expected.surfaceContainerLow)
        assertThat(actual.surfaceContainer).isEqualTo(expected.surfaceContainer)
        assertThat(actual.surfaceContainerHigh).isEqualTo(expected.surfaceContainerHigh)
        assertThat(actual.surfaceContainerHighest).isEqualTo(expected.surfaceContainerHighest)
    }
}
