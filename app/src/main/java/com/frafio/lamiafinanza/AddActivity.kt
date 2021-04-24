package com.frafio.lamiafinanza

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.frafio.lamiafinanza.MainActivity.Companion.CURRENT_USER
import com.frafio.lamiafinanza.MainActivity.Companion.PURCHASE_ID_LIST
import com.frafio.lamiafinanza.MainActivity.Companion.PURCHASE_LIST
import com.frafio.lamiafinanza.models.Purchase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class AddActivity : AppCompatActivity() {

    lateinit var layout: RelativeLayout
    var nunito: Typeface? = null

    lateinit var mToolbar: MaterialToolbar
    lateinit var mNameET: EditText
    lateinit var mPriceET:EditText
    lateinit var mDateBtn: ConstraintLayout
    lateinit var mTypeLayout: GridLayout
    lateinit var mBigliettoLayout: GridLayout
    lateinit var mDateET: TextView
    lateinit var mGenBtn:TextView
    lateinit var mSpeBtn:TextView
    lateinit var mBigBtn:TextView
    lateinit var mTIBtn:TextView
    lateinit var mAmBtn:TextView
    lateinit var mAltroBtn:TextView
    lateinit var mDateArrowImg: ImageView
    lateinit var mTotSwitch: SwitchMaterial
    lateinit var mAddBtn: ExtendedFloatingActionButton

    var requestCode = 0
    var purchaseId: String = ""
    var purchaseName: String = ""
    var purchasePrice = 0.0
    var purchaseType = 0
    var purchasePosition = 0

    var year = 0
    var month = 0
    var day = 0

    val interpolator = OvershootInterpolator()

    companion object {
        private val TAG = AddActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

        // toolbar
        mToolbar = findViewById(R.id.add_toolbar)
        setSupportActionBar(mToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // collegamento view
        layout = findViewById(R.id.add_layout)
        mNameET = findViewById(R.id.add_name_EditText)
        mPriceET = findViewById(R.id.add_price_EditText)
        mDateBtn = findViewById(R.id.add_dateLayout)
        mDateET = findViewById(R.id.add_dateTextView)
        mDateArrowImg = findViewById(R.id.add_dateArrowImageView)
        mTotSwitch = findViewById(R.id.add_totale_switch)
        mTypeLayout = findViewById(R.id.add_typeLayout)
        mGenBtn = findViewById(R.id.add_generico_tv)
        mSpeBtn = findViewById(R.id.add_spesa_tv)
        mBigBtn = findViewById(R.id.add_biglietto_tv)
        mBigliettoLayout = findViewById(R.id.add_bigliettoLayout)
        mTIBtn = findViewById(R.id.add_trenitalia_tv)
        mAmBtn = findViewById(R.id.add_amtab_tv)
        mAltroBtn = findViewById(R.id.add_altro_tv)
        mAddBtn = findViewById(R.id.add_addButton)

        mBigliettoLayout.alpha = 0f

        // stabilisci se bisogna creare un nuovo evento (1) o modificarne uno esistente (2)
        requestCode = intent.getIntExtra("com.frafio.myfinance.REQUESTCODE", 0)

        if (requestCode == 1) {
            // set data odierna
            year = LocalDate.now().year
            month = LocalDate.now().monthValue
            day = LocalDate.now().dayOfMonth
            mGenBtn.isSelected = true
            setTypeButton()
            setTotSwitch()
            setDatePicker()
        } else if (requestCode == 2) {
            mTotSwitch.visibility = View.GONE
            purchaseId = intent.getStringExtra("com.frafio.myfinance.PURCHASE_ID")!!
            purchaseName = intent.getStringExtra("com.frafio.myfinance.PURCHASE_NAME")!!
            purchasePrice = intent.getDoubleExtra("com.frafio.myfinance.PURCHASE_PRICE", 0.0)
            purchaseType = intent.getIntExtra("com.frafio.myfinance.PURCHASE_TYPE", 0)
            purchasePosition = intent.getIntExtra("com.frafio.myfinance.PURCHASE_POSITION", 0)
            year = intent.getIntExtra("com.frafio.myfinance.PURCHASE_YEAR", 0)
            month = intent.getIntExtra("com.frafio.myfinance.PURCHASE_MONTH", 0)
            day = intent.getIntExtra("com.frafio.myfinance.PURCHASE_DAY", 0)
            mNameET.setText(purchaseName)
            val locale = Locale("en", "UK")
            val nf = NumberFormat.getInstance(locale)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("###,###,##0.00")
            mPriceET.setText("€ ${formatter.format(purchasePrice)}")
            when (purchaseType) {
                1 -> {
                    mGenBtn.isEnabled = false
                    mSpeBtn.isSelected = true
                    mBigBtn.isEnabled = false
                }
                2 -> {
                    mGenBtn.isSelected = true
                    mSpeBtn.isEnabled = false
                    mBigBtn.isEnabled = false
                }
                3 -> {
                    mGenBtn.isEnabled = false
                    mSpeBtn.isEnabled = false
                    mBigBtn.isSelected = true
                    setBigliettoLayout()
                }
            }
            val dayString: String = if (day < 10) {
                "0$day"
            } else {
                day.toString()
            }
            val monthString: String = if (month < 10) {
                "0$month"
            } else {
                month.toString()
            }
            val dateString = "$dayString/$monthString/$year"
            mDateET.text = dateString
            mDateET.setTextColor(ContextCompat.getColor(applicationContext, R.color.disabled_text))
            mDateBtn.isClickable = false
            mDateArrowImg.visibility = View.GONE
            mAddBtn.text = "Modifica"
            mAddBtn.setIcon(ContextCompat.getDrawable(applicationContext, R.drawable.ic_create))
        }

        mAddBtn.setOnClickListener {
            addPurchase()
        }
    }

    private fun setDatePicker() {
        val dayString: String = if (day < 10) {
            "0$day"
        } else {
            day.toString() + ""
        }
        val monthString: String = if (month < 10) {
            "0$month"
        } else {
            month.toString() + ""
        }
        val dateString = "$dayString/$monthString/$year"
        mDateET.text = dateString

        // date picker
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Seleziona una data")
        builder.setSelection(today)
        builder.setTheme(R.style.ThemeOverlay_MyFinance_DatePicker)
        val materialDatePicker = builder.build()
        mDateBtn.setOnClickListener {
            showDatePicker(materialDatePicker)
        }
    }

    private fun showDatePicker(materialDatePicker: MaterialDatePicker<*>) {
        if (!materialDatePicker.isAdded) {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
            materialDatePicker.addOnPositiveButtonClickListener { selection -> // get selected date
                val date = Date(selection.toString().toLong())
                val calendar = Calendar.getInstance()

                calendar.time = date

                year = calendar[Calendar.YEAR]
                month = calendar[Calendar.MONTH] + 1
                day = calendar[Calendar.DAY_OF_MONTH]

                val dayString: String = if (day < 10) {
                    "0$day"
                } else {
                    day.toString() + ""
                }

                val monthString: String = if (month < 10) {
                    "0$month"
                } else {
                    month.toString() + ""
                }
                val dateString = "$dayString/$monthString/$year"
                mDateET.text = dateString
            }
        }
    }

    private fun setTypeButton() {
        mGenBtn.setOnClickListener {
            if (!mGenBtn.isSelected) {
                closeTicketBtn()
                if (mBigBtn.isSelected) {
                    mNameET.setText("")
                }
                mNameET.isEnabled = true
                mGenBtn.isSelected = true
                mSpeBtn.isSelected = false
                mBigBtn.isSelected = false
            }
        }

        mSpeBtn.setOnClickListener {
            if (!mSpeBtn.isSelected) {
                closeTicketBtn()
                if (mBigBtn.isSelected) {
                    mNameET.setText("")
                }
                mNameET.isEnabled = true
                mGenBtn.isSelected = false
                mSpeBtn.isSelected = true
                mBigBtn.isSelected = false
            }
        }

        mBigBtn.setOnClickListener {
            if (!mBigBtn.isSelected) {
                mGenBtn.isSelected = false
                mSpeBtn.isSelected = false
                mBigBtn.isSelected = true
                setBigliettoLayout()
            }
        }
    }

    private fun setBigliettoLayout() {
        if (mBigBtn.isSelected) {
            openTicketBtn()
            mTIBtn.setOnClickListener {
                if (!mTIBtn.isSelected) {
                    mTIBtn.isSelected = true
                    mAmBtn.isSelected = false
                    mAltroBtn.isSelected = false
                    mNameET.setText("Biglietto TrenItalia")
                    mNameET.isEnabled = false
                }
            }

            mAmBtn.setOnClickListener {
                if (!mAmBtn.isSelected) {
                    mTIBtn.isSelected = false
                    mAmBtn.isSelected = true
                    mAltroBtn.isSelected = false
                    mNameET.setText("Biglietto Amtab")
                    mNameET.isEnabled = false
                }
            }

            mAltroBtn.setOnClickListener {
                if (!mAltroBtn.isSelected) {
                    mTIBtn.isSelected = false
                    mAmBtn.isSelected = false
                    mAltroBtn.isSelected = true
                    mNameET.setText("")
                    mNameET.isEnabled = true
                }
            }

            if (requestCode == 2) {
                if (purchaseName == "Biglietto TrenItalia") {
                    mTIBtn.performClick()
                } else if (purchaseName == "Biglietto Amtab") {
                    mAmBtn.performClick()
                } else {
                    mAltroBtn.performClick()
                }
            } else {
                mTIBtn.performClick()
            }
        } else {
            closeTicketBtn()
        }
    }

    private fun openTicketBtn() {
        if (mBigliettoLayout.visibility == View.GONE) {
            mBigliettoLayout.animate().setInterpolator(interpolator).alpha(1f).setDuration(1500)
                .start()
            mBigliettoLayout.visibility = View.VISIBLE
            val root = layout as ViewGroup
            TransitionManager.beginDelayedTransition(root)
            val transition = AutoTransition()
            transition.duration = 2000
            TransitionManager.beginDelayedTransition(root, transition)
        }
    }

    private fun closeTicketBtn() {
        if (mBigliettoLayout.visibility == View.VISIBLE) {
            mBigliettoLayout.animate().setInterpolator(interpolator).alpha(0f).setDuration(1500)
                .start()
            mBigliettoLayout.visibility = View.GONE
            val root = layout as ViewGroup
            TransitionManager.beginDelayedTransition(root)
            val transition = AutoTransition()
            transition.duration = 2000
            TransitionManager.beginDelayedTransition(root, transition)
        }
    }

    private fun setTotSwitch() {
        mTotSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mNameET.setText("Totale")
                mNameET.isEnabled = false
                mPriceET.setText("0.00")
                mPriceET.isEnabled = false
                mNameET.error = null
                mPriceET.error = null
                mGenBtn.isEnabled = false
                mSpeBtn.isEnabled = false
                mBigBtn.isEnabled = false
                closeTicketBtn()
            } else {
                mNameET.setText("")
                mNameET.isEnabled = true
                mPriceET.setText("")
                mPriceET.isEnabled = true
                mGenBtn.isEnabled = true
                mSpeBtn.isEnabled = true
                mBigBtn.isEnabled = true
                setBigliettoLayout()
            }
        }
    }

    private fun addPurchase() {
        val name = mNameET.text.toString().trim()

        // controlla le info aggiunte
        if (TextUtils.isEmpty(name)) {
            mNameET.error = "Inserisci il nome dell'acquisto."
            return
        }

        if (name == "Totale" && !mTotSwitch.isChecked) {
            mNameET.error = "L'acquisto non può chiamarsi 'Totale'."
            return
        }

        if (mTotSwitch.isChecked) {
            val purchase = Purchase(CURRENT_USER?.email, name, 0.0, year, month, day, 0)

            val fStore = FirebaseFirestore.getInstance()
            val totID = "$year$month$day"
            fStore.collection("purchases").document(totID).set(purchase)
                .addOnSuccessListener {
                    updateAndGoToList()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    showSnackbar("Totale non aggiunto!")
                }
        } else {
            val priceString = mPriceET.text.toString().trim()
            if (TextUtils.isEmpty(priceString)) {
                mPriceET.error = "Inserisci il costo dell'acquisto."
                return
            }

            val price = priceString.toDouble()

            if (requestCode == 1) {
                val type = if (mGenBtn.isSelected) {
                    2
                } else if (mSpeBtn.isSelected) {
                    1
                } else {
                    3
                }

                val purchase = Purchase(CURRENT_USER?.email, name, price, year, month, day, type)

                val fStore = FirebaseFirestore.getInstance()
                fStore.collection("purchases").add(purchase)
                    .addOnSuccessListener {
                        var sum = if (purchase.type != 3) {
                            purchase.price ?: 0.0
                        } else {
                            0.0
                        }
                        for (item in PURCHASE_LIST) {
                            if (item.email == CURRENT_USER?.email && item.type != 0
                                && item.type != 3 && item.year == purchase.year
                                && item.month == purchase.month && item.day == purchase.day) {
                                sum += item.price ?: 0.0
                            }
                        }
                        val totalP = Purchase(CURRENT_USER?.email, "Totale", sum, year, month, day, 0)
                        val fStore1 = FirebaseFirestore.getInstance()
                        val totID = "$year$month$day"
                        fStore1.collection("purchases").document(totID).set(totalP)
                            .addOnSuccessListener {
                                updateAndGoToList()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error! ${e.localizedMessage}")
                                showSnackbar("Acquisto non aggiunto correttamente!")
                            }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error! ${e.localizedMessage}")
                        showSnackbar("Acquisto non aggiunto!")
                    }
            } else if (requestCode == 2) {
                val purchase = Purchase(CURRENT_USER?.email, name, price, year, month, day, purchaseType)
                val fStore = FirebaseFirestore.getInstance()
                fStore.collection("purchases").document(purchaseId).set(purchase)
                    .addOnSuccessListener {
                        PURCHASE_LIST[purchasePosition] = purchase
                        if (price != purchasePrice) {
                            var sum = 0.0
                            for (item in PURCHASE_LIST) {
                                if (item.email == CURRENT_USER?.email && item.type != 0
                                    && item.type != 3 && item.year == purchase.year
                                    && item.month == purchase.month && item.day == purchase.day) {
                                    sum += item.price ?: 0.0
                                }
                            }
                            val totalP = Purchase(CURRENT_USER?.email, "Totale", sum, year, month, day, 0)
                            val fStore1 = FirebaseFirestore.getInstance()
                            val totID = "$year$month$day"
                            fStore1.collection("purchases").document(totID).set(totalP)
                                .addOnSuccessListener {
                                    updateAndGoToList()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")
                                    showSnackbar("Acquisto non aggiunto correttamente!")
                                }
                        } else {
                            // torna alla home
                            val returnIntent = Intent()
                            returnIntent.putExtra("com.frafio.myfinance.purchaseRequest", true)
                            setResult(RESULT_OK, returnIntent)
                            finish()
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error! ${e.localizedMessage}")
                        showSnackbar("Acquisto non modificato!")
                    }
            }
        }
    }

    // metodo per aggiornare i progressi dell'utente
    fun updateAndGoToList() {
        PURCHASE_LIST = mutableListOf()
        PURCHASE_ID_LIST = mutableListOf()
        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("purchases").whereEqualTo("email", CURRENT_USER?.email)
            .orderBy("year", Query.Direction.DESCENDING)
            .orderBy("month", Query.Direction.DESCENDING)
            .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
            .orderBy("price", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for ((position, document) in queryDocumentSnapshots.withIndex()) {
                    val purchase = document.toObject(Purchase::class.java)
                    PURCHASE_ID_LIST.add(position, document.id)
                    PURCHASE_LIST.add(position, purchase)
                }

                // torna alla home
                val returnIntent = Intent()
                returnIntent.putExtra("com.frafio.myfinance.purchaseRequest", true)
                setResult(RESULT_OK, returnIntent)
                finish()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
            }
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
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