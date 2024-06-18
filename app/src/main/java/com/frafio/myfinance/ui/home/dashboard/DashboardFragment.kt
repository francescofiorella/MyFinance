package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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