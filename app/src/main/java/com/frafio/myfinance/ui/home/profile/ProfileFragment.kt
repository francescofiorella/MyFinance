package com.frafio.myfinance.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.frafio.myfinance.R
import androidx.fragment.app.viewModels
import com.frafio.myfinance.databinding.FragmentProfileBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.utils.dateToString

class ProfileFragment : BaseFragment() {

    private val viewModel by viewModels<ProfileViewModel>()
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_profile, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.user?.apply {
            binding.signupDateTV.text = getString(R.string.signUpDate, dateToString(creationDay, creationMonth, creationYear))
        }

        return binding.root
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.profileScrollView.scrollTo(0, 0)
    }
}