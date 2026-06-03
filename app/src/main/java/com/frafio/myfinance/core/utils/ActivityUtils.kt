package com.frafio.myfinance.core.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.frafio.myfinance.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

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

        val tv = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        tv.typeface = nunito

        actionText?.also { text ->
            snackBar.setAction(text) {
                actionFun()
            }

            val actionView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            actionView.typeface = nunito
        }

        snackBar.show()

        return snackBar
    }
}