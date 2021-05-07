package com.frafio.myfinance.data.repositories

import android.view.View
import com.frafio.myfinance.data.manager.PurchaseManager
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class StatsRepository {

    fun calculateStats() : List<String> {
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

        PurchaseManager.getPurchaseList().forEach { purchasePair ->
            val purchase = purchasePair.second
            // totale biglietti Amtab
            if (purchase.name == "Biglietto Amtab") {
                amTot++
            }
            if (purchase.type == 0) {
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
            } else if (purchase.type != 3) {
                // totale acquisti (senza biglietti)
                numTot++
            } else {
                // totale biglietti
                ticketTot += purchase.price ?: 0.0

                // totale biglietti TrenItalia
                if (purchase.name == "Biglietto TrenItalia") {
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

    fun dashboardWarningVisibility() : Int {
        return if (PurchaseManager.getPurchaseList().isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun dashboardStatsVisibility() : Int {
        return if (PurchaseManager.getPurchaseList().isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}