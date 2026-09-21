package com.frafio.myfinance.testing.storage

import android.graphics.Bitmap
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import java.io.InputStream

/**
 * No file system, no decoding: the bitmap is always `null` on the JVM.
 */
class TestProfileImageStorage : ProfileImageStorage {

    var savedImagePath: String? = "/data/user/0/com.frafio.myfinance/files/profile_pic.png"
    var saveCount = 0
        private set
    var deleteCount = 0
        private set

    override suspend fun saveImage(inputStream: InputStream): String? {
        saveCount++
        return savedImagePath
    }

    override suspend fun loadBitmap(): Bitmap? = null

    override fun loadBitmapSync(): Bitmap? = null

    override fun deleteImage() {
        deleteCount++
    }
}
