package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
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

    fun getCategories(): LiveData<Pair<PurchaseResult, List<String>>> {
        return purchaseManager.getCategories()
    }

    fun updatePurchaseList(): LiveData<PurchaseResult> {
        return purchaseManager.updateList()
    }

    fun purchaseListSize(): Int {
        return PurchaseStorage.purchaseList.size
    }

    fun getPurchaseList(): List<Purchase> {
        return PurchaseStorage.purchaseList
    }

    fun calculateStats(): List<String> {
        /*
        values (index)
        0: dayAvg
        1: monthAvg
        2: todayTot
        3: tot
        4: lastMonthTot
        5: rentTot
        6: shoppingTot
        7: transportTot
        */

        val values = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        var nDays = 0
        var nMonth: Long
        var lastMonth = 0
        var lastYear = 0

        PurchaseStorage.purchaseList.forEach { purchase ->
            when (purchase.type) {
                DbPurchases.TYPES.TOTAL.value -> {
                    // today total
                    val year = LocalDate.now().year
                    val month = LocalDate.now().monthValue
                    val day = LocalDate.now().dayOfMonth
                    if (purchase.year == year && purchase.month == month) {
                        values[4] += purchase.price ?: 0.0

                        if (purchase.day == day) {
                            values[2] = purchase.price ?: 0.0
                        }
                    }

                    // increment tot
                    values[3] += purchase.price ?: 0.0

                    // count the day number
                    nDays++

                    // count the month number
                    if (purchase.year != lastYear) {
                        lastYear = purchase.year ?: 0
                        lastMonth = purchase.month ?: 0
                    } else if (purchase.month != lastMonth) {
                        lastMonth = purchase.month ?: 0
                    }
                }

                DbPurchases.TYPES.TRANSPORT.value -> {
                    // transportTot
                    values[7] += purchase.price ?: 0.0
                }

                DbPurchases.TYPES.RENT.value -> {
                    // rentTot
                    values[5] += purchase.price ?: 0.0
                }

                DbPurchases.TYPES.SHOPPING.value -> {
                    // shoppingTot
                    values[6] += purchase.price ?: 0.0
                }
            }
        }

        if (PurchaseStorage.purchaseList.isNotEmpty()) {
            PurchaseStorage.purchaseList.first().also { first ->
                val endDate = LocalDate.of(
                    first.year!!,
                    first.month!!,
                    first.day!!
                )
                PurchaseStorage.purchaseList.last().also { last ->
                    val startDate = LocalDate.of(
                        last.year!!,
                        last.month!!,
                        last.day!!
                    )

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

    fun deletePurchaseAt(position: Int): LiveData<Triple<PurchaseResult, List<Purchase>, Int?>> {
        return purchaseManager.deleteAt(position)
    }

    fun addTotal(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addTotal(purchase)
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addPurchase(purchase)
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int,
        purchasePrice: Double
    ): LiveData<PurchaseResult> {
        return purchaseManager.editPurchase(purchase, position, purchasePrice)
    }

    private fun calculateAvgTrend(): List<Pair<String, Double>> {
        val avgList = mutableListOf<Pair<String, Double>>()

        if (PurchaseStorage.purchaseList.isEmpty()) {
            return avgList
        }
        // save the date of the first purchase
        var startDate = LocalDate.of(
            PurchaseStorage.purchaseList.last().year!!,
            PurchaseStorage.purchaseList.last().month!!,
            PurchaseStorage.purchaseList.last().day!!
        )

        var priceSum = 0.0
        var purchaseCount = 0

        var lastCount = 0
        val lastDate = dateToString(
            PurchaseStorage.purchaseList.first().day!!,
            PurchaseStorage.purchaseList.first().month!!,
            PurchaseStorage.purchaseList.first().year!!
        )

        // purchaseList is inverted -> loop in reverse
        for (i in PurchaseStorage.purchaseList.size - 1 downTo 0) {
            PurchaseStorage.purchaseList[i].also { purchase ->
                // consider just the totals
                if (purchase.type == DbPurchases.TYPES.TOTAL.value) {
                    // increment sum and count
                    priceSum += purchase.price!!
                    purchaseCount++

                    // calculate the new date
                    val newDate = LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)

                    // if has passed a week, update
                    if (hasPassedAWeekBetween(startDate, newDate)) {
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

    private fun hasPassedAWeekBetween(startDate: LocalDate, endDate: LocalDate): Boolean {
        return ChronoUnit.DAYS.between(startDate, endDate) >= 7
    }

    fun setCollection(collection: String): LiveData<PurchaseResult> {
        return purchaseManager.updateListByCollection(collection)
    }

    fun getSelectedCollection(): String {
        return purchaseManager.getSelectedCollection()
    }

    fun setDynamicColorActive(active: Boolean) {
        purchaseManager.setDynamicColorActive(active)
    }

    fun getDynamicColorActive(): Boolean {
        return purchaseManager.getDynamicColorActive()
    }

    fun existLastYear(): Boolean {
        return PurchaseStorage.existLastYear
    }
}