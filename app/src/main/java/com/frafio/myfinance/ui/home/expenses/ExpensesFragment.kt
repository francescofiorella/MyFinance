package com.frafio.myfinance.ui.home.expenses

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.manager.ExpensesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.databinding.FragmentExpensesBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.addTotalsToExpenses
import com.frafio.myfinance.utils.addTotalsToExpensesWithoutPrices
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView

class ExpensesFragment : BaseFragment(), ExpenseInteractionListener, ExpensesListener {

    private lateinit var binding: FragmentExpensesBinding
    private val viewModel by viewModels<ExpensesViewModel>()
    private var isListBlocked = false
    private var maxExpensesNumber = DEFAULT_LIMIT + 1
    private val recViewLiveData = MediatorLiveData<List<Expense>>()
    private lateinit var localExpensesLiveData: LiveData<List<Expense>>

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data!!.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.listener = this

        binding.listRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    /*SCROLL_STATE_IDLE -> {
                        val position = (binding.listRecyclerView.layoutManager as LinearLayoutManager)
                            .findFirstVisibleItemPosition()
                        scrollTo(position)
                    }*/
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        requireActivity().hideSoftKeyboard(binding.root)
                        binding.searchET.clearFocus()
                    }
                }
            }
        })

        viewModel.getExpensesNumber().observe(viewLifecycleOwner) {
            viewModel.isExpensesEmpty.value = it == 0
        }

        binding.searchET.doOnTextChanged { text, _, _, _ ->
            recViewLiveData.removeSource(localExpensesLiveData)
            localExpensesLiveData = if (text.isNullOrEmpty()) {
                binding.clearIcon.visibility = View.INVISIBLE
                viewModel.getLocalExpenses()
            } else {
                binding.clearIcon.visibility = View.VISIBLE
                viewModel.filterExpenses(text.toString())
            }
            recViewLiveData.addSource(localExpensesLiveData) { value ->
                recViewLiveData.value = value
            }
        }

        binding.clearIcon.setOnClickListener {
            requireActivity().hideSoftKeyboard(binding.root)
            binding.searchET.clearFocus()
            binding.searchET.clearText()
        }

        localExpensesLiveData = viewModel.getLocalExpenses()
        recViewLiveData.addSource(localExpensesLiveData) { value ->
            recViewLiveData.value = value
        }
        recViewLiveData.observe(viewLifecycleOwner) { expenses ->
            // Evaluate limit and decide if new items can be retrieved
            val limit = if (binding.listRecyclerView.adapter != null) {
                (binding.listRecyclerView.adapter as ExpenseAdapter).getLimit()
            } else {
                DEFAULT_LIMIT
            }
            // Get list with limit and update recList
            var nl = expenses.take(limit.toInt()).map { p -> p.copy() }
            nl = if (binding.searchET.text.isNullOrEmpty()) {
                addTotalsToExpenses(nl)
            } else {
                addTotalsToExpensesWithoutPrices(nl)
            }
            maxExpensesNumber = expenses.size.toLong()
            binding.listRecyclerView.also {
                if (it.adapter == null) {
                    it.adapter = ExpenseAdapter(nl, this)
                    isListBlocked = limit >= maxExpensesNumber
                    val position = (it.adapter as ExpenseAdapter).getTodayPosition()
                    scrollTo(position)
                } else {
                    it.post {
                        (it.adapter as ExpenseAdapter).updateData(nl)
                        isListBlocked = limit >= maxExpensesNumber
                    }
                }
            }
        }

        return binding.root
    }

    override fun onItemInteraction(
        interactionID: Int,
        expense: Expense,
        position: Int
    ) {
        when (interactionID) {
            ON_CLICK -> Unit

            ON_LONG_CLICK -> {
                if (resources.getBoolean(R.bool.is600dp)) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_edit_expense_bottom_sheet)
                    defineSheetInterface(
                        sideSheetDialog.findViewById(android.R.id.content)!!,
                        expense,
                        position,
                        editResultLauncher,
                        viewModel,
                        sideSheetDialog::hide
                    )
                    sideSheetDialog.show()
                } else {
                    val modalBottomSheet = ModalBottomSheet(
                        this,
                        expense,
                        position,
                        editResultLauncher,
                        viewModel
                    )
                    modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
                }
            }

            ON_BUTTON_CLICK -> {
                if (resources.getBoolean(R.bool.is600dp)) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_category_bottom_sheet)
                    defineSheetInterface(
                        sideSheetDialog.findViewById(android.R.id.content)!!,
                        expense,
                        position,
                        editResultLauncher,
                        viewModel,
                        sideSheetDialog::hide,
                        true
                    )
                    sideSheetDialog.show()
                } else {
                    val modalBottomSheet = ModalBottomSheet(
                        this,
                        expense,
                        position,
                        editResultLauncher,
                        viewModel,
                        true
                    )
                    modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
                }
            }

            ON_LOAD_MORE_REQUEST -> {
                // Increment elements limit on scroll
                if (!isListBlocked) {
                    isListBlocked = true
                    binding.listRecyclerView.adapter?.let {
                        (it as ExpenseAdapter).getLimit(true)
                    }
                    // this trigger the observer
                    recViewLiveData.removeSource(localExpensesLiveData)
                    recViewLiveData.addSource(localExpensesLiveData) { value ->
                        recViewLiveData.value = value
                    }
                }
            }
        }
    }

    override fun onCompleted(response: LiveData<FinanceResult>) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS.code -> {
                    val limit = if (binding.listRecyclerView.adapter != null) {
                        (binding.listRecyclerView.adapter as ExpenseAdapter).getLimit()
                    } else {
                        DEFAULT_LIMIT
                    }
                    isListBlocked = limit >= maxExpensesNumber
                }

                FinanceCode.EXPENSE_EDIT_SUCCESS.code -> {
                    Unit
                }

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

    override fun scrollUp() {
        super.scrollUp()
        binding.listRecyclerView.apply {
            val position = (adapter as ExpenseAdapter).getTodayPosition()
            scrollTo(position)
        }
    }

    fun scrollTo(position: Int, animate: Boolean = false) {
        binding.listRecyclerView.apply {
            stopScroll()
            if (animate) {
                val linearSmoothScroller: LinearSmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                            return 200.0f / displayMetrics.densityDpi
                        }
                    }
                linearSmoothScroller.targetPosition = position
                (layoutManager as LinearLayoutManager?)?.startSmoothScroll(linearSmoothScroller)
            } else {
                (layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(position, 0)
            }
        }
    }

    class ModalBottomSheet(
        private val fragment: ExpensesFragment,
        private val expense: Expense,
        private val position: Int,
        private val editResultLauncher: ActivityResultLauncher<Intent>,
        private val viewModel: ExpensesViewModel,
        private val fromCategoryIcon: Boolean = false
    ) : BottomSheetDialogFragment() {

        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout = inflater.inflate(
                if (fromCategoryIcon) R.layout.layout_category_bottom_sheet
                else R.layout.layout_edit_expense_bottom_sheet,
                container,
                false
            )
            fragment.defineSheetInterface(
                layout,
                expense,
                position,
                editResultLauncher,
                viewModel,
                this::dismiss,
                fromCategoryIcon
            )
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        expense: Expense,
        position: Int,
        editResultLauncher: ActivityResultLauncher<Intent>,
        viewModel: ExpensesViewModel,
        dismissFun: () -> Unit,
        fromCategoryIcon: Boolean = false
    ) {
        layout.findViewById<MaterialTextView>(R.id.nameTV).text = expense.name
        layout.findViewById<MaterialTextView>(R.id.dateTV).text =
            dateToString(expense.day, expense.month, expense.year)
        layout.findViewById<MaterialTextView>(R.id.priceTV).text =
            doubleToPrice(expense.price ?: 0.0)
        layout.findViewById<MaterialButton>(R.id.expenseCategoryIcon).icon =
            ContextCompat.getDrawable(
                requireContext(),
                when (expense.category) {
                    FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )

        if (fromCategoryIcon) {
            // layout_category_bottom_sheet.xml
            layout.findViewById<ConstraintLayout>(R.id.categoryDetailLayout).visibility = View.GONE
            layout.findViewById<ConstraintLayout>(R.id.expenseDetailLayout).visibility =
                View.VISIBLE
            layout.findViewById<LinearLayout>(R.id.housing_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.HOUSING.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.groceries_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.GROCERIES.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.personal_care_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.PERSONAL_CARE.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.entertainment_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.ENTERTAINMENT.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.education_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.EDUCATION.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.dining_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.DINING.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.health_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.HEALTH.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.transportation_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.TRANSPORTATION.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.miscellaneous_layout).setOnClickListener {
                viewModel.updateCategory(expense, FirestoreEnums.CATEGORIES.MISCELLANEOUS.value)
                dismissFun()
            }
            return
        }

        // layout_edit_expense_bottom_sheet.xml
        val editLayout = layout.findViewById<LinearLayout>(R.id.edit_layout)
        val deleteLayout = layout.findViewById<LinearLayout>(R.id.delete_layout)
        editLayout.setOnClickListener {
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
            dismissFun()
        }
        deleteLayout.setOnClickListener {
            viewModel.deleteExpense(expense)
            dismissFun()
        }
    }
}