package com.securetrack.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.securetrack.SecureTrackApp
import com.securetrack.services.CoreProtectionService

/**
 * Boot Receiver
 * Starts protection service when device boots
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON"
        )

        if (intent.action !in validActions) return

        Log.d(TAG, "Device booted, action: ${intent.action}")

        // Check if setup is complete and protection is enabled
        val securePrefs = SecureTrackApp.securePrefs
        if (!securePrefs.isSetupComplete) {
            Log.d(TAG, "Setup not complete, skipping service start")
            return
        }

        if (!securePrefs.isProtectionEnabled) {
            Log.d(TAG, "Protection disabled, skipping service start")
            return
        }

        // Start the core protection service
        Log.d(TAG, "Starting CoreProtectionService")
        val serviceIntent = Intent(context, CoreProtectionService::class.java).apply {
            action = CoreProtectionService.ACTION_START
        }
        
        try {
            context.startForegroundService(serviceIntent)
            Log.d(TAG, "CoreProtectionService started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start CoreProtectionService", e)
        }
    }
}
