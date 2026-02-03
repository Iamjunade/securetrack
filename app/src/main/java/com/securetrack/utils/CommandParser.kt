package com.securetrack.utils

/**
 * Command Parser
 * Parses and validates SMS commands with PIN verification
 * 
 * Command Format: #COMMAND_PIN
 * Example: #LOCATE_123456
 */
object CommandParser {

    /**
     * All supported commands
     */
    enum class Command(val prefix: String, val description: String) {
        LOCATE("#LOCATE_", "Get device GPS location"),
        SIREN("#SIREN_", "Trigger emergency alarm"),
        LOCK("#LOCK_", "Lock device screen"),
        CALLME("#CALLME_", "Force call to emergency number"),
        WIPE("#WIPE_", "Factory reset device (requires separate PIN)"),
        STOP_SIREN("#STOPSIREN_", "Stop emergency alarm")
    }

    /**
     * Parsed command result
     */
    data class ParsedCommand(
        val command: Command,
        val pin: String,
        val isValid: Boolean,
        val rawMessage: String
    )

    /**
     * Parse an SMS message and extract command + PIN
     * Returns null if the message is not a valid command format
     */
    fun parse(message: String): ParsedCommand? {
        val trimmedMessage = message.trim().uppercase()
        
        // Find matching command
        for (cmd in Command.values()) {
            if (trimmedMessage.startsWith(cmd.prefix)) {
                val pin = trimmedMessage.removePrefix(cmd.prefix).trim()
                
                // Validate PIN format based on command type
                val isValidPin = when (cmd) {
                    Command.WIPE -> isValidWipePin(pin)
                    else -> isValidStandardPin(pin)
                }
                
                return ParsedCommand(
                    command = cmd,
                    pin = pin,
                    isValid = isValidPin,
                    rawMessage = message
                )
            }
        }
        
        return null
    }

    /**
     * Check if message contains any command prefix (quick filter)
     */
    fun isCommandMessage(message: String): Boolean {
        val upper = message.trim().uppercase()
        return Command.values().any { upper.startsWith(it.prefix) }
    }

    /**
     * Validate standard 6-digit PIN format
     */
    private fun isValidStandardPin(pin: String): Boolean {
        return pin.length == 6 && pin.all { it.isDigit() }
    }

    /**
     * Validate 8-digit wipe PIN format
     */
    private fun isValidWipePin(pin: String): Boolean {
        return pin.length == 8 && pin.all { it.isDigit() }
    }

    /**
     * Get help text for all available commands
     */
    fun getCommandHelp(): String {
        return buildString {
            appendLine("SecureTrack Commands:")
            appendLine()
            Command.values().forEach { cmd ->
                val pinLength = if (cmd == Command.WIPE) "8" else "6"
                appendLine("${cmd.prefix}<$pinLength-digit PIN>")
                appendLine("  → ${cmd.description}")
                appendLine()
            }
        }
    }
}
