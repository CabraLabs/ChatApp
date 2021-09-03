package com.psandroidlabs.chatapp.utils

import android.content.ContentResolver
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import com.psandroidlabs.chatapp.MainApplication
import java.io.*
import java.util.*

object PictureManager {

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bmp = bitmap.toDrawable(Resources.getSystem()).bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun createUri(name: String): Uri {
        val file =
            File(
                MainApplication.applicationContext().getExternalFilesDir(Constants.IMAGE_DIR),
                name
            )
        return FileProvider.getUriForFile(MainApplication.applicationContext(), "com.psandroidlabs.chatapp.provider_file", file)
    }

    fun bitmapToUri(bitmap: Bitmap, name: String): Uri {
        val file =
            File(
                MainApplication.applicationContext().getExternalFilesDir(Constants.IMAGE_DIR),
                name
            )

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
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
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    fun base64ToBitmap(string: String): Bitmap? {
        var bitmap: Bitmap? = null
        bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byteArrayToBitmap(Base64.decode(string, Base64.NO_WRAP))
        } else {
            BitmapFactory.decodeByteArray(
                Base64.decode(string, Base64.NO_WRAP),
                0,
                Base64.decode(string, Base64.NO_WRAP).size
            )
        }
        return bitmap
    }

    fun setImageName(): String {
        return "${UUID.randomUUID()}.jpg"
    }

    fun getImage (name: String): Bitmap? {
        val file =
            File(
                MainApplication.applicationContext().getExternalFilesDir(Constants.IMAGE_DIR),
                name
            )
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun fileToBitmap(path: String): Bitmap? {
        return if (File(path).exists()) {
            return BitmapFactory.decodeFile(File(path).absolutePath)
        } else {
            null
        }
    }

    fun loadMyAvatar(): Bitmap? {
        val preference = AppPreferences.getClient(MainApplication.applicationContext())
        return if (preference.isNotEmpty() && !preference[3].isNullOrBlank()) {
            preference[2]?.let { getImage(it) }
        } else null
    }

    fun loadMembersAvatar(id: Int): Bitmap? {
        var bitmap: Bitmap? = null
        ChatManager.chatMembersList.forEach {
            if(it.id == id){
                bitmap = it.photoProfile?.let { base64 -> base64ToBitmap(base64) }
            }
        }
        return bitmap
    }
}