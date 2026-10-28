package com.example.avesargentinas.ui.sound

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.ui.bird.BirdInfoActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BirdSoundActivity : BaseActivity() {

    private lateinit var viewModel: BirdSoundViewModel

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardStatus: MaterialCardView
    private lateinit var imgStatusIcon: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var txtTimer: TextView
    private lateinit var progressBarIndeterminate: ProgressBar

    private lateinit var cardError: MaterialCardView
    private lateinit var txtErrorMessage: TextView
    private lateinit var btnErrorAction: MaterialButton

    private lateinit var btnMainAction: MaterialButton

    @VisibleForTesting
    internal lateinit var recyclerResults: RecyclerView

    @VisibleForTesting
    internal lateinit var resultAdapter: BirdSoundResultAdapter

    @VisibleForTesting
    internal lateinit var headerAdapter: HeaderAdapter

    @VisibleForTesting
    internal lateinit var footerAdapter: FooterAdapter

    private var savedRecyclerState: Parcelable? = null
    private var lastRenderedDetections: List<BirdDetection>? = null
    private var micPulseAnimator: ObjectAnimator? = null

    @VisibleForTesting
    internal var pendingRecordIntent = false

    @VisibleForTesting
    internal var permissionChecker: (Context) -> Boolean = { context ->
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @VisibleForTesting
    internal var rationaleChecker: (android.app.Activity) -> Boolean = { activity ->
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.RECORD_AUDIO
        )
    }

    @VisibleForTesting
    internal var accessibilityAnnouncer: (CharSequence) -> Unit = { text ->
        testAccessibilityAnnouncer?.invoke(text) ?: run {
            @Suppress("DEPRECATION")
            recyclerResults.announceForAccessibility(text)
        }
    }

    private var permissionRequestedBefore = false
    private var isShowingPermissionError = false
    private var isPermanentDenial = false
    private var isNavigatingToBirdInfo = false
    private var pendingNavigationDetection: BirdDetection? = null
    private var isRestoredFromRotation = false
    private var lastActionClickTime = 0L

    private fun isFastDoubleClick(): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastActionClickTime < 500L) {
            return true
        }
        lastActionClickTime = now
        return false
    }

    @VisibleForTesting
    internal val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird_sound)

        setupInsets()
        initializeViews()
        initializeViewModel()
        restoreInstanceState(savedInstanceState)
        setupActions()
        observeViewModelState()
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.insets_container)
        root.setTag(R.id.tag_insets_listener_attached, true)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.sound_toolbar)
        cardStatus = findViewById(R.id.cardStatus)
        imgStatusIcon = findViewById(R.id.imgStatusIcon)
        txtStatus = findViewById(R.id.txtStatus)
        txtTimer = findViewById(R.id.txtTimer)
        progressBarIndeterminate = findViewById(R.id.progressBarIndeterminate)

        cardError = findViewById(R.id.cardError)
        txtErrorMessage = findViewById(R.id.txtErrorMessage)
        btnErrorAction = findViewById(R.id.btnErrorAction)
        btnMainAction = findViewById(R.id.btnMainAction)

        recyclerResults = findViewById(R.id.recyclerResults)
        recyclerResults.layoutManager = LinearLayoutManager(this)
        (recyclerResults.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        headerAdapter = HeaderAdapter(this, recyclerResults)
        resultAdapter = BirdSoundResultAdapter { detection -> openBirdInfo(detection) }
        resultAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        footerAdapter = FooterAdapter(this, recyclerResults) { showAttributionDialog() }

        val concatAdapter = ConcatAdapter(headerAdapter, resultAdapter, footerAdapter)
        recyclerResults.adapter = concatAdapter

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        permissionRequestedBefore = prefs.getBoolean(KEY_PERMISSION_REQUESTED, false)
    }

    private fun initializeViewModel() {
        val factory = testViewModelFactory?.let { customFactory ->
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return customFactory() as T
                }
            }
        } ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)

        viewModel = ViewModelProvider(this, factory)[BirdSoundViewModel::class.java]
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isRestoredFromRotation = true
            pendingRecordIntent = savedInstanceState.getBoolean(KEY_PENDING_RECORD_INTENT, false)
            isShowingPermissionError = savedInstanceState.getBoolean(KEY_SHOWING_PERMISSION_ERROR, false)
            isPermanentDenial = savedInstanceState.getBoolean(KEY_PERMANENT_DENIAL, false)
            savedRecyclerState = BundleCompat.getParcelable(savedInstanceState, KEY_RECYCLER_STATE, Parcelable::class.java)
            if (isShowingPermissionError) {
                if (isPermanentDenial) {
                    showPermanentDenialUi()
                } else {
                    showRationaleUi()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PENDING_RECORD_INTENT, pendingRecordIntent)
        outState.putBoolean(KEY_SHOWING_PERMISSION_ERROR, isShowingPermissionError)
        outState.putBoolean(KEY_PERMANENT_DENIAL, isPermanentDenial)
        val layoutManager = recyclerResults.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            outState.putParcelable(KEY_RECYCLER_STATE, it.onSaveInstanceState())
        }
    }

    private fun setupActions() {
        toolbar.setNavigationOnClickListener {
            viewModel.abandon()
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.abandon()
                finish()
            }
        })

        btnMainAction.setOnClickListener {
            if (isFastDoubleClick()) return@setOnClickListener
            when (viewModel.state.value.phase) {
                BirdSoundPhase.LISTENING -> {
                    viewModel.stop()
                }
                BirdSoundPhase.PREPARING, BirdSoundPhase.STOPPING -> {
                    // Acción bloqueada durante preparación o detención
                }
                else -> {
                    startRecordingFlow()
                }
            }
        }
    }

    private fun showAttributionDialog() {
        MaterialAlertDialogBuilder(this, R.style.Theme_AvesArgentinas_Dialog)
            .setTitle(R.string.sound_attribution_dialog_title)
            .setMessage(R.string.sound_attribution_dialog_body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isNavigatingToBirdInfo = false
        pendingNavigationDetection = null

        // Si se otorgó el permiso mientras estábamos en Ajustes, limpiar el aviso pero NO autograbar
        val hasPermission = permissionChecker(this)

        if (hasPermission && isShowingPermissionError) {
            hideError()
            renderState(viewModel.state.value)
        }
    }

    override fun onStop() {
        super.onStop()
        stopMicPulse()
        if (!isChangingConfigurations) {
            pendingRecordIntent = false
            viewModel.background()
        }
    }

    override fun onDestroy() {
        stopMicPulse()
        if (isFinishing) {
            viewModel.abandon()
        }
        super.onDestroy()
    }

    fun startRecordingFlow() {
        if (pendingRecordIntent) return

        val hasPermission = permissionChecker(this)

        if (hasPermission) {
            hideError()
            startRecording()
            return
        }

        val shouldShowRationale = rationaleChecker(this)

        if (!shouldShowRationale && permissionRequestedBefore) {
            // Denegación permanente
            showPermanentDenialUi()
            return
        }

        pendingRecordIntent = true
        markPermissionRequested()
        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    @VisibleForTesting
    internal fun handlePermissionResult(isGranted: Boolean) {
        if (!pendingRecordIntent) {
            return // Ignorar si no había intención activa de grabar
        }
        pendingRecordIntent = false

        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            // Callback tardío recibido cuando la actividad no está en primer plano: no autograbar
            return
        }

        if (isGranted) {
            hideError()
            startRecording()
        } else {
            val shouldShowRationale = rationaleChecker(this)
            if (shouldShowRationale) {
                showRationaleUi()
            } else {
                showPermanentDenialUi()
            }
        }
    }

    private fun startRecording() {
        try {
            viewModel.start()
        } catch (e: SecurityException) {
            showRationaleUi()
        }
    }

    private fun markPermissionRequested() {
        permissionRequestedBefore = true
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_REQUESTED, true)
            .apply()
    }

    private fun showRationaleUi() {
        isShowingPermissionError = true
        isPermanentDenial = false
        cardError.visibility = View.VISIBLE
        txtErrorMessage.text = getString(R.string.sound_permission_rationale)
        btnErrorAction.visibility = View.VISIBLE
        btnErrorAction.text = getString(R.string.sound_action_retry)
        btnErrorAction.setOnClickListener {
            if (isFastDoubleClick()) return@setOnClickListener
            startRecordingFlow()
        }
    }

    private fun showPermanentDenialUi() {
        isShowingPermissionError = true
        isPermanentDenial = true
        cardError.visibility = View.VISIBLE
        txtErrorMessage.text = getString(R.string.sound_permission_denied_permanent)
        btnErrorAction.visibility = View.VISIBLE
        btnErrorAction.text = getString(R.string.sound_action_settings)
        btnErrorAction.setOnClickListener {
            if (isFastDoubleClick()) return@setOnClickListener
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this, "No se pudieron abrir los ajustes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideError() {
        isShowingPermissionError = false
        isPermanentDenial = false
        cardError.visibility = View.GONE
    }

    private fun updateStatusText(newText: CharSequence) {
        if (txtStatus.text != newText) {
            txtStatus.text = newText
        }
    }

    private fun animationsEnabled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f) > 0f
        }
    } catch (_: SecurityException) {
        false
    }

    private fun startMicPulse() {
        if (!animationsEnabled()) {
            stopMicPulse()
            return
        }
        if (micPulseAnimator?.isRunning == true) return
        stopMicPulse()
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.18f, 1.0f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.18f, 1.0f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.75f, 1.0f)
        micPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(imgStatusIcon, scaleX, scaleY, alpha).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopMicPulse() {
        micPulseAnimator?.cancel()
        micPulseAnimator = null
        imgStatusIcon.scaleX = 1.0f
        imgStatusIcon.scaleY = 1.0f
        imgStatusIcon.alpha = 1.0f
    }

    private fun clearResults() {
        savedRecyclerState = null
        headerAdapter.showResultsTitle = false
        footerAdapter.showScoreExplanation = false
        resultAdapter.submitList(emptyList())
        lastRenderedDetections = null
        recyclerResults.scrollToPosition(0)
    }

    private fun renderState(state: BirdSoundState) {
        progressBarIndeterminate.visibility = when (state.phase) {
            BirdSoundPhase.PREPARING, BirdSoundPhase.STOPPING -> View.VISIBLE
            else -> View.GONE
        }

        when (state.phase) {
            BirdSoundPhase.READY, BirdSoundPhase.PREPARING -> {
                txtTimer.visibility = View.GONE
            }
            BirdSoundPhase.LISTENING, BirdSoundPhase.STOPPING, BirdSoundPhase.STOPPED, BirdSoundPhase.ERROR -> {
                txtTimer.visibility = View.VISIBLE
                txtTimer.text = state.formattedTime
            }
        }

        if (state.phase == BirdSoundPhase.LISTENING) {
            startMicPulse()
        } else {
            stopMicPulse()
        }

        when (state.phase) {
            BirdSoundPhase.READY -> {
                if (!isShowingPermissionError) hideError()
                updateStatusText(getString(R.string.sound_status_ready))
                btnMainAction.isEnabled = true
                btnMainAction.text = getString(R.string.sound_action_listen)
                btnMainAction.setIconResource(R.drawable.ic_mic)
            }
            BirdSoundPhase.PREPARING -> {
                if (!isShowingPermissionError) hideError()
                updateStatusText(getString(R.string.sound_status_preparing))
                btnMainAction.isEnabled = false
                btnMainAction.text = getString(R.string.sound_action_listen)
                btnMainAction.setIconResource(R.drawable.ic_mic)
            }
            BirdSoundPhase.LISTENING -> {
                if (!isShowingPermissionError) hideError()
                val text = if (state.detections.isEmpty()) {
                    getString(R.string.sound_status_listening_waiting)
                } else {
                    getString(R.string.sound_status_listening)
                }
                updateStatusText(text)
                btnMainAction.isEnabled = true
                btnMainAction.text = getString(R.string.sound_action_stop)
                btnMainAction.setIconResource(0)
            }
            BirdSoundPhase.STOPPING -> {
                updateStatusText(getString(R.string.sound_status_stopping))
                btnMainAction.isEnabled = false
                btnMainAction.text = getString(R.string.sound_action_stop)
                btnMainAction.setIconResource(0)
            }
            BirdSoundPhase.STOPPED -> {
                if (!isShowingPermissionError) hideError()
                val text = if (state.detections.isEmpty()) {
                    getString(R.string.sound_no_detections)
                } else {
                    getString(R.string.sound_status_completed)
                }
                updateStatusText(text)
                btnMainAction.isEnabled = true
                btnMainAction.text = getString(R.string.sound_action_listen)
                btnMainAction.setIconResource(R.drawable.ic_mic)
            }
            BirdSoundPhase.ERROR -> {
                val errorMsg = when (state.errorReason) {
                    BirdSoundState.ErrorReason.INTERRUPTED -> getString(R.string.sound_error_interrupted)
                    BirdSoundState.ErrorReason.RECORDING -> getString(R.string.sound_error_recording)
                    BirdSoundState.ErrorReason.RECOGNITION, null -> getString(R.string.sound_error_recognition)
                }
                cardError.visibility = View.VISIBLE
                txtErrorMessage.text = errorMsg
                btnErrorAction.visibility = View.GONE

                if (state.detections.isEmpty()) {
                    updateStatusText(getString(R.string.sound_identification_title))
                } else {
                    updateStatusText(getString(R.string.sound_status_completed))
                }
                btnMainAction.isEnabled = true
                btnMainAction.text = getString(R.string.sound_action_listen)
                btnMainAction.setIconResource(R.drawable.ic_mic)
            }
        }

        renderResults(state.detections)
    }

    private fun renderResults(detections: List<BirdDetection>) {
        if (detections.isEmpty()) {
            savedRecyclerState = null
            headerAdapter.showResultsTitle = false
            footerAdapter.showScoreExplanation = false
            resultAdapter.submitList(emptyList())
            lastRenderedDetections = null
            isRestoredFromRotation = false
            return
        }

        headerAdapter.showResultsTitle = true
        footerAdapter.showScoreExplanation = true

        if (detections !== lastRenderedDetections) {
            val previousList = lastRenderedDetections ?: emptyList()
            lastRenderedDetections = detections
            resultAdapter.submitList(detections) {
                savedRecyclerState?.let { state ->
                    recyclerResults.layoutManager?.onRestoreInstanceState(state)
                    savedRecyclerState = null
                }
            }

            // TalkBack anuncia un lote nuevo sin repetir lista/reloj/mejoras
            if (!isRestoredFromRotation &&
                detections.size > previousList.size &&
                viewModel.state.value.phase == BirdSoundPhase.LISTENING
            ) {
                val newItems = detections.subList(previousList.size, detections.size)
                val names = newItems.joinToString(", ") { it.commonName ?: it.scientificName }
                val announcement = if (newItems.size == 1) {
                    getString(R.string.sound_new_detection_single, names)
                } else {
                    getString(R.string.sound_new_detections_batch, names)
                }
                accessibilityAnnouncer(announcement)
            }
            isRestoredFromRotation = false
        }
    }

    private fun openBirdInfo(detection: BirdDetection) {
        if (isNavigatingToBirdInfo || isFastDoubleClick()) return
        isNavigatingToBirdInfo = true
        pendingNavigationDetection = detection

        val currentPhase = viewModel.state.value.phase
        if (currentPhase == BirdSoundPhase.LISTENING || currentPhase == BirdSoundPhase.PREPARING) {
            // Congelar primero la sesión y esperar la liberación del micrófono antes de navegar
            lifecycleScope.launch {
                val finalState = viewModel.stopAndAwait()

                // Al completar, comprobar que la Activity sigue activa, que no cambió el intento
                // y que la especie pertenece a la lista congelada
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) || isFinishing || isDestroyed) {
                    isNavigatingToBirdInfo = false
                    pendingNavigationDetection = null
                    return@launch
                }

                if (pendingNavigationDetection != detection) {
                    isNavigatingToBirdInfo = false
                    return@launch
                }

                if (finalState.phase != BirdSoundPhase.STOPPED) {
                    // Si falla limpiar, conservar error/lista y no navegar dejando micrófono activo
                    isNavigatingToBirdInfo = false
                    pendingNavigationDetection = null
                    return@launch
                }

                val belongsToList = finalState.detections.any {
                    it.scientificName.trim().equals(detection.scientificName.trim(), ignoreCase = true)
                }
                if (!belongsToList) {
                    isNavigatingToBirdInfo = false
                    pendingNavigationDetection = null
                    return@launch
                }

                launchBirdInfoIntent(detection)
            }
        } else {
            // Desde Detenido (o READY / ERROR) se consulta directamente
            launchBirdInfoIntent(detection)
        }
    }

    private fun launchBirdInfoIntent(detection: BirdDetection) {
        runCatching {
            val intent = Intent(this, BirdInfoActivity::class.java).apply {
                putExtra(
                    BirdInfoActivity.EXTRA_COMMON_NAME,
                    detection.commonName ?: detection.scientificName
                )
                putExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, detection.scientificName)
                putExtra(BirdInfoActivity.EXTRA_CONFIDENCE, detection.score)
                detection.distributionUri?.let {
                    putExtra(BirdInfoActivity.EXTRA_DISTRIBUTION_URI, it)
                }
            }
            startActivity(intent)
        }.onFailure {
            isNavigatingToBirdInfo = false
            pendingNavigationDetection = null
            Toast.makeText(this, "No se pudo abrir la información del ave", Toast.LENGTH_SHORT).show()
        }
    }

    override fun <T : View> findViewById(id: Int): T {
        val view = super.findViewById<T>(id)
        if (view != null) return view
        if (::headerAdapter.isInitialized) {
            val inHeader = headerAdapter.currentView?.findViewById<T>(id)
            if (inHeader != null) return inHeader
        }
        if (::footerAdapter.isInitialized) {
            val inFooter = footerAdapter.currentView?.findViewById<T>(id)
            if (inFooter != null) return inFooter
        }
        if (::recyclerResults.isInitialized) {
            for (i in 0 until recyclerResults.childCount) {
                val child = recyclerResults.getChildAt(i)
                val match = child.findViewById<T>(id)
                if (match != null) return match
            }
        }
        return null as T
    }

    @VisibleForTesting
    internal fun getResultCount(): Int = if (::resultAdapter.isInitialized) resultAdapter.itemCount else 0

    @VisibleForTesting
    internal fun getResultItemAt(position: Int): BirdDetection? {
        return if (::resultAdapter.isInitialized && position in 0 until resultAdapter.itemCount) {
            resultAdapter.currentList[position]
        } else {
            null
        }
    }

    @VisibleForTesting
    internal fun getResultViewAt(position: Int): View? {
        if (!::recyclerResults.isInitialized || !::headerAdapter.isInitialized) return null
        val globalPos = headerAdapter.itemCount + position
        val existing = recyclerResults.findViewHolderForAdapterPosition(globalPos)?.itemView
            ?: recyclerResults.layoutManager?.findViewByPosition(globalPos)
        if (existing != null) return existing

        if (position in 0 until getResultCount()) {
            (recyclerResults.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(globalPos, 0)
        }
        return recyclerResults.findViewHolderForAdapterPosition(globalPos)?.itemView
            ?: recyclerResults.layoutManager?.findViewByPosition(globalPos)
    }

    internal class HeaderAdapter(context: Context, parent: ViewGroup) : RecyclerView.Adapter<HeaderAdapter.ViewHolder>() {
        var currentView: View? = LayoutInflater.from(context).inflate(R.layout.item_bird_sound_header, parent, false)

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cardExplanation: View = view.findViewById(R.id.cardExplanation)
            val txtExplanation: TextView = view.findViewById(R.id.txtExplanation)
            val txtDuration: TextView = view.findViewById(R.id.txtDuration)
            val txtResultsTitle: TextView = view.findViewById(R.id.txtResultsTitle)
        }

        var showResultsTitle: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    currentView?.findViewById<TextView>(R.id.txtResultsTitle)?.visibility =
                        if (value) View.VISIBLE else View.GONE
                    notifyItemChanged(0)
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = currentView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_bird_sound_header, parent, false).also { currentView = it }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            currentView = holder.view
            holder.txtResultsTitle.visibility = if (showResultsTitle) View.VISIBLE else View.GONE
        }

        override fun getItemCount(): Int = 1
    }

    internal class FooterAdapter(
        context: Context,
        parent: ViewGroup,
        private val onAttributionClick: () -> Unit
    ) : RecyclerView.Adapter<FooterAdapter.ViewHolder>() {
        var currentView: View? = LayoutInflater.from(context).inflate(R.layout.item_bird_sound_footer, parent, false)

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val txtScoreExplanation: TextView = view.findViewById(R.id.txtScoreExplanation)
            val btnAttribution: MaterialButton = view.findViewById(R.id.btnAttribution)
        }

        var showScoreExplanation: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    currentView?.findViewById<TextView>(R.id.txtScoreExplanation)?.visibility =
                        if (value) View.VISIBLE else View.GONE
                    notifyItemChanged(0)
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = currentView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_bird_sound_footer, parent, false).also { currentView = it }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            currentView = holder.view
            holder.txtScoreExplanation.visibility = if (showScoreExplanation) View.VISIBLE else View.GONE
            holder.btnAttribution.setOnClickListener { onAttributionClick() }
        }

        override fun getItemCount(): Int = 1
    }

    companion object {
        private const val PREFS_NAME = "bird_sound_prefs"
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
        private const val KEY_PENDING_RECORD_INTENT = "pending_record_intent"
        private const val KEY_SHOWING_PERMISSION_ERROR = "showing_permission_error"
        private const val KEY_PERMANENT_DENIAL = "permanent_denial"
        private const val KEY_RECYCLER_STATE = "key_recycler_state"

        @VisibleForTesting
        internal var testViewModelFactory: (() -> BirdSoundViewModel)? = null

        @VisibleForTesting
        internal var testAccessibilityAnnouncer: ((CharSequence) -> Unit)? = null
    }
}
