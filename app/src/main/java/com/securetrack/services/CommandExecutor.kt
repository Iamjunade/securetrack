package com.securetrack.services

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.securetrack.SecureTrackApp
import com.securetrack.utils.CommandParser
import com.securetrack.admin.SecureTrackDeviceAdmin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Command Executor
 * Handles execution of validated SMS commands
 */
object CommandExecutor {

    private const val TAG = "CommandExecutor"

    data class ExecutionResult(
        val success: Boolean,
        val message: String
    )

    /**
     * Execute a validated command
     */
    suspend fun execute(
        context: Context,
        command: CommandParser.Command,
        senderNumber: String,
        logId: Long
    ): ExecutionResult = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "Executing command: ${command.name} from: $senderNumber")

        try {
            when (command) {
                CommandParser.Command.LOCATE -> executeLocate(context, senderNumber, logId)
                CommandParser.Command.SIREN -> executeSiren(context, true)
                CommandParser.Command.STOP_SIREN -> executeSiren(context, false)
                CommandParser.Command.LOCK -> executeLock(context)
                CommandParser.Command.CALLME -> executeCallMe(context, senderNumber)
                CommandParser.Command.WIPE -> executeWipe(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed", e)
            ExecutionResult(false, "Execution failed: ${e.message}")
        }
    }

    /**
     * LOCATE command - Get GPS and send back via SMS
     */
    private suspend fun executeLocate(
        context: Context,
        senderNumber: String,
        logId: Long
    ): ExecutionResult {
        Log.d(TAG, "Starting location tracking for $senderNumber")

        // Start location service with callback intent
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_GET_LOCATION
            putExtra(LocationService.EXTRA_SENDER_NUMBER, senderNumber)
            putExtra(LocationService.EXTRA_LOG_ID, logId)
        }
        context.startForegroundService(intent)

        return ExecutionResult(true, "Location request initiated")
    }

    /**
     * SIREN command - Start/Stop emergency alarm
     */
    private fun executeSiren(context: Context, start: Boolean): ExecutionResult {
        val intent = Intent(context, SirenService::class.java).apply {
            action = if (start) SirenService.ACTION_START_SIREN else SirenService.ACTION_STOP_SIREN
        }

        if (start) {
            context.startForegroundService(intent)
            return ExecutionResult(true, "Siren activated")
        } else {
            context.stopService(intent)
            return ExecutionResult(true, "Siren stopped")
        }
    }

    /**
     * LOCK command - Lock device immediately
     */
    private fun executeLock(context: Context): ExecutionResult {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = SecureTrackDeviceAdmin.getComponentName(context)

        return if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.lockNow()
            ExecutionResult(true, "Device locked")
        } else {
            ExecutionResult(false, "Device admin not active")
        }
    }

    /**
     * CALLME command - Initiate call to sender
     */
    private fun executeCallMe(context: Context, senderNumber: String): ExecutionResult {
        val intent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_CALL
            putExtra(CallService.EXTRA_PHONE_NUMBER, senderNumber)
        }
        context.startService(intent)

        return ExecutionResult(true, "Call initiated to $senderNumber")
    }

    /**
     * WIPE command - Factory reset (requires explicit enable)
     */
    private fun executeWipe(context: Context): ExecutionResult {
        val securePrefs = SecureTrackApp.securePrefs

        // Double-check wipe is enabled
        if (!securePrefs.isWipeEnabled) {
            return ExecutionResult(false, "Wipe feature is disabled")
        }

        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = SecureTrackDeviceAdmin.getComponentName(context)

        return if (devicePolicyManager.isAdminActive(adminComponent)) {
            // Perform factory reset
            devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
            ExecutionResult(true, "Wipe initiated")
        } else {
            ExecutionResult(false, "Device admin not active for wipe")
        }
    }
}
