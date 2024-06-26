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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.managers.PurchaseManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentBudgetBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.createTextDrawable
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView

class BudgetFragment : BaseFragment(), BudgetListener, IncomeInteractionListener {
    private lateinit var binding: FragmentBudgetBinding
    private val viewModel by viewModels<BudgetViewModel>()
    private var isListBlocked = false
    private var maxIncomeNumber: Long = DEFAULT_LIMIT + 1

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            val editRequest = data!!.getIntExtra(AddActivity.PURCHASE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_INCOME_CODE) {
                viewModel.updateLocalIncomeList()
                (activity as HomeActivity).showSnackBar(PurchaseCode.INCOME_EDIT_SUCCESS.message)
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

        viewModel.updateIncomeNumber()
        viewModel.updateIncomeList(DEFAULT_LIMIT)
        viewModel.getMonthlyBudgetFromDb()

        viewModel.incomes.observe(viewLifecycleOwner) { incomes ->
            val nl = incomes.map { i -> i.copy() }
            binding.budgetRecycleView.also {
                if (it.adapter == null) {
                    it.adapter = IncomeAdapter(nl, this)
                } else {
                    (it.adapter as IncomeAdapter).updateData(nl)
                }
            }
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
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
                requireContext().hideSoftKeyboard(requireView().rootView)
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
                viewModel.updateMonthlyBudget(0.0, true)
            } else {
                // confirm modifications
                val budget = binding.monthlyBudgetET.text.toString().toDouble()
                viewModel.updateMonthlyBudget(budget, true)
            }
        }

        binding.monthlyBudgetET.doOnTextChanged { text, _, _, _ ->
            val newBudget = doubleToString(text?.trim().toString().toDoubleOrNull() ?: 0.0)
            val previousBudget = binding.monthlyBudgetTV.text.toString()
            binding.monthlyBudgetDeleteBtn.isEnabled =
                newBudget != "0.00" && newBudget != previousBudget
        }

        return binding.root
    }

    override fun onCompleted(response: LiveData<PurchaseResult>, previousBudget: Double?) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                PurchaseCode.PURCHASE_COUNT_SUCCESS.code -> {
                    maxIncomeNumber = result.message.toLong()
                    val limit = if (binding.budgetRecycleView.adapter != null) {
                        (binding.budgetRecycleView.adapter as IncomeAdapter).getLimit()
                    } else {
                        DEFAULT_LIMIT
                    }
                    isListBlocked = limit >= maxIncomeNumber
                }

                PurchaseCode.INCOME_LIST_UPDATE_SUCCESS.code -> {
                    viewModel.updateLocalIncomeList()
                    val limit = if (binding.budgetRecycleView.adapter != null) {
                        (binding.budgetRecycleView.adapter as IncomeAdapter).getLimit()
                    } else {
                        DEFAULT_LIMIT
                    }
                    isListBlocked = limit >= maxIncomeNumber
                }

                PurchaseCode.BUDGET_UPDATE_SUCCESS.code -> {
                    viewModel.updateMonthlyBudgetFromStorage()
                    if (!binding.monthlyBudgetTV.isVisible) {
                        binding.monthlyBudgetTV.visibility = View.VISIBLE
                        binding.monthlyBudgetET.visibility = View.GONE
                        requireContext().hideSoftKeyboard(requireView().rootView)
                        binding.monthlyBudgetEditBtn.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_create)
                        binding.monthlyBudgetDeleteBtn.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                    }
                    previousBudget?.let {
                        (activity as HomeActivity).showSnackBar(
                            result.message,
                            getString(R.string.cancel)
                        ) {
                            viewModel.updateMonthlyBudget(previousBudget)
                        }
                    }
                }

                PurchaseCode.INCOME_ADD_SUCCESS.code -> {
                    viewModel.updateLocalIncomeList()
                    val payload = result.message.split("&")
                    (activity as HomeActivity).showSnackBar(payload[0])
                }

                else -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }
            }
        }
    }

    override fun onDeleteCompleted(response: LiveData<PurchaseResult>, income: Purchase) {
        response.observe(viewLifecycleOwner) { result ->
            if (result.code == PurchaseCode.INCOME_DELETE_SUCCESS.code) {
                viewModel.updateLocalIncomeList()

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

    override fun scrollUp() {
        super.scrollUp()
        binding.budgetScrollView.scrollTo(0, 0)
    }

    override fun onItemInteraction(interactionID: Int, income: Purchase, position: Int) {
        when (interactionID) {
            ON_LONG_CLICK -> {
                if (requireActivity().findViewById<NavigationView?>(R.id.nav_drawer) != null) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_edit_purchase_bottom_sheet)
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
                    viewModel.updateIncomeList(
                        (binding.budgetRecycleView.adapter as IncomeAdapter).getLimit(true)
                    )
                    isListBlocked = true
                }
            }
        }
    }

    fun scrollIncomesTo(position: Int) {
        (binding.budgetRecycleView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            position,
            0
        )
    }

    fun refreshData() {
        viewModel.updateLocalIncomeList()
        viewModel.getMonthlyBudgetFromDb()
    }

    class ModalBottomSheet(
        private val fragment: BudgetFragment,
        private val income: Purchase,
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
                inflater.inflate(R.layout.layout_edit_purchase_bottom_sheet, container, false)
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
        income: Purchase,
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
        layout.findViewById<MaterialButton>(R.id.purchaseCategoryIcon).icon =
            createTextDrawable(layout.context, income.name!![0].uppercase())

        val editLayout = layout.findViewById<LinearLayout>(R.id.edit_layout)
        val deleteLayout = layout.findViewById<LinearLayout>(R.id.delete_layout)
        layout.findViewById<LinearLayout>(R.id.editPurchaseLayout).visibility = View.VISIBLE
        layout.findViewById<ConstraintLayout>(R.id.editCategoryLayout).visibility = View.GONE
        editLayout.setOnClickListener {
            Intent(context, AddActivity::class.java).also {
                it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                it.putExtra(AddActivity.PURCHASE_REQUEST_KEY, AddActivity.REQUEST_INCOME_CODE)
                it.putExtra(AddActivity.PURCHASE_ID_KEY, income.id)
                it.putExtra(AddActivity.PURCHASE_NAME_KEY, income.name)
                it.putExtra(AddActivity.PURCHASE_PRICE_KEY, income.price)
                it.putExtra(AddActivity.PURCHASE_CATEGORY_KEY, income.category)
                it.putExtra(AddActivity.PURCHASE_POSITION_KEY, position)
                it.putExtra(AddActivity.PURCHASE_YEAR_KEY, income.year)
                it.putExtra(AddActivity.PURCHASE_MONTH_KEY, income.month)
                it.putExtra(AddActivity.PURCHASE_DAY_KEY, income.day)
                editResultLauncher.launch(it)
            }
            dismissFun()
        }
        deleteLayout.setOnClickListener {
            viewModel.deleteIncomeAt(position, income)
            dismissFun()
        }
    }
}