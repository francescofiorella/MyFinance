package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.icon_bg)

        val fAuth = FirebaseAuth.getInstance()

        if (fAuth.currentUser != null) {
            HomeActivity::class.java
            // metodo per aggiornare i progressi dell'utente
            HomeActivity.PURCHASE_LIST = mutableListOf()
            HomeActivity.PURCHASE_ID_LIST = mutableListOf()
            val fStore = FirebaseFirestore.getInstance()
            fStore.collection("purchases").whereEqualTo("email", fAuth.currentUser!!.email)
                .orderBy("year", Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING)
                .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
                .orderBy("price", Query.Direction.DESCENDING)
                .get().addOnSuccessListener { queryDocumentSnapshots ->
                    for ((position, document) in queryDocumentSnapshots.withIndex()) {
                        val purchase = document.toObject(Purchase::class.java)
                        HomeActivity.PURCHASE_ID_LIST.add(position, document.id)
                        HomeActivity.PURCHASE_LIST.add(position, purchase)
                    }
                    val activityOptionsCompat = ActivityOptionsCompat
                        .makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out)
                    Intent(applicationContext, HomeActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it, activityOptionsCompat.toBundle())
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! " + e.localizedMessage)
                }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val activityOptionsCompat = ActivityOptionsCompat
                    .makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out)
                Intent(applicationContext, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it, activityOptionsCompat.toBundle())
                }
            }, 500)
        }
    }
}