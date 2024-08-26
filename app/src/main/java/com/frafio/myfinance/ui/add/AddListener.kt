package com.frafio.myfinance.ui.add

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.FinanceResult

interface AddListener {

    fun onAddStart()

    fun onAddSuccess(response: LiveData<FinanceResult>)

    fun onAddFailure(financeResult: FinanceResult)

}