package com.securetrack.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Secure Preferences Manager
 * Uses Android Keystore-backed encryption for sensitive data
 */
class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "securetrack_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_MASTER_PASSWORD_HASH = "master_password_hash"
        private const val KEY_ORIGINAL_SIM_ICCID = "original_sim_iccid"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_WIPE_ENABLED = "wipe_enabled"
        private const val KEY_WIPE_PIN_HASH = "wipe_pin_hash"
        private const val KEY_AIRPLANE_MODE_DETECTED = "airplane_mode_detected"

        // Sequential patterns to block
        private val BLOCKED_PATTERNS = listOf(
            "000000", "111111", "222222", "333333", "444444",
            "555555", "666666", "777777", "888888", "999999",
            "123456", "654321", "012345", "543210",
            "123123", "456456", "789789", "121212", "343434"
        )
    }

    // ============ PIN Management ============

    /**
     * Validates PIN format: 6 digits, non-sequential
     */
    fun isValidPin(pin: String): Boolean {
        if (pin.length != 6) return false
        if (!pin.all { it.isDigit() }) return false
        if (BLOCKED_PATTERNS.contains(pin)) return false
        
        // Check for simple ascending/descending sequences
        val isAscending = pin.zipWithNext().all { (a, b) -> b.code - a.code == 1 }
        val isDescending = pin.zipWithNext().all { (a, b) -> a.code - b.code == 1 }
        
        return !isAscending && !isDescending
    }

    /**
     * Stores PIN as SHA-256 hash
     */
    fun setPin(pin: String): Boolean {
        if (!isValidPin(pin)) return false
        prefs.edit().putString(KEY_PIN_HASH, hashString(pin)).apply()
        return true
    }

    /**
     * Verifies PIN against stored hash
     */
    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashString(pin) == storedHash
    }

    fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HASH)

    // ============ Master Password (for Device Admin) ============

    fun setMasterPassword(password: String): Boolean {
        if (password.length < 8) return false
        prefs.edit().putString(KEY_MASTER_PASSWORD_HASH, hashString(password)).apply()
        return true
    }

    fun verifyMasterPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_MASTER_PASSWORD_HASH, null) ?: return false
        return hashString(password) == storedHash
    }

    fun isMasterPasswordSet(): Boolean = prefs.contains(KEY_MASTER_PASSWORD_HASH)

    // ============ Wipe Feature (Disabled by Default) ============

    var isWipeEnabled: Boolean
        get() = prefs.getBoolean(KEY_WIPE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_WIPE_ENABLED, value).apply()

    /**
     * Wipe has a separate, longer PIN for extra security
     */
    fun setWipePin(pin: String): Boolean {
        // Wipe PIN must be 8 digits
        if (pin.length != 8 || !pin.all { it.isDigit() }) return false
        prefs.edit().putString(KEY_WIPE_PIN_HASH, hashString(pin)).apply()
        return true
    }

    fun verifyWipePin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_WIPE_PIN_HASH, null) ?: return false
        return hashString(pin) == storedHash
    }

    // ============ SIM Tracking ============

    var originalSimIccid: String?
        get() = prefs.getString(KEY_ORIGINAL_SIM_ICCID, null)
        set(value) = prefs.edit().putString(KEY_ORIGINAL_SIM_ICCID, value).apply()

    // ============ App State ============

    var isSetupComplete: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETE, value).apply()

    var isProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, value).apply()

    // ============ Airplane Mode Tracking ============

    fun setAirplaneModeDetected(detected: Boolean) {
        prefs.edit().putBoolean(KEY_AIRPLANE_MODE_DETECTED, detected).apply()
    }

    fun wasAirplaneModeDetected(): Boolean {
        return prefs.getBoolean(KEY_AIRPLANE_MODE_DETECTED, false)
    }

    // ============ Lock Screen Tracking ============
    var failedUnlockAttempts: Int
        get() = prefs.getInt("failed_unlock_attempts", 0)
        set(value) = prefs.edit().putInt("failed_unlock_attempts", value).apply()

    // ============ Utility ============

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}

