package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.databinding.ActivityLoginBinding
import com.frafio.myfinance.ui.home.MainActivity
import com.frafio.myfinance.utils.snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity(), AuthListener {

    // definizione variabili
    lateinit var layout: RelativeLayout

    private lateinit var mToolbar: MaterialToolbar
    private lateinit var mEmailLayout: TextInputLayout
    private lateinit var mPasswordLayout: TextInputLayout
    private lateinit var mProgressIndicator: LinearProgressIndicator

    // viewModel
    lateinit var viewModel: AuthViewModel

    // login Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 101
        private val TAG = LoginActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository()
        val factory = AuthViewModelFactory(repository)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        // toolbar
        mToolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(mToolbar)

        // collegamento view
        layout = findViewById(R.id.login_layout)
        mProgressIndicator = findViewById(R.id.login_progressIindicator)
        mEmailLayout = findViewById(R.id.login_emailInputLayout)
        mPasswordLayout = findViewById(R.id.login_passwordInputLayout)
    }

    fun  onGoogleButtonClick(view: View){
        mProgressIndicator.show()

        // Create request
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // SignIn Intent
        mGoogleSignInClient.signInIntent.also {
            startActivityForResult(it, RC_SIGN_IN)
        }

    }

    fun goToSignupActivity(view: View) {
        Intent(applicationContext, SignupActivity::class.java).also {
            startActivityForResult(it, 1)
        }
    }

    override fun onAuthStarted() {
        mProgressIndicator.show()

        mEmailLayout.isErrorEnabled = false
        mPasswordLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<Any>) {
        response.observe(this, Observer { responseData ->
            mProgressIndicator.hide()

            when (responseData) {
                1 -> {
                    Intent(applicationContext, MainActivity::class.java).also {
                        it.putExtra("com.frafio.myfinance.userRequest", true)
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }
                2 -> mEmailLayout.error = "L'email inserita non è ben formata."
                3 -> mPasswordLayout.error = "La password inserita non è corretta."
                4 -> mEmailLayout.error = "L'email inserita non ha un account associato."
                is String -> layout.snackbar(responseData)
            }
        })
    }

    override fun onAuthFailure(errorCode: Int) {
        mProgressIndicator.hide()

        when (errorCode) {
            1 -> mEmailLayout.error = "Inserisci la tua email."
            2 -> mPasswordLayout.error = "Inserisci la password."
            3 -> mPasswordLayout.error = "La password deve essere lunga almeno 8 caratteri!"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            viewModel.onGoogleRequest(data)
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent(applicationContext, MainActivity::class.java).also {
                it.putExtra("com.frafio.myfinance.userRequest", true)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
    }
}