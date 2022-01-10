package com.frafio.myfinance.ui.home.menu

import android.content.DialogInterface
import android.content.Intent
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
import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.data.enums.db.PurchaseCodeIT
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import com.frafio.myfinance.utils.setValueLineChartData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance
import java.util.*

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
            it.isChecked = viewModel.isSwitchChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCollection(isChecked)
            }
        }

        binding.languageCard.setOnClickListener(languageCardListener)
        return binding.root
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
        binding.collectionSwitch.isEnabled = false
    }

    private val languageCardListener = View.OnClickListener {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setIcon(R.drawable.ic_language)
        builder.setTitle(getString(R.string.language))
        builder.setSingleChoiceItems(
            resources.getStringArray(R.array.languages),
            when (viewModel.getLanguage()) {
                Languages.ENGLISH.value -> 0
                Languages.ITALIANO.value -> 1
                else -> -1 // error, do not select a default option
            },
            languageListener
        )
        builder.show()
    }

    private val languageListener = DialogInterface.OnClickListener { dialog, selectedItem ->
        val language: String = when (selectedItem) {
            0 -> Languages.ENGLISH.value
            1 -> Languages.ITALIANO.value
            else -> Languages.ENGLISH.value
        }
        viewModel.setLanguage(language)

        val locale = Locale(language)
        val displayMetrics = resources.displayMetrics
        Locale.setDefault(locale)
        resources.configuration.setLocale(locale)
        resources.updateConfiguration(resources.configuration, displayMetrics)
        dialog.dismiss()
    }

    override fun onCompleted(result: LiveData<PurchaseResult>) {
        result.observe(this, { purchaseResult ->
            (activity as HomeActivity).hideProgressIndicator()
            binding.collectionSwitch.isEnabled = true

            when (purchaseResult.code) {
                PurchaseCodeIT.PURCHASE_LIST_UPDATE_SUCCESS.code -> {

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
        })
    }

    private fun animateRoot() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        val transition = AutoTransition()
        transition.duration = 2000
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
    }
}