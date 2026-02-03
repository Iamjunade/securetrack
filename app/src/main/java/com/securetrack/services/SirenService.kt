package com.securetrack.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.ui.MainActivity

/**
 * Siren Service
 * Plays emergency alarm at maximum volume, bypassing DND
 */
class SirenService : Service() {

    companion object {
        private const val TAG = "SirenService"
        private const val NOTIFICATION_ID = 2002

        const val ACTION_START_SIREN = "com.securetrack.action.START_SIREN"
        const val ACTION_STOP_SIREN = "com.securetrack.action.STOP_SIREN"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var originalVolume: Int = 0
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Get vibrator service
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SIREN -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startSiren()
            }
            ACTION_STOP_SIREN -> {
                stopSiren()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startSiren() {
        Log.d(TAG, "Starting siren")

        // Save original volume and set to max
        audioManager?.let { am ->
            originalVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        }

        // Try to use alarm ringtone, fallback to notification
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "Siren playing")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start siren playback", e)
        }

        // Start vibration pattern
        startVibration()
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000) // Vibrate, pause, vibrate...
        
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat from index 0
                v.vibrate(effect)
            }
        }
    }

    private fun stopSiren() {
        Log.d(TAG, "Stopping siren")

        // Stop media player
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        // Stop vibration
        vibrator?.cancel()

        // Restore original volume
        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, SirenService::class.java).apply {
            action = ACTION_STOP_SIREN
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SecureTrackApp.CHANNEL_SIREN)
            .setContentTitle("🚨 EMERGENCY SIREN ACTIVE")
            .setContentText("Tap to open app or use button to stop")
            .setSmallIcon(R.drawable.ic_siren)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_stop, "STOP SIREN", stopPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopSiren()
    }
}
