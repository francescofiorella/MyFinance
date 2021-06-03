package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.ActivityHomeBinding
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.list.ListFragment
import com.frafio.myfinance.utils.snackbar

class HomeActivity : AppCompatActivity() {

    // definizione variabili
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        // toolbar
        setSupportActionBar(binding.homeToolbar)

        // collegamento view
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.homeBottomNavView.setupWithNavController(navController)

        if (savedInstanceState == null) {
            // controlla se si è appena fatto l'accesso
            if (intent.hasExtra("com.frafio.myfinance.userRequest")) {
                val userRequest =
                    intent.extras?.getBoolean("com.frafio.myfinance.userRequest", false) ?: false
                val userName = intent.extras?.getString("com.frafio.myfinance.userName")
                if (userRequest) {
                    binding.root.snackbar(
                        "Hai effettuato l'accesso come $userName",
                        binding.homeAddBtn
                    )
                }
            }
        }
    }

    fun onAddButtonClick(view: View) {
        val activityOptionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(
            binding.homeAddBtn, 0, 0,
            binding.homeAddBtn.measuredWidth, binding.homeAddBtn.measuredHeight
        )
        Intent(applicationContext, AddActivity::class.java).also {
            it.putExtra("com.frafio.myfinance.REQUESTCODE", 1)
            startActivityForResult(it, 1, activityOptionsCompat.toBundle())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == RESULT_OK) {
                    val purchaseRequest =
                        data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
                    if (purchaseRequest) {
                        navController.navigate(R.id.listFragment)
                        val fragment =
                            navHostFragment.childFragmentManager.fragments[0] as ListFragment?
                        fragment?.reloadPurchaseList()
                        binding.root.snackbar("Acquisto aggiunto!", binding.homeAddBtn)
                    }
                }
            }

            2 -> {
                if (resultCode == RESULT_OK) {
                    val editRequest =
                        data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
                    if (editRequest) {
                        val fragment =
                            navHostFragment.childFragmentManager.fragments[0] as ListFragment?
                        fragment?.reloadPurchaseList()
                        binding.root.snackbar("Acquisto modificato!", binding.homeAddBtn)
                    }
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun showSnackbar(message: String) {
        binding.root.snackbar(message, binding.homeAddBtn)
    }
}