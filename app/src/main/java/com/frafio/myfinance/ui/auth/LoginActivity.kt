package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.ui.home.MainActivity.Companion.CURRENT_USER
import com.frafio.myfinance.R
import com.frafio.myfinance.data.User
import com.frafio.myfinance.databinding.ActivityLoginBinding
import com.frafio.myfinance.ui.home.MainActivity
import com.frafio.myfinance.util.snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity(), AuthListener {

    // definizione variabili
    lateinit var layout: RelativeLayout

    lateinit var mToolbar: MaterialToolbar
    lateinit var mEmailLayout: TextInputLayout
    lateinit var mPasswordLayout: TextInputLayout
    lateinit var mEmail: TextInputEditText
    lateinit var mPassword: TextInputEditText
    lateinit var mLoginBtn: MaterialButton
    lateinit var mGoogleBtn: MaterialButton
    lateinit var mResetBtn: TextView
    lateinit var mSignupBtn: TextView
    lateinit var mProgressIndicator: LinearProgressIndicator

    // firebase
    private lateinit var fAuth: FirebaseAuth

    //login Google
    lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        private val RC_SIGN_IN = 101
        private val TAG = LoginActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        /*fAuth = FirebaseAuth.getInstance()
        if (fAuth.currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // toolbar
        mToolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(mToolbar)

        // collegamento view
        layout = findViewById(R.id.login_layout)
        mEmail = findViewById(R.id.login_nameInputText)
        mPassword = findViewById(R.id.login_passwordInputText)

        mEmailLayout = findViewById(R.id.login_emailInputLayout)
        mPasswordLayout = findViewById(R.id.login_passwordInputLayout)

        mLoginBtn = findViewById(R.id.loginButton)
        mResetBtn = findViewById(R.id.resetPassTextView)
        mGoogleBtn = findViewById(R.id.googleButton)
        mProgressIndicator = findViewById(R.id.login_progressIindicator)
        mSignupBtn = findViewById(R.id.lRegisterTextView)*/

        /*mLoginBtn.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                mEmailLayout.error = "Inserisci la tua email."
                return@setOnClickListener
            } else {
                mEmailLayout.isErrorEnabled = false
            }

            if (TextUtils.isEmpty(password)) {
                mPasswordLayout.error = "Inserisci la password."
                return@setOnClickListener
            } else {
                mPasswordLayout.isErrorEnabled = false
            }

            if (password.length < 8) {
                mPasswordLayout.error = "La password deve essere lunga almeno 8 caratteri!"
                return@setOnClickListener
            } else {
                mPasswordLayout.isErrorEnabled = false
            }

            mProgressIndicator.show()

            // authenticate the user
            fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("com.frafio.myfinance.userRequest", true)
                startActivity(intent)
                finish()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        val errorCode = e.errorCode
                        if (errorCode == "ERROR_INVALID_EMAIL") {
                            mEmailLayout.error = "L'email inserita non è ben formata."
                        } else if (errorCode == "ERROR_WRONG_PASSWORD") {
                            mPasswordLayout.error = "La password inserita non è corretta."
                        }
                    }
                    is FirebaseAuthInvalidUserException -> {
                        val errorCode = e.errorCode
                        if (errorCode == "ERROR_USER_NOT_FOUND") {
                            mEmailLayout.error = "L'email inserita non ha un account associato."
                        } else if (errorCode == "ERROR_USER_DISABLED") {
                            mEmailLayout.error = "Il tuo account è stato disabilitato."
                        } else {
                            showSnackbar(e.getLocalizedMessage() ?: "Accesso fallito.")
                        }
                    }
                    else -> {
                        showSnackbar("Accesso fallito.")
                    }
                }
                mProgressIndicator.hide()
            }
        }

        // Configure Google Sign In

        // Configure Google Sign In
        createRequest()
        mGoogleBtn.setOnClickListener {
            mProgressIndicator.show()
            signIn()
        }

        // reset password tramite email
        mResetBtn.setOnClickListener {
            val email = mEmail.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                mEmailLayout.error = "Inserisci la tua email."
                return@setOnClickListener
            }
            fAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    showSnackbar("Email inviata. Controlla la tua posta!") }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    if (e is FirebaseTooManyRequestsException) {
                        showSnackbar("Email non inviata! Sono state effettuate troppe richieste.")
                    } else {
                        showSnackbar("Errore! Email non inviata.")
                    }
                }
        }

        mSignupBtn.setOnClickListener {
            startActivityForResult(Intent(applicationContext, SignupActivity::class.java), 1)
        }*/
    }

    override fun onStarted() {
        snackbar(layout, "Login Started")
    }

    override fun onSuccess() {
        snackbar(layout, "Login Success")
    }

    override fun onFailure(message: String) {
        snackbar(layout, message)
    }

    /*// Configure Google Sign In
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                // crea utente in Firestore se non esiste
                createFirestoreUser()
            } else {
                // If sign in fails, display a message to the user.
                showSnackbar("Accesso con Google fallito.")
                Log.e(TAG, "Error! ${task.exception?.localizedMessage}")
                mProgressIndicator.hide()
            }
        }
    }

    // se accedi con Google crea l'utente in firestore (se non è già presente)
    private fun createFirestoreUser() {
        val fUser = fAuth.currentUser
        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("users").whereEqualTo("email", fUser?.email).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.isEmpty) {
                    val user = User(fUser?.displayName, fUser?.email, fUser?.photoUrl.toString())
                    val fStore1 = FirebaseFirestore.getInstance()
                    fStore1.collection("users").document(fUser!!.uid).set(user).addOnSuccessListener {
                        CURRENT_USER = user
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.putExtra("com.frafio.myfinance.userRequest", true)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { e ->
                        showSnackbar("Accesso con Google non effettuato correttamente.")
                        Log.e(TAG, "Error! " + e.localizedMessage)
                        mProgressIndicator.hide()
                        fAuth.signOut()
                    }
                } else {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("com.frafio.myfinance.userRequest", true)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { e ->
                showSnackbar("Accesso con Google non effettuato correttamente.")
                Log.e(TAG, "Error! ${e.localizedMessage}")
                mProgressIndicator.hide()
                fAuth.signOut()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Error! ${e.localizedMessage}")
                showSnackbar("Accesso con Google fallito.")
                mProgressIndicator.hide()
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("com.frafio.myfinance.userRequest", true)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun showSnackbar(string: String) {
        val snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.snackbar))
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.inverted_primary_text))
        val tv = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito
        snackbar.show()
    }*/
}