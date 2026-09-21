package com.frafio.myfinance.core.data.manager

import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.remote.RemoteDataSource
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomesSyncManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    database: MyFinanceDatabase,
    incomeDao: IncomeDao,
    remote: RemoteDataSource,
    @Dispatcher(MyFinanceDispatchers.IO) ioDispatcher: CoroutineDispatcher
) : BaseSyncManager<Income>(userPreferencesRepository, database, Income::class.java, remote, ioDispatcher) {

    override val collectionName: String = FirestoreEnums.FIELDS.INCOMES.value
    override val baseDao = incomeDao
    override val listUpdateSuccessCode = FinanceCode.INCOME_LIST_UPDATE_SUCCESS
    override val listUpdateFailureCode = FinanceCode.INCOME_LIST_UPDATE_FAILURE
    override val addSuccessCode = FinanceCode.INCOME_ADD_SUCCESS
    override val addFailureCode = FinanceCode.INCOME_ADD_FAILURE
    override val editSuccessCode = FinanceCode.INCOME_EDIT_SUCCESS
    override val editFailureCode = FinanceCode.INCOME_EDIT_FAILURE
    override val deleteSuccessCode = FinanceCode.INCOME_DELETE_SUCCESS
    override val deleteFailureCode = FinanceCode.INCOME_DELETE_FAILURE

    override suspend fun getLastSync(userPrefs: UserPreferencesData): Long = userPrefs.lastIncomesSync
    override suspend fun updateLastSync(timestamp: Long) = userPreferencesRepository.updateLastIncomesSync(timestamp)
    override suspend fun getLastAppSync(userPrefs: UserPreferencesData): Long = userPrefs.lastIncomesAppSync
    override suspend fun updateLastAppSync(timestamp: Long) = userPreferencesRepository.updateLastIncomesAppSync(timestamp)
}
