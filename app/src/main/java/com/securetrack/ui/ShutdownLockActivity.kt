package com.securetrack.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.services.StatusBarBlockerService

/**
 * Shutdown Lock Activity
 * Intercepts power menu and requires PIN to proceed
 */
class ShutdownLockActivity : AppCompatActivity() {

    private val securePrefs = SecureTrackApp.securePrefs
    private var attempts = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen and show over lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_shutdown_lock)

        setupUI()
    }

    private fun setupUI() {
        val editPin = findViewById<TextInputEditText>(R.id.editPin)
        val btnUnlock = findViewById<MaterialButton>(R.id.btnUnlock)
        val btnEmergency = findViewById<TextView>(R.id.btnEmergency)

        btnUnlock.setOnClickListener {
            val pin = editPin.text?.toString() ?: ""
            validatePin(pin)
        }

        btnEmergency.setOnClickListener {
            // Trigger emergency process or just dialer
            val intent = Intent(Intent.ACTION_DIAL)
            startActivity(intent)
        }
    }

    private fun validatePin(pin: String) {
        if (securePrefs.verifyPin(pin)) {
            // Correct PIN
            Toast.makeText(this, "Shutdown Unlocked", Toast.LENGTH_SHORT).show()
            
            // Notify Service to allow shutdown temporarily
            StatusBarBlockerService.shouldAllowShutdown = true
            
            // Auto-reset flag after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                StatusBarBlockerService.shouldAllowShutdown = false
            }, 30000)

            // Finish and let user access power menu again
            finish()
        } else {
            attempts++
            if (attempts >= maxAttempts) {
                // Too many wrong attempts -> FAKE SHUTDOWN (Deception)
                Toast.makeText(this, "Too many attempts", Toast.LENGTH_LONG).show()
                triggerFakeShutdown()
            } else {
                Toast.makeText(this, "Incorrect PIN ($attempts/$maxAttempts)", Toast.LENGTH_SHORT).show()
                findViewById<TextInputEditText>(R.id.editPin).text?.clear()
            }
        }
    }

    private fun triggerFakeShutdown() {
        val intent = Intent(this, FakeShutdownActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Block Back Button to prevent circumvention
    override fun onBackPressed() {
        // Just go home or lock screen, do not dismiss without PIN unless we want to allow "Cancel"
        // If we allow Cancel, they can't shutdown. Verified.
        super.onBackPressed()
    }
}
