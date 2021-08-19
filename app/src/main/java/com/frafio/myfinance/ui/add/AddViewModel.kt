package com.frafio.myfinance.ui.add

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.add.AddActivity.Companion.ADD_PURCHASE_CODE
import com.frafio.myfinance.ui.add.AddActivity.Companion.EDIT_PURCHASE_CODE
import java.time.LocalDate

class AddViewModel(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {
    var listener: AddListener? = null

    val proPic: String? = userRepository.getProPic()

    var name: String? = null
    var priceString: String? = null
    var type: Int? = null

    var dateString: String? = null

    var year: Int? = LocalDate.now().year
    var month: Int? = LocalDate.now().monthValue
    var day: Int? = LocalDate.now().dayOfMonth

    var totChecked: Boolean = false

    var purchaseID: String? = null
    var purchasePrice: Double? = null
    var purchaseType: Int? = null
    var purchasePosition: Int? = null

    var requestCode: Int? = null

    fun updateTime(datePickerBtn: DatePickerButton) {
        year = datePickerBtn.year
        month = datePickerBtn.month
        day = datePickerBtn.day
        dateString = datePickerBtn.dateString
    }

    fun onAddButtonClick(view: View) {
        listener?.onAddStart()

        val userEmail = userRepository.getUser()!!.email

        // controlla le info aggiunte
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if (name == DbPurchases.NAMES.TOTALE.value && !totChecked) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.WRONG_NAME_TOTAL))
            return
        }

        if (totChecked) {
            val purchase =
                Purchase(userEmail, name, 0.0, year, month, day, DbPurchases.TYPES.TOTAL.value)
            val response = purchaseRepository.addTotale(purchase)
            listener?.onAddSuccess(response)
        } else {
            if (priceString.isNullOrEmpty()) {
                listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
                return
            }

            val price = priceString!!.toDouble()

            if (requestCode == ADD_PURCHASE_CODE) {
                val purchase = Purchase(userEmail, name, price, year, month, day, type)
                val response = purchaseRepository.addPurchase(purchase)
                listener?.onAddSuccess(response)
            } else if (requestCode == EDIT_PURCHASE_CODE) {
                val purchase =
                    Purchase(userEmail, name, price, year, month, day, purchaseType, purchaseID)

                val response =
                    purchaseRepository.editPurchase(purchase, purchasePosition!!, purchasePrice!!)
                listener?.onAddSuccess(response)
            }
        }
    }

    fun updateLocalList() {
        val response = purchaseRepository.updatePurchaseList()
        listener?.onAddSuccess(response)
    }
}