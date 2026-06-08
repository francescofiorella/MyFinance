package com.frafio.myfinance.features.home

import android.graphics.Bitmap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.frafio.myfinance.core.navigation.Navigator
import com.frafio.myfinance.core.navigation.toEntries
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.R
import com.frafio.myfinance.app.HomeUiEvent
import com.frafio.myfinance.app.HomeViewModel
import com.frafio.myfinance.features.add.AddViewModel
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.features.budget.navigation.budgetEntry
import com.frafio.myfinance.features.dashboard.navigation.dashboardEntry
import com.frafio.myfinance.features.expenses.navigation.expensesEntry
import com.frafio.myfinance.features.profile.navigation.profileEntry
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen(
    appState: MyFinanceAppState,
    homeViewModel: HomeViewModel,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    onNavigateToRoot: (RootKey) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String
) {
    val navigator = remember { Navigator(appState.navigationState) }
    val currentTab = appState.navigationState.currentTopLevelKey as HomeTabKey

    val user by homeViewModel.user.collectAsStateWithLifecycle()
    val profilePicture by homeViewModel.profilePicture.collectAsStateWithLifecycle()

    val loginSuccessString = stringResource(id = R.string.login_successful)

    LaunchedEffect(homeViewModel.navEvents) {
        homeViewModel.navEvents.collect { key ->
            when (key) {
                is RootKey -> onNavigateToRoot(key)
                is HomeTabKey -> {
                    if (key != currentTab) {
                        navigator.navigate(key)
                    }
                }
            }
        }
    }


    LaunchedEffect(homeViewModel.uiEvents) {
        homeViewModel.uiEvents.collect { event ->
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
                    appState.showSnackBar("$loginSuccessString ${homeViewModel.getFullName()}")
                }
            }
        }
    }

    val comingSoonString = stringResource(id = R.string.coming_soon)

    HomeScreenContent(
        appState = appState,
        currentTab = currentTab,
        profilePicture = profilePicture,
        windowAdaptiveInfo = windowAdaptiveInfo,
        onTabClick = { navKey ->
            if (currentTab == navKey) {
                appState.onReselect(navKey)
            } else {
                navigator.navigate(navKey)
            }
        },
        onAddClick = {
            homeViewModel.navigateTo(
                RootKey.AddEditTransaction(
                    requestCode = AddViewModel.REQUEST_ADD_CODE,
                    expenseCode = AddViewModel.REQUEST_EXPENSE_CODE
                )
            )
        },
        onLogoutClick = { homeViewModel.onLogoutButtonClick() },
        onProPicClick = { navigator.navigate(HomeTabKey.Profile) },
        screenContent = {
            val homeEntries = appState.navigationState.toEntries(
                entryProvider = entryProvider {
                    dashboardEntry(appState)
                    expensesEntry(
                        appState = appState,
                        parentScrollEvents = homeViewModel.scrollEvents,
                        resetParentScrollEvents = homeViewModel::resetScrollEvent,
                        onItemLongClick = { expense, position ->
                            onNavigateToRoot(
                                RootKey.AddEditTransaction(
                                    requestCode = AddViewModel.REQUEST_EDIT_CODE,
                                    expenseCode = AddViewModel.REQUEST_EXPENSE_CODE,
                                    transaction = expense,
                                    position = position
                                )
                            )
                        },
                        getDateLabel = getDateLabel
                    )
                    budgetEntry(
                        appState = appState,
                        parentScrollEvents = homeViewModel.scrollEvents,
                        resetParentScrollEvents = homeViewModel::resetScrollEvent,
                        onEditIncome = { income, position ->
                            onNavigateToRoot(
                                RootKey.AddEditTransaction(
                                    requestCode = AddViewModel.REQUEST_EDIT_CODE,
                                    expenseCode = AddViewModel.REQUEST_INCOME_CODE,
                                    transaction = income,
                                    position = position
                                )
                            )
                        }
                    )
                    profileEntry(
                        appState = appState,
                        initialUser = user,
                        profilePicture = profilePicture,
                        onUploadProPic = {
                            appState.showSnackBar(comingSoonString)
                        }
                    )
                }
            )

            NavDisplay(
                entries = homeEntries,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                popTransitionSpec = { fadeIn() togetherWith fadeOut() },
                predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
                onBack = { navigator.goBack() }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun HomeScreenContent(
    appState: MyFinanceAppState,
    currentTab: HomeTabKey,
    profilePicture: Bitmap?,
    windowAdaptiveInfo: WindowAdaptiveInfo,
    onTabClick: (HomeTabKey) -> Unit,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    screenContent: @Composable () -> Unit
) {
    val navSuiteState = rememberNavigationSuiteScaffoldState()

    val layoutType =
        if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }

    Box(modifier = Modifier.fillMaxSize()) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                val tabs = listOf(
                    HomeTabKey.Dashboard,
                    HomeTabKey.Expenses,
                    HomeTabKey.Budget,
                    HomeTabKey.Profile
                )
                tabs.forEach { navKey ->
                    val isSelected = currentTab == navKey
                    item(
                        selected = isSelected,
                        onClick = { onTabClick(navKey) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (navKey) {
                                        HomeTabKey.Dashboard -> if (isSelected) R.drawable.ic_home_filled else R.drawable.ic_home_outline
                                        HomeTabKey.Expenses -> if (isSelected) R.drawable.ic_swap_horizontal_circle_filled else R.drawable.ic_swap_horizontal_circle_outline
                                        HomeTabKey.Budget -> if (isSelected) R.drawable.ic_savings_filled else R.drawable.ic_savings_outline
                                        HomeTabKey.Profile -> if (isSelected) R.drawable.ic_account_circle_filled else R.drawable.ic_account_circle_outline
                                    }
                                ),
                                contentDescription = null,
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    id = when (navKey) {
                                        HomeTabKey.Dashboard -> R.string.dashboard
                                        HomeTabKey.Expenses -> R.string.expenses
                                        HomeTabKey.Budget -> R.string.budget
                                        HomeTabKey.Profile -> R.string.profile
                                    }
                                )
                            )
                        }
                    )
                }
            },
            layoutType = layoutType,
            state = navSuiteState
        ) {
            MainScaffold(
                appState = appState,
                currentTab = currentTab,
                profilePicture = profilePicture,
                onAddClick = onAddClick,
                onLogoutClick = onLogoutClick,
                onProPicClick = onProPicClick,
                screenContent = screenContent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyFinanceTheme {
        val appState = rememberMyFinanceAppState()
        HomeScreenContent(
            appState = appState,
            currentTab = HomeTabKey.Dashboard,
            profilePicture = null,
            windowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
            onTabClick = {},
            onAddClick = {},
            onLogoutClick = {},
            onProPicClick = {},
            screenContent = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Dashboard Content")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    appState: MyFinanceAppState,
    currentTab: HomeTabKey,
    profilePicture: Bitmap?,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    screenContent: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SwipeableSnackbarHost(hostState = appState.snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp, top = 16.dp),
                        title = {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(
                                        id = when (currentTab) {
                                            HomeTabKey.Dashboard -> R.string.dashboard
                                            HomeTabKey.Expenses -> R.string.expenses
                                            HomeTabKey.Budget -> R.string.budget
                                            HomeTabKey.Profile -> R.string.profile
                                        }
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            if (currentTab == HomeTabKey.Profile) {
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
                                    } ?: painterResource(id = R.drawable.image_user)
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
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                screenContent()
            }
        }
    }
}
