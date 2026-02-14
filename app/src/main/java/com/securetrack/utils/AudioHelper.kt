package com.securetrack.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Audio Recording Helper
 * Handles recording audio from the microphone
 */
class AudioHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    companion object {
        private const val TAG = "AudioHelper"
    }

    @Suppress("DEPRECATION")
    fun startRecording(durationMs: Long, onFinished: (File) -> Unit, onError: (Exception) -> Unit) {
        val outputFile = File(context.filesDir, "audio_capture_${System.currentTimeMillis()}.m4a")

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    Log.d(TAG, "Recording started")

                    // Schedule stop
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        stopRecording(outputFile, onFinished)
                    }, durationMs)

                } catch (e: IOException) {
                    Log.e(TAG, "prepare() failed", e)
                    onError(e)
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "start() failed", e)
                    onError(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaRecorder", e)
            onError(e)
        }
    }

    private fun stopRecording(file: File, onFinished: (File) -> Unit) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d(TAG, "Recording stopped. Saved to: ${file.absolutePath}")
            onFinished(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            // Even if stop fails, the file might exist
            if (file.exists()) {
                onFinished(file)
            }
        }
    }

    fun shutdown() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
