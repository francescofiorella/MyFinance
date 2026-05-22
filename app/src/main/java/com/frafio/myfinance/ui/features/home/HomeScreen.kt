package com.frafio.myfinance.ui.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.ui.features.home.budget.BudgetScreen
import com.frafio.myfinance.ui.features.home.dashboard.DashboardScreen
import com.frafio.myfinance.ui.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.ui.features.home.profile.ProfileScreen
import com.frafio.myfinance.ui.home.HomeViewModel
import com.frafio.myfinance.ui.home.budget.BudgetViewModel
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModel
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.home.profile.ProfileViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    dashboardViewModel: DashboardViewModel,
    expensesViewModel: ExpensesViewModel,
    budgetViewModel: BudgetViewModel,
    profileViewModel: ProfileViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    showProgress: Boolean,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    onEditExpense: (Expense, Int) -> Unit,
    onEditIncome: (Income, Int) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
    onShowSnackBar: (String) -> Unit,
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val proPic = viewModel.getProPic()
    val useNavRail = windowWidthSizeClass != WindowWidthSizeClass.Compact

    BackHandler(enabled = viewModel.getNavigationStackSize() > 1) {
        viewModel.navigateBack()
    }

    HomeContent(
        currentScreen = currentScreen,
        proPic = proPic,
        useNavRail = useNavRail,
        showProgress = showProgress,
        snackbarHostState = snackbarHostState,
        onAddClick = onAddClick,
        onLogoutClick = onLogoutClick,
        onProPicClick = onProPicClick,
        onNavigateTo = { screen ->
            if (currentScreen == screen) {
                when (screen) {
                    HomeViewModel.Screen.DASHBOARD -> dashboardViewModel.scrollToTop()
                    HomeViewModel.Screen.EXPENSES -> {
                        val today = LocalDate.now()
                        val todayId = "total_${today.dayOfMonth}_${today.monthValue}_${today.year}"
                        expensesViewModel.scrollToId(todayId)
                    }
                    HomeViewModel.Screen.BUDGET -> budgetViewModel.scrollToId(null)
                    HomeViewModel.Screen.PROFILE -> profileViewModel.scrollToTop()
                }
            } else {
                viewModel.navigateTo(screen)
            }
        },
        screenContent = {
            val comingSoonString = stringResource(id = R.string.coming_soon)
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    HomeViewModel.Screen.DASHBOARD -> DashboardScreen(viewModel = dashboardViewModel)
                    HomeViewModel.Screen.EXPENSES -> ExpensesScreen(
                        viewModel = expensesViewModel,
                        onItemLongClick = onEditExpense,
                        getDateLabel = getDateLabel
                    )
                    HomeViewModel.Screen.BUDGET -> BudgetScreen(
                        viewModel = budgetViewModel,
                        onEditIncome = onEditIncome
                    )
                    HomeViewModel.Screen.PROFILE -> ProfileScreen(
                        viewModel = profileViewModel,
                        onUploadProPic = { onShowSnackBar(comingSoonString) },
                        onDynamicColorChanged = onDynamicColorChanged
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    currentScreen: HomeViewModel.Screen,
    proPic: String?,
    useNavRail: Boolean,
    showProgress: Boolean,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    onNavigateTo: (HomeViewModel.Screen) -> Unit,
    screenContent: @Composable () -> Unit
) {
    val navigationContent = @Composable {
        if (useNavRail) {
            PermanentDrawerSheet(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    .width(200.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                drawerTonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(12.dp))
                    HomeViewModel.Screen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = stringResource(id = screen.titleRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = isSelected,
                            onClick = { onNavigateTo(screen) },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = when (screen) {
                                            HomeViewModel.Screen.DASHBOARD -> if (isSelected) R.drawable.ic_home_filled else R.drawable.ic_home_outline
                                            HomeViewModel.Screen.EXPENSES -> if (isSelected) R.drawable.ic_swap_horizontal_circle_filled else R.drawable.ic_swap_horizontal_circle_outline
                                            HomeViewModel.Screen.BUDGET -> if (isSelected) R.drawable.ic_savings_filled else R.drawable.ic_savings_outline
                                            HomeViewModel.Screen.PROFILE -> if (isSelected) R.drawable.ic_account_circle_filled else R.drawable.ic_account_circle_outline
                                        }
                                    ),
                                    contentDescription = null
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    }

    if (useNavRail) {
        PermanentNavigationDrawer(
            drawerContent = navigationContent
        ) {
            MainScaffold(
                currentScreen = currentScreen,
                proPic = proPic,
                useNavRail = true,
                showProgress = showProgress,
                snackbarHostState = snackbarHostState,
                onAddClick = onAddClick,
                onLogoutClick = onLogoutClick,
                onProPicClick = onProPicClick,
                onNavigateTo = onNavigateTo,
                screenContent = screenContent
            )
        }
    } else {
        MainScaffold(
            currentScreen = currentScreen,
            proPic = proPic,
            useNavRail = false,
            showProgress = showProgress,
            snackbarHostState = snackbarHostState,
            onAddClick = onAddClick,
            onLogoutClick = onLogoutClick,
            onProPicClick = onProPicClick,
            onNavigateTo = onNavigateTo,
            screenContent = screenContent
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    currentScreen: HomeViewModel.Screen,
    proPic: String?,
    useNavRail: Boolean,
    showProgress: Boolean,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProPicClick: () -> Unit,
    onNavigateTo: (HomeViewModel.Screen) -> Unit,
    screenContent: @Composable () -> Unit
) {
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
                                    text = stringResource(id = currentScreen.titleRes),
                                    fontFamily = FontFamily(Font(R.font.nunito)),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            if (currentScreen == HomeViewModel.Screen.PROFILE) {
                                FilledTonalIconButton(onClick = onLogoutClick) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logout_filled),
                                        contentDescription = "Logout"
                                    )
                                }
                            } else {
                                IconButton(onClick = onProPicClick) {
                                    AsyncImage(
                                        model = proPic ?: R.drawable.ic_user,
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = R.drawable.ic_user),
                                        placeholder = painterResource(id = R.drawable.ic_user)
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
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar {
                        HomeViewModel.Screen.entries.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    val isSelected = currentScreen == screen
                                    Icon(
                                        painter = painterResource(
                                            id = when (screen) {
                                                HomeViewModel.Screen.DASHBOARD -> if (isSelected) R.drawable.ic_home_filled else R.drawable.ic_home_outline
                                                HomeViewModel.Screen.EXPENSES -> if (isSelected) R.drawable.ic_swap_horizontal_circle_filled else R.drawable.ic_swap_horizontal_circle_outline
                                                HomeViewModel.Screen.BUDGET -> if (isSelected) R.drawable.ic_savings_filled else R.drawable.ic_savings_outline
                                                HomeViewModel.Screen.PROFILE -> if (isSelected) R.drawable.ic_account_circle_filled else R.drawable.ic_account_circle_outline
                                            }
                                        ),
                                        contentDescription = stringResource(id = screen.titleRes)
                                    )
                                },
                                label = { Text(text = stringResource(id = screen.titleRes)) },
                                selected = currentScreen == screen,
                                onClick = { onNavigateTo(screen) }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (
                    (currentScreen == HomeViewModel.Screen.DASHBOARD) ||
                    (currentScreen == HomeViewModel.Screen.EXPENSES) ||
                    (currentScreen == HomeViewModel.Screen.BUDGET)
                ) {
                    if (useNavRail) {
                        ExtendedFloatingActionButton(
                            onClick = onAddClick,
                            icon = { Icon(painterResource(R.drawable.ic_add_filled), contentDescription = null) },
                            text = { Text(text = stringResource(id = R.string.add)) }
                        )
                    } else {
                        FloatingActionButton(onClick = onAddClick) {
                            Icon(painterResource(R.drawable.ic_add_filled), contentDescription = stringResource(id = R.string.add))
                        }
                    }
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

@Preview(showBackground = true, name = "Mobile Portrait")
@Composable
fun HomeScreenMobilePreview() {
    MyFinanceTheme {
        HomeContent(
            currentScreen = HomeViewModel.Screen.DASHBOARD,
            proPic = null,
            useNavRail = false,
            showProgress = false,
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
            onLogoutClick = {},
            onProPicClick = {},
            onNavigateTo = {},
            screenContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Dashboard Content Placeholder")
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600, name = "Tablet Landscape")
@Composable
fun HomeScreenTabletPreview() {
    MyFinanceTheme {
        HomeContent(
            currentScreen = HomeViewModel.Screen.EXPENSES,
            proPic = null,
            useNavRail = true,
            showProgress = true,
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
            onLogoutClick = {},
            onProPicClick = {},
            onNavigateTo = {},
            screenContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Expenses Content Placeholder")
                }
            }
        )
    }
}
