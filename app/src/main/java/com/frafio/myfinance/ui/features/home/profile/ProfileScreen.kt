package com.frafio.myfinance.ui.features.home.profile

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.ui.home.profile.ProfileViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditProfileClick: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val isDynamicColorChecked by viewModel.isSwitchDynamicColorChecked.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.scrollToTop) {
        viewModel.scrollToTop.collectLatest {
            scrollState.animateScrollTo(0)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            ProfileContentLandscape(
                user = user,
                googleSignIn = viewModel.googleSignIn,
                versionName = viewModel.versionName,
                isDynamicColorAvailable = viewModel.isDynamicColorAvailable,
                isDynamicColorChecked = isDynamicColorChecked,
                scrollState = scrollState,
                onEditProfileClick = onEditProfileClick,
                onDynamicColorChanged = onDynamicColorChanged
            )
        } else {
            ProfileContentPortrait(
                user = user,
                googleSignIn = viewModel.googleSignIn,
                versionName = viewModel.versionName,
                isDynamicColorAvailable = viewModel.isDynamicColorAvailable,
                isDynamicColorChecked = isDynamicColorChecked,
                scrollState = scrollState,
                onEditProfileClick = onEditProfileClick,
                onDynamicColorChanged = onDynamicColorChanged
            )
        }
    }
}

@Composable
private fun ProfileContentPortrait(
    user: User?,
    googleSignIn: Boolean,
    versionName: String,
    isDynamicColorAvailable: Boolean,
    isDynamicColorChecked: Boolean,
    scrollState: ScrollState,
    onEditProfileClick: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ProfileHeader(user = user)

        Spacer(modifier = Modifier.height(32.dp))

        ProfileCards(
            user = user,
            googleSignIn = googleSignIn,
            versionName = versionName,
            isDynamicColorAvailable = isDynamicColorAvailable,
            isDynamicColorChecked = isDynamicColorChecked,
            onEditProfileClick = onEditProfileClick,
            onDynamicColorChanged = onDynamicColorChanged
        )
    }
}

@Composable
private fun ProfileContentLandscape(
    user: User?,
    googleSignIn: Boolean,
    versionName: String,
    isDynamicColorAvailable: Boolean,
    isDynamicColorChecked: Boolean,
    scrollState: ScrollState,
    onEditProfileClick: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ProfileHeader(user = user)
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            ProfileCards(
                user = user,
                googleSignIn = googleSignIn,
                versionName = versionName,
                isDynamicColorAvailable = isDynamicColorAvailable,
                isDynamicColorChecked = isDynamicColorChecked,
                onEditProfileClick = onEditProfileClick,
                onDynamicColorChanged = onDynamicColorChanged
            )
        }
    }
}

@Composable
private fun ProfileHeader(user: User?) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(user?.photoUrl)
            .crossfade(false)
            .build(),
        contentDescription = stringResource(id = R.string.profile_picture),
        placeholder = painterResource(id = R.drawable.ic_user),
        error = painterResource(id = R.drawable.ic_user),
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )

    Text(
        text = user?.fullName ?: "",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
    )

    Text(
        text = user?.email ?: "",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileCards(
    user: User?,
    googleSignIn: Boolean,
    versionName: String,
    isDynamicColorAvailable: Boolean,
    isDynamicColorChecked: Boolean,
    onEditProfileClick: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit
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
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )

    SegmentedListItem(
        onClick = onEditProfileClick,
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = 0,
            count = if (googleSignIn) 3 else 2,
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
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down_filled),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
    )

    // Provider Card
    if (googleSignIn) {
        SegmentedListItem(
            onClick = {},
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(
                index = 1,
                count = 3,
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
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.sign_up_provider),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
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
            index = if (googleSignIn) 2 else 1,
            count = if (googleSignIn) 3 else 2,
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
                fontWeight = FontWeight.Normal,
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
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )

    if (isDynamicColorAvailable) {
        SegmentedListItem(
            onClick = {},
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(
                index = 0,
                count = 2,
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
                    fontWeight = FontWeight.Normal,
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
        shapes = if (isDynamicColorAvailable) {
            ListItemDefaults.segmentedShapes(
                index = 1,
                count = 2,
                defaultShapes = ListItemDefaults.shapes()
            )
        } else {
            ListItemDefaults.shapes(
                shape = ListItemDefaults.shapes().selectedShape
            )
        },
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
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(88.dp))
}

@Preview(showBackground = true, name = "Portrait")
@Composable
fun ProfilePortraitPreview() {
    MyFinanceTheme {
        ProfileContentPortrait(
            user = User(
                fullName = "John Doe",
                email = "john.doe@example.com",
                photoUrl = null,
                creationDay = 1,
                creationMonth = 1,
                creationYear = 2023
            ),
            googleSignIn = true,
            versionName = "1.0.0",
            isDynamicColorAvailable = true,
            isDynamicColorChecked = false,
            scrollState = rememberScrollState(),
            onEditProfileClick = {},
            onDynamicColorChanged = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400, name = "Landscape")
@Composable
fun ProfileLandscapePreview() {
    MyFinanceTheme {
        ProfileContentLandscape(
            user = User(
                fullName = "John Doe",
                email = "john.doe@example.com",
                photoUrl = null,
                creationDay = 1,
                creationMonth = 1,
                creationYear = 2023
            ),
            googleSignIn = true,
            versionName = "My Finance 1.0.0",
            isDynamicColorAvailable = true,
            isDynamicColorChecked = true,
            scrollState = rememberScrollState(),
            onEditProfileClick = {},
            onDynamicColorChanged = {}
        )
    }
}
