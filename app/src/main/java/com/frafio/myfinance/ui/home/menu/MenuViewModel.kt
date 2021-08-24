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
    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val avgTrendList: List<Pair<String, Double>>
        get() = purchaseRepository.avgTrendList

    val avgTrendListSize: Int = avgTrendList.size
}