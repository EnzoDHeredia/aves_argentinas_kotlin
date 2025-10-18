package com.example.avesargentinas.ui.log

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.ObservationLogger
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.data.Observation
import com.example.avesargentinas.data.ObservationRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ObservationDetailActivity : BaseActivity() {

    // No necesitamos ajustes de teclado ya que usamos diálogo
    override val allowKeyboardAdjustments: Boolean = false

    private lateinit var repository: ObservationRepository
    private var currentObservation: Observation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_detail)

        repository = ObservationRepository.getInstance(applicationContext)
        // Ajuste dinámico para status bar / notch sobre el card de la toolbar
        val toolbarCard = findViewById<android.view.View>(R.id.toolbarCard)
        ViewCompat.setOnApplyWindowInsetsListener(toolbarCard) { v, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extraTopPx = (32 * resources.displayMetrics.density).toInt() // 32dp de separación visual (duplicado)
            val lp = v.layoutParams as? ViewGroup.MarginLayoutParams
            lp?.topMargin = statusBarTop + extraTopPx
            v.layoutParams = lp
            insets
        }
        ViewCompat.requestApplyInsets(toolbarCard)

        setupToolbar()
        loadObservation()

        val btnBackDetail: android.widget.ImageButton? = findViewById(R.id.btnBackDetail)
        btnBackDetail?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Reaplicar margin en onCreate (post) y en onResume para asegurarnos del orden de layout
        toolbarCard.post { applyTopMargin() }
    }

    override fun onResume() {
        super.onResume()
        applyTopMargin()
    }

    private fun applyTopMargin() {
        val rootView = findViewById<View>(android.R.id.content)
        val insets = ViewCompat.getRootWindowInsets(rootView)
        val statusBarTop = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        val extraTopPx = (32 * resources.displayMetrics.density).toInt()
        val toolbarCard = findViewById<View>(R.id.toolbarCard)
        val lp = toolbarCard.layoutParams as? ViewGroup.MarginLayoutParams
        lp?.topMargin = statusBarTop + extraTopPx
        toolbarCard.layoutParams = lp
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.observation_detail_title)
        // Único handler para navigation icon
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
        val regionalName: TextView = findViewById(R.id.txtRegionalName)
        val txtNotesPreview: TextView = findViewById(R.id.txtNotesPreview)
        val btnEditNotes: MaterialButton = findViewById(R.id.btnEditNotes)

        Glide.with(this)
            .load(observation.imageUri)
            .fitCenter()
            .placeholder(R.drawable.ic_image_placeholder)
            .into(image)

        // Hacer la imagen clickeable para zoom
        image.setOnClickListener {
            val intent = Intent(this, ImageZoomActivity::class.java)
            intent.putExtra(ImageZoomActivity.EXTRA_IMAGE_URI, observation.imageUri)
            startActivity(intent)
        }

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

        // Mostrar nombre regional si existe
        observation.regionalName?.let {
            regionalName.visibility = View.VISIBLE
            regionalName.text = getString(R.string.observation_regional_format, it)
        } ?: run {
            regionalName.visibility = View.GONE
        }

        // Mostrar preview de notas
        updateNotesPreview(observation.notes, txtNotesPreview)

        // Configurar botón de editar notas
        // Usar currentObservation si está disponible para evitar cargar una instancia antigua
        btnEditNotes.setOnClickListener {
            val target = currentObservation ?: observation
            showNotesDialog(target)
        }
    }

    private fun updateNotesPreview(notes: String?, txtNotesPreview: TextView) {
        if (notes.isNullOrBlank()) {
            txtNotesPreview.text = "Sin notas"
            txtNotesPreview.alpha = 0.5f
        } else {
            txtNotesPreview.text = notes
            txtNotesPreview.alpha = 1f
        }
    }

    private fun showNotesDialog(observation: Observation) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_observation_notes)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val edtNotes: TextInputEditText = dialog.findViewById(R.id.edtNotes)
        val btnSaveNotes: MaterialButton = dialog.findViewById(R.id.btnSaveNotes)
        val btnCancelNotes: MaterialButton = dialog.findViewById(R.id.btnCancelNotes)

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Cargar notas actuales
        edtNotes.setText(observation.notes ?: "")

        btnSaveNotes.setOnClickListener {
            // Guardar notas en background y mantener el diálogo abierto mostrando el valor guardado
            lifecycleScope.launch {
                val newNotes = edtNotes.text?.toString()?.takeIf { it.isNotBlank() }
                val updated = saveNotes(observation, newNotes)

                // Actualizar EditText del diálogo con el valor guardado (por si hubo normalización)
                edtNotes.setText(updated.notes ?: "")

                // Actualizar preview de la pantalla principal
                val txtNotesPreview: TextView = findViewById(R.id.txtNotesPreview)
                updateNotesPreview(updated.notes, txtNotesPreview)

                Toast.makeText(
                    this@ObservationDetailActivity,
                    "Notas guardadas",
                    Toast.LENGTH_SHORT
                ).show()
                // Cerrar el diálogo tras guardar
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }

        btnCancelNotes.setOnClickListener {
            dialog.dismiss()
        }
    }

    // Ahora es suspend y devuelve la observación actualizada
    private suspend fun saveNotes(observation: Observation, notes: String?): Observation {
        val updated = observation.copy(notes = notes)
        repository.update(updated)
        currentObservation = updated
        return updated
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
