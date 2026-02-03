package com.securetrack.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_logs)

        setupBackButton()
        setupRecyclerView()
        observeLogs()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        logAdapter = CommandLogAdapter()
        findViewById<RecyclerView>(R.id.recyclerLogs).apply {
            layoutManager = LinearLayoutManager(this@LogsActivity)
            adapter = logAdapter
        }
    }

    private fun observeLogs() {
        lifecycleScope.launch {
            SecureTrackApp.database.commandLogDao().getRecentLogs(50).collectLatest { logs ->
                logAdapter.submitList(logs)
                findViewById<View>(R.id.txtEmptyState).visibility = 
                    if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
