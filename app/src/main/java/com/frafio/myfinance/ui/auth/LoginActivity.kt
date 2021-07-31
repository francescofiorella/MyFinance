package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
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

    private val googleSignInResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        val data: Intent? = result.data
        viewModel.onGoogleRequest(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        // toolbar
        setSupportActionBar(binding.loginToolbar)
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

    private val signUpResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val name = viewModel.getUserName()
            Intent(applicationContext, HomeActivity::class.java).also {
                it.putExtra("com.frafio.myfinance.userRequest", true)
                it.putExtra("com.frafio.myfinance.userName", name)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
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

    override fun onAuthSuccess(response: LiveData<Any>) {
        response.observe(this, { responseData ->
            if (responseData != 1) {
                binding.loginProgressIndicator.hide()
            }

            when (responseData) {
                1 -> viewModel.updateUserData()
                2 -> binding.loginEmailInputLayout.error = "L'email inserita non è ben formata."
                3 -> binding.loginPasswordInputLayout.error = "La password inserita non è corretta."
                4 -> binding.loginEmailInputLayout.error =
                    "L'email inserita non ha un account associato."
                "List updated" -> {
                    val name = viewModel.getUserName()
                    Intent(applicationContext, HomeActivity::class.java).also {
                        it.putExtra("com.frafio.myfinance.userRequest", true)
                        it.putExtra("com.frafio.myfinance.userName", name)
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }
                is String -> binding.root.snackbar(responseData)
            }
        })
    }

    override fun onAuthFailure(errorCode: Int) {
        binding.loginProgressIndicator.hide()

        when (errorCode) {
            1 -> binding.loginEmailInputLayout.error = "Inserisci la tua email."
            2 -> binding.loginPasswordInputLayout.error = "Inserisci la password."
            3 -> binding.loginPasswordInputLayout.error =
                "La password deve essere lunga almeno 8 caratteri!"
        }
    }
}