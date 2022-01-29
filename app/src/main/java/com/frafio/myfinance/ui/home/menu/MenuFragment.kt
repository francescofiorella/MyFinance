package com.frafio.myfinance.ui.home.menu

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.setValueLineChartData
import org.kodein.di.generic.instance

class MenuFragment : BaseFragment(), MenuListener {

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.listener = this

        binding.collectionSwitch.also {
            it.isChecked = viewModel.isSwitchCollectionChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCollection(isChecked)
            }
        }

        binding.dynamicColorSwitch.also {
            it.isChecked = viewModel.isSwitchDynamicColorChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDynamicColor(isChecked)
                (activity as HomeActivity).showSnackBar(getString(R.string.restart_app_changes))
            }
        }
        return binding.root
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
        binding.collectionSwitch.isEnabled = false
    }

    override fun onCompleted(result: LiveData<PurchaseResult>) {
        result.observe(this) { purchaseResult ->
            (activity as HomeActivity).hideProgressIndicator()
            binding.collectionSwitch.isEnabled = true

            when (purchaseResult.code) {
                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {

                    binding.lineChart.also { lineChart ->
                        val list = viewModel.avgTrendList

                        if (list.size < 2) {
                            animateRoot()
                            binding.chartCard.instantHide()
                        } else {
                            animateRoot()
                            binding.chartCard.instantShow()
                        }

                        lineChart.clearChart()
                        setValueLineChartData(lineChart, list)
                    }
                }

                else -> {
                    (activity as HomeActivity).showSnackBar(purchaseResult.message)
                }
            }
        }
    }

    private fun animateRoot() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        val transition = AutoTransition()
        transition.duration = 2000
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
    }
}