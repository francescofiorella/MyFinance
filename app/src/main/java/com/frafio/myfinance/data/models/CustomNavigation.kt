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
    firstTime: Boolean,
    private val animateTV: Boolean
) {
    companion object {
        private const val TRANSITION_DURATION: Long = 500
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
        if (firstTime) {
            setDashboardBlue()
        }
    }

    fun setDashboardBlue() {
        dashboardIcon.isSelected = true
        dashboardText.isSelected = true
        listIcon.isSelected = false
        listText.isSelected = false
        profileIcon.isSelected = false
        profileText.isSelected = false
        menuIcon.isSelected = false
        menuText.isSelected = false

        if (animateTV) {
            dashboardText.instantShow()
            listText.instantHide()
            profileText.instantHide()
            menuText.instantHide()
            animateLayout()
        }
    }

    private fun setOnClickListener() {
        dashboardLayout.setOnClickListener {
            onItem1ClickAction()

            _selectedItem = Item.ITEM_1

            dashboardIcon.isSelected = true
            dashboardText.isSelected = true
            listIcon.isSelected = false
            listText.isSelected = false
            profileIcon.isSelected = false
            profileText.isSelected = false
            menuIcon.isSelected = false
            menuText.isSelected = false

            if (animateTV) {
                dashboardText.instantShow()
                listText.instantHide()
                profileText.instantHide()
                menuText.instantHide()
                animateLayout()
            }
        }

        listLayout.setOnClickListener {
            onItem2ClickAction()

            _selectedItem = Item.ITEM_2

            dashboardIcon.isSelected = false
            dashboardText.isSelected = false
            listIcon.isSelected = true
            listText.isSelected = true
            profileIcon.isSelected = false
            profileText.isSelected = false
            menuIcon.isSelected = false
            menuText.isSelected = false

            if (animateTV) {
                dashboardText.instantHide()
                listText.instantShow()
                profileText.instantHide()
                menuText.instantHide()
                animateLayout()
            }
        }

        profileLayout.setOnClickListener {
            onItem3ClickAction()

            _selectedItem = Item.ITEM_3

            dashboardIcon.isSelected = false
            dashboardText.isSelected = false
            listIcon.isSelected = false
            listText.isSelected = false
            profileIcon.isSelected = true
            profileText.isSelected = true
            menuIcon.isSelected = false
            menuText.isSelected = false

            if (animateTV) {
                dashboardText.instantHide()
                listText.instantHide()
                profileText.instantShow()
                menuText.instantHide()
                animateLayout()
            }
        }

        menuLayout.setOnClickListener {
            onItem4ClickAction()

            _selectedItem = Item.ITEM_4

            dashboardIcon.isSelected = false
            dashboardText.isSelected = false
            listIcon.isSelected = false
            listText.isSelected = false
            profileIcon.isSelected = false
            profileText.isSelected = false
            menuIcon.isSelected = true
            menuText.isSelected = true

            if (animateTV) {
                dashboardText.instantHide()
                listText.instantHide()
                profileText.instantHide()
                menuText.instantShow()
                animateLayout()
            }
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
}