package com.securetrack.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.databinding.ActivitySetupWizardBinding
import com.securetrack.services.CoreProtectionService
import com.securetrack.utils.PermissionHelper

/**
 * Setup Wizard Activity
 * Guides user through initial app configuration
 */
class SetupWizardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupWizardBinding
    private lateinit var wizardAdapter: SetupWizardAdapter
    
    private var currentStep = 0
    private val totalSteps = 5
    
    private val stepIndicators = mutableListOf<View>()

    // Permission launchers
    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            wizardAdapter.updatePermissionStatus(currentStep)
        } else {
            Toast.makeText(this, "SMS permissions required for core functionality", Toast.LENGTH_LONG).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // Now request background location
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private val backgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        wizardAdapter.updatePermissionStatus(currentStep)
    }

    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        wizardAdapter.updatePermissionStatus(currentStep)
    }

    private val deviceAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        wizardAdapter.updatePermissionStatus(currentStep)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStepIndicators()
        setupViewPager()
        setupNavigation()
    }

    private fun setupStepIndicators() {
        stepIndicators.addAll(listOf(
            binding.step1, binding.step2, binding.step3, binding.step4, binding.step5
        ))
        updateStepIndicators()
    }

    private fun setupViewPager() {
        wizardAdapter = SetupWizardAdapter(
            onRequestSmsPermission = {
                smsPermissionLauncher.launch(PermissionHelper.getSmsPermissions())
            },
            onRequestLocationPermission = {
                locationPermissionLauncher.launch(PermissionHelper.getLocationPermissions())
            },
            onRequestPhonePermission = {
                phonePermissionLauncher.launch(PermissionHelper.getPhonePermission())
            },
            onRequestDeviceAdmin = {
                deviceAdminLauncher.launch(PermissionHelper.getDeviceAdminIntent(this))
            },
            onRequestAccessibility = {
                startActivity(PermissionHelper.getAccessibilitySettingsIntent())
            },
            onRequestNotificationPolicy = {
                startActivity(PermissionHelper.getNotificationPolicyIntent())
            },
            onPinSet = { pin ->
                SecureTrackApp.securePrefs.setPin(pin)
            },
            onMasterPasswordSet = { password ->
                SecureTrackApp.securePrefs.setMasterPassword(password)
            },
            checkPermissions = {
                PermissionHelper.checkAllPermissions(this)
            }
        )

        binding.viewPager.adapter = wizardAdapter
        binding.viewPager.isUserInputEnabled = false // Disable swipe
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentStep = position
                updateStepIndicators()
                updateNavigationButtons()
            }
        })
    }

    private fun setupNavigation() {
        binding.btnBack.setOnClickListener {
            if (currentStep > 0) {
                binding.viewPager.currentItem = currentStep - 1
            }
        }

        binding.btnNext.setOnClickListener {
            if (canProceed()) {
                if (currentStep < totalSteps - 1) {
                    binding.viewPager.currentItem = currentStep + 1
                } else {
                    completeSetup()
                }
            }
        }
    }

    private fun canProceed(): Boolean {
        return when (currentStep) {
            0 -> true // Welcome - always can proceed
            1 -> SecureTrackApp.securePrefs.isPinSet() // PIN step
            2 -> SecureTrackApp.securePrefs.isMasterPasswordSet() // Password step
            3 -> { // Permissions step
                val status = PermissionHelper.checkAllPermissions(this)
                status.smsGranted && status.locationGranted
            }
            4 -> true // Complete - always can proceed
            else -> true
        }
    }

    private fun updateStepIndicators() {
        stepIndicators.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index <= currentStep) {
                    R.drawable.step_indicator_active
                } else {
                    R.drawable.step_indicator_inactive
                }
            )
        }
    }

    private fun updateNavigationButtons() {
        binding.btnBack.visibility = if (currentStep > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNext.text = if (currentStep == totalSteps - 1) {
            getString(R.string.btn_finish)
        } else {
            getString(R.string.btn_next)
        }
    }

    private fun completeSetup() {
        // Mark setup as complete
        SecureTrackApp.securePrefs.isSetupComplete = true
        SecureTrackApp.securePrefs.isProtectionEnabled = true

        // Start protection service
        val serviceIntent = Intent(this, CoreProtectionService::class.java).apply {
            action = CoreProtectionService.ACTION_START
        }
        startForegroundService(serviceIntent)

        // Go to main activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Update permission status when returning from settings
        wizardAdapter.updatePermissionStatus(currentStep)
    }
}
