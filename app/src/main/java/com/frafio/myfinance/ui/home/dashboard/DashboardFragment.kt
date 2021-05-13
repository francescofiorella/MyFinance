package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentDashboardBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class DashboardFragment : Fragment(), KodeinAware {

    private lateinit var viewModel: DashboardViewModel

    override val kodein by kodein()
    private val factory: DashboardViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentDashboardBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)

        viewModel.getStats()

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}