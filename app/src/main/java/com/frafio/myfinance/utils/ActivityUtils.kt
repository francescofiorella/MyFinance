package com.frafio.myfinance.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.frafio.myfinance.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun Activity.setFullScreenEnabled(enable: Boolean) {
    if (enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.show(
                android.view.WindowInsets.Type.statusBars()
            )
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            )
        }
    }
}

fun Activity.snackbar(message: String, anchor: View? = null) {
    val root = findViewById<ViewGroup>(android.R.id.content).rootView

    val nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

    val snackbar = Snackbar.make(root, message, BaseTransientBottomBar.LENGTH_SHORT)

    anchor?.let{
        snackbar.setAnchorView(it)
    }

    val tv = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
    tv.typeface = nunito

    snackbar.show()
}