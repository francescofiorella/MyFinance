package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AUTH_RESULT
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
            if (authResult.code != AUTH_RESULT.SIGNUP_SUCCESS.code) {
                binding.signupProgressIndicator.hide()
            }

            when (authResult.code) {
                AUTH_RESULT.SIGNUP_SUCCESS.code -> viewModel.updateUserData()

                AUTH_RESULT.WEAK_PASSWORD.code ->
                    binding.signupPasswordConfirmInputLayout.error = authResult.message

                AUTH_RESULT.EMAIL_NOT_WELL_FORMED.code or AUTH_RESULT.EMAIL_ALREADY_ASSOCIATED.code ->
                    binding.signupEmailInputLayout.error = authResult.message

                AUTH_RESULT.PROFILE_NOT_UPDATED.code
                        or AUTH_RESULT.SIGNUP_FAILURE.code
                        or AUTH_RESULT.USER_DATA_NOT_UPDATED.code ->
                    binding.root.snackbar(authResult.message)

                AUTH_RESULT.USER_DATA_UPDATED.code ->
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
            AUTH_RESULT.EMPTY_NAME.code -> binding.signupNameInputLayout.error = authResult.message

            AUTH_RESULT.EMPTY_EMAIL.code ->
                binding.signupEmailInputLayout.error = authResult.message

            AUTH_RESULT.EMPTY_PASSWORD.code or AUTH_RESULT.SHORT_PASSWORD.code ->
                binding.signupPasswordInputLayout.error = authResult.message

            AUTH_RESULT.EMPTY_PASSWORD_CONFIRM.code or AUTH_RESULT.PASSWORD_NOT_MATCH.code ->
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