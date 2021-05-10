package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.home.list.ListFragment
import com.frafio.myfinance.ui.store.AddActivity
import com.frafio.myfinance.util.snackbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {

    // definizione variabili
    private lateinit var layout: CoordinatorLayout

    private lateinit var mToolbar: MaterialToolbar
    private lateinit var mBottomNavigationView: BottomNavigationView
    private lateinit var mAddBtn: FloatingActionButton

    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // toolbar
        mToolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(mToolbar)

        // collegamento view
        layout = findViewById(R.id.main_layout)
        mBottomNavigationView = findViewById(R.id.home_bottomNavView)
        mAddBtn = findViewById(R.id.home_addBtn)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        mBottomNavigationView.setupWithNavController(navController)

        if (savedInstanceState == null) {
            // controlla se si è appena fatto l'accesso
            if (intent.hasExtra("com.frafio.myfinance.userRequest")) {
                val userRequest = intent.extras?.getBoolean("com.frafio.myfinance.userRequest", false) ?: false
                if (userRequest) {
                    fAuth = FirebaseAuth.getInstance()
                    layout.snackbar("Hai effettuato l'accesso come " + fAuth.currentUser?.displayName, mAddBtn)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            (supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment).navController,
            AppBarConfiguration(setOf(R.id.dashboardFragment, R.id.listFragment, R.id.profileFragment, R.id.menuFragment))
        )
    }

    fun onAddButtonClick(view: View) {
        val activityOptionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(
            mAddBtn, 0, 0,
            mAddBtn.measuredWidth, mAddBtn.measuredHeight
        )
        Intent(applicationContext, AddActivity::class.java).also {
            it.putExtra("com.frafio.myfinance.REQUESTCODE", 1)
            startActivityForResult(it, 1, activityOptionsCompat.toBundle())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val purchaseRequest =
                data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
            if (purchaseRequest) {
                mBottomNavigationView.selectedItemId = R.id.listFragment
                layout.snackbar("Acquisto aggiunto!", mAddBtn)
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            val editRequest = data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
            if (editRequest) {
                val navHostFragment: Fragment? =
                    supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView)
                val fragment = navHostFragment!!.childFragmentManager.fragments[0] as ListFragment?
                fragment?.reloadPurchaseList()
                layout.snackbar("Acquisto modificato!", mAddBtn)
            }
        }
    }

    fun showSnackbar(message: String) {
        layout.snackbar(message, mAddBtn)
    }
}