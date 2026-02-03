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
        Log.w(TAG, "Password failed attempt detected")
        // Could trigger siren or location fetch here for added security
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, userHandle: android.os.UserHandle) {
        super.onPasswordSucceeded(context, intent, userHandle)
        Log.d(TAG, "Password succeeded")
    }
}
