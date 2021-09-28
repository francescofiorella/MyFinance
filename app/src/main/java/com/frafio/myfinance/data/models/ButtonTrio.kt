package com.frafio.myfinance.data.models

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.GridLayout
import android.widget.TextView
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow

open class ButtonTrio(
    private val layout: GridLayout,
    private val btn1: TextView,
    private val btn2: TextView,
    private val btn3: TextView
) {
    companion object {
        private const val ANIMATION_DURATION: Long = 1500
        private const val TRANSITION_DURATION: Long = 2000
    }

    /* inizializzato a 3 in modo da poter selezionare subito Btn1
    mantenendo l'if che controlla se è già selezionato o meno */
    private var _selectedBtn: Button = Button.BUTTON_3
    var selectedBtn: Button
        get() = _selectedBtn
        set(value) {
            when (value) {
                Button.BUTTON_1 -> btn1.performClick()

                Button.BUTTON_2 -> btn2.performClick()

                Button.BUTTON_3 -> btn3.performClick()
            }
            _selectedBtn = value
        }

    var isEnabled: Boolean = true
        set(value) {
            if (value) {
                enableButtons()
                setOnClickListener()
            } else {
                removeOnClickListener()
                disableButtons()
            }

            field = value
        }

    private var _isVisible: Boolean = true
    var isVisible: Boolean
        get() = _isVisible
        set(value) {
            _isVisible = value
            if (value) {
                layout.alpha = 1f
            } else {
                layout.alpha = 0f
            }
        }

    init {
        setOnClickListener()
        selectedBtn = Button.BUTTON_1
    }

    fun enableOnlySelectedBtn() {
        when (_selectedBtn) {
            Button.BUTTON_1 -> {
                isEnabled = false
                btn1.isEnabled = true

                btn1.isSelected = true
            }

            Button.BUTTON_2 -> {
                isEnabled = false
                btn2.isEnabled = true

                btn2.isSelected = true
            }

            Button.BUTTON_3 -> {
                isEnabled = false
                btn3.isEnabled = true

                btn3.isSelected = true
            }
        }
    }

    fun show(root: ViewGroup? = null) {
        if (!_isVisible) {
            val interpolator = OvershootInterpolator()

            layout.animate().setInterpolator(interpolator).alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .start()
            layout.instantShow()

            root?.let {
                TransitionManager.beginDelayedTransition(root)
                val transition = AutoTransition()
                transition.duration = TRANSITION_DURATION
                TransitionManager.beginDelayedTransition(root, transition)
            }

            _isVisible = true
        }
    }

    fun hide(root: ViewGroup? = null) {
        if (_isVisible) {
            val interpolator = OvershootInterpolator()

            layout.animate().setInterpolator(interpolator).alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .start()
            layout.instantHide()

            root?.let {
                TransitionManager.beginDelayedTransition(root)
                val transition = AutoTransition()
                transition.duration = TRANSITION_DURATION
                TransitionManager.beginDelayedTransition(root, transition)
            }

            _isVisible = false
        }
    }

    fun performClick() {
        // imposta _selectedBtn ad un altro valore in modo da effettuare il click
        when (_selectedBtn) {
            Button.BUTTON_1 -> {
                _selectedBtn = Button.BUTTON_3
                selectedBtn = Button.BUTTON_1
            }

            Button.BUTTON_2 -> {
                _selectedBtn = Button.BUTTON_3
                selectedBtn = Button.BUTTON_2
            }

            Button.BUTTON_3 -> {
                _selectedBtn = Button.BUTTON_1
                selectedBtn = Button.BUTTON_3
            }
        }
    }

    private fun setOnClickListener() {
        btn1.setOnClickListener {
            if (_selectedBtn != Button.BUTTON_1) {
                onBtn1ClickAction()

                _selectedBtn = Button.BUTTON_1

                btn1.isSelected = true
                btn2.isSelected = false
                btn3.isSelected = false
            }
        }

        btn2.setOnClickListener {
            if (_selectedBtn != Button.BUTTON_2) {
                onBtn2ClickAction()

                _selectedBtn = Button.BUTTON_2

                btn1.isSelected = false
                btn2.isSelected = true
                btn3.isSelected = false
            }
        }

        btn3.setOnClickListener {
            if (_selectedBtn != Button.BUTTON_3) {
                onBtn3ClickAction()

                _selectedBtn = Button.BUTTON_3

                btn1.isSelected = false
                btn2.isSelected = false
                btn3.isSelected = true
            }
        }
    }

    open fun onBtn1ClickAction() {}

    open fun onBtn2ClickAction() {}

    open fun onBtn3ClickAction() {}

    private fun removeOnClickListener() {
        btn1.setOnClickListener(null)
        btn2.setOnClickListener(null)
        btn3.setOnClickListener(null)
    }

    private fun enableButtons() {
        btn1.isEnabled = true
        btn2.isEnabled = true
        btn3.isEnabled = true
    }

    private fun disableButtons() {
        btn1.isEnabled = false
        btn2.isEnabled = false
        btn3.isEnabled = false
    }

    enum class Button {
        BUTTON_1,
        BUTTON_2,
        BUTTON_3
    }
}