package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivityLoginBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.kodein.di.generic.instance

class LoginActivity : BaseActivity(), AuthListener {

    // binding
    private lateinit var binding: ActivityLoginBinding

    // viewModel
    private lateinit var viewModel: AuthViewModel

    // login Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val factory: AuthViewModelFactory by instance()

    private val googleSignInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            val data: Intent? = result.data
            viewModel.onGoogleRequest(data)
        }

    private val signUpResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val name = viewModel.getUserName()
                Intent(applicationContext, HomeActivity::class.java).also {
                    it.putExtra("${getString(R.string.default_path)}.userRequest", true)
                    it.putExtra("${getString(R.string.default_path)}.userName", name)
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
    }

    fun onGoogleButtonClick(view: View) {
        binding.loginProgressIndicator.show()

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

    fun goToSignupActivity(view: View) {
        Intent(applicationContext, SignupActivity::class.java).also {
            signUpResultLauncher.launch(it)
        }
    }

    override fun onAuthStarted() {
        binding.loginProgressIndicator.show()

        binding.loginEmailInputLayout.isErrorEnabled = false
        binding.loginPasswordInputLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code != AuthCode.LOGIN_SUCCESS.code) {
                binding.loginProgressIndicator.hide()
            }

            when (authResult.code) {
                AuthCode.LOGIN_SUCCESS.code -> viewModel.updateUserData()

                AuthCode.GOOGLE_LOGIN_FAILURE.code -> snackbar(authResult.message)

                AuthCode.USER_DISABLED.code -> snackbar(authResult.message)

                AuthCode.LOGIN_FAILURE.code -> snackbar(authResult.message)

                AuthCode.USER_DATA_NOT_UPDATED.code -> snackbar(authResult.message)

                AuthCode.INVALID_EMAIL.code ->
                    binding.loginEmailInputLayout.error = authResult.message

                AuthCode.USER_NOT_FOUND.code ->
                    binding.loginEmailInputLayout.error = authResult.message

                AuthCode.WRONG_PASSWORD.code ->
                    binding.loginPasswordInputLayout.error = authResult.message

                AuthCode.USER_DATA_UPDATED.code -> {
                    val name = viewModel.getUserName()
                    Intent(applicationContext, HomeActivity::class.java).also {
                        it.putExtra("${getString(R.string.default_path)}.userRequest", true)
                        it.putExtra("${getString(R.string.default_path)}.userName", name)
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }

                else -> snackbar(authResult.message)
            }
        })
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.loginProgressIndicator.hide()

        when (authResult.code) {
            AuthCode.EMPTY_EMAIL.code ->
                binding.loginEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code ->
                binding.loginPasswordInputLayout.error = authResult.message

            AuthCode.SHORT_PASSWORD.code ->
                binding.loginPasswordInputLayout.error = authResult.message

            else -> snackbar(authResult.message)
        }
    }
}