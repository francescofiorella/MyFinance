package com.frafio.myfinance

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.frafio.myfinance.MainActivity.Companion.CURRENT_USER
import com.frafio.myfinance.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    // definizione variabili
    lateinit var layout: RelativeLayout
    var nunito: Typeface? = null

    lateinit var mToolbar: MaterialToolbar
    lateinit var mFullNameLayout: TextInputLayout
    lateinit var mEmailLayout:TextInputLayout
    lateinit var mPasswordLayout:TextInputLayout
    lateinit var mPasswordAgainLayout:TextInputLayout
    lateinit var mFullName: TextInputEditText
    lateinit var mEmail:TextInputEditText
    lateinit var mPassword:TextInputEditText
    lateinit var mPasswordAgain:TextInputEditText
    lateinit var mSignupButton: MaterialButton
    lateinit var mLoginBtn: TextView
    lateinit var mProgressIndicator: LinearProgressIndicator

    // firebase
    lateinit var fAuth: FirebaseAuth
    var fUser: FirebaseUser? = null

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

        // toolbar
        mToolbar = findViewById(R.id.signup_toolbar)
        setSupportActionBar(mToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // collegamento view
        layout = findViewById(R.id.signup_layout)
        mFullName = findViewById(R.id.signup_nameInputText)
        mEmail = findViewById(R.id.signup_emailInputText)
        mPassword = findViewById(R.id.signup_passwordInputText)
        mPasswordAgain = findViewById(R.id.signup_passwordAgainInputText)

        mFullNameLayout = findViewById(R.id.signup_nameInputLayout)
        mEmailLayout = findViewById(R.id.signup_emailInputLayout)
        mPasswordLayout = findViewById(R.id.signup_passwordInputLayout)
        mPasswordAgainLayout = findViewById(R.id.signup_passwordAgainInputLayout)

        mSignupButton = findViewById(R.id.signupButton)
        mLoginBtn = findViewById(R.id.sLogintextView)
        mProgressIndicator = findViewById(R.id.signup_progressIndicator)

        fAuth = FirebaseAuth.getInstance()

        mSignupButton.setOnClickListener {
            val fullName = mFullName.text.toString().trim()
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()
            val passwordAgain = mPasswordAgain.text.toString().trim()

            // controlla la info aggiunte
            if (TextUtils.isEmpty(fullName)) {
                mFullNameLayout.error = "Inserisci nome e cognome."
                return@setOnClickListener
            } else {
                mFullNameLayout.isErrorEnabled = false
            }

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

            if (passwordAgain.length == 0) {
                mPasswordAgainLayout.error = "Inserisci nuovamente la password."
                return@setOnClickListener
            } else {
                mPasswordAgainLayout.isErrorEnabled = false
            }

            if (passwordAgain != password) {
                mPasswordAgainLayout.error = "Le password inserite non corrispondono!"
                return@setOnClickListener
            } else {
                mPasswordAgainLayout.isErrorEnabled = false
            }

            mProgressIndicator.show()

            // register the user in firebase
            signUp(fullName, email, password)
        }

        mLoginBtn.setOnClickListener {
            finish()
        }
    }

    // signup method
    private fun signUp(fullName: String, email: String, password: String) {
        fAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // verify the email
                fUser = fAuth.currentUser
                fUser?.sendEmailVerification()?.addOnSuccessListener {
                    Log.d(TAG, "Email di verifica inviata!")
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                }

                // store data in firestore
                createFirestoreUser(fullName)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        mPasswordLayout.error = "La password inserita non è abbastanza sicura."
                    is FirebaseAuthInvalidCredentialsException ->
                        mEmailLayout.error = "L'email inserita non è ben formata."
                    is FirebaseAuthUserCollisionException ->
                        mEmailLayout.error = "L'email inserita ha già un account associato."
                    else -> showSnackbar("Registrazione fallita.")
                }

                mProgressIndicator.hide()
            }
    }

    // crea l'utente in firebase
    private fun createFirestoreUser(fullName: String) {
        val fStore = FirebaseFirestore.getInstance()

        // insert name into fUser
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
        fUser?.updateProfile(profileUpdates)?.addOnSuccessListener {
            // store data in firestore
            val user = User(fullName, fUser?.email, "")
            fStore.collection("users").document(fUser!!.uid).set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "onSuccess: user Profile is created for ${fUser?.uid}")
                    CURRENT_USER = user
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! " + e.localizedMessage)
                    mProgressIndicator.hide()
                    showSnackbar("Account non creato correttamente!")
                }
        }?.addOnFailureListener {
            showSnackbar("Account non creato correttamente!")
            mProgressIndicator.hide()
        }
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSnackbar(string: String) {
        val snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.snackbar))
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.inverted_primary_text))
        val tv = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito
        snackbar.show()
    }
}