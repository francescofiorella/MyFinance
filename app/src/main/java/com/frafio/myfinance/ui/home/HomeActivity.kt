package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inizializza la splashScreen
        installSplashScreen()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        viewModel.listener = this
        binding.viewmodel = viewModel

        // importa i dati dal db
        val content: View = findViewById(android.R.id.content)
        binding.propicImageView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    viewModel.checkUser()
                    return if (viewModel.isReady) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )

        // collegamento view
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.homeBottomNavView?.setupWithNavController(navController)

        if (savedInstanceState == null) {
            setNavCustomLayout(true, binding.landHolder != null)

            // controlla se si è appena fatto l'accesso
            if (intent.hasExtra("${getString(R.string.default_path)}.userRequest")) {
                val userRequest =
                    intent.extras?.getBoolean(
                        "${getString(R.string.default_path)}.userRequest",
                        false
                    ) ?: false

                val userName =
                    intent.extras?.getString("${getString(R.string.default_path)}.userName")

                if (userRequest) {
                    snackbar(
                        "${getString(R.string.login_successful)} $userName",
                        binding.homeAddBtn
                    )
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        binding.navigationLayout?.let {
            setNavCustomLayout(false, binding.landHolder != null)

            when (navController.currentDestination!!.id) {
                R.id.dashboardFragment -> navCustom.setDashboardBlue()

                R.id.listFragment -> navCustom.setListBlue()

                R.id.profileFragment -> navCustom.setProfileBlue()

                R.id.menuFragment -> navCustom.setMenuBlue()
            }
        }
    }

    private fun setNavCustomLayout(firstTime: Boolean, animateTV: Boolean) {
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
                firstTime,
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

    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
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
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }


    fun onAddButtonClick(view: View) {
        val activityOptionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(
            view, 0, 0,
            view.measuredWidth, view.measuredHeight
        )

        Intent(applicationContext, AddActivity::class.java).also {
            it.putExtra("${getString(R.string.default_path)}.REQUESTCODE", 1)
            addResultLauncher.launch(it, activityOptionsCompat)
        }
    }

    fun showSnackbar(message: String) {
        snackbar(message, binding.homeAddBtn)
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                Intent(applicationContext, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        })
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    viewModel.updateUserData()
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
                    }, 1000)
                }

                AuthCode.USER_DATA_UPDATED.code -> {
                    viewModel.isReady = true
                }

                AuthCode.USER_DATA_NOT_UPDATED.code -> snackbar(authResult.message)

                else -> Unit
            }
        })
    }

    fun onProPicClick(view: View) {
        navController.navigateUp()
        navController.navigate(R.id.profileFragment)

        binding.navigationLayout?.let {
            navCustom.selectedItem = CustomNavigation.Item.ITEM_3
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.navigationLayout?.let {
            navCustom.setDashboardBlue()
        }
    }

    fun showProgressIndicator() {
        binding.homeProgressIndicator.show()
    }

    fun hideProgressIndicator() {
        binding.homeProgressIndicator.hide()
    }
}