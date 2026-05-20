package com.frafio.myfinance.ui.home.expenses

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.widget.DatePickerRangeDialog
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.features.home.expenses.CategorySheet
import com.frafio.myfinance.ui.components.EditTransactionSheet
import com.frafio.myfinance.ui.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.ui.features.home.expenses.LabelsSheet
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ExpensesFragment : BaseFragment(), ExpensesListener {

    private val viewModel by viewModels<ExpensesViewModel>()
    private lateinit var datePickerRangeDialog: DatePickerRangeDialog
    private var pendingScrollId: String? = null

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data?.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_EXPENSE_CODE) {
                (activity as HomeActivity).showSnackBar(FinanceCode.EXPENSE_EDIT_SUCCESS.message)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.listener = this

        if (pendingScrollId != null) {
            viewModel.scrollToId(pendingScrollId!!)
            pendingScrollId = null
        } else if (savedInstanceState == null) {
            scrollUp()
        }

        datePickerRangeDialog = object : DatePickerRangeDialog(
            requireActivity()
        ) {
            override fun onStart() {
                super.onStart()
                requireActivity().hideSoftKeyboard(requireView())
            }

            override fun onPositiveBtnClickListener() {
                super.onPositiveBtnClickListener()
                viewModel.onDateFilterChanged(Pair(startDate!!, endDate!!))
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    val selectedCategories by viewModel.selectedCategories.collectAsState()
                    ExpensesScreen(
                        viewModel = viewModel,
                        onSelectCategory = {
                            showCategorySelectionSheet(
                                disabledCategories = selectedCategories,
                                onCategorySelected = { viewModel.onCategoryFilterChanged(it) }
                            )
                        },
                        onSelectLabel = {
                            showLabelsSheet(
                                showNewLabel = false,
                                onLabelCheckedChanged = { _, label, active ->
                                    viewModel.onLabelFilterChanged(label, active)
                                }
                            )
                        },
                        onSelectDateRange = {
                            datePickerRangeDialog.show()
                        },
                        onItemLongClick = { expense, position ->
                            showEditExpenseSheet(expense, position)
                        },
                        onCategoryClick = { expense, _ ->
                            showCategorySelectionSheet(expense = expense) {
                                viewModel.updateCategory(expense, it)
                            }
                        },
                        getDateLabel = { start, end -> getDateChipLabel(start, end) }
                    )
                }
            }
        }
    }

    fun scrollToId(id: String) {
        if (isAdded) {
            viewModel.scrollToId(id)
        } else {
            pendingScrollId = id
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        val today = LocalDate.now()
        val todayId = "total_${today.dayOfMonth}_${today.monthValue}_${today.year}"
        viewModel.scrollToId(todayId)
    }

    private fun showEditExpenseSheet(expense: Expense, position: Int) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getEditExpenseSheetDialogComposeView(
            expense,
            position,
            sheetDialog::hide
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun showCategorySelectionSheet(
        expense: Expense? = null,
        disabledCategories: List<Int> = emptyList(),
        onCategorySelected: (Int) -> Unit
    ) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getCategorySheetDialogComposeView(
            expense = expense,
            disabledCategories = disabledCategories,
            onDismiss = sheetDialog::hide,
            onCategorySelected = {
                onCategorySelected(it)
                sheetDialog.hide()
            }
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun showLabelsSheet(
        expense: Expense? = null,
        showNewLabel: Boolean = true,
        onLabelCheckedChanged: (Expense?, String, Boolean) -> Unit
    ) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getLabelsSheetDialogComposeView(
            expenseId = expense?.id,
            showNewLabel = showNewLabel,
            onNewLabel = viewModel::addLabel,
            onLabelCheckedChanged = onLabelCheckedChanged,
            onEditLabel = { oldLabel, newLabel ->
                viewModel.editLabel(oldLabel, newLabel)
            },
            onDeleteLabel = { label ->
                viewModel.deleteLabel(label)
            }
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun getDateChipLabel(startDate: LocalDate, endDate: LocalDate): String {
        return when (startDate.year) {
            endDate.year if startDate.monthValue == endDate.monthValue -> {
                val startDayOfMonth =
                    if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                "$startDayOfMonth - ${dateToExtendedString(endDate)}"
            }

            endDate.year -> {
                val startDayOfMonth =
                    if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                val startMonth =
                    startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .replaceFirstChar { it.uppercase() }
                "$startDayOfMonth $startMonth - ${dateToExtendedString(endDate)}"
            }

            else -> "${dateToExtendedString(startDate)} - ${dateToExtendedString(endDate)}"
        }
    }

    override fun onCompleted(response: LiveData<FinanceResult>) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS.code -> {}
                FinanceCode.EXPENSE_EDIT_SUCCESS.code -> {}
                FinanceCode.EXPENSE_ADD_SUCCESS.code -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }

                else -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }
            }
        }
    }

    override fun onDeleteCompleted(
        response: LiveData<FinanceResult>,
        expense: Expense
    ) {
        response.observe(viewLifecycleOwner) { result ->
            if (result.code == FinanceCode.EXPENSE_DELETE_SUCCESS.code) {
                (activity as HomeActivity).showSnackBar(
                    result.message,
                    getString(R.string.cancel)
                ) {
                    viewModel.addExpense(expense)
                }
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
        }
    }

    override fun onDeleteCompleted(
        response: LiveData<FinanceResult>,
        label: String
    ) {
        response.observe(viewLifecycleOwner) { result ->
            if (result.code == FinanceCode.LABEL_DELETE_SUCCESS.code) {
                (activity as HomeActivity).showSnackBar(
                    message = result.message,
                    actionText = getString(R.string.cancel),
                    actionFun = { viewModel.undoDeleteLabel() }
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if (event != DISMISS_EVENT_ACTION) {
                            viewModel.resetLastDeletedLabel()
                        }
                    }
                })
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
        }
    }

    private fun getEditExpenseSheetDialogComposeView(
        expense: Expense,
        position: Int,
        onDismiss: () -> Unit
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    EditTransactionSheet(
                        transaction = expense,
                        onDismiss = onDismiss,
                        onLabels = {
                            showLabelsSheet(expense = expense) { updatedExpense, label, checked ->
                                updatedExpense?.let {
                                    if (checked) {
                                        viewModel.addLabelToExpense(it, label)
                                    } else {
                                        viewModel.removeLabelFromExpense(it, label)
                                    }
                                }
                            }
                        },
                        onEdit = {
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra(
                                    AddActivity.REQUEST_CODE_KEY,
                                    AddActivity.REQUEST_EDIT_CODE
                                )
                                it.putExtra(
                                    AddActivity.EXPENSE_REQUEST_KEY,
                                    AddActivity.REQUEST_EXPENSE_CODE
                                )
                                it.putExtra(AddActivity.EXTRA_TRANSACTION, expense)
                                it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                                editResultLauncher.launch(it)
                            }
                            onDismiss()
                        },
                        onDelete = {
                            viewModel.deleteExpense(expense)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }

    private fun getCategorySheetDialogComposeView(
        expense: Expense? = null,
        disabledCategories: List<Int> = listOf(),
        onDismiss: () -> Unit,
        onCategorySelected: (Int) -> Unit
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    CategorySheet(
                        expense = expense,
                        disabledCategories = disabledCategories,
                        onCategorySelected = onCategorySelected,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }

    private fun getLabelsSheetDialogComposeView(
        expenseId: String?,
        showNewLabel: Boolean,
        onNewLabel: (String) -> Unit,
        onLabelCheckedChanged: (Expense?, String, Boolean) -> Unit,
        onDeleteLabel: (String) -> Unit = {},
        onEditLabel: (String, String) -> Unit = { _, _ -> }
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    val labels by viewModel.labels.collectAsState()
                    val selectedLabels by viewModel.selectedLabels.collectAsState()
                    val expenses by viewModel.expenses.collectAsState()
                    val currentExpense = if (expenseId != null)
                        expenses.find { it.id == expenseId }
                    else
                        null
                    LabelsSheet(
                        expense = currentExpense,
                        labels = labels,
                        selectedLabels = selectedLabels,
                        showEditLabel = showNewLabel,
                        onNewLabel = onNewLabel,
                        onLabelCheckedChanged = { label, checked ->
                            onLabelCheckedChanged(currentExpense, label, checked)
                        },
                        onDeleteLabel = onDeleteLabel,
                        onEditLabel = onEditLabel
                    )
                }
            }
        }
    }
}
