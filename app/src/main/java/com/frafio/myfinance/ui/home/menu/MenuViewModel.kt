package com.frafio.myfinance.ui.home.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.google.android.material.color.DynamicColors

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: MenuListener? = null

    var isDynamicColorAvailable: Boolean = DynamicColors.isDynamicColorAvailable()
    var isSwitchDynamicColorChecked: Boolean = getDynamicColorCheck()

    var isLastYearOk: Boolean = purchaseRepository.existLastYear()

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val avgTrendList: List<Pair<String, Double>>
        get() = purchaseRepository.avgTrendList

    val avgTrendListSize: Int = avgTrendList.size

    fun getCategories() {
        val response = purchaseRepository.getCategories()
        listener?.onCompleted(response)
    }

    fun setCollection(collection: String) {
        listener?.onStarted()
        val response = purchaseRepository.setCollection(collection)
        listener?.onCompleted(response)
    }

    fun getSelectedCollection(): String {
        return purchaseRepository.getSelectedCollection()
    }

    fun setDynamicColor(active: Boolean) {
        purchaseRepository.setDynamicColorActive(active)
    }

    private fun getDynamicColorCheck(): Boolean {
        return purchaseRepository.getDynamicColorActive()
    }
}