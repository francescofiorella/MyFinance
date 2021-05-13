package com.frafio.myfinance.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.ActivitySplashScreenBinding
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.util.snackbar
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SplashScreenActivity : AppCompatActivity(), SplashScreenListener, KodeinAware {

    lateinit var binding: ActivitySplashScreenBinding
    private lateinit var viewModel: SplashScreenViewModel

    override val kodein by kodein()
    private val factory: SplashScreenViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        viewModel = ViewModelProvider(this, factory).get(SplashScreenViewModel::class.java)

        viewModel.listener = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        viewModel.checkUser()
    }

    override fun onComplete(response: LiveData<Any>) {
        response.observe(this, { value ->
            when (value) {
                "User logged" -> viewModel.updateUserData()

                "User not logged" -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        ActivityOptionsCompat
                            .makeCustomAnimation(
                                applicationContext,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            ).also { options ->
                                Intent(applicationContext, LoginActivity::class.java).also {
                                    it.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(it, options.toBundle())
                                }
                            }
                    }, 500)
                }

                "List updated" -> {
                    ActivityOptionsCompat
                        .makeCustomAnimation(
                            applicationContext,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        ).also { options ->
                            Intent(applicationContext, HomeActivity::class.java).also {
                                it.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(it, options.toBundle())
                            }
                        }
                }

                is String -> binding.root.snackbar(value)
            }
        })
    }
}