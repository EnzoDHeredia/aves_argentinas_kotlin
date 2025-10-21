package com.example.avesargentinas

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
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

class MainActivity : BaseActivity() {

    // Componentes de negocio
    private lateinit var birdClassifier: BirdClassifier
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var focusManager: FocusManager

    // Vistas
    private lateinit var photoView: InteractiveZoomageView
    private lateinit var focusOverlay: FocusOverlayView
    private lateinit var txtResult: TextView
    private var labelResultado: TextView? = null // Optional: only present in certain layouts
    private lateinit var resultHeader: View
    private lateinit var txtBirdNameHeader: TextView
    private lateinit var txtScientificNameHeader: TextView
    private lateinit var chipConfidence: com.google.android.material.chip.Chip
    private lateinit var imgExpertWarning: ImageView
    private lateinit var txtPositiveMsg: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var btnPick: MaterialButton
    private lateinit var btnCamera: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLog: MaterialButton
    private lateinit var btnBirdInfo: ImageButton
    private lateinit var infoHint: TextView
    private lateinit var infoContainer: View
    private lateinit var btnThemeToggle: ImageButton
    private lateinit var btnBackMain: ImageButton

    // Estado
    private var originalBitmap: Bitmap? = null
    private var photoUri: Uri? = null
    private var reclassifyJob: Job? = null
    private var isProcessing = false
    private var ignoreMatrixChanges = false
    private var lastMatrixChangeTime = 0L
    private var pendingMatrixChange = false
    private var lastPrediction: Prediction? = null
    private var lastAnyPrediction: Prediction? = null
    private var pendingSaveAfterPermission = false
    private var pendingObservationCount: Int = 1
    private var currentImageUri: Uri? = null

    // ConfiguraciÃƒÂ³n
    private val CONF_THRESH = 0.60f
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
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sin ajuste dinÃƒÂ¡mico de insets: el sistema maneja los WindowInsets.

        initializeViews()
        initializeComponents()
        setupPhotoView()
        setupButtons()
        setupBackNavigation()
        updateThemeToggleIcon()
        setupEmptyState()
        checkAndRequestPermissions()
        restoreState(savedInstanceState)

    }

    private fun initializeViews() {
        photoView = findViewById(R.id.photoView)
        focusOverlay = findViewById(R.id.focusOverlay)
        txtResult = findViewById(R.id.txt)
        val labelResourceId = resources.getIdentifier("labelResultado", "id", packageName)
        if (labelResourceId != 0) {
            labelResultado = findViewById(labelResourceId)
        }
        resultHeader = findViewById(R.id.resultHeader)
        txtBirdNameHeader = findViewById(R.id.txtBirdName)
        txtScientificNameHeader = findViewById(R.id.txtScientificName)
        chipConfidence = findViewById(R.id.chipConfidence)
        imgExpertWarning = findViewById(R.id.imgExpertWarning)
        txtPositiveMsg = findViewById(R.id.txtPositiveMsg)
        progressBar = findViewById(R.id.progress)
        emptyState = findViewById(R.id.emptyState)
        btnPick = findViewById(R.id.btnPick)
        btnCamera = findViewById(R.id.btnCamera)
        btnSave = findViewById(R.id.btnSave)
        btnLog = findViewById(R.id.btnLog)
        btnBirdInfo = findViewById(R.id.btnBirdInfo)
        infoHint = findViewById(R.id.infoHint)
        infoContainer = findViewById(R.id.infoContainer)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
        btnBackMain = findViewById(R.id.btnBackMain)
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
                        // Si estÃƒÂ¡ procesando, marcar que hay cambios pendientes
                        pendingMatrixChange = true
                    } else {
                        // Si no estÃƒÂ¡ procesando, actualizar y programar clasificaciÃƒÂ³n
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
        btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeToggleIcon()
        }
        btnBirdInfo.setOnClickListener {
            val pred = lastAnyPrediction ?: return@setOnClickListener
            runCatching {
                val intent = Intent(this, com.example.avesargentinas.ui.bird.BirdInfoActivity::class.java)
                intent.putExtra(com.example.avesargentinas.ui.bird.BirdInfoActivity.EXTRA_COMMON_NAME, pred.displayName)
                intent.putExtra(com.example.avesargentinas.ui.bird.BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, pred.scientificName)
                intent.putExtra(com.example.avesargentinas.ui.bird.BirdInfoActivity.EXTRA_CONFIDENCE, pred.probability)
                startActivity(intent)
            }.onFailure { e ->
                Toast.makeText(this, "No pude abrir info del ave: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        btnBackMain.setOnClickListener {
            finish()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Al presionar atrÃƒÂ¡s, ir al menÃƒÂº principal
                // AÃƒÂ±adir flag para indicar que venimos de un back press
                val intent = Intent(this@MainActivity, MainMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("from_back_press", true)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun setupEmptyState() {
        emptyState.setOnClickListener { openGallery() }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        val uriString = savedInstanceState?.getString(KEY_CURRENT_IMAGE_URI)
        if (!uriString.isNullOrBlank()) {
            val uri = Uri.parse(uriString)
            photoView.post { loadAndClassifyImage(uri, restored = true) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        currentImageUri?.let { uri ->
            outState.putString(KEY_CURRENT_IMAGE_URI, uri.toString())
        }
        super.onSaveInstanceState(outState)
    }

    private fun updateThemeToggleIcon() {
        val isDark = ThemeManager.isDarkTheme(this)
        val iconRes = if (isDark) R.drawable.ic_sun else R.drawable.ic_moon
        val descriptionRes = if (isDark) R.string.theme_toggle_light else R.string.theme_toggle_dark
        btnThemeToggle.setImageDrawable(AppCompatResources.getDrawable(this, iconRes))
        btnThemeToggle.contentDescription = getString(descriptionRes)
        btnThemeToggle.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.brand_on_surface)
        )
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
            Toast.makeText(this, "Permiso de cÃƒÂ¡mara necesario", Toast.LENGTH_SHORT).show()
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

        // En modo experto, mostrar selector de ave primero
        val isExpertMode = SettingsManager.isExpertMode(this@MainActivity)
        if (isExpertMode) {
            showBirdSelectorDialog { scientificName, commonName ->
                // Actualizar la predicciÃƒÂ³n con la ave seleccionada
                val currentPrediction = lastPrediction
                if (currentPrediction != null) {
                    lastPrediction = currentPrediction.copy(
                        displayName = commonName,
                        scientificName = scientificName
                    )
                }
                
                // Mostrar diÃƒÂ¡logo de cantidad
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
        } else {
            // Modo normal: ir directo a cantidad
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
    }

    private fun showObservationCountDialog(onCountConfirmed: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_observation_count, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.inputCountLayout)
        val input = dialogView.findViewById<TextInputEditText>(R.id.editCount)
        val decrement = dialogView.findViewById<MaterialButton>(R.id.btnDecrement)
        val increment = dialogView.findViewById<MaterialButton>(R.id.btnIncrement)
        val cancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val confirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirm)

        val horizontalInset = resources.getDimensionPixelSize(R.dimen.dialog_horizontal_inset)
        val displayWidth = resources.displayMetrics.widthPixels
        val targetWidth = (displayWidth - horizontalInset * 2).coerceAtLeast(displayWidth / 2)
        dialogView.layoutParams = ViewGroup.LayoutParams(targetWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        var currentCount = pendingObservationCount.coerceAtLeast(1)
        var programmaticChange = false

        fun updateCount(newValue: Int) {
            val sanitized = newValue.coerceAtLeast(1)
            currentCount = sanitized
            val existingValue = input.text?.toString()?.toIntOrNull()
            if (!programmaticChange && existingValue == sanitized) {
                inputLayout.error = null
                return
            }
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

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
            .create()

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        confirm.setOnClickListener {
            val typedValue = input.text?.toString()?.toIntOrNull()
            if (typedValue == null || typedValue <= 0) {
                inputLayout.error = getString(R.string.observation_count_invalid)
                programmaticChange = true
                input.setText(currentCount.toString())
                input.setSelection(input.text?.length ?: 0)
                programmaticChange = false
                return@setOnClickListener
            }

            updateCount(typedValue)
            onCountConfirmed(currentCount)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(targetWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showBirdSelectorDialog(onBirdSelected: (scientificName: String, commonName: String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_bird_selector, null)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteBird)
        val txtScientific = dialogView.findViewById<TextView>(R.id.txtScientificName)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelBird)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmBird)
        
        // Cargar nombres comunes
        val commonNames = BirdLabelsManager.getCommonNamesArray(this)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item_bird, commonNames)
        autoComplete.setAdapter(adapter)
        
        var selectedScientificName: String? = null
        var selectedCommonName: String? = null
        
        // FunciÃƒÂ³n auxiliar para formatear el nombre cientÃƒÂ­fico
        fun formatScientificName(name: String): String {
            return name.replace("_", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
        }
        
        // Listener para actualizar el nombre cientÃƒÂ­fico
        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val capitalizedName = adapter.getItem(position)
            selectedCommonName = capitalizedName
            selectedScientificName = BirdLabelsManager.getScientificName(this, capitalizedName!!)
            
            txtScientific.visibility = View.VISIBLE
            txtScientific.text = "Nombre cientÃƒÂ­fico: ${selectedScientificName?.let { formatScientificName(it) } ?: "-"}"
            btnConfirm.isEnabled = true
        }
        
        // TambiÃƒÂ©n detectar cuando se escribe manualmente
        autoComplete.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString()?.trim() ?: ""
                val matchingName = commonNames.find { it.equals(text, ignoreCase = true) }
                
                if (matchingName != null) {
                    selectedCommonName = matchingName
                    selectedScientificName = BirdLabelsManager.getScientificName(this@MainActivity, matchingName)
                    txtScientific.visibility = View.VISIBLE
                    txtScientific.text = "Nombre cientÃƒÂ­fico: ${selectedScientificName?.let { formatScientificName(it) } ?: "-"}"
                    btnConfirm.isEnabled = true
                } else {
                    selectedScientificName = null
                    selectedCommonName = null
                    txtScientific.visibility = View.GONE
                    btnConfirm.isEnabled = false
                }
            }
        })
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            if (selectedScientificName != null && selectedCommonName != null) {
                onBirdSelected(selectedScientificName!!, selectedCommonName!!)
                dialog.dismiss()
            }
        }
        
        dialog.show()

        // Preseleccionar la predicciÃƒÂ³n del modelo si existe (buscar coincidencia exacta en adapter)
        lastPrediction?.displayName?.let { predictedCommonRaw ->
            val predictedCommon = predictedCommonRaw.trim()
            if (predictedCommon.isNotEmpty()) {
                // Buscar en la lista de nombres comunes (case-insensitive) y usar la entrada exacta del adapter
                val match = commonNames.find { it.equals(predictedCommon, ignoreCase = true) }
                if (match != null) {
                    autoComplete.setText(match, false)
                    selectedCommonName = match
                    selectedScientificName = lastPrediction?.scientificName ?: BirdLabelsManager.getScientificName(this, match)
                    txtScientific.visibility = View.VISIBLE
                    txtScientific.text = "Nombre cientÃƒÂ­fico: ${selectedScientificName?.let { formatScientificName(it) } ?: "-"}"
                    btnConfirm.isEnabled = true
                    // no desplegar las opciones automÃƒÂ¡ticamente; dejar el valor preseleccionado
                }
            }
        }
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
            Log.e("MainActivity", "Error al guardar observaciÃƒÂ³n: ${e.message}", e)
            showMessage("Error al guardar: ${e.message}")
        } finally {
            ignoreMatrixChanges = previousIgnore
            btnSave.isEnabled = lastPrediction != null
            pendingObservationCount = if (saveCompleted) 1 else individualCount
        }
    }

    // ==================== GestiÃƒÂ³n de ImÃƒÂ¡genes ====================

    private fun loadAndClassifyImage(uri: Uri, restored: Boolean = false) {
        val bitmap = imageProcessor.decodeBitmap(contentResolver, uri)
        if (bitmap == null) {
            if (!restored) {
                showMessage("No se pudo leer la imagen")
            }
            return
        }
        currentImageUri = uri
        displayImageAndClassify(bitmap)
    }

    private fun displayImageAndClassify(bitmap: Bitmap) {
        // Cancelar cualquier clasificaciÃƒÂ³n pendiente
        reclassifyJob?.cancel()
        pendingMatrixChange = false
        lastPrediction = null
        btnSave.isEnabled = false

        // Restablecer visibilidad inicial: mostrar texto base y ocultar encabezado
        resultHeader.visibility = View.GONE
        // Ocultar botÃƒÂ³n e hint de informaciÃƒÂ³n hasta tener una predicciÃƒÂ³n
        btnBirdInfo.visibility = View.GONE
        hideInfoHintImmediate()
        labelResultado?.visibility = View.GONE
        txtResult.visibility = View.GONE

        // Limpiar bitmap anterior
        originalBitmap?.recycle()
        originalBitmap = bitmap

        // Bloquear detecciÃƒÂ³n de cambios temporalmente
        ignoreMatrixChanges = true

        // Mostrar imagen - el visor no debe ajustar zoom automÃƒÂ¡ticamente
        photoView.setImageBitmap(bitmap)
        emptyState.visibility = View.GONE
        focusManager.showOverlay()

        // Esperar a que se renderice completamente
        photoView.post {
            focusManager.updateOverlay()

            // Esperar a que termine cualquier animaciÃƒÂ³n automÃƒÂ¡tica del visor
            photoView.postDelayed({
                // Reactivar detecciÃƒÂ³n
                ignoreMatrixChanges = false
                lastMatrixChangeTime = System.currentTimeMillis()

                // Primera clasificaciÃƒÂ³n
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

    // ==================== ClasificaciÃƒÂ³n ====================

    private fun scheduleReclassify() {
        // Cancelar job anterior
        reclassifyJob?.cancel()

        // Programar nueva clasificaciÃƒÂ³n con debounce inteligente
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

        // Bloquear listener durante TODO el proceso de clasificaciÃƒÂ³n
        withContext(Dispatchers.Main) {
            ignoreMatrixChanges = true
        }

        setLoading(true)

        try {
            // Recortar segÃƒÂºn el recuadro de enfoque
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
            Log.e("MainActivity", "Error en clasificaciÃƒÂ³n: ${e.message}", e)
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

        val topPrediction = result.topPrediction
        val isExpertMode = SettingsManager.isExpertMode(this@MainActivity)
        val meetsThreshold = topPrediction?.probability?.let { it >= CONF_THRESH } == true

        withContext(Dispatchers.Main) {
            val hasPrediction = topPrediction != null
            val showConfidentHeader = hasPrediction && (isExpertMode || meetsThreshold)
            val showLowConfidenceBanner = hasPrediction && !isExpertMode && !meetsThreshold
            val confidenceText = topPrediction?.confidencePercentage?.format(1)
            val thresholdPercent = (CONF_THRESH * 100f).format(0)

            lastPrediction = if (isExpertMode || meetsThreshold) topPrediction else null
            lastAnyPrediction = topPrediction
            btnSave.isEnabled = showConfidentHeader

            when {
                showConfidentHeader && topPrediction != null -> {
                    resultHeader.visibility = View.VISIBLE
                    txtBirdNameHeader.text = topPrediction.displayName
                    txtScientificNameHeader.visibility = View.VISIBLE
                    txtScientificNameHeader.text = getString(
                        R.string.observation_scientific_format,
                        topPrediction.scientificName
                    )
                    chipConfidence.visibility = View.VISIBLE
                    chipConfidence.text = getString(
                        R.string.result_confidence_value,
                        topPrediction.confidencePercentage.format(1)
                    )
                    if (isExpertMode && !meetsThreshold) {
                        txtPositiveMsg.visibility = View.VISIBLE
                        txtPositiveMsg.text = getString(
                            R.string.result_expert_low_confidence_message,
                            thresholdPercent
                        )
                    } else {
                        txtPositiveMsg.visibility = View.GONE
                    }
                    imgExpertWarning.visibility =
                        if (isExpertMode && !meetsThreshold) View.VISIBLE else View.GONE
                    labelResultado?.visibility = View.GONE
                    txtResult.visibility = View.GONE

                    val wasVisible = btnBirdInfo.visibility == View.VISIBLE
                    btnBirdInfo.visibility = View.VISIBLE
                    if (!wasVisible) {
                        showInfoHintOnce()
                    }
                }

                showLowConfidenceBanner && topPrediction != null -> {
                    resultHeader.visibility = View.VISIBLE
                    txtBirdNameHeader.text = getString(R.string.result_low_confidence_title)
                    txtScientificNameHeader.visibility = View.VISIBLE
                    txtScientificNameHeader.text =
                        getString(R.string.result_low_confidence_subtitle, thresholdPercent)
                    chipConfidence.visibility = View.VISIBLE
                    chipConfidence.text =
                        getString(R.string.result_confidence_value, confidenceText ?: "--")
                    txtPositiveMsg.visibility = View.VISIBLE
                    txtPositiveMsg.text =
                        getString(R.string.result_low_confidence_message, topPrediction.displayName)
                    imgExpertWarning.visibility = View.GONE
                    labelResultado?.visibility = View.GONE
                    txtResult.visibility = View.GONE
                    btnBirdInfo.visibility = View.GONE
                    hideInfoHintImmediate()
                }

                else -> {
                    resultHeader.visibility = View.GONE
                    imgExpertWarning.visibility = View.GONE
                    chipConfidence.visibility = View.GONE
                    txtPositiveMsg.visibility = View.GONE
                    txtScientificNameHeader.visibility = View.GONE
                    labelResultado?.visibility = View.VISIBLE
                    txtResult.visibility = View.VISIBLE
                    txtResult.text = getString(R.string.result_no_detection_message)
                    btnBirdInfo.visibility = View.GONE
                    hideInfoHintImmediate()
                }
            }
        }
    }

    private var infoHintJob: Job? = null

    private fun showInfoHintOnce() {
        infoHintJob?.cancel()
        infoHint.bringToFront()
        infoHint.translationZ = dp(8f)
        infoHint.visibility = View.VISIBLE
        // Colocar la pista levemente contraÃƒÂ­da hacia la izquierda y transparente
        infoHint.alpha = 0f
        // Al estar el ÃƒÂ­cono en la derecha, la pista sale hacia la izquierda
        infoHint.translationX = dp(16f)
        // Animar apariciÃƒÂ³n
        infoHint.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(220)
            .withEndAction {
                // Mantener visible un momento y ocultar
                infoHintJob = lifecycleScope.launch {
                    delay(1600)
                    hideInfoHintAnimated()
                }
            }
            .start()
    }

    private fun hideInfoHintAnimated() {
        if (infoHint.visibility != View.VISIBLE) return
        infoHint.animate()
            .alpha(0f)
            .translationX(dp(16f))
            .setDuration(180)
            .withEndAction {
                infoHint.visibility = View.GONE
            }
            .start()
    }

    private fun hideInfoHintImmediate() {
        infoHintJob?.cancel()
        infoHint.clearAnimation()
        infoHint.alpha = 0f
        infoHint.translationX = 0f
        infoHint.visibility = View.GONE
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density



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

    companion object {
        private const val KEY_CURRENT_IMAGE_URI = "current_image_uri"
    }
}


