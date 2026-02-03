package com.securetrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securetrack.R
import com.securetrack.data.entities.CommandLog
import com.securetrack.data.entities.CommandStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for Command Logs
 */
class CommandLogAdapter : ListAdapter<CommandLog, CommandLogAdapter.LogViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_command_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgLogStatus)
        private val txtCommand: TextView = itemView.findViewById(R.id.txtLogCommand)
        private val txtSender: TextView = itemView.findViewById(R.id.txtLogSender)
        private val txtTime: TextView = itemView.findViewById(R.id.txtLogTime)
        private val txtResult: TextView = itemView.findViewById(R.id.txtLogResult)

        private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        fun bind(log: CommandLog) {
            txtCommand.text = "#${log.command}"
            txtSender.text = "From: ${log.senderNumber}"
            txtTime.text = dateFormat.format(Date(log.timestamp))
            txtResult.text = log.resultMessage ?: ""

            val (iconRes, colorRes) = when (log.status) {
                CommandStatus.SUCCESS -> R.drawable.ic_check to R.color.status_active
                CommandStatus.FAILED -> R.drawable.ic_error to R.color.status_inactive
                CommandStatus.UNAUTHORIZED -> R.drawable.ic_lock to R.color.status_warning
                CommandStatus.PROCESSING -> R.drawable.ic_sync to R.color.primary
                CommandStatus.RECEIVED -> R.drawable.ic_sms to R.color.text_secondary_light
            }

            imgStatus.setImageResource(iconRes)
            imgStatus.setColorFilter(itemView.context.getColor(colorRes))
            
            txtResult.visibility = if (log.resultMessage.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    class LogDiffCallback : DiffUtil.ItemCallback<CommandLog>() {
        override fun areItemsTheSame(oldItem: CommandLog, newItem: CommandLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommandLog, newItem: CommandLog): Boolean {
            return oldItem == newItem
        }
    }
}
