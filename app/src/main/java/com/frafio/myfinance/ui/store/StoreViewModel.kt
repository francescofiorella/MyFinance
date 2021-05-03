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
}