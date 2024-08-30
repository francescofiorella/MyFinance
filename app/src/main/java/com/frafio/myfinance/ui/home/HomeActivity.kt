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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.ui.home.budget.BudgetFragment
import com.frafio.myfinance.ui.home.dashboard.DashboardFragment
import com.frafio.myfinance.ui.home.expenses.ExpensesFragment
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

    private var dashboardFragment: DashboardFragment? = null
    private var expensesFragment: ExpensesFragment? = null
    private var budgetFragment: BudgetFragment? = null
    private var profileFragment: ProfileFragment? = null
    private var activeFragment: BaseFragment? = null

    private var userRequest: Boolean = false
    var isLayoutReady: Boolean = false

    companion object {
        private const val DASHBOARD_FRAGMENT_TAG = "dashboard_fragment_tag"
        private const val EXPENSES_FRAGMENT_TAG = "expenses_fragment_tag"
        private const val BUDGET_FRAGMENT_TAG = "budget_fragment_tag"
        private const val PROFILE_FRAGMENT_TAG = "profile_fragment_tag"
    }

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data!!
            val expenseRequest = data.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)
            val message = data.getStringExtra(AddActivity.ADD_RESULT_MESSAGE) ?: ""
            when (expenseRequest) {
                AddActivity.REQUEST_EXPENSE_CODE -> {
                    showFragment(R.id.expensesFragment)
                    showSnackBar(message)
                }

                AddActivity.REQUEST_INCOME_CODE -> {
                    showFragment(R.id.budgetFragment)
                    showSnackBar(message)
                }
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
                installSplashScreen().apply {
                    setKeepOnScreenCondition {
                        // keep if the layout is not ready
                        !isLayoutReady
                    }
                    setOnExitAnimationListener { splashScreenViewProvider ->
                        supportFragmentManager.unregisterFragmentLifecycleCallbacks(
                            dashboardCallback
                        )
                        splashScreenViewProvider.remove()
                    }
                }
                if (getSharedDynamicColor((application as MyFinanceApplication).sharedPreferences)) {
                    DynamicColors.applyToActivityIfAvailable(this)
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
                viewModel.updateUserExpenses()
                viewModel.updateUserIncomes()
                viewModel.updateMonthlyBudget()
                viewModel.updateLocalMonthlyBudget()
                initFragments()
                intent.extras?.getString(LoginActivity.INTENT_USER_NAME).also { userName ->
                    showSnackBar("${getString(R.string.login_successful)} $userName")
                }
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
            viewModel.fragmentStack.removeLast()
            when (viewModel.fragmentStack.lastOrNull()) {
                DASHBOARD_FRAGMENT_TAG -> showFragment(R.id.dashboardFragment)
                EXPENSES_FRAGMENT_TAG -> showFragment(R.id.expensesFragment)
                BUDGET_FRAGMENT_TAG -> showFragment(R.id.budgetFragment)
                PROFILE_FRAGMENT_TAG -> showFragment(R.id.profileFragment)
                else -> finish()
            }
        }
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        dashboardFragment = supportFragmentManager
            .findFragmentByTag(DASHBOARD_FRAGMENT_TAG) as DashboardFragment?
        expensesFragment = supportFragmentManager
            .findFragmentByTag(EXPENSES_FRAGMENT_TAG) as ExpensesFragment?
        budgetFragment = supportFragmentManager
            .findFragmentByTag(BUDGET_FRAGMENT_TAG) as BudgetFragment?
        profileFragment = supportFragmentManager
            .findFragmentByTag(PROFILE_FRAGMENT_TAG) as ProfileFragment?

        when (viewModel.fragmentStack.lastOrNull()) {
            EXPENSES_FRAGMENT_TAG -> {
                activeFragment = expensesFragment
                showFragment(R.id.expensesFragment)
            }

            BUDGET_FRAGMENT_TAG -> {
                activeFragment = budgetFragment
                showFragment(R.id.budgetFragment)
            }

            PROFILE_FRAGMENT_TAG -> {
                activeFragment = profileFragment
                showFragment(R.id.profileFragment)
            }

            else -> {
                // Either tag is DASHBOARD_FRAGMENT_TAG or set Dashboard as default
                activeFragment = dashboardFragment
                showFragment(R.id.dashboardFragment)
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
        val currentId = if (binding.navBar != null) {
            binding.navBar!!.selectedItemId
        } else if (binding.navRail != null) {
            binding.navRail!!.selectedItemId
        } else {
            binding.navDrawer!!.checkedItem?.itemId
        }
        if (currentId == itemId) {
            activeFragment!!.scrollUp()
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        when (itemId) {
            R.id.dashboardFragment -> {
                if (dashboardFragment == null) {
                    dashboardFragment = DashboardFragment()
                    transaction.add(
                        R.id.home_fragmentContainerView,
                        dashboardFragment!!,
                        DASHBOARD_FRAGMENT_TAG
                    )
                }
                binding.fragmentTitle.text = getString(R.string.dashboard)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()

                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(dashboardFragment!!).commit()
                activeFragment = dashboardFragment

                if (viewModel.fragmentStack.lastOrNull() != DASHBOARD_FRAGMENT_TAG)
                    viewModel.fragmentStack.add(DASHBOARD_FRAGMENT_TAG)
            }

            R.id.expensesFragment -> {
                if (expensesFragment == null) {
                    expensesFragment = ExpensesFragment()
                    transaction.add(
                        R.id.home_fragmentContainerView,
                        expensesFragment!!,
                        EXPENSES_FRAGMENT_TAG
                    )
                }
                binding.fragmentTitle.text = getString(R.string.expenses)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(expensesFragment!!).commit()
                activeFragment = expensesFragment

                if (viewModel.fragmentStack.lastOrNull() != EXPENSES_FRAGMENT_TAG)
                    viewModel.fragmentStack.add(EXPENSES_FRAGMENT_TAG)
            }

            R.id.budgetFragment -> {
                if (budgetFragment == null) {
                    budgetFragment = BudgetFragment()
                    transaction.add(
                        R.id.home_fragmentContainerView,
                        budgetFragment!!,
                        BUDGET_FRAGMENT_TAG
                    )
                }
                binding.fragmentTitle.text = getString(R.string.budget)
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(budgetFragment!!).commit()
                activeFragment = budgetFragment

                if (viewModel.fragmentStack.lastOrNull() != BUDGET_FRAGMENT_TAG)
                    viewModel.fragmentStack.add(BUDGET_FRAGMENT_TAG)
            }

            R.id.profileFragment -> {
                if (profileFragment == null) {
                    profileFragment = ProfileFragment()
                    transaction.add(
                        R.id.home_fragmentContainerView,
                        profileFragment!!,
                        PROFILE_FRAGMENT_TAG
                    )
                }
                binding.fragmentTitle.text = getString(R.string.profile)
                binding.logoutBtn.instantShow()
                binding.propicImageView.instantHide()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment!!).show(profileFragment!!).commit()
                activeFragment = profileFragment

                if (viewModel.fragmentStack.lastOrNull() != PROFILE_FRAGMENT_TAG)
                    viewModel.fragmentStack.add(PROFILE_FRAGMENT_TAG)
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
    fun showSnackBar(
        message: String,
        actionText: String? = null,
        actionFun: () -> Unit = {}
    ): Snackbar {
        var view = when {
            binding.homeAddBtn != null -> binding.homeAddBtn
            else -> binding.homeAddExtBtn
        }

        if (view?.isVisible == false) {
            view = null
        }

        return snackBar(message, view, actionText, actionFun)
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
                    showProgressIndicator()
                    viewModel.updateUserExpenses()
                    viewModel.updateUserIncomes()
                    viewModel.updateMonthlyBudget()
                    viewModel.updateLocalMonthlyBudget()
                    initFragments()
                    if (userRequest) {
                        hideProgressIndicator()
                        intent.extras?.getString(LoginActivity.INTENT_USER_NAME).also { userName ->
                            showSnackBar("${getString(R.string.login_successful)} $userName")
                        }
                    } else {
                        supportFragmentManager.registerFragmentLifecycleCallbacks(
                            dashboardCallback,
                            true
                        )
                    }
                }

                AuthCode.USER_NOT_LOGGED.code -> {
                    goToLoginActivity()
                }

                else -> Unit
            }
        }
    }

    override fun onUserDataUpdated(response: LiveData<FinanceResult>) {
        response.observe(this) { result ->
            when (result.code) {
                FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS.code -> {
                    hideProgressIndicator()
                }

                FinanceCode.BUDGET_UPDATE_SUCCESS.code -> Unit

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
        viewModel.fragmentStack.add(DASHBOARD_FRAGMENT_TAG)
        dashboardFragment = DashboardFragment()
        activeFragment = dashboardFragment
        supportFragmentManager.beginTransaction()
            .add(R.id.home_fragmentContainerView, dashboardFragment!!, DASHBOARD_FRAGMENT_TAG)
            .commit()
    }

    private fun showFragment(fragmentId: Int) {
        binding.navBar?.selectedItemId = fragmentId
        binding.navRail?.selectedItemId = fragmentId
        binding.navDrawer?.let {
            navigateTo(fragmentId)
            it.setCheckedItem(fragmentId)
        }
    }
}