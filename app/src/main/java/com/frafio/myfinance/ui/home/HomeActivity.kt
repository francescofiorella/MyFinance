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
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.ui.auth.AuthActivity
import com.frafio.myfinance.ui.features.add.navigation.addEntry
import com.frafio.myfinance.ui.features.home.HomeScreen
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.RootKey
import com.frafio.myfinance.ui.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.dateToExtendedString
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var loadingRepository: LoadingRepository

    private var userRequest: Boolean = false
    private var isLayoutReady by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

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

        lifecycleScope.launch {
            viewModel.userPreferences.collect { prefs ->
                if (prefs != null) {
                    // Re-apply if dynamic color changed
                    if (prefs.dynamicColor) {
                        DynamicColors.applyToActivityIfAvailable(this@HomeActivity)
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            userRequest =
                intent.extras?.getBoolean(AuthActivity.INTENT_USER_REQUEST, false) ?: false
            if (!userRequest) {
                splashScreen.apply {
                    setKeepOnScreenCondition { !isLayoutReady || viewModel.userPreferences.value == null }
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
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val useDynamicColor = userPreferences?.dynamicColor ?: false
            val isLoading by loadingRepository.isLoading.collectAsStateWithLifecycle()

            MyFinanceTheme(dynamicColor = useDynamicColor) {
                val appState = rememberMyFinanceAppState()
                val rootBackStack = rememberNavBackStack(RootKey.Home)

                LaunchedEffect(isLoading) {
                    appState.showProgress = isLoading
                }

                val decorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator()
                )
                val rootEntries = rememberDecoratedNavEntries(
                    backStack = rootBackStack,
                    entryDecorators = decorators,
                    entryProvider = entryProvider {
                        entry<RootKey.Home> {
                            HomeScreen(
                                appState = appState,
                                homeViewModel = viewModel,
                                onNavigateToRoot = { rootKey ->
                                    rootBackStack.add(rootKey)
                                },
                                getDateLabel = { start, end -> getDateChipLabel(start, end) }
                            )
                        }
                        addEntry(
                            appState = appState,
                            onBackClick = { rootBackStack.removeAt(rootBackStack.size - 1) },
                            onSaveSuccess = { isExpense, day, month, year ->
                                viewModel.onTransactionCommitted(isExpense, day, month, year)
                                rootBackStack.removeAt(rootBackStack.size - 1)
                            }
                        )
                    }
                )

                CompositionLocalProvider(LocalSnackbarHostState provides appState.snackbarHostState) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavDisplay(
                            entries = rootEntries,
                            transitionSpec = { slideInHorizontally(initialOffsetX = { (it * 0.1).toInt() }) + fadeIn() togetherWith
                                    slideOutHorizontally(targetOffsetX = { -(it * 0.1).toInt() }) + fadeOut() },
                            popTransitionSpec = { slideInHorizontally(initialOffsetX = { -(it * 0.1).toInt() }) + fadeIn() togetherWith
                                    slideOutHorizontally(targetOffsetX = { (it * 0.1).toInt() }) + fadeOut() },
                            predictivePopTransitionSpec = { slideInHorizontally(initialOffsetX = { -(it * 0.1).toInt() }) + fadeIn() togetherWith
                                    slideOutHorizontally(targetOffsetX = { (it * 0.1).toInt() }) + fadeOut() },
                            onBack = { rootBackStack.removeAt(rootBackStack.size - 1) }
                        )

                        if (appState.showProgress) {
                            val density = LocalDensity.current
                            val amplitude = with(density) { 3.dp.toPx() }
                            val stroke = Stroke(
                                width = with(density) { 4.dp.toPx() },
                                cap = StrokeCap.Round,
                            )
                            val waveLength = 40.dp
                            LinearWavyProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .align(Alignment.TopCenter),
                                stroke = stroke,
                                trackStroke = stroke,
                                amplitude = amplitude,
                                wavelength = waveLength,
                                waveSpeed = waveLength
                            )
                        }
                    }
                }
            }
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
