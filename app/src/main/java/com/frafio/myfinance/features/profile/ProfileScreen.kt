package com.frafio.myfinance.features.profile

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.components.rememberProfilePicturePainter
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCurrencyIcon
import com.frafio.myfinance.features.profile.components.CurrencySheet
import com.frafio.myfinance.features.profile.components.EditFullNameSheet
import com.frafio.myfinance.features.profile.components.SelectProPicSheet
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    initialUser: User? = null,
    initialProfilePicture: Bitmap? = null,
    onManageLabels: () -> Unit,
    onCategoriesDescriptionClick: () -> Unit,
    onChangePassword: () -> Unit
) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val profilePictureState by viewModel.profilePicture.collectAsStateWithLifecycle()

    // Preserve user data during logout transition to avoid "flashing"
    var lastUser by remember { mutableStateOf(initialUser) }
    if (userState != null) {
        lastUser = userState
    }
    val user = userState ?: lastUser

    var lastProfilePicture by remember { mutableStateOf(initialProfilePicture) }
    if (profilePictureState != null) {
        lastProfilePicture = profilePictureState
    }
    val profilePicture = profilePictureState ?: lastProfilePicture

    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isDynamicColorChecked by viewModel.isSwitchDynamicColorChecked.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val currencyCode = userPreferences?.currencyCode ?: "EUR"

    var showEditFullNameSheet by remember { mutableStateOf(false) }
    var showSelectProPicSheet by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }

    EditFullNameSheet(
        show = showEditFullNameSheet,
        fullName = user?.fullName ?: "",
        onDismiss = {
            if (showEditFullNameSheet) {
                showEditFullNameSheet = false
            }
        },
        onEditFullName = { viewModel.editFullName(it) }
    )

    SelectProPicSheet(
        show = showSelectProPicSheet,
        googlePhotoUrl = user?.photoUrl,
        currentProPic = userPreferences?.proPicChoice,
        onDismiss = {
            if (showSelectProPicSheet) {
                showSelectProPicSheet = false
            }
        },
        onSelectPhoto = {
            viewModel.setProPicChoice(it)
            showSelectProPicSheet = false
        }
    )

    CurrencySheet(
        show = showCurrencySheet,
        onDismiss = { showCurrencySheet = false },
        onCurrencySelected = {
            viewModel.setCurrencyCode(it)
            showCurrencySheet = false
        },
        currencyCode = currencyCode
    )

    LaunchedEffect(viewModel.scrollToTop) {
        viewModel.scrollToTop.collectLatest {
            scrollState.animateScrollTo(0)
        }
    }

    ProfileContent(
        modifier = modifier,
        user = user,
        profilePicture = profilePicture,
        proPicChoice = userPreferences?.proPicChoice,
        versionName = viewModel.versionName,
        isDynamicColorAvailable = viewModel.isDynamicColorAvailable,
        isDynamicColorChecked = isDynamicColorChecked,
        scrollState = scrollState,
        onSelectProPic = { showSelectProPicSheet = true },
        onEditFullName = { showEditFullNameSheet = true },
        onDynamicColorChanged = { viewModel.setDynamicColor(it) },
        onManageLabels = onManageLabels,
        onCategoriesDescriptionClick = onCategoriesDescriptionClick,
        onSelectCurrency = { showCurrencySheet = true },
        onChangePasswordClick = onChangePassword,
        currencyCode = currencyCode
    )
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    user: User?,
    profilePicture: Bitmap?,
    proPicChoice: String?,
    versionName: String,
    isDynamicColorAvailable: Boolean,
    isDynamicColorChecked: Boolean,
    scrollState: ScrollState,
    onSelectProPic: () -> Unit,
    onEditFullName: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onManageLabels: () -> Unit,
    onCategoriesDescriptionClick: () -> Unit,
    onSelectCurrency: () -> Unit,
    onChangePasswordClick: () -> Unit,
    currencyCode: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ProfileHeader(user = user, profilePicture = profilePicture, proPicChoice = proPicChoice)

        Spacer(modifier = Modifier.height(32.dp))

        ProfileCards(
            user = user,
            versionName = versionName,
            isDynamicColorAvailable = isDynamicColorAvailable,
            isDynamicColorChecked = isDynamicColorChecked,
            onSelectProPic = onSelectProPic,
            onEditFullName = onEditFullName,
            onDynamicColorChanged = onDynamicColorChanged,
            onManageLabels = onManageLabels,
            onCategoriesDescriptionClick = onCategoriesDescriptionClick,
            onSelectCurrency = onSelectCurrency,
            onChangePasswordClick = onChangePasswordClick,
            currencyCode = currencyCode
        )
    }
}

@Composable
private fun ProfileHeader(user: User?, profilePicture: Bitmap?, proPicChoice: String?) {
    val painter = rememberProfilePicturePainter(proPicChoice, profilePicture)
    Image(
        painter = painter,
        contentDescription = stringResource(id = R.string.profile_picture),
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )

    Text(
        text = user?.fullName ?: "",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
    )

    Text(
        text = user?.email ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileCards(
    user: User?,
    versionName: String,
    isDynamicColorAvailable: Boolean,
    isDynamicColorChecked: Boolean,
    onSelectProPic: () -> Unit,
    onEditFullName: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onManageLabels: () -> Unit,
    onCategoriesDescriptionClick: () -> Unit,
    onSelectCurrency: () -> Unit,
    onChangePasswordClick: () -> Unit,
    currencyCode: String
) {
    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        text = stringResource(id = R.string.profile),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    var expanded by rememberSaveable { mutableStateOf(false) }

    val googleLinked = user?.isGoogleLinked == true
    val emailLinked = user?.hasPassword == true

    SegmentedListItem(
        onClick = { expanded = !expanded },
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = 0,
            count = if (expanded) 4 else 3,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(id = R.string.edit_profile),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (expanded)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surfaceContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (expanded)
                            R.drawable.ic_keyboard_arrow_up_filled
                        else
                            R.drawable.ic_keyboard_arrow_down_filled
                    ),
                    contentDescription = null,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
    )
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        Column {
            SegmentedListItem(
                onClick = onSelectProPic,
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 1,
                    count = 4,
                    defaultShapes = ListItemDefaults.shapes()
                ),
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_face_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                content = {
                    Text(
                        text = stringResource(id = R.string.edit_propic),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp),
            )

            SegmentedListItem(
                onClick = onEditFullName,
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 2,
                    count = 4,
                    defaultShapes = ListItemDefaults.shapes()
                ),
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                content = {
                    Text(
                        text = stringResource(id = R.string.edit_full_name),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp),
            )

            SegmentedListItem(
                onClick = onChangePasswordClick,
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 3,
                    count = 4,
                    defaultShapes = ListItemDefaults.shapes()
                ),
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_password_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                content = {
                    Text(
                        text = stringResource(id = R.string.change_password),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            )
        }
    }

    // Provider Card
    SegmentedListItem(
        onClick = {},
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = if (expanded) 0 else 1,
            count = if (expanded) 2 else 3,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when {
                            googleLinked && emailLinked -> R.drawable.ic_google
                            googleLinked -> R.drawable.ic_google
                            else -> R.drawable.ic_mail_filled
                        }
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(
                    id = when {
                        googleLinked && emailLinked -> R.string.sign_up_google_email
                        googleLinked -> R.string.sign_up_provider
                        else -> R.string.sign_up_email
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp)
    )

    SegmentedListItem(
        onClick = {},
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = if (expanded) 1 else 2,
            count = if (expanded) 2 else 3,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_today_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(
                    id = R.string.signUpDate,
                    user?.getCreationDataString() ?: ""
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp)
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    val myFinanceItemCount = (if (isDynamicColorAvailable) 1 else 0) + 4

    SegmentedListItem(
        onClick = onManageLabels,
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = 0,
            count = myFinanceItemCount,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sell_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(id = R.string.manage_labels),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                    contentDescription = null,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
    )

    SegmentedListItem(
        onClick = onSelectCurrency,
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = 1,
            count = myFinanceItemCount,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getCurrencyIcon(currencyCode)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(id = R.string.change_currency),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                    contentDescription = null,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
    )

    SegmentedListItem(
        onClick = onCategoriesDescriptionClick,
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = 2,
            count = myFinanceItemCount,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = stringResource(id = R.string.categories_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                    contentDescription = null,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
    )

    if (isDynamicColorAvailable) {
        SegmentedListItem(
            onClick = {},
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(
                index = 3,
                count = myFinanceItemCount,
                defaultShapes = ListItemDefaults.shapes()
            ),
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_palette_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.use_dynamic_color),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingContent = {
                Switch(
                    checked = isDynamicColorChecked,
                    onCheckedChange = onDynamicColorChanged,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )
    }

    SegmentedListItem(
        onClick = {},
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = if (isDynamicColorAvailable) 4 else 3,
            count = myFinanceItemCount,
            defaultShapes = ListItemDefaults.shapes()
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_android_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(112.dp)) // Floating Action Button space
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MyFinanceTheme {
        ProfileContent(
            user = User(
                fullName = "John Doe",
                email = "john.doe@example.com",
                photoUrl = null,
                creationDay = 1,
                creationMonth = 1,
                creationYear = 2023,
                isGoogleLinked = true,
                hasPassword = true
            ),
            profilePicture = null,
            proPicChoice = null,
            versionName = "MyFinance 1.0.0",
            isDynamicColorAvailable = true,
            isDynamicColorChecked = false,
            scrollState = rememberScrollState(),
            onSelectProPic = {},
            onEditFullName = {},
            onDynamicColorChanged = {},
            onManageLabels = {},
            onCategoriesDescriptionClick = {},
            onSelectCurrency = {},
            onChangePasswordClick = {},
            currencyCode = "EUR"
        )
    }
}
