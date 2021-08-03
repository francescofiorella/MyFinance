package com.frafio.myfinance.ui.add

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository
import java.time.LocalDate

class AddViewModel(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    var listener: AddListener? = null

    var name: String? = null
    var priceString: String? = null
    var type: Int? = null

    var dateString: String? = null

    var year: Int? = null
    var month: Int? = null
    var day: Int? = null
    var totChecked: Boolean = false

    var purchaseID: String? = null
    var purchasePrice: Double? = null
    var purchaseType: Int? = null
    var purchasePosition: Int? = null

    var requestCode: Int? = null

    fun updateDateTV(code: Int?): String? {
        code?.let {
            if (code == 1) {
                // set data odierna
                year = LocalDate.now().year
                month = LocalDate.now().monthValue
                day = LocalDate.now().dayOfMonth
            }
        }

        day?.let { day ->
            month?.let { month ->
                year?.let { year ->
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

                    dateString = "$dayString/$monthString/$year"
                }
            }
        }

        return dateString
    }

    fun onAddButtonClick(view: View) {
        listener?.onAddStart()

        val userEmail = userRepository.getUser()!!.email
        // controlla la info aggiunte
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(1)
            return
        }

        if (name == "Totale" && !totChecked) {
            listener?.onAddFailure(2)
            return
        }


        if (totChecked) {
            val purchase = Purchase(userEmail, name, 0.0, year, month, day, 0)
            val response = purchaseRepository.addTotale(purchase)
            listener?.onAddSuccess(response)
        } else {
            if (priceString.isNullOrEmpty()) {
                listener?.onAddFailure(3)
                return
            }

            val price = priceString!!.toDouble()

            if (requestCode == 1) {
                val purchase = Purchase(userEmail, name, price, year, month, day, type)
                val response = purchaseRepository.addPurchase(purchase)
                listener?.onAddSuccess(response)
            } else if (requestCode == 2) {
                val purchase = Purchase(userEmail, name, price, year, month, day, purchaseType, purchaseID)

                val response = purchaseRepository.editPurchase(purchase, purchasePosition!!, purchasePrice!!)
                listener?.onAddSuccess(response)
            }
        }
    }

    fun updateLocalList() {
        val response = purchaseRepository.updatePurchaseList()
        listener?.onAddSuccess(response as LiveData<Any>)
    }
}