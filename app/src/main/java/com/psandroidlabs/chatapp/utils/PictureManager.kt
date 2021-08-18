package com.psandroidlabs.chatapp.utils

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.drawable.toDrawable
import java.io.ByteArrayOutputStream
import java.util.*

object PictureManager {

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bmp = bitmap.toDrawable(Resources.getSystem()).bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun compressBitmap(bitmap: Bitmap): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun uriToBitmap(uri: Uri?, contentResolver: ContentResolver): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri != null) {
            uri.let {
                ImageDecoder.createSource(
                    contentResolver,
                    it
                )
            }.let { ImageDecoder.decodeBitmap(it) }
        } else {
            BitmapFactory.decodeFileDescriptor(uri?.let {
                contentResolver.openFileDescriptor(
                    it,
                    "r"
                )?.fileDescriptor
            })
        }
    }

    fun bitmapToString(bitmap: Bitmap): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(bitmapToByteArray(compressBitmap(bitmap)))
        } else {
            "aff"
            //TODO
        }
    }

    fun stringToBitmap(string: String): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return byteArrayToBitmap(Base64.getDecoder().decode(string))
        } else {
            Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
            //TODO
        }
    }
}