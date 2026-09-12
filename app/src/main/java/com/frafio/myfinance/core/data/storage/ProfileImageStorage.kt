package com.frafio.myfinance.core.data.storage

import android.graphics.Bitmap
import java.io.InputStream

interface ProfileImageStorage {
    suspend fun saveImage(inputStream: InputStream): String?
    suspend fun loadBitmap(): Bitmap?
    fun loadBitmapSync(): Bitmap?
    fun deleteImage()
}
