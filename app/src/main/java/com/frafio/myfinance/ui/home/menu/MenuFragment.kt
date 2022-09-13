package com.frafio.myfinance.ui.home.menu

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.setValueLineChartData
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MenuFragment : BaseFragment(), MenuListener {

    private val viewModel by viewModels<MenuViewModel>()
    private lateinit var binding: FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.listener = this

        val yearArray = resources.getStringArray(R.array.years)
        val selectedCollection = when (viewModel.getSelectedCollection()) {
            DbPurchases.COLLECTIONS.ZERO_ONE.value -> 0
            DbPurchases.COLLECTIONS.ONE_TWO.value -> 1
            DbPurchases.COLLECTIONS.TWO_THREE.value -> 2
            else -> 0 // error, do not select a default option
        }
        binding.collectionTV.text = getString(R.string.year, yearArray[selectedCollection])

        binding.collectionCard.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setIcon(R.drawable.ic_visibility)
            builder.setTitle(getString(R.string.year, ""))
            builder.setSingleChoiceItems(
                yearArray,
                when (viewModel.getSelectedCollection()) {
                    DbPurchases.COLLECTIONS.ZERO_ONE.value -> 0
                    DbPurchases.COLLECTIONS.ONE_TWO.value -> 1
                    DbPurchases.COLLECTIONS.TWO_THREE.value -> 2
                    else -> -1 // error, do not select a default option
                }
            ) { dialog, selectedItem ->
                val collection = when (selectedItem) {
                    0 -> DbPurchases.COLLECTIONS.ZERO_ONE.value
                    1 -> DbPurchases.COLLECTIONS.ONE_TWO.value
                    2 -> DbPurchases.COLLECTIONS.TWO_THREE.value
                    else -> DbPurchases.COLLECTIONS.ZERO_ONE.value
                }
                viewModel.setCollection(collection)
                binding.collectionTV.text = getString(R.string.year, yearArray[selectedItem])
                dialog.dismiss()
            }
            builder.show()
        }

        binding.dynamicColorSwitch.also {
            it.isChecked = viewModel.isSwitchDynamicColorChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDynamicColor(isChecked)
                (activity as HomeActivity).showSnackBar(
                    getString(R.string.restart_app_changes),
                    false
                ).also { snackBar ->
                    snackBar.setAction(getString(R.string.restart)) {
                        (activity as HomeActivity).finish()
                    }

                    snackBar.show()
                }
            }
        }
        return binding.root
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
    }

    override fun onCompleted(result: LiveData<PurchaseResult>) {
        result.observe(this) { purchaseResult ->
            (activity as HomeActivity).hideProgressIndicator()

            when (purchaseResult.code) {
                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                    refreshPlotData(animate = true)
                    (activity as HomeActivity).refreshFragmentData(dashboard = true, list = true)
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

    fun refreshPlotData(animate: Boolean = false) {
        binding.lineChart.also { lineChart ->
            val list = viewModel.avgTrendList

            if (animate) {
                animateRoot()
            }

            if (list.size < 2) {
                binding.chartCard.instantHide()
            } else {
                binding.chartCard.instantShow()
            }

            lineChart.clearChart()
            setValueLineChartData(lineChart, list)
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.menuScrollView.scrollTo(0, 0)
    }
}