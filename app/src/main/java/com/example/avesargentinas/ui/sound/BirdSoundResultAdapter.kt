package com.example.avesargentinas.ui.sound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.avesargentinas.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class BirdSoundResultAdapter(
    private val onBirdInfoClicked: (BirdDetection) -> Unit
) : ListAdapter<BirdDetection, BirdSoundResultAdapter.ViewHolder>(BirdDetectionDiffCallback) {

    object BirdDetectionDiffCallback : DiffUtil.ItemCallback<BirdDetection>() {
        override fun areItemsTheSame(oldItem: BirdDetection, newItem: BirdDetection): Boolean {
            return canonicalScientificName(oldItem.scientificName) == canonicalScientificName(newItem.scientificName)
        }

        override fun areContentsTheSame(oldItem: BirdDetection, newItem: BirdDetection): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: BirdDetection, newItem: BirdDetection): Any? {
            return if (oldItem.score != newItem.score || oldItem.commonName != newItem.commonName) {
                SCORE_CHANGE_PAYLOAD
            } else {
                null
            }
        }

        private fun canonicalScientificName(name: String): String {
            return name.trim().lowercase(Locale.ROOT)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCommon: TextView = itemView.findViewById(R.id.txtBirdCommonName)
        val txtScientific: TextView = itemView.findViewById(R.id.txtBirdScientificName)
        val txtScore: TextView = itemView.findViewById(R.id.txtBirdScore)
        val btnViewInfo: MaterialButton = itemView.findViewById(R.id.btnViewBirdInfo)
        val txtUnavailable: TextView = itemView.findViewById(R.id.txtBirdInfoUnavailable)

        fun bind(detection: BirdDetection, onBirdInfoClicked: (BirdDetection) -> Unit) {
            val context = itemView.context
            val birdDisplayName = detection.commonName ?: detection.scientificName

            if (detection.commonName != null) {
                txtCommon.text = detection.commonName
                txtScientific.text = detection.scientificName
            } else {
                txtCommon.text = detection.scientificName
                txtScientific.text = context.getString(R.string.sound_common_name_unavailable)
            }

            bindScore(detection)

            if (detection.localKey != null) {
                btnViewInfo.visibility = View.VISIBLE
                btnViewInfo.contentDescription = context.getString(
                    R.string.sound_action_view_info_description,
                    birdDisplayName
                )
                txtUnavailable.visibility = View.GONE
                btnViewInfo.setOnClickListener {
                    onBirdInfoClicked(detection)
                }
            } else {
                btnViewInfo.visibility = View.GONE
                txtUnavailable.visibility = View.VISIBLE
            }
        }

        fun bindScore(detection: BirdDetection) {
            val context = itemView.context
            txtScore.text = context.getString(
                R.string.sound_score_format,
                String.format(Locale.US, "%.1f", detection.score)
            )
            txtScore.contentDescription = context.getString(
                R.string.sound_score_description,
                String.format(Locale.getDefault(), "%.1f", detection.score)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_bird_sound_result,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onBirdInfoClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val detection = getItem(position)
            holder.bindScore(detection)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        val SCORE_CHANGE_PAYLOAD = Any()
    }
}
