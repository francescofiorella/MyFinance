package com.frafio.myfinance.utils

import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication.Companion.DYNAMIC_COLOR_KEY
import com.frafio.myfinance.MyFinanceApplication.Companion.MONTHLY_BUDGET_KEY
import androidx.core.content.edit

fun getSharedDynamicColor(sharedPreferences: SharedPreferences): Boolean {
    return sharedPreferences.getBoolean(DYNAMIC_COLOR_KEY, false)
}

fun setSharedDynamicColor(sharedPreferences: SharedPreferences, activate: Boolean) {
    sharedPreferences.edit { putBoolean(DYNAMIC_COLOR_KEY, activate) }
}

fun getSharedMonthlyBudget(sharedPreferences: SharedPreferences): Double {
    return sharedPreferences.getFloat(MONTHLY_BUDGET_KEY, 0.0F).toDouble()
}

fun setSharedMonthlyBudget(sharedPreferences: SharedPreferences, value: Double) {
    sharedPreferences.edit { putFloat(MONTHLY_BUDGET_KEY, value.toFloat()) }
}