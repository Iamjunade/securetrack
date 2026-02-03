package com.securetrack.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Global Crash Handler
 * Captures uncaught exceptions and saves them to local storage
 * Critical for "1M User" scalability to debug issues in the wild
 */
class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashReport(thread, throwable)
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to save crash report", e)
        }

        // Delegate to default handler (crashes app standard way)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashReport(thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val fileName = "crash_$timestamp.txt"
        
        val report = StringBuilder()
        report.append("SecureTrack Crash Report\n")
        report.append("Time: $timestamp\n")
        report.append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        report.append("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
        report.append("App Version: 4.2.0\n") // Matching our holographic UI version
        report.append("\n--- Stack Trace ---\n")
        report.append(stackTrace)

        // Save to internal storage (private to app)
        try {
            val dir = File(context.filesDir, "crash_logs")
            if (!dir.exists()) dir.mkdirs()
            
            val file = File(dir, fileName)
            FileOutputStream(file).use {
                it.write(report.toString().toByteArray())
            }
            
            Log.e("CrashHandler", "Crash saved to: ${file.absolutePath}")
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun init(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        }
    }
}
