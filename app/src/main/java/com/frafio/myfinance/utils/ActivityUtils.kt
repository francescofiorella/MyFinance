package com.frafio.myfinance.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.frafio.myfinance.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


@Suppress("UNUSED")
fun Activity.setFullScreenEnabled(enable: Boolean) {
    if (enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        } else {
            @Suppress("DEPRECATION")
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
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            )
        }
    }
}

fun Activity.snackBar(message: String, anchor: View? = null, show: Boolean = true) : Snackbar {
    val root = findViewById<ViewGroup>(android.R.id.content).rootView
    val nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

    Snackbar.make(root, message, BaseTransientBottomBar.LENGTH_SHORT).also { snackBar ->
        anchor?.let {
            snackBar.setAnchorView(it)
        }

        snackBar.view.background = AppCompatResources.getDrawable(this, R.drawable.bg_snackbar)

        val tv = snackBar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito

        if (show)
            snackBar.show()

        return snackBar
    }
}