package com.frafio.myfinance.di

import android.content.Context
import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(MyFinanceApplication.PREFERENCES_KEY, Context.MODE_PRIVATE)
    }
}
