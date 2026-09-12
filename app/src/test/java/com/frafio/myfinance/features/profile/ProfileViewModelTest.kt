package com.frafio.myfinance.features.profile

import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.data.testPreferences
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = TestUserRepository()
    private val expensesRepository = TestExpensesRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private val profileImageStorage = TestProfileImageStorage()
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        userRepository.setUser(testUser(fullName = "Ada Lovelace"))
        viewModel = ProfileViewModel(
            userRepository,
            expensesRepository,
            userPreferencesRepository,
            loadingRepository,
            profileImageStorage,
        )
    }

    @Test
    fun user_isInitialisedFromTheCurrentUser() {
        assertThat(viewModel.user.value).isEqualTo(testUser(fullName = "Ada Lovelace"))
    }

    @Test
    fun user_followsTheRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.user.collect() }

        userRepository.setUser(testUser(fullName = "Grace"))

        assertThat(viewModel.user.value?.fullName).isEqualTo("Grace")
    }

    @Test
    fun profilePicture_startsFromTheStorageAndFollowsTheRepository() = runTest {
        assertThat(viewModel.profilePicture.value).isNull()

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.profilePicture.collect() }
        assertThat(viewModel.profilePicture.value).isNull()
    }

    @Test
    fun userPreferences_isInitialisedFromTheRepository() {
        assertThat(viewModel.userPreferences.value).isEqualTo(testPreferences())
    }

    @Test
    fun isSwitchDynamicColorChecked_startsFromPreferences() {
        // Otherwise the switch renders off for a frame and flips on once the flow is collected.
        assertThat(userPreferencesRepository.userPreferencesFlow.value.dynamicColor).isTrue()
        assertThat(viewModel.isSwitchDynamicColorChecked.value).isTrue()
    }

    @Test
    fun isSwitchDynamicColorChecked_followsPreferencesOnceCollected() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.userPreferences.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isSwitchDynamicColorChecked.collect() }

        assertThat(viewModel.isSwitchDynamicColorChecked.value).isTrue()

        userPreferencesRepository.setPreferences(testPreferences(dynamicColor = false))
        assertThat(viewModel.isSwitchDynamicColorChecked.value).isFalse()
    }

    @Test
    fun editFullName_trimsAndEmitsThePreviousName() = runTest {
        val events = collectEvents()

        viewModel.editFullName("  Grace Hopper  ")

        assertThat(userRepository.fullNameUpdates).containsExactly("Grace Hopper")
        assertThat(events).containsExactly(ProfileUiEvent.FullNameUpdated("Ada Lovelace"))
    }

    @Test
    fun editFullName_blank_isANoOpThatNeverStartsLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()
        val events = collectEvents()

        viewModel.editFullName("   ")
        runCurrent()

        assertThat(userRepository.fullNameUpdates).isEmpty()
        assertThat(events).isEmpty()
        assertThat(loading).containsExactly(false)
    }

    @Test
    fun editFullName_failure_emitsSnackBar() = runTest {
        userRepository.updateFullNameResult = AuthResult(AuthCode.USER_FULL_NAME_NOT_UPDATED)
        val events = collectEvents()

        viewModel.editFullName("Grace")

        assertThat(events).containsExactly(
            ProfileUiEvent.ShowSnackBar(AuthCode.USER_FULL_NAME_NOT_UPDATED.message)
        )
    }

    @Test
    fun editFullName_withoutNotify_emitsNothing() = runTest {
        val events = collectEvents()

        viewModel.editFullName("Grace", notify = false)
        userRepository.updateFullNameResult = AuthResult(AuthCode.USER_FULL_NAME_NOT_UPDATED)
        viewModel.editFullName("Grace", notify = false)

        assertThat(userRepository.fullNameUpdates).hasSize(2)
        assertThat(events).isEmpty()
    }

    @Test
    fun editFullName_previousNameIsEmptyWhenThereIsNoUser() = runTest {
        userRepository.setUser(null)
        viewModel = ProfileViewModel(
            userRepository, expensesRepository, userPreferencesRepository, loadingRepository, profileImageStorage,
        )
        val events = collectEvents()

        viewModel.editFullName("Grace")

        assertThat(events).containsExactly(ProfileUiEvent.FullNameUpdated(""))
    }

    @Test
    fun setDynamicColor_delegates() = runTest {
        viewModel.setDynamicColor(false)

        assertThat(expensesRepository.dynamicColorCalls).containsExactly(false)
    }

    @Test
    fun setCurrencyCode_delegatesAndBracketsWithLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()

        viewModel.setCurrencyCode("USD")
        runCurrent()

        assertThat(expensesRepository.currencyCodeCalls).containsExactly("USD")
        assertThat(loading).containsExactly(false, true, false).inOrder()
    }

    @Test
    fun setProPicChoice_delegatesAndBracketsWithLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()

        viewModel.setProPicChoice("avatar_2")
        runCurrent()

        assertThat(expensesRepository.proPicChoiceCalls).containsExactly("avatar_2")
        assertThat(loading).containsExactly(false, true, false).inOrder()
    }

    @Test
    fun versionName_isPrefixedWithTheAppName() {
        assertThat(viewModel.versionName).startsWith("MyFinance ")
        assertThat(viewModel.versionName.removePrefix("MyFinance ")).isNotEmpty()
    }

    @Test
    fun isDynamicColorAvailable_isFalseOnTheJvm() {
        // Build.VERSION.SDK_INT reads as 0 from the stubbed android.jar.
        assertThat(viewModel.isDynamicColorAvailable).isFalse()
    }

    @Test
    fun scrollToTop_emitsToSubscribers() = runTest {
        val scrolls = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.scrollToTop.toList(scrolls) }

        viewModel.scrollToTop()

        assertThat(scrolls).hasSize(1)
    }

    private fun TestScope.collectEvents(): List<ProfileUiEvent> {
        val events = mutableListOf<ProfileUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    // When nothing suspends between startLoading and stopLoading, an unconfined Main lets the
    // StateFlow conflate the `true` away before the collector runs. A standard Main plus
    // runCurrent() observes both steps.
    private fun TestScope.useStandardMain() = Dispatchers.setMain(StandardTestDispatcher(testScheduler))

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
