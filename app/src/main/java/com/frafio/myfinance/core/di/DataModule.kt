package com.frafio.myfinance.core.di

import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepositoryImpl
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import com.frafio.myfinance.core.data.repository.ExpensesRepositoryImpl
import com.frafio.myfinance.core.data.repository.IncomeRepository
import com.frafio.myfinance.core.data.repository.IncomeRepositoryImpl
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepositoryImpl
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepositoryImpl
import com.frafio.myfinance.core.data.repository.UserRepository
import com.frafio.myfinance.core.data.repository.UserRepositoryImpl
import com.frafio.myfinance.core.data.remote.AuthDataSource
import com.frafio.myfinance.core.data.remote.FirebaseAuthDataSource
import com.frafio.myfinance.core.data.remote.FirestoreRemoteDataSource
import com.frafio.myfinance.core.data.remote.RemoteDataSource
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import com.frafio.myfinance.core.data.storage.ProfileImageStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsExpensesRepository(impl: ExpensesRepositoryImpl): ExpensesRepository

    @Binds
    abstract fun bindsIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    abstract fun bindsExpensesLocalRepository(impl: ExpensesLocalRepositoryImpl): ExpensesLocalRepository

    @Binds
    abstract fun bindsIncomesLocalRepository(impl: IncomesLocalRepositoryImpl): IncomesLocalRepository

    @Binds
    abstract fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindsUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindsProfileImageStorage(impl: ProfileImageStorageImpl): ProfileImageStorage

    @Binds
    abstract fun bindsRemoteDataSource(impl: FirestoreRemoteDataSource): RemoteDataSource

    @Binds
    abstract fun bindsAuthDataSource(impl: FirebaseAuthDataSource): AuthDataSource
}
