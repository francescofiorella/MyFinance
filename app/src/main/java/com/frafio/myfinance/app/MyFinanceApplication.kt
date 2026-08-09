package com.frafio.myfinance.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyFinanceApplication : Application(), SingletonImageLoader.Factory {

    companion object {
        lateinit var instance: MyFinanceApplication private set
    }

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

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
