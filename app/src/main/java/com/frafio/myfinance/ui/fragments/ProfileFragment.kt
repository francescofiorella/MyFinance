package com.frafio.myfinance.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frafio.myfinance.ui.home.MainActivity.Companion.CURRENT_USER
import com.frafio.myfinance.R

class ProfileFragment : Fragment() {

    lateinit var mUserImage: ImageView
    lateinit var mUserNameTv: TextView
    lateinit var mEmailTv:TextView

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        // collegamento view
        mUserImage = view.findViewById(R.id.profile_propic_iv)
        mUserNameTv = view.findViewById(R.id.profile_username_tv)
        mEmailTv = view.findViewById(R.id.profile_email_tv)

        setUserData()

        return view
    }

    private fun setUserData() {
        mUserNameTv.text = CURRENT_USER?.fullName
        mEmailTv.text = CURRENT_USER?.email
        if (CURRENT_USER?.image != "") {
            Glide.with(context!!).load(CURRENT_USER?.image).apply(RequestOptions.circleCropTransform()).into(mUserImage)
        }
    }
}