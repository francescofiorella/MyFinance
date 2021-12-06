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
import com.frafio.myfinance.utils.doubleToStringWithoutDecimals
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PurchaseRepository(private val purchaseManager: PurchaseManager) {

    val avgTrendList: List<Pair<String, Double>>
        get() = calculateAvgTrend()

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
        6: spesaTot
        7: ticketTot
        */

        val values = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        var nDays = 0
        var nMonth = 0
        var lastMonth = 0
        var lastYear = 0

        PurchaseStorage.purchaseList.forEach { purchase ->
            when (purchase.type) {
                DbPurchases.TYPES.TOTAL.value -> {
                    // totale di oggi
                    val year = LocalDate.now().year
                    val month = LocalDate.now().monthValue
                    val day = LocalDate.now().dayOfMonth
                    if (purchase.year == year && purchase.month == month) {
                        values[4] += purchase.price ?: 0.0

                        if (purchase.day == day) {
                            values[2] = purchase.price ?: 0.0
                        }
                    }

                    // incrementa il totale
                    values[3] += purchase.price ?: 0.0

                    // conta i giorni
                    nDays++

                    // conta i mesi
                    if (purchase.year != lastYear) {
                        lastYear = purchase.year ?: 0
                        lastMonth = purchase.month ?: 0
                        nMonth++
                    } else if (purchase.month != lastMonth) {
                        lastMonth = purchase.month ?: 0
                        nMonth++
                    }
                }

                DbPurchases.TYPES.TRANSPORT.value -> {
                    // totale biglietti
                    values[7] += purchase.price ?: 0.0
                }

                DbPurchases.TYPES.RENT.value -> {
                    // totale affitto
                    values[5] += purchase.price ?: 0.0
                }

                DbPurchases.TYPES.SHOPPING.value -> {
                    // totale spesa
                    values[6] += purchase.price ?: 0.0
                }
            }
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

    fun addTotale(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addTotale(purchase)
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
        // salva la data del primo acquisto
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

        // purchaseList è invertita -> cicla al contrario
        for (i in PurchaseStorage.purchaseList.size - 1 downTo 0) {
            PurchaseStorage.purchaseList[i].also { purchase ->
                // considera solo i totali
                if (purchase.type == DbPurchases.TYPES.TOTAL.value) {
                    // incrementa sum e count
                    priceSum += purchase.price!!
                    purchaseCount++

                    // calcola nuova data
                    val newDate = LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)

                    // se è passata una settimana, aggiorna
                    if (hasPassedAWeekBetween(startDate, newDate)) {
                        // memorizza il punto in cui effettuo il calcolo
                        lastCount = purchaseCount

                        startDate = newDate

                        // calcola la nuova media
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
        // se ci sono acquisti rimanenti, aggiungili
        if (lastCount != purchaseCount) {
            val newValue: Double = priceSum / purchaseCount
            val element = Pair(lastDate!!, newValue)
            avgList.add(element)
        }

        return avgList
    }

    private fun hasPassedAWeekBetween(startDate: LocalDate, endDate: LocalDate): Boolean {
        return ChronoUnit.DAYS.between(startDate, endDate) > 7
    }

    fun setCollection(isOldYear: Boolean): LiveData<PurchaseResult> {
        return purchaseManager.updateListByCollection(isOldYear)
    }

    fun getSelectedCollection(): String {
        return purchaseManager.getSelectedCollection()
    }

    fun existLastYear(): Boolean {
        return PurchaseStorage.existLastYear
    }
}