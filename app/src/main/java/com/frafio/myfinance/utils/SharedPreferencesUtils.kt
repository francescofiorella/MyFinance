package com.frafio.myfinance.utils

import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication.Companion.DYNAMIC_COLOR_KEY

fun getSharedDynamicColor(sharedPreferences: SharedPreferences): Boolean {
    return sharedPreferences.getBoolean(DYNAMIC_COLOR_KEY, false)
}

fun setSharedDynamicColor(sharedPreferences: SharedPreferences, activate: Boolean) {
    sharedPreferences.edit().putBoolean(DYNAMIC_COLOR_KEY, activate).apply()
}