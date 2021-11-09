package com.frafio.myfinance.data.models

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow

open class CustomNavigation(
    private val root: ViewGroup,
    private val dashboardLayout: ConstraintLayout,
    private val dashboardIcon: ImageView,
    private val dashboardText: TextView,
    private val listLayout: ConstraintLayout,
    private val listIcon: ImageView,
    private val listText: TextView,
    private val profileLayout: ConstraintLayout,
    private val profileIcon: ImageView,
    private val profileText: TextView,
    private val menuLayout: ConstraintLayout,
    private val menuIcon: ImageView,
    private val menuText: TextView,
    private val animateTV: Boolean
) {
    companion object {
        private const val TRANSITION_DURATION: Long = 250
    }

    private var _selectedItem: Item = Item.ITEM_1
    var selectedItem: Item
        get() = _selectedItem
        set(value) {
            when (value) {
                Item.ITEM_1 -> dashboardLayout.performClick()

                Item.ITEM_2 -> listLayout.performClick()

                Item.ITEM_3 -> profileLayout.performClick()

                Item.ITEM_4 -> menuLayout.performClick()
            }
            _selectedItem = value
        }

    init {
        setOnClickListener()
        checkDashboard()
    }

    private fun setOnClickListener() {
        dashboardLayout.setOnClickListener {
            onItem1ClickAction()

            checkDashboard()
        }

        listLayout.setOnClickListener {
            onItem2ClickAction()

            checkList()
        }

        profileLayout.setOnClickListener {
            onItem3ClickAction()

            checkProfile()
        }

        menuLayout.setOnClickListener {
            onItem4ClickAction()

            checkMenu()
        }
    }

    open fun onItem1ClickAction() {}

    open fun onItem2ClickAction() {}

    open fun onItem3ClickAction() {}

    open fun onItem4ClickAction() {}

    enum class Item {
        ITEM_1,
        ITEM_2,
        ITEM_3,
        ITEM_4
    }

    private fun animateLayout() {
        TransitionManager.beginDelayedTransition(root)
        val transition = AutoTransition()
        transition.duration = TRANSITION_DURATION
        TransitionManager.beginDelayedTransition(root, transition)
    }

    fun checkDashboard() {
        dashboardIcon.isSelected = true
        dashboardText.isSelected = true
        listIcon.isSelected = false
        listText.isSelected = false
        profileIcon.isSelected = false
        profileText.isSelected = false
        menuIcon.isSelected = false
        menuText.isSelected = false

        _selectedItem = Item.ITEM_1

        if (animateTV) {
            dashboardText.instantShow()
            listText.instantHide()
            profileText.instantHide()
            menuText.instantHide()
            animateLayout()
        }
    }

    private fun checkList() {
        dashboardIcon.isSelected = false
        dashboardText.isSelected = false
        listIcon.isSelected = true
        listText.isSelected = true
        profileIcon.isSelected = false
        profileText.isSelected = false
        menuIcon.isSelected = false
        menuText.isSelected = false

        _selectedItem = Item.ITEM_2

        if (animateTV) {
            dashboardText.instantHide()
            listText.instantShow()
            profileText.instantHide()
            menuText.instantHide()
            animateLayout()
        }
    }

    private fun checkProfile() {
        dashboardIcon.isSelected = false
        dashboardText.isSelected = false
        listIcon.isSelected = false
        listText.isSelected = false
        profileIcon.isSelected = true
        profileText.isSelected = true
        menuIcon.isSelected = false
        menuText.isSelected = false

        _selectedItem = Item.ITEM_3

        if (animateTV) {
            dashboardText.instantHide()
            listText.instantHide()
            profileText.instantShow()
            menuText.instantHide()
            animateLayout()
        }
    }

    private fun checkMenu() {
        dashboardIcon.isSelected = false
        dashboardText.isSelected = false
        listIcon.isSelected = false
        listText.isSelected = false
        profileIcon.isSelected = false
        profileText.isSelected = false
        menuIcon.isSelected = true
        menuText.isSelected = true

        _selectedItem = Item.ITEM_4

        if (animateTV) {
            dashboardText.instantHide()
            listText.instantHide()
            profileText.instantHide()
            menuText.instantShow()
            animateLayout()
        }
    }
}