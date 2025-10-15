package com.example.avesargentinas

import android.Manifest
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
import androidx.lifecycle.lifecycleScope
import com.jsibbold.zoomage.AutoResetMode
import com.jsibbold.zoomage.ZoomageView
import com.google.android.material.button.MaterialButton
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

    // Estado
    private var originalBitmap: Bitmap? = null
    private var photoUri: Uri? = null
    private var reclassifyJob: Job? = null
    private var isProcessing = false
    private var ignoreMatrixChanges = false
    private var lastMatrixChangeTime = 0L
    private var pendingMatrixChange = false

    // Configuración
    private val CONF_THRESH = 0.55f
    private val MATRIX_CHANGE_DEBOUNCE_MS = 500L

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
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error en clasificación: ${e.message}", e)
            withContext(Dispatchers.Main) {
                showMessage("Error: ${e.message}")
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