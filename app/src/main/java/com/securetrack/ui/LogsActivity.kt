package com.securetrack.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.securetrack.R
import com.securetrack.SecureTrackApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Logs Activity - Holographic Theme
 * Shows command execution history
 */
class LogsActivity : AppCompatActivity() {

    private lateinit var logAdapter: CommandLogAdapter
    private lateinit var intruderAdapter: IntruderLogAdapter
    private var isShowingCommands = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_logs)

        setupBackButton()
        setupRecyclerView()
        setupTabs()
        observeLogs()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        logAdapter = CommandLogAdapter()
        intruderAdapter = IntruderLogAdapter { path ->
            // Show full screen image dialog
            val dialog = android.app.Dialog(this@LogsActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.dialog_fullscreen_image)
            
            val imgFull = dialog.findViewById<ImageView>(R.id.imgFull)
            val btnClose = dialog.findViewById<View>(R.id.btnClose)
            
            val bitmap = android.graphics.BitmapFactory.decodeFile(path)
            imgFull.setImageBitmap(bitmap)
            
            btnClose.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
        
        findViewById<RecyclerView>(R.id.recyclerLogs).apply {
            layoutManager = LinearLayoutManager(this@LogsActivity)
            adapter = logAdapter
        }
    }

    private fun setupTabs() {
        val tabCommands = findViewById<TextView>(R.id.tabCommands)
        val tabIntruders = findViewById<TextView>(R.id.tabIntruders)

        tabCommands.setOnClickListener { switchTab(true) }
        tabIntruders.setOnClickListener { switchTab(false) }
    }

    private fun switchTab(showCommands: Boolean) {
        if (isShowingCommands == showCommands) return
        isShowingCommands = showCommands
        
        val tabCommands = findViewById<TextView>(R.id.tabCommands)
        val tabIntruders = findViewById<TextView>(R.id.tabIntruders)
        val recycler = findViewById<RecyclerView>(R.id.recyclerLogs)

        if (showCommands) {
            tabCommands.setBackgroundResource(R.drawable.bg_fab_gradient)
            tabCommands.setTextColor(getColor(android.R.color.white))
            tabIntruders.setBackgroundResource(android.R.color.transparent)
            tabIntruders.setTextColor(getColor(R.color.text_secondary_dark))
            recycler.adapter = logAdapter
        } else {
            tabIntruders.setBackgroundResource(R.drawable.bg_fab_gradient)
            tabIntruders.setTextColor(getColor(android.R.color.white))
            tabCommands.setBackgroundResource(android.R.color.transparent)
            tabCommands.setTextColor(getColor(R.color.text_secondary_dark))
            recycler.adapter = intruderAdapter
        }
    }

    private fun observeLogs() {
        // Observe Commands
        lifecycleScope.launch {
            SecureTrackApp.database.commandLogDao().getRecentLogs(50).collectLatest { logs ->
                logAdapter.submitList(logs)
                updateEmptyState()
            }
        }
        
        // Observe Intruders
        lifecycleScope.launch {
            SecureTrackApp.database.intruderLogDao().getAllLogs().collectLatest { logs ->
                intruderAdapter.submitList(logs)
                updateEmptyState()
            }
        }
    }
    
    private fun updateEmptyState() {
        val isEmpty = if (isShowingCommands) logAdapter.currentList.isEmpty() else intruderAdapter.currentList.isEmpty()
        findViewById<View>(R.id.txtEmptyState).visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
