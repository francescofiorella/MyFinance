package com.frafio.myfinance.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.MyFinanceApplication.Companion.COLLECTION_KEY
import com.frafio.myfinance.MyFinanceApplication.Companion.DYNAMIC_COLOR_KEY
import com.frafio.myfinance.data.enums.db.DbPurchases

fun getSharedCollection(sharedPreferences: SharedPreferences): String {
    return sharedPreferences.getString(COLLECTION_KEY, MyFinanceApplication.CURRENT_YEAR)
        ?: MyFinanceApplication.CURRENT_YEAR
}

@SuppressLint("ApplySharedPref")
fun setSharedCollection(sharedPreferences: SharedPreferences, collection: String) {
    sharedPreferences.edit().putString(COLLECTION_KEY, collection).commit()
}

fun getSharedDynamicColor(sharedPreferences: SharedPreferences): Boolean {
    return sharedPreferences.getBoolean(DYNAMIC_COLOR_KEY, false)
}

fun setSharedDynamicColor(sharedPreferences: SharedPreferences, activate: Boolean) {
    sharedPreferences.edit().putBoolean(DYNAMIC_COLOR_KEY, activate).apply()
}