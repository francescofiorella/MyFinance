package com.frafio.myfinance.features.auth

import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = TestUserRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        viewModel = AuthViewModel(userRepository, loadingRepository)
    }

    // region field changes

    @Test
    fun initialState_isEmptyLoginForm() {
        val state = viewModel.uiState.value
        assertThat(state).isEqualTo(AuthUiState())
        assertThat(state.isSigningUp).isFalse()
    }

    @Test
    fun onEmailChange_setsEmailAndClearsOnlyItsError() = runTest {
        viewModel.onLoginButtonClick() // empty form -> email and password errors

        viewModel.onEmailChange("ada@example.com")

        val state = viewModel.uiState.value
        assertThat(state.email).isEqualTo("ada@example.com")
        assertThat(state.emailError).isNull()
        assertThat(state.passwordError).isNotNull()
    }

    @Test
    fun onPasswordChange_setsPasswordAndClearsOnlyItsError() = runTest {
        viewModel.onLoginButtonClick()

        viewModel.onPasswordChange("12345678")

        val state = viewModel.uiState.value
        assertThat(state.password).isEqualTo("12345678")
        assertThat(state.passwordError).isNull()
        assertThat(state.emailError).isNotNull()
    }

    @Test
    fun onFullNameAndConfirmPasswordChange_setValuesAndClearTheirErrors() = runTest {
        viewModel.onToggleAuthMode()
        viewModel.onSignupButtonClick() // all four errors

        viewModel.onFullNameChange("Ada")
        viewModel.onConfirmPasswordChange("12345678")

        val state = viewModel.uiState.value
        assertThat(state.fullName).isEqualTo("Ada")
        assertThat(state.fullNameError).isNull()
        assertThat(state.confirmPassword).isEqualTo("12345678")
        assertThat(state.confirmPasswordError).isNull()
        assertThat(state.emailError).isNotNull()
        assertThat(state.passwordError).isNotNull()
    }

    @Test
    fun onToggleAuthMode_flipsModeAndClearsEveryError() = runTest {
        viewModel.onToggleAuthMode()
        viewModel.onSignupButtonClick()
        assertThat(viewModel.uiState.value.fullNameError).isNotNull()

        viewModel.onToggleAuthMode()

        val state = viewModel.uiState.value
        assertThat(state.isSigningUp).isFalse()
        assertThat(state.emailError).isNull()
        assertThat(state.passwordError).isNull()
        assertThat(state.fullNameError).isNull()
        assertThat(state.confirmPasswordError).isNull()
    }

    @Test
    fun setShowResetPasswordSheet_toggles() {
        viewModel.setShowResetPasswordSheet(true)
        assertThat(viewModel.uiState.value.showResetPasswordSheet).isTrue()
        viewModel.setShowResetPasswordSheet(false)
        assertThat(viewModel.uiState.value.showResetPasswordSheet).isFalse()
    }

    // endregion

    // region login

    @Test
    fun login_trimsTheEmailBeforeValidating() = runTest {
        collectEvents()
        viewModel.onEmailChange("  ada@example.com  ")
        viewModel.onPasswordChange("12345678")

        viewModel.onLoginButtonClick()

        assertThat(viewModel.uiState.value.email).isEqualTo("ada@example.com")
        assertThat(userRepository.loginCalls).containsExactly("ada@example.com" to "12345678")
    }

    @Test
    fun login_emptyEmail_setsErrorAndSkipsTheRepository() = runTest {
        val events = collectEvents()
        viewModel.onPasswordChange("12345678")

        viewModel.onLoginButtonClick()

        assertThat(viewModel.uiState.value.emailError).isEqualTo(AuthCode.EMPTY_EMAIL.message)
        assertThat(userRepository.loginCalls).isEmpty()
        assertThat(events).isEmpty()
    }

    @Test
    fun login_emptyPassword_setsError() = runTest {
        collectEvents()
        viewModel.onEmailChange("ada@example.com")

        viewModel.onLoginButtonClick()

        assertThat(viewModel.uiState.value.passwordError).isEqualTo(AuthCode.EMPTY_PASSWORD.message)
        assertThat(userRepository.loginCalls).isEmpty()
    }

    @Test
    fun login_shortPassword_setsError() = runTest {
        collectEvents()
        viewModel.onEmailChange("ada@example.com")
        viewModel.onPasswordChange("1234567")

        viewModel.onLoginButtonClick()

        assertThat(viewModel.uiState.value.passwordError).isEqualTo(AuthCode.SHORT_PASSWORD.message)
        assertThat(userRepository.loginCalls).isEmpty()
    }

    @Test
    fun login_eightCharacterPassword_passesValidation() = runTest {
        collectEvents()
        viewModel.onEmailChange("ada@example.com")
        viewModel.onPasswordChange("12345678")

        viewModel.onLoginButtonClick()

        assertThat(viewModel.uiState.value.passwordError).isNull()
        assertThat(userRepository.loginCalls).hasSize(1)
    }

    @Test
    fun login_emptyForm_setsBothErrorsInOnePass() = runTest {
        collectEvents()

        viewModel.onLoginButtonClick()

        val state = viewModel.uiState.value
        assertThat(state.emailError).isEqualTo(AuthCode.EMPTY_EMAIL.message)
        assertThat(state.passwordError).isEqualTo(AuthCode.EMPTY_PASSWORD.message)
    }

    @Test
    fun login_success_emitsSuccess() = runTest {
        val events = collectEvents()
        fillValidLogin()

        viewModel.onLoginButtonClick()

        assertThat(events).containsExactly(AuthUiEvent.Success)
    }

    @Test
    fun login_failure_emitsErrorWithTheMessage() = runTest {
        userRepository.loginResult = AuthResult(AuthCode.WRONG_PASSWORD)
        val events = collectEvents()
        fillValidLogin()

        viewModel.onLoginButtonClick()

        assertThat(events).containsExactly(AuthUiEvent.Error(AuthCode.WRONG_PASSWORD.message))
    }

    @Test
    fun login_startsAndStopsLoadingOnBothOutcomes() = runTest {
        useStandardMain()
        val loading = collectLoading()
        collectEvents()
        fillValidLogin()

        viewModel.onLoginButtonClick()
        runCurrent()
        userRepository.loginResult = AuthResult(AuthCode.LOGIN_FAILURE)
        viewModel.onLoginButtonClick()
        runCurrent()

        assertThat(loading).containsExactly(false, true, false, true, false).inOrder()
    }

    @Test
    fun login_validationFailure_neverStartsLoading() = runTest {
        val loading = collectLoading()
        collectEvents()

        viewModel.onLoginButtonClick()

        assertThat(loading).containsExactly(false)
    }

    // endregion

    // region signup

    @Test
    fun signup_trimsNameAndEmail() = runTest {
        collectEvents()
        fillValidSignup(fullName = "  Ada  ", email = "  ada@example.com ")

        viewModel.onSignupButtonClick()

        assertThat(viewModel.uiState.value.fullName).isEqualTo("Ada")
        assertThat(viewModel.uiState.value.email).isEqualTo("ada@example.com")
        assertThat(userRepository.signupCalls).containsExactly(Triple("Ada", "ada@example.com", "12345678"))
    }

    @Test
    fun signup_emptyName_setsError() = runTest {
        collectEvents()
        fillValidSignup(fullName = "")

        viewModel.onSignupButtonClick()

        assertThat(viewModel.uiState.value.fullNameError).isEqualTo(AuthCode.EMPTY_NAME.message)
        assertThat(userRepository.signupCalls).isEmpty()
    }

    @Test
    fun signup_emptyConfirmPassword_setsError() = runTest {
        collectEvents()
        fillValidSignup(confirmPassword = "")

        viewModel.onSignupButtonClick()

        assertThat(viewModel.uiState.value.confirmPasswordError).isEqualTo(AuthCode.EMPTY_CONFIRM_PASSWORD.message)
    }

    @Test
    fun signup_mismatchedConfirmPassword_setsError() = runTest {
        collectEvents()
        fillValidSignup(confirmPassword = "87654321")

        viewModel.onSignupButtonClick()

        assertThat(viewModel.uiState.value.confirmPasswordError).isEqualTo(AuthCode.PASSWORD_NOT_MATCH.message)
    }

    @Test
    fun signup_emptyPassword_suppressesTheMismatchError() = runTest {
        collectEvents()
        fillValidSignup(password = "", confirmPassword = "87654321")

        viewModel.onSignupButtonClick()

        val state = viewModel.uiState.value
        assertThat(state.passwordError).isEqualTo(AuthCode.EMPTY_PASSWORD.message)
        assertThat(state.confirmPasswordError).isNull()
    }

    @Test
    fun signup_emptyForm_setsAllFourErrors() = runTest {
        collectEvents()
        viewModel.onToggleAuthMode()

        viewModel.onSignupButtonClick()

        val state = viewModel.uiState.value
        assertThat(state.fullNameError).isEqualTo(AuthCode.EMPTY_NAME.message)
        assertThat(state.emailError).isEqualTo(AuthCode.EMPTY_EMAIL.message)
        assertThat(state.passwordError).isEqualTo(AuthCode.EMPTY_PASSWORD.message)
        assertThat(state.confirmPasswordError).isEqualTo(AuthCode.EMPTY_CONFIRM_PASSWORD.message)
    }

    @Test
    fun signup_success_emitsSuccess() = runTest {
        val events = collectEvents()
        fillValidSignup()

        viewModel.onSignupButtonClick()

        assertThat(events).containsExactly(AuthUiEvent.Success)
    }

    @Test
    fun signup_failure_emitsError() = runTest {
        userRepository.signupResult = AuthResult(AuthCode.EMAIL_ALREADY_ASSOCIATED)
        val events = collectEvents()
        fillValidSignup()

        viewModel.onSignupButtonClick()

        assertThat(events).containsExactly(AuthUiEvent.Error(AuthCode.EMAIL_ALREADY_ASSOCIATED.message))
    }

    // endregion

    // region reset password

    @Test
    fun resetPassword_trimsAndEmitsMessageWhenSent() = runTest {
        val events = collectEvents()

        viewModel.resetPassword("  ada@example.com ")

        assertThat(userRepository.resetPasswordCalls).containsExactly("ada@example.com")
        assertThat(events).containsExactly(AuthUiEvent.Message(AuthCode.EMAIL_SENT.message))
    }

    @Test
    fun resetPassword_failure_emitsError() = runTest {
        userRepository.resetPasswordResult = AuthResult(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS)
        val events = collectEvents()

        viewModel.resetPassword("ada@example.com")

        assertThat(events).containsExactly(AuthUiEvent.Error(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS.message))
    }

    @Test
    fun resetPassword_startsAndStopsLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()
        collectEvents()

        viewModel.resetPassword("ada@example.com")
        runCurrent()

        assertThat(loading).containsExactly(false, true, false).inOrder()
    }

    // endregion

    @Test
    fun startAndStopLoading_delegateToTheRepository() {
        viewModel.startLoading()
        assertThat(loadingRepository.isLoading.value).isTrue()
        viewModel.stopLoading()
        assertThat(loadingRepository.isLoading.value).isFalse()
    }

    private fun fillValidLogin() {
        viewModel.onEmailChange("ada@example.com")
        viewModel.onPasswordChange("12345678")
    }

    private fun fillValidSignup(
        fullName: String = "Ada",
        email: String = "ada@example.com",
        password: String = "12345678",
        confirmPassword: String = "12345678",
    ) {
        viewModel.onToggleAuthMode()
        viewModel.onFullNameChange(fullName)
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmPasswordChange(confirmPassword)
    }

    private fun TestScope.collectEvents(): List<AuthUiEvent> {
        val events = mutableListOf<AuthUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    // startLoading and stopLoading run back-to-back before the event emit suspends, so an
    // unconfined Main lets the StateFlow conflate the `true` away. A standard Main plus
    // runCurrent() observes both steps.
    private fun TestScope.useStandardMain() = Dispatchers.setMain(StandardTestDispatcher(testScheduler))

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
