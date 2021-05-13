package com.frafio.myfinance

import android.app.Application
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

        bind() from singleton { UserRepository() }
        bind() from singleton { PurchaseRepository() }
        bind() from singleton { ReceiptRepository() }
        bind() from provider { SplashScreenViewModelFactory(instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance(), instance()) }
        bind() from provider { DashboardViewModelFactory(instance()) }
        bind() from provider { ListViewModelFactory(instance()) }
        bind() from provider { ProfileViewModelFactory(instance()) }
        bind() from provider { MenuViewModelFactory(instance()) }
        bind() from provider { ReceiptViewModelFactory(instance()) }
        bind() from provider { AddViewModelFactory(instance(), instance()) }
    }
}