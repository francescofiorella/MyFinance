package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.BarChart
import com.frafio.myfinance.data.models.ProgressBar
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardFragment : BaseFragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    val isLayoutReady: LiveData<Boolean>
        get() = viewModel.isLayoutReady

    private lateinit var monthlyBarChart: BarChart
    private lateinit var budgetProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)

        viewModel.updateStats()

        budgetProgressBar = ProgressBar(binding.budgetLayout, requireContext())
        monthlyBarChart = BarChart(binding.monthlyChart, requireContext())

        var firstTime = true

        viewModel.lastYearPurchases.observe(viewLifecycleOwner) { purchases ->
            if (firstTime) {
                firstTime = false
                return@observe
            }
            (activity as HomeActivity).hideProgressIndicator()
            viewModel.isListEmpty.value = purchases.isEmpty()
            if (!viewModel.isLayoutReady.value!!)
                viewModel.isLayoutReady.value = true

            viewModel.thisYearSum = 0.0
            viewModel.thisMonthSum = 0.0
            viewModel.todaySum = 0.0

            val today = LocalDate.now()
            val values = mutableListOf<Double>()
            val labels = mutableListOf<String>()
            var currentDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
            var monthString = if (currentDate.monthValue > 9) {
                "${currentDate.monthValue}"
            } else {
                "0${currentDate.monthValue}"
            }
            var currentLabel = "${monthString}/${currentDate.year - 2000}"
            var monthsPassed = 1
            var currentValue = 0.0
            for (p in purchases) {
                if (p.year == today.year && p.price != null) {
                    viewModel.thisYearSum += p.price
                    if (p.month == today.monthValue) {
                        viewModel.thisMonthSum += p.price
                        if (p.day == today.dayOfMonth) {
                            viewModel.todaySum += p.price
                        }
                    }
                }
                monthString = if (p.month!! > 9) {
                    "${p.month}"
                } else {
                    "0${p.month}"
                }
                val pLabel = "${monthString}/${p.year!! - 2000}"
                if (currentLabel != pLabel) {
                    labels.add(currentLabel)
                    values.add(currentValue)
                    currentDate = currentDate.minusMonths(1)
                    monthsPassed += 1
                    monthString = if (currentDate.monthValue > 9) {
                        "${currentDate.monthValue}"
                    } else {
                        "0${currentDate.monthValue}"
                    }
                    var newLabel = "${monthString}/${currentDate.year - 2000}"
                    while (newLabel != pLabel) {
                        labels.add(newLabel)
                        values.add(0.0)
                        currentDate = currentDate.minusMonths(1)
                        monthsPassed += 1
                        monthString = if (currentDate.monthValue > 9) {
                            "${currentDate.monthValue}"
                        } else {
                            "0${currentDate.monthValue}"
                        }
                        newLabel = "${monthString}/${currentDate.year - 2000}"
                    }
                    currentLabel = pLabel
                    currentValue = p.price!!
                } else {
                    currentValue += p.price!!
                }
            }
            labels.add(currentLabel)
            values.add(currentValue)
            while (monthsPassed != 12) {
                currentDate = currentDate.minusMonths(1)
                monthsPassed += 1
                monthString = if (currentDate.monthValue > 9) {
                    "${currentDate.monthValue}"
                } else {
                    "0${currentDate.monthValue}"
                }
                val newLabel = "${monthString}/${currentDate.year - 2000}"
                labels.add(newLabel)
                values.add(0.0)
            }

            monthlyBarChart.updateValues(labels, values)
            // Update today TV
            binding.todayTotTV.text = if (viewModel.todaySum < 1000)
                doubleToPrice(viewModel.todaySum)
            else
                doubleToPriceWithoutDecimals(viewModel.todaySum)
            // Update this month and this year TV
            if (viewModel.monthShown) {
                binding.thisMonthTVTitle.text = getString(R.string.this_month)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisMonthSum)
                binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                binding.thisYearTV.text = if (viewModel.thisYearSum < 1000)
                    doubleToPrice(viewModel.thisYearSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisYearSum)
            } else {
                binding.thisMonthTVTitle.text = getString(R.string.this_year)
                binding.thisMonthTV.text = doubleToPrice(viewModel.thisYearSum)
                binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                binding.thisYearTV.text = if (viewModel.thisMonthSum < 1000)
                    doubleToPrice(viewModel.thisMonthSum)
                else
                    doubleToPriceWithoutDecimals(viewModel.thisMonthSum)
            }
            val monthlyBudget = viewModel.monthlyBudget.value
            if (monthlyBudget != null) {
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
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            if (viewModel.monthShown) {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble())
            } else {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble() * 12)
            }
            if (viewModel.lastYearPurchases.value != null) {
                if (viewModel.monthShown) {
                    budgetProgressBar.updateValue(viewModel.thisMonthSum, monthlyBudget.toDouble())
                } else {
                    budgetProgressBar.updateValue(
                        viewModel.thisYearSum,
                        monthlyBudget.toDouble() * 12
                    )
                }
            }
        }

        binding.changeBtn.setOnClickListener {
            if (viewModel.lastYearPurchases.value != null
                && viewModel.monthlyBudget.value != null
            ) {
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
                        viewModel.monthlyBudget.value!!.toDouble()
                    )
                    budgetProgressBar.updateValue(
                        viewModel.thisMonthSum,
                        viewModel.monthlyBudget.value!!.toDouble()
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
                        viewModel.monthlyBudget.value!!.toDouble() * 12
                    )
                    budgetProgressBar.updateValue(
                        viewModel.thisYearSum,
                        viewModel.monthlyBudget.value!!.toDouble() * 12
                    )
                }
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    fun refreshStatsData() {
        viewModel.updateStats()
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.dashboardScrollView.scrollTo(0, 0)
    }
}