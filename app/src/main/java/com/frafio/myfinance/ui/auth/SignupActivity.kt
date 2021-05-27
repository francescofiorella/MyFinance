package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.ActivitySignupBinding
import com.frafio.myfinance.utils.snackbar
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignupActivity : AppCompatActivity(), AuthListener, KodeinAware {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        // toolbar
        setSupportActionBar(binding.signupToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onAuthStarted() {
        binding.signupProgressIndicator.show()

        binding.signupNameInputLayout.isErrorEnabled = false
        binding.signupEmailInputLayout.isErrorEnabled = false
        binding.signupPasswordInputLayout.isErrorEnabled = false
        binding.signupPasswordConfirmInputLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<Any>) {
        response.observe(this, { responseData ->
            if (responseData != 1) {
                binding.signupProgressIndicator.hide()
            }

            when (responseData) {
                1 -> viewModel.updateUserData()
                2 -> binding.signupPasswordConfirmInputLayout.error =
                    "Le password inserite non corrispondono!"
                3 -> binding.signupEmailInputLayout.error = "L'email inserita non è ben formata."
                4 -> binding.signupEmailInputLayout.error =
                    "L'email inserita ha già un account associato."
                "List updated" ->
                    Intent().also {
                        setResult(RESULT_OK, it)
                        finish()
                    }
                is String -> binding.root.snackbar(responseData)
            }
        })
    }

    override fun onAuthFailure(errorCode: Int) {
        binding.signupProgressIndicator.hide()

        when (errorCode) {
            1 -> binding.signupNameInputLayout.error = "Inserisci nome e cognome."
            2 -> binding.signupEmailInputLayout.error = "Inserisci la tua email."
            3 -> binding.signupPasswordInputLayout.error = "Inserisci la password."
            4 -> binding.signupPasswordInputLayout.error =
                "La password deve essere lunga almeno 8 caratteri!"
            5 -> binding.signupPasswordConfirmInputLayout.error =
                "Inserisci nuovamente la password."
            6 -> binding.signupPasswordConfirmInputLayout.error =
                "Le password inserite non corrispondono!"
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