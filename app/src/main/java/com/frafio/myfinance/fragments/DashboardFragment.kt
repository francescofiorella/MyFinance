package com.frafio.myfinance.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.frafio.myfinance.ui.MainActivity
import com.frafio.myfinance.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class DashboardFragment : Fragment() {

    lateinit var mStatsLayout: ConstraintLayout
    lateinit var mWarningTV: TextView
    lateinit var mDayAvgTV: TextView
    lateinit var mMonthAvgTV: TextView
    lateinit var mTodayTotTV: TextView
    lateinit var mTotTV:TextView
    lateinit var mNumTotTV:TextView
    lateinit var mTicketTotTV:TextView
    lateinit var mTrenTotTV:TextView
    lateinit var mAmTotTV:TextView

    var dayAvg = 0.0
    var monthAvg = 0.0
    var todayTot = 0.0
    var tot = 0.0
    var ticketTot = 0.0
    var numTot = 0
    var trenTot = 0
    var amTot = 0

    companion object {
        private val TAG = DashboardFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        mStatsLayout = view.findViewById(R.id.stats_layout)
        mWarningTV = view.findViewById(R.id.dashboard_warningTV)
        mDayAvgTV = view.findViewById(R.id.dayAvg_TV)
        mMonthAvgTV = view.findViewById(R.id.monthAvg_TV)
        mTodayTotTV = view.findViewById(R.id.todayTot_TV)
        mTotTV = view.findViewById(R.id.tot_TV)
        mNumTotTV = view.findViewById(R.id.numTot_TV)
        mTicketTotTV = view.findViewById(R.id.ticketTot_TV)
        mTrenTotTV = view.findViewById(R.id.trenTot_TV)
        mAmTotTV = view.findViewById(R.id.amTot_TV)

        if (MainActivity.PURCHASE_LIST.isEmpty()) {
            mWarningTV.visibility = View.VISIBLE
            mStatsLayout.visibility = View.GONE
        } else {
            mWarningTV.visibility = View.GONE
            mStatsLayout.visibility = View.VISIBLE
        }

        if (savedInstanceState == null) {
            calculateStats()
        }

        return view
    }

    private fun calculateStats() {
        var nDays = 0
        var nMonth = 0
        var lastMonth = 0
        var lastYear = 0
        for (purchase in MainActivity.PURCHASE_LIST) {
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
        setViews()
    }

    private fun setViews() {
        val locale = Locale("en", "UK")
        val nf = NumberFormat.getInstance(locale)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("###,###,##0.00")
        mDayAvgTV.text = "€ ${formatter.format(dayAvg)}"
        mMonthAvgTV.text = "€ ${formatter.format(monthAvg)}"
        mTodayTotTV.text = "€ ${formatter.format(todayTot)}"
        mTotTV.text = "€ ${formatter.format(tot)}"
        mNumTotTV.text = numTot.toString()
        mTicketTotTV.text = "€ ${formatter.format(ticketTot)}"
        mTrenTotTV.text = trenTot.toString()
        mAmTotTV.text = amTot.toString()
    }
}