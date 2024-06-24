package com.frafio.myfinance.ui.home.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentBudgetBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.hideSoftKeyboard

class BudgetFragment : BaseFragment() {
    private lateinit var binding: FragmentBudgetBinding
    private val viewModel by viewModels<BudgetViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_budget, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.monthlyBudgetEditBtn.setOnClickListener {
            if (binding.monthlyBudgetTV.isVisible) {
                // show edit interface
                binding.monthlyBudgetET.setText(doubleToString(viewModel.monthlyBudget!!))
                binding.monthlyBudgetTV.visibility = View.INVISIBLE
                binding.monthlyBudgetET.visibility = View.VISIBLE
                binding.monthlyBudgetEditBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
                binding.monthlyBudgetDeleteBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
                binding.monthlyBudgetDeleteBtn.isEnabled = false
            } else {
                // cancel any modifications
                binding.monthlyBudgetTV.visibility = View.VISIBLE
                binding.monthlyBudgetET.visibility = View.INVISIBLE
                requireContext().hideSoftKeyboard(requireView().rootView)
                binding.monthlyBudgetEditBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_create)
                binding.monthlyBudgetDeleteBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                binding.monthlyBudgetDeleteBtn.isEnabled = true
            }
        }

        binding.monthlyBudgetDeleteBtn.setOnClickListener {
            if (binding.monthlyBudgetTV.isVisible) {
                // delete budget
            } else {
                // modify budget
                binding.monthlyBudgetTV.visibility = View.VISIBLE
                binding.monthlyBudgetET.visibility = View.INVISIBLE
                requireContext().hideSoftKeyboard(requireView().rootView)
                binding.monthlyBudgetEditBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_create)
                binding.monthlyBudgetDeleteBtn.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                binding.monthlyBudgetDeleteBtn.isEnabled = true
            }
        }

        return binding.root
    }
}