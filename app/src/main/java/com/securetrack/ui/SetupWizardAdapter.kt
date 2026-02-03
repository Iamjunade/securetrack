package com.securetrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.securetrack.R
import com.securetrack.utils.PermissionHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Setup Wizard ViewPager Adapter
 * Manages wizard step pages
 */
class SetupWizardAdapter(
    private val onRequestSmsPermission: () -> Unit,
    private val onRequestLocationPermission: () -> Unit,
    private val onRequestPhonePermission: () -> Unit,
    private val onRequestDeviceAdmin: () -> Unit,
    private val onRequestAccessibility: () -> Unit,
    private val onRequestNotificationPolicy: () -> Unit,
    private val onPinSet: (String) -> Unit,
    private val onMasterPasswordSet: (String) -> Unit,
    private val checkPermissions: () -> PermissionHelper.PermissionStatus
) : RecyclerView.Adapter<SetupWizardAdapter.StepViewHolder>() {

    companion object {
        private const val STEP_WELCOME = 0
        private const val STEP_PIN = 1
        private const val STEP_PASSWORD = 2
        private const val STEP_PERMISSIONS = 3
        private const val STEP_COMPLETE = 4
    }

    private val viewHolders = mutableMapOf<Int, StepViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val layoutRes = when (viewType) {
            STEP_WELCOME -> R.layout.step_welcome
            STEP_PIN -> R.layout.step_pin
            STEP_PASSWORD -> R.layout.step_password
            STEP_PERMISSIONS -> R.layout.step_permissions
            STEP_COMPLETE -> R.layout.step_complete
            else -> R.layout.step_welcome
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return StepViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        viewHolders[position] = holder
        holder.bind(position)
    }

    override fun getItemCount(): Int = 5

    override fun getItemViewType(position: Int): Int = position

    fun updatePermissionStatus(currentStep: Int) {
        viewHolders[STEP_PERMISSIONS]?.updatePermissions()
    }

    inner class StepViewHolder(itemView: View, private val stepType: Int) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            when (stepType) {
                STEP_PIN -> setupPinStep()
                STEP_PASSWORD -> setupPasswordStep()
                STEP_PERMISSIONS -> setupPermissionsStep()
            }
        }

        private fun setupPinStep() {
            val pinInput = itemView.findViewById<TextInputEditText>(R.id.editPin)
            val confirmInput = itemView.findViewById<TextInputEditText>(R.id.editPinConfirm)
            val btnSetPin = itemView.findViewById<Button>(R.id.btnSetPin)
            val errorText = itemView.findViewById<TextView>(R.id.txtPinError)

            btnSetPin?.setOnClickListener {
                val pin = pinInput?.text?.toString() ?: ""
                val confirm = confirmInput?.text?.toString() ?: ""

                when {
                    pin.length != 6 || !pin.all { it.isDigit() } -> {
                        errorText?.text = "PIN must be exactly 6 digits"
                        errorText?.visibility = View.VISIBLE
                    }
                    pin != confirm -> {
                        errorText?.text = "PINs do not match"
                        errorText?.visibility = View.VISIBLE
                    }
                    else -> {
                        errorText?.visibility = View.GONE
                        onPinSet(pin)
                        btnSetPin.text = "✓ PIN Set"
                        btnSetPin.isEnabled = false
                    }
                }
            }
        }

        private fun setupPasswordStep() {
            val passwordInput = itemView.findViewById<TextInputEditText>(R.id.editPassword)
            val confirmInput = itemView.findViewById<TextInputEditText>(R.id.editPasswordConfirm)
            val btnSetPassword = itemView.findViewById<Button>(R.id.btnSetPassword)
            val errorText = itemView.findViewById<TextView>(R.id.txtPasswordError)

            btnSetPassword?.setOnClickListener {
                val password = passwordInput?.text?.toString() ?: ""
                val confirm = confirmInput?.text?.toString() ?: ""

                when {
                    password.length < 8 -> {
                        errorText?.text = "Password must be at least 8 characters"
                        errorText?.visibility = View.VISIBLE
                    }
                    password != confirm -> {
                        errorText?.text = "Passwords do not match"
                        errorText?.visibility = View.VISIBLE
                    }
                    else -> {
                        errorText?.visibility = View.GONE
                        onMasterPasswordSet(password)
                        btnSetPassword.text = "✓ Password Set"
                        btnSetPassword.isEnabled = false
                    }
                }
            }
        }

        private fun setupPermissionsStep() {
            itemView.findViewById<View>(R.id.rowSms)?.setOnClickListener { onRequestSmsPermission() }
            itemView.findViewById<View>(R.id.rowLocation)?.setOnClickListener { onRequestLocationPermission() }
            itemView.findViewById<View>(R.id.rowPhone)?.setOnClickListener { onRequestPhonePermission() }
            itemView.findViewById<View>(R.id.rowDeviceAdmin)?.setOnClickListener { onRequestDeviceAdmin() }
            itemView.findViewById<View>(R.id.rowAccessibility)?.setOnClickListener { onRequestAccessibility() }
            itemView.findViewById<View>(R.id.rowDnd)?.setOnClickListener { onRequestNotificationPolicy() }
            
            updatePermissions()
        }

        fun updatePermissions() {
            if (stepType != STEP_PERMISSIONS) return
            
            val status = checkPermissions()

            updatePermissionRow(R.id.imgSmsStatus, status.smsGranted)
            updatePermissionRow(R.id.imgLocationStatus, status.locationGranted && status.backgroundLocationGranted)
            updatePermissionRow(R.id.imgPhoneStatus, status.phoneGranted)
            updatePermissionRow(R.id.imgAdminStatus, status.deviceAdminActive)
            updatePermissionRow(R.id.imgAccessibilityStatus, status.accessibilityEnabled)
            updatePermissionRow(R.id.imgDndStatus, status.notificationPolicyAccess)
        }

        private fun updatePermissionRow(imageViewId: Int, granted: Boolean) {
            val imageView = itemView.findViewById<ImageView>(imageViewId) ?: return
            if (granted) {
                imageView.setImageResource(R.drawable.ic_check)
                imageView.setColorFilter(itemView.context.getColor(R.color.permission_granted))
            } else {
                imageView.setImageResource(R.drawable.ic_arrow_right)
                imageView.setColorFilter(itemView.context.getColor(R.color.text_secondary_light))
            }
        }
    }
}
