package com.frafio.myfinance.ui.add

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.PurchaseResult

interface AddListener {

    fun onAddStart()

    fun onAddSuccess(response: LiveData<PurchaseResult>)

    fun onAddFailure(result: PurchaseResult)

}