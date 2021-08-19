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
import org.kodein.di.generic.instance

class MenuFragment : BaseFragment() {

    private lateinit var viewModel: MenuViewModel

    private val factory: MenuViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMenuBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        viewModel = ViewModelProvider(this, factory).get(MenuViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}