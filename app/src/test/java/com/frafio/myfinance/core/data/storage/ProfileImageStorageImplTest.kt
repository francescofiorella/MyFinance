package com.frafio.myfinance.core.data.storage

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLog
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Native graphics on purpose: in Robolectric's legacy mode `compress` writes a stub and
 * `decodeStream` returns a 1×1 bitmap, so the sizes asserted here would be meaningless.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileImageStorageImplTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val file = File(context.filesDir, "profile_pic.png")
    private lateinit var subject: ProfileImageStorageImpl

    @Before
    fun setup() {
        file.delete()
        subject = ProfileImageStorageImpl(context)
    }

    private fun pngBytes(width: Int = 8, height: Int = 8): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    @Test
    fun saveImage_writesProfilePicPng_andReturnsItsPath() = runBlocking {
        val path = subject.saveImage(pngBytes().inputStream())

        assertThat(path).isEqualTo(file.absolutePath)
        assertThat(path).endsWith("profile_pic.png")
        assertThat(file.exists()).isTrue()
        assertThat(file.length()).isGreaterThan(0L)
    }

    @Test
    fun saveImage_overwritesThePreviousPicture() = runBlocking {
        subject.saveImage(pngBytes(width = 4, height = 4).inputStream())

        subject.saveImage(pngBytes(width = 16, height = 16).inputStream())

        assertThat(subject.loadBitmap()!!.width).isEqualTo(16)
    }

    @Test
    fun saveImage_withUndecodableBytes_returnsNull() = runBlocking {
        val path = subject.saveImage("not an image".byteInputStream())

        assertThat(path).isNull()
    }

    @Test
    fun saveImage_withUndecodableBytes_keepsThePreviousPicture() = runBlocking {
        subject.saveImage(pngBytes().inputStream())

        subject.saveImage("not an image".byteInputStream())

        assertThat(subject.loadBitmapSync()).isNotNull()
    }

    @Test
    fun loadBitmap_returnsTheSavedImage() = runBlocking {
        subject.saveImage(pngBytes().inputStream())

        val bitmap = subject.loadBitmap()!!

        assertThat(bitmap.width).isEqualTo(8)
        assertThat(bitmap.height).isEqualTo(8)
        assertThat(subject.loadBitmapSync()!!.width).isEqualTo(8)
    }

    @Test
    fun loadBitmap_withoutASavedImage_isNull() = runBlocking {
        assertThat(subject.loadBitmap()).isNull()
        assertThat(subject.loadBitmapSync()).isNull()
    }

    @Test
    fun deleteImage_thatFails_isLogged() {
        // A non-empty directory where the picture belongs cannot be deleted.
        file.mkdirs()
        File(file, "blocker").writeText("x")
        try {
            subject.deleteImage()

            assertThat(file.exists()).isTrue()
            assertThat(ShadowLog.getLogsForTag("ProfileImageStorage").map { it.msg })
                .containsExactly("Could not delete the stored profile picture")
        } finally {
            file.deleteRecursively()
        }
    }

    @Test
    fun deleteImage_removesTheFile() = runBlocking {
        subject.saveImage(pngBytes().inputStream())

        subject.deleteImage()

        assertThat(file.exists()).isFalse()
        assertThat(subject.loadBitmapSync()).isNull()
    }
}
