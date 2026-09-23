package com.frafio.myfinance.features.profile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class ProfileScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val emailUser = testUser(fullName = "Ada Lovelace", hasPassword = true)
    private val googleUser = testUser(fullName = "Ada Lovelace", provider = User.GOOGLE_PROVIDER, providers = listOf("google.com"), hasPassword = false, isGoogleLinked = true)

    @Test
    fun emailAccount() {
        composeTestRule.captureMultiDevice("ProfileScreenEmailAccount") { Profile(emailUser) }
    }

    @Test
    fun emailAccount_dark() {
        composeTestRule.capturePhoneDark("ProfileScreenEmailAccount") { Profile(emailUser) }
    }

    @Test
    fun googleAccount() {
        composeTestRule.captureMultiDevice("ProfileScreenGoogleAccount") { Profile(googleUser) }
    }

    @Composable
    private fun Profile(user: User) {
        ProfileContent(
            user = user,
            profilePicture = null,
            proPicChoice = null,
            versionName = "MyFinance 5.0.3",
            isDynamicColorAvailable = true,
            isDynamicColorChecked = false,
            scrollState = rememberScrollState(),
            onSelectProPic = {},
            onEditFullName = {},
            onDynamicColorChanged = {},
            onManageLabels = {},
            onCategoriesDescriptionClick = {},
            onSelectCurrency = {},
            onChangePasswordClick = {},
            currencyCode = "EUR",
        )
    }
}
