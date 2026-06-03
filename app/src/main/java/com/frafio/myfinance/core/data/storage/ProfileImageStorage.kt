package com.frafio.myfinance.core.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PROFILE_PIC_NAME = "profile_pic.png"
    }

    suspend fun saveImage(inputStream: InputStream): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, PROFILE_PIC_NAME)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun loadBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        loadBitmapSync()
    }

    fun loadBitmapSync(): Bitmap? {
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

    fun deleteImage() {
        val file = File(context.filesDir, PROFILE_PIC_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
