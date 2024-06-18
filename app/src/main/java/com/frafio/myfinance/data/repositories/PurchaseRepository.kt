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
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
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

        if (PurchaseStorage.purchaseList.isEmpty()) {
            return mutableListOf("0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0")
        }

        val values = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val todayDate = LocalDate.now()
        val lastPurchase = PurchaseStorage.purchaseList.last()
        var purchaseDate = lastPurchase.getLocalDate()
        val nDays = ChronoUnit.DAYS.between(purchaseDate, todayDate) + 1
        var nMonth: Long
        var lastMonth = 0
        var lastYear = 0

        PurchaseStorage.purchaseList.forEach { purchase ->
            purchaseDate = purchase.getLocalDate()
            if (ChronoUnit.DAYS.between(purchaseDate, todayDate) >= 0) {
                when (purchase.type) {
                    DbPurchases.TYPES.TOTAL.value -> {
                        // today total
                        if (purchase.year == todayDate.year && purchase.month == todayDate.monthValue) {
                            values[4] += purchase.price ?: 0.0

                            if (purchase.day == todayDate.dayOfMonth) {
                                values[2] = purchase.price ?: 0.0
                            }
                        }

                        // increment tot
                        values[3] += purchase.price ?: 0.0

                        // count the month number
                        if (purchase.year != lastYear) {
                            lastYear = purchase.year!!
                            lastMonth = purchase.month!!
                        } else if (purchase.month != lastMonth) {
                            lastMonth = purchase.month!!
                        }
                    }

                    DbPurchases.TYPES.HOUSING.value -> {
                        // housingTot
                        values[5] += purchase.price ?: 0.0
                    }

                    DbPurchases.TYPES.GROCERIES.value -> {
                        // groceriesTot
                        values[6] += purchase.price ?: 0.0
                    }

                    DbPurchases.TYPES.TRANSPORTATION.value -> {
                        // transportationTot
                        values[7] += purchase.price ?: 0.0
                    }
                }
            }
        }

        if (PurchaseStorage.purchaseList.isNotEmpty()) {
            PurchaseStorage.purchaseList.first().also { first ->
                val endDate = first.getLocalDate()
                PurchaseStorage.purchaseList.last().also { last ->
                    val startDate = last.getLocalDate()

                    nMonth = ChronoUnit.MONTHS.between(startDate, endDate) + 1
                }
            }
        } else {
            nMonth = 1 // should not be executed
        }

        values[0] = values[3] / nDays
        values[1] = values[3] / nMonth

        val stats = mutableListOf<String>()

        values.forEach { value ->
            val string = if (value < 1000.0) {
                doubleToPrice(value)
            } else {
                doubleToPriceWithoutDecimals(value)
            }
            stats.add(string)
        }

        return stats
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
                    purchase.type == DbPurchases.TYPES.TOTAL.value
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