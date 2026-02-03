package com.securetrack.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.securetrack.SecureTrackApp
import com.securetrack.data.entities.CommandLog
import com.securetrack.data.entities.CommandStatus
import com.securetrack.services.CommandExecutor
import com.securetrack.utils.CommandParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * SMS Broadcast Receiver
 * Listens for incoming SMS and processes SecureTrack commands
 * High priority receiver to intercept commands before other apps
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Check if protection is enabled
        val securePrefs = SecureTrackApp.securePrefs
        if (!securePrefs.isProtectionEnabled || !securePrefs.isSetupComplete) {
            Log.d(TAG, "Protection disabled or setup incomplete, ignoring SMS")
            return
        }

        // Extract SMS messages
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Combine multi-part messages
        val senderNumber = messages[0].displayOriginatingAddress ?: return
        val fullMessage = messages.joinToString("") { it.messageBody ?: "" }

        Log.d(TAG, "Received SMS from: $senderNumber")

        // Quick check if message looks like a command
        if (!CommandParser.isCommandMessage(fullMessage)) {
            Log.d(TAG, "Not a command message, ignoring")
            return
        }

        // Parse the command
        val parsedCommand = CommandParser.parse(fullMessage)
        if (parsedCommand == null) {
            Log.d(TAG, "Failed to parse command")
            return
        }

        Log.d(TAG, "Parsed command: ${parsedCommand.command.name}")

        // Process command asynchronously
        scope.launch {
            processCommand(context, senderNumber, parsedCommand)
        }

        // Optionally abort broadcast to hide command SMS from inbox
        // abortBroadcast()
    }

    private suspend fun processCommand(
        context: Context,
        senderNumber: String,
        parsedCommand: CommandParser.ParsedCommand
    ) {
        val database = SecureTrackApp.database
        val securePrefs = SecureTrackApp.securePrefs
        val commandLogDao = database.commandLogDao()

        // Create initial log entry
        val logId = commandLogDao.insertLog(
            CommandLog(
                command = parsedCommand.command.name,
                senderNumber = senderNumber,
                status = CommandStatus.RECEIVED
            )
        )

        try {
            // Validate PIN format
            if (!parsedCommand.isValid) {
                Log.w(TAG, "Invalid PIN format")
                commandLogDao.updateLogStatus(logId, CommandStatus.UNAUTHORIZED, "Invalid PIN format")
                return
            }

            // Special handling for WIPE command
            if (parsedCommand.command == CommandParser.Command.WIPE) {
                // Check if wipe is enabled
                if (!securePrefs.isWipeEnabled) {
                    Log.w(TAG, "Wipe command received but feature is disabled")
                    commandLogDao.updateLogStatus(logId, CommandStatus.FAILED, "Wipe feature disabled")
                    return
                }
                // Verify wipe PIN (8 digits)
                if (!securePrefs.verifyWipePin(parsedCommand.pin)) {
                    Log.w(TAG, "Invalid wipe PIN")
                    commandLogDao.updateLogStatus(logId, CommandStatus.UNAUTHORIZED, "Invalid wipe PIN")
                    return
                }
            } else {
                // Verify standard PIN
                if (!securePrefs.verifyPin(parsedCommand.pin)) {
                    Log.w(TAG, "Invalid PIN")
                    commandLogDao.updateLogStatus(logId, CommandStatus.UNAUTHORIZED, "Invalid PIN")
                    return
                }
            }

            // Update status to processing
            commandLogDao.updateLogStatus(logId, CommandStatus.PROCESSING, null)

            // Execute command
            val result = CommandExecutor.execute(context, parsedCommand.command, senderNumber, logId)

            // Update final status
            commandLogDao.updateLogStatus(
                logId,
                if (result.success) CommandStatus.SUCCESS else CommandStatus.FAILED,
                result.message
            )

            Log.d(TAG, "Command executed: ${parsedCommand.command.name}, success: ${result.success}")

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            commandLogDao.updateLogStatus(logId, CommandStatus.FAILED, e.message)
        }
    }
}
