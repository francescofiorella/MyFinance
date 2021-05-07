package com.frafio.myfinance.ui.splash

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
import com.frafio.myfinance.data.manager.UserManager
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.snackbar
import com.google.firebase.auth.FirebaseAuth
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class SplashScreenActivity : AppCompatActivity(), ManagerListener, KodeinAware {

    lateinit var layout: ConstraintLayout

    override val kodein by kodein()
    private val fAuth: FirebaseAuth by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        PurchaseManager.managerListener = this

        layout = findViewById(R.id.splashScreen_layout)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.icon_bg)

        if (fAuth.currentUser != null) {
            UserManager.updateUser(fAuth.currentUser!!)
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