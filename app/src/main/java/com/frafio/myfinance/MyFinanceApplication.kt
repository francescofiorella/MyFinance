package com.frafio.myfinance

import android.app.Application
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.auth.AuthViewModelFactory
import com.frafio.myfinance.ui.home.menu.MenuViewModelFactory
import com.frafio.myfinance.ui.home.profile.ProfileViewModelFactory
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
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { ProfileViewModelFactory(instance()) }
        bind() from provider { MenuViewModelFactory(instance()) }
    }
}