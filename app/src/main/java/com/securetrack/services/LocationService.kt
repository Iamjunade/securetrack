package com.securetrack.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.data.entities.CommandStatus
import com.securetrack.ui.MainActivity
import kotlinx.coroutines.*

/**
 * Location Service using Adaptive Battery Strategy
 * Fetches GPS coordinates and sends via SMS
 */
class LocationService : Service() {

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 2001
        private const val LOCATION_TIMEOUT_MS = 30000L // 30 seconds

        const val ACTION_GET_LOCATION = "com.securetrack.action.GET_LOCATION"
        const val EXTRA_SENDER_NUMBER = "sender_number"
        const val EXTRA_LOG_ID = "log_id"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        when (intent?.action) {
            ACTION_GET_LOCATION -> {
                val senderNumber = intent.getStringExtra(EXTRA_SENDER_NUMBER)
                val logId = intent.getLongExtra(EXTRA_LOG_ID, -1)
                
                if (senderNumber != null) {
                    fetchAndSendLocation(senderNumber, logId)
                } else {
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun fetchAndSendLocation(senderNumber: String, logId: Long) {
        Log.d(TAG, "Fetching location for $senderNumber")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted")
            sendLocationSms(senderNumber, null, logId)
            stopSelf()
            return
        }

        // Try to get last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null && isLocationFresh(lastLocation)) {
                Log.d(TAG, "Using last known location")
                sendLocationSms(senderNumber, lastLocation, logId)
                stopSelf()
            } else {
                // Request fresh location
                requestFreshLocation(senderNumber, logId)
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to get last location", it)
            requestFreshLocation(senderNumber, logId)
        }
    }

    private fun isLocationFresh(location: Location): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age < 60000 // Less than 1 minute old
    }

    /**
     * High Precision Location Strategy
     * FORCED MAX ACCURACY as per user request.
     * Ignores battery constraints to ensure precise GPS triangulation.
     */
    private fun requestFreshLocation(senderNumber: String, logId: Long) {
        Log.d(TAG, "Requesting fresh location (High Precision Mode)")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            sendLocationSms(senderNumber, null, logId)
            stopSelf()
            return
        }

        // Force High Accuracy regardless of battery
        val priority = Priority.PRIORITY_HIGH_ACCURACY

        val locationRequest = LocationRequest.Builder(priority, 1000)
            .setWaitForAccurateLocation(true) // Wait for GPS lock
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdates(1)
            .build()
        
        Log.d(TAG, "Requesting location with priority: $priority")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    Log.d(TAG, "Got precise location: ${location.latitude}, ${location.longitude} (Acc: ${location.accuracy}m)")
                    sendLocationSms(senderNumber, location, logId)
                    cleanup()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        // Timeout handler - Keep 30s as it's enough for GPS lock usually
        scope.launch {
            delay(LOCATION_TIMEOUT_MS)
            if (locationCallback != null) {
                Log.w(TAG, "Location request timed out")
                sendLocationSms(senderNumber, null, logId)
                cleanup()
            }
        }
    }

    private fun sendLocationSms(senderNumber: String, location: Location?, logId: Long) {
        scope.launch {
            try {
                val message = if (location != null) {
                    val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    val accuracy = "Accuracy: ${location.accuracy.toInt()}m"
                    "SecureTrack Location:\n$mapsUrl\n$accuracy"
                } else {
                    "SecureTrack: Unable to get location. GPS may be disabled or unavailable."
                }

                // Send SMS
                val smsManager = getSystemService(SmsManager::class.java)
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(senderNumber, null, parts, null, null)

                Log.d(TAG, "Location SMS sent to $senderNumber")

                // Update log with location if available
                if (logId != -1L && location != null) {
                    SecureTrackApp.database.commandLogDao().updateLogLocation(
                        logId, location.latitude, location.longitude
                    )
                    SecureTrackApp.database.commandLogDao().updateLogStatus(
                        logId, CommandStatus.SUCCESS, "Location sent: ${location.latitude}, ${location.longitude}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SMS", e)
                if (logId != -1L) {
                    SecureTrackApp.database.commandLogDao().updateLogStatus(
                        logId, CommandStatus.FAILED, "SMS send failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun cleanup() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SecureTrackApp.CHANNEL_PROTECTION)
            .setContentTitle("Fetching Location")
            .setContentText("Getting device location...")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        scope.cancel()
    }
}
