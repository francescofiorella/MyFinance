package com.frafio.myfinance.utils

import android.view.View
import android.widget.TextView

fun View.instantShow() {
    visibility = View.VISIBLE
}

fun View.instantHide() {
    visibility = View.GONE
}

fun TextView.clearText() {
    text = ""
}