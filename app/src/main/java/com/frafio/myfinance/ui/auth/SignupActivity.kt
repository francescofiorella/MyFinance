package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCodeIT
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivitySignupBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.snackBar
import org.kodein.di.generic.instance

class SignupActivity : BaseActivity(), AuthListener {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

    private val factory: AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.authListener = this
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
            if (authResult.code != AuthCodeIT.SIGNUP_SUCCESS.code) {
                binding.signupProgressIndicator.hide()
            }

            when (authResult.code) {
                AuthCodeIT.SIGNUP_SUCCESS.code -> viewModel.updateUserData()

                AuthCodeIT.WEAK_PASSWORD.code ->
                    binding.signupPasswordConfirmInputLayout.error = authResult.message

                AuthCodeIT.EMAIL_NOT_WELL_FORMED.code ->
                    binding.signupEmailInputLayout.error = authResult.message

                AuthCodeIT.EMAIL_ALREADY_ASSOCIATED.code ->
                    binding.signupEmailInputLayout.error = authResult.message

                AuthCodeIT.PROFILE_NOT_UPDATED.code -> snackBar(authResult.message)

                AuthCodeIT.SIGNUP_FAILURE.code -> snackBar(authResult.message)

                AuthCodeIT.USER_DATA_NOT_UPDATED.code -> snackBar(authResult.message)

                AuthCodeIT.USER_DATA_UPDATED.code ->
                    Intent().also {
                        setResult(RESULT_OK, it)
                        finish()
                    }

                else -> snackBar(authResult.message)
            }
        })
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.signupProgressIndicator.hide()

        when (authResult.code) {
            AuthCodeIT.EMPTY_NAME.code -> binding.signupNameInputLayout.error = authResult.message

            AuthCodeIT.EMPTY_EMAIL.code ->
                binding.signupEmailInputLayout.error = authResult.message

            AuthCodeIT.EMPTY_PASSWORD.code ->
                binding.signupPasswordInputLayout.error = authResult.message

            AuthCodeIT.SHORT_PASSWORD.code ->
                binding.signupPasswordInputLayout.error = authResult.message

            AuthCodeIT.EMPTY_PASSWORD_CONFIRM.code ->
                binding.signupPasswordConfirmInputLayout.error = authResult.message

            AuthCodeIT.PASSWORD_NOT_MATCH.code ->
                binding.signupPasswordConfirmInputLayout.error = authResult.message

            else -> snackBar(authResult.message)
        }
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressed()
    }
}