package com.securetrack.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.lang.reflect.Method

/**
 * Status Bar Blocker Service
 * Accessibility service that prevents notification shade from being pulled down on lock screen
 */
class StatusBarBlockerService : AccessibilityService() {

    companion object {
        private const val TAG = "StatusBarBlocker"
        
        // Status bar package name
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        
        // Known notification shade class names
        private val NOTIFICATION_SHADE_CLASSES = listOf(
            "com.android.systemui.statusbar.phone.StatusBarWindowView",
            "com.android.systemui.shade.NotificationShadeWindowView",
            "com.android.systemui.statusbar.phone.PhoneStatusBarView",
            "android.widget.FrameLayout"  // Fallback
        )
    }

    private var keyguardManager: KeyguardManager? = null
    private var lastCollapseTime = 0L
    private val collapseDebounceMs = 500L

    override fun onCreate() {
        super.onCreate()
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        Log.d(TAG, "StatusBarBlockerService created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Configure accessibility service settings
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
            // packageNames = arrayOf(SYSTEM_UI_PACKAGE) // Listen to all packages to catch Power Menu
        }
        serviceInfo = info
        
        Log.d(TAG, "StatusBarBlockerService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only block/trigger when device is locked
        if (!isDeviceLocked()) return

        // 1. Check for Power Menu (Fake Shutdown Trigger)
        if (isPowerMenuEvent(event)) {
            Log.d(TAG, "Power Menu detected while locked! Initiating Fake Shutdown...")
            triggerFakeShutdown()
            // Try to dismiss the real power menu if possible
            performGlobalAction(GLOBAL_ACTION_BACK) 
            return
        }

        // 2. Check for Notification Shade (Status Bar Blocker)
        if (event.packageName?.toString() == SYSTEM_UI_PACKAGE) {
            val className = event.className?.toString() ?: return
            if (isNotificationShadeEvent(className, event)) {
                Log.d(TAG, "Notification shade detected while locked, collapsing...")
                collapseStatusBar()
            }
        }
    }

    private fun isPowerMenuEvent(event: AccessibilityEvent): Boolean {
        // Check text content for power off keywords
        val textList = event.text
        for (text in textList) {
            val content = text.toString().lowercase()
            if (content.contains("power off") || 
                content.contains("shut down") || 
                content.contains("restart") ||
                content.contains("emergency mode")) {
                return true
            }
        }
        
        // Also check content description
        val contentDesc = event.contentDescription?.toString()?.lowercase() ?: ""
        if (contentDesc.contains("power off") || contentDesc.contains("shut down")) {
            return true
        }

        return false
    }

    private fun triggerFakeShutdown() {
        val intent = android.content.Intent(this, com.securetrack.ui.FakeShutdownActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }

    private fun isDeviceLocked(): Boolean {
        return keyguardManager?.isKeyguardLocked == true
    }

    private fun isNotificationShadeEvent(className: String, event: AccessibilityEvent): Boolean {
        // Check if class matches known notification shade classes
        if (NOTIFICATION_SHADE_CLASSES.any { className.contains(it, ignoreCase = true) }) {
            return true
        }

        // Check content description for notification-related text
        val contentDesc = event.contentDescription?.toString()?.lowercase() ?: ""
        if (contentDesc.contains("notification") || contentDesc.contains("shade")) {
            return true
        }

        return false
    }

    private fun collapseStatusBar() {
        val now = System.currentTimeMillis()
        if (now - lastCollapseTime < collapseDebounceMs) {
            return // Debounce to prevent rapid firing
        }
        lastCollapseTime = now

        try {
            // Use reflection to call StatusBarManager.collapsePanels()
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            
            val collapseMethod: Method = try {
                statusBarManager.getMethod("collapsePanels")
            } catch (e: NoSuchMethodException) {
                // Try alternative method name
                statusBarManager.getMethod("collapse")
            }
            
            collapseMethod.invoke(statusBarService)
            Log.d(TAG, "Status bar collapsed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collapse status bar", e)
            
            // Fallback: Use global action to go home (less intrusive but still blocks access)
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "StatusBarBlockerService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "StatusBarBlockerService destroyed")
    }
}
