package com.frafio.myfinance.ui.home

import android.content.Intent
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
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.CustomNavigation
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.loadRoundImage
import com.frafio.myfinance.utils.snackbar
import org.kodein.di.generic.instance

class HomeActivity : BaseActivity(), HomeListener {

    // definizione variabili
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var navCustom: CustomNavigation

    private lateinit var navController: NavController

    private val factory: HomeViewModelFactory by instance()

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val purchaseRequest =
                data!!.getBooleanExtra("${getString(R.string.default_path)}.purchaseRequest", false)
            if (purchaseRequest) {
                binding.homeBottomNavView?.let { view ->
                    if (view.selectedItemId == R.id.listFragment) {
                        navController.popBackStack()
                    }
                    navController.navigate(R.id.listFragment)
                }

                binding.navigationLayout?.let {
                    if (navCustom.selectedItem == CustomNavigation.Item.ITEM_2) {
                        navController.popBackStack()
                        navController.navigate(R.id.listFragment)
                    }

                    navCustom.selectedItem = CustomNavigation.Item.ITEM_2
                }

                snackbar(getString(R.string.purchase_added), binding.homeAddBtn)
            }
        }
    }

    private val logInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadDashboard()
                binding.logoutBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_logout
                    )
                )
                loadRoundImage(binding.propicImageView, viewModel.getProPic())

                val userRequest =
                    result.data?.extras?.getBoolean(
                        "${getString(R.string.default_path)}.userRequest",
                        false
                    ) ?: false

                val userName =
                    result.data?.extras?.getString("${getString(R.string.default_path)}.userName")

                if (userRequest) {
                    snackbar(
                        "${getString(R.string.login_successful)} $userName",
                        binding.homeAddBtn
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().also {
            it.setKeepVisibleCondition{
                !viewModel.isLayoutReady
            }
            it.setOnExitAnimationListener{ splashScreenViewProvider ->
                supportFragmentManager.unregisterFragmentLifecycleCallbacks(dashboardCallback)
                splashScreenViewProvider.remove()
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        viewModel.listener = this
        binding.viewmodel = viewModel

        // importa i dati dal db
        viewModel.checkUser()

        // collegamento fragment view
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController.also {
            binding.homeBottomNavView?.setupWithNavController(it)
        }

        if (savedInstanceState == null) {
            setNavCustomLayout(binding.landHolder != null)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // risolve bug (se non sono sul primo fragment, si sovrappongono i fragment nello stack)
        if (navController.currentDestination?.id != R.id.dashboardFragment) {
            navController.navigateUp()
        }

        binding.navigationLayout?.let {
            setNavCustomLayout(binding.landHolder != null)
        }
    }

    private fun setNavCustomLayout(animateTV: Boolean) {
        binding.navigationLayout?.let { rootLayout ->
            navCustom = object : CustomNavigation(
                rootLayout.navViewLayout,
                rootLayout.dashboardLayout,
                rootLayout.dashboardItemIcon,
                rootLayout.dashboardItemText,
                rootLayout.listLayout,
                rootLayout.listItemIcon,
                rootLayout.listItemText,
                rootLayout.profileLayout,
                rootLayout.profileItemIcon,
                rootLayout.profileItemText,
                rootLayout.menuLayout,
                rootLayout.menuItemIcon,
                rootLayout.menuItemText,
                animateTV
            ) {
                override fun onItem1ClickAction() {
                    super.onItem1ClickAction()
                    if (selectedItem != Item.ITEM_1) {
                        navController.navigateUp()
                        navController.navigate(R.id.dashboardFragment)
                    }
                }

                override fun onItem2ClickAction() {
                    super.onItem2ClickAction()
                    if (selectedItem != Item.ITEM_2) {
                        navController.navigateUp()
                        navController.navigate(R.id.listFragment)
                    }
                }

                override fun onItem3ClickAction() {
                    super.onItem3ClickAction()
                    if (selectedItem != Item.ITEM_3) {
                        navController.navigateUp()
                        navController.navigate(R.id.profileFragment)
                    }
                }

                override fun onItem4ClickAction() {
                    super.onItem4ClickAction()
                    if (selectedItem != Item.ITEM_4) {
                        navController.navigateUp()
                        navController.navigate(R.id.menuFragment)
                    }
                }
            }
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

    private val navigationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.label) {
            getString(R.string.nav_1) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
            }

            getString(R.string.nav_2_extended) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()
            }

            getString(R.string.nav_3) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantShow()
                binding.propicImageView.instantHide()
            }

            getString(R.string.nav_4) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantHide()
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
                    it.putExtra("${getString(R.string.default_path)}.REQUESTCODE", 1)
                    addResultLauncher.launch(it, activityOptionsCompat)
                }
            }
        } else {
            snackbar(getString(R.string.warning_not_logged_home), binding.homeAddBtn)
        }

    }

    // method for children
    fun showSnackbar(message: String) {
        snackbar(message, binding.homeAddBtn)
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                loadRoundImage(binding.propicImageView, null)
                binding.logoutBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_login
                    )
                )
                goToLogin()
                // close profile fragment so that there will not be 2 dashboards
                navController.popBackStack()
            }
        })
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    viewModel.updateUserData()
                    binding.logoutBtn.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_logout
                        )
                    )

                    viewModel.isLayoutReady = false
                }

                AuthCode.USER_NOT_LOGGED.code -> {
                    binding.logoutBtn.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_login
                        )
                    )
                    viewModel.isLayoutReady = true
                }

                AuthCode.USER_DATA_UPDATED.code -> {
                    supportFragmentManager.registerFragmentLifecycleCallbacks(dashboardCallback, true)
                    loadRoundImage(binding.propicImageView, viewModel.getProPic())
                    reloadDashboard()
                }

                AuthCode.USER_DATA_NOT_UPDATED.code -> snackbar(authResult.message)

                else -> Unit
            }
        })
    }

    fun onProPicClick(view: View) {
        if (viewModel.isLogged()) {
            navController.navigateUp()
            navController.navigate(R.id.profileFragment)

            binding.navigationLayout?.let {
                navCustom.selectedItem = CustomNavigation.Item.ITEM_3
            }
        } else {
            goToLogin()
        }
    }

    private fun goToLogin() {
        Intent(applicationContext, LoginActivity::class.java).also {
            logInResultLauncher.launch(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.navigationLayout?.let {
            navCustom.checkDashboard()
        }
    }

    fun showProgressIndicator() {
        binding.homeProgressIndicator.show()
    }

    fun hideProgressIndicator() {
        binding.homeProgressIndicator.hide()
    }

    private fun reloadDashboard() {
        navController.popBackStack()
        navController.navigate(R.id.dashboardFragment)
        binding.navigationLayout?.let {
            navCustom.checkDashboard()
        }
    }

    fun onLogoutButtonClick(view: View) {
        if (viewModel.isLogged()) {
            viewModel.logOut()
        } else {
            goToLogin()
        }
    }
}