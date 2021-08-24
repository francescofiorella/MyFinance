package com.frafio.myfinance.ui.home.menu

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.R
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.round
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries

class MenuViewModel(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {
    companion object {
        private const val CHART_COLOR_ALPHA: Int = 150
    }

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    fun getChartColor(context: Context): Int {
        return ColorUtils.setAlphaComponent(
            ContextCompat.getColor(context, R.color.accent),
            CHART_COLOR_ALPHA
        )
    }

    fun addCharPointsTo(series: ValueLineSeries) {
        purchaseRepository.avgTrendList.forEach { pair ->
            val date: String = pair.first
            // converti to Float e arrotonda alla seconda cifra decimale
            val value: Float = pair.second.toFloat().round(2)
            series.addPoint(ValueLinePoint(date, value))
        }
    }
}