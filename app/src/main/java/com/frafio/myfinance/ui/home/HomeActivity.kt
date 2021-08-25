package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.TabletNavigation
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.snackbar
import org.kodein.di.generic.instance

class HomeActivity : BaseActivity(), LogoutListener {

    // definizione variabili
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var navTablet: TabletNavigation

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

                binding.navRailView?.let { view ->
                    if (view.selectedItemId == R.id.listFragment) {
                        navController.popBackStack()
                    }

                    view.selectedItemId = R.id.listFragment
                }

                binding.navigationTabletLayout?.let {
                    if (navTablet.selectedItem == TabletNavigation.Item.ITEM_2) {
                        navController.popBackStack()
                    }

                    navTablet.selectedItem = TabletNavigation.Item.ITEM_2
                }

                snackbar(getString(R.string.purchase_added), binding.homeAddBtn)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        viewModel.logoutListener = this
        binding.viewmodel = viewModel

        // collegamento view
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.homeBottomNavView?.setupWithNavController(navController)

        setNavRailListener()

        if (savedInstanceState == null) {
            setNavTabletListener(true)

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
        binding.navRailView?.selectedItemId = navController.currentDestination!!.id

        binding.navigationTabletLayout?.let {
            setNavTabletListener(false)

            when (navController.currentDestination!!.id) {
                R.id.dashboardFragment -> navTablet.selectedItem = TabletNavigation.Item.ITEM_1

                R.id.listFragment -> navTablet.selectedItem = TabletNavigation.Item.ITEM_2

                R.id.profileFragment -> navTablet.selectedItem = TabletNavigation.Item.ITEM_3

                R.id.menuFragment -> navTablet.selectedItem = TabletNavigation.Item.ITEM_4
            }
        }
    }

    private fun setNavRailListener() {
        binding.navRailView?.setOnItemSelectedListener { item ->
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
    }

    private fun setNavTabletListener(firstTime: Boolean) {
        binding.navigationTabletLayout?.let { rootLayout ->
            navTablet = object : TabletNavigation(
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
                firstTime
            ) {
                override fun onItem1ClickAction() {
                    super.onItem1ClickAction()
                    navController.navigateUp()
                    navController.navigate(R.id.dashboardFragment)
                }

                override fun onItem2ClickAction() {
                    super.onItem2ClickAction()
                    navController.navigateUp()
                    navController.navigate(R.id.listFragment)
                }

                override fun onItem3ClickAction() {
                    super.onItem3ClickAction()
                    navController.navigateUp()
                    navController.navigate(R.id.profileFragment)
                }

                override fun onItem4ClickAction() {
                    super.onItem4ClickAction()
                    navController.navigateUp()
                    navController.navigate(R.id.menuFragment)
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

    fun onProPicClick(view: View) {
        navController.navigateUp()
        navController.navigate(R.id.profileFragment)

        binding.navRailView?.selectedItemId = R.id.profileFragment

        binding.navigationTabletLayout?.let {
            navTablet.selectedItem = TabletNavigation.Item.ITEM_3
        }
    }

    override fun onBackPressed() {
        binding.navRailView?.setOnItemSelectedListener(null)
        binding.navRailView?.selectedItemId = R.id.dashboardFragment
        setNavRailListener()

        binding.navigationTabletLayout?.let {
            navTablet.setDashboardBlue()
        }
        super.onBackPressed()
    }
}