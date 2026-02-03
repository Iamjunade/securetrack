package com.securetrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.securetrack.R
import com.securetrack.SecureTrackApp
import com.securetrack.data.entities.EmergencyContact
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Contacts Activity
 * CRUD interface for managing emergency contacts
 */
class ContactsActivity : AppCompatActivity() {

    private lateinit var contactsAdapter: ContactsAdapter
    private val contactDao = SecureTrackApp.database.emergencyContactDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_contacts)

        setupBackButton()
        setupRecyclerView()
        setupFab()
        observeContacts()

        // Handle Window Insets (Notch/Status Bar)
        val contactsContent = findViewById<android.view.View>(R.id.contactsContent)
        val originalPaddingTop = contactsContent.paddingTop
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(contactsContent) { v, insets ->
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

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(
            onEdit = { contact -> showEditDialog(contact) },
            onDelete = { contact -> confirmDelete(contact) },
            onSetPrimary = { contact -> setPrimaryContact(contact) }
        )
        
        findViewById<RecyclerView>(R.id.recyclerContacts).apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = contactsAdapter
        }
    }

    private fun setupFab() {
        findViewById<View>(R.id.fabAddContact).setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeContacts() {
        lifecycleScope.launch {
            contactDao.getAllContacts().collectLatest { contacts ->
                contactsAdapter.submitList(contacts)
                findViewById<TextView>(R.id.txtEmptyState).visibility = 
                    if (contacts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editName)
        val phoneInput = dialogView.findViewById<TextInputEditText>(R.id.editPhone)

        AlertDialog.Builder(this)
            .setTitle(R.string.add_contact)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val name = nameInput.text?.toString()?.trim() ?: ""
                val phone = phoneInput.text?.toString()?.trim() ?: ""
                
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    lifecycleScope.launch {
                        contactDao.insertContact(EmergencyContact(name = name, phoneNumber = phone))
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showEditDialog(contact: EmergencyContact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editName)
        val phoneInput = dialogView.findViewById<TextInputEditText>(R.id.editPhone)
        
        nameInput.setText(contact.name)
        phoneInput.setText(contact.phoneNumber)

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_contact)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val name = nameInput.text?.toString()?.trim() ?: ""
                val phone = phoneInput.text?.toString()?.trim() ?: ""
                
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    lifecycleScope.launch {
                        contactDao.updateContact(contact.copy(
                            name = name,
                            phoneNumber = phone,
                            updatedAt = System.currentTimeMillis()
                        ))
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun confirmDelete(contact: EmergencyContact) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_contact)
            .setMessage("Delete ${contact.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    contactDao.deleteContact(contact)
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun setPrimaryContact(contact: EmergencyContact) {
        lifecycleScope.launch {
            contactDao.setPrimaryContact(contact.id)
            Toast.makeText(this@ContactsActivity, "${contact.name} set as primary", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Contacts RecyclerView Adapter
 */
class ContactsAdapter(
    private val onEdit: (EmergencyContact) -> Unit,
    private val onDelete: (EmergencyContact) -> Unit,
    private val onSetPrimary: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtContactName)
        private val txtPhone: TextView = itemView.findViewById(R.id.txtContactPhone)
        private val imgPrimary: ImageView = itemView.findViewById(R.id.imgPrimary)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(contact: EmergencyContact) {
            txtName.text = contact.name
            txtPhone.text = contact.phoneNumber
            imgPrimary.visibility = if (contact.isPrimary) View.VISIBLE else View.GONE

            btnEdit.setOnClickListener { onEdit(contact) }
            btnDelete.setOnClickListener { onDelete(contact) }
            itemView.setOnLongClickListener { 
                onSetPrimary(contact)
                true 
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact) = 
            oldItem == newItem
    }
}
