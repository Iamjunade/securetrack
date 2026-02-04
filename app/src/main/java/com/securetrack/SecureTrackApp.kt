package com.securetrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.securetrack.data.AppDatabase
import com.securetrack.utils.SecurePreferences

/**
 * SecureTrack Application Class
 * Initializes core components: Database, Notifications, and Secure Preferences
 */
class SecureTrackApp : Application() {

    companion object {
        // Notification Channel IDs
        const val CHANNEL_PROTECTION = "protection_service"
        const val CHANNEL_ALERTS = "security_alerts"
        const val CHANNEL_SIREN = "emergency_siren"

        lateinit var database: AppDatabase
            private set

        lateinit var securePrefs: SecurePreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "securetrack_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        // Initialize Secure Preferences
        securePrefs = SecurePreferences(applicationContext)

        // Initialize Crash Handler (1M User Reliability)
        com.securetrack.utils.CrashHandler.init(applicationContext)

        // Create Notification Channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Protection Service Channel (Low importance - persistent but silent)
        val protectionChannel = NotificationChannel(
            CHANNEL_PROTECTION,
            "Protection Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when SecureTrack protection is active"
            setShowBadge(false)
        }

        // Security Alerts Channel (High importance)
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Security Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Important security notifications"
            enableVibration(true)
            setShowBadge(true)
        }

        // Emergency Siren Channel (Max importance - bypasses DND)
        val sirenChannel = NotificationChannel(
            CHANNEL_SIREN,
            "Emergency Siren",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Emergency alarm notifications"
            enableVibration(true)
            setBypassDnd(true)
        }

        notificationManager.createNotificationChannels(
            listOf(protectionChannel, alertsChannel, sirenChannel)
        )
    }
}
