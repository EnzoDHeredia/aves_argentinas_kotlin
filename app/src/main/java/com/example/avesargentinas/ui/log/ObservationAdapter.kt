package com.example.avesargentinas.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.avesargentinas.R
import com.example.avesargentinas.data.Observation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationAdapter(
    private val onObservationSelected: (Observation) -> Unit
) : ListAdapter<Observation, ObservationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_observation, parent, false)
        return ViewHolder(view, onObservationSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onClick: (Observation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val thumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val title: TextView = itemView.findViewById(R.id.txtTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
        private val timestamp: TextView = itemView.findViewById(R.id.txtTimestamp)

        fun bind(observation: Observation) {
            Glide.with(thumbnail)
                .load(observation.imageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .into(thumbnail)

            title.text = observation.displayName
            val confidenceText = "Confianza ${"%.1f".format(Locale.getDefault(), observation.confidence)}%"
            val scientific = observation.scientificName
            subtitle.text = "$scientific · $confidenceText"
            timestamp.text = formatTimestamp(observation.capturedAt)

            itemView.setOnClickListener { onClick(observation) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Observation>() {
        override fun areItemsTheSame(oldItem: Observation, newItem: Observation): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Observation, newItem: Observation): Boolean =
            oldItem == newItem
    }
}
