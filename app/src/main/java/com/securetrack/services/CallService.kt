package com.securetrack.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * Call Service
 * Initiates phone call to specified number (for #CALLME command)
 */
class CallService : Service() {

    companion object {
        private const val TAG = "CallService"
        
        const val ACTION_CALL = "com.securetrack.action.CALL"
        const val EXTRA_PHONE_NUMBER = "phone_number"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CALL -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                if (phoneNumber != null) {
                    initiateCall(phoneNumber)
                }
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    private fun initiateCall(phoneNumber: String) {
        Log.d(TAG, "Initiating call to $phoneNumber")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "CALL_PHONE permission not granted")
            return
        }

        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(callIntent)

            Log.d(TAG, "Call initiated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate call", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
