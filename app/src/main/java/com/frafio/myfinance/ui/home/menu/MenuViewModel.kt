package com.frafio.myfinance.ui.home.menu

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.repositories.PurchaseRepository

class MenuViewModel(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {
    var listener: MenuListener? = null

    var isSwitchChecked: Boolean = getChecked()

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val avgTrendList: List<Pair<String, Double>>
        get() = purchaseRepository.avgTrendList

    val avgTrendListSize: Int = avgTrendList.size

    fun setCollection(isOldYear: Boolean) {
        listener?.onStarted()
        val response = purchaseRepository.setCollection(isOldYear)
        listener?.onCompleted(response)
    }

    private fun getChecked() : Boolean {
        return purchaseRepository.getSelectedCollection() == DbPurchases.COLLECTIONS.ZERO_UNO.value
    }
}