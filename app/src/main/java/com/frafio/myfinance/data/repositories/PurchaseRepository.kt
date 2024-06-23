package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.utils.dateToString
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PurchaseRepository(private val purchaseManager: PurchaseManager) {

    val avgTrendList: List<Pair<String, Double>>
        get() = calculateAvgTrend()

    fun purchaseListSize(): Int {
        return PurchaseStorage.purchaseList.size
    }

    fun getPurchaseList(): List<Purchase> {
        return PurchaseStorage.purchaseList
    }

    fun updatePurchaseList(limit: Long): LiveData<PurchaseResult> {
        return purchaseManager.updateList(limit)
    }

    fun getPurchaseNumber(): LiveData<PurchaseResult> {
        return purchaseManager.getPurchaseNumber()
    }

    fun getSumPrices(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getSumPrices(result)
    }

    fun getTodayTotal(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getTodayTotal(result)
    }

    fun getThisMonthTotal(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getThisMonthTotal(result)
    }

    fun calculateStats(): List<String> {
        /*
        values (index)
        0: dayAvg
        1: monthAvg
        2: todayTot
        3: tot
        4: lastMonthTot
        5: housingTot
        6: groceriesTot
        7: transportationTot
        */

        return mutableListOf(
            "€ 0.00",
            "€ 0.00",
            "€ 0.00",
            "€ 0.00",
            "€ 0.00",
            "€ 0.00",
            "€ 0.00",
            "€ 0.00"
        )
    }

    fun deletePurchaseAt(position: Int): LiveData<PurchaseResult> {
        return purchaseManager.deleteAt(position)
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addPurchase(purchase)
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int
    ): LiveData<PurchaseResult> {
        return purchaseManager.editPurchase(purchase, position)
    }

    private fun calculateAvgTrend(): List<Pair<String, Double>> {
        val avgList = mutableListOf<Pair<String, Double>>()

        if (PurchaseStorage.purchaseList.isEmpty()) {
            return avgList
        }
        // save the date of the first purchase
        var startDate = PurchaseStorage.purchaseList.last().getLocalDate()

        var priceSum = 0.0
        var purchaseCount = 0

        var lastCount = 0
        var lastDate: String? = null

        // purchaseList is inverted -> loop in reverse
        val todayDate = LocalDate.now()
        for (i in PurchaseStorage.purchaseList.size - 1 downTo 0) {
            PurchaseStorage.purchaseList[i].also { purchase ->
                val purchaseDate = purchase.getLocalDate()
                // consider just the totals
                if (ChronoUnit.DAYS.between(purchaseDate, todayDate) >= 0 &&
                    purchase.category == DbPurchases.CATEGORIES.TOTAL.value
                ) {
                    lastDate = dateToString(
                        purchase.day,
                        purchase.month,
                        purchase.year
                    )
                    // increment sum and count
                    priceSum += purchase.price!!
                    purchaseCount++

                    // calculate the new date
                    val newDate = purchase.getLocalDate()

                    // if has passed a week, update
                    if (ChronoUnit.DAYS.between(startDate, newDate) >= 7) {
                        // store the point where the evaluation is made
                        lastCount = purchaseCount

                        startDate = newDate

                        // calculate the new average
                        val newValue: Double = priceSum / purchaseCount
                        val element = Pair(
                            dateToString(purchase.day, purchase.month, purchase.year)!!,
                            newValue
                        )
                        avgList.add(element)
                    }
                }
            }
        }
        // if there are other purchases, add them
        if (lastCount != purchaseCount) {
            val newValue: Double = priceSum / purchaseCount
            val element = Pair(lastDate!!, newValue)
            avgList.add(element)
        }

        return avgList
    }

    fun setDynamicColorActive(active: Boolean) {
        purchaseManager.setDynamicColorActive(active)
    }

    fun getDynamicColorActive(): Boolean {
        return purchaseManager.getDynamicColorActive()
    }
}