package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.dashboard.DashboardFragment
import com.frafio.myfinance.ui.home.list.ListFragment
import com.frafio.myfinance.ui.home.menu.MenuFragment
import com.frafio.myfinance.ui.home.profile.ProfileFragment
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.setImageViewRoundDrawable
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import org.kodein.di.generic.instance

class HomeActivity : BaseActivity(), HomeListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var dashboardFragment: Fragment
    private lateinit var listFragment: Fragment
    private lateinit var profileFragment: Fragment
    private lateinit var menuFragment: Fragment
    private lateinit var activeFragment: Fragment

    private val factory: HomeViewModelFactory by instance()

    companion object {
        private const val ACTIVE_FRAGMENT_ID_KEY = "active_fragment_id"
        private const val DASHBOARD_FRAGMENT_ID_KEY = "dashboard_fragment_id"
        private const val LIST_FRAGMENT_ID_KEY = "list_fragment_id"
        private const val PROFILE_FRAGMENT_ID_KEY = "profile_fragment_id"
        private const val MENU_FRAGMENT_ID_KEY = "menu_fragment_id"
    }

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val purchaseRequest =
                data!!.getBooleanExtra(AddActivity.INTENT_PURCHASE_REQUEST, false)
            if (purchaseRequest) {
                showFragment(R.id.listFragment)
                (activeFragment as ListFragment).updateListData()
                refreshOnPurchaseChanges()
                showSnackBar(getString(R.string.purchase_added))
            }
        }
    }

    private val logInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                showFragment(R.id.dashboardFragment)
                setDrawableToButtons(isLogged = true)
                setImageViewRoundDrawable(binding.propicImageView, viewModel.getProPic())

                val userRequest =
                    result.data?.extras?.getBoolean(LoginActivity.INTENT_USER_REQUEST, false)
                        ?: false

                val userName = result.data?.extras?.getString(LoginActivity.INTENT_USER_NAME)

                if (userRequest) {
                    refreshFragments()
                    showSnackBar("${getString(R.string.login_successful)} $userName")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().also {
            it.setKeepOnScreenCondition {
                // keep if the layout is not ready
                !viewModel.isLayoutReady
            }
            it.setOnExitAnimationListener { splashScreenViewProvider ->
                supportFragmentManager.unregisterFragmentLifecycleCallbacks(dashboardCallback)
                splashScreenViewProvider.remove()
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.listener = this
        binding.viewModel = viewModel

        if (savedInstanceState == null) {
            // import db data
            viewModel.checkUser()
        }

        binding.navBar?.setOnItemSelectedListener(navBarListener)
        binding.navRail?.setOnItemSelectedListener(navBarListener)
        binding.navDrawer?.setNavigationItemSelectedListener(navDrawerListener)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        dashboardFragment = DashboardFragment()
        listFragment = ListFragment()
        profileFragment = supportFragmentManager.fragments[1]
        menuFragment = MenuFragment()
        activeFragment = profileFragment
        supportFragmentManager.beginTransaction()
            .add(R.id.home_fragmentContainerView, menuFragment).hide(menuFragment)
            .add(R.id.home_fragmentContainerView, listFragment).hide(listFragment)
            .add(R.id.home_fragmentContainerView, dashboardFragment).hide(dashboardFragment).commit()
    }

    private val navBarListener = NavigationBarView.OnItemSelectedListener { item ->
        navigateTo(item.itemId)
        true
    }

    private val navDrawerListener = NavigationView.OnNavigationItemSelectedListener { item ->
        navigateTo(item.itemId)
        true
    }

    private fun navigateTo(itemId: Int) {
        when (itemId) {
            R.id.dashboardFragment -> {
                binding.fragmentTitle.text = getString(R.string.nav_1)
                binding.logoutCard.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment).show(dashboardFragment).commit()
                activeFragment = dashboardFragment
            }

            R.id.listFragment -> {
                binding.fragmentTitle.text = getString(R.string.nav_2_extended)
                binding.logoutCard.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment).show(listFragment).commit()
                activeFragment = listFragment
            }

            R.id.profileFragment -> {
                binding.fragmentTitle.text = getString(R.string.nav_3)
                binding.logoutCard.instantShow()
                binding.propicImageView.instantHide()
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment).show(profileFragment).commit()
                activeFragment = profileFragment
            }

            R.id.menuFragment -> {
                binding.fragmentTitle.text = getString(R.string.nav_4)
                binding.logoutCard.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment).show(menuFragment).commit()
                activeFragment = menuFragment
            }

            else -> Unit // should not be possible
        }
    }

    // called only one time, when the activity is loaded for the first time
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
        val view = when {
            binding.homeAddBtn != null -> binding.homeAddBtn
            else -> binding.homeAddExtBtn
        }

        snackBar(message, view)
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                setImageViewRoundDrawable(binding.propicImageView, null)
                setDrawableToButtons(isLogged = false)

                goToLogin()
                refreshFragments()
                showFragment(R.id.dashboardFragment)
            }
        }
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    viewModel.updateUserData()
                    setDrawableToButtons(isLogged = true)

                    viewModel.isLayoutReady = false
                }

                AuthCode.USER_NOT_LOGGED.code -> {
                    supportFragmentManager.registerFragmentLifecycleCallbacks(
                        dashboardCallback,
                        true
                    )
                    setDrawableToButtons(isLogged = false)

                    initFragments()
                }

                AuthCode.USER_DATA_UPDATED.code -> {
                    supportFragmentManager.registerFragmentLifecycleCallbacks(
                        dashboardCallback,
                        true
                    )
                    setImageViewRoundDrawable(binding.propicImageView, viewModel.getProPic())

                    initFragments()
                }

                AuthCode.USER_DATA_NOT_UPDATED.code -> showSnackBar(authResult.message)

                else -> Unit
            }
        }
    }

    fun onProPicClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (binding.navBar != null || binding.navDrawer != null) {
            navigateTo(R.id.profileFragment)
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

    override fun onBackPressed() {
        if (activeFragment != dashboardFragment) {
            showFragment(R.id.dashboardFragment)
        } else {
            super.onBackPressed()
        }
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (viewModel.isLogged()) {
            viewModel.logOut()
        } else {
            goToLogin()
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

    private fun initFragments() {
        dashboardFragment = DashboardFragment()
        listFragment = ListFragment()
        profileFragment = ProfileFragment()
        menuFragment = MenuFragment()
        activeFragment = dashboardFragment
        binding.fragmentTitle.text = getString(R.string.nav_1)
        binding.logoutCard.instantHide()
        binding.propicImageView.instantShow()
        supportFragmentManager.beginTransaction()
            .add(R.id.home_fragmentContainerView, menuFragment).hide(menuFragment)
            .add(R.id.home_fragmentContainerView, profileFragment).hide(profileFragment)
            .add(R.id.home_fragmentContainerView, listFragment).hide(listFragment)
            .add(R.id.home_fragmentContainerView, dashboardFragment).commit()
    }

    private fun showFragment(fragmentId: Int) {
        binding.navBar?.selectedItemId = fragmentId
        binding.navRail?.selectedItemId = fragmentId
        binding.navDrawer?.setCheckedItem(fragmentId)
    }

    private fun refreshFragments() {
        supportFragmentManager.beginTransaction().detach(dashboardFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(dashboardFragment).commitNow()
        supportFragmentManager.beginTransaction().detach(listFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(listFragment).commitNow()
        supportFragmentManager.beginTransaction().detach(profileFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(profileFragment).commitNow()
        supportFragmentManager.beginTransaction().detach(menuFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(menuFragment).commitNow()
    }

    fun refreshOnPurchaseChanges() {
        supportFragmentManager.beginTransaction().detach(dashboardFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(dashboardFragment).commitNow()
        supportFragmentManager.beginTransaction().detach(menuFragment).commitNow()
        supportFragmentManager.beginTransaction().attach(menuFragment).commitNow()
    }
}