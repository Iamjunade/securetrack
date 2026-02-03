@file:Suppress("DEPRECATION")

package com.securetrack.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * OTA Update Checker
 * Checks GitHub raw JSON for updates and handles self-update
 */
object UpdateChecker {

    // RAW JSON from user's repo (main branch)
    private const val UPDATE_URL = "https://raw.githubusercontent.com/lamjunade/securetrack/main/version.json"
    
    // Executor for network operations
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun checkForUpdates(activity: Activity, isManualCheck: Boolean = false) {
        executor.execute {
            try {
                // Get current version
                val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                val currentVersionCode = pInfo.versionCode

                val url = URL(UPDATE_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = JSONObject(response.toString())
                    val remoteVersionCode = json.getInt("versionCode")
                    val downloadUrl = json.getString("url")
                    val changes = json.optString("changes", "New features and bug fixes.")

                    handler.post {
                        if (remoteVersionCode > currentVersionCode) {
                            showUpdateDialog(activity, downloadUrl, changes)
                        } else if (isManualCheck) {
                            Toast.makeText(activity, "You are up to date!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if (isManualCheck) handler.post { 
                        Toast.makeText(activity, "Check failed: ${conn.responseCode}", Toast.LENGTH_SHORT).show() 
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isManualCheck) handler.post { 
                    Toast.makeText(activity, "Update check failed: ${e.message}", Toast.LENGTH_SHORT).show() 
                }
            }
        }
    }

    private fun showUpdateDialog(activity: Activity, downloadUrl: String, changes: String) {
        AlertDialog.Builder(activity)
            .setTitle("New Update Available")
            .setMessage("A new version of SecureTrack is available.\n\nChanges:\n$changes")
            .setPositiveButton("Update Now") { _, _ ->
                downloadAndInstall(activity, downloadUrl)
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

    private fun downloadAndInstall(activity: Activity, downloadUrl: String) {
        val progressDialog = android.app.ProgressDialog(activity).apply {
            setMessage("Downloading update...")
            isIndeterminate = false
            max = 100
            setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            show()
        }

        executor.execute {
            try {
                val url = URL(downloadUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connect()

                val fileLength = conn.contentLength
                val input = conn.inputStream
                
                val file = File(activity.externalCacheDir, "update.apk")
                val output = FileOutputStream(file)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        handler.post { progressDialog.progress = progress }
                    }
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                handler.post {
                    progressDialog.dismiss()
                    installApk(activity, file)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun installApk(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Install error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
