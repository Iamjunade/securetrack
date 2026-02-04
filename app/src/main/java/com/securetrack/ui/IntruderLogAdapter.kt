package com.securetrack.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securetrack.R
import com.securetrack.data.IntruderLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IntruderLogAdapter(private val onImageClick: (String) -> Unit) : 
    ListAdapter<IntruderLog, IntruderLogAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intruder_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIntruder: ImageView = itemView.findViewById(R.id.imgIntruder)
        private val txtTimestamp: TextView = itemView.findViewById(R.id.txtTimestamp)
        private val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        fun bind(log: IntruderLog) {
            txtTimestamp.text = dateFormat.format(Date(log.timestamp))
            txtLocation.text = "Lat/Lng: Unknown" // Placeholder until we link location

            // Load Image
            val imgFile = File(log.imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imgIntruder.setImageBitmap(bitmap)
            } else {
                imgIntruder.setImageResource(R.drawable.ic_error)
            }

            itemView.setOnClickListener { onImageClick(log.imagePath) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<IntruderLog>() {
        override fun areItemsTheSame(oldItem: IntruderLog, newItem: IntruderLog) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: IntruderLog, newItem: IntruderLog) = oldItem == newItem
    }
}
