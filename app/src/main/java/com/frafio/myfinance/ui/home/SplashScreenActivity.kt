package com.frafio.myfinance.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityOptionsCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val fAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val firstActivity = if (fAuth.currentUser != null) {
                MainActivity::class.java

            } else {
                LoginActivity::class.java
            }

            val activityOptionsCompat = ActivityOptionsCompat
                .makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out)
            val intent = Intent(applicationContext, firstActivity)
            startActivity(intent, activityOptionsCompat.toBundle())
            finish()
        }, 1000)
    }
}