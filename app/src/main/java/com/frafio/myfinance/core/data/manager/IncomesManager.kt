package com.frafio.myfinance.core.data.manager

import android.util.Log
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomesManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val incomesLocalRepository: IncomesLocalRepository
) {

    companion object {
        private val TAG = IncomesManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 100
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private suspend fun getUserEmail(): String? {
        return userPreferencesRepository.userPreferencesFlow.first().user?.email
    }

    suspend fun updateIncomeList(): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.INCOME_LIST_UPDATE_FAILURE)
        return@withContext try {
            val queryDocumentSnapshots = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.INCOMES.value)
                .get().await()
            val incomeList = mutableListOf<Income>()
            queryDocumentSnapshots.forEach { document ->
                val income = document.toObject(Income::class.java)
                income.id = document.id
                incomeList.add(income)
            }
            incomesLocalRepository.updateTable(incomeList)
            FinanceResult(FinanceCode.INCOME_LIST_UPDATE_SUCCESS)
        } catch (e: Exception) {
            val error = "Error! ${e.localizedMessage}"
            Log.e(TAG, error)
            FinanceResult(FinanceCode.INCOME_LIST_UPDATE_FAILURE)
        }
    }

    suspend fun addIncome(income: Income): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.INCOME_ADD_FAILURE)
        return@withContext try {
            val documentReference = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.INCOMES.value)
                .add(income).await()
            income.id = documentReference.id
            incomesLocalRepository.insertIncome(income)
            FinanceResult(FinanceCode.INCOME_ADD_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.INCOME_ADD_FAILURE)
        }
    }

    suspend fun editIncome(income: Income): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.INCOME_EDIT_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.INCOMES.value)
                .document(income.id).set(income).await()
            incomesLocalRepository.updateIncome(income)
            FinanceResult(FinanceCode.INCOME_EDIT_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.INCOME_EDIT_FAILURE)
        }
    }

    suspend fun deleteIncome(income: Income): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.INCOME_DELETE_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.INCOMES.value)
                .document(income.id).delete().await()
            incomesLocalRepository.deleteIncome(income)
            FinanceResult(FinanceCode.INCOME_DELETE_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.INCOME_DELETE_FAILURE)
        }
    }
}
