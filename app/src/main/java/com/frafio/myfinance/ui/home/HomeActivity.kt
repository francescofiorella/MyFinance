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
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.snackbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import org.kodein.di.generic.instance

class HomeActivity : BaseActivity(), LogoutListener {

    // definizione variabili
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private val factory: HomeViewModelFactory by instance()

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val purchaseRequest =
                data!!.getBooleanExtra("${getString(R.string.default_path)}.purchaseRequest", false)
            if (purchaseRequest) {
                if (binding.homeBottomNavView.selectedItemId == R.id.listFragment) {
                    navController.popBackStack()
                }
                navController.navigate(R.id.listFragment)
                binding.root.snackbar(getString(R.string.purchase_added), binding.homeAddBtn)
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
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.homeBottomNavView.setupWithNavController(navController)

        if (savedInstanceState == null) {
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
                    binding.root.snackbar(
                        "${getString(R.string.login_successful)} $userName",
                        binding.homeAddBtn
                    )
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
                binding.propicImageView.instantShow()}

            getString(R.string.nav_3) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantShow()
                binding.propicImageView.instantHide()}

            getString(R.string.nav_4) -> {
                binding.fragmentTitle.text = destination.label
                binding.logoutBtn.instantHide()
                binding.propicImageView.instantShow()}
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
        binding.root.snackbar(message, binding.homeAddBtn)
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
    }
}