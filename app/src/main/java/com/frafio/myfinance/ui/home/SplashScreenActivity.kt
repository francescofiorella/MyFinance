package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.icon_bg)

        val fAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val firstActivity = if (fAuth.currentUser != null) {
                HomeActivity::class.java

            } else {
                LoginActivity::class.java
            }

            val activityOptionsCompat = ActivityOptionsCompat
                    .makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out)
            Intent(applicationContext, firstActivity).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it, activityOptionsCompat.toBundle())
            }
        }, 500)
    }
}