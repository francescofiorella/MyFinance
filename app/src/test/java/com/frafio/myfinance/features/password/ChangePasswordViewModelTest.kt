package com.frafio.myfinance.features.password

import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.repository.TestUserRepository
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
class ChangePasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = TestUserRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setup() {
        userRepository.setUser(testUser())
        viewModel = ChangePasswordViewModel(userRepository, loadingRepository)
    }

    @Test
    fun user_isInitialisedFromTheCurrentUser() {
        assertThat(viewModel.user.value).isEqualTo(testUser())
    }

    @Test
    fun user_followsTheRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.user.collect() }

        userRepository.setUser(null)

        assertThat(viewModel.user.value).isNull()
    }

    @Test
    fun isLoading_isTheRepositoryFlow() {
        assertThat(viewModel.isLoading).isSameInstanceAs(loadingRepository.isLoading)
    }

    @Test
    fun changePassword_success_emitsSuccess() = runTest {
        val events = collectEvents()

        viewModel.changePassword("new-password", "old-password")

        assertThat(events).containsExactly(
            ChangePasswordUiEvent.Success(AuthCode.PASSWORD_UPDATED.message)
        )
        assertThat(userRepository.changePasswordCalls).containsExactly("new-password" to "old-password")
    }

    @Test
    fun changePassword_wrongOldPassword_emitsInvalidCurrentPassword() = runTest {
        userRepository.changePasswordResult = AuthResult(AuthCode.WRONG_OLD_PASSWORD)
        val events = collectEvents()

        viewModel.changePassword("new-password", "old-password")

        assertThat(events).containsExactly(
            ChangePasswordUiEvent.InvalidCurrentPassword(AuthCode.WRONG_OLD_PASSWORD.message)
        )
    }

    @Test
    fun changePassword_otherFailure_emitsSnackBar() = runTest {
        userRepository.changePasswordResult = AuthResult(AuthCode.PASSWORD_NOT_UPDATED)
        val events = collectEvents()

        viewModel.changePassword("new-password")

        assertThat(events).containsExactly(
            ChangePasswordUiEvent.ShowSnackBar(AuthCode.PASSWORD_NOT_UPDATED.message)
        )
    }

    @Test
    fun changePassword_forwardsNullCurrentPassword() = runTest {
        collectEvents()

        viewModel.changePassword("new-password")

        assertThat(userRepository.changePasswordCalls).containsExactly("new-password" to null)
    }

    @Test
    fun changePassword_startsAndStopsLoading() = runTest {
        val loading = collectLoading()
        collectEvents()

        viewModel.changePassword("new-password")

        assertThat(loading).containsExactly(false, true, false).inOrder()
    }

    @Test
    fun resetPassword_sendsToTheCurrentUserEmail() = runTest {
        val events = collectEvents()

        viewModel.resetPassword()

        assertThat(userRepository.resetPasswordCalls).containsExactly("ada@example.com")
        assertThat(events).containsExactly(ChangePasswordUiEvent.ShowSnackBar(AuthCode.EMAIL_SENT.message))
    }

    @Test
    fun resetPassword_alwaysEmitsSnackBarEvenOnFailure() = runTest {
        userRepository.resetPasswordResult = AuthResult(AuthCode.EMAIL_NOT_SENT)
        val events = collectEvents()

        viewModel.resetPassword()

        assertThat(events).containsExactly(ChangePasswordUiEvent.ShowSnackBar(AuthCode.EMAIL_NOT_SENT.message))
    }

    @Test
    fun resetPassword_withoutAUser_doesNothing() = runTest {
        userRepository.setUser(null)
        viewModel = ChangePasswordViewModel(userRepository, loadingRepository)
        val loading = collectLoading()
        val events = collectEvents()

        viewModel.resetPassword()

        assertThat(userRepository.resetPasswordCalls).isEmpty()
        assertThat(events).isEmpty()
        assertThat(loading).containsExactly(false)
    }

    @Test
    fun resetPassword_startsAndStopsLoading() = runTest {
        val loading = collectLoading()
        collectEvents()

        viewModel.resetPassword()

        assertThat(loading).containsExactly(false, true, false).inOrder()
    }

    private fun TestScope.collectEvents(): List<ChangePasswordUiEvent> {
        val events = mutableListOf<ChangePasswordUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
