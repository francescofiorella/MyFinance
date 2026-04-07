package com.frafio.myfinance.ui.home.budget

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.components.EditTransactionSheet
import com.frafio.myfinance.ui.features.home.budget.BudgetScreen
import com.frafio.myfinance.ui.features.home.budget.EditBudgetSheet
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog

class BudgetFragment : BaseFragment(), BudgetListener {
    private val viewModel by viewModels<BudgetViewModel>()
    private var pendingScrollId: String? = null

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data?.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_INCOME_CODE) {
                (activity as HomeActivity).showSnackBar(FinanceCode.INCOME_EDIT_SUCCESS.message)
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

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    BudgetScreen(
                        viewModel = viewModel,
                        onItemLongClick = { income, position ->
                            showEditIncomeSheet(income, position)
                        },
                        onEditBudgetClick = { budget ->
                            showEditBudgetSheet(budget)
                        }
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
        viewModel.scrollToId(null)
    }

    private fun showEditBudgetSheet(budget: Double) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getEditBudgetSheetDialogComposeView(
            budget,
            sheetDialog::hide
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun getEditBudgetSheetDialogComposeView(
        budget: Double,
        onDismiss: () -> Unit
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    EditBudgetSheet(
                        budget = budget,
                        onDismiss = onDismiss,
                        onEditBudget = { viewModel.setMonthlyBudget(it, true) }
                    )
                }
            }
        }
    }

    private fun showEditIncomeSheet(income: Income, position: Int) {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getEditIncomeSheetDialogComposeView(
            income,
            position,
            sheetDialog::hide
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun getEditIncomeSheetDialogComposeView(
        income: Income,
        position: Int,
        onDismiss: () -> Unit
    ): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    EditTransactionSheet(
                        transaction = income,
                        onDismiss = onDismiss,
                        onEdit = {
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                                it.putExtra(AddActivity.EXPENSE_REQUEST_KEY, AddActivity.REQUEST_INCOME_CODE)
                                it.putExtra(AddActivity.EXPENSE_ID_KEY, income.id)
                                it.putExtra(AddActivity.EXPENSE_NAME_KEY, income.name)
                                it.putExtra(AddActivity.EXPENSE_PRICE_KEY, income.price)
                                it.putExtra(AddActivity.EXPENSE_CATEGORY_KEY, income.category)
                                it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                                it.putExtra(AddActivity.EXPENSE_YEAR_KEY, income.year)
                                it.putExtra(AddActivity.EXPENSE_MONTH_KEY, income.month)
                                it.putExtra(AddActivity.EXPENSE_DAY_KEY, income.day)
                                editResultLauncher.launch(it)
                            }
                            onDismiss()
                        },
                        onDelete = {
                            viewModel.deleteIncome(income)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onCompleted(response: androidx.lifecycle.LiveData<com.frafio.myfinance.data.model.FinanceResult>, previousBudget: Double?) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                FinanceCode.BUDGET_UPDATE_SUCCESS.code -> {
                    previousBudget?.let {
                        (activity as HomeActivity).showSnackBar(
                            result.message,
                            getString(R.string.cancel)
                        ) {
                            viewModel.setMonthlyBudget(previousBudget)
                        }
                    }
                }
                FinanceCode.INCOME_ADD_SUCCESS.code -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }
                else -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }
            }
        }
    }

    override fun onDeleteCompleted(response: androidx.lifecycle.LiveData<com.frafio.myfinance.data.model.FinanceResult>, income: Income) {
        response.observe(viewLifecycleOwner) { result ->
            if (result.code == FinanceCode.INCOME_DELETE_SUCCESS.code) {
                (activity as HomeActivity).showSnackBar(
                    result.message,
                    getString(R.string.cancel)
                ) {
                    viewModel.addIncome(income)
                }
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
        }
    }
}
