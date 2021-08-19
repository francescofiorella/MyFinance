package com.frafio.myfinance.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.FragmentProfileBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.auth.LoginActivity
import org.kodein.di.generic.instance

class ProfileFragment : BaseFragment(), ProfileListener {

    private lateinit var viewModel: ProfileViewModel

    private val factory: ProfileViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentProfileBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
        viewModel.profileListener = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                Intent(context, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        })
    }
}