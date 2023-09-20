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
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.getSharedDynamicColor
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

        binding.actualCollectionTV.text = viewModel.getSelectedCategory()

        binding.collectionCard.setOnClickListener {
            viewModel.getCategories()
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

        val dynamicColors = getSharedDynamicColor(
            (requireActivity().application as MyFinanceApplication).sharedPreferences
        )
        binding.lineChart.setValueLineChartData(viewModel.avgTrendList, dynamicColors)
        return binding.root
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
    }

    override fun <T> onCompleted(result: LiveData<T>) {
        result.observe(this) { value ->
            (activity as HomeActivity).hideProgressIndicator()

            if (value is PurchaseResult) {
                when (value.code) {
                    PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                        refreshPlotData(animate = true)
                        (activity as HomeActivity).refreshFragmentData(
                            dashboard = true,
                            payments = true
                        )
                    }

                    else -> {
                        (activity as HomeActivity).showSnackBar(value.message)
                    }
                }
            } else if (value is Pair<*, *>) {
                val purchaseResult = value.first
                val categories = value.second
                if (purchaseResult is PurchaseResult && categories is List<*>) {
                    when (purchaseResult.code) {
                        PurchaseCode.PURCHASE_GET_CATEGORIES_SUCCESS.code -> {
                            showCategoriesDialog(categories as List<String>)
                        }

                        else -> {
                            (activity as HomeActivity).showSnackBar(purchaseResult.message)
                        }
                    }
                } else {
                    (activity as HomeActivity).showSnackBar(getString(R.string.generic_error))
                }
            } else {
                (activity as HomeActivity).showSnackBar(getString(R.string.generic_error))
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
            val dynamicColors = getSharedDynamicColor(
                (requireActivity().application as MyFinanceApplication).sharedPreferences
            )
            binding.lineChart.setValueLineChartData(list, dynamicColors)
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.menuScrollView.scrollTo(0, 0)
    }

    private fun showCategoriesDialog(categories : List<String>) {
        var builder = MaterialAlertDialogBuilder(requireContext())
        builder.setIcon(R.drawable.ic_auto_awesome_motion)
        builder.setTitle(getString(R.string.categories))
        builder.setSingleChoiceItems(
            categories.toTypedArray(),
            categories.indexOf(viewModel.getSelectedCategory())
        ) { dialog, selectedItem ->
            val collection = categories[selectedItem]
            viewModel.setCollection(collection)
            binding.actualCollectionTV.text = collection
            dialog.dismiss()
        }
        /*builder.setPositiveButton(getString(R.string.create)) { upperDialog, _ ->
            upperDialog.dismiss()
            builder = MaterialAlertDialogBuilder(requireContext())
            builder.setIcon(R.drawable.ic_auto_awesome_motion)
            builder.setTitle(getString(R.string.create_category))
            builder.setNegativeButton(getString(R.string.cancel), null)
            builder.setPositiveButton(getString(R.string.create)) { lowerDialog, _ ->
                lowerDialog.dismiss()
            }
            builder.show()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)*/
        builder.show()
    }
}