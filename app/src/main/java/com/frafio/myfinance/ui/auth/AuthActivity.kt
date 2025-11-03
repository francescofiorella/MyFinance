package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.databinding.ActivityAuthBinding
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.animateRoot
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.snackBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AuthActivity : AppCompatActivity(), AuthListener {

    companion object {
        const val INTENT_USER_REQUEST: String = "com.frafio.myfinance.USER_REQUEST"
        const val INTENT_USER_NAME: String = "com.frafio.myfinance.USER_NAME"
    }

    // binding
    private lateinit var binding: ActivityAuthBinding

    // viewModel
    private val viewModel by viewModels<AuthViewModel>()

    // login Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val googleSignInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            val data: Intent? = result.data
            viewModel.onGoogleRequest(data)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.viewModel = viewModel

        viewModel.authListener = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                // Apply padding
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }

        binding.authFullNameInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authFullNameInputLayout.isErrorEnabled = false
        }

        binding.authEmailInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authEmailInputLayout.isErrorEnabled = false
        }
        binding.authPasswordInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authPasswordInputLayout.isErrorEnabled = false
        }

        binding.authConfirmPasswordInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.authConfirmPasswordInputLayout.isErrorEnabled = false
        }

        binding.authBackArrow.setOnClickListener { prepareLoginLayout() }
        if (savedInstanceState == null) {
            prepareLoginLayout(false)
        } else {
            if (viewModel.isSigningUp) {
                prepareSignUpLayout()
            } else {
                prepareLoginLayout(false)
            }
        }
    }

    fun onResetButtonClick(view: View) {
        if (resources.getBoolean(R.bool.is600dp)) {
            val sideSheetDialog = SideSheetDialog(view.context)
            sideSheetDialog.setContentView(R.layout.layout_reset_password_sheet)
            defineSheetInterface(
                sideSheetDialog.findViewById(android.R.id.content)!!,
                viewModel,
                sideSheetDialog::hide
            )
            sideSheetDialog.show()
        } else {
            val modalBottomSheet = ModalBottomSheet(
                this,
                viewModel
            )
            modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        }
    }

    class ModalBottomSheet(
        private val activity: AuthActivity,
        private val viewModel: AuthViewModel
    ) : BottomSheetDialogFragment() {
        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout =
                inflater.inflate(R.layout.layout_reset_password_sheet, container, false)
            activity.defineSheetInterface(layout, viewModel, this::dismiss)
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        viewModel: AuthViewModel,
        dismissFun: () -> Unit
    ) {
        val emailInputLayout = layout.findViewById<TextInputLayout>(R.id.reset_password_emailInputLayout)
        val emailInputText = layout.findViewById<TextInputEditText>(R.id.reset_password_emailInputText)
        val sendButton = layout.findViewById<Button>(R.id.send_mail_btn)

        emailInputText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank()) {
                emailInputLayout.isErrorEnabled = false
                sendButton.isEnabled = true
            } else {
                sendButton.isEnabled = false
            }
        }
        sendButton.setOnClickListener {
            binding.googleButton.isEnabled = false
            binding.authButton.isEnabled = false

            clearErrors()
            emailInputLayout.isErrorEnabled = false

            val email = emailInputText.text.toString().trim()
            viewModel.resetPassword(email)
            dismissFun()
        }
    }

    private fun clearErrors() {
        binding.authFullNameInputLayout.isErrorEnabled = false
        binding.authEmailInputLayout.isErrorEnabled = false
        binding.authPasswordInputLayout.isErrorEnabled = false
        binding.authConfirmPasswordInputLayout.isErrorEnabled = false
    }

    private fun clearInputs() {
        binding.authFullNameInputText.clearText()
        binding.authFullNameInputText.clearFocus()
        binding.authEmailInputText.clearText()
        binding.authEmailInputText.clearFocus()
        binding.authPasswordInputText.clearText()
        binding.authPasswordInputText.clearFocus()
        binding.authConfirmPasswordInputText.clearText()
        binding.authConfirmPasswordInputText.clearFocus()
    }

    override fun onStart() {
        super.onStart()
        clearErrors()
    }

    fun onGoogleButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        binding.authProgressIndicator.show()

        binding.googleButton.isEnabled = false
        binding.authButton.isEnabled = false

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

    fun prepareSignUpLayout(clearInputs: Boolean = true) {
        if (clearInputs) {
            clearErrors()
            clearInputs()
        }

        viewModel.isSigningUp = true
        binding.authParentLayout.animateRoot()

        binding.authSwitchTextView.setOnClickListener { prepareLoginLayout() }
        binding.authButton.setOnClickListener { viewModel.onSignupButtonClick() }

        binding.authBackArrow.instantShow()
        binding.authFullNameInputLayout.instantShow()
        binding.authPasswordInputLayout.hint = getString(R.string.signup_password)
        binding.authConfirmPasswordInputLayout.instantShow()
        binding.authButton.text = getString(R.string.signup)
        binding.authResetPassTextView.text = getString(R.string.auth_or)
        binding.authResetPassTextView.isClickable = false
        binding.authSwitchTextView.text = getString(R.string.signup_login)


        if (!onBackPressedDispatcher.hasEnabledCallbacks()) {
            onBackPressedDispatcher.addCallback {
                prepareLoginLayout()
                isEnabled = false
            }
        }
    }

    fun prepareLoginLayout(clearInputs: Boolean = true) {
        if (clearInputs) {
            clearErrors()
            clearInputs()
        }

        viewModel.isSigningUp = false
        binding.authParentLayout.animateRoot()

        binding.authSwitchTextView.setOnClickListener { prepareSignUpLayout() }
        binding.authButton.setOnClickListener { viewModel.onLoginButtonClick() }

        binding.authBackArrow.instantHide()
        binding.authFullNameInputLayout.instantHide()
        binding.authPasswordInputLayout.hint = getString(R.string.login_password)
        binding.authConfirmPasswordInputLayout.instantHide()
        binding.authButton.text = getString(R.string.login)
        binding.authResetPassTextView.text = getString(R.string.forgotten_password)
        binding.authResetPassTextView.isClickable = true
        binding.authSwitchTextView.text = getString(R.string.login_signup)
    }

    override fun onAuthStarted() {
        binding.authProgressIndicator.show()

        binding.authFullNameInputLayout.isEnabled = false
        binding.authEmailInputLayout.isEnabled = false
        binding.authPasswordInputLayout.isEnabled = false
        binding.authConfirmPasswordInputLayout.isEnabled = false
        binding.authResetPassTextView.isEnabled = false

        binding.googleButton.isEnabled = false
        binding.authButton.isEnabled = false

        clearErrors()
    }

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code != AuthCode.LOGIN_SUCCESS.code || authResult.code != AuthCode.SIGNUP_SUCCESS.code) {
                binding.authProgressIndicator.hide()

                binding.authFullNameInputLayout.isEnabled = true
                binding.authEmailInputLayout.isEnabled = true
                binding.authPasswordInputLayout.isEnabled = true
                binding.authConfirmPasswordInputLayout.isEnabled = true
                binding.authResetPassTextView.isEnabled = true

                binding.googleButton.isEnabled = true
                binding.authButton.isEnabled = true
            }

            when (authResult.code) {
                AuthCode.LOGIN_SUCCESS.code,
                AuthCode.SIGNUP_SUCCESS.code ->
                    goToHomeActivity()

                AuthCode.INVALID_EMAIL.code,
                AuthCode.USER_NOT_FOUND.code,
                AuthCode.EMAIL_NOT_WELL_FORMED.code,
                AuthCode.EMAIL_ALREADY_ASSOCIATED.code ->
                    binding.authEmailInputLayout.error = authResult.message

                AuthCode.WRONG_PASSWORD.code ->
                    binding.authPasswordInputLayout.error = authResult.message

                AuthCode.WEAK_PASSWORD.code ->
                    binding.authConfirmPasswordInputLayout.error = authResult.message

                AuthCode.GOOGLE_LOGIN_FAILURE.code,
                AuthCode.USER_DISABLED.code,
                AuthCode.LOGIN_FAILURE.code,
                AuthCode.SIGNUP_PROFILE_NOT_UPDATED.code,
                AuthCode.SIGNUP_FAILURE.code ->
                    snackBar(authResult.message)

                else -> snackBar(authResult.message)
            }
        }
    }

    override fun onAuthFailure(authResult: AuthResult) {
        binding.authProgressIndicator.hide()

        binding.authFullNameInputLayout.isEnabled = true
        binding.authEmailInputLayout.isEnabled = true
        binding.authPasswordInputLayout.isEnabled = true
        binding.authConfirmPasswordInputLayout.isEnabled = true
        binding.authResetPassTextView.isEnabled = true

        binding.googleButton.isEnabled = true
        binding.authButton.isEnabled = true

        when (authResult.code) {
            AuthCode.EMPTY_NAME.code ->
                binding.authFullNameInputLayout.error = authResult.message

            AuthCode.EMPTY_EMAIL.code ->
                binding.authEmailInputLayout.error = authResult.message

            AuthCode.EMPTY_PASSWORD.code,
            AuthCode.SHORT_PASSWORD.code ->
                binding.authPasswordInputLayout.error = authResult.message

            AuthCode.EMPTY_CONFIRM_PASSWORD.code,
            AuthCode.PASSWORD_NOT_MATCH.code ->
                binding.authConfirmPasswordInputLayout.error = authResult.message

            else -> snackBar(authResult.message)
        }
    }

    private fun goToHomeActivity() {
        viewModel.getUserName().also { userName ->
            Intent(applicationContext, HomeActivity::class.java).also {
                it.putExtra(INTENT_USER_REQUEST, true)
                it.putExtra(INTENT_USER_NAME, userName)
                startActivity(it)
                finish()
            }
        }

    }
}