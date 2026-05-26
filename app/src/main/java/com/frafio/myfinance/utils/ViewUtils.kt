package com.frafio.myfinance.utils

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

fun View.instantShow() {
    visibility = View.VISIBLE
}

fun View.instantHide() {
    visibility = View.GONE
}

fun TextView.clearText() {
    text = ""
}

fun ViewGroup.animateRoot(duration: Long? = 200) {
    val transition = AutoTransition()
    duration?.let { transition.duration = it }
    TransitionManager.beginDelayedTransition(this, transition)
}
