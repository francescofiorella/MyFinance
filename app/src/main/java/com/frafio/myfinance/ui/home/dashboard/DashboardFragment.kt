package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import com.frafio.myfinance.ui.BaseFragment
import org.kodein.di.generic.instance

class DashboardFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel

    private val factory: DashboardViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentDashboardBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        viewModel.getStats()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}