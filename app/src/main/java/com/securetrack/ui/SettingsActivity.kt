package com.securetrack.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.securetrack.R
import com.securetrack.SecureTrackApp

/**
 * Settings Activity - Holographic Theme
 * App configuration and security settings
 */
class SettingsActivity : AppCompatActivity() {

    private val securePrefs = SecureTrackApp.securePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_settings)

        setupBackButton()
        setupSettings()

        // Handle Window Insets (Notch/Status Bar)
        val settingsContent = findViewById<android.view.View>(R.id.settingsContent)
        val originalPaddingTop = settingsContent.paddingTop
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(settingsContent) { v, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, originalPaddingTop + bars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSettings() {
        // Protection toggle
        val switchProtection = findViewById<SwitchMaterial>(R.id.switchProtection)
        switchProtection.isChecked = securePrefs.isProtectionEnabled
        switchProtection.setOnCheckedChangeListener { _, isChecked ->
            securePrefs.isProtectionEnabled = isChecked
        }

        // Wipe feature toggle (disabled by default)
        val switchWipe = findViewById<SwitchMaterial>(R.id.switchWipe)
        switchWipe.isChecked = securePrefs.isWipeEnabled
        switchWipe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showEnableWipeDialog(switchWipe)
            } else {
                securePrefs.isWipeEnabled = false
            }
        }

        // Test Fake Shutdown
        findViewById<android.view.View>(R.id.btnTestFakeShutdown).setOnClickListener {
            Toast.makeText(this, "Starting Fake Shutdown...", Toast.LENGTH_SHORT).show()
            try {
                val intent = android.content.Intent(this, FakeShutdownActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // Change PIN
        findViewById<android.view.View>(R.id.rowChangePin).setOnClickListener {
            showChangePinDialog()
        }

        // Change Master Password
        findViewById<android.view.View>(R.id.rowChangePassword).setOnClickListener {
            showChangePasswordDialog()
        }

        // Check for Updates
        findViewById<android.view.View>(R.id.btnCheckUpdates).setOnClickListener {
             com.securetrack.utils.UpdateChecker.checkForUpdates(this, true)
        }

        // Share App (APK)
        findViewById<android.view.View>(R.id.btnShareApp).setOnClickListener {
            shareAppApk()
        }
    }

    private fun shareAppApk() {
        try {
            val appInfo = applicationContext.applicationInfo
            val originalApk = java.io.File(appInfo.sourceDir)

            // Create a temp copy to share (safer and better naming)
            val tempFile = java.io.File(externalCacheDir, "SecureTrack_Install.apk")
            
            try {
                // Copy file
                val inStream = java.io.FileInputStream(originalApk)
                val outStream = java.io.FileOutputStream(tempFile)
                val buffer = ByteArray(1024)
                var read: Int
                while (inStream.read(buffer).also { read = it } != -1) {
                    outStream.write(buffer, 0, read)
                }
                inStream.close()
                outStream.flush()
                outStream.close()

                // Share Intent
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    tempFile
                )

                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(android.content.Intent.createChooser(intent, "Share SecureTrack via"))

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error preparing APK: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEnableWipeDialog(switchWipe: SwitchMaterial) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_wipe_pin, null)
        val pinInput = dialogView.findViewById<TextInputEditText>(R.id.editWipePin)
        val confirmInput = dialogView.findViewById<TextInputEditText>(R.id.editWipePinConfirm)

        AlertDialog.Builder(this)
            .setTitle("Enable Remote Wipe")
            .setMessage("⚠️ WARNING: This allows factory reset via SMS.\n\nSet an 8-digit wipe PIN (different from your regular PIN):")
            .setView(dialogView)
            .setPositiveButton("Enable") { _, _ ->
                val pin = pinInput.text?.toString() ?: ""
                val confirm = confirmInput.text?.toString() ?: ""

                when {
                    pin.length != 8 || !pin.all { it.isDigit() } -> {
                        Toast.makeText(this, "Wipe PIN must be 8 digits", Toast.LENGTH_LONG).show()
                        switchWipe.isChecked = false
                    }
                    pin != confirm -> {
                        Toast.makeText(this, "PINs do not match", Toast.LENGTH_LONG).show()
                        switchWipe.isChecked = false
                    }
                    else -> {
                        securePrefs.setWipePin(pin)
                        securePrefs.isWipeEnabled = true
                        Toast.makeText(this, "Remote wipe enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                switchWipe.isChecked = false
            }
            .setCancelable(false)
            .show()
    }

    private fun showChangePinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_pin, null)
        val currentPin = dialogView.findViewById<TextInputEditText>(R.id.editCurrentPin)
        val newPin = dialogView.findViewById<TextInputEditText>(R.id.editNewPin)
        val confirmPin = dialogView.findViewById<TextInputEditText>(R.id.editConfirmPin)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_change_pin)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val current = currentPin.text?.toString() ?: ""
                val new = newPin.text?.toString() ?: ""
                val confirm = confirmPin.text?.toString() ?: ""

                when {
                    !securePrefs.verifyPin(current) -> {
                        Toast.makeText(this, "Current PIN is incorrect", Toast.LENGTH_LONG).show()
                    }
                    !securePrefs.isValidPin(new) -> {
                        Toast.makeText(this, "New PIN must be 6 non-sequential digits", Toast.LENGTH_LONG).show()
                    }
                    new != confirm -> {
                        Toast.makeText(this, "PINs do not match", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        securePrefs.setPin(new)
                        Toast.makeText(this, "PIN changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPassword = dialogView.findViewById<TextInputEditText>(R.id.editCurrentPassword)
        val newPassword = dialogView.findViewById<TextInputEditText>(R.id.editNewPassword)
        val confirmPassword = dialogView.findViewById<TextInputEditText>(R.id.editConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_change_password)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val current = currentPassword.text?.toString() ?: ""
                val new = newPassword.text?.toString() ?: ""
                val confirm = confirmPassword.text?.toString() ?: ""

                when {
                    !securePrefs.verifyMasterPassword(current) -> {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_LONG).show()
                    }
                    new.length < 8 -> {
                        Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_LONG).show()
                    }
                    new != confirm -> {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        securePrefs.setMasterPassword(new)
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}
