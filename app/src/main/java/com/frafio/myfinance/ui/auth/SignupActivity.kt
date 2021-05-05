package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.ActivitySignupBinding
import com.frafio.myfinance.utils.snackbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignupActivity : AppCompatActivity(), AuthListener, KodeinAware {

    // definizione variabili
    private lateinit var layout: RelativeLayout

    private lateinit var mToolbar: MaterialToolbar
    private lateinit var mFullNameLayout: TextInputLayout
    private lateinit var mEmailLayout:TextInputLayout
    private lateinit var mPasswordLayout:TextInputLayout
    private lateinit var mPasswordConfirmLayout:TextInputLayout
    private lateinit var mProgressIndicator: LinearProgressIndicator

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        val viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        // toolbar
        mToolbar = findViewById(R.id.signup_toolbar)
        setSupportActionBar(mToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // collegamento view
        layout = findViewById(R.id.signup_layout)

        mFullNameLayout = findViewById(R.id.signup_nameInputLayout)
        mEmailLayout = findViewById(R.id.signup_emailInputLayout)
        mPasswordLayout = findViewById(R.id.signup_passwordInputLayout)
        mPasswordConfirmLayout = findViewById(R.id.signup_passwordConfirmInputLayout)

        mProgressIndicator = findViewById(R.id.signup_progressIndicator)
    }

    override fun onAuthStarted() {
        mProgressIndicator.show()

        mFullNameLayout.isErrorEnabled = false
        mEmailLayout.isErrorEnabled = false
        mPasswordLayout.isErrorEnabled = false
        mPasswordConfirmLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<Any>) {
        response.observe(this, Observer { responseData ->
            mProgressIndicator.hide()

            when (responseData) {
                1 -> {
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
                2 -> mPasswordConfirmLayout.error = "Le password inserite non corrispondono!"
                3 -> mEmailLayout.error = "L'email inserita non è ben formata."
                4 -> mEmailLayout.error = "L'email inserita ha già un account associato."
                is String -> layout.snackbar(responseData)
            }
        })
    }

    override fun onAuthFailure(errorCode: Int) {
        mProgressIndicator.hide()

        when (errorCode) {
            1 -> mFullNameLayout.error = "Inserisci nome e cognome."
            2 -> mEmailLayout.error = "Inserisci la tua email."
            3 -> mPasswordLayout.error = "Inserisci la password."
            4 -> mPasswordLayout.error = "La password deve essere lunga almeno 8 caratteri!"
            5 -> mPasswordConfirmLayout.error = "Inserisci nuovamente la password."
            6 -> mPasswordConfirmLayout.error = "Le password inserite non corrispondono!"
        }
    }

    fun goToLoginActivity(view: View) {
        finish()
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}