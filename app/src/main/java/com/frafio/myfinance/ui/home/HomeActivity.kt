package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCodeIT
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.setImageViewRoundDrawable
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.navigation.NavigationBarView
import org.kodein.di.generic.instance

class HomeActivity : BaseActivity(), HomeListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var navController: NavController

    private val factory: HomeViewModelFactory by instance()

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val purchaseRequest =
                data!!.getBooleanExtra(AddActivity.INTENT_PURCHASE_REQUEST, false)
            if (purchaseRequest) {
                if (navController.currentDestination?.id == R.id.listFragment) {
                    navController.popBackStack()
                }
                navController.navigate(R.id.listFragment)

                showSnackBar(getString(R.string.purchase_added))
            }
        }
    }

    private val logInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadDashboard()
                setDrawableToButtons(isLogged = true)
                setImageViewRoundDrawable(binding.propicImageView, viewModel.getProPic())

                val userRequest =
                    result.data?.extras?.getBoolean(LoginActivity.INTENT_USER_REQUEST, false)
                        ?: false

                val userName = result.data?.extras?.getString(LoginActivity.INTENT_USER_NAME)

                if (userRequest) {
                    showSnackBar("${getString(R.string.login_successful)} $userName")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().also {
            it.setKeepOnScreenCondition {
                !viewModel.isLayoutReady
            }
            it.setOnExitAnimationListener { splashScreenViewProvider ->
                supportFragmentManager.unregisterFragmentLifecycleCallbacks(dashboardCallback)
                splashScreenViewProvider.remove()
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        viewModel.listener = this
        binding.viewModel = viewModel

        // import db data
        viewModel.checkUser()

        // link fragment view
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController.also {
            binding.navBar?.setupWithNavController(it)
            binding.navDrawer?.setupWithNavController(it)
            //binding.navRail?.setupWithNavController(it)
        }

        binding.navRail?.setOnItemSelectedListener(navRailListener)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // fix bug (if we're not on the dashboard, fragments overlaps in the stack)
        if (navController.currentDestination?.id != R.id.dashboardFragment) {
            navController.popBackStack()
        }
    }

    private val navRailListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.dashboardFragment -> {
                navController.navigateUp()
                navController.navigate(R.id.dashboardFragment)
                true
            }

            R.id.listFragment -> {
                navController.navigateUp()
                navController.navigate(R.id.listFragment)
                true
            }

            R.id.profileFragment -> {
                navController.navigateUp()
                navController.navigate(R.id.profileFragment)
                true
            }

            R.id.menuFragment -> {
                navController.navigateUp()
                navController.navigate(R.id.menuFragment)
                true
            }

            else -> false
        }
    }

    private val dashboardCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentCreated(fm, f, savedInstanceState)
            viewModel.isLayoutReady = true
        }
    }

    private val navigationListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.label) {
                getString(R.string.nav_1) -> {
                    binding.fragmentTitle.text = destination.label
                    binding.logoutCard.instantHide()
                    binding.propicImageView.instantShow()
                }

                getString(R.string.nav_2_extended) -> {
                    binding.fragmentTitle.text = destination.label
                    binding.logoutCard.instantHide()
                    binding.propicImageView.instantShow()
                }

                getString(R.string.nav_3) -> {
                    binding.fragmentTitle.text = destination.label
                    binding.logoutCard.instantShow()
                    binding.propicImageView.instantHide()
                }

                getString(R.string.nav_4) -> {
                    binding.fragmentTitle.text = destination.label
                    binding.logoutCard.instantHide()
                    binding.propicImageView.instantShow()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(navigationListener)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(navigationListener)
    }


    fun onAddButtonClick(view: View) {
        if (viewModel.isLogged()) {
            ActivityOptionsCompat.makeClipRevealAnimation(
                view, 0, 0, view.measuredWidth, view.measuredHeight
            ).also { activityOptionsCompat ->
                Intent(applicationContext, AddActivity::class.java).also {
                    it.putExtra(
                        AddActivity.INTENT_REQUEST_CODE,
                        AddActivity.INTENT_REQUEST_ADD_CODE
                    )
                    addResultLauncher.launch(it, activityOptionsCompat)
                }
            }
        } else {
            goToLogin()
        }

    }

    // method for children
    fun showSnackBar(message: String) {
        val view = if (binding.homeAddBtn != null) {
            binding.homeAddBtn
        } else {
            binding.homeAddExtBtn
        }

        snackBar(message, view)
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code == AuthCodeIT.LOGOUT_SUCCESS.code) {
                setImageViewRoundDrawable(binding.propicImageView, null)
                setDrawableToButtons(isLogged = false)

                goToLogin()
                // close profile fragment so that there will not be 2 dashboards
                navController.popBackStack()
                checkDashboardInRailView()
            }
        })
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            when (authResult.code) {
                AuthCodeIT.USER_LOGGED.code -> {
                    viewModel.updateUserData()
                    setDrawableToButtons(isLogged = true)

                    viewModel.isLayoutReady = false
                }

                AuthCodeIT.USER_NOT_LOGGED.code -> {
                    setDrawableToButtons(isLogged = false)
                    viewModel.isLayoutReady = true
                }

                AuthCodeIT.USER_DATA_UPDATED.code -> {
                    supportFragmentManager.registerFragmentLifecycleCallbacks(
                        dashboardCallback,
                        true
                    )
                    setImageViewRoundDrawable(binding.propicImageView, viewModel.getProPic())
                    reloadDashboard()
                }

                AuthCodeIT.USER_DATA_NOT_UPDATED.code -> showSnackBar(authResult.message)

                else -> Unit
            }
        })
    }

    fun onProPicClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (binding.navBar != null || binding.navDrawer != null) {
            navController.navigateUp()
            navController.navigate(R.id.profileFragment)
        } else {
            binding.navRail?.selectedItemId = R.id.profileFragment
        }
    }

    private fun goToLogin() {
        Intent(applicationContext, LoginActivity::class.java).also {
            logInResultLauncher.launch(it)
        }
    }

    fun showProgressIndicator() {
        binding.homeProgressIndicator.show()
    }

    fun hideProgressIndicator() {
        binding.homeProgressIndicator.hide()
    }

    private fun reloadDashboard() {
        checkDashboardInRailView()
        navController.popBackStack()
        navController.navigate(R.id.dashboardFragment)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        checkDashboardInRailView()
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (viewModel.isLogged()) {
            viewModel.logOut()
        } else {
            goToLogin()
        }
    }

    private fun checkDashboardInRailView() {
        binding.navRail?.let {
            it.setOnItemSelectedListener(null)
            it.selectedItemId = R.id.dashboardFragment
            it.setOnItemSelectedListener(navRailListener)
        }
    }

    private fun setDrawableToButtons(isLogged: Boolean) {
        if (isLogged) {
            binding.logoutImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_logout
                )
            )

            binding.homeAddBtn?.also { fab ->
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
                fab.contentDescription = getString(R.string.addBtnContentDescription)
            }

            binding.homeAddExtBtn?.also { extFab ->
                extFab.icon = ContextCompat.getDrawable(this, R.drawable.ic_add)
                extFab.text = getString(R.string.add)
                extFab.contentDescription = getString(R.string.addBtnContentDescription)
            }

        } else {
            binding.logoutImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_login
                )
            )

            binding.homeAddBtn?.also { fab ->
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_login))
                fab.contentDescription = getString(R.string.login)
            }

            binding.homeAddExtBtn?.also { extFab ->
                extFab.icon = ContextCompat.getDrawable(this, R.drawable.ic_login)
                extFab.text = getString(R.string.login)
                extFab.contentDescription = getString(R.string.login)
            }
        }
    }
}