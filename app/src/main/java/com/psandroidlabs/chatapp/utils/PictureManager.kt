package com.psandroidlabs.chatapp.utils

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.graphics.drawable.toDrawable
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import android.provider.MediaStore.Images
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.psandroidlabs.chatapp.MainApplication
import com.psandroidlabs.chatapp.R
import java.io.FileOutputStream
import java.io.IOException


object PictureManager {

    val defaultAvatar by lazy {
        MainApplication.applicationContext().let { ContextCompat.getDrawable(it, R.drawable.ic_goat)?.toBitmap(200, 200) }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bmp = bitmap.toDrawable(Resources.getSystem()).bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
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

    fun bitmapToUri(bitmap: Bitmap, contentResolver: ContentResolver): Uri {
        val path = Images.Media.insertImage(contentResolver, bitmap, "avatar", null)
        return Uri.parse(path)
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

    fun bitmapToFile(context: Context?, bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(MainApplication.applicationContext())

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }
}