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

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            if (viewModel.thisMonthTotalResult.value == null) {
                updatePriceProgressBar(
                    binding.progressBarFront,
                    binding.progressBarBack,
                    binding.percentageTV,
                    1.0,
                    0.0
                )
            } else {
                updatePriceProgressBar(
                    binding.progressBarFront,
                    binding.progressBarBack,
                    binding.percentageTV,
                    viewModel.thisMonthTotalResult.value!!.second,
                    monthlyBudget.toDouble()
                )
            }
        }

        viewModel.totalSumResult.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val price = value.second
            if (result.code == PurchaseCode.PURCHASE_AGGREGATE_SUCCESS.code) {
                viewModel.updateStats(totalSum = price)
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
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

        viewModel.thisMonthTotalResult.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val price = value.second
            if (result.code == PurchaseCode.PURCHASE_AGGREGATE_SUCCESS.code) {
                viewModel.updateStats(thisMonthTotal = price)
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }

            // Update monthly budget bar
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
    }
}