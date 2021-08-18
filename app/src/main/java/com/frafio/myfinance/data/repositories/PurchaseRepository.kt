package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class PurchaseRepository(private val purchaseManager: PurchaseManager) {

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
        val dayAvg: Double
        val monthAvg: Double
        var todayTot = 0.0
        var tot = 0.0
        var ticketTot = 0.0
        var numTot = 0
        var trenTot = 0
        var amTot = 0

        var nDays = 0
        var nMonth = 0
        var lastMonth = 0
        var lastYear = 0

        PurchaseStorage.purchaseList.forEach { purchase ->
            // totale biglietti Amtab
            if (purchase.name == DbPurchases.NAMES.AMTAB.value) {
                amTot++
            }
            if (purchase.type == DbPurchases.TYPES.TOTAL.value) {
                // totale di oggi
                val year = LocalDate.now().year
                val month = LocalDate.now().monthValue
                val day = LocalDate.now().dayOfMonth
                if (purchase.year == year && purchase.month == month && purchase.day == day) {
                    todayTot = purchase.price ?: 0.0
                }

                // incrementa il totale
                tot += purchase.price ?: 0.0

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
            } else if (purchase.type != DbPurchases.TYPES.TICKET.value) {
                // totale acquisti (senza biglietti)
                numTot++
            } else {
                // totale biglietti
                ticketTot += purchase.price ?: 0.0

                // totale biglietti TrenItalia
                if (purchase.name == DbPurchases.NAMES.TRENITALIA.value) {
                    trenTot++
                }
            }
        }

        dayAvg = tot / nDays
        monthAvg = tot / nMonth

        val locale = Locale("en", "UK")
        val nf = NumberFormat.getInstance(locale)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("###,###,##0.00")

        val stats = mutableListOf<String>()
        stats.add("€ ${formatter.format(dayAvg)}")
        stats.add("€ ${formatter.format(monthAvg)}")
        stats.add("€ ${formatter.format(todayTot)}")
        stats.add("€ ${formatter.format(tot)}")
        stats.add(numTot.toString())
        stats.add("€ ${formatter.format(ticketTot)}")
        stats.add(trenTot.toString())
        stats.add(amTot.toString())

        return stats
    }

    fun deletePurchaseAt(position: Int): LiveData<Triple<PurchaseResult, Int?, Int?>> {
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
}