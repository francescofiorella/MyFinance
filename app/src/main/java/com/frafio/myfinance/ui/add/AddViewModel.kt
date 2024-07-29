package com.frafio.myfinance.ui.add

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.repositories.IncomeRepository
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp
import java.time.LocalDate

class AddViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomeManager
    )

    var listener: AddListener? = null

    var name: String? = null
    var priceString: String? = null
    var category: Int? = null

    var dateString: String? = null

    var year: Int? = LocalDate.now().year
    var month: Int? = LocalDate.now().monthValue
    var day: Int? = LocalDate.now().dayOfMonth

    var purchaseID: String? = null
    var purchasePosition: Int? = null

    var requestCode: Int? = null
    var purchaseCode: Int? = null

    fun updateTime(datePickerBtn: DatePickerButton) {
        year = datePickerBtn.year
        month = datePickerBtn.month
        day = datePickerBtn.day
        dateString = datePickerBtn.dateString
    }

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onAddStart()

        // check info
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if ((name == DbPurchases.NAMES.TOTAL.valueEn || name == DbPurchases.NAMES.TOTAL.valueIt)) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.WRONG_NAME_TOTAL))
            return
        }

        if (priceString.isNullOrEmpty()) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
            return
        }

        if (purchaseCode == AddActivity.REQUEST_INCOME_CODE) {
            category = DbPurchases.CATEGORIES.INCOME.value
        } else if (category == -1) {
            listener?.onAddFailure(PurchaseResult(PurchaseCode.EMPTY_CATEGORY))
            return
        }

        val price = priceString!!.toDouble()

        when (requestCode) {
            AddActivity.REQUEST_ADD_CODE -> {
                val response = if (purchaseCode == AddActivity.REQUEST_PAYMENT_CODE) {
                    val purchase = Purchase(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category
                    )
                    purchaseRepository.addPurchase(purchase)
                } else {
                    val income = Income(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category
                    )
                    incomeRepository.addIncome(income)
                }
                listener?.onAddSuccess(response)
            }

            AddActivity.REQUEST_EDIT_CODE -> {
                val response = if (purchaseCode == AddActivity.REQUEST_PAYMENT_CODE) {
                    val purchase = Purchase(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category,
                        purchaseID!!
                    )
                    purchaseRepository.editPurchase(purchase)
                } else {
                    val income = Income(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category,
                        purchaseID!!
                    )
                    incomeRepository.editIncome(income, purchasePosition!!)
                }
                listener?.onAddSuccess(response)
            }
        }
    }
}