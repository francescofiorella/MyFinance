package com.frafio.myfinance.ui.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.AuthActivity
import com.frafio.myfinance.ui.features.home.HomeScreen
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import com.frafio.myfinance.ui.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.dateToExtendedString
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val viewModel by viewModels<HomeViewModel>()

    private var userRequest: Boolean = false
    private var isLayoutReady by mutableStateOf(false)

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data!!
            val expenseRequest = data.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)
            val message = data.getStringExtra(AddActivity.ADD_RESULT_MESSAGE) ?: ""
            when (expenseRequest) {
                AddActivity.REQUEST_EXPENSE_CODE -> {
                    viewModel.navigateTo(MyFinanceNavKey.Expenses)
                    viewModel.showSnackBar(message)
                }

                AddActivity.REQUEST_INCOME_CODE -> {
                    viewModel.navigateTo(MyFinanceNavKey.Budget)
                    viewModel.showSnackBar(message)
                }
            }
        }
    }

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data?.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_EXPENSE_CODE) {
                viewModel.showSnackBar(FinanceCode.EXPENSE_EDIT_SUCCESS.message)
            } else if (editRequest == AddActivity.REQUEST_INCOME_CODE) {
                viewModel.showSnackBar(FinanceCode.INCOME_EDIT_SUCCESS.message)
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

        if (viewModel.isDynamicColorOn()) {
            DynamicColors.applyToActivityIfAvailable(this)
        }

        enableEdgeToEdge()

        lifecycleScope.launch {
            viewModel.logicEvents.collect { event ->
                when (event) {
                    HomeLogicEvent.UserLocalDataLoaded -> {
                        isLayoutReady = true
                    }

                    HomeLogicEvent.UserNotLogged, HomeLogicEvent.LogoutSuccess -> {
                        goToLoginActivity()
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            userRequest =
                intent.extras?.getBoolean(AuthActivity.INTENT_USER_REQUEST, false) ?: false
            if (!userRequest) {
                splashScreen.apply {
                    setKeepOnScreenCondition { !isLayoutReady }
                    setOnExitAnimationListener { splashScreenViewProvider ->
                        val fadeOut = ObjectAnimator.ofFloat(
                            splashScreenViewProvider.view,
                            View.ALPHA,
                            1f,
                            0f,
                        ).apply {
                            interpolator = LinearInterpolator()
                            duration = 200L
                        }
                        fadeOut.doOnEnd { splashScreenViewProvider.remove() }
                        fadeOut.start()
                    }
                }
            }

            viewModel.checkUser(userRequest)
        } else {
            isLayoutReady = true
        }

        setContent {
            MyFinanceTheme {
                val appState = rememberMyFinanceAppState()
                // show progress bar as it is loading data
                // this is connected to viewModel.checkUser(userRequest)
                // the ui event might be lost as it is called before the composition
                if (savedInstanceState == null) appState.showProgress = true
                HomeScreen(
                    appState = appState,
                    viewModel = viewModel,
                    onAddClick = { onAddButtonClick() },
                    onLogoutClick = { viewModel.onLogoutButtonClick(View(this)) },
                    onProPicClick = { viewModel.navigateTo(MyFinanceNavKey.Profile) },
                    onEditExpense = { expense, position ->
                        Intent(this, AddActivity::class.java).also {
                            it.putExtra(
                                AddActivity.REQUEST_CODE_KEY,
                                AddActivity.REQUEST_EDIT_CODE
                            )
                            it.putExtra(
                                AddActivity.EXPENSE_REQUEST_KEY,
                                AddActivity.REQUEST_EXPENSE_CODE
                            )
                            it.putExtra(AddActivity.EXTRA_TRANSACTION, expense)
                            it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                            editResultLauncher.launch(it)
                        }
                    },
                    onEditIncome = { income, position ->
                        Intent(this, AddActivity::class.java).also {
                            it.putExtra(
                                AddActivity.REQUEST_CODE_KEY,
                                AddActivity.REQUEST_EDIT_CODE
                            )
                            it.putExtra(
                                AddActivity.EXPENSE_REQUEST_KEY,
                                AddActivity.REQUEST_INCOME_CODE
                            )
                            it.putExtra(AddActivity.EXTRA_TRANSACTION, income)
                            it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                            editResultLauncher.launch(it)
                        }
                    },
                    getDateLabel = { start, end -> getDateChipLabel(start, end) },
                    restartApplication = {
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
                        startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                )
            }
        }
    }

    private fun onAddButtonClick() {
        Intent(applicationContext, AddActivity::class.java).also {
            it.putExtra(
                AddActivity.REQUEST_CODE_KEY,
                AddActivity.REQUEST_ADD_CODE
            )
            addResultLauncher.launch(it)
        }
    }

    private fun goToLoginActivity() {
        Intent(applicationContext, AuthActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun getDateChipLabel(startDate: LocalDate, endDate: LocalDate): String {
        return when (startDate.year) {
            endDate.year -> {
                if (startDate.monthValue == endDate.monthValue) {
                    val startDayOfMonth =
                        if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                    "$startDayOfMonth - ${dateToExtendedString(endDate)}"
                } else {
                    val startDayOfMonth =
                        if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth.toString()
                    val startMonth =
                        startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            .replaceFirstChar { it.uppercase() }
                    "$startDayOfMonth $startMonth - ${dateToExtendedString(endDate)}"
                }
            }

            else -> "${dateToExtendedString(startDate)} - ${dateToExtendedString(endDate)}"
        }
    }
}
