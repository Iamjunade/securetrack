package com.securetrack.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import com.securetrack.SecureTrackApp
import com.securetrack.utils.SecurePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Detects when Airplane Mode is enabled and attempts to reverse it.
 * Also queues an alert SMS to be sent when connectivity is restored.
 */
class AirplaneModeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AirplaneModeReceiver"
        private const val MAX_REVERSAL_ATTEMPTS = 3
        private const val REVERSAL_DELAY_MS = 2000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_AIRPLANE_MODE_CHANGED) return

        val isAirplaneModeOn = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0

        Log.d(TAG, "Airplane mode changed: $isAirplaneModeOn")

        val prefs = SecurePreferences(context)
        if (!prefs.isProtectionEnabled) {
            Log.d(TAG, "Protection not enabled, ignoring")
            return
        }

        if (isAirplaneModeOn) {
            Log.w(TAG, "Airplane mode ENABLED - attempting reversal")
            
            // Mark that we detected airplane mode (for alert when restored)
            prefs.setAirplaneModeDetected(true)
            
            // Attempt to reverse airplane mode
            CoroutineScope(Dispatchers.IO).launch {
                attemptAirplaneModeReversal(context)
            }
        } else {
            // Airplane mode was turned OFF
            if (prefs.wasAirplaneModeDetected()) {
                Log.i(TAG, "Airplane mode disabled - sending alert")
                prefs.setAirplaneModeDetected(false)
                
                // Send alert to emergency contacts
                sendAirplaneModeAlert(context)
            }
        }
    }

    private suspend fun attemptAirplaneModeReversal(context: Context) {
        for (attempt in 1..MAX_REVERSAL_ATTEMPTS) {
            Log.d(TAG, "Reversal attempt $attempt of $MAX_REVERSAL_ATTEMPTS")
            
            try {
                // Method 1: Try Settings.Global (requires WRITE_SETTINGS)
                val success = disableAirplaneMode(context)
                if (success) {
                    Log.i(TAG, "Successfully disabled airplane mode on attempt $attempt")
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reversal attempt $attempt failed: ${e.message}")
            }
            
            delay(REVERSAL_DELAY_MS)
            
            // Check if already disabled
            val isStillOn = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            
            if (!isStillOn) {
                Log.i(TAG, "Airplane mode already disabled")
                return
            }
        }
        
        Log.w(TAG, "Failed to reverse airplane mode after $MAX_REVERSAL_ATTEMPTS attempts")
    }

    private fun disableAirplaneMode(context: Context): Boolean {
        return try {
            // This requires WRITE_SETTINGS permission
            // On Android 10+, this may require the user to grant it manually
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            )
            
            // Broadcast the change
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            intent.putExtra("state", false)
            context.sendBroadcast(intent)
            
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot modify airplane mode: ${e.message}")
            // Try alternative method - open airplane mode settings
            tryOpenAirplaneSettings(context)
            false
        }
    }

    private fun tryOpenAirplaneSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open airplane settings: ${e.message}")
        }
    }

    private fun sendAirplaneModeAlert(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contacts = SecureTrackApp.database.emergencyContactDao().getAllContactsSync()
                
                if (contacts.isEmpty()) {
                    Log.w(TAG, "No emergency contacts to alert")
                    return@launch
                }

                val smsManager = SmsManager.getDefault()
                val message = "SecureTrack Alert: Airplane mode was detected and reversed on this device. " +
                        "Someone may have attempted to disable connectivity."

                contacts.forEach { contact ->
                    try {
                        smsManager.sendTextMessage(
                            contact.phoneNumber,
                            null,
                            message,
                            null,
                            null
                        )
                        Log.i(TAG, "Alert sent to ${contact.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send alert to ${contact.name}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending airplane mode alerts: ${e.message}")
            }
        }
    }
}
