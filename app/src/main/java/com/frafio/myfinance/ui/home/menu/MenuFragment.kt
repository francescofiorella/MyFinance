package com.frafio.myfinance.ui.home.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.AUTH_RESULT
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.databinding.FragmentMenuBinding
import com.frafio.myfinance.ui.auth.AuthListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MenuFragment : Fragment(), AuthListener, KodeinAware {

    private lateinit var viewModel: MenuViewModel

    override val kodein by kodein()
    private val factory: MenuViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMenuBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        viewModel = ViewModelProvider(this, factory).get(MenuViewModel::class.java)
        viewModel.authListener = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onAuthStarted() = Unit

    override fun onAuthSuccess(response: LiveData<AuthResult>) {
        response.observe(this, { authResult ->
            if (authResult.code == AUTH_RESULT.LOGOUT_SUCCESS.code) {
                Intent(context, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        })
    }

    override fun onAuthFailure(authResult: AuthResult) = Unit
}