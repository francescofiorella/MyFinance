package com.frafio.myfinance.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.utils.animateRoot
import com.frafio.myfinance.core.utils.clearText
import com.frafio.myfinance.core.utils.instantHide
import com.frafio.myfinance.core.utils.instantShow
import com.frafio.myfinance.core.utils.snackBar
import com.frafio.myfinance.databinding.ActivityAuthBinding
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.features.auth.AuthUiEvent
import com.frafio.myfinance.features.auth.AuthViewModel
import com.frafio.myfinance.features.auth.ResetPasswordSheet
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    companion object {
        private val TAG = AuthActivity::class.java.simpleName
        const val INTENT_USER_REQUEST: String = "com.frafio.myfinance.USER_REQUEST"
    }

    // binding
    private lateinit var binding: ActivityAuthBinding

    // viewModel
    private val viewModel by viewModels<AuthViewModel>()

    private var showResetPasswordSheet by mutableStateOf(false)

    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.viewModel = viewModel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                // Apply padding
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }

        binding.authFullNameInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authFullNameInputLayout.isErrorEnabled = false
        }

        binding.authEmailInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authEmailInputLayout.isErrorEnabled = false
        }
        binding.authPasswordInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authPasswordInputLayout.isErrorEnabled = false
        }

        binding.authConfirmPasswordInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authConfirmPasswordInputLayout.isErrorEnabled = false
        }

        binding.authBackArrow.setOnClickListener { prepareLoginLayout() }
        if (savedInstanceState == null) {
            prepareLoginLayout(false)
        } else {
            if (viewModel.isSigningUp) {
                prepareSignUpLayout()
            } else {
                prepareLoginLayout(false)
            }
        }

        viewModel.credentialManager = CredentialManager.create(baseContext)

        lifecycleScope.launch {
            viewModel.userPreferences.collect { prefs ->
                if (prefs?.dynamicColor == true) {
                    DynamicColors.applyToActivityIfAvailable(this@AuthActivity)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvents.collect { event ->
                    when (event) {
                        AuthUiEvent.Loading -> onAuthStarted()
                        is AuthUiEvent.Success -> onAuthSuccess(event.authResult)
                        is AuthUiEvent.Error -> onAuthFailure(event.authResult)
                        AuthUiEvent.NavigateToHome -> goToHomeActivity()
                    }
                }
            }
        }

        binding.authComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
                val useDynamicColor = userPreferences?.dynamicColor ?: false
                MyFinanceTheme(dynamicColor = useDynamicColor) {
                    ResetPasswordSheet(
                        show = showResetPasswordSheet,
                        onDismiss = {
                            if (showResetPasswordSheet) {
                                showResetPasswordSheet = false
                            }
                        },
                        onSend = {
                            onAuthStarted()
                            clearErrors()
                            viewModel.resetPassword(it)
                            showResetPasswordSheet = false
                        }
                    )
                }
            }
        }
    }

    fun onResetButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        showResetPasswordSheet = true
    }

    private fun clearErrors() {
        binding.authFullNameInputLayout.isErrorEnabled = false
        binding.authEmailInputLayout.isErrorEnabled = false
        binding.authPasswordInputLayout.isErrorEnabled = false
        binding.authConfirmPasswordInputLayout.isErrorEnabled = false
    }

    private fun clearInputs() {
        binding.authFullNameInputText.clearText()
        binding.authFullNameInputText.clearFocus()
        binding.authEmailInputText.clearText()
        binding.authEmailInputText.clearFocus()
        binding.authPasswordInputText.clearText()
        binding.authPasswordInputText.clearFocus()
        binding.authConfirmPasswordInputText.clearText()
        binding.authConfirmPasswordInputText.clearFocus()
    }

    override fun onStart() {
        super.onStart()
        clearErrors()
    }

    fun onGoogleButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onAuthStarted()
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID
            .setServerClientId(getString(R.string.default_web_client_id))
            // Only show accounts previously used to sign in
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = viewModel.credentialManager!!.getCredential(
                    context = this@AuthActivity,
                    request = request
                )

                // Extract credential from the result returned by Credential Manager
                viewModel.onGoogleRequest(result.credential)
            } catch (e: GetCredentialException) {
                onAuthSuccess(AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE))
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            } catch (e: NoCredentialException) {
                onAuthSuccess(AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE))
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    fun prepareSignUpLayout(clearInputs: Boolean = true) {
        if (clearInputs) {
            clearErrors()
            clearInputs()
        }

        viewModel.isSigningUp = true
        binding.authParentLayout.animateRoot()

        binding.authSwitchTextView.setOnClickListener { prepareLoginLayout() }
        binding.authButton.setOnClickListener { viewModel.onSignupButtonClick() }

        binding.authBackArrow.instantShow()
        binding.authFullNameInputLayout.instantShow()
        binding.authPasswordInputLayout.hint = getString(R.string.signup_password)
        binding.authConfirmPasswordInputLayout.instantShow()
        binding.authButton.text = getString(R.string.signup)
        binding.authResetPassTextView.text = getString(R.string.auth_or)
        binding.authResetPassTextView.isClickable = false
        binding.authSwitchTextView.text = getString(R.string.signup_login)

        if (!onBackPressedDispatcher.hasEnabledCallbacks()) {
            if (backPressedCallback == null) {
                backPressedCallback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        prepareLoginLayout()
                    }
                }
            }
            onBackPressedDispatcher.addCallback(backPressedCallback!!)
        }
    }

    fun prepareLoginLayout(clearInputs: Boolean = true) {
        if (clearInputs) {
            clearErrors()
            clearInputs()
        }

        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            backPressedCallback?.remove()
        }

        viewModel.isSigningUp = false
        binding.authParentLayout.animateRoot()

        binding.authSwitchTextView.setOnClickListener { prepareSignUpLayout() }
        binding.authButton.setOnClickListener { viewModel.onLoginButtonClick() }

        binding.authBackArrow.instantHide()
        binding.authFullNameInputLayout.instantHide()
        binding.authPasswordInputLayout.hint = getString(R.string.login_password)
        binding.authConfirmPasswordInputLayout.instantHide()
        binding.authButton.text = getString(R.string.login)
        binding.authResetPassTextView.text = getString(R.string.forgotten_password)
        binding.authResetPassTextView.isClickable = true
        binding.authSwitchTextView.text = getString(R.string.login_signup)
    }

    private fun onAuthStarted() {
        binding.authProgressIndicator.show()

        binding.authFullNameInputLayout.isEnabled = false
        binding.authEmailInputLayout.isEnabled = false
        binding.authPasswordInputLayout.isEnabled = false
        binding.authConfirmPasswordInputLayout.isEnabled = false
        binding.authButton.isEnabled = false
        binding.authResetPassTextView.isEnabled = false
        binding.googleButton.isEnabled = false
        binding.authSwitchTextView.isEnabled = false

        clearErrors()
    }

    private fun onAuthSuccess(authResult: AuthResult) {
        binding.authProgressIndicator.hide()

        binding.authFullNameInputLayout.isEnabled = true
        binding.authEmailInputLayout.isEnabled = true
        binding.authPasswordInputLayout.isEnabled = true
        binding.authConfirmPasswordInputLayout.isEnabled = true
        binding.authButton.isEnabled = true
        binding.authResetPassTextView.isEnabled = true
        binding.googleButton.isEnabled = true
        binding.authSwitchTextView.isEnabled = true

        when (authResult.code) {
            AuthCode.LOGIN_SUCCESS.code,
            AuthCode.SIGNUP_SUCCESS.code ->
                goToHomeActivity()

            AuthCode.INVALID_EMAIL.code,
            AuthCode.USER_NOT_FOUND.code,
            AuthCode.EMAIL_NOT_WELL_FORMED.code,
            AuthCode.EMAIL_ALREADY_ASSOCIATED.code ->
                binding.authEmailInputLayout.error = authResult.message

            AuthCode.WRONG_PASSWORD.code ->
                binding.authPasswordInputLayout.error = authResult.message

            AuthCode.WEAK_PASSWORD.code ->
                binding.authConfirmPasswordInputLayout.error = authResult.message

            AuthCode.GOOGLE_LOGIN_FAILURE.code,
            AuthCode.USER_DISABLED.code,
            AuthCode.LOGIN_FAILURE.code,
            AuthCode.SIGNUP_PROFILE_NOT_UPDATED.code,
            AuthCode.SIGNUP_FAILURE.code ->
                snackBar(authResult.message, binding.authDivider)

            else -> snackBar(authResult.message, binding.authDivider)
        }
    }

    private fun onAuthFailure(authResult: AuthResult) {
        binding.authProgressIndicator.hide()

        binding.authFullNameInputLayout.isEnabled = true
        binding.authEmailInputLayout.isEnabled = true
        binding.authPasswordInputLayout.isEnabled = true
        binding.authConfirmPasswordInputLayout.isEnabled = true
        binding.authButton.isEnabled = true
        binding.authResetPassTextView.isEnabled = true
        binding.googleButton.isEnabled = true
        binding.authSwitchTextView.isEnabled = true

        when (authResult.code) {
            AuthCode.EMPTY_NAME.code ->
                binding.authFullNameInputLayout.error = authResult.message

            AuthCode.EMPTY_EMAIL.code ->
                binding.authEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code,
            AuthCode.SHORT_PASSWORD.code ->
                binding.authPasswordInputLayout.error = authResult.message

            AuthCode.EMPTY_CONFIRM_PASSWORD.code,
            AuthCode.PASSWORD_NOT_MATCH.code ->
                binding.authConfirmPasswordInputLayout.error = authResult.message

            else -> snackBar(authResult.message, binding.authDivider)
        }
    }

    private fun goToHomeActivity() {
        Intent(applicationContext, HomeActivity::class.java).also {
            it.putExtra(INTENT_USER_REQUEST, true)
            startActivity(it)
            finish()
        }
    }
}