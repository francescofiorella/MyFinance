package com.frafio.myfinance.testing.di

import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepositoryImpl
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import com.frafio.myfinance.core.data.repository.IncomeRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepositoryImpl
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepositoryImpl
import com.frafio.myfinance.core.data.repository.UserRepository
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import com.frafio.myfinance.core.di.DataModule
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Remote repositories and image storage are fakes, so nothing reaches Firebase; the local
 * repositories and preferences are the real implementations over the in-memory Room and DataStore
 * from [TestDatabaseModule] and [TestDataStoreModule]. Tests inject the concrete fakes to seed them.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataModule::class])
abstract class TestDataModule {

    @Binds
    abstract fun bindsExpensesLocalRepository(impl: ExpensesLocalRepositoryImpl): ExpensesLocalRepository

    @Binds
    abstract fun bindsIncomesLocalRepository(impl: IncomesLocalRepositoryImpl): IncomesLocalRepository

    @Binds
    abstract fun bindsUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindsUserRepository(fake: TestUserRepository): UserRepository

    @Binds
    abstract fun bindsExpensesRepository(fake: TestExpensesRepository): ExpensesRepository

    @Binds
    abstract fun bindsIncomeRepository(fake: TestIncomeRepository): IncomeRepository

    @Binds
    abstract fun bindsProfileImageStorage(fake: TestProfileImageStorage): ProfileImageStorage

    companion object {
        @Provides
        @Singleton
        fun providesTestUserRepository() = TestUserRepository()

        @Provides
        @Singleton
        fun providesTestExpensesRepository() = TestExpensesRepository()

        @Provides
        @Singleton
        fun providesTestIncomeRepository() = TestIncomeRepository()

        @Provides
        @Singleton
        fun providesTestProfileImageStorage() = TestProfileImageStorage()
    }
}
