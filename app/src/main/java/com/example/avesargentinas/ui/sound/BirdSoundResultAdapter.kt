package com.example.avesargentinas.ui.sound

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.avesargentinas.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale
import kotlin.math.roundToInt

class BirdSoundResultAdapter(
    private val onBirdInfoClicked: (BirdDetection) -> Unit
) : ListAdapter<BirdDetection, BirdSoundResultAdapter.ViewHolder>(BirdDetectionDiffCallback) {

    private val pendingHighlightKeys = mutableSetOf<String>()

    @VisibleForTesting
    internal var animationsEnabledProvider: (Context) -> Boolean = { context ->
        defaultAnimationsEnabled(context)
    }

    object BirdDetectionDiffCallback : DiffUtil.ItemCallback<BirdDetection>() {
        override fun areItemsTheSame(oldItem: BirdDetection, newItem: BirdDetection): Boolean {
            return canonicalScientificName(oldItem.scientificName) == canonicalScientificName(newItem.scientificName)
        }

        override fun areContentsTheSame(oldItem: BirdDetection, newItem: BirdDetection): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: BirdDetection, newItem: BirdDetection): Any? {
            return if (newItem.score > oldItem.score) {
                SCORE_IMPROVED_PAYLOAD
            } else if (oldItem.score != newItem.score || oldItem.commonName != newItem.commonName) {
                SCORE_CHANGE_PAYLOAD
            } else {
                null
            }
        }

        internal fun canonicalScientificName(name: String): String {
            return name.trim().lowercase(Locale.ROOT)
        }
    }

    fun submitList(
        list: List<BirdDetection>?,
        highlightNewItems: Boolean,
        commitCallback: Runnable? = null
    ) {
        if (highlightNewItems && list != null) {
            val newKeys = findNewSpeciesKeys(currentList, list)
            pendingHighlightKeys.addAll(newKeys)
        } else {
            pendingHighlightKeys.clear()
        }
        super.submitList(list, commitCallback)
    }

    override fun submitList(list: List<BirdDetection>?) {
        submitList(list, highlightNewItems = true, commitCallback = null)
    }

    override fun submitList(list: List<BirdDetection>?, commitCallback: Runnable?) {
        submitList(list, highlightNewItems = true, commitCallback = commitCallback)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val txtCommon: TextView = itemView.findViewById(R.id.txtBirdCommonName)
        val txtScientific: TextView = itemView.findViewById(R.id.txtBirdScientificName)
        val txtScore: TextView = itemView.findViewById(R.id.txtBirdScore)
        val btnViewInfo: MaterialButton = itemView.findViewById(R.id.btnViewBirdInfo)
        val txtUnavailable: TextView = itemView.findViewById(R.id.txtBirdInfoUnavailable)

        private val originalColor: Int = ContextCompat.getColor(itemView.context, R.color.brand_surface)
        private val highlightColor: Int = ContextCompat.getColor(itemView.context, R.color.sound_card_highlight)
        private var highlightAnimator: ValueAnimator? = null

        @VisibleForTesting
        internal val isHighlighting: Boolean
            get() = highlightAnimator?.isRunning == true

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
            val percent = scoreToPercent(detection.score)
            txtScore.text = context.getString(
                R.string.sound_score_format,
                percent
            )
            txtScore.contentDescription = context.getString(
                R.string.sound_score_description,
                percent
            )
        }

        fun startHighlightAnimation(animationsEnabled: Boolean) {
            cancelHighlightAnimation()
            if (!animationsEnabled) {
                cardView.setCardBackgroundColor(originalColor)
                return
            }
            cardView.setCardBackgroundColor(highlightColor)
            val animator = ValueAnimator.ofObject(ArgbEvaluator(), highlightColor, originalColor).apply {
                duration = HIGHLIGHT_DURATION_MS
                addUpdateListener { va ->
                    val color = va.animatedValue as? Int ?: return@addUpdateListener
                    cardView.setCardBackgroundColor(color)
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (highlightAnimator === animation) {
                            highlightAnimator = null
                            cardView.setCardBackgroundColor(originalColor)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        if (highlightAnimator === animation) {
                            highlightAnimator = null
                        }
                    }
                })
            }
            highlightAnimator = animator
            animator.start()
        }

        fun cancelHighlightAnimation() {
            highlightAnimator?.cancel()
            highlightAnimator = null
            cardView.setCardBackgroundColor(originalColor)
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
        val detection = getItem(position)
        holder.bind(detection, onBirdInfoClicked)
        val key = BirdDetectionDiffCallback.canonicalScientificName(detection.scientificName)
        if (pendingHighlightKeys.remove(key)) {
            holder.startHighlightAnimation(animationsEnabledProvider(holder.itemView.context))
        } else {
            holder.cancelHighlightAnimation()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val detection = getItem(position)
            holder.bindScore(detection)
            if (payloads.any { it === SCORE_IMPROVED_PAYLOAD }) {
                holder.startHighlightAnimation(animationsEnabledProvider(holder.itemView.context))
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelHighlightAnimation()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.cancelHighlightAnimation()
    }

    companion object {
        val SCORE_CHANGE_PAYLOAD = Any()
        val SCORE_IMPROVED_PAYLOAD = Any()
        const val HIGHLIGHT_DURATION_MS = 1200L

        fun scoreToPercent(score: Float): Int {
            if (score.isNaN()) return 0
            return (score * 100f).roundToInt().coerceIn(0, 100)
        }

        fun findNewSpeciesKeys(
            previousList: List<BirdDetection>,
            newList: List<BirdDetection>
        ): Set<String> {
            val previousKeys = HashSet<String>(previousList.size)
            for (item in previousList) {
                previousKeys.add(BirdDetectionDiffCallback.canonicalScientificName(item.scientificName))
            }
            val newKeys = LinkedHashSet<String>()
            for (item in newList) {
                val key = BirdDetectionDiffCallback.canonicalScientificName(item.scientificName)
                if (!previousKeys.contains(key)) {
                    newKeys.add(key)
                }
            }
            return newKeys
        }

        private fun defaultAnimationsEnabled(context: Context): Boolean = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ValueAnimator.areAnimatorsEnabled()
            } else {
                Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f) > 0f
            }
        } catch (_: Throwable) {
            false
        }
    }
}
