package com.frafio.myfinance

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.google.android.material.color.DynamicColors

class MyFinanceApplication : Application() {

    companion object {
        lateinit var instance: MyFinanceApplication private set
        const val PREFERENCES_KEY = "COLLECTION_PREFERENCES"
        const val COLLECTION_KEY = "COLLECTION_OPTIONS"
        const val DYNAMIC_COLOR_KEY = "DYNAMIC_COLOR_OPTIONS"
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var authManager: AuthManager
    lateinit var purchaseManager: PurchaseManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        authManager = AuthManager(sharedPreferences)
        purchaseManager = PurchaseManager(sharedPreferences)

        // if the user activated it, change the colors
        if (getSharedDynamicColor(sharedPreferences)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}

object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return try {
            MyFinanceApplication.instance.getString(stringRes, *formatArgs)
        } catch (e: UninitializedPropertyAccessException) {
            ""
        }
    }
}