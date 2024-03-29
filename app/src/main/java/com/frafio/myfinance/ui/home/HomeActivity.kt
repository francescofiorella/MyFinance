package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.budget.BudgetFragment
import com.frafio.myfinance.ui.home.dashboard.DashboardFragment
import com.frafio.myfinance.ui.home.payments.PaymentsFragment
import com.frafio.myfinance.ui.home.menu.MenuFragment
import com.frafio.myfinance.ui.home.profile.ProfileFragment
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity(), HomeListener {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var dashboardFragment: DashboardFragment
    private lateinit var paymentsFragment: PaymentsFragment
    private lateinit var budgetFragment: BudgetFragment
    private lateinit var menuFragment: MenuFragment
    private lateinit var profileFragment: ProfileFragment
    private var activeFragment: Fragment? = null

    private var userRequest: Boolean = false
    var isLayoutReady: Boolean = false

    companion object {
        private const val ACTIVE_FRAGMENT_KEY = "active_fragment_key"
        private const val DASHBOARD_FRAGMENT_TAG = "dashboard_fragment_tag"
        private const val PAYMENTS_FRAGMENT_TAG = "payments_fragment_tag"
        private const val BUDGET_FRAGMENT_TAG = "budget_fragment_tag"
        private const val MENU_FRAGMENT_TAG = "menu_fragment_tag"
        private const val PROFILE_FRAGMENT_TAG = "profile_fragment_tag"
    }

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val purchaseRequest =
                data!!.getBooleanExtra(AddActivity.PURCHASE_REQUEST_KEY, false)
            if (purchaseRequest) {
                showFragment(R.id.paymentsFragment)
                refreshFragmentData(dashboard = true, payments = true, menu = true)
                showSnackBar(getString(R.string.purchase_added))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            userRequest = intent.extras?.getBoolean(LoginActivity.INTENT_USER_REQUEST, false)
                ?: false
            if (userRequest) {
                setTheme(R.style.Theme_MyFinance)
            } else {
                installSplashScreen().also {
                    it.setKeepOnScreenCondition {
                        // keep if the layout is not ready
                        !isLayoutReady
                    }
                    it.setOnExitAnimationListener { splashScreenViewProvider ->
                        supportFragmentManager.unregisterFragmentLifecycleCallbacks(
                            dashboardCallback
                        )
                        splashScreenViewProvider.remove()
                    }
                    if (getSharedDynamicColor((application as MyFinanceApplication).sharedPreferences)) {
                        DynamicColors.applyToActivityIfAvailable(this)
                    }
                }
            }
        } else {
            setTheme(R.style.Theme_MyFinance)
            if (getSharedDynamicColor((application as MyFinanceApplication).sharedPreferences)) {
                DynamicColors.applyToActivityIfAvailable(this)
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel.listener = this
        binding.viewModel = viewModel

        if (savedInstanceState == null) {
            if (viewModel.isDynamicColorOn()) {
                DynamicColors.applyToActivityIfAvailable(this)
            }

            if (userRequest) {
                showProgressIndicator()
                viewModel.updateUserData()
            } else {
                // import db data
                viewModel.checkUser()
            }

            binding.navDrawer?.setCheckedItem(R.id.dashboardFragment)
        }

        binding.navBar?.setOnItemSelectedListener(navBarListener)
        binding.navRail?.setOnItemSelectedListener(navBarListener)
        binding.navDrawer?.setNavigationItemSelectedListener(navDrawerListener)

        onBackPressedDispatcher.addCallback {
            if (activeFragment != dashboardFragment) {
                showFragment(R.id.dashboardFragment)
            } else {
                finish()
            }
        }
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        activeFragment?.let { outState.putString(ACTIVE_FRAGMENT_KEY, it.tag) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        dashboardFragment = supportFragmentManager
            .findFragmentByTag(DASHBOARD_FRAGMENT_TAG)!! as DashboardFragment
        paymentsFragment = supportFragmentManager
            .findFragmentByTag(PAYMENTS_FRAGMENT_TAG)!! as PaymentsFragment
        budgetFragment = supportFragmentManager
            .findFragmentByTag(BUDGET_FRAGMENT_TAG)!! as BudgetFragment
        menuFragment = supportFragmentManager
            .findFragmentByTag(MENU_FRAGMENT_TAG)!! as MenuFragment
        profileFragment = supportFragmentManager
            .findFragmentByTag(PROFILE_FRAGMENT_TAG)!! as ProfileFragment

        savedInstanceState.getString(ACTIVE_FRAGMENT_KEY).also { tag ->
            when (tag) {
                DASHBOARD_FRAGMENT_TAG -> {
                    activeFragment = dashboardFragment
                    showFragment(R.id.dashboardFragment)
                }

                PAYMENTS_FRAGMENT_TAG -> {
                    activeFragment = paymentsFragment
                    showFragment(R.id.paymentsFragment)
                }

                BUDGET_FRAGMENT_TAG -> {
                    activeFragment = budgetFragment
                    showFragment(R.id.budgetFragment)
                }

                MENU_FRAGMENT_TAG -> {
                    activeFragment = menuFragment
                    showFragment(R.id.menuFragment)
                }

                PROFILE_FRAGMENT_TAG -> {
                    activeFragment = profileFragment
                    showFragment(R.id.profileFragment)
                }

                else -> {
                    activeFragment = dashboardFragment
                    showFragment(R.id.dashboardFragment)
                }
            }
        }
    }

    private val navBarListener = NavigationBarView.OnItemSelectedListener { item ->
        activeFragment?.let { navigateTo(item.itemId) }
        true
    }

    private val navDrawerListener = NavigationView.OnNavigationItemSelectedListener { item ->
        activeFragment?.let { navigateTo(item.itemId) }
        true
    }

    private fun navigateTo(itemId: Int) {
        when (itemId) {
            R.id.dashboardFragment -> {
                binding.backArrow.instantHide()
                binding.homeAddBtn?.instantShow()
                binding.homeAddExtBtn?.instantShow()
                binding.navBar?.instantShow()
                binding.navRail?.instantShow()
                binding.navDrawer?.instantShow()
                dashboardFragment.scrollUp()
                binding.fragmentTitle.text = getString(R.string.nav_1)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(dashboardFragment).commit()
                activeFragment = dashboardFragment
            }

            R.id.paymentsFragment -> {
                binding.backArrow.instantHide()
                binding.homeAddBtn?.instantShow()
                binding.homeAddExtBtn?.instantShow()
                binding.navBar?.instantShow()
                binding.navRail?.instantShow()
                binding.navDrawer?.instantShow()
                paymentsFragment.scrollUp()
                binding.fragmentTitle.text = getString(R.string.nav_2)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(paymentsFragment).commit()
                activeFragment = paymentsFragment
            }

            R.id.budgetFragment -> {
                binding.backArrow.instantHide()
                binding.homeAddBtn?.instantShow()
                binding.homeAddExtBtn?.instantShow()
                binding.navBar?.instantShow()
                binding.navRail?.instantShow()
                binding.navDrawer?.instantShow()
                binding.fragmentTitle.text = getString(R.string.nav_5)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(budgetFragment).commit()
                activeFragment = budgetFragment
            }

            R.id.menuFragment -> {
                binding.backArrow.instantHide()
                binding.homeAddBtn?.instantShow()
                binding.homeAddExtBtn?.instantShow()
                binding.navBar?.instantShow()
                binding.navRail?.instantShow()
                binding.navDrawer?.instantShow()
                menuFragment.scrollUp()
                binding.fragmentTitle.text = getString(R.string.nav_4)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(menuFragment).commit()
                activeFragment = menuFragment
            }

            R.id.profileFragment -> {
                binding.backArrow.instantShow()
                binding.homeAddBtn?.instantHide()
                binding.homeAddExtBtn?.instantHide()
                binding.navBar?.instantHide()
                binding.navRail?.instantHide()
                binding.navDrawer?.instantHide()
                binding.fragmentTitle.text = getString(R.string.nav_3)
                binding.logoutBtn.instantShow()
                binding.propicImageView.instantHide()
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(profileFragment).commit()
                activeFragment = profileFragment
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
            isLayoutReady = true
        }
    }

    fun onAddButtonClick(view: View) {
        ActivityOptionsCompat.makeClipRevealAnimation(
            view, 0, 0, view.measuredWidth, view.measuredHeight
        ).also { activityOptionsCompat ->
            Intent(applicationContext, AddActivity::class.java).also {
                it.putExtra(
                    AddActivity.REQUEST_CODE_KEY,
                    AddActivity.REQUEST_ADD_CODE
                )
                addResultLauncher.launch(it, activityOptionsCompat)
            }
        }
    }

    // method for children
    fun showSnackBar(message: String, show: Boolean = true): Snackbar {
        val view = when {
            binding.homeAddBtn != null -> binding.homeAddBtn
            else -> binding.homeAddExtBtn
        }

        return snackBar(message, view, show)
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                goToLoginActivity()
            }
        }
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    viewModel.updateUserData()
                    isLayoutReady = false
                }

                AuthCode.USER_NOT_LOGGED.code -> {
                    goToLoginActivity()
                }

                AuthCode.USER_DATA_UPDATED.code -> {
                    initFragments()
                    if (!userRequest) {
                        supportFragmentManager.registerFragmentLifecycleCallbacks(
                            dashboardCallback,
                            true
                        )
                        userRequest = false
                    } else {
                        hideProgressIndicator()
                        intent.extras?.getString(LoginActivity.INTENT_USER_NAME).also { userName ->
                            showSnackBar("${getString(R.string.login_successful)} $userName")
                        }
                    }
                }

                AuthCode.USER_DATA_NOT_UPDATED.code -> showSnackBar(authResult.message)

                else -> Unit
            }
        }
    }

    fun onProPicClick(@Suppress("UNUSED_PARAMETER") view: View) {
        showFragment(R.id.profileFragment)
    }

    private fun goToLoginActivity() {
        Intent(applicationContext, LoginActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun showProgressIndicator() {
        binding.homeProgressIndicator.show()
    }

    fun hideProgressIndicator() {
        binding.homeProgressIndicator.hide()
    }

    private fun initFragments() {
        dashboardFragment = DashboardFragment()
        paymentsFragment = PaymentsFragment()
        budgetFragment = BudgetFragment()
        menuFragment = MenuFragment()
        profileFragment = ProfileFragment()
        activeFragment = dashboardFragment
        supportFragmentManager.beginTransaction()
            .add(R.id.home_fragmentContainerView, profileFragment, PROFILE_FRAGMENT_TAG)
            .add(R.id.home_fragmentContainerView, menuFragment, MENU_FRAGMENT_TAG)
            .add(R.id.home_fragmentContainerView, budgetFragment, BUDGET_FRAGMENT_TAG)
            .add(R.id.home_fragmentContainerView, paymentsFragment, PAYMENTS_FRAGMENT_TAG)
            .add(R.id.home_fragmentContainerView, dashboardFragment, DASHBOARD_FRAGMENT_TAG)
            .hide(profileFragment)
            .hide(menuFragment)
            .hide(budgetFragment)
            .hide(paymentsFragment)
            .commit()
    }

    private fun showFragment(fragmentId: Int) {
        binding.navBar?.selectedItemId = fragmentId
        binding.navRail?.selectedItemId = fragmentId
        binding.navDrawer?.let {
            it.setCheckedItem(fragmentId)
            navigateTo(fragmentId)
        }
    }

    fun refreshFragmentData(
        dashboard: Boolean = false,
        payments: Boolean = false,
        menu: Boolean = false
    ) {
        if (dashboard) {
            dashboardFragment.refreshStatsData()
        }
        if (payments) {
            paymentsFragment.refreshListData()
        }
        if (menu) {
            menuFragment.refreshPlotData()
        }
    }
}