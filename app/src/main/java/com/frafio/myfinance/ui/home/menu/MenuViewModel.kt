package com.frafio.myfinance.ui.home.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.google.android.material.color.DynamicColors

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: MenuListener? = null

    var isSwitchCollectionChecked: Boolean = getCollectionCheck()

    var isDynamicColorAvailable: Boolean = DynamicColors.isDynamicColorAvailable()
    var isSwitchDynamicColorChecked: Boolean = getDynamicColorCheck()

    var isLastYearOk: Boolean = purchaseRepository.existLastYear()

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val avgTrendList: List<Pair<String, Double>>
        get() = purchaseRepository.avgTrendList

    val avgTrendListSize: Int = avgTrendList.size

    fun setCollection(isOldYear: Boolean) {
        listener?.onStarted()
        val response = purchaseRepository.setCollection(isOldYear)
        listener?.onCompleted(response)
    }

    private fun getCollectionCheck(): Boolean {
        return purchaseRepository.getSelectedCollection() == DbPurchases.COLLECTIONS.ZERO_ONE.value
    }

    fun setDynamicColor(active: Boolean) {
        purchaseRepository.setDynamicColorActive(active)
    }

    private fun getDynamicColorCheck(): Boolean {
        return purchaseRepository.getDynamicColorActive()
    }
}