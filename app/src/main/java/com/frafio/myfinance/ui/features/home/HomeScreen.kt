package com.frafio.myfinance.ui.features.home

import android.graphics.Bitmap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.ui.features.home.budget.navigation.budgetEntry
import com.frafio.myfinance.ui.features.home.dashboard.navigation.dashboardEntry
import com.frafio.myfinance.ui.features.home.expenses.navigation.expensesEntry
import com.frafio.myfinance.ui.features.home.profile.navigation.profileEntry
import com.frafio.myfinance.ui.home.HomeUiEvent
import com.frafio.myfinance.ui.home.HomeViewModel
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import com.frafio.myfinance.ui.navigation.Navigator
import com.frafio.myfinance.ui.navigation.TOP_LEVEL_NAV_KEYS
import com.frafio.myfinance.ui.navigation.toEntries
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen(
    appState: MyFinanceAppState,
    viewModel: HomeViewModel,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    onEditExpense: (Expense, Int) -> Unit,
    onEditIncome: (Income, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String
) {
    val navigator = remember { Navigator(appState.navigationState) }
    val currentTopLevelKey = appState.navigationState.currentTopLevelKey
    val user by viewModel.user.collectAsStateWithLifecycle()
    val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(isLoading) {
        appState.showProgress = isLoading
    }

    val layoutType =
        if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }

    val loginSuccessString = stringResource(id = R.string.login_successful)

    LaunchedEffect(viewModel.navEvents) {
        viewModel.navEvents.collect { key ->
            navigator.navigate(key)
        }
    }

    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is HomeUiEvent.ShowSnackBar -> {
                    appState.showSnackBar(
                        event.message,
                        event.actionText,
                        event.actionFun,
                        event.dismissFun
                    )
                }

                HomeUiEvent.LoginSuccess -> {
                    appState.showSnackBar("$loginSuccessString ${viewModel.getFullName()}")
                }
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_NAV_KEYS.forEach { navKey ->
                val isSelected = currentTopLevelKey == navKey
                item(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            appState.onReselect(navKey)
                        } else {
                            navigator.navigate(navKey)
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = when (navKey) {
                                    MyFinanceNavKey.Dashboard -> if (isSelected) R.drawable.ic_home_filled else R.drawable.ic_home_outline
                                    MyFinanceNavKey.Expenses -> if (isSelected) R.drawable.ic_swap_horizontal_circle_filled else R.drawable.ic_swap_horizontal_circle_outline
                                    MyFinanceNavKey.Budget -> if (isSelected) R.drawable.ic_savings_filled else R.drawable.ic_savings_outline
                                    MyFinanceNavKey.Profile -> if (isSelected) R.drawable.ic_account_circle_filled else R.drawable.ic_account_circle_outline
                                    else -> R.drawable.ic_home_outline
                                }
                            ),
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(
                                id = when (navKey) {
                                    MyFinanceNavKey.Dashboard -> R.string.dashboard
                                    MyFinanceNavKey.Expenses -> R.string.expenses
                                    MyFinanceNavKey.Budget -> R.string.budget
                                    MyFinanceNavKey.Profile -> R.string.profile
                                    else -> R.string.app_name
                                }
                            )
                        )
                    }
                )
            }
        },
        layoutType = layoutType
    ) {
        CompositionLocalProvider(LocalSnackbarHostState provides appState.snackbarHostState) {
            MainScaffold(
                currentTopLevelKey = currentTopLevelKey as MyFinanceNavKey,
                profilePicture = profilePicture,
                showProgress = appState.showProgress,
                onAddClick = onAddClick,
                onLogoutClick = onLogoutClick,
                onProPicClick = onProPicClick,
                screenContent = {
                    val comingSoonString = stringResource(id = R.string.coming_soon)
                    val snackbarHostState = LocalSnackbarHostState.current
                    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
                        dashboardEntry(appState)
                        expensesEntry(
                            appState = appState,
                            onItemLongClick = onEditExpense,
                            getDateLabel = getDateLabel
                        )
                        budgetEntry(
                            appState = appState,
                            onEditIncome = onEditIncome
                        )
                        profileEntry(
                            appState = appState,
                            initialUser = user,
                            profilePicture = profilePicture,
                            onUploadProPic = {
                                appState.coroutineScope.launch {
                                    snackbarHostState.showSnackbar(comingSoonString)
                                }
                            }
                        )
                    }

                    NavDisplay(
                        entries = appState.navigationState.toEntries(entryProvider),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
                        predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) {
                        navigator.goBack()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    currentTopLevelKey: MyFinanceNavKey,
    profilePicture: Bitmap?,
    showProgress: Boolean,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    screenContent: @Composable () -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp, top = 16.dp),
                        title = {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontFamily = FontFamily(Font(R.font.nunito_bold)),
                                    fontSize = 24.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(
                                        id = when (currentTopLevelKey) {
                                            MyFinanceNavKey.Dashboard -> R.string.dashboard
                                            MyFinanceNavKey.Expenses -> R.string.expenses
                                            MyFinanceNavKey.Budget -> R.string.budget
                                            MyFinanceNavKey.Profile -> R.string.profile
                                        }
                                    ),
                                    fontFamily = FontFamily(Font(R.font.nunito)),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            if (currentTopLevelKey == MyFinanceNavKey.Profile) {
                                FilledTonalIconButton(onClick = onLogoutClick) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logout_filled),
                                        contentDescription = "Logout"
                                    )
                                }
                            } else {
                                IconButton(onClick = onProPicClick) {
                                    val painter = remember(profilePicture) {
                                        if (profilePicture != null) {
                                            BitmapPainter(profilePicture.asImageBitmap())
                                        } else {
                                            null
                                        }
                                    } ?: painterResource(id = R.drawable.ic_user)
                                    Image(
                                        painter = painter,
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(
                        painterResource(R.drawable.ic_add_filled),
                        contentDescription = stringResource(id = R.string.add)
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                screenContent()
            }
        }

        if (showProgress) {
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
