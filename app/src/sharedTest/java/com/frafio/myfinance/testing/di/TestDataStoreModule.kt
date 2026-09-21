package com.frafio.myfinance.testing.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.frafio.myfinance.core.di.DataStoreModule
import com.frafio.myfinance.testing.util.InMemoryDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataStoreModule::class])
object TestDataStoreModule {

    @Provides
    @Singleton
    fun providesDataStore(): DataStore<Preferences> = InMemoryDataStore(emptyPreferences())
}
