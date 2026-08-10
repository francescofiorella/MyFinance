package com.frafio.myfinance.core.components

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums

data class AvatarOption(
    val id: String,
    @DrawableRes val drawableRes: Int
)

val avatarOptions = listOf(
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_1.value, R.drawable.image_profile_interface_cuate),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_2.value, R.drawable.image_profile_interface_pana),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_3.value, R.drawable.image_balloon_seller_amico),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_4.value, R.drawable.image_edit_photo_pana),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_5.value, R.drawable.image_online_resume_pana),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_6.value, R.drawable.image_online_resume_cuate),
    AvatarOption(FirestoreEnums.PRO_PIC_TYPES.AVATAR_7.value, R.drawable.image_people_creating_robot_rafiki)
)

@Composable
fun rememberProfilePicturePainter(
    proPicChoice: String?,
    profilePicture: Bitmap?
): Painter {
    return when (proPicChoice) {
        FirestoreEnums.PRO_PIC_TYPES.GOOGLE.value -> {
            if (profilePicture != null) {
                BitmapPainter(profilePicture.asImageBitmap())
            } else {
                painterResource(id = R.drawable.image_profile_interface_cuate)
            }
        }
        else -> {
            val drawableRes = avatarOptions.find { it.id == proPicChoice }?.drawableRes
                ?: R.drawable.image_profile_interface_cuate
            painterResource(id = drawableRes)
        }
    }
}
