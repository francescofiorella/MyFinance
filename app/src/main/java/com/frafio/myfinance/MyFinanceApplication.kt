package com.frafio.myfinance

import android.app.Application
import android.content.SharedPreferences
import androidx.annotation.StringRes
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.frafio.myfinance.data.manager.AuthManager
import com.frafio.myfinance.data.manager.IncomesManager
import com.frafio.myfinance.data.manager.ExpensesManager
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyFinanceApplication : Application(), SingletonImageLoader.Factory {

    companion object {
        lateinit var instance: MyFinanceApplication private set
        const val PREFERENCES_KEY = "SHARED_PREFERENCES"
        const val DYNAMIC_COLOR_KEY = "DYNAMIC_COLOR_OPTIONS"
        const val MONTHLY_BUDGET_KEY = "MONTHLY_BUDGET_KEY"
        const val LABELS_KEY = "LABELS_KEY"
    }

    @Inject lateinit var sharedPreferences: SharedPreferences
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

        // if the user activated it, change the colors
        if (getSharedDynamicColor(sharedPreferences)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
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
