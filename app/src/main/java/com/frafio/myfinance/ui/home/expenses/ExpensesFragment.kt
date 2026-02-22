package com.frafio.myfinance.ui.home.expenses

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.frafio.myfinance.data.widget.DatePickerRangeDialog
import com.frafio.myfinance.databinding.FragmentExpensesBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.composable.components.CategorySheetDialog
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.addTotalsToExpenses
import com.frafio.myfinance.utils.addTotalsToExpensesWithoutToday
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.sidesheet.SideSheetDialog
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ExpensesFragment : BaseFragment(), ExpenseInteractionListener, ExpensesListener {

    private lateinit var binding: FragmentExpensesBinding
    private val viewModel by viewModels<ExpensesViewModel>()
    private lateinit var datePickerRangeDialog: DatePickerRangeDialog

    private var isListBlocked = false
    private var maxExpensesNumber = DEFAULT_LIMIT + 1

    private val recViewLiveData = MediatorLiveData<List<Expense>>()
    private lateinit var localExpensesLiveData: LiveData<List<Expense>>

    private var toScroll: String? = null

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

        binding.filterIcon.setOnClickListener(onFilterClickListener)

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
            viewModel.nameFilter = text.toString()
            recViewLiveData.removeSource(localExpensesLiveData)
            localExpensesLiveData = viewModel.getLocalExpenses()
            recViewLiveData.addSource(localExpensesLiveData) { value ->
                recViewLiveData.value = value
            }
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
            nl = if (
                viewModel.nameFilter.isEmpty() &&
                viewModel.categoryFilterList.isEmpty() &&
                viewModel.dateFilter == null
            ) {
                addTotalsToExpenses(nl)
            } else {
                addTotalsToExpensesWithoutToday(nl)
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
                toScroll?.let { id ->
                    // If the list was just created and an item was just added
                    scrollToId(id)
                    toScroll = null
                }
            }
        }

        datePickerRangeDialog = object : DatePickerRangeDialog(
            requireActivity()
        ) {
            override fun onStart() {
                super.onStart()
                requireActivity().hideSoftKeyboard(binding.root)
            }

            override fun onPositiveBtnClickListener() {
                super.onPositiveBtnClickListener()
                viewModel.dateFilter = Pair(startDate!!, endDate!!)
                addDateChip(startDate!!, endDate!!)
                recViewLiveData.removeSource(localExpensesLiveData)
                localExpensesLiveData = viewModel.getLocalExpenses()
                recViewLiveData.addSource(localExpensesLiveData) { value ->
                    recViewLiveData.value = value
                }
            }
        }

        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (viewModel.dateFilter != null) {
            addDateChip(
                viewModel.dateFilter!!.first,
                viewModel.dateFilter!!.second
            )
        }
        for (categoryId in viewModel.categoryFilterList) {
            addCategoryChip(categoryId)
        }
        recViewLiveData.removeSource(localExpensesLiveData)
        localExpensesLiveData = viewModel.getLocalExpenses()
        recViewLiveData.addSource(localExpensesLiveData) { value ->
            recViewLiveData.value = value
        }
    }

    private val onFilterClickListener = View.OnClickListener {
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(requireContext())
        } else {
            BottomSheetDialog(requireContext())
        }
        val composeView = getFilterExpensesSheetDialogComposeView(sheetDialog::hide)
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    override fun onItemInteraction(
        interactionID: Int,
        expense: Expense,
        position: Int
    ) {
        when (interactionID) {
            ON_CLICK -> Unit

            ON_LONG_CLICK -> {
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

            ON_BUTTON_CLICK -> {
                val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
                    SideSheetDialog(requireContext())
                } else {
                    BottomSheetDialog(requireContext())
                }
                val composeView = getCategorySheetDialogComposeView(
                    expense = expense,
                    onDismiss = sheetDialog::hide,
                    onCategorySelected = { viewModel.updateCategory(expense, it) }
                )
                sheetDialog.setContentView(composeView)
                sheetDialog.show()
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
                    localExpensesLiveData = viewModel.getLocalExpenses()
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

    override fun scrollUp() {
        super.scrollUp()
        (binding.listRecyclerView.adapter as ExpenseAdapter?)?.apply {
            val position = getTodayPosition()
            scrollTo(position)
        }
    }

    private fun scrollTo(position: Int, animate: Boolean = false) {
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

    fun scrollToId(id: String) {
        if (::binding.isInitialized) {
            val position = (binding.listRecyclerView.adapter as ExpenseAdapter)
                .getItemPositionWithId(id)
            scrollTo(position)
        } else {
            toScroll = id
        }
    }

    private fun getCategoryDrawable(categoryId: Int) = ContextCompat.getDrawable(
        requireContext(),
        when (categoryId) {
            FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_home_filled
            FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart_filled
            FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care_filled
            FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy_filled
            FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school_filled
            FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant_filled
            FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines_filled
            FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_subway_filled
            FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_grid_3x3_filled
            else -> R.drawable.ic_grid_3x3_filled
        }
    )

    private fun addCategoryChip(categoryId: Int) {
        val label = resources.getStringArray(R.array.categories)[categoryId]
        val chip = layoutInflater.inflate(
            R.layout.layout_chip_input,
            binding.filterChipGroup,
            false
        ) as Chip
        chip.text = label
        chip.chipIcon = getCategoryDrawable(categoryId)
        chip.setOnCloseIconClickListener {
            binding.filterChipGroup.removeView(chip)
            viewModel.categoryFilterList.remove(categoryId)
            if (viewModel.categoryFilterList.isEmpty() && viewModel.dateFilter == null)
                binding.filterChipGroup.visibility = View.GONE

            recViewLiveData.removeSource(localExpensesLiveData)
            localExpensesLiveData = viewModel.getLocalExpenses()
            recViewLiveData.addSource(localExpensesLiveData) { value ->
                recViewLiveData.value = value
            }
        }
        binding.filterChipGroup.addView(chip)
        binding.filterChipGroup.visibility = View.VISIBLE
    }

    private fun addDateChip(startDate: LocalDate, endDate: LocalDate) {
        val label =
            when (startDate.year) {
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
                else -> {
                    "${dateToExtendedString(startDate)} - ${dateToExtendedString(endDate)}"
                }
            }
        val chip = layoutInflater.inflate(
            R.layout.layout_chip_input,
            binding.filterChipGroup,
            false
        ) as Chip
        chip.text = label
        chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_today_filled)
        chip.setOnCloseIconClickListener {
            viewModel.dateFilter = null
            binding.filterChipGroup.removeView(chip)
            if (viewModel.categoryFilterList.isEmpty() && viewModel.dateFilter == null)
                binding.filterChipGroup.visibility = View.GONE

            recViewLiveData.removeSource(localExpensesLiveData)
            localExpensesLiveData = viewModel.getLocalExpenses()
            recViewLiveData.addSource(localExpensesLiveData) { value ->
                recViewLiveData.value = value
            }
        }
        binding.filterChipGroup.addView(chip)
        binding.filterChipGroup.visibility = View.VISIBLE
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
                                it.putExtra(
                                    AddActivity.REQUEST_CODE_KEY,
                                    AddActivity.REQUEST_EDIT_CODE
                                )
                                it.putExtra(
                                    AddActivity.EXPENSE_REQUEST_KEY,
                                    AddActivity.REQUEST_EXPENSE_CODE
                                )
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
                    FilterExpensesSheet(
                        onDismiss = onDismiss,
                        categoryEnabled = viewModel.categoryFilterList.size != 9,
                        dateRangeEnabled = viewModel.dateFilter == null,
                        onSelectCategory = {
                            onDismiss()
                            val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
                                SideSheetDialog(requireContext())
                            } else {
                                BottomSheetDialog(requireContext())
                            }
                            val composeView = getCategorySheetDialogComposeView(
                                disabledCategories = viewModel.categoryFilterList,
                                onDismiss = sheetDialog::hide,
                                onCategorySelected = {
                                    viewModel.categoryFilterList.add(it)
                                    addCategoryChip(it)
                                    recViewLiveData.removeSource(localExpensesLiveData)
                                    localExpensesLiveData = viewModel.getLocalExpenses()
                                    recViewLiveData.addSource(localExpensesLiveData) { value ->
                                        recViewLiveData.value = value
                                    }
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