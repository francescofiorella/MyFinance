package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.BarChart
import com.frafio.myfinance.data.models.BarChartEntry
import com.frafio.myfinance.data.models.PieChart
import com.frafio.myfinance.data.models.ProgressBar
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString
import java.time.format.DateTimeFormatter

class DashboardFragment : BaseFragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    private lateinit var monthlyBarChart: BarChart
    private lateinit var budgetProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        budgetProgressBar = ProgressBar(binding.barChartLayout, requireContext())
        monthlyBarChart = BarChart(binding.monthlyChart, requireContext())

        viewModel.getPurchaseNumber().observe(viewLifecycleOwner) {
            viewModel.isListEmpty.value = it == 0
        }

        viewModel.getPriceSumFromToday().observe(viewLifecycleOwner) {
            val todaySum = it ?: 0.0
            // Update today TV
            binding.todayTotTV.text = if (todaySum < 1000)
                doubleToPrice(todaySum)
            else
                doubleToPriceWithoutDecimals(todaySum)
        }

        viewModel.getPriceSumFromThisMonth().observe(viewLifecycleOwner) {
            viewModel.thisMonthSum = it ?: 0.0
            if (viewModel.monthShown) {
                binding.thisMonthTVTitle.text = getString(R.string.this_month)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisMonthSum)
                budgetProgressBar.updateValue(viewModel.thisMonthSum, viewModel.monthlyBudget)
            } else {
                binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                binding.thisYearTV.text = if (viewModel.thisMonthSum < 1000)
                    doubleToPrice(viewModel.thisMonthSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisMonthSum)
            }
        }

        viewModel.getPriceSumFromThisYear().observe(viewLifecycleOwner) {
            viewModel.thisYearSum = it ?: 0.0
            if (viewModel.monthShown) {
                binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                binding.thisYearTV.text = if (viewModel.thisYearSum < 1000)
                    doubleToPrice(viewModel.thisYearSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisYearSum)
            } else {
                binding.thisMonthTVTitle.text = getString(R.string.this_year)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisYearSum)
                budgetProgressBar.updateValue(viewModel.thisYearSum, viewModel.monthlyBudget * 12)
            }
        }

        val barChartLiveData = MediatorLiveData<List<BarChartEntry>>()
        var pricesLiveData = viewModel.getPricesList()
        barChartLiveData.addSource(pricesLiveData) { value ->
            barChartLiveData.value = value
        }
        barChartLiveData.observe(viewLifecycleOwner) { entries ->
            if (entries == null) return@observe
            val labels = mutableListOf<String>()
            val values = mutableListOf<Double>()
            var currentDate = viewModel.lastDateForBarChart.value!!
            var j = 0
            for (i in 0..<12) {
                if (j < entries.size
                    && entries[j].year == currentDate.year
                    && entries[j].month == currentDate.monthValue
                ) {
                    val monthString = if (entries[j].month < 10)
                        "0${entries[j].month}" else entries[j].month.toString()
                    labels.add("$monthString/${entries[j].year - 2000}")
                    values.add(entries[j].value)
                    j++
                } else {
                    val monthString = if (currentDate.monthValue < 10)
                        "0${currentDate.monthValue}" else currentDate.monthValue.toString()
                    labels.add("$monthString/${currentDate.year - 2000}")
                    values.add(0.0)
                }
                currentDate = currentDate.minusMonths(1)
            }
            monthlyBarChart.updateValues(labels, values)
        }

        viewModel.lastDateForBarChart.observe(viewLifecycleOwner) {
            // this trigger the observer
            barChartLiveData.removeSource(pricesLiveData)
            pricesLiveData = viewModel.getPricesList()
            barChartLiveData.addSource(pricesLiveData) { value ->
                barChartLiveData.value = value
            }
        }
        val pieChartLiveData = MediatorLiveData<List<Purchase>>()
        var purchasesOfMonthLiveData = viewModel.getPurchasesOfMonth()
        pieChartLiveData.addSource(purchasesOfMonthLiveData) { value ->
            pieChartLiveData.value = value
        }
        var animationPlayed = false
        pieChartLiveData.observe(viewLifecycleOwner) { purchases ->
            // Create a LocalDate with the given month
            val formatter = DateTimeFormatter.ofPattern("MMMM uuuu")
            binding.pieChartTitle?.text = viewModel.pieChartDate.value!!.format(formatter)
            val values = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            purchases.forEach { p ->
                if (p.category!! <= 8) {
                    values[p.category] += p.price!!
                }
            }
            binding.textView21?.text = doubleToPriceWithoutDecimals(values.sum())
            binding.textView22?.text = doubleToPriceWithoutDecimals(values[0])
            binding.textView23?.text = doubleToPriceWithoutDecimals(values[1])
            binding.textView24?.text = doubleToPriceWithoutDecimals(values[2])
            binding.textView25?.text = doubleToPriceWithoutDecimals(values[3])
            binding.textView26?.text = doubleToPriceWithoutDecimals(values[4])
            binding.textView27?.text = doubleToPriceWithoutDecimals(values[5])
            binding.textView28?.text = doubleToPriceWithoutDecimals(values[6])
            binding.textView29?.text = doubleToPriceWithoutDecimals(values[7])
            binding.textView210?.text = doubleToPriceWithoutDecimals(values[8])
            binding.pieChartComposeView?.disposeComposition()
            binding.pieChartComposeView?.setContent {
                PieChart(
                    data = values,
                    animate = !animationPlayed
                )
            }
            animationPlayed = true
        }

        viewModel.pieChartDate.observe(viewLifecycleOwner) {
            // this trigger the observer
            pieChartLiveData.removeSource(purchasesOfMonthLiveData)
            purchasesOfMonthLiveData = viewModel.getPurchasesOfMonth()
            pieChartLiveData.addSource(purchasesOfMonthLiveData) { value ->
                pieChartLiveData.value = value
            }
        }

        binding.pieChartPreviousBtn?.setOnClickListener {
            viewModel.previousPieChartDate()
        }

        binding.pieChartNextBtn?.setOnClickListener {
            viewModel.nextPieChartDate()
        }

        binding.barChartPreviousBtn.setOnClickListener {
            viewModel.previousBarChartDate()
        }

        binding.barChartNextBtn.setOnClickListener {
            viewModel.nextBarChartDate()
        }

        PurchaseStorage.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            if (monthlyBudget == null) return@observe
            viewModel.monthlyBudget = monthlyBudget
            if (viewModel.monthShown) {
                binding.budgetTV.text = doubleToString(monthlyBudget)
                budgetProgressBar.updateValue(viewModel.thisMonthSum, monthlyBudget)
            } else {
                binding.budgetTV.text = doubleToString(monthlyBudget * 12)
                budgetProgressBar.updateValue(
                    viewModel.thisYearSum,
                    monthlyBudget * 12
                )
            }
        }

        binding.changeBtn.setOnClickListener {
            binding.changeBtn.animate().rotationBy(
                if (viewModel.monthShown) 180f else -180f
            ).setDuration(200).start()
            viewModel.monthShown = !viewModel.monthShown
            if (viewModel.monthShown) {
                binding.thisMonthTVTitle.text = getString(R.string.this_month)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisMonthSum)
                binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                binding.thisYearTV.text = if (viewModel.thisYearSum < 1000)
                    doubleToPrice(viewModel.thisYearSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisYearSum)
                binding.budgetTV.text = doubleToString(viewModel.monthlyBudget)
                budgetProgressBar.updateValue(
                    viewModel.thisMonthSum,
                    viewModel.monthlyBudget
                )
            } else {
                binding.thisMonthTVTitle.text = getString(R.string.this_year)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisYearSum)
                binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                binding.thisYearTV.text = if (viewModel.thisMonthSum < 1000)
                    doubleToPrice(viewModel.thisMonthSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisMonthSum)
                binding.budgetTV.text = doubleToString(
                    viewModel.monthlyBudget * 12
                )
                budgetProgressBar.updateValue(
                    viewModel.thisYearSum,
                    viewModel.monthlyBudget * 12
                )
            }
        }

        return binding.root
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.dashboardScrollView.apply {
            stopNestedScroll()
            scrollTo(0, 0)
        }
    }
}