package com.frafio.myfinance.ui

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    open fun scrollUp() = Unit
}