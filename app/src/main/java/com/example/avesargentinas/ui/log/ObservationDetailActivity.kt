package com.example.avesargentinas.ui.log

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.avesargentinas.ObservationLogger
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.data.Observation
import com.example.avesargentinas.data.ObservationRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationDetailActivity : AppCompatActivity() {

    private lateinit var repository: ObservationRepository
    private var currentObservation: Observation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_detail)

        repository = ObservationRepository.getInstance(applicationContext)
        setupToolbar()
        loadObservation()
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.observation_detail_title)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadObservation() {
        val observationId = intent.getLongExtra(EXTRA_OBSERVATION_ID, -1L)
        if (observationId <= 0) {
            finish()
            return
        }

        lifecycleScope.launch {
            val observation = repository.getById(observationId)
            if (observation == null) {
                Toast.makeText(
                    this@ObservationDetailActivity,
                    getString(R.string.observation_not_found),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }
            currentObservation = observation
            bindData(observation)
            setupActions()
        }
    }

    private fun bindData(observation: Observation) {
        val image: ImageView = findViewById(R.id.imgObservation)
        val title: TextView = findViewById(R.id.txtTitle)
    val count: TextView = findViewById(R.id.txtCount)
        val scientific: TextView = findViewById(R.id.txtScientific)
        val confidence: TextView = findViewById(R.id.txtConfidence)
        val location: TextView = findViewById(R.id.txtLocation)
        val timestamp: TextView = findViewById(R.id.txtTimestamp)
        val notes: TextView = findViewById(R.id.txtNotes)

        Glide.with(this)
            .load(observation.imageUri)
            .fitCenter()
            .placeholder(R.drawable.ic_image_placeholder)
            .into(image)

        title.text = observation.displayName
        val countLabel = resources.getQuantityString(
            R.plurals.observation_individuals,
            observation.individualCount,
            observation.individualCount
        )
        count.text = getString(R.string.observation_detail_count_format, countLabel)
        scientific.text = getString(
            R.string.observation_scientific_format,
            observation.scientificName
        )
        confidence.text = getString(
            R.string.observation_confidence_format,
            observation.confidence
        )

        if (observation.latitude != null && observation.longitude != null) {
            location.text = getString(
                R.string.observation_location_format,
                observation.latitude,
                observation.longitude
            )
        } else {
            location.text = getString(R.string.observation_location_unknown)
        }

        timestamp.text = getString(
            R.string.observation_timestamp_format,
            formatTimestamp(observation.capturedAt)
        )

        val regionalAndNotes = buildString {
            observation.regionalName?.let {
                append(
                    getString(R.string.observation_regional_format, it)
                )
            }
            val userNotes = observation.notes
            if (!userNotes.isNullOrBlank()) {
                if (isNotEmpty()) append("\n")
                append(userNotes)
            }
        }

        if (regionalAndNotes.isNotBlank()) {
            notes.visibility = View.VISIBLE
            notes.text = regionalAndNotes
        } else {
            notes.visibility = View.GONE
        }
    }

    private fun setupActions() {
        val btnShare: MaterialButton = findViewById(R.id.btnShare)
        val btnDelete: MaterialButton = findViewById(R.id.btnDelete)

        btnShare.setOnClickListener { shareObservation() }
        btnDelete.setOnClickListener { deleteObservation() }
    }

    private fun shareObservation() {
        val observation = currentObservation ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(observation.imageUri))
            putExtra(Intent.EXTRA_TEXT, ObservationLogger.buildShareText(observation))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.observation_share_title)))
    }

    private fun deleteObservation() {
        val observation = currentObservation ?: return
        lifecycleScope.launch {
            repository.delete(observation)
            Toast.makeText(
                this@ObservationDetailActivity,
                getString(R.string.observation_deleted),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    companion object {
        const val EXTRA_OBSERVATION_ID = "extra_observation_id"
    }
}
