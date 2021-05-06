package com.frafio.myfinance.ui.store

import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {

    var nome: String? = null
    var costoString: String? = null
    var dataString: String? = null
    var checked: Boolean? = null

    var year: Int? = null
    var month: Int? = null
    var day: Int? = null

    fun onNoPurchaseCheckedChanged(view: CompoundButton, checked: Boolean) {

    }

    fun onAddButtonClick(view: View) {
    }
}