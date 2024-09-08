package com.frafio.myfinance.ui.home.budget

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.manager.IncomesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.storage.UserStorage
import com.frafio.myfinance.databinding.FragmentBudgetBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.addTotalsToIncomes
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.createTextDrawable
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView

class BudgetFragment : BaseFragment(), BudgetListener, IncomeInteractionListener {
    private lateinit var binding: FragmentBudgetBinding
    private val viewModel by viewModels<BudgetViewModel>()
    private var isListBlocked = false
    private var maxIncomeNumber: Long = DEFAULT_LIMIT + 1
    private val recViewLiveData = MediatorLiveData<List<Income>>()
    private lateinit var localIncomesLiveData: LiveData<List<Income>>

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data!!.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_budget, container, false)

        viewModel.listener = this

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        localIncomesLiveData = viewModel.getLocalIncomes()
        recViewLiveData.addSource(localIncomesLiveData) { value ->
            recViewLiveData.value = value
        }
        recViewLiveData.observe(viewLifecycleOwner) { incomes ->
            // Evaluate limit and decide if new items can be retrieved
            val limit = if (binding.budgetRecyclerView.adapter != null) {
                (binding.budgetRecyclerView.adapter as IncomeAdapter).getLimit()
            } else {
                DEFAULT_LIMIT
            }
            // Get list with limit and update recList
            var nl = incomes.take(limit.toInt()).map { i -> i.copy() }
            nl = addTotalsToIncomes(nl)
            viewModel.updateIncomesEmpty(nl.isEmpty())
            maxIncomeNumber = incomes.size.toLong()
            binding.budgetRecyclerView.also {
                if (it.adapter == null) {
                    it.adapter = IncomeAdapter(nl, this)
                    isListBlocked = limit >= maxIncomeNumber
                } else {
                    it.post {
                        (it.adapter as IncomeAdapter).updateData(nl)
                        isListBlocked = limit >= maxIncomeNumber
                    }
                }
            }
        }

        UserStorage.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            viewModel.updateAnnualBudget(budget * 12)
            binding.monthlyBudgetDeleteBtn.isEnabled = budget != 0.0
        }

        binding.monthlyBudgetEditBtn.setOnClickListener {
            if (binding.monthlyBudgetTV.isVisible) {
                // show edit interface
                if (binding.monthlyBudgetTV.text == "0.00") {
                    binding.monthlyBudgetET.clearText()
                } else {
                    binding.monthlyBudgetET.setText(binding.monthlyBudgetTV.text)
                }
                binding.monthlyBudgetTV.visibility = View.GONE
                binding.monthlyBudgetET.visibility = View.VISIBLE
                binding.monthlyBudgetEditBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
                binding.monthlyBudgetDeleteBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
                binding.monthlyBudgetDeleteBtn.isEnabled = false
            } else {
                // cancel any modifications
                binding.monthlyBudgetTV.visibility = View.VISIBLE
                binding.monthlyBudgetET.visibility = View.GONE
                requireContext().hideSoftKeyboard(binding.root)
                binding.monthlyBudgetEditBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_create)
                binding.monthlyBudgetDeleteBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                binding.monthlyBudgetDeleteBtn.isEnabled = binding.monthlyBudgetTV.text != "0.00"
            }
        }

        binding.monthlyBudgetDeleteBtn.setOnClickListener {
            if (binding.monthlyBudgetTV.isVisible) {
                // delete budget
                viewModel.setMonthlyBudget(0.0, true)
            } else {
                // confirm modifications
                val budget = binding.monthlyBudgetET.text.toString().toDouble()
                viewModel.setMonthlyBudget(budget, true)
            }
        }

        binding.monthlyBudgetET.doOnTextChanged { text, _, _, _ ->
            // Remove possibility of adding more than 2 decimals
            if (!text.isNullOrEmpty() && text.isNotBlank() && text.contains(".")) {
                var lastPartOfText = text.split(".")[text.split(".").size - 1]
                if (lastPartOfText.count() > 2) {
                    lastPartOfText = text.substring(0, text.indexOf(".") + 3)
                    val selection = binding.monthlyBudgetET.selectionEnd
                    binding.monthlyBudgetET.setText(lastPartOfText)
                    if (selection >= lastPartOfText.length)
                        binding.monthlyBudgetET.setSelection(lastPartOfText.length)
                    else
                        binding.monthlyBudgetET.setSelection(selection)
                }
            }

            // Check if budget is not changed from db value
            val newBudget = doubleToString(text?.trim().toString().toDoubleOrNull() ?: 0.0)
            val previousBudget = binding.monthlyBudgetTV.text.toString()
            binding.monthlyBudgetDeleteBtn.isEnabled =
                newBudget != "0.00" && newBudget != previousBudget
        }

        return binding.root
    }

    override fun onCompleted(response: LiveData<FinanceResult>, previousBudget: Double?) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                FinanceCode.INCOME_LIST_UPDATE_SUCCESS.code -> {
                    val limit = if (binding.budgetRecyclerView.adapter != null) {
                        (binding.budgetRecyclerView.adapter as IncomeAdapter).getLimit()
                    } else {
                        DEFAULT_LIMIT
                    }
                    isListBlocked = limit >= maxIncomeNumber
                }

                FinanceCode.BUDGET_UPDATE_SUCCESS.code -> {
                    if (!binding.monthlyBudgetTV.isVisible) {
                        binding.monthlyBudgetTV.visibility = View.VISIBLE
                        binding.monthlyBudgetET.visibility = View.GONE
                        requireContext().hideSoftKeyboard(binding.root)
                        binding.monthlyBudgetEditBtn.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_create)
                        binding.monthlyBudgetDeleteBtn.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                    }
                    // Show snackbar for undoing the operation
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

    override fun onDeleteCompleted(response: LiveData<FinanceResult>, income: Income) {
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

    override fun onItemInteraction(interactionID: Int, income: Income, position: Int) {
        when (interactionID) {
            ON_LONG_CLICK -> {
                if (resources.getBoolean(R.bool.is600dp)) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_edit_expense_bottom_sheet)
                    defineSheetInterface(
                        sideSheetDialog.findViewById(android.R.id.content)!!,
                        income,
                        position,
                        editResultLauncher,
                        viewModel,
                        sideSheetDialog::hide
                    )
                    sideSheetDialog.show()
                } else {
                    val modalBottomSheet = ModalBottomSheet(
                        this,
                        income,
                        position,
                        editResultLauncher,
                        viewModel
                    )
                    modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
                }
            }

            ON_LOAD_MORE_REQUEST -> {
                // Increment elements limit on scroll
                if (!isListBlocked) {
                    isListBlocked = true
                    binding.budgetRecyclerView.adapter?.let {
                        (it as IncomeAdapter).getLimit(true)
                    }
                    // this trigger the observer
                    recViewLiveData.removeSource(localIncomesLiveData)
                    recViewLiveData.addSource(localIncomesLiveData) { value ->
                        recViewLiveData.value = value
                    }
                }
            }
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.budgetScrollView.apply {
            fling(0)
            smoothScrollTo(0, 0)
        }
        binding.budgetRecyclerView.apply {
            stopScroll()
            (layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
        }
    }

    fun scrollIncomesTo(position: Int) {
        binding.budgetRecyclerView.stopScroll()
        binding.budgetScrollView.stopNestedScroll()
        (binding.budgetRecyclerView.layoutManager as LinearLayoutManager?)
            ?.scrollToPositionWithOffset(position, 0)
    }

    class ModalBottomSheet(
        private val fragment: BudgetFragment,
        private val income: Income,
        private val position: Int,
        private val editResultLauncher: ActivityResultLauncher<Intent>,
        private val viewModel: BudgetViewModel
    ) : BottomSheetDialogFragment() {

        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout =
                inflater.inflate(R.layout.layout_edit_expense_bottom_sheet, container, false)
            fragment.defineSheetInterface(
                layout,
                income,
                position,
                editResultLauncher,
                viewModel,
                this::dismiss
            )
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        income: Income,
        position: Int,
        editResultLauncher: ActivityResultLauncher<Intent>,
        viewModel: BudgetViewModel,
        dismissFun: () -> Unit
    ) {
        layout.findViewById<MaterialTextView>(R.id.nameTV).text = income.name
        layout.findViewById<MaterialTextView>(R.id.dateTV).text =
            dateToString(income.day, income.month, income.year)
        layout.findViewById<MaterialTextView>(R.id.priceTV).text =
            doubleToPrice(income.price ?: 0.0)
        layout.findViewById<MaterialButton>(R.id.expenseCategoryIcon).icon =
            createTextDrawable(layout.context, income.name!![0].uppercase())

        val editLayout = layout.findViewById<LinearLayout>(R.id.edit_layout)
        val deleteLayout = layout.findViewById<LinearLayout>(R.id.delete_layout)
        editLayout.setOnClickListener {
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
            dismissFun()
        }
        deleteLayout.setOnClickListener {
            viewModel.deleteIncome(income)
            dismissFun()
        }
    }
}