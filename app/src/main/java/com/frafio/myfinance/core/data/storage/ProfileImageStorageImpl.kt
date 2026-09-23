package com.frafio.myfinance.core.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileImageStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ProfileImageStorage {
    companion object {
        private const val PROFILE_PIC_NAME = "profile_pic.png"
    }

    override suspend fun saveImage(inputStream: InputStream): String? = withContext(Dispatchers.IO) {
        try {
            // Decode before opening the file: opening it truncates, so undecodable bytes
            // (an error page served with 200, a truncated download) would destroy the stored picture.
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            val file = File(context.filesDir, PROFILE_PIC_NAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        loadBitmapSync()
    }

    override fun loadBitmapSync(): Bitmap? {
        try {
            val file = File(context.filesDir, PROFILE_PIC_NAME)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun deleteImage() {
        val file = File(context.filesDir, PROFILE_PIC_NAME)
        if (file.exists() && !file.delete()) {
            Log.w("ProfileImageStorage", "Could not delete the stored profile picture")
        }
    }
}
