package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.databinding.ActivitySignupBinding
import com.frafio.myfinance.utils.snackBar

class SignupActivity : AppCompatActivity(), AuthListener {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        binding.viewModel = viewModel

        viewModel.authListener = this
    }

    override fun onStart() {
        super.onStart()
        binding.signupNameInputLayout.isErrorEnabled = false
        binding.signupEmailInputLayout.isErrorEnabled = false
        binding.signupPasswordInputLayout.isErrorEnabled = false
        binding.signupPasswordConfirmInputLayout.isErrorEnabled = false
    }

    override fun onAuthStarted() {
        binding.signupProgressIndicator.show()
        binding.signupButton.isEnabled = false

        binding.signupNameInputLayout.isErrorEnabled = false
        binding.signupEmailInputLayout.isErrorEnabled = false
        binding.signupPasswordInputLayout.isErrorEnabled = false
        binding.signupPasswordConfirmInputLayout.isErrorEnabled = false
    }

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code != AuthCode.SIGNUP_SUCCESS.code) {
                binding.signupProgressIndicator.hide()
                binding.signupButton.isEnabled = true
            }

            when (authResult.code) {
                AuthCode.SIGNUP_SUCCESS.code -> Intent().also {
                    setResult(RESULT_OK, it)
                    finish()
                }

                AuthCode.WEAK_PASSWORD.code ->
                    binding.signupPasswordConfirmInputLayout.error = authResult.message

                AuthCode.EMAIL_NOT_WELL_FORMED.code,
                AuthCode.EMAIL_ALREADY_ASSOCIATED.code ->
                    binding.signupEmailInputLayout.error = authResult.message

                AuthCode.SIGNUP_PROFILE_NOT_UPDATED.code,
                AuthCode.SIGNUP_FAILURE.code -> snackBar(authResult.message)

                else -> snackBar(authResult.message)
            }
        }
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.signupProgressIndicator.hide()
        binding.signupButton.isEnabled = true

        when (authResult.code) {
            AuthCode.EMPTY_NAME.code -> binding.signupNameInputLayout.error = authResult.message

            AuthCode.EMPTY_EMAIL.code ->
                binding.signupEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code,
            AuthCode.SHORT_PASSWORD.code ->
                binding.signupPasswordInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD_CONFIRM.code,
            AuthCode.PASSWORD_NOT_MATCH.code ->
                binding.signupPasswordConfirmInputLayout.error = authResult.message

            else -> snackBar(authResult.message)
        }
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressedDispatcher.onBackPressed()
    }
}