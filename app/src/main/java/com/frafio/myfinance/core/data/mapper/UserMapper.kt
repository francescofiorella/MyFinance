package com.frafio.myfinance.core.data.mapper

import com.frafio.myfinance.core.data.model.User
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar

fun FirebaseUser.toUser(): User = buildUser(
    displayName = displayName,
    email = checkNotNull(email) { "Firebase user $uid has no email; every supported provider must supply one" },
    photoUrl = photoUrl?.toString(),
    providerIds = providerData.map { it.providerId },
    providerId = providerId,
    creationTimestamp = metadata?.creationTimestamp
)

internal fun buildUser(
    displayName: String?,
    email: String,
    photoUrl: String?,
    providerIds: List<String>,
    providerId: String,
    creationTimestamp: Long?
): User {
    var userPic = ""
    photoUrl?.let { url ->
        userPic = url.replace("s96-c", "s400-c")
    }
    val providers = providerIds.toMutableList()
    if (!providers.contains(providerId)) {
        providers.add(providerId)
    }

    val hasPassword = providers.any { it.contains("password") }
    val isGoogleLinked = providers.any { it.contains("google.com") }

    var provider = User.EMAIL_PROVIDER
    if (isGoogleLinked) {
        provider = User.GOOGLE_PROVIDER
    }
    var day: Int? = null
    var month: Int? = null
    var year: Int? = null
    creationTimestamp?.let {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = it
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH) + 1
        year = calendar.get(Calendar.YEAR)
    }
    return User(
        fullName = displayName,
        email = email,
        photoUrl = userPic,
        localPhotoPath = null,
        provider = provider,
        providers = providers,
        hasPassword = hasPassword,
        isGoogleLinked = isGoogleLinked,
        creationYear = year,
        creationMonth = month,
        creationDay = day
    )
}
