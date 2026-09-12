package com.frafio.myfinance.core.data.mapper

import com.frafio.myfinance.core.data.model.User
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class UserMapperTest {

    private fun build(
        displayName: String? = "Ada",
        email: String = "ada@example.com",
        photoUrl: String? = null,
        providerIds: List<String> = listOf("password"),
        providerId: String = "firebase",
        creationTimestamp: Long? = null,
    ): User = buildUser(displayName, email, photoUrl, providerIds, providerId, creationTimestamp)

    @Test
    fun copiesDisplayNameAndEmail() {
        val user = build(displayName = "Ada", email = "ada@example.com")
        assertThat(user.fullName).isEqualTo("Ada")
        assertThat(user.email).isEqualTo("ada@example.com")
        assertThat(user.localPhotoPath).isNull()
    }

    @Test
    fun photoUrl_upgradesGoogleThumbnailSize() {
        val user = build(photoUrl = "https://lh3.googleusercontent.com/a/abc=s96-c")
        assertThat(user.photoUrl).isEqualTo("https://lh3.googleusercontent.com/a/abc=s400-c")
    }

    @Test
    fun photoUrl_replacesEveryOccurrence() {
        val user = build(photoUrl = "https://x/s96-c/y/s96-c")
        assertThat(user.photoUrl).isEqualTo("https://x/s400-c/y/s400-c")
    }

    @Test
    fun photoUrl_withoutThumbnailMarker_passesThrough() {
        val user = build(photoUrl = "https://example.com/p.png")
        assertThat(user.photoUrl).isEqualTo("https://example.com/p.png")
    }

    @Test
    fun photoUrl_null_becomesEmptyString() {
        assertThat(build(photoUrl = null).photoUrl).isEmpty()
    }

    @Test
    fun providers_appendsMainProviderWhenMissing() {
        val user = build(providerIds = listOf("google.com"), providerId = "firebase")
        assertThat(user.providers).containsExactly("google.com", "firebase").inOrder()
    }

    @Test
    fun providers_doesNotDuplicateMainProvider() {
        val user = build(providerIds = listOf("password", "firebase"), providerId = "firebase")
        assertThat(user.providers).containsExactly("password", "firebase").inOrder()
    }

    @Test
    fun hasPassword_whenAnyProviderContainsPassword() {
        assertThat(build(providerIds = listOf("password")).hasPassword).isTrue()
        assertThat(build(providerIds = listOf("google.com")).hasPassword).isFalse()
    }

    @Test
    fun isGoogleLinked_whenAnyProviderContainsGoogle() {
        assertThat(build(providerIds = listOf("google.com")).isGoogleLinked).isTrue()
        assertThat(build(providerIds = listOf("password")).isGoogleLinked).isFalse()
    }

    @Test
    fun provider_isGoogleWheneverGoogleIsLinkedEvenWithAPassword() {
        val user = build(providerIds = listOf("password", "google.com"))
        assertThat(user.provider).isEqualTo(User.GOOGLE_PROVIDER)
        assertThat(user.hasPassword).isTrue()
    }

    @Test
    fun provider_isEmailWhenGoogleIsNotLinked() {
        assertThat(build(providerIds = listOf("password")).provider).isEqualTo(User.EMAIL_PROVIDER)
    }

    @Test
    fun creationTimestamp_isSplitIntoYearMonthDayWithOneBasedMonth() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 15, 12, 0, 0)
        }

        val user = build(creationTimestamp = calendar.timeInMillis)

        assertThat(user.creationYear).isEqualTo(2024)
        assertThat(user.creationMonth).isEqualTo(3)
        assertThat(user.creationDay).isEqualTo(15)
        assertThat(user.getCreationDataString()).isEqualTo("15/03/2024")
    }

    @Test
    fun creationTimestamp_null_leavesDateFieldsNull() {
        val user = build(creationTimestamp = null)
        assertThat(user.creationYear).isNull()
        assertThat(user.creationMonth).isNull()
        assertThat(user.creationDay).isNull()
        assertThat(user.getCreationDataString()).isEmpty()
    }
}
