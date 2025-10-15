package com.example.avesargentinas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.avesargentinas.data.ObservationRepository
import com.example.avesargentinas.ui.log.ObservationLogActivity
import com.jsibbold.zoomage.AutoResetMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    // Componentes de negocio
    private lateinit var birdClassifier: BirdClassifier
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var focusManager: FocusManager

    // Vistas
    private lateinit var photoView: InteractiveZoomageView
    private lateinit var focusOverlay: FocusOverlayView
    private lateinit var txtResult: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var btnPick: MaterialButton
    private lateinit var btnCamera: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLog: MaterialButton

    // Estado
    private var originalBitmap: Bitmap? = null
    private var photoUri: Uri? = null
    private var reclassifyJob: Job? = null
    private var isProcessing = false
    private var ignoreMatrixChanges = false
    private var lastMatrixChangeTime = 0L
    private var pendingMatrixChange = false
    private var lastPrediction: Prediction? = null
    private var pendingSaveAfterPermission = false
    private var pendingObservationCount: Int = 1

    // Configuración
    private val CONF_THRESH = 0.55f
    private val MATRIX_CHANGE_DEBOUNCE_MS = 500L

    // Dependencias auxiliares
    private lateinit var observationRepository: ObservationRepository
    private lateinit var observationLogger: ObservationLogger
    private lateinit var locationProvider: LocationProvider

    // Activity Result Launchers
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (!allGranted) {
                Toast.makeText(this, "Se necesitan permisos para usar la app", Toast.LENGTH_LONG).show()
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { loadAndClassifyImage(it) }
        }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                loadAndClassifyImage(photoUri!!)
            }
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (pendingSaveAfterPermission) {
                lifecycleScope.launch {
                    performSaveObservation(pendingObservationCount)
                }
            }
            pendingSaveAfterPermission = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeComponents()
        setupPhotoView()
        setupButtons()
        setupEmptyState()
        checkAndRequestPermissions()
    }

    private fun initializeViews() {
        photoView = findViewById(R.id.photoView)
        focusOverlay = findViewById(R.id.focusOverlay)
        txtResult = findViewById(R.id.txt)
        progressBar = findViewById(R.id.progress)
        emptyState = findViewById(R.id.emptyState)
        btnPick = findViewById(R.id.btnPick)
        btnCamera = findViewById(R.id.btnCamera)
        btnSave = findViewById(R.id.btnSave)
        btnLog = findViewById(R.id.btnLog)
    }

    private fun initializeComponents() {
        imageProcessor = ImageProcessor()

        birdClassifier = BirdClassifier(assets, numThreads = 4)
        birdClassifier.initialize().fold(
            onSuccess = {
                Log.i("MainActivity", "Clasificador inicializado correctamente")
            },
            onFailure = { error ->
                showMessage("Error al inicializar modelo: ${error.message}")
            }
        )

        focusManager = FocusManager(photoView, focusOverlay, imageProcessor)

        observationRepository = ObservationRepository.getInstance(applicationContext)
        val imageSaver = ImageSaver(applicationContext)
        locationProvider = LocationProvider(applicationContext)
        observationLogger = ObservationLogger(observationRepository, imageSaver)
    }

    private fun setupPhotoView() {
        photoView.apply {
            setScaleRange(0.3f, 15f)
            setDoubleTapToZoomScaleFactor(3f)
            setAutoResetMode(AutoResetMode.NEVER)
            setRestrictBounds(false) // Permite mover la imagen hasta los bordes del recuadro
            setAutoCenter(false) // Evita que la imagen vuelva sola al centro al soltar

            setOnMatrixChangeListener {
                if (!ignoreMatrixChanges && originalBitmap != null) {
                    lastMatrixChangeTime = System.currentTimeMillis()

                    if (isProcessing) {
                        // Si está procesando, marcar que hay cambios pendientes
                        pendingMatrixChange = true
                    } else {
                        // Si no está procesando, actualizar y programar clasificación
                        focusManager.updateOverlay()
                        scheduleReclassify()
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        btnPick.setOnClickListener { openGallery() }
        btnCamera.setOnClickListener { openCamera() }
        btnSave.setOnClickListener { handleSaveObservationClick() }
        btnLog.setOnClickListener {
            startActivity(Intent(this, ObservationLogActivity::class.java))
        }
    }

    private fun setupEmptyState() {
        emptyState.setOnClickListener { openGallery() }
    }

    private fun openGallery() {
        if (hasStoragePermission()) {
            pickImage.launch("image/*")
        } else {
            Toast.makeText(this, "Permiso de almacenamiento necesario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        if (hasCameraPermission()) {
            photoUri = createImageUri()
            photoUri?.let { uri -> takePhoto.launch(uri) }
        } else {
            Toast.makeText(this, "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSaveObservationClick() {
        val predictionAvailable = lastPrediction != null
        val hasBitmap = originalBitmap != null

        if (!predictionAvailable) {
            showMessage(getString(R.string.observation_save_missing_prediction))
            btnSave.isEnabled = false
            return
        }

        if (!hasBitmap) {
            showMessage(getString(R.string.observation_save_missing_image))
            btnSave.isEnabled = false
            return
        }

        cancelReclassifyJob()

        showObservationCountDialog { count ->
            pendingObservationCount = count
            if (!hasLocationPermission()) {
                pendingSaveAfterPermission = true
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                pendingSaveAfterPermission = false
                lifecycleScope.launch {
                    performSaveObservation(count)
                }
            }
        }
    }

    private fun showObservationCountDialog(onCountConfirmed: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_observation_count, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.inputCountLayout)
        val input = dialogView.findViewById<TextInputEditText>(R.id.editCount)
        val decrement = dialogView.findViewById<MaterialButton>(R.id.btnDecrement)
        val increment = dialogView.findViewById<MaterialButton>(R.id.btnIncrement)

        var currentCount = pendingObservationCount.coerceAtLeast(1)
        var programmaticChange = false

        fun updateCount(newValue: Int) {
            val sanitized = newValue.coerceAtLeast(1)
            if (currentCount == sanitized && !programmaticChange) return
            currentCount = sanitized
            programmaticChange = true
            input.setText(currentCount.toString())
            input.setSelection(input.text?.length ?: 0)
            programmaticChange = false
            inputLayout.error = null
        }

        input.doAfterTextChanged { editable ->
            if (programmaticChange) return@doAfterTextChanged
            val value = editable?.toString()?.toIntOrNull()
            if (value != null && value > 0) {
                currentCount = value
                inputLayout.error = null
            } else {
                inputLayout.error = getString(R.string.observation_count_invalid)
            }
        }

        decrement.setOnClickListener {
            updateCount(currentCount - 1)
        }

        increment.setOnClickListener {
            updateCount(currentCount + 1)
        }

        updateCount(currentCount)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.observation_count_positive) { _, _ ->
                val value = input.text?.toString()?.toIntOrNull()
                val sanitized = value?.takeIf { it > 0 } ?: currentCount
                onCountConfirmed(sanitized)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private suspend fun performSaveObservation(individualCount: Int) {
        val prediction = lastPrediction ?: run {
            showMessage(getString(R.string.observation_save_missing_prediction))
            btnSave.isEnabled = false
            return
        }

        val original = originalBitmap ?: run {
            showMessage(getString(R.string.observation_save_missing_image))
            btnSave.isEnabled = false
            return
        }

        cancelReclassifyJob()

        val previousIgnore = ignoreMatrixChanges
        ignoreMatrixChanges = true

        btnSave.isEnabled = false
        var saveCompleted = false

        try {
            when (val cropResult = focusManager.cropToFocus(original)) {
                is CropResult.Error -> {
                    showMessage(cropResult.message)
                }
                is CropResult.Success -> {
                    val focusBitmap = cropResult.bitmap
                    try {
                        val coordinates = locationProvider.getCurrentLocation(this@MainActivity)
                        val saveResult = observationLogger.saveObservation(
                            original,
                            prediction,
                            coordinates,
                            individualCount
                        )

                        when (saveResult) {
                            is ObservationLogger.Result.Success -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.observation_saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                                saveCompleted = true
                            }
                            is ObservationLogger.Result.Error -> showMessage(saveResult.message)
                        }
                    } finally {
                        if (focusBitmap != original) {
                            focusBitmap.recycle()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al guardar observación: ${e.message}", e)
            showMessage("Error al guardar: ${e.message}")
        } finally {
            ignoreMatrixChanges = previousIgnore
            btnSave.isEnabled = lastPrediction != null
            pendingObservationCount = if (saveCompleted) 1 else individualCount
        }
    }

    // ==================== Gestión de Imágenes ====================

    private fun loadAndClassifyImage(uri: Uri) {
        val bitmap = imageProcessor.decodeBitmap(contentResolver, uri)
        if (bitmap == null) {
            showMessage("No se pudo leer la imagen")
            return
        }
        displayImageAndClassify(bitmap)
    }

    private fun displayImageAndClassify(bitmap: Bitmap) {
        // Cancelar cualquier clasificación pendiente
        reclassifyJob?.cancel()
        pendingMatrixChange = false
    lastPrediction = null
    btnSave.isEnabled = false

        // Limpiar bitmap anterior
        originalBitmap?.recycle()
        originalBitmap = bitmap

        // Bloquear detección de cambios temporalmente
        ignoreMatrixChanges = true

        // Mostrar imagen - el visor no debe ajustar zoom automáticamente
        photoView.setImageBitmap(bitmap)
        emptyState.visibility = View.GONE
        focusManager.showOverlay()

        // Esperar a que se renderice completamente
        photoView.post {
            focusManager.updateOverlay()

            // Esperar a que termine cualquier animación automática del visor
            photoView.postDelayed({
                // Reactivar detección
                ignoreMatrixChanges = false
                lastMatrixChangeTime = System.currentTimeMillis()

                // Primera clasificación
                scheduleReclassify()
            }, 200)
        }
    }

    private fun createImageUri(): Uri? {
        return try {
            val imageFile = File.createTempFile(
                "photo_${System.currentTimeMillis()}",
                ".jpg",
                cacheDir
            )
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    // ==================== Clasificación ====================

    private fun scheduleReclassify() {
        // Cancelar job anterior
        reclassifyJob?.cancel()

        // Programar nueva clasificación con debounce inteligente
        reclassifyJob = lifecycleScope.launch {
            delay(MATRIX_CHANGE_DEBOUNCE_MS)

            // Verificar si ha habido cambios muy recientes
            val timeSinceLastChange = System.currentTimeMillis() - lastMatrixChangeTime
            if (timeSinceLastChange < MATRIX_CHANGE_DEBOUNCE_MS - 50) {
                // Hubo cambios muy recientes, reprogramar
                scheduleReclassify()
            } else {
                classifyCurrentView()
            }
        }
    }

    private fun cancelReclassifyJob() {
        reclassifyJob?.cancel()
        reclassifyJob = null
        pendingMatrixChange = false
    }

    private suspend fun classifyCurrentView() {
        val original = originalBitmap ?: return

        // Prevenir clasificaciones concurrentes
        if (isProcessing) return

        isProcessing = true

        // Bloquear listener durante TODO el proceso de clasificación
        withContext(Dispatchers.Main) {
            ignoreMatrixChanges = true
        }

        setLoading(true)

        try {
            // Recortar según el recuadro de enfoque
            val cropResult = withContext(Dispatchers.Default) {
                focusManager.cropToFocus(original)
            }

            when (cropResult) {
                is CropResult.Success -> {
                    classifyBitmap(cropResult.bitmap)
                    if (cropResult.bitmap != original) {
                        cropResult.bitmap.recycle()
                    }
                }
                is CropResult.Error -> {
                    withContext(Dispatchers.Main) {
                        showMessage(cropResult.message)
                        lastPrediction = null
                        btnSave.isEnabled = false
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error en clasificación: ${e.message}", e)
            withContext(Dispatchers.Main) {
                showMessage("Error: ${e.message}")
                lastPrediction = null
                btnSave.isEnabled = false
            }
        } finally {
            setLoading(false)

            // Esperar a que todo se estabilice antes de reactivar
            withContext(Dispatchers.Main) {
                ignoreMatrixChanges = true

                photoView.postDelayed({
                    // Verificar si hubo cambios RECIENTES durante el procesamiento
                    val timeSinceLastChange = System.currentTimeMillis() - lastMatrixChangeTime
                    val hadRecentChanges = pendingMatrixChange && timeSinceLastChange < 400
                    
                    // Resetear estados
                    isProcessing = false
                    pendingMatrixChange = false
                    ignoreMatrixChanges = false  // Reactivar listener

                    // Solo reclasificar si hubo cambios MUY recientes
                    if (hadRecentChanges) {
                        focusManager.updateOverlay()
                        scheduleReclassify()
                    }
                }, 200)  // Aumentado a 200ms para mayor estabilidad
            }
        }
    }

    private suspend fun classifyBitmap(bitmap: Bitmap) {
        val result = withContext(Dispatchers.Default) {
            birdClassifier.classify(bitmap, topK = 3)
        }

        val resultText = buildResultText(result)

        withContext(Dispatchers.Main) {
            txtResult.text = resultText
            lastPrediction = result.topPrediction
            btnSave.isEnabled = lastPrediction != null
        }
    }

    private fun buildResultText(result: ClassificationResult): String {
        return buildString {
            val best = result.topPrediction
            if (best == null) {
                append("No se pudo clasificar la imagen")
                return@buildString
            }

            if (best.probability >= CONF_THRESH) {
                append(best.displayName)
                append("\nNombre científico: ${best.scientificName}")
                append("\nConfianza: ${best.confidencePercentage.format(1)}%")

                val alternatives = result.predictions.drop(1).filter { it.probability > 0.1f }
                if (alternatives.isNotEmpty()) {
                    append("\n\nOtras posibilidades:\n")
                    alternatives.forEach { pred ->
                        append("• ${pred.displayName} (${pred.confidencePercentage.format(1)}%)\n")
                    }
                }
            } else {
                append("Confianza baja: ${best.confidencePercentage.format(1)}%")
                append("\n\nIntenta:\n")
                append("• Acercar más al ave\n")
                append("• Mejor iluminación\n")
                append("• Imagen más clara")
            }
        }
    }

    // ==================== Permisos ====================

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (!hasCameraPermission()) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (!hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (!hasLocationPermission()) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            requestPermissions.launch(permissions.toTypedArray())
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    // ==================== UI Helpers ====================

    private fun setLoading(on: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (on) View.VISIBLE else View.INVISIBLE
            txtResult.alpha = if (on) 0.3f else 1f
            btnPick.isEnabled = !on
            btnCamera.isEnabled = !on
        }
    }

    private fun showMessage(message: String) {
        txtResult.text = message
    }

    private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)

    override fun onDestroy() {
        super.onDestroy()
        reclassifyJob?.cancel()
        originalBitmap?.recycle()
        birdClassifier.close()
    }
}