package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.databinding.ActivityLoginBinding
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.snackBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginActivity : AppCompatActivity(), AuthListener {

    companion object {
        const val INTENT_USER_REQUEST: String = "com.frafio.myfinance.USER_REQUEST"
        const val INTENT_USER_NAME: String = "com.frafio.myfinance.USER_NAME"
    }

    // binding
    private lateinit var binding: ActivityLoginBinding

    // viewModel
    private val viewModel by viewModels<AuthViewModel>()

    // login Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val googleSignInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            val data: Intent? = result.data
            viewModel.onGoogleRequest(data)
        }

    private val signUpResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                goToHomeActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel

        viewModel.authListener = this

        binding.loginEmailInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.loginEmailInputLayout.isErrorEnabled = false
        }
        binding.loginPasswordInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.loginPasswordInputLayout.isErrorEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        binding.loginEmailInputLayout.isErrorEnabled = false
        binding.loginPasswordInputLayout.isErrorEnabled = false
    }

    fun onGoogleButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        binding.loginProgressIndicator.show()

        binding.googleButton.isEnabled = false
        binding.loginButton.isEnabled = false

        mGoogleSignInClient = getGoogleClient()

        // SignIn Intent
        mGoogleSignInClient.signInIntent.also {
            googleSignInResultLauncher.launch(it)
        }

    }

    private fun getGoogleClient(): GoogleSignInClient {
        // Create request
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        return GoogleSignIn.getClient(this, gso)
    }

    fun goToSignupActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        Intent(applicationContext, SignupActivity::class.java).also {
            signUpResultLauncher.launch(it)
        }
    }

    override fun onAuthStarted() {
        binding.loginProgressIndicator.show()

        binding.googleButton.isEnabled = false
        binding.loginButton.isEnabled = false

        binding.loginEmailInputLayout.isErrorEnabled = false
        binding.loginPasswordInputLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code != AuthCode.LOGIN_SUCCESS.code) {
                binding.loginProgressIndicator.hide()

                binding.googleButton.isEnabled = true
                binding.loginButton.isEnabled = true
            }

            when (authResult.code) {
                AuthCode.LOGIN_SUCCESS.code -> goToHomeActivity()

                AuthCode.GOOGLE_LOGIN_FAILURE.code,
                AuthCode.USER_DISABLED.code,
                AuthCode.LOGIN_FAILURE.code ->
                    snackBar(authResult.message)

                AuthCode.INVALID_EMAIL.code,
                AuthCode.USER_NOT_FOUND.code ->
                    binding.loginEmailInputLayout.error = authResult.message

                AuthCode.WRONG_PASSWORD.code ->
                    binding.loginPasswordInputLayout.error = authResult.message

                else -> snackBar(authResult.message)
            }
        }
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.loginProgressIndicator.hide()

        binding.googleButton.isEnabled = true
        binding.loginButton.isEnabled = true

        when (authResult.code) {
            AuthCode.EMPTY_EMAIL.code ->
                binding.loginEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code,
            AuthCode.SHORT_PASSWORD.code ->
                binding.loginPasswordInputLayout.error = authResult.message

            else -> snackBar(authResult.message)
        }
    }

    private fun goToHomeActivity() {
        viewModel.getUserName().also { userName ->
            Intent(applicationContext, HomeActivity::class.java).also {
                it.putExtra(INTENT_USER_REQUEST, true)
                it.putExtra(INTENT_USER_NAME, userName)
                startActivity(it)
                finish()
            }
        }

    }
}