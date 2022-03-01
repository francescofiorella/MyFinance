package com.frafio.myfinance

import android.app.Application
import android.content.Context
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.managers.InvoiceManager
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.ReceiptRepository
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.add.AddViewModelFactory
import com.frafio.myfinance.ui.auth.AuthViewModelFactory
import com.frafio.myfinance.ui.home.HomeViewModelFactory
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModelFactory
import com.frafio.myfinance.ui.home.list.ListViewModelFactory
import com.frafio.myfinance.ui.home.list.invoice.InvoiceViewModelFactory
import com.frafio.myfinance.ui.home.menu.MenuViewModelFactory
import com.frafio.myfinance.ui.home.profile.ProfileViewModelFactory
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.google.android.material.color.DynamicColors
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class MyFinanceApplication : Application(), KodeinAware {

    companion object {
        const val PREFERENCES_KEY = "COLLECTION_PREFERENCES"
        const val COLLECTION_KEY = "COLLECTION_OPTIONS"
        const val DYNAMIC_COLOR_KEY = "DYNAMIC_COLOR_OPTIONS"
    }

    private val sharedPreferences by lazy {
        getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@MyFinanceApplication))

        // managers
        bind() from singleton { AuthManager(sharedPreferences) }
        bind() from singleton { PurchaseManager(sharedPreferences) }
        bind() from singleton { InvoiceManager(sharedPreferences) }

        // repositories
        bind() from singleton { UserRepository(instance()) }
        bind() from singleton { PurchaseRepository(instance()) }
        bind() from singleton { ReceiptRepository(instance()) }

        // viewModelFactories
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }
        bind() from provider { DashboardViewModelFactory(instance()) }
        bind() from provider { ListViewModelFactory(instance()) }
        bind() from provider { ProfileViewModelFactory(instance()) }
        bind() from provider {
            MenuViewModelFactory(instance())
        }
        bind() from provider { InvoiceViewModelFactory(instance()) }
        bind() from provider { AddViewModelFactory(instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        // if the user activated it, change the colors
        if (getSharedDynamicColor(sharedPreferences)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}