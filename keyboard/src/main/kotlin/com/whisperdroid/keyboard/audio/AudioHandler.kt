package com.whisperdroid.keyboard.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioHandler(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        try {
            audioFile = File(context.cacheDir, "recording.m4a")
            if (audioFile?.exists() == true) {
                audioFile?.delete()
            }

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioChannels(1)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioHandler", "Recording started: ${audioFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("AudioHandler", "Failed to start recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            throw e
        }
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            Log.d("AudioHandler", "Recording stopped")
        } catch (e: Exception) {
            Log.e("AudioHandler", "Failed to stop recording", e)
        } finally {
            mediaRecorder = null
        }
        return audioFile
    }

    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
