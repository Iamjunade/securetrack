package com.securetrack.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.securetrack.SecureTrackApp
import com.securetrack.services.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.telephony.SmsManager

/**
 * SIM Change Receiver
 * Detects SIM card swap and alerts emergency contacts
 */
class SimChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SimChangeReceiver"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.SIM_STATE_CHANGED") return

        val state = intent.getStringExtra("ss")
        Log.d(TAG, "SIM state changed: $state")

        // Only process when SIM is ready
        if (state != "READY" && state != "LOADED") return

        // Check if setup is complete
        val securePrefs = SecureTrackApp.securePrefs
        if (!securePrefs.isSetupComplete || !securePrefs.isProtectionEnabled) {
            Log.d(TAG, "Protection not enabled, ignoring SIM change")
            return
        }

        // Get current SIM ICCID
        val currentIccid = getCurrentSimIccid(context)
        if (currentIccid == null) {
            Log.w(TAG, "Could not get current SIM ICCID")
            return
        }

        // Compare with stored ICCID
        val originalIccid = securePrefs.originalSimIccid
        if (originalIccid == null) {
            // First time - store current SIM
            securePrefs.originalSimIccid = currentIccid
            Log.d(TAG, "Stored original SIM ICCID")
            return
        }

        if (currentIccid != originalIccid) {
            Log.w(TAG, "SIM CHANGE DETECTED! Original: $originalIccid, Current: $currentIccid")
            handleSimChange(context, currentIccid)
        }
    }

    private fun getCurrentSimIccid(context: Context): String? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.simSerialNumber
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for SIM info", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SIM info", e)
            null
        }
    }

    private fun handleSimChange(context: Context, newIccid: String) {
        scope.launch {
            try {
                // Get emergency contacts
                val contacts = SecureTrackApp.database.emergencyContactDao().getAllContactsList()
                
                if (contacts.isEmpty()) {
                    Log.w(TAG, "No emergency contacts configured")
                    return@launch
                }

                // Get current phone number if available
                val newNumber = getPhoneNumber(context) ?: "Unknown"

                // Prepare alert message
                val alertMessage = buildString {
                    appendLine("⚠️ SECURETRACK ALERT ⚠️")
                    appendLine("SIM card has been changed!")
                    appendLine("New number: $newNumber")
                    appendLine()
                    appendLine("Fetching location...")
                }

                // Send alert to all contacts
                val smsManager = context.getSystemService(SmsManager::class.java)
                contacts.forEach { contact ->
                    try {
                        val parts = smsManager.divideMessage(alertMessage)
                        smsManager.sendMultipartTextMessage(
                            contact.phoneNumber, null, parts, null, null
                        )
                        Log.d(TAG, "SIM change alert sent to ${contact.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send alert to ${contact.name}", e)
                    }
                }

                // Start location fetch and send to primary contact
                val primaryContact = contacts.find { it.isPrimary } ?: contacts.first()
                val locationIntent = Intent(context, LocationService::class.java).apply {
                    action = LocationService.ACTION_GET_LOCATION
                    putExtra(LocationService.EXTRA_SENDER_NUMBER, primaryContact.phoneNumber)
                    putExtra(LocationService.EXTRA_LOG_ID, -1L)
                }
                context.startForegroundService(locationIntent)

            } catch (e: Exception) {
                Log.e(TAG, "Error handling SIM change", e)
            }
        }
    }

    private fun getPhoneNumber(context: Context): String? {
        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val info = subscriptionManager.activeSubscriptionInfoList?.firstOrNull()
            info?.number
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone number", e)
            null
        }
    }
}
