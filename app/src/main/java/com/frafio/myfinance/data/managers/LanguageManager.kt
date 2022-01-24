package com.frafio.myfinance.data.managers

import java.util.*

object LanguageManager {
    fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }
}