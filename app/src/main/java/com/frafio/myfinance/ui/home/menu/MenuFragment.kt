package com.frafio.myfinance.ui.home.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.utils.hide
import com.frafio.myfinance.utils.show
import org.eazegraph.lib.models.ValueLineSeries
import org.kodein.di.generic.instance

class MenuFragment : BaseFragment() {

    private lateinit var viewModel: MenuViewModel
    private lateinit var binding: FragmentMenuBinding

    private val factory: MenuViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        viewModel = ViewModelProvider(this, factory).get(MenuViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        setChartData()

        return binding.root
    }

    private fun setChartData() {
        binding.lineChart.clearChart()

        ValueLineSeries().also { series ->
            series.color = viewModel.getChartColor(requireContext())

            viewModel.addCharPointsTo(series)

            if (series.series.size < 2) {
                binding.chartCard.hide()
            } else {
                binding.chartCard.show()
                binding.lineChart.addSeries(series)
                binding.lineChart.startAnimation()
            }
        }
    }
}