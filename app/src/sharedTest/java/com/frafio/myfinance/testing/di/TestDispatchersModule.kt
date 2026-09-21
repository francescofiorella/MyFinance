package com.frafio.myfinance.testing.di

import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.DispatchersModule
import com.frafio.myfinance.core.di.MyFinanceDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DispatchersModule::class])
object TestDispatchersModule {

    @Provides
    @Singleton
    fun providesTestDispatcher(): TestDispatcher = UnconfinedTestDispatcher()

    @Provides
    @Dispatcher(MyFinanceDispatchers.IO)
    fun providesIODispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

    @Provides
    @Dispatcher(MyFinanceDispatchers.Default)
    fun providesDefaultDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher
}
