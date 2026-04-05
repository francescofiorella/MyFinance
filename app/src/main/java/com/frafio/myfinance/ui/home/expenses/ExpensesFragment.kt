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
import com.frafio.myfinance.ui.features.home.expenses.CategorySheetDialog
import com.frafio.myfinance.ui.features.home.EditTransactionSheet
import com.frafio.myfinance.ui.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.ui.features.home.expenses.FilterExpensesSheet
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog
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
                    ExpensesScreen(
                        viewModel = viewModel,
                        onFilterClick = { showFilterSheet() },
                        onItemLongClick = { expense, position ->
                            showEditExpenseSheet(expense, position)
                        },
                        onCategoryClick = { expense, _ ->
                            showCategorySelectionSheet(expense)
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

    private fun showFilterSheet() {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getFilterExpensesSheetDialogComposeView(sheetDialog::hide)
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
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

    private fun showCategorySelectionSheet(expense: Expense) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getCategorySheetDialogComposeView(
            expense = expense,
            onDismiss = sheetDialog::hide,
            onCategorySelected = {
                viewModel.updateCategory(expense, it)
                sheetDialog.hide()
            }
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun getDateChipLabel(startDate: LocalDate, endDate: LocalDate): String {
        return when (startDate.year) {
            endDate.year if startDate.monthValue == endDate.monthValue -> {
                val startDayOfMonth = if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                "$startDayOfMonth - ${dateToExtendedString(endDate)}"
            }
            endDate.year -> {
                val startDayOfMonth = if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                val startMonth = startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replaceFirstChar { it.uppercase() }
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
                        onEdit = {
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                                it.putExtra(AddActivity.EXPENSE_REQUEST_KEY, AddActivity.REQUEST_EXPENSE_CODE)
                                it.putExtra(AddActivity.EXPENSE_ID_KEY, expense.id)
                                it.putExtra(AddActivity.EXPENSE_NAME_KEY, expense.name)
                                it.putExtra(AddActivity.EXPENSE_PRICE_KEY, expense.price)
                                it.putExtra(AddActivity.EXPENSE_CATEGORY_KEY, expense.category)
                                it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                                it.putExtra(AddActivity.EXPENSE_YEAR_KEY, expense.year)
                                it.putExtra(AddActivity.EXPENSE_MONTH_KEY, expense.month)
                                it.putExtra(AddActivity.EXPENSE_DAY_KEY, expense.day)
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

    private fun getFilterExpensesSheetDialogComposeView(
        onDismiss: () -> Unit
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    val selectedCategories by viewModel.selectedCategories.collectAsState()
                    val dateRange by viewModel.dateRange.collectAsState()
                    FilterExpensesSheet(
                        onDismiss = onDismiss,
                        categoryEnabled = selectedCategories.size != 9,
                        dateRangeEnabled = dateRange == null,
                        onSelectCategory = {
                            onDismiss()
                            val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
                                SideSheetDialog(requireContext())
                            } else {
                                BottomSheetDialog(requireContext())
                            }
                            val composeView = getCategorySheetDialogComposeView(
                                disabledCategories = selectedCategories,
                                onDismiss = sheetDialog::hide,
                                onCategorySelected = {
                                    viewModel.onCategoryFilterChanged(it)
                                }
                            )
                            sheetDialog.setContentView(composeView)
                            sheetDialog.show()
                        },
                        onSelectDateRange = {
                            onDismiss()
                            datePickerRangeDialog.show()
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
                    CategorySheetDialog(
                        expense = expense,
                        disabledCategories = disabledCategories,
                        onCategorySelected = onCategorySelected,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}
