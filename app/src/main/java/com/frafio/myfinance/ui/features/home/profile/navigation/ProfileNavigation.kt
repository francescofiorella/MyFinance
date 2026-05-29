package com.frafio.myfinance.ui.features.home.profile.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.ui.features.home.profile.ProfileScreen
import com.frafio.myfinance.ui.home.profile.ProfileUiEvent
import com.frafio.myfinance.ui.home.profile.ProfileViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.profileEntry(
    appState: MyFinanceAppState,
    initialUser: User? = null,
    profilePicture: android.graphics.Bitmap? = null,
    onUploadProPic: () -> Unit
) {
    entry<MyFinanceNavKey.Profile> {
        val viewModel: ProfileViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()

        val fnUpdatedString = stringResource(id = R.string.full_name_updated)
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == MyFinanceNavKey.Profile) {
                    viewModel.scrollToTop()
                }
            }
        }

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is ProfileUiEvent.ShowSnackBar -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                event.actionText,
                                event.actionFun,
                                event.dismissFun
                            )
                        }
                    }

                    is ProfileUiEvent.FullNameUpdated -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                fnUpdatedString,
                                undoString,
                                {
                                    viewModel.editFullName(event.previousFullName, notify = false)
                                }
                            )
                        }
                    }
                }
            }
        }

        ProfileScreen(
            viewModel = viewModel,
            initialUser = initialUser,
            initialProfilePicture = profilePicture,
            onUploadProPic = onUploadProPic
        )
    }
}
