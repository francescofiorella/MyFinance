package com.frafio.myfinance.app

import android.app.Application
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyFinanceApplication : Application() {

    companion object {
        lateinit var instance: MyFinanceApplication private set
    }

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
