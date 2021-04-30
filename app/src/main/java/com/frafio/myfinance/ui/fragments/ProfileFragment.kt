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

import com.frafio.myfinance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ProfileFragment : Fragment() {

    lateinit var mUserImage: ImageView
    lateinit var mUserNameTv: TextView
    lateinit var mEmailTv: TextView

    private var fUser: FirebaseUser? = null

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        // collegamento view
        mUserImage = view.findViewById(R.id.profile_propic_iv)
        mUserNameTv = view.findViewById(R.id.profile_username_tv)
        mEmailTv = view.findViewById(R.id.profile_email_tv)

        fUser = FirebaseAuth.getInstance().currentUser

        setUserData()

        return view
    }

    private fun setUserData() {
        mUserNameTv.text = fUser?.displayName
        mEmailTv.text = fUser?.email
        fUser?.photoUrl?.let { uri ->
            Glide.with(context!!).load(uri.toString()).apply(RequestOptions.circleCropTransform()).into(mUserImage)
        }
    }
}