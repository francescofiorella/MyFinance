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
import com.frafio.myfinance.ui.features.home.budget.BudgetScreen
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme

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
                        onEditIncome = { income, position ->
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                                it.putExtra(AddActivity.EXPENSE_REQUEST_KEY, AddActivity.REQUEST_INCOME_CODE)
                                it.putExtra(AddActivity.EXTRA_TRANSACTION, income)
                                it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                                editResultLauncher.launch(it)
                            }
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
