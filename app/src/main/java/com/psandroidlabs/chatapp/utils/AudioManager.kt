package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

object AudioManager {
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


    fun audioDir(fileName: String, context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File(context.getExternalFilesDir(Constants.AUDIO_DIR), fileName).toString()
        } else {
            File(context.cacheDir, fileName).toString()
        }
    }
}