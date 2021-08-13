package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivitySignupBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.snackbar
import org.kodein.di.generic.instance

class SignupActivity : BaseActivity(), AuthListener {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

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

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code != AuthCode.SIGNUP_SUCCESS.code) {
                binding.signupProgressIndicator.hide()
            }

            when (authResult.code) {
                AuthCode.SIGNUP_SUCCESS.code -> viewModel.updateUserData()

                AuthCode.WEAK_PASSWORD.code ->
                    binding.signupPasswordConfirmInputLayout.error = authResult.message

                AuthCode.EMAIL_NOT_WELL_FORMED.code or AuthCode.EMAIL_ALREADY_ASSOCIATED.code ->
                    binding.signupEmailInputLayout.error = authResult.message

                AuthCode.PROFILE_NOT_UPDATED.code
                        or AuthCode.SIGNUP_FAILURE.code
                        or AuthCode.USER_DATA_NOT_UPDATED.code ->
                    binding.root.snackbar(authResult.message)

                AuthCode.USER_DATA_UPDATED.code ->
                    Intent().also {
                        setResult(RESULT_OK, it)
                        finish()
                    }

                else -> Unit
            }
        })
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.signupProgressIndicator.hide()

        when (authResult.code) {
            AuthCode.EMPTY_NAME.code -> binding.signupNameInputLayout.error = authResult.message

            AuthCode.EMPTY_EMAIL.code ->
                binding.signupEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code or AuthCode.SHORT_PASSWORD.code ->
                binding.signupPasswordInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD_CONFIRM.code or AuthCode.PASSWORD_NOT_MATCH.code ->
                binding.signupPasswordConfirmInputLayout.error = authResult.message

            else -> Unit
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