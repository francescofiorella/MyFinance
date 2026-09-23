package com.frafio.myfinance.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.manager.AuthManager
import com.frafio.myfinance.core.data.storage.ProfileImageStorageImpl
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.remote.TestAuthDataSource
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import java.io.File

/** The profile-picture download, over a real OkHttp client and the real storage. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UserRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = TestUserPreferencesRepository()
    private val server = MockWebServer()
    private lateinit var storage: ProfileImageStorageImpl
    private lateinit var subject: UserRepositoryImpl

    private val avatarUrl get() = server.url("/avatar.png").toString()

    @Before
    fun setup() {
        File(context.filesDir, "profile_pic.png").delete()
        server.start()
        storage = ProfileImageStorageImpl(context)
        val authManager = AuthManager(
            TestAuthDataSource(), preferences, TestExpensesLocalRepository(), TestIncomesLocalRepository(),
            TestProfileImageStorage(), UnconfinedTestDispatcher(),
        )
        subject = UserRepositoryImpl(authManager, preferences, storage, OkHttpClient())
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    private fun pngResponse(): MockResponse {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val bytes = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
        return MockResponse().setBody(Buffer().write(bytes))
    }

    private val storedUser get() = preferences.userPreferencesFlow.value.user

    @Test
    fun syncProfilePicture_downloadsTheImage_storesIt_andUpdatesThePreferences() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(pngResponse())

        subject.syncProfilePicture(avatarUrl)

        assertThat(server.takeRequest().path).isEqualTo("/avatar.png")
        assertThat(storedUser?.localPhotoPath).endsWith("profile_pic.png")
        assertThat(storage.loadBitmapSync()).isNotNull()
        assertThat(subject.profilePicture.first()).isNotNull()
    }

    @Test
    fun syncProfilePicture_withoutAUrl_makesNoRequest() = runBlocking {
        preferences.setUser(testUser(photoUrl = null))

        subject.syncProfilePicture(null)
        // UserMapper turns a missing Google picture into an empty string, not null.
        subject.syncProfilePicture("")

        assertThat(server.requestCount).isEqualTo(0)
        assertThat(storedUser?.localPhotoPath).isNull()
    }

    @Test
    fun syncProfilePicture_whenAlreadyDownloaded_makesNoRequest() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl).copy(localPhotoPath = "/data/profile_pic.png"))

        subject.syncProfilePicture(avatarUrl)

        assertThat(server.requestCount).isEqualTo(0)
    }

    @Test
    fun syncProfilePicture_afterThePictureChanges_downloadsAgain() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl).copy(localPhotoPath = "/data/profile_pic.png"))
        server.enqueue(pngResponse())
        val newUrl = server.url("/new-avatar.png").toString()

        subject.syncProfilePicture(newUrl)

        assertThat(server.takeRequest().path).isEqualTo("/new-avatar.png")
        assertThat(storedUser?.localPhotoPath).endsWith("profile_pic.png")
    }

    @Test
    fun syncProfilePicture_serverError_storesNothing() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(MockResponse().setResponseCode(404))

        subject.syncProfilePicture(avatarUrl)

        assertThat(server.requestCount).isEqualTo(1)
        assertThat(storedUser?.localPhotoPath).isNull()
        assertThat(subject.profilePicture.first()).isNull()
    }

    @Test
    fun syncProfilePicture_undecodableBody_storesNothing() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(MockResponse().setBody("<html>sign in to the network</html>"))

        subject.syncProfilePicture(avatarUrl)

        assertThat(storedUser?.localPhotoPath).isNull()
        assertThat(storage.loadBitmapSync()).isNull()
    }

    @Test
    fun syncProfilePicture_networkFailure_isSwallowed() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        val deadUrl = avatarUrl
        server.shutdown()

        subject.syncProfilePicture(deadUrl)

        assertThat(storedUser?.localPhotoPath).isNull()
    }

    @Test
    fun syncProfilePicture_withoutAUser_doesNotWritePreferences() = runBlocking {
        preferences.setUser(null)
        server.enqueue(pngResponse())

        subject.syncProfilePicture(avatarUrl)

        assertThat(server.requestCount).isEqualTo(1)
        assertThat(storedUser).isNull()
        assertThat(subject.profilePicture.first()).isNull()
    }

    @Test
    fun logout_clearsTheProfilePicture() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(pngResponse())
        subject.syncProfilePicture(avatarUrl)
        assertThat(subject.profilePicture.first()).isNotNull()

        subject.userLogout()

        assertThat(subject.profilePicture.first()).isNull()
    }
}
