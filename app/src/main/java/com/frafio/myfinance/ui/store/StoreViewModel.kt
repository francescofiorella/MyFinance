package com.frafio.myfinance.ui.store

import android.view.View
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class StoreViewModel : ViewModel() {

    var nome: String? = null
    var costoString: String? = null
    var dataString: String? = null
    var checked: Boolean? = null

    var year: Int? = null
    var month: Int? = null
    var day: Int? = null

    var storeListener: StoreListener? = null

    fun onNoPurchaseCheckedChanged(view: CompoundButton, checked: Boolean) {

    }

    fun onAddButtonClick(view: View) {

    }

    fun initLayout(code: Int) {
        if (code == 1) {
            // set data odierna
            year = LocalDate.now().year
            month = LocalDate.now().monthValue
            day = LocalDate.now().dayOfMonth

            mGenBtn.isSelected = true

            setTypeButton()
            setTotSwitch()

            setDatePicker()
        } else if (code == 2) {
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
            mPriceET.setText(formatter.format(purchasePrice))

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
            mAddBtn.icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_create)
        }
    }
}