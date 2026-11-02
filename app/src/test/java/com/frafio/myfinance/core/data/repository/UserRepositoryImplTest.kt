package com.frafio.myfinance.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.manager.AuthManager
import com.frafio.myfinance.core.data.remote.AuthException
import com.frafio.myfinance.core.data.storage.ProfileImageStorageImpl
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.remote.TestAuthDataSource
import com.frafio.myfinance.testing.remote.TestRemoteDataSource
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
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
import java.util.concurrent.TimeUnit

/** The profile-picture download and its restore at login, over a real OkHttp client and the real storage. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UserRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = TestUserPreferencesRepository()
    private val auth = TestAuthDataSource()
    private val remote = TestRemoteDataSource()
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
            auth, preferences, TestExpensesLocalRepository(), TestIncomesLocalRepository(),
            TestProfileImageStorage(), UnconfinedTestDispatcher(),
        )
        subject = UserRepositoryImpl(authManager, preferences, storage, OkHttpClient(), remote)
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
    private val storedChoice get() = preferences.userPreferencesFlow.value.proPicChoice

    private val choiceField = FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value
    private val google = FirestoreEnums.PRO_PIC_TYPES.GOOGLE.value

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
    fun syncProfilePicture_serverError_releasesTheConnection() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))
        server.enqueue(pngResponse())

        subject.syncProfilePicture(avatarUrl)
        subject.syncProfilePicture(server.url("/new-avatar.png").toString())

        server.takeRequest()
        // OkHttp reuses a connection only after the previous response on it was closed.
        assertThat(server.takeRequest().sequenceNumber).isEqualTo(1)
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
    fun concurrentSyncsOfTheSamePicture_downloadItOnce() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(pngResponse().setBodyDelay(500, TimeUnit.MILLISECONDS))
        server.enqueue(pngResponse())

        val first = launch(Dispatchers.Default) { subject.syncProfilePicture(avatarUrl) }
        val second = launch(Dispatchers.Default) { subject.syncProfilePicture(avatarUrl) }
        joinAll(first, second)

        assertThat(server.requestCount).isEqualTo(1)
        assertThat(subject.profilePicture.first()).isNotNull()
    }

    @Test
    fun logoutDuringASync_stopsTheDownload_andNothingIsStoredAfterwards() = runBlocking {
        preferences.setUser(testUser(photoUrl = avatarUrl))
        server.enqueue(pngResponse().setBodyDelay(10, TimeUnit.SECONDS))
        val sync = async(Dispatchers.Default) { subject.syncProfilePicture(avatarUrl) }
        server.takeRequest()

        val start = System.currentTimeMillis()
        subject.userLogout()
        sync.await()

        assertThat(System.currentTimeMillis() - start).isLessThan(2_000)
        assertThat(storage.loadBitmapSync()).isNull()
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

    // region login

    @Test
    fun login_withTheGooglePictureChosen_downloadsItBeforeReturning() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = google
        server.enqueue(pngResponse())

        val result = subject.userLogin("ada@example.com", "password123")

        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(storedChoice).isEqualTo(google)
        assertThat(server.takeRequest().path).isEqualTo("/avatar.png")
        assertThat(storedUser?.localPhotoPath).endsWith("profile_pic.png")
        assertThat(subject.profilePicture.first()).isNotNull()
    }

    @Test
    fun login_withAnAvatarChosen_storesTheChoice_andDownloadsNothing() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = "avatar_3"

        subject.userLogin("ada@example.com", "password123")

        assertThat(storedChoice).isEqualTo("avatar_3")
        assertThat(server.requestCount).isEqualTo(0)
    }

    @Test
    fun login_withoutARemoteChoice_clearsTheStoredOne() = runBlocking {
        preferences.updateProPicChoice("avatar_1")

        subject.userLogin("ada@example.com", "password123")

        assertThat(storedChoice).isNull()
        assertThat(server.requestCount).isEqualTo(0)
    }

    @Test
    fun login_whenTheChoiceCannotBeRead_stillSucceeds_andKeepsTheStoredChoice() = runBlocking {
        preferences.updateProPicChoice("avatar_1")
        remote.failNext = IllegalStateException("offline")

        val result = subject.userLogin("ada@example.com", "password123")

        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(storedChoice).isEqualTo("avatar_1")
    }

    @Test
    fun login_failure_readsNothing() = runBlocking {
        auth.signInFailure = AuthException(AuthException.Kind.INVALID_CREDENTIALS)

        val result = subject.userLogin("ada@example.com", "wrong")

        assertThat(result.code).isNotEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(remote.getUserFieldsCalls).isEqualTo(0)
    }

    @Test
    fun login_slowDownload_returnsAtTheTimeout_andTheDownloadCompletesInTheBackground() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = google
        server.enqueue(pngResponse().setBodyDelay(UserRepositoryImpl.PROFILE_PICTURE_TIMEOUT_MS + 2_000, TimeUnit.MILLISECONDS))

        val start = System.currentTimeMillis()
        val result = subject.userLogin("ada@example.com", "password123")

        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(System.currentTimeMillis() - start).isLessThan(UserRepositoryImpl.PROFILE_PICTURE_TIMEOUT_MS + 1_500)
        withTimeout(10_000) { subject.profilePicture.first { it != null } }
        assertThat(storedUser?.localPhotoPath).endsWith("profile_pic.png")
    }

    @Test
    fun syncAfterASlowLogin_waitsForTheLoginDownload_insteadOfDownloadingAgain() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = google
        server.enqueue(pngResponse().setBodyDelay(UserRepositoryImpl.PROFILE_PICTURE_TIMEOUT_MS + 1_000, TimeUnit.MILLISECONDS))
        server.enqueue(pngResponse())

        subject.userLogin("ada@example.com", "password123")
        subject.syncProfilePicture(avatarUrl)

        assertThat(server.requestCount).isEqualTo(1)
        assertThat(subject.profilePicture.first()).isNotNull()
    }

    @Test
    fun syncAfterAFailedLoginDownload_downloadsAgain() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = google
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(pngResponse())

        subject.userLogin("ada@example.com", "password123")
        subject.syncProfilePicture(avatarUrl)

        assertThat(server.requestCount).isEqualTo(2)
        assertThat(subject.profilePicture.first()).isNotNull()
    }

    @Test
    fun logoutDuringTheLoginDownload_stopsIt_andNothingIsStoredAfterwards() = runBlocking {
        auth.photoUrl = avatarUrl
        remote.userFields[choiceField] = google
        server.enqueue(pngResponse().setBodyDelay(10, TimeUnit.SECONDS))
        subject.userLogin("ada@example.com", "password123")

        val start = System.currentTimeMillis()
        subject.userLogout()

        assertThat(System.currentTimeMillis() - start).isLessThan(2_000)
        assertThat(storedUser).isNull()
        assertThat(storage.loadBitmapSync()).isNull()
        assertThat(subject.profilePicture.first()).isNull()
    }

    // endregion
}
