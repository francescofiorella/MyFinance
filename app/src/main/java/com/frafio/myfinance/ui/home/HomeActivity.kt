package com.frafio.myfinance.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.ui.home.list.ListFragment
import com.frafio.myfinance.ui.store.AddActivity
import com.frafio.myfinance.utils.snackbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    // definizione variabili
    private lateinit var layout: CoordinatorLayout

    private lateinit var mToolbar: MaterialToolbar
    private lateinit var mFragmentTitle: TextView
    private lateinit var mBottomNavigationView: BottomNavigationView
    private lateinit var mAddBtn: FloatingActionButton

    private lateinit var fAuth: FirebaseAuth

    companion object {
        var PURCHASE_LIST = mutableListOf<Purchase>()
        var PURCHASE_ID_LIST = mutableListOf<String>()
        private val TAG = HomeActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // toolbar
        mToolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(mToolbar)

        // collegamento view
        layout = findViewById(R.id.main_layout)
        mFragmentTitle = findViewById(R.id.home_fragmentTitle)
        mBottomNavigationView = findViewById(R.id.home_bottomNavView)
        mAddBtn = findViewById(R.id.home_addBtn)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.dashboardFragment, R.id.listFragment, R.id.profileFragment, R.id.menuFragment))

        mBottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (savedInstanceState == null) {
            // controlla se si è appena fatto l'accesso
            fAuth = FirebaseAuth.getInstance()
            if (intent.hasExtra("com.frafio.myfinance.userRequest")) {
                val userRequest = intent.extras?.getBoolean("com.frafio.myfinance.userRequest", false) ?: false
                if (userRequest) {
                    layout.snackbar("Hai effettuato l'accesso come " + fAuth.currentUser?.displayName, mAddBtn)
                }
            }

            // aggiorna i dati dell'utente
            updateList()
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
        val intent = Intent(applicationContext, AddActivity::class.java)
        intent.putExtra("com.frafio.myfinance.REQUESTCODE", 1)
        startActivityForResult(intent, 1, activityOptionsCompat.toBundle())
    }

    // metodo per aggiornare i progressi dell'utente
    private fun updateList() {
        PURCHASE_LIST = mutableListOf()
        PURCHASE_ID_LIST = mutableListOf()
        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("purchases").whereEqualTo("email", fAuth.currentUser!!.email)
            .orderBy("year", Query.Direction.DESCENDING)
            .orderBy("month", Query.Direction.DESCENDING)
            .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
            .orderBy("price", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                for ((position, document) in queryDocumentSnapshots.withIndex()) {
                    val purchase = document.toObject(Purchase::class.java)
                    PURCHASE_ID_LIST.add(position, document.id)
                    PURCHASE_LIST.add(position, purchase)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! " + e.localizedMessage)
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
                val fragment = supportFragmentManager.findFragmentById(R.id.home_fragmentContainerView) as ListFragment?
                fragment?.loadPurchasesList()
                layout.snackbar("Acquisto modificato!", mAddBtn)
            }
        }
    }

    fun showSnackbar(message: String) {
        layout.snackbar(message, mAddBtn)
    }
}