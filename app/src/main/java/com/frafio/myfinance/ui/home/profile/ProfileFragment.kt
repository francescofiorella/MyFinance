package com.frafio.myfinance.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ProfileFragment : Fragment(), KodeinAware {

    private lateinit var mUserImage: ImageView

    private lateinit var viewModel: ProfileViewModel

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
    }

    override val kodein by kodein()
    private val factory: ProfileViewModelFactory by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        // collegamento view
        mUserImage = binding.root.findViewById(R.id.profile_propic_iv)

        FirebaseAuth.getInstance().currentUser?.let { fUser ->
            fUser.photoUrl?.let { uri ->
                context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions.circleCropTransform()).into(mUserImage) }
            }
        }

        return binding.root
    }
}