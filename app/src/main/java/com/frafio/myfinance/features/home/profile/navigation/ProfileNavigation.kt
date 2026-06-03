package com.frafio.myfinance.features.home.profile.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.features.home.profile.ProfileScreen
import com.frafio.myfinance.features.home.profile.ProfileUiEvent
import com.frafio.myfinance.features.home.profile.ProfileViewModel
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.HomeTabKey
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.profileEntry(
    appState: MyFinanceAppState,
    initialUser: User? = null,
    profilePicture: android.graphics.Bitmap? = null,
    onUploadProPic: () -> Unit
) {
    entry<HomeTabKey.Profile> {
        val viewModel: ProfileViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()

        val fnUpdatedString = stringResource(id = R.string.full_name_updated)
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == HomeTabKey.Profile) {
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
