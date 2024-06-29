package com.frafio.myfinance.ui.home.dashboard

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.animateRoot
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToString
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardFragment : BaseFragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)

        viewModel.updateStats()

        updateBarChartLabels(
            listOf(
                binding.bar0Label,
                binding.bar1Label,
                binding.bar2Label,
                binding.bar3Label,
                binding.bar4Label,
                binding.bar5Label,
                binding.bar6Label,
                binding.bar7Label,
                binding.bar8Label,
                binding.bar9Label,
                binding.bar10Label,
                binding.bar11Label,
            )
        )

        viewModel.lastYearPurchases.observe(viewLifecycleOwner) { purchases ->
            if (purchases.isEmpty()) {
                return@observe
            }
            val values = mutableListOf<Double>()
            val labels = mutableListOf<String>()
            var currentDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
            var currentLabel = "${currentDate.monthValue}/${currentDate.year}"
            var monthsPassed = 1
            var currentValue = 0.0
            for (p in purchases) {
                val pLabel = "${p.month}/${p.year}"
                if (currentLabel != pLabel) {
                    labels.add(currentLabel)
                    values.add(currentValue)
                    currentDate = currentDate.minusMonths(1)
                    monthsPassed += 1
                    var newLabel = "${currentDate.monthValue}/${currentDate.year}"
                    while (newLabel != pLabel) {
                        labels.add(newLabel)
                        values.add(0.0)
                        currentDate = currentDate.minusMonths(1)
                        monthsPassed += 1
                        newLabel = "${currentDate.monthValue}/${currentDate.year}"
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
                val newLabel = "${currentDate.monthValue}/${currentDate.year}"
                labels.add(newLabel)
                values.add(0.0)
            }
            updateBarChart(
                binding.barPlaceHolder,
                listOf(
                    binding.bar0,
                    binding.bar1,
                    binding.bar2,
                    binding.bar3,
                    binding.bar4,
                    binding.bar5,
                    binding.bar6,
                    binding.bar7,
                    binding.bar8,
                    binding.bar9,
                    binding.bar10,
                    binding.bar11,
                ),
                binding.indicatorTV,
                values
            )
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            if (viewModel.monthShown) {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble())
            } else {
                binding.budgetTV.text = doubleToString(monthlyBudget.toDouble() * 12)
            }
            if (viewModel.thisMonthResult.value == null) {
                updatePriceProgressBar(
                    binding.progressBarFront,
                    binding.progressBarBack,
                    binding.percentageTV,
                    1.0,
                    0.0
                )
            } else {
                if (viewModel.monthShown) {
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        viewModel.thisMonthResult.value!!.second,
                        monthlyBudget.toDouble()
                    )
                } else {
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        viewModel.thisYearResult.value!!.second,
                        monthlyBudget.toDouble() * 12
                    )
                }
            }
        }

        viewModel.thisYearResult.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val price = value.second
            if (result.code == PurchaseCode.PURCHASE_AGGREGATE_SUCCESS.code) {
                viewModel.updateStats(thisYearTot = price)
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
            if (viewModel.monthShown) {
                binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                binding.thisYearTV.text = viewModel.thisYearString.value
            } else {
                binding.thisMonthTVTitle.text = getString(R.string.this_year)
                binding.thisMonthTV.text = doubleToPrice(price)
            }

            // Update annual budget bar
            if (!viewModel.monthShown) {
                viewModel.monthlyBudget.value?.let { monthlyBudget ->
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        price,
                        monthlyBudget.toDouble() * 12
                    )
                }
            }
        }

        viewModel.thisMonthResult.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val price = value.second
            if (result.code == PurchaseCode.PURCHASE_AGGREGATE_SUCCESS.code) {
                viewModel.updateStats(thisMonthTot = price)
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
            if (viewModel.monthShown) {
                binding.thisMonthTVTitle.text = getString(R.string.this_month)
                binding.thisMonthTV.text = doubleToPrice(price)
            } else {
                binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                binding.thisYearTV.text = viewModel.thisMonthString.value
            }

            // Update monthly budget bar
            if (viewModel.monthShown) {
                viewModel.monthlyBudget.value?.let { monthlyBudget ->
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        price,
                        monthlyBudget.toDouble()
                    )
                }
            }
        }

        viewModel.todayTotalResult.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val price = value.second
            if (result.code == PurchaseCode.PURCHASE_AGGREGATE_SUCCESS.code) {
                viewModel.updateStats(todayTot = price)
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
        }

        binding.changeBtn.setOnClickListener {
            binding.changeBtn.animate().rotationBy(
                if (viewModel.monthShown) 180f else -180f
            ).setDuration(200).start()
            viewModel.monthShown = !viewModel.monthShown
            if (viewModel.thisMonthResult.value != null
                && viewModel.thisYearResult.value != null
                && viewModel.monthlyBudget.value != null
            ) {
                if (viewModel.monthShown) {
                    binding.thisMonthTVTitle.text = getString(R.string.this_month)
                    binding.thisMonthTV.text = doubleToPrice(
                        viewModel.thisMonthResult.value!!.second
                    )
                    binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                    binding.thisYearTV.text = viewModel.thisYearString.value
                    binding.budgetTV.text = doubleToString(
                        viewModel.monthlyBudget.value!!.toDouble()
                    )
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        viewModel.thisMonthResult.value!!.second,
                        viewModel.monthlyBudget.value!!.toDouble()
                    )
                } else {
                    binding.thisMonthTVTitle.text = getString(R.string.this_year)
                    binding.thisMonthTV.text = doubleToPrice(
                        viewModel.thisYearResult.value!!.second
                    )
                    binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                    binding.thisYearTV.text = viewModel.thisMonthString.value
                    binding.budgetTV.text = doubleToString(
                        viewModel.monthlyBudget.value!!.toDouble() * 12
                    )
                    updatePriceProgressBar(
                        binding.progressBarFront,
                        binding.progressBarBack,
                        binding.percentageTV,
                        viewModel.thisYearResult.value!!.second,
                        viewModel.monthlyBudget.value!!.toDouble() * 12
                    )
                }
            } else {
                if (viewModel.monthShown) {
                    binding.thisMonthTVTitle.text = getString(R.string.this_month)
                    binding.thisMonthTV.text = getString(R.string.zero_price)
                    binding.thisYearTVTitle.text = getString(R.string.this_year_next)
                    binding.thisYearTV.text = getString(R.string.zero_price)
                    binding.budgetTV.text = getString(R.string.zero_double)
                } else {
                    binding.thisMonthTVTitle.text = getString(R.string.this_year)
                    binding.thisMonthTV.text = getString(R.string.zero_price)
                    binding.thisYearTVTitle.text = getString(R.string.this_month_next)
                    binding.thisYearTV.text = getString(R.string.zero_price)
                    binding.budgetTV.text = getString(R.string.zero_double)
                }
                updatePriceProgressBar(
                    binding.progressBarFront,
                    binding.progressBarBack,
                    binding.percentageTV,
                    1.0,
                    0.0
                )
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

    private fun updatePriceProgressBar(
        frontView: View,
        backView: View,
        percentageTextView: TextView,
        value: Double,
        maxValue: Double
    ) {
        if (maxValue == 0.0) {
            frontView.visibility = View.GONE
            percentageTextView.visibility = View.GONE
            return
        }

        val newWidth: Int
        val color: Int

        frontView.visibility = View.VISIBLE
        percentageTextView.visibility = View.VISIBLE

        val percentage = value / maxValue
        val percString = "${(percentage * 100).toInt()}%"
        percentageTextView.text = percString
        if (percentage <= 1) {
            val nw = (backView.width * percentage).toInt()
            newWidth = if (nw == 0) {
                frontView.visibility = View.INVISIBLE
                1
            } else {
                nw
            }
            val typedValue = TypedValue()
            requireActivity().theme.resolveAttribute(
                android.R.attr.colorPrimary,
                typedValue,
                true
            )
            color = ContextCompat.getColor(requireContext(), typedValue.resourceId)
        } else {
            newWidth = backView.width
            val typedValue = TypedValue()
            requireActivity().theme.resolveAttribute(
                android.R.attr.colorError,
                typedValue,
                true
            )
            color = ContextCompat.getColor(requireContext(), typedValue.resourceId)
        }
        val layoutParams = frontView.layoutParams
        layoutParams.width = newWidth
        frontView.layoutParams = layoutParams
        frontView.backgroundTintList = ColorStateList.valueOf(color)

        (binding.materialCardView1 as ViewGroup).animateRoot()
    }

    private fun updateBarChartLabels(labelViews: List<TextView>) {
        var currentDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        for (view in labelViews) {
            var label = if (currentDate.monthValue > 9) {
                "${currentDate.monthValue}"
            } else {
                "0${currentDate.monthValue}"
            }
            if (view == labelViews[0]) {
                label = "${label}/${currentDate.year - 2000}"
            }
            view.text = label
            currentDate = currentDate.minusMonths(1)
        }
    }

    private fun updateBarChart(
        holderBar: View,
        barViews: List<View>,
        indicatorTV: TextView,
        values: List<Double>
    ) {
        val maxValue = values.max()
        for (i in values.indices) {
            val height = if (values[i] != 0.0) {
                barViews[i].visibility = View.VISIBLE
                (values[i] * holderBar.height / maxValue).toInt()
            } else {
                barViews[i].visibility = View.INVISIBLE
                1
            }
            val layoutParams = barViews[i].layoutParams
            layoutParams.height = height
            barViews[i].layoutParams = layoutParams
        }
        indicatorTV.text = doubleToPrice(values[0])

        (binding.materialCardView4 as ViewGroup).animateRoot()
    }
}