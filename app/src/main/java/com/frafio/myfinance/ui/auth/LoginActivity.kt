package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.manager.FetchListener
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.databinding.ActivityLoginBinding
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.util.snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class LoginActivity : AppCompatActivity(), AuthListener, FetchListener, KodeinAware {

    // binding
    private lateinit var binding: ActivityLoginBinding

    // viewModel
    private lateinit var viewModel: AuthViewModel

    // login Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 101
    }

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
        PurchaseManager.fetchListener = this

        // toolbar
        setSupportActionBar(binding.loginToolbar)
    }

    fun  onGoogleButtonClick(view: View){
        binding.loginProgressIindicator.show()

        mGoogleSignInClient = getGoogleClient()

        // SignIn Intent
        mGoogleSignInClient.signInIntent.also {
            startActivityForResult(it, RC_SIGN_IN)
        }

    }

    private fun getGoogleClient() : GoogleSignInClient {
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
            startActivityForResult(it, 1)
        }
    }

    override fun onAuthStarted() {
        binding.loginProgressIindicator.show()

        binding.loginEmailInputLayout.isErrorEnabled = false
        binding.loginPasswordInputLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<Any>) {
        response.observe(this, { responseData ->
            if (responseData != 1) {
                binding.loginProgressIindicator.hide()
            }

            when (responseData) {
                1 -> PurchaseManager.updatePurchaseList()
                2 -> binding.loginEmailInputLayout.error = "L'email inserita non è ben formata."
                3 -> binding.loginPasswordInputLayout.error = "La password inserita non è corretta."
                4 -> binding.loginEmailInputLayout.error = "L'email inserita non ha un account associato."
                is String -> binding.root.snackbar(responseData)
            }
        })
    }

    override fun onAuthFailure(errorCode: Int) {
        binding.loginProgressIindicator.hide()

        when (errorCode) {
            1 -> binding.loginEmailInputLayout.error = "Inserisci la tua email."
            2 -> binding.loginPasswordInputLayout.error = "Inserisci la password."
            3 -> binding.loginPasswordInputLayout.error = "La password deve essere lunga almeno 8 caratteri!"
        }
    }

    override fun onFetchSuccess(response: LiveData<Any>?) {
        binding.loginProgressIindicator.hide()
        Intent(applicationContext, HomeActivity::class.java).also {
            it.putExtra("com.frafio.myfinance.userRequest", true)
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    override fun onFetchFailure(message: String) {
        binding.loginProgressIindicator.hide()
        binding.root.snackbar(message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            viewModel.onGoogleRequest(data)
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent(applicationContext, HomeActivity::class.java).also {
                it.putExtra("com.frafio.myfinance.userRequest", true)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
    }
}