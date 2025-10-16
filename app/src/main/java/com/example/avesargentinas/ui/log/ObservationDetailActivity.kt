package com.example.avesargentinas.ui.log

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationDetailActivity : BaseActivity() {

    // Permitir que el sistema ajuste el contenido cuando aparece el teclado
    override val allowKeyboardAdjustments: Boolean = true

    private lateinit var repository: ObservationRepository
    private var currentObservation: Observation? = null
    private var notesChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_detail)

        repository = ObservationRepository.getInstance(applicationContext)
        setupWindowInsets()
        setupToolbar()
        loadObservation()
        setupTouchListener()
    }
    
    /**
     * Configura el manejo de window insets para el teclado
     * Esto es más robusto que adjustResize en dispositivos modernos
     */
    private fun setupWindowInsets() {
        val rootLayout = findViewById<View>(R.id.rootLayout)
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Aplicar padding inferior cuando aparece el teclado
            scrollView.setPadding(
                scrollView.paddingLeft,
                scrollView.paddingTop,
                scrollView.paddingRight,
                imeInsets.bottom
            )
            
            // Cuando el teclado aparece, hacer scroll al campo enfocado
            if (imeInsets.bottom > 0) {
                scrollView.post {
                    val edtNotes: TextInputEditText? = findViewById(R.id.edtNotes)
                    if (edtNotes != null && edtNotes.isFocused) {
                        scrollView.smoothScrollTo(0, edtNotes.bottom + imeInsets.bottom)
                    }
                }
            }
            
            insets
        }
    }

    /**
     * Configura el listener para cerrar el teclado al tocar fuera del campo
     */
    private fun setupTouchListener() {
        val rootLayout = findViewById<View>(R.id.rootLayout)
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        
        // Listener en el root layout
        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleTouchOutside(event)
            }
            false
        }
        
        // Listener en el scroll view también
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleTouchOutside(event)
            }
            false
        }
    }
    
    private fun handleTouchOutside(event: MotionEvent) {
        val edtNotes: TextInputEditText? = findViewById(R.id.edtNotes)
        if (edtNotes != null && edtNotes.isFocused) {
            // Obtener las coordenadas del campo de notas
            val location = IntArray(2)
            edtNotes.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1]
            val width = edtNotes.width
            val height = edtNotes.height
            
            // Verificar si el toque fue fuera del campo
            if (event.rawX < x || event.rawX > x + width ||
                event.rawY < y || event.rawY > y + height) {
                // Cerrar teclado y quitar foco (el guardado se hace en onFocusChange)
                edtNotes.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtNotes.windowToken, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Guardar automáticamente al salir de la pantalla
        saveNotesIfChanged()
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
        val regionalName: TextView = findViewById(R.id.txtRegionalName)
        val edtNotes: TextInputEditText = findViewById(R.id.edtNotes)

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

        // Cargar notas existentes
        edtNotes.setText(observation.notes ?: "")

        val scrollView: NestedScrollView = findViewById(R.id.scrollView)
        
        // Detectar cambios en el foco
        edtNotes.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Cuando obtiene el foco, hacer scroll hacia el final
                // Usar múltiples intentos para asegurar que funcione en todos los dispositivos
                view.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
                view.postDelayed({
                    scrollView.fullScroll(View.FOCUS_DOWN)
                    // Hacer scroll específicamente al campo de notas
                    view.requestRectangleOnScreen(
                        android.graphics.Rect(0, 0, view.width, view.height),
                        false
                    )
                }, 100)
                view.postDelayed({
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }, 300)
            } else {
                // Cuando pierde el foco, guardar
                saveNotesIfChanged()
            }
        }

        // Marcar que hubo cambios al escribir
        edtNotes.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                notesChanged = true
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Cerrar teclado con la acción Done (el guardado se hace en onFocusChange)
        edtNotes.setOnEditorActionListener { v, _, _ ->
            // Ocultar teclado y quitar foco
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
            true
        }
    }

    private fun saveNotesIfChanged() {
        if (!notesChanged) return
        
        val observation = currentObservation ?: return
        val edtNotes: TextInputEditText? = findViewById(R.id.edtNotes)
        
        if (edtNotes == null) return
        
        val newNotes = edtNotes.text?.toString()?.takeIf { it.isNotBlank() }
        
        lifecycleScope.launch {
            val updated = observation.copy(notes = newNotes)
            repository.update(updated)
            currentObservation = updated
            notesChanged = false
            
            // Mostrar mensaje con Toast
            runOnUiThread {
                Toast.makeText(
                    this@ObservationDetailActivity,
                    "Notas guardadas",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
