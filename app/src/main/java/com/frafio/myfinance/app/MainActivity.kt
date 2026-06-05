package com.frafio.myfinance.app

import android.os.Bundle
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.LocalSnackbarHostState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.dateToExtendedString
import com.frafio.myfinance.features.add.navigation.addEntry
import com.frafio.myfinance.features.auth.navigation.authEntry
import com.frafio.myfinance.features.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var loadingRepository: LoadingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Keep the splash screen on-screen until the UI state is loaded.
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is HomeUiState.Loading
        }

        if (savedInstanceState == null) {
            viewModel.checkUser(notify = false)
        }

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val useDynamicColor = userPreferences?.dynamicColor ?: false
            val isLoading by loadingRepository.isLoading.collectAsStateWithLifecycle()

            MyFinanceTheme(dynamicColor = useDynamicColor) {
                val appState = rememberMyFinanceAppState()
                val rootBackStack = rememberNavBackStack(if (viewModel.userRepository.isUserLoggedIn()) RootKey.Home else RootKey.Auth)

                LaunchedEffect(isLoading) {
                    appState.showProgress = isLoading
                }

                LaunchedEffect(Unit) {
                    viewModel.mainEvents.collect { event ->
                        when (event) {
                            MainEvent.UserNotLogged -> {
                                rootBackStack.clear()
                                rootBackStack.add(RootKey.Auth)
                            }

                            MainEvent.LogoutSuccess -> {
                                rootBackStack.clear()
                                rootBackStack.add(RootKey.Auth)
                            }
                        }
                    }
                }

                val decorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator()
                )
                val rootEntries = rememberDecoratedNavEntries(
                    backStack = rootBackStack,
                    entryDecorators = decorators,
                    entryProvider = entryProvider {
                        authEntry(
                            appState = appState,
                            onAuthSuccess = {
                                appState.navigationState.reset()
                                viewModel.checkUser(notify = true)
                                rootBackStack.clear()
                                rootBackStack.add(RootKey.Home)
                            }
                        )
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
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavDisplay(
                                entries = rootEntries,
                                transitionSpec = {
                                    slideInHorizontally(initialOffsetX = { (it * 0.1).toInt() }) + fadeIn() togetherWith
                                            slideOutHorizontally(targetOffsetX = { -(it * 0.1).toInt() }) + fadeOut()
                                },
                                popTransitionSpec = {
                                    slideInHorizontally(initialOffsetX = { -(it * 0.1).toInt() }) + fadeIn() togetherWith
                                            slideOutHorizontally(targetOffsetX = { (it * 0.1).toInt() }) + fadeOut()
                                },
                                predictivePopTransitionSpec = {
                                    slideInHorizontally(initialOffsetX = { -(it * 0.1).toInt() }) + fadeIn() togetherWith
                                            slideOutHorizontally(targetOffsetX = { (it * 0.1).toInt() }) + fadeOut()
                                },
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
