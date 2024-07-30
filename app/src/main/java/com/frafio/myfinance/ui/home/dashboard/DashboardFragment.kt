package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.BarChart
import com.frafio.myfinance.data.models.ProgressBar
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

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
            }
        }

        viewModel.getPricesList().observe(viewLifecycleOwner) { entries ->
            val labels = mutableListOf<String>()
            val values = mutableListOf<Double>()
            var currentDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
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

        PurchaseStorage.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            if (monthlyBudget == null) return@observe
            if (viewModel.monthShown) {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble())
                budgetProgressBar.updateValue(viewModel.thisMonthSum, monthlyBudget.toDouble())
            } else {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble() * 12)
                budgetProgressBar.updateValue(
                    viewModel.thisYearSum,
                    monthlyBudget.toDouble() * 12
                )
            }
        }

        binding.changeBtn.setOnClickListener {
            if (PurchaseStorage.monthlyBudget.value != null) {
                binding.changeBtn.animate().rotationBy(
                    if (viewModel.monthShown) 180f else -180f
                ).setDuration(200).start()
                viewModel.monthShown = !viewModel.monthShown
                if (viewModel.monthShown) {
                    binding.thisMonthTVTitle.text = getString(R.string.this_month)
                    binding.thisMonthTV.text = doubleToPrice(
                        viewModel.thisMonthSum
                    )
                    binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                    binding.thisYearTV.text = if (viewModel.thisYearSum < 1000)
                        doubleToPrice(viewModel.thisYearSum)
                    else
                        doubleToPriceWithoutDecimals(viewModel.thisYearSum)
                    binding.budgetTV.text = doubleToString(
                        PurchaseStorage.monthlyBudget.value!!.toDouble()
                    )
                    budgetProgressBar.updateValue(
                        viewModel.thisMonthSum,
                        PurchaseStorage.monthlyBudget.value!!.toDouble()
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
                        PurchaseStorage.monthlyBudget.value!!.toDouble() * 12
                    )
                    budgetProgressBar.updateValue(
                        viewModel.thisYearSum,
                        PurchaseStorage.monthlyBudget.value!!.toDouble() * 12
                    )
                }
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.dashboardScrollView.scrollTo(0, 0)
    }
}