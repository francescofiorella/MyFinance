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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.auth.AuthActivity
import com.frafio.myfinance.ui.features.home.HomeScreen
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import com.frafio.myfinance.ui.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.dateToExtendedString
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class HomeActivity : ComponentActivity(), HomeListener {

    private val viewModel by viewModels<HomeViewModel>()

    private var userRequest: Boolean = false
    private var isLayoutReady by mutableStateOf(false)
    private var showProgress by mutableStateOf(false)
    private val snackbarHostState = SnackbarHostState()

    private var addResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data!!
            val expenseRequest = data.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)
            val message = data.getStringExtra(AddActivity.ADD_RESULT_MESSAGE) ?: ""
            // totalId is used for scrolling, we might need a shared event for this now
            // val totalId = data.getStringExtra(AddActivity.ADD_RESULT_TOTAL_ID) ?: ""
            when (expenseRequest) {
                AddActivity.REQUEST_EXPENSE_CODE -> {
                    viewModel.navigateTo(MyFinanceNavKey.Expenses)
                    showSnackBar(message)
                }

                AddActivity.REQUEST_INCOME_CODE -> {
                    viewModel.navigateTo(MyFinanceNavKey.Budget)
                    showSnackBar(message)
                }
            }
        }
    }

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data?.getIntExtra(AddActivity.EXPENSE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_EXPENSE_CODE) {
                showSnackBar(FinanceCode.EXPENSE_EDIT_SUCCESS.message)
            } else if (editRequest == AddActivity.REQUEST_INCOME_CODE) {
                showSnackBar(FinanceCode.INCOME_EDIT_SUCCESS.message)
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        if (viewModel.isDynamicColorOn()) {
            DynamicColors.applyToActivityIfAvailable(this)
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (savedInstanceState == null) {
            userRequest = intent.extras?.getBoolean(AuthActivity.INTENT_USER_REQUEST, false) ?: false
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
        }

        viewModel.listener = this
        // Feature ViewModels are now managed by Navigation 3 entries

        if (savedInstanceState == null) {
            if (userRequest) {
                showProgressIndicator()
                viewModel.updateUserExpenses()
                viewModel.updateUserIncomes()
                viewModel.updateMonthlyBudget()
                viewModel.updateLabels()
                viewModel.updateLocalMonthlyBudget()
                viewModel.updateLocalLabels()
                viewModel.updateLocalUser()
                intent.extras?.getString(AuthActivity.INTENT_USER_NAME).also { userName ->
                    showSnackBar("${getString(R.string.login_successful)} $userName")
                }
                isLayoutReady = true
            } else {
                viewModel.checkUser()
            }
        } else {
            isLayoutReady = true
        }

        setContent {
            MyFinanceTheme {
                val appState = rememberMyFinanceAppState()
                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                HomeScreen(
                    appState = appState,
                    viewModel = viewModel,
                    showProgress = showProgress,
                    onAddClick = { onAddButtonClick() },
                    onLogoutClick = { viewModel.onLogoutButtonClick(View(this)) },
                    onProPicClick = { viewModel.navigateTo(MyFinanceNavKey.Profile) },
                    onEditExpense = { expense, position ->
                        Intent(this, AddActivity::class.java).also {
                            it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                            it.putExtra(AddActivity.EXPENSE_REQUEST_KEY, AddActivity.REQUEST_EXPENSE_CODE)
                            it.putExtra(AddActivity.EXTRA_TRANSACTION, expense)
                            it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                            editResultLauncher.launch(it)
                        }
                    },
                    onEditIncome = { income, position ->
                        Intent(this, AddActivity::class.java).also {
                            it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                            it.putExtra(AddActivity.EXPENSE_REQUEST_KEY, AddActivity.REQUEST_INCOME_CODE)
                            it.putExtra(AddActivity.EXTRA_TRANSACTION, income)
                            it.putExtra(AddActivity.EXPENSE_POSITION_KEY, position)
                            editResultLauncher.launch(it)
                        }
                    },
                    onDynamicColorChanged = { isChecked ->
                        viewModel.setDynamicColor(isChecked)
                        showSnackBar(
                            message = getString(R.string.restart_app_changes),
                            actionText = getString(R.string.restart),
                            actionFun = {
                                val intent = packageManager.getLaunchIntentForPackage(packageName)
                                val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
                                startActivity(mainIntent)
                                Runtime.getRuntime().exit(0)
                            }
                        )
                    },
                    getDateLabel = { start, end -> getDateChipLabel(start, end) }
                )
                }
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

    fun showSnackBar(
        message: String,
        actionText: String? = null,
        actionFun: () -> Unit = {},
        dismissFun: () -> Unit = {}
    ) {
        lifecycleScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionText,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                actionFun()
            } else {
                dismissFun()
            }
        }
    }

    override fun onLogOutSuccess(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            if (authResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                goToLoginActivity()
            }
        }
    }

    override fun onSplashOperationComplete(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    showProgressIndicator()
                    viewModel.updateUserExpenses()
                    viewModel.updateUserIncomes()
                    viewModel.updateMonthlyBudget()
                    viewModel.updateLabels()
                    viewModel.updateLocalMonthlyBudget()
                    viewModel.updateLocalLabels()
                    viewModel.updateLocalUser()
                    if (userRequest) {
                        hideProgressIndicator()
                        intent.extras?.getString(AuthActivity.INTENT_USER_NAME).also { userName ->
                            showSnackBar("${getString(R.string.login_successful)} $userName")
                        }
                    }
                    isLayoutReady = true
                }

                AuthCode.USER_NOT_LOGGED.code -> {
                    goToLoginActivity()
                }

                else -> Unit
            }
        }
    }

    override fun onUserDataUpdated(response: LiveData<FinanceResult>) {
        response.observe(this) { result ->
            when (result.code) {
                FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS.code -> {
                    hideProgressIndicator()
                }
                else -> Unit
            }
        }
    }

    private fun goToLoginActivity() {
        Intent(applicationContext, AuthActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun showProgressIndicator() {
        showProgress = true
    }

    fun hideProgressIndicator() {
        showProgress = false
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
