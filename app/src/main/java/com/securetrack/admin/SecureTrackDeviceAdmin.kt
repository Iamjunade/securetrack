package com.securetrack.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * SecureTrack Device Administrator Receiver
 * Provides device admin capabilities for anti-uninstall and device lock
 */
class SecureTrackDeviceAdmin : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "SecureTrackDeviceAdmin"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, SecureTrackDeviceAdmin::class.java)
        }

        fun isAdminActive(context: Context): Boolean {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) 
                as android.app.admin.DevicePolicyManager
            return devicePolicyManager.isAdminActive(getComponentName(context))
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device admin enabled")
        Toast.makeText(context, "SecureTrack: Device Admin Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device admin disabled")
        Toast.makeText(context, "SecureTrack: Device Admin Disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Log.w(TAG, "Device admin disable requested")
        
        // Return warning message - user will see this when trying to disable admin
        return "⚠️ WARNING: Disabling SecureTrack admin will remove anti-theft protection!\n\n" +
               "If you are the device owner, enter your master password in the app before disabling.\n\n" +
               "If you found this device, please contact the owner."
    }

    override fun onPasswordFailed(context: Context, intent: Intent, userHandle: android.os.UserHandle) {
        super.onPasswordFailed(context, intent, userHandle)
        Log.e(TAG, "!!! PASSWORD FAILED DETECTED !!!") // Error log for visibility
        
        val settings = com.securetrack.utils.SecurePreferences(context)
        val currentFailed = settings.failedUnlockAttempts + 1
        settings.failedUnlockAttempts = currentFailed
        
        Log.d(TAG, "Failed attempts count: $currentFailed")
        
        // Trigger capture immediately on first attempt (>= 1)
        if (currentFailed >= 1) {
            if (!com.securetrack.utils.PermissionHelper.checkCameraPermission(context)) {
                Log.e(TAG, "Cannot capture intruder: Camera permission not granted")
                return
            }

            Log.w(TAG, "Threshold reached! Triggering intruder capture service.")
            val captureIntent = Intent(context, com.securetrack.services.CoreProtectionService::class.java).apply {
                action = com.securetrack.services.CoreProtectionService.ACTION_CAPTURE_INTRUDER
            }
            
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(captureIntent)
                } else {
                    context.startService(captureIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "FAILED to start foreground service: ${e.message}. Trying Fallback.", e)
                e.printStackTrace()
                
                // FALLBACK: Broadcast to an existing service or use AlarmManager?
                // For now, let's log the critical implementation detail:
                // Device Owner/Admin apps usually have exemptions, but if not, we might need a workaround.
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, userHandle: android.os.UserHandle) {
        super.onPasswordSucceeded(context, intent, userHandle)
        Log.d(TAG, "Password succeeded")
        val settings = com.securetrack.utils.SecurePreferences(context)
        settings.failedUnlockAttempts = 0
    }
}
