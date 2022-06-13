package com.frafio.myfinance.ui.add

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository
import java.time.LocalDate
import kotlin.concurrent.thread

class AddViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: AddListener? = null

    var name: String? = null
    var priceString: String? = null
    var type: Int? = null

    var dateString: String? = null

    var year: Int? = LocalDate.now().year
    var month: Int? = LocalDate.now().monthValue
    var day: Int? = LocalDate.now().dayOfMonth

    var purchaseID: String? = null
    var purchasePrice: Double? = null
    var purchaseType: Int? = null
    var purchasePosition: Int? = null

    var requestCode: Int? = null

    fun updateTime(datePickerBtn: DatePickerButton) {
        thread {
            year = datePickerBtn.year
            month = datePickerBtn.month
            day = datePickerBtn.day
            dateString = datePickerBtn.dateString
        }
    }

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onAddStart()

        val userEmail = userRepository.getUser()!!.email

        // check info
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if ((name == DbPurchases.NAMES.TOTAL.value_en || name == DbPurchases.NAMES.TOTAL.value_it)
            && type != DbPurchases.TYPES.TOTAL.value
        ) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.WRONG_NAME_TOTAL))
            return
        }

        if (type == DbPurchases.TYPES.TOTAL.value) {
            val purchase =
                Purchase(userEmail, name, 0.0, year, month, day, DbPurchases.TYPES.TOTAL.value)
            val response = purchaseRepository.addTotal(purchase)
            listener?.onAddSuccess(response)
        } else {
            if (priceString.isNullOrEmpty()) {
                listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
                return
            }

            val price = priceString!!.toDouble()

            if (requestCode == AddActivity.REQUEST_ADD_CODE) {
                val purchase = Purchase(userEmail, name, price, year, month, day, type)
                val response = purchaseRepository.addPurchase(purchase)
                listener?.onAddSuccess(response)
            } else if (requestCode == AddActivity.REQUEST_EDIT_CODE) {
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