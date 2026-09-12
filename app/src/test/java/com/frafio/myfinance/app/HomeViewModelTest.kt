package com.frafio.myfinance.app

import androidx.lifecycle.ViewModelStore
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = TestUserRepository()
    private val expensesRepository = TestExpensesRepository()
    private val incomeRepository = TestIncomeRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val incomesLocalRepository = TestIncomesLocalRepository()
    private val loadingRepository = LoadingRepository()
    private val profileImageStorage = TestProfileImageStorage()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        // checkUser awaits the first local count of each table, as Room would emit immediately.
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())
        userPreferencesRepository.setUser(testUser(photoUrl = "https://example.com/p.png"))
        viewModel = HomeViewModel(
            userRepository,
            expensesRepository,
            incomeRepository,
            userPreferencesRepository,
            expensesLocalRepository,
            incomesLocalRepository,
            loadingRepository,
            profileImageStorage,
        )
    }

    @Test
    fun uiState_startsLoading() {
        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Loading)
    }

    @Test
    fun userAndPreferences_areInitialisedFromTheRepository() {
        assertThat(viewModel.userPreferences.value).isEqualTo(userPreferencesRepository.userPreferencesFlow.value)
        assertThat(viewModel.user.value).isEqualTo(testUser(photoUrl = "https://example.com/p.png"))
        assertThat(viewModel.profilePicture.value).isNull()
    }

    @Test
    fun getFullName_isEmptyWithoutAUserAndFollowsPreferencesOnceCollected() = runTest {
        userPreferencesRepository.setUser(null)
        viewModel = HomeViewModel(
            userRepository, expensesRepository, incomeRepository, userPreferencesRepository,
            expensesLocalRepository, incomesLocalRepository, loadingRepository, profileImageStorage,
        )
        assertThat(viewModel.getFullName()).isEmpty()

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.userPreferences.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.user.collect() }
        userPreferencesRepository.setUser(testUser(fullName = "Ada"))

        assertThat(viewModel.getFullName()).isEqualTo("Ada")
    }

    // region checkUser

    @Test
    fun checkUser_loggedIn_completesAndStartsAllThreeListeners() = runTest {
        val events = collectUiEvents()

        viewModel.checkUser(notify = false)

        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Complete)
        assertThat(expensesRepository.rootSnapshotListenerRunning).isTrue()
        assertThat(expensesRepository.snapshotListenerRunning).isTrue()
        assertThat(incomeRepository.snapshotListenerRunning).isTrue()
        assertThat(loadingRepository.isLoading.value).isFalse()
        assertThat(loadingRepository.isFirstSync.value).isFalse()
        assertThat(events).isEmpty()
    }

    @Test
    fun checkUser_withNotify_emitsLoginSuccessOnce() = runTest {
        val events = collectUiEvents()

        viewModel.checkUser(notify = true)

        assertThat(events).containsExactly(HomeUiEvent.LoginSuccess)
    }

    @Test
    fun checkUser_syncsTheProfilePictureFromPreferences() = runTest {
        viewModel.checkUser(notify = false)

        assertThat(userRepository.syncedPhotoUrls).containsExactly("https://example.com/p.png")
    }

    @Test
    fun checkUser_dismissesTheSplashBeforeRemoteSyncFinishes() = runTest {
        expensesRepository.completeInitialSyncImmediately = false

        viewModel.checkUser(notify = false)

        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Complete)
        // Still awaiting the remote sync, so loading has not been released yet.
        assertThat(loadingRepository.isLoading.value).isTrue()
        assertThat(loadingRepository.isFirstSync.value).isTrue()
    }

    @Test
    fun checkUser_notLoggedIn_completesAndSignalsMainWithoutListeners() = runTest {
        userRepository.isUserLoggedResult = AuthResult(AuthCode.USER_NOT_LOGGED)
        val mainEvents = collectMainEvents()

        viewModel.checkUser(notify = true)

        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Complete)
        assertThat(mainEvents).containsExactly(MainEvent.UserNotLogged)
        assertThat(expensesRepository.rootSnapshotListenerRunning).isFalse()
        assertThat(expensesRepository.snapshotListenerRunning).isFalse()
        assertThat(incomeRepository.snapshotListenerRunning).isFalse()
        assertThat(loadingRepository.isLoading.value).isFalse()
        assertThat(loadingRepository.isFirstSync.value).isFalse()
    }

    @Test
    fun checkUser_repositoryThrows_completesAndUnwindsBothLoadingFlags() = runTest {
        userRepository.isUserLoggedError = IllegalStateException("boom")

        viewModel.checkUser(notify = false)

        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Complete)
        assertThat(loadingRepository.isLoading.value).isFalse()
        assertThat(loadingRepository.isFirstSync.value).isFalse()
    }

    // endregion

    // region navigation and scroll events

    @Test
    fun navigateTo_deliversTheKey() = runTest {
        val navEvents = collectNavEvents()

        viewModel.navigateTo(RootKey.Labels)

        assertThat(navEvents).containsExactly(RootKey.Labels)
    }

    @Test
    fun onTransactionCommitted_expense_navigatesToExpensesAndScrollsToTheDayTotal() = runTest {
        val navEvents = collectNavEvents()
        val scrolls = collectScrollEvents()

        viewModel.onTransactionCommitted(isExpense = true, day = 5, month = 3, year = 2024)

        assertThat(navEvents).containsExactly(HomeTabKey.Expenses)
        assertThat(scrolls).containsExactly("total_5_3_2024" to true)
    }

    @Test
    fun onTransactionCommitted_income_navigatesToBudgetAndScrollsToTheYear() = runTest {
        val navEvents = collectNavEvents()
        val scrolls = collectScrollEvents()

        viewModel.onTransactionCommitted(isExpense = false, day = 5, month = 3, year = 2024)

        assertThat(navEvents).containsExactly(HomeTabKey.Budget)
        assertThat(scrolls).containsExactly("2024" to false)
    }

    @Test
    fun scrollEvents_replayToLateSubscribersAndCanBeReset() = runTest {
        viewModel.onTransactionCommitted(isExpense = true, day = 1, month = 1, year = 2024)

        val scrolls = collectScrollEvents()
        assertThat(scrolls).containsExactly("total_1_1_2024" to true)

        viewModel.resetScrollEvent()
        assertThat(scrolls).containsExactly("total_1_1_2024" to true, null).inOrder()
    }

    // endregion

    // region logout and clear

    @Test
    fun onLogoutButtonClick_success_stopsListenersAndSignalsMain() = runTest {
        val mainEvents = collectMainEvents()
        viewModel.checkUser(notify = false)

        viewModel.onLogoutButtonClick()

        assertThat(expensesRepository.stopCallCount).isEqualTo(1)
        assertThat(incomeRepository.stopCallCount).isEqualTo(1)
        assertThat(mainEvents).containsExactly(MainEvent.LogoutSuccess)
    }

    @Test
    fun onLogoutButtonClick_failure_doesNothing() = runTest {
        userRepository.logoutResult = AuthResult(AuthCode.LOGIN_FAILURE)
        val mainEvents = collectMainEvents()

        viewModel.onLogoutButtonClick()

        assertThat(expensesRepository.stopCallCount).isEqualTo(0)
        assertThat(incomeRepository.stopCallCount).isEqualTo(0)
        assertThat(mainEvents).isEmpty()
    }

    @Test
    fun onCleared_stopsBothListeners() {
        ViewModelStore().apply {
            put("home", viewModel)
            clear()
        }

        assertThat(expensesRepository.stopCallCount).isEqualTo(1)
        assertThat(incomeRepository.stopCallCount).isEqualTo(1)
    }

    // endregion

    private fun TestScope.collectUiEvents(): List<HomeUiEvent> {
        val events = mutableListOf<HomeUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    private fun TestScope.collectMainEvents(): List<MainEvent> {
        val events = mutableListOf<MainEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.mainEvents.toList(events) }
        return events
    }

    private fun TestScope.collectNavEvents(): List<NavKey> {
        val events = mutableListOf<NavKey>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.navEvents.toList(events) }
        return events
    }

    private fun TestScope.collectScrollEvents(): List<Pair<String, Boolean>?> {
        val events = mutableListOf<Pair<String, Boolean>?>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.scrollEvents.toList(events) }
        return events
    }
}
