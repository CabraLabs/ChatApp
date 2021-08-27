package com.psandroidlabs.chatapp.utils

import android.app.Dialog
import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.psandroidlabs.chatapp.MainApplication
import com.psandroidlabs.chatapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.*
import java.util.*

object PictureManager {

    val defaultAvatar by lazy {
        MainApplication.applicationContext()
            .let { ContextCompat.getDrawable(it, R.drawable.ic_goat)?.toBitmap(200, 200) }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
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
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    fun base64ToBitmap(string: String): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return byteArrayToBitmap(Base64.decode(string, Base64.NO_WRAP))
        } else {
            BitmapFactory.decodeByteArray(
                Base64.decode(string, Base64.NO_WRAP),
                0,
                Base64.decode(string, Base64.NO_WRAP).size
            )
        }
    }

    fun bitmapToFile(bitmap: Bitmap): Uri {
        val file =
            File(
                MainApplication.applicationContext().getExternalFilesDir(Constants.IMAGE_DIR),
                "${UUID.randomUUID()}.jpg"
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
            preference[2]?.let { fileToBitmap(it) }
        } else null
    }

    fun loadMembersAvatar(id: Int): Bitmap? {
        ChatManager.chatMembersList.forEach {
            if(it.id == id){
                return it.photoProfile?.let { it1 -> PictureManager.base64ToBitmap(it1) }
            }
        }
        return null
    }
}