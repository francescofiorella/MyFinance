package com.frafio.myfinance.features.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LabelsUiEvent {
    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : LabelsUiEvent

    data class LabelDeleted(
        val label: String,
        val message: String
    ) : LabelsUiEvent
}

@HiltViewModel
class LabelsViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val loadingRepository: LoadingRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val allLabels: StateFlow<List<String>> = userPreferencesRepository.userPreferencesFlow
        .map { it.labels }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiEvents = MutableSharedFlow<LabelsUiEvent>()
    val uiEvents: SharedFlow<LabelsUiEvent> = _uiEvents

    fun addLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val result = expensesRepository.addLabel(label)
                _uiEvents.emit(LabelsUiEvent.ShowSnackBar(result.message))
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun deleteLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val deleteResult = expensesRepository.deleteLabel(label)
                if (deleteResult.financeResult.code == com.frafio.myfinance.core.data.enums.db.FinanceCode.LABEL_DELETE_SUCCESS.code) {
                    _uiEvents.emit(
                        LabelsUiEvent.LabelDeleted(
                            label = label,
                            message = deleteResult.financeResult.message
                        )
                    )
                } else {
                    _uiEvents.emit(LabelsUiEvent.ShowSnackBar(deleteResult.financeResult.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun undoDeleteLabel(scope: CoroutineScope) {
        scope.launch {
            try {
                loadingRepository.startLoading()
                val result = expensesRepository.undoDeleteLabel()
                _uiEvents.emit(LabelsUiEvent.ShowSnackBar(result.message))
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun resetLastDeletedLabel() {
        expensesRepository.resetLastDeletedLabel()
    }

    fun editLabel(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val result = expensesRepository.editLabel(oldName, newName)
                _uiEvents.emit(LabelsUiEvent.ShowSnackBar(result.message))
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }
}
