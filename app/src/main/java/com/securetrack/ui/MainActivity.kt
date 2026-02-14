package com.securetrack.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.databinding.ActivityMainBinding
import com.securetrack.services.CoreProtectionService
import com.securetrack.utils.PermissionHelper

/**
 * Main Activity - Holographic Dashboard
 * Shows protection status, commands console, and recent logs
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for immersive experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if setup is complete
        if (!SecureTrackApp.securePrefs.isSetupComplete) {
            startActivity(Intent(this, SetupWizardActivity::class.java))
            finish()
            return
        }

        setupStatusCard()
        setupCommandCards()
        setupNavigation()
        updateStatus()

        // Check for updates
        com.securetrack.utils.UpdateChecker.checkForUpdates(this)
        
        // Quick Update Button
        findViewById<android.view.View>(R.id.btnQuickUpdate).setOnClickListener {
             com.securetrack.utils.UpdateChecker.checkForUpdates(this, true)
        }

        // Handle Window Insets (Notch/Status Bar)
        val mainContent = findViewById<android.view.View>(R.id.mainContent)
        val originalPaddingTop = mainContent.paddingTop
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainContent) { v, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, originalPaddingTop + bars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun setupCommandCards() {
        // LOCATE
        binding.cmdLocate.apply {
            txtCommandCode.text = "#LOCATE_<PIN>"
            txtDescription.text = "Triangulate device coordinates"
            imgIcon.setImageResource(R.drawable.ic_location)
            txtCommandCode.setTextColor(getColor(R.color.cmd_locate))
            imgIcon.setColorFilter(getColor(R.color.cmd_locate))
        }

        // SIREN
        binding.cmdSiren.apply {
            txtCommandCode.text = "#SIREN_<PIN>"
            txtDescription.text = "Trigger max volume alarm sequence"
            imgIcon.setImageResource(R.drawable.ic_siren)
            txtCommandCode.setTextColor(getColor(R.color.cmd_siren))
            imgIcon.setColorFilter(getColor(R.color.cmd_siren))
        }

        // LOCK
        binding.cmdLock.apply {
            txtCommandCode.text = "#LOCK_<PIN>"
            txtDescription.text = "Engage remote lockout protocol"
            imgIcon.setImageResource(R.drawable.ic_lock)
            txtCommandCode.setTextColor(getColor(R.color.cmd_lock))
            imgIcon.setColorFilter(getColor(R.color.cmd_lock))
        }

        // CALLME
        binding.cmdCallMe.apply {
            txtCommandCode.text = "#CALLME_<PIN>"
            txtDescription.text = "Initiate callback request"
            imgIcon.setImageResource(R.drawable.ic_call)
            txtCommandCode.setTextColor(getColor(R.color.cmd_callme))
            imgIcon.setColorFilter(getColor(R.color.cmd_callme))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        
        // Ensure Camera permission is granted if protection is enabled
        if (SecureTrackApp.securePrefs.isProtectionEnabled && !PermissionHelper.checkCameraPermission(this)) {
            requestPermissions(arrayOf(PermissionHelper.getCameraPermission()), 102)
        }
    }

    private fun setupStatusCard() {
        binding.switchProtection.setOnCheckedChangeListener { _, isChecked ->
            SecureTrackApp.securePrefs.isProtectionEnabled = isChecked
            updateProtectionService(isChecked)
            
            // Check Camera permission if enabling protection
            if (isChecked && !PermissionHelper.checkCameraPermission(this)) {
                requestPermissions(arrayOf(PermissionHelper.getCameraPermission()), 101)
            }
            
            updateStatus()
        }
        
        // Allow clicking the status card to fix permissions
        binding.cardStatus.setOnClickListener {
             val permissions = PermissionHelper.checkAllPermissions(this)
             if (!permissions.cameraGranted) {
                 requestPermissions(arrayOf(PermissionHelper.getCameraPermission()), 101)
             }
             // Add other permission requests here if needed
        }
    }

    private fun setupNavigation() {
        // Bottom nav - Home
        binding.navHome.setOnClickListener {
            // Already on home
            android.widget.Toast.makeText(this, "Home selected", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Bottom nav - Contacts
        binding.navContacts.setOnClickListener {
            try {
                android.widget.Toast.makeText(this, "Opening Contacts...", android.widget.Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ContactsActivity::class.java))
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // Bottom nav - Logs
        binding.navLogs.setOnClickListener {
            try {
                startActivity(Intent(this, LogsActivity::class.java))
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "Error opening logs", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom nav - Settings
        binding.navSettings.setOnClickListener {
            try {
                startActivity(Intent(this, SettingsActivity::class.java))
            } catch (e: Exception) {
                 android.widget.Toast.makeText(this, "Error opening settings", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Center FAB
        binding.fabAdd.setOnClickListener {
            // TODO: Quick action menu
            android.widget.Toast.makeText(this, "Quick Action", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatus() {
        val permissions = PermissionHelper.checkAllPermissions(this)
        val isEnabled = SecureTrackApp.securePrefs.isProtectionEnabled

        binding.switchProtection.isChecked = isEnabled

        if (isEnabled && permissions.allGranted) {
            // Active: Cyan/Green Pulse
            binding.lottieShield.apply {
                // Ensure animation is playing
                if (!isAnimating) playAnimation()
                // Set color to Primary (Cyan) or Status Active (Green)
                // Lottie properties are complex, so we'll use a simple ColorFilter overlay for the view
                setColorFilter(getColor(R.color.primary))
            }
            binding.txtStatusTitle.text = getString(R.string.protection_active)
            binding.txtStatusDetail.text = getString(R.string.status_all_granted)
        } else if (isEnabled) {
            // Warning: Orange/Yellow
            binding.lottieShield.setColorFilter(getColor(R.color.status_warning))
            binding.txtStatusTitle.text = getString(R.string.protection_active)
            binding.txtStatusDetail.text = getString(R.string.status_missing_permissions)
        } else {
            // Inactive: Red/Dimmed
            binding.lottieShield.apply {
                cancelAnimation()
                progress = 0f
                setColorFilter(getColor(R.color.status_inactive))
            }
            binding.txtStatusTitle.text = getString(R.string.protection_inactive)
            binding.txtStatusDetail.text = "Tap switch to enable"
        }
    }

    private fun updateProtectionService(enable: Boolean) {
        val intent = Intent(this, CoreProtectionService::class.java).apply {
            action = if (enable) {
                CoreProtectionService.ACTION_START
            } else {
                CoreProtectionService.ACTION_STOP
            }
        }

        if (enable) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }
    }
}
