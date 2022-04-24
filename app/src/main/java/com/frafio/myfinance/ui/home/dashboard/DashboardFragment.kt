package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment

class DashboardFragment : BaseFragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)

        viewModel.getStats()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    fun refreshStatsData() {
        viewModel.getStats()
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.dashboardScrollView.scrollTo(0, 0)
    }
}