package com.securetrack.utils

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.securetrack.admin.SecureTrackDeviceAdmin
import com.securetrack.services.StatusBarBlockerService

/**
 * Permission Helper
 * Manages all permission checks and requests for SecureTrack
 */
object PermissionHelper {

    data class PermissionStatus(
        val smsGranted: Boolean,
        val locationGranted: Boolean,
        val backgroundLocationGranted: Boolean,
        val phoneGranted: Boolean,
        val deviceAdminActive: Boolean,
        val accessibilityEnabled: Boolean,
        val notificationPolicyAccess: Boolean,
        val allGranted: Boolean
    )

    /**
     * Check all required permissions
     */
    fun checkAllPermissions(context: Context): PermissionStatus {
        val sms = checkSmsPermissions(context)
        val location = checkLocationPermission(context)
        val bgLocation = checkBackgroundLocationPermission(context)
        val phone = checkPhonePermission(context)
        val admin = checkDeviceAdmin(context)
        val accessibility = checkAccessibilityService(context)
        val notification = checkNotificationPolicyAccess(context)

        return PermissionStatus(
            smsGranted = sms,
            locationGranted = location,
            backgroundLocationGranted = bgLocation,
            phoneGranted = phone,
            deviceAdminActive = admin,
            accessibilityEnabled = accessibility,
            notificationPolicyAccess = notification,
            allGranted = sms && location && bgLocation && phone && admin && accessibility && notification
        )
    }

    // ============ Individual Permission Checks ============

    fun checkSmsPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBackgroundLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPhonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    fun checkDeviceAdmin(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = SecureTrackDeviceAdmin.getComponentName(context)
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    fun checkAccessibilityService(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val serviceClass = StatusBarBlockerService::class.java.name
        
        return enabledServices.any { 
            it.resolveInfo.serviceInfo.name == serviceClass 
        }
    }

    fun checkNotificationPolicyAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    // ============ Permission Request Intents ============

    fun getSmsPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
        )
    }

    fun getLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun getBackgroundLocationPermission(): String {
        return Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }

    fun getPhonePermission(): String {
        return Manifest.permission.CALL_PHONE
    }

    fun getDeviceAdminIntent(context: Context): Intent {
        val adminComponent = SecureTrackDeviceAdmin.getComponentName(context)
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "SecureTrack needs Device Admin to lock your device and prevent unauthorized uninstallation."
            )
        }
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun getNotificationPolicyIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    }

    // ============ Write Settings (for Airplane Mode Control) ============

    fun checkWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun getWriteSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}

