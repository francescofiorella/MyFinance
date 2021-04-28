package com.frafio.myfinance.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.frafio.myfinance.R
import com.frafio.myfinance.fragments.DashboardFragment
import com.frafio.myfinance.fragments.ListFragment
import com.frafio.myfinance.fragments.MenuFragment
import com.frafio.myfinance.fragments.ProfileFragment
import com.frafio.myfinance.data.Purchase
import com.frafio.myfinance.data.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    // definizione variabili
    lateinit var layout: CoordinatorLayout
    var nunito: Typeface? = null

    lateinit var mToolbar: MaterialToolbar
    lateinit var mFragmentTitle: TextView
    lateinit var mBottomNavigationView: BottomNavigationView
    lateinit var mAddBtn: FloatingActionButton

    private lateinit var fAuth: FirebaseAuth

    // 1 home, 2 list, 3 profile, 4 settings
    var currentFragment = 0

    companion object {
        var CURRENT_USER: User? = null
        var PURCHASE_LIST = mutableListOf<Purchase>()
        var PURCHASE_ID_LIST = mutableListOf<String>()
        private val KEY_FRAGMENT = "com.frafio.myfinance.SAVE_FRAGMENT"
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

        // toolbar
        mToolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(mToolbar)

        // collegamento view
        layout = findViewById(R.id.main_layout)
        mFragmentTitle = findViewById(R.id.main_fragmentTitle)
        mBottomNavigationView = findViewById(R.id.main_bottomNavView)
        mAddBtn = findViewById(R.id.main_addBtn)

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(KEY_FRAGMENT)
        } else {
            // controlla se si è appena fatto l'accesso
            fAuth = FirebaseAuth.getInstance()
            if (intent.hasExtra("com.frafio.myfinance.userRequest")) {
                val userRequest = intent.extras?.getBoolean("com.frafio.myfinance.userRequest", false) ?: false
                if (userRequest) {
                    showSnackbar("Hai effettuato l'accesso come " + fAuth.currentUser?.displayName)
                }
            }

            // aggiorna i dati dell'utente
            updateCurrentUser()
        }

        // imposta la bottomNavView
        mBottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> setFragment(1)
                R.id.list -> setFragment(2)
                R.id.profile -> setFragment(3)
                R.id.menu -> setFragment(4)
            }
            true
        }

        mAddBtn.setOnClickListener {
            val activityOptionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(
                mAddBtn, 0, 0,
                mAddBtn.measuredWidth, mAddBtn.measuredHeight
            )
            val intent = Intent(applicationContext, AddActivity::class.java)
            intent.putExtra("com.frafio.myfinance.REQUESTCODE", 1)
            startActivityForResult(intent, 1, activityOptionsCompat.toBundle())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_FRAGMENT, currentFragment)
    }

    // metodo per cambiare fragment (senza influenzare la bottomNavView)
    private fun setFragment(num: Int) {
        if (currentFragment != num) {
            var mFragmentToSet: Fragment? = null
            when (num) {
                1 -> {
                    mFragmentTitle.text = getString(R.string.nav_1)
                    mFragmentToSet = DashboardFragment()
                }
                2 -> {
                    mFragmentTitle.text = getString(R.string.nav_2_extended)
                    mFragmentToSet = ListFragment()
                }
                3 -> {
                    mFragmentTitle.text = getString(R.string.nav_3)
                    mFragmentToSet = ProfileFragment()
                }
                4 -> {
                    mFragmentTitle.text = getString(R.string.nav_4)
                    mFragmentToSet = MenuFragment()
                }
            }
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_frameLayout, mFragmentToSet!!).commit()
            currentFragment = num
        } else if (currentFragment == 2) {
            val fragment: ListFragment? =
                supportFragmentManager.findFragmentById(R.id.main_frameLayout) as ListFragment?
            fragment?.scrollListToTop()
        }
    }

    // metodo per aggiornare i dati dell'utente
    private fun updateCurrentUser() {
        fAuth = FirebaseAuth.getInstance()
        val fUser = fAuth.currentUser
        if (fUser != null) {
            if (CURRENT_USER == null) {
                val fStore = FirebaseFirestore.getInstance()
                fStore.collection("users").document(fUser.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        CURRENT_USER = documentSnapshot.toObject(User::class.java)!!
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error! " + e.localizedMessage)
                    }
            }
            updateList()
        }
    }

    // metodo per aggiornare i progressi dell'utente
    fun updateList() {
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
                if (currentFragment == 0) {
                    setFragment(1)
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
                if (currentFragment == 2) {
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.main_frameLayout) as ListFragment?
                    fragment?.loadPurchasesList()
                } else {
                    mBottomNavigationView.selectedItemId = R.id.list
                }
                showSnackbar("Acquisto aggiunto!")
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            val editRequest = data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
            if (editRequest) {
                val fragment = supportFragmentManager.findFragmentById(R.id.main_frameLayout) as ListFragment?
                fragment?.loadPurchasesList()
                showSnackbar("Acquisto modificato!")
            }
        }
    }

    // onBackPressed
    override fun onBackPressed() {
        if (currentFragment != 1) {
            mBottomNavigationView.selectedItemId = R.id.dashboard
        } else {
            super.onBackPressed()
        }
    }

    // snackbar
    fun showSnackbar(string: String) {
        val snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
            .setAnchorView(mAddBtn)
            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.snackbar))
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.inverted_primary_text))
        val tv = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito
        snackbar.show()
    }
}