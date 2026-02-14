package com.securetrack.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.ui.MainActivity

import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.launch

/**
 * Core Protection Service
 * Persistent foreground service that keeps SecureTrack running
 * Ensures SMS receiver is always active even when app is in background
 */
class CoreProtectionService : LifecycleService() {

    companion object {
        private const val TAG = "CoreProtectionService"
        private const val NOTIFICATION_ID = 2000

        const val ACTION_START = "com.securetrack.action.START_PROTECTION"
        const val ACTION_STOP = "com.securetrack.action.STOP_PROTECTION"
        const val ACTION_CAPTURE_INTRUDER = "com.securetrack.action.CAPTURE_INTRUDER"
    }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CoreProtectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Starting protection service")
                startForeground(NOTIFICATION_ID, createNotification())
            }
            ACTION_CAPTURE_INTRUDER -> {
                Log.w(TAG, "Available for Capture!")
                captureIntruder()
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping protection service")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY // Restart if killed
    }
    
    private fun captureIntruder() {
        val helper = com.securetrack.utils.CameraHelper(this, this)
        helper.takeSilentSelfie(
            onImageSaved = { file ->
                Log.d(TAG, "Intruder captured: ${file.absolutePath}")
                
                // Save to DB
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val log = com.securetrack.data.IntruderLog(
                        imagePath = file.absolutePath,
                        timestamp = System.currentTimeMillis(),
                        location = "Unknown" // TODO: Get last known location
                    )
                    SecureTrackApp.database.intruderLogDao().insertLog(log)

                    // ALSO Add to Command Logs so user sees it in the main list
                    SecureTrackApp.database.commandLogDao().insertLog(
                        com.securetrack.data.entities.CommandLog(
                            command = "INTRUDER_ALERT",
                            response = "Photo captured: ${file.name}",
                            timestamp = System.currentTimeMillis(),
                            isSuccess = true
                        )
                    )
                }
                
                // Helper shutdown handled by garbage collection or we can make it singleton
                helper.shutdown()
            },
            onError = { exc ->
                Log.e(TAG, "Failed to capture intruder", exc)
                helper.shutdown()
            }
        )
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SecureTrackApp.CHANNEL_PROTECTION)
            .setContentTitle("SecureTrack Active")
            .setContentText("Protection is running")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CoreProtectionService destroyed")
        
        // Restart service if protection is enabled
        if (SecureTrackApp.securePrefs.isProtectionEnabled) {
            val restartIntent = Intent(this, CoreProtectionService::class.java).apply {
                action = ACTION_START
            }
            startForegroundService(restartIntent)
        }
    }
}
