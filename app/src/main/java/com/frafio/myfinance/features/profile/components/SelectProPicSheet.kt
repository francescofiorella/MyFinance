package com.frafio.myfinance.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AdaptiveSheet
import com.frafio.myfinance.core.components.ImageSelectorButton
import com.frafio.myfinance.core.components.SheetDialog
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.components.avatarOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProPicSheet(
    show: Boolean,
    googlePhotoUrl: String?,
    currentProPic: String?,
    onDismiss: () -> Unit,
    onSelectPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        SheetDialog(
            icon = R.drawable.ic_face_filled,
            title = stringResource(id = R.string.your_propic),
            label = stringResource(id = R.string.select),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val imageSize = 82.dp
                val iconSize = 32.dp

                Spacer(modifier = Modifier.width(16.dp))

                if (googlePhotoUrl != null) {
                    ImageSelectorButton(
                        modifier = Modifier.size(imageSize),
                        url = googlePhotoUrl,
                        onClick = { onSelectPhoto(FirestoreEnums.PRO_PIC_TYPES.GOOGLE.value) },
                        contentDescription = stringResource(id = R.string.profile_picture),
                        containerSize = imageSize,
                        contentSize = iconSize,
                        isSelected = currentProPic == FirestoreEnums.PRO_PIC_TYPES.GOOGLE.value
                    )
                }

                avatarOptions.forEach { option ->
                    ImageSelectorButton(
                        modifier = Modifier.size(imageSize),
                        drawable = option.drawableRes,
                        onClick = { onSelectPhoto(option.id) },
                        contentDescription = stringResource(id = R.string.profile_picture),
                        containerSize = imageSize,
                        contentSize = iconSize,
                        isSelected = currentProPic == option.id
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectProPicSheetPreview() {
    MyFinanceTheme {
        SelectProPicSheet(
            show = true,
            googlePhotoUrl = null,
            currentProPic = FirestoreEnums.PRO_PIC_TYPES.AVATAR_1.value,
            onDismiss = {},
            onSelectPhoto = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
