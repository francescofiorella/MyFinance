package com.frafio.myfinance.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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

fun Activity.snackBar(
    message: String,
    anchorView: View? = null,
    actionText: String? = null,
    actionFun: () -> Unit = {}
): Snackbar {
    val root = findViewById<ViewGroup>(android.R.id.content).rootView
    val nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

    Snackbar.make(root, message, BaseTransientBottomBar.LENGTH_SHORT).also { snackBar ->
        anchorView?.let {
            snackBar.setAnchorView(it)
        }

        snackBar.view.background = AppCompatResources.getDrawable(this, R.drawable.bg_round_corners)

        val tv = snackBar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito

        actionText?.also { text ->
            snackBar.setAction(text) {
                actionFun()
            }
        }

        snackBar.show()

        return snackBar
    }
}

fun Context.hideSoftKeyboard(view: View) {
    val inputMethodManager =
        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}