package com.frafio.myfinance.ui.home.menu

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.getSharedLanguage
import com.frafio.myfinance.utils.setSharedLanguage

class MenuViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    var listener: MenuListener? = null

    var isSwitchChecked: Boolean = getChecked()

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

    private fun getChecked(): Boolean {
        return purchaseRepository.getSelectedCollection() == DbPurchases.COLLECTIONS.ZERO_ONE.value
    }

    fun getLanguage(): String {
        return getSharedLanguage(sharedPreferences)
    }

    fun setLanguage(language: String) {
        setSharedLanguage(sharedPreferences, language)
    }
}