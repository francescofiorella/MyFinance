package com.frafio.myfinance.app

import android.app.Application
import androidx.annotation.StringRes
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.frafio.myfinance.core.data.manager.AuthManager
import com.frafio.myfinance.core.data.manager.ExpensesManager
import com.frafio.myfinance.core.data.manager.IncomesManager
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyFinanceApplication : Application(), SingletonImageLoader.Factory {

    companion object {
        lateinit var instance: MyFinanceApplication private set
    }

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var expensesManager: ExpensesManager
    @Inject lateinit var incomesManager: IncomesManager

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return try {
            MyFinanceApplication.instance.getString(stringRes, *formatArgs)
        } catch (_: Exception) {
            ""
        }
    }
}
