package com.frafio.myfinance

import android.app.Application
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.managers.ReceiptManager
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.ReceiptRepository
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.add.AddViewModelFactory
import com.frafio.myfinance.ui.auth.AuthViewModelFactory
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModelFactory
import com.frafio.myfinance.ui.home.list.ListViewModelFactory
import com.frafio.myfinance.ui.home.list.receipt.ReceiptViewModelFactory
import com.frafio.myfinance.ui.home.menu.MenuViewModelFactory
import com.frafio.myfinance.ui.home.profile.ProfileViewModelFactory
import com.frafio.myfinance.ui.splash.SplashScreenViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class MyFinanceApplication : Application(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@MyFinanceApplication))

        // managers
        bind() from singleton { AuthManager() }
        bind() from singleton { PurchaseManager() }
        bind() from singleton { ReceiptManager() }

        // repositories
        bind() from singleton { UserRepository(instance()) }
        bind() from singleton { PurchaseRepository(instance()) }
        bind() from singleton { ReceiptRepository(instance()) }

        // viewModelFactories
        bind() from provider { SplashScreenViewModelFactory(instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { DashboardViewModelFactory(instance(), instance()) }
        bind() from provider { ListViewModelFactory(instance(), instance()) }
        bind() from provider { ProfileViewModelFactory(instance()) }
        bind() from provider { MenuViewModelFactory(instance(), instance()) }
        bind() from provider { ReceiptViewModelFactory(instance()) }
        bind() from provider { AddViewModelFactory(instance(), instance()) }
    }
}