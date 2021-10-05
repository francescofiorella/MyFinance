package com.frafio.myfinance.ui.home.menu

import android.os.Bundle
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
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        binding.collectionSwitch.also {
            it.isChecked = viewModel.isSwitchChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCollection(isChecked)
            }
        }

        return binding.root
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
    }

    override fun onCompleted(result: LiveData<PurchaseResult>) {
        result.observe(this, { purchaseResult ->
            when (purchaseResult.code) {
                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                    (activity as HomeActivity).hideProgressIndicator()
                }

                else -> (activity as HomeActivity).showSnackbar(purchaseResult.message)
            }
        })
    }
}