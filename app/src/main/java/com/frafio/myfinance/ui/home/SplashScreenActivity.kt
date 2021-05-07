package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.data.manager.ManagerListener
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.utils.snackbar
import com.google.firebase.auth.FirebaseAuth


class SplashScreenActivity : AppCompatActivity(), ManagerListener {

    lateinit var layout: ConstraintLayout

    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        PurchaseManager.managerListener = this

        layout = findViewById(R.id.splashScreen_layout)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.icon_bg)

        val fAuth = FirebaseAuth.getInstance()

        if (fAuth.currentUser != null) {
            PurchaseManager.updatePurchaseList()
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

    override fun onManagerSuccess() {
        val activityOptionsCompat = ActivityOptionsCompat
            .makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out)
        Intent(applicationContext, HomeActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it, activityOptionsCompat.toBundle())
        }
    }

    override fun onManagerFailure(message: String) {
        layout.snackbar(message)
    }
}