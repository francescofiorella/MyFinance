package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.storage.MyFinanceStorage
import com.frafio.myfinance.data.widget.ProgressBar
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.components.BarChart
import com.frafio.myfinance.ui.components.PieChart
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.getThemeColor
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class DashboardFragment : BaseFragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    private lateinit var budgetProgressBar: ProgressBar

    private var pieChartAnimationPlayed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        budgetProgressBar =
            ProgressBar(binding.monthlyBarChartLayout.barChartLayout, requireContext())

        setupBarChart()
        setupPieChart()
        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupBarChart() {
        binding.monthlyChart.setContent {
            val barData by viewModel.barChartData.observeAsState(initial = emptyList<Double>() to emptyList())
            MyFinanceTheme {
                BarChart(
                    data = barData.first,
                    labels = barData.second,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun setupPieChart() {
        val pieChartLiveData = MediatorLiveData<List<Expense>>()
        var expensesSource = viewModel.getExpensesOfMonth()
        pieChartLiveData.addSource(expensesSource) { pieChartLiveData.value = it }

        viewModel.pieChartDate.observe(viewLifecycleOwner) {
            pieChartLiveData.removeSource(expensesSource)
            expensesSource = if (viewModel.monthlyShownInPieChart)
                viewModel.getExpensesOfMonth()
            else
                viewModel.getExpensesOfYear()
            pieChartLiveData.addSource(expensesSource) { pieChartLiveData.value = it }
        }

        binding.pieChartComposeView.setContent {
            val expenses by pieChartLiveData.observeAsState(initial = emptyList())
            val date by viewModel.pieChartDate.observeAsState(initial = java.time.LocalDate.now())
            
            // Sync the TV date text (kept for compatibility with current layout)
            val formatter = DateTimeFormatter.ofPattern(
                if (viewModel.monthlyShownInPieChart) "MMMM uuuu" else "uuuu"
            )
            binding.dataShownTV.text = date.format(formatter)

            val values = rememberPieValues(expenses)
            
            MyFinanceTheme {
                PieChart(
                    data = values,
                    chartBarWidth = if (resources.getBoolean(R.bool.isLandscape)) 12.dp else 10.dp,
                    chartEntryOffset = if (resources.getBoolean(R.bool.isLandscape)) 11 else 9,
                    animate = !pieChartAnimationPlayed
                )
            }
            pieChartAnimationPlayed = true
        }
    }

    private fun rememberPieValues(expenses: List<Expense>): List<Double> {
        val values = MutableList(9) { 0.0 }
        expenses.forEach { p ->
            if (p.category != null && p.category <= 8) {
                values[p.category] += p.price ?: 0.0
            }
        }
        return values
    }

    private fun setupObservers() {
        viewModel.getExpensesNumber().observe(viewLifecycleOwner) {
            viewModel.isListEmpty.value = it == 0
        }

        viewModel.getPriceSumFromToday().observe(viewLifecycleOwner) {
            val todaySum = it ?: 0.0
            binding.todayTotTV.text = if (todaySum < 1000)
                doubleToPrice(todaySum)
            else
                doubleToPriceWithoutDecimals(todaySum)
        }

        viewModel.getPriceSumFromThisMonth().observe(viewLifecycleOwner) {
            viewModel.thisMonthSum = it ?: 0.0
            updateBudgetUI()
        }

        viewModel.getPriceSumFromThisYear().observe(viewLifecycleOwner) {
            viewModel.thisYearSum = it ?: 0.0
            updateBudgetUI()
        }

        setupBalanceObservers()

        MyFinanceStorage.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            viewModel.monthlyBudget = monthlyBudget ?: 0.0
            updateBudgetUI()
        }
    }

    private fun setupBalanceObservers() {
        val expensesSumLiveData = MediatorLiveData<Double?>()
        var expensesSumLD = viewModel.getExpensesSumForBalance()
        expensesSumLiveData.addSource(expensesSumLD) { value ->
            expensesSumLiveData.value = value
        }
        expensesSumLiveData.observe(viewLifecycleOwner) {
            viewModel.expensesSum = it ?: 0.0
            updateBalanceUI()
        }

        val incomesSumLiveData = MediatorLiveData<Double?>()
        var incomesSumLD = viewModel.getIncomesSumForBalance()
        incomesSumLiveData.addSource(incomesSumLD) { value ->
            incomesSumLiveData.value = value
        }
        incomesSumLiveData.observe(viewLifecycleOwner) {
            viewModel.incomesSum = it ?: 0.0
            updateBalanceUI()
        }

        viewModel.balanceYearShown.observe(viewLifecycleOwner) {
            binding.balanceTVTitle.text = getString(
                R.string.annual_balance,
                it.toString()
            )
            incomesSumLiveData.removeSource(incomesSumLD)
            incomesSumLD = viewModel.getIncomesSumForBalance()
            incomesSumLiveData.addSource(incomesSumLD) { value ->
                incomesSumLiveData.value = value
            }
            expensesSumLiveData.removeSource(expensesSumLD)
            expensesSumLD = viewModel.getExpensesSumForBalance()
            expensesSumLiveData.addSource(expensesSumLD) { value ->
                expensesSumLiveData.value = value
            }
        }
    }

    private fun updateBalanceUI() {
        binding.balanceExpensesTV.text = doubleToPrice(viewModel.expensesSum)
        binding.balanceIncomesTV.text = doubleToPrice(viewModel.incomesSum)
        val balance = viewModel.incomesSum - viewModel.expensesSum
        binding.balanceTV.text = doubleToPrice(abs(balance))
        val balanceColor = requireContext().getThemeColor(
            if (balance < 0.0) androidx.appcompat.R.attr.colorError else androidx.appcompat.R.attr.colorPrimary
        )
        binding.balanceTV.setTextColor(balanceColor)
    }

    private fun updateBudgetUI() {
        if (viewModel.monthShown) {
            binding.thisMonthTVTitle.text = getString(R.string.this_month)
            binding.thisMonthTV.text = doubleToPrice(viewModel.thisMonthSum)
            binding.thisYearTVTitle.text = getString(R.string.this_year_next)
            binding.thisYearTV.text = if (viewModel.thisYearSum < 1000) doubleToPrice(viewModel.thisYearSum) else doubleToPriceWithoutDecimals(viewModel.thisYearSum)
            binding.onBudgetTV.text = getString(R.string.on_total_budget, doubleToString(viewModel.monthlyBudget))
            budgetProgressBar.updateValue(viewModel.thisMonthSum, viewModel.monthlyBudget)
        } else {
            binding.thisMonthTVTitle.text = getString(R.string.this_year)
            binding.thisMonthTV.text = doubleToPrice(viewModel.thisYearSum)
            binding.thisYearTVTitle.text = getString(R.string.this_month_next)
            binding.thisYearTV.text = if (viewModel.thisMonthSum < 1000) doubleToPrice(viewModel.thisMonthSum) else doubleToPriceWithoutDecimals(viewModel.thisMonthSum)
            binding.onBudgetTV.text = getString(R.string.on_total_budget, doubleToString(viewModel.monthlyBudget * 12))
            budgetProgressBar.updateValue(viewModel.thisYearSum, viewModel.monthlyBudget * 12)
        }
        
        val visibility = if (viewModel.monthlyBudget == 0.0) View.GONE else View.VISIBLE
        binding.onBudgetTV.visibility = visibility
        binding.changeBtn.visibility = visibility
    }

    private fun setupClickListeners() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.size == 1) {
                pieChartAnimationPlayed = false
                viewModel.switchPieChartData(checkedIds[0] == R.id.monthly_chip)
            }
        }

        binding.balancePreviousBtn.setOnClickListener { viewModel.previousBalanceYear() }
        binding.balanceNextBtn.setOnClickListener { viewModel.nextBalanceYear() }
        binding.pieChartPreviousBtn.setOnClickListener { pieChartAnimationPlayed = false; viewModel.previousPieChartDate() }
        binding.pieChartNextBtn.setOnClickListener { pieChartAnimationPlayed = false; viewModel.nextPieChartDate() }
        binding.barChartPreviousBtn.setOnClickListener { viewModel.previousBarChartDate() }
        binding.barChartNextBtn.setOnClickListener { viewModel.nextBarChartDate() }

        binding.changeBtn.setOnClickListener {
            binding.changeBtn.animate().rotationBy(if (viewModel.monthShown) 180f else -180f).setDuration(200).start()
            viewModel.monthShown = !viewModel.monthShown
            updateBudgetUI()
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.dashboardScrollView.apply {
            fling(0)
            smoothScrollTo(0, 0)
        }
    }
}
