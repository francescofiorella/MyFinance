package com.frafio.myfinance.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivitySplashScreenBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.setFullScreenEnabled
import com.frafio.myfinance.utils.snackbar
import org.kodein.di.generic.instance

class SplashScreenActivity : BaseActivity(), SplashScreenListener {

    companion object {
        private const val SPLASH_TIME: Long = 500
        private const val LOAD_DELAY: Long = 2000
    }

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var viewModel: SplashScreenViewModel

    private val factory: SplashScreenViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        viewModel = ViewModelProvider(this, factory).get(SplashScreenViewModel::class.java)

        viewModel.listener = this

        viewModel.checkUser()
    }

    override fun onComplete(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    viewModel.updateUserData()

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.progressBar.show()
                    }, LOAD_DELAY)
                }

                AuthCode.USER_NOT_LOGGED.code -> {
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
                    }, SPLASH_TIME)
                }

                AuthCode.USER_DATA_UPDATED.code -> {
                    ActivityOptionsCompat.makeCustomAnimation(
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

                AuthCode.USER_DATA_NOT_UPDATED.code -> snackbar(authResult.message)

                else -> Unit
            }
        })
    }
}