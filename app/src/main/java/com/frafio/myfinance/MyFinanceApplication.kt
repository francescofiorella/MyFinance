package com.frafio.myfinance

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.managers.InvoiceManager
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.google.android.material.color.DynamicColors

class MyFinanceApplication : Application() {

    companion object {
        const val PREFERENCES_KEY = "COLLECTION_PREFERENCES"
        const val COLLECTION_KEY = "COLLECTION_OPTIONS"
        const val DYNAMIC_COLOR_KEY = "DYNAMIC_COLOR_OPTIONS"
        val CURRENT_YEAR = DbPurchases.COLLECTIONS.THREE_FOUR.value
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var authManager: AuthManager
    lateinit var purchaseManager: PurchaseManager
    lateinit var invoiceManager: InvoiceManager

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        authManager = AuthManager(sharedPreferences)
        purchaseManager = PurchaseManager(sharedPreferences)
        invoiceManager = InvoiceManager(sharedPreferences)

        // if the user activated it, change the colors
        if (getSharedDynamicColor(sharedPreferences)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}