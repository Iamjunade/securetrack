package com.securetrack.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.securetrack.databinding.ActivityFakeShutdownBinding
import com.securetrack.services.LocationService

/**
 * Fake Shutdown Activity
 * Mimics system shutdown and enters stealth tracking mode
 */
class FakeShutdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFakeShutdownBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make it full screen and blocking
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        
        // Hide system bars
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding = ActivityFakeShutdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFakeShutdownSequence()
    }

    private fun startFakeShutdownSequence() {
        // 1. Play animation for 3 seconds
        handler.postDelayed({
            enterStealthMode()
        }, 3000)

        // Start tracking immediately
        startLocationTracking()
    }

    private fun enterStealthMode() {
        // Hide the spinner and text to make screen look "off"
        binding.shutdownContainer.visibility = View.GONE
        
        // Dim the screen to minimum brightness to simulate "off"
        val params = window.attributes
        params.screenBrightness = 0.01f // Almost off but kept on to keep CPU checking
        window.attributes = params
        
        // In a real device, we might not be able to turn screen fully off without sleeping
        // So we just keep it black and non-interactive
    }

    private fun startLocationTracking() {
        // Start foreground service to fetch location silently
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_GET_LOCATION
        // We don't have a sender number here, but the service handles "emergency mode" if needed
        // Or we can just trigger it to log location
        // For now, let's just make sure the service is running
    }

    // Block Back Button
    override fun onBackPressed() {
        // Do nothing - user cannot exit
    }

    // Block other keys if possible (Volume, Home logic handled by system/launcher)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return true // Consume all keys
    }
}
