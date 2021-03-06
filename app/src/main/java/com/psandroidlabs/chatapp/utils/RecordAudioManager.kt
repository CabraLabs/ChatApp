package com.psandroidlabs.chatapp.utils

import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import java.io.File
import java.io.FileOutputStream


object RecordAudioManager {
    /**
     * Create and return a MediaRecorder to record audio messages.
     */
    fun createAudioRecorder(audioPath: String) = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(audioPath)
    }

    fun nameAudio(): String = ChatManager.getEpoch().toString() + ".mp3"

    fun audioDir(fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File(applicationContext().getExternalFilesDir(Constants.AUDIO_DIR), fileName).toString()
        } else {
            File(applicationContext().cacheDir, fileName).toString()
        }
    }

    fun audioBase64(path: String): String {
        val file = File(path)
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }

    fun base64toAudio(base64: String?): String? {
        return try {
            val audio = Base64.decode(base64, Base64.NO_WRAP)
            val audioName = nameAudio()

            val filePath = audioDir(audioName)

            FileOutputStream(filePath).apply {
                write(audio)
                flush()
                close()
            }

            audioName
        } catch (e: java.lang.IllegalArgumentException) {
            null
        }
    }
}