package com.frafio.myfinance.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.ui.auth.LoginActivity
import com.frafio.myfinance.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MenuFragment : Fragment() {

    lateinit var mLogoutBtn: MaterialButton
    lateinit var mAppVersionTV: TextView

    companion object {
        private val TAG = MenuFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_menu, container, false)

        mLogoutBtn = view.findViewById(R.id.menu_logoutBtn)
        mAppVersionTV = view.findViewById(R.id.menu_appVersion_TV)

        mLogoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(context, LoginActivity::class.java))
            activity!!.finish()
        }

        mAppVersionTV.text = "MyFinance ${BuildConfig.VERSION_NAME}"
        return view
    }
}