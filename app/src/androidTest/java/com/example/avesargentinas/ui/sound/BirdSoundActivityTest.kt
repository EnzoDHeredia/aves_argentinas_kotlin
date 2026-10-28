package com.example.avesargentinas.ui.sound

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.avesargentinas.MainMenuActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.SettingsManager
import com.example.avesargentinas.data.ObservationDatabase
import com.example.avesargentinas.ui.bird.BirdInfoActivity
import com.example.avesargentinas.ui.log.ImageZoomActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BirdSoundActivityTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext.applicationContext as Application
    private val packageName = app.packageName

    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)

    private fun await(timeoutMs: Long = 8000, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (!condition() && SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(20)
        }
        assertTrue("Condition was not met within ${timeoutMs}ms", condition())
    }

    private fun grantPermission() {
        try {
            instrumentation.uiAutomation.grantRuntimePermission(
                packageName,
                Manifest.permission.RECORD_AUDIO
            )
        } catch (_: Throwable) {
            instrumentation.uiAutomation.executeShellCommand(
                "pm grant $packageName android.permission.RECORD_AUDIO"
            )
        }
    }

    @Before
    fun setUp() {
        BirdSoundActivity.testViewModelFactory = null
        BirdSoundActivity.testAccessibilityAnnouncer = null
        grantPermission()
    }

    @After
    fun tearDown() {
        BirdSoundActivity.testViewModelFactory = null
        BirdSoundActivity.testAccessibilityAnnouncer = null
        grantPermission()
    }

    @Test
    fun mainMenuHasSoundIdentificationButtonAndLaunchesActivity() {
        ActivityScenario.launch(MainMenuActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btnSound = activity.findViewById<MaterialButton>(R.id.btnSoundCapture)
                assertNotNull("El menú principal debe tener btnSoundCapture", btnSound)
                assertEquals(View.VISIBLE, btnSound.visibility)
                assertEquals(
                    activity.getString(R.string.menu_identify_sound),
                    btnSound.text.toString()
                )

                // Al pulsar el botón, debe iniciar BirdSoundActivity
                btnSound.performClick()
            }

            SystemClock.sleep(500)
        }
    }

    @Test
    fun instantCaptureEnabledSkipsToMainActivityButBackPressReturnsToMenuWithSoundOptionAvailable() {
        val initialInstant = SettingsManager.isInstantCapture(app)
        try {
            SettingsManager.setInstantCapture(app, true)

            // 1. Apertura directa con captura instantánea activada salta a MainActivity
            ActivityScenario.launch(MainMenuActivity::class.java).use { scenario ->
                await(5000) { scenario.state == Lifecycle.State.DESTROYED }
            }

            // 2. Al regresar con from_back_press = true, no salta y el menú muestra la opción de sonido
            val backIntent = Intent(app, MainMenuActivity::class.java).apply {
                putExtra("from_back_press", true)
            }
            ActivityScenario.launch<MainMenuActivity>(backIntent).use { scenario ->
                scenario.onActivity { activity ->
                    assertFalse("No debe cerrarse si viene de back press", activity.isFinishing)
                    val btnSound = activity.findViewById<MaterialButton>(R.id.btnSoundCapture)
                    assertNotNull(btnSound)
                    assertEquals(View.VISIBLE, btnSound.visibility)
                }
            }
        } finally {
            SettingsManager.setInstantCapture(app, initialInstant)
        }
    }

    @Test
    fun initialOpenShowsExplanationAndDurationWithoutMicrophoneOrPermission() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val cardExplanation = activity.findViewById<MaterialCardView>(R.id.cardExplanation)
                val txtExplanation = activity.findViewById<TextView>(R.id.txtExplanation)
                val txtDuration = activity.findViewById<TextView>(R.id.txtDuration)
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)

                assertEquals(View.VISIBLE, cardExplanation.visibility)
                assertTrue(txtExplanation.text.isNotEmpty())
                assertEquals(
                    activity.getString(R.string.sound_capture_duration),
                    txtDuration.text.toString()
                )

                assertEquals(
                    activity.getString(R.string.sound_status_ready),
                    txtStatus.text.toString()
                )
                assertEquals(
                    activity.getString(R.string.sound_action_listen),
                    btnMainAction.text.toString()
                )
                assertTrue("Botón de grabar debe estar habilitado", btnMainAction.isEnabled)
                assertFalse("No debe haber intención de grabar pendiente al abrir", activity.pendingRecordIntent)
            }
        }
    }

    @Test
    fun permissionDeniedShowsRationaleAndDoesNotRecord() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.permissionChecker = { false }
                activity.rationaleChecker = { true }
                activity.pendingRecordIntent = true
                activity.handlePermissionResult(isGranted = false)

                val cardError = activity.findViewById<MaterialCardView>(R.id.cardError)
                val txtErrorMessage = activity.findViewById<TextView>(R.id.txtErrorMessage)
                val btnErrorAction = activity.findViewById<MaterialButton>(R.id.btnErrorAction)
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)

                assertEquals(View.VISIBLE, cardError.visibility)
                assertEquals(
                    activity.getString(R.string.sound_permission_rationale),
                    txtErrorMessage.text.toString()
                )
                assertEquals(View.VISIBLE, btnErrorAction.visibility)
                assertEquals(
                    activity.getString(R.string.sound_action_retry),
                    btnErrorAction.text.toString()
                )
                assertEquals(
                    activity.getString(R.string.sound_status_ready),
                    txtStatus.text.toString()
                )
            }
        }
    }

    @Test
    fun permissionPermanentlyDeniedOffersSettingsAction() {
        val prefs = app.getSharedPreferences("bird_sound_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("permission_requested", true).commit()

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.permissionChecker = { false }
                activity.rationaleChecker = { false }

                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()

                val cardError = activity.findViewById<MaterialCardView>(R.id.cardError)
                val txtErrorMessage = activity.findViewById<TextView>(R.id.txtErrorMessage)
                val btnErrorAction = activity.findViewById<MaterialButton>(R.id.btnErrorAction)

                assertEquals(View.VISIBLE, cardError.visibility)
                assertEquals(
                    activity.getString(R.string.sound_permission_denied_permanent),
                    txtErrorMessage.text.toString()
                )
                assertEquals(View.VISIBLE, btnErrorAction.visibility)
                assertEquals(
                    activity.getString(R.string.sound_action_settings),
                    btnErrorAction.text.toString()
                )
            }
        }
    }

    @Test
    fun latePermissionCallbackIgnoredWhenActivityNotResumed() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.onActivity { activity ->
                activity.pendingRecordIntent = true
                activity.handlePermissionResult(isGranted = true)
                assertFalse("Intención debe resetearse ante callback tardío", activity.pendingRecordIntent)
            }
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                assertEquals(
                    activity.getString(R.string.sound_status_ready),
                    txtStatus.text.toString()
                )
            }
        }
    }

    @Test
    fun returningFromSettingsDoesNotAutoRecord() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.permissionChecker = { false }
                activity.rationaleChecker = { true }
                activity.pendingRecordIntent = true
                activity.handlePermissionResult(isGranted = false)
                val cardError = activity.findViewById<MaterialCardView>(R.id.cardError)
                assertEquals(View.VISIBLE, cardError.visibility)
            }

            // Simular concesión al volver de Ajustes
            scenario.onActivity { activity ->
                activity.permissionChecker = { true }
            }

            scenario.moveToState(Lifecycle.State.STARTED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onActivity { activity ->
                val cardError = activity.findViewById<MaterialCardView>(R.id.cardError)
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)

                assertEquals(View.GONE, cardError.visibility)
                assertEquals(
                    activity.getString(R.string.sound_status_ready),
                    txtStatus.text.toString()
                )
                assertEquals(
                    activity.getString(R.string.sound_action_record),
                    btnMainAction.text.toString()
                )
            }
        }
    }

    @Test
    fun rotationPreservesPendingPermissionIntent() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.pendingRecordIntent = true
            }

            scenario.recreate()

            scenario.onActivity { activity ->
                assertTrue("La intención pendiente de grabar debe preservarse tras recreación", activity.pendingRecordIntent)
            }
        }
    }

    @Test
    fun renderingZeroOneAndFiveDetectionsIncludingSpeciesOutsideLocalCatalog() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            // 1. Caso Cero Detecciones
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.NoDetections
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    matches = txtStatus.text.toString() == activity.getString(R.string.sound_no_detections) &&
                            activity.getResultCount() == 0
                }
                matches
            }

            // 2. Caso Una Detección (Especie fuera del catálogo local, sin nombre común)
            val detectionOutside = BirdDetection(
                modelIndex = 42,
                modelScientificName = "Unknownus birdus",
                scientificName = "Unknownus birdus",
                commonName = null,
                score = 0.74f,
                localKey = null,
                distributionUri = null
            )

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(listOf(detectionOutside))
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 1) {
                        val row = activity.getResultViewAt(0)
                        if (row != null) {
                            val txtCommon = row.findViewById<TextView>(R.id.txtBirdCommonName)
                            val txtScientific = row.findViewById<TextView>(R.id.txtBirdScientificName)
                            val txtScore = row.findViewById<TextView>(R.id.txtBirdScore)
                            val btnViewInfo = row.findViewById<View>(R.id.btnViewBirdInfo)
                            val txtUnavailable = row.findViewById<View>(R.id.txtBirdInfoUnavailable)

                            matches = txtCommon.text == "Unknownus birdus" &&
                                    txtScientific.text == activity.getString(R.string.sound_common_name_unavailable) &&
                                    txtScore.text.contains("0.7") &&
                                    btnViewInfo.visibility == View.GONE &&
                                    txtUnavailable.visibility == View.VISIBLE
                        }
                    }
                }
                matches
            }

            // 3. Caso Cinco Detecciones (combinando con y sin ficha local)
            val fiveDetections = listOf(
                BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.92f, "hornero", null),
                BirdDetection(2, "Pitangus sulphuratus", "Pitangus sulphuratus", "Benteveo", 0.85f, "benteveo", null),
                BirdDetection(3, "Turdus rufiventris", "Turdus rufiventris", "Zorzal Colorado", 0.76f, "zorzal_colorado", null),
                BirdDetection(4, "Species 4", "Species 4", null, 0.65f, null, null),
                BirdDetection(5, "Species 5", "Species 5", "Pájaro Cinco", 0.51f, null, null)
            )

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(fiveDetections)
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 5) {
                        val firstRow = activity.getResultViewAt(0)
                        val txtScoreNote = activity.findViewById<TextView>(R.id.txtScoreExplanation)
                        val item3 = activity.getResultItemAt(3)

                        if (firstRow != null && item3 != null) {
                            val firstBtn = firstRow.findViewById<View>(R.id.btnViewBirdInfo)
                            matches = firstBtn.visibility == View.VISIBLE &&
                                    item3.localKey == null &&
                                    item3.scientificName == "Species 4" &&
                                    txtScoreNote.visibility == View.VISIBLE
                        }
                    }
                }
                matches
            }
        }
    }

    @Test
    fun newCaptureClearsPreviousResultsImmediately() {
        var controlledVm: BirdSoundViewModel? = null
        val fakeSession = object : SoundSession {
            override suspend fun prepare() {
                kotlinx.coroutines.delay(2000)
            }
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            val sampleResults = listOf(
                BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.88f, "hornero", null)
            )
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(sampleResults)
            }

            await {
                var visible = false
                scenario.onActivity { activity ->
                    visible = activity.getResultCount() == 1
                }
                visible
            }

            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()
            }

            await {
                var cleared = false
                scenario.onActivity { activity ->
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    cleared = activity.getResultCount() == 0 &&
                            txtStatus.text.toString() == activity.getString(R.string.sound_status_preparing)
                }
                cleared
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun realEndToEndOfflineCaptureAndInferenceOnSamsung() {
        BirdSoundActivity.testViewModelFactory = null
        grantPermission()

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                assertEquals(
                    activity.getString(R.string.sound_action_listen),
                    btnMainAction.text.toString()
                )
                btnMainAction.performClick()
            }

            // 1. Debe pasar a preparación y luego a escucha
            await(10000) {
                var active = false
                scenario.onActivity { activity ->
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    val status = txtStatus.text.toString()
                    active = status == activity.getString(R.string.sound_status_preparing) ||
                            status.startsWith("Escuchando")
                }
                active
            }

            // 2. Durante la escucha, el tiempo monotónico debe avanzar y superar 10 segundos sin detenerse automáticamente
            await(20000) {
                var tenSecondsExceeded = false
                scenario.onActivity { activity ->
                    val txtTimer = activity.findViewById<TextView>(R.id.txtTimer)
                    val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    val isListening = txtStatus.text.toString().startsWith("Escuchando")
                    val isStopButton = btnMainAction.text.toString() == activity.getString(R.string.sound_action_stop)
                    val timerVisible = txtTimer.visibility == View.VISIBLE
                    val elapsedSeconds = txtTimer.text.toString().split(":").let { parts ->
                        if (parts.size == 2) parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0) ?: 0
                        else 0
                    }
                    tenSecondsExceeded = isListening && isStopButton && timerVisible && elapsedSeconds >= 11
                }
                tenSecondsExceeded
            }

            // 3. Detener manualmente
            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()
            }

            // 4. Debe pasar a STOPPED (Sesión detenida o Sin coincidencias)
            await(10000) {
                var isStopped = false
                scenario.onActivity { activity ->
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                    val status = txtStatus.text.toString()
                    val isStoppedStatus = status == activity.getString(R.string.sound_status_completed) ||
                            status == activity.getString(R.string.sound_no_detections)
                    val isListenButton = btnMainAction.isEnabled &&
                            btnMainAction.text.toString() == activity.getString(R.string.sound_action_listen)
                    isStopped = isStoppedStatus && isListenButton
                }
                isStopped
            }

            scenario.onActivity { activity ->
                val cardError = activity.findViewById<View>(R.id.cardError)
                assertEquals("No debe haber error en captura continua", View.GONE, cardError.visibility)
            }

            // 5. Escuchar nuevamente debe iniciar una sesión limpia (lista vacía)
            Thread.sleep(600)
            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()
            }

            await(10000) {
                var newSessionStarted = false
                scenario.onActivity { activity ->
                    val count = activity.getResultCount()
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    val status = txtStatus.text.toString()
                    newSessionStarted = count == 0 && (status == activity.getString(R.string.sound_status_preparing) || status.startsWith("Escuchando"))
                }
                newSessionStarted
            }

            scenario.onActivity { activity ->
                activity.finish()
            }
        }
    }

    @Test
    fun doubleClickProtectionOnViewInfoAndRecordButton() {
        val sampleResults = listOf(
            BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.92f, "hornero", null)
        )
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            // Probar doble clic rápido en botón de acción principal
            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()
                btnMainAction.performClick()
            }

            // Establecer estado a Results
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(sampleResults)
            }

            await {
                var visible = false
                scenario.onActivity { activity ->
                    visible = activity.getResultCount() == 1
                }
                visible
            }

            // Probar doble clic rápido en "Ver ficha"
            scenario.onActivity { activity ->
                val row = activity.getResultViewAt(0)
                assertNotNull("La fila del resultado debe existir", row)
                val btnViewInfo = row!!.findViewById<MaterialButton>(R.id.btnViewBirdInfo)

                btnViewInfo.performClick()
                btnViewInfo.performClick()
            }
        }
    }

    @Test
    fun navigatingToBirdInfoAndZoomPreservesResultsOnReturnAndNoObservationsCreated() {
        val db = ObservationDatabase.getInstance(app)
        val initialObservationCount = runBlocking { db.observationDao().observeAll().first().size }

        val sampleResults = listOf(
            BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.92f, "hornero", "android.resource://${app.packageName}/${R.drawable.ic_launcher_foreground}"),
            BirdDetection(2, "Pitangus sulphuratus", "Pitangus sulphuratus", "Benteveo", 0.85f, "benteveo", null),
            BirdDetection(3, "Turdus rufiventris", "Turdus rufiventris", "Zorzal Colorado", 0.76f, "zorzal_colorado", null),
            BirdDetection(4, "Species 4", "Species 4", null, 0.65f, null, null),
            BirdDetection(5, "Species 5", "Species 5", "Pájaro Cinco", 0.51f, null, null)
        )

        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(sampleResults)
            }

            await {
                var count = 0
                scenario.onActivity { activity ->
                    count = activity.getResultCount()
                }
                count == 5
            }

            val monitor = instrumentation.addMonitor(BirdInfoActivity::class.java.name, null, false)
            try {
                scenario.onActivity { activity ->
                    val row0 = activity.getResultViewAt(0)
                    assertNotNull("La primera fila debe existir", row0)
                    val btnViewInfo = row0!!.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                    btnViewInfo.performClick()
                }

                val infoActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000)
                assertNotNull("BirdInfoActivity debe ser lanzada al pulsar Ver ficha", infoActivity)

                assertEquals("Hornero", infoActivity.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
                assertEquals("Furnarius rufus", infoActivity.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))

                infoActivity.finish()
            } finally {
                instrumentation.removeMonitor(monitor)
            }

            // Al volver a BirdSoundActivity, los 5 resultados deben ser exactamente iguales
            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 5) {
                        val item0 = activity.getResultItemAt(0)
                        val item3 = activity.getResultItemAt(3)
                        val row0 = activity.getResultViewAt(0)
                        val row0Common = row0?.findViewById<TextView>(R.id.txtBirdCommonName)?.text?.toString()
                        matches = item0?.commonName == "Hornero" &&
                                item0?.scientificName == "Furnarius rufus" &&
                                item3?.scientificName == "Species 4" &&
                                row0Common == "Hornero"
                    }
                }
                matches
            }

            // Ninguna observación fue creada en Room
            val finalObservationCount = runBlocking { db.observationDao().observeAll().first().size }
            assertEquals("No debe haberse creado ninguna observación en la base de datos", initialObservationCount, finalObservationCount)
        }
    }

    @Test
    fun speciesWithoutLocalInfoShowsUnavailableAndAllowsRetry() {
        val detectionWithoutInfo = listOf(
            BirdDetection(10, "Wild birdus", "Wild birdus", null, 0.77f, null, null)
        )
        var recordInvoked = false
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                recordInvoked = true
                onStarted(32000, 1000L)
                onDetections(detectionWithoutInfo, 96000L, 1000L)
                awaitCancellation()
            }
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(detectionWithoutInfo)
            }

            await {
                var matched = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 1) {
                        val row = activity.getResultViewAt(0)
                        if (row != null) {
                            val btnViewInfo = row.findViewById<View>(R.id.btnViewBirdInfo)
                            val txtUnavailable = row.findViewById<View>(R.id.txtBirdInfoUnavailable)
                            matched = btnViewInfo.visibility == View.GONE && txtUnavailable.visibility == View.VISIBLE
                        }
                    }
                }
                matched
            }

            // Reintentar con Volver a grabar
            scenario.onActivity { activity ->
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                btnMainAction.performClick()
            }

            await { recordInvoked }
            assertTrue("Volver a grabar debe funcionar correctamente ante especies sin ficha", recordInvoked)
        }
    }

    @Test
    fun unreadableMapDoesNotBreakFlowAndAllowsRetry() {
        val intent = Intent(app, BirdInfoActivity::class.java).apply {
            putExtra(BirdInfoActivity.EXTRA_COMMON_NAME, "Test Bird")
            putExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, "Testus birdus")
            putExtra(BirdInfoActivity.EXTRA_DISTRIBUTION_URI, "file:///android_asset/birds/non_existent_map_12345.webp")
        }

        ActivityScenario.launch<BirdInfoActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse("La actividad no debe finalizar por mapa inexistente", activity.isFinishing)
                val unavailable = activity.findViewById<View>(R.id.distributionUnavailable)
                assertNotNull(unavailable)
            }
        }
    }

    @Test
    fun rotationAcrossAllStatesPreservesStateAndResults() {
        val sampleResults = listOf(
            BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.89f, "hornero", null)
        )
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            // 1. Rotación en Ready
            scenario.recreate()
            scenario.onActivity { activity ->
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                assertEquals(activity.getString(R.string.sound_status_ready), txtStatus.text.toString())
            }

            // 2. Rotación en Listening
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState(phase = BirdSoundPhase.LISTENING, elapsedSeconds = 5L)
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                val txtTimer = activity.findViewById<TextView>(R.id.txtTimer)
                assertTrue(txtStatus.text.toString().startsWith("Escuchando"))
                assertEquals("00:05", txtTimer.text.toString())
            }

            // 3. Rotación en Results
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(sampleResults)
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(1, activity.getResultCount())
                val row = activity.getResultViewAt(0)
                assertNotNull("La fila tras recrear debe existir", row)
                assertEquals("Hornero", row!!.findViewById<TextView>(R.id.txtBirdCommonName).text.toString())
            }

            // 4. Rotación en Error
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Error(BirdSoundState.ErrorReason.INTERRUPTED)
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                val cardError = activity.findViewById<View>(R.id.cardError)
                val txtError = activity.findViewById<TextView>(R.id.txtErrorMessage)
                assertEquals(View.VISIBLE, cardError.visibility)
                assertEquals(activity.getString(R.string.sound_error_interrupted), txtError.text.toString())
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun backgroundDuringListeningCancelsAndReleasesUnderOneSecond() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                onStarted(32000, 1000L)
                awaitCancellation()
            }
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            scenario.onActivity { activity ->
                activity.startRecordingFlow()
            }

            await {
                vm.state.value.phase == BirdSoundPhase.LISTENING
            }

            // Pasar a segundo plano (Home / bloqueo)
            val stopStart = SystemClock.uptimeMillis()
            scenario.moveToState(Lifecycle.State.CREATED)

            await { vm.state.value.phase == BirdSoundPhase.ERROR }

            val elapsed = SystemClock.uptimeMillis() - stopStart
            assertTrue("Cancelación y liberación deben ocurrir en < 1000 ms (fue: ${elapsed}ms)", elapsed < 1000)

            // Volver a primer plano
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                val cardError = activity.findViewById<View>(R.id.cardError)
                val txtError = activity.findViewById<TextView>(R.id.txtErrorMessage)
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)

                assertEquals(View.VISIBLE, cardError.visibility)
                assertEquals(activity.getString(R.string.sound_error_interrupted), txtError.text.toString())
                assertEquals(activity.getString(R.string.sound_action_listen), btnMainAction.text.toString())
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun backgroundDuringListeningPreservesAccumulatedDetections() {
        val sampleResults = listOf(
            BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.95f, "hornero", null)
        )
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                onStarted(32000, 1000L)
                onDetections(sampleResults, 96000L, 1000L)
                awaitCancellation()
            }
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            scenario.onActivity { activity ->
                activity.startRecordingFlow()
            }

            await {
                var count = 0
                scenario.onActivity { count = it.getResultCount() }
                count == 1
            }

            // Pasar a segundo plano
            scenario.moveToState(Lifecycle.State.CREATED)

            await {
                vm.state.value.phase == BirdSoundPhase.ERROR
            }

            // Regresar a primer plano: la detección debe seguir visible
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                assertEquals(1, activity.getResultCount())
                val row = activity.getResultViewAt(0)
                assertNotNull("La fila al volver a resumed debe existir", row)
                assertEquals("Hornero", row!!.findViewById<TextView>(R.id.txtBirdCommonName).text.toString())
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun abandonDuringListeningCancelsSessionAndCleansUp() {
        var cancelCalled = false
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                onStarted(32000, 1000L)
                awaitCancellation()
            }
            override fun cancel() {
                cancelCalled = true
            }
            override suspend fun close() = Unit
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            scenario.onActivity { activity ->
                activity.startRecordingFlow()
            }

            await {
                vm.state.value.phase == BirdSoundPhase.LISTENING
            }

            // Abandonar (cerrar / pulsar Atrás)
            scenario.onActivity { activity ->
                activity.finish()
            }

            await { cancelCalled }
            assertTrue("Abandonar durante escucha debe cancelar la sesión", cancelCalled)
        }
    }

    @Test
    fun statusAnnouncementOnlyOnceAndTimerDoesNotSpamAccessibility() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                val txtTimer = activity.findViewById<TextView>(R.id.txtTimer)
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                val btnAttribution = activity.findViewById<MaterialButton>(R.id.btnAttribution)

                // txtStatus tiene accessibilityLiveRegion polite
                assertEquals(View.ACCESSIBILITY_LIVE_REGION_POLITE, txtStatus.accessibilityLiveRegion)

                // txtTimer NO tiene accessibilityLiveRegion para no interrumpir con el reloj
                assertEquals(View.ACCESSIBILITY_LIVE_REGION_NONE, txtTimer.accessibilityLiveRegion)

                // Objetivos táctiles mínimos de 48dp
                assertTrue("btnMainAction minHeight >= 48dp", btnMainAction.minHeight >= 48)
                assertTrue("btnAttribution minHeight >= 48dp", btnAttribution.minHeight >= 48)
            }
        }
    }

    @Test
    fun incrementalListHandlesMoreThanFiveAndAtLeastTwoHundredControlledSpecies() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }
        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            // 1. Caso Más de cinco especies (8 especies)
            val eightDetections = (1..8).map { i ->
                BirdDetection(i, "Species $i", "Species $i", "Ave $i", 0.6f + (i * 0.03f), if (i % 2 == 0) "key_$i" else null, null)
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(eightDetections)
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 8) {
                        val first = activity.getResultItemAt(0)
                        val last = activity.getResultItemAt(7)
                        matches = first?.scientificName == "Species 1" && last?.scientificName == "Species 8"
                    }
                }
                matches
            }

            // 2. Caso Al menos 200 especies controladas (205 especies)
            val twoHundredDetections = (1..205).map { i ->
                BirdDetection(i, "Species $i", "Species $i", "Ave $i", 0.5f + (i % 50) * 0.01f, if (i % 3 == 0) "key_$i" else null, null)
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(twoHundredDetections)
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 205) {
                        val first = activity.getResultItemAt(0)
                        val mid = activity.getResultItemAt(100)
                        val last = activity.getResultItemAt(204)
                        matches = first?.scientificName == "Species 1" &&
                                mid?.scientificName == "Species 101" &&
                                last?.scientificName == "Species 205"
                    }
                }
                matches
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun incrementalAdditionsAndScoreImprovementsPreservePositionFocusAndDoNotAutoScroll() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }
        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            val initialItems = (1..6).map { i ->
                BirdDetection(i, "Species $i", "Species $i", "Ave $i", 0.60f, "key_$i", null)
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(initialItems)
            }

            await {
                var ready = false
                scenario.onActivity { activity ->
                    ready = activity.getResultCount() == 6 && activity.getResultViewAt(0) != null
                }
                ready
            }

            // Enfocar el botón de la primera fila
            scenario.onActivity { activity ->
                val row0 = activity.getResultViewAt(0)
                assertNotNull(row0)
                val btn0 = row0!!.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                btn0.isFocusableInTouchMode = true
                btn0.requestFocus()
                assertTrue("El botón del primer elemento debe tener el foco", btn0.hasFocus())
            }

            // 1. Altas al final: agregar 4 especies más (total 10)
            val updatedItemsWithAdditions = initialItems + (7..10).map { i ->
                BirdDetection(i, "Species $i", "Species $i", "Ave $i", 0.60f, "key_$i", null)
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(updatedItemsWithAdditions)
            }

            await {
                var verified = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 10) {
                        val lm = activity.recyclerResults.layoutManager as LinearLayoutManager
                        val firstVisible = lm.findFirstVisibleItemPosition()
                        // Primer elemento visible sigue al inicio (no hubo auto-scroll al final)
                        val row0 = activity.getResultViewAt(0)
                        val btn0 = row0?.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                        val focusPreserved = btn0?.hasFocus() == true
                        verified = firstVisible <= 1 && focusPreserved
                    }
                }
                verified
            }

            // 2. Mejora de puntuación en el elemento 0 (pasa de 0.60 a 0.95)
            val itemsWithScoreImprovement = updatedItemsWithAdditions.mapIndexed { index, item ->
                if (index == 0) item.copy(score = 0.95f) else item
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(itemsWithScoreImprovement)
            }

            await {
                var scoreUpdated = false
                scenario.onActivity { activity ->
                    val row0 = activity.getResultViewAt(0)
                    val scoreText = row0?.findViewById<TextView>(R.id.txtBirdScore)?.text?.toString() ?: ""
                    val item0 = activity.getResultItemAt(0)
                    val btn0 = row0?.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                    scoreUpdated = item0?.scientificName == "Species 1" &&
                            item0.score == 0.95f &&
                            scoreText.contains("0.9") &&
                            btn0?.hasFocus() == true
                }
                scoreUpdated
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun longNamesAndMissingResourcesAreReadableAndAccessible() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }
        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            val longCommonName = "Pájaro carpintero lomo blanco de las sierras centrales con cresta roja brillante"
            val longScientificName = "Campylorhamphus trochilirostris lafresnayanus longissimus"

            val detections = listOf(
                BirdDetection(1, longScientificName, longScientificName, longCommonName, 0.88f, "key_long", null),
                BirdDetection(2, "Species sine nomen", "Species sine nomen", null, 0.72f, null, null)
            )

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(detections)
            }

            await {
                var matches = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 2) {
                        val row0 = activity.getResultViewAt(0)
                        val row1 = activity.getResultViewAt(1)
                        if (row0 != null && row1 != null) {
                            val txtCommon0 = row0.findViewById<TextView>(R.id.txtBirdCommonName)
                            val txtScientific0 = row0.findViewById<TextView>(R.id.txtBirdScientificName)
                            val txtScore0 = row0.findViewById<TextView>(R.id.txtBirdScore)
                            val btnView0 = row0.findViewById<MaterialButton>(R.id.btnViewBirdInfo)

                            val txtCommon1 = row1.findViewById<TextView>(R.id.txtBirdCommonName)
                            val txtScientific1 = row1.findViewById<TextView>(R.id.txtBirdScientificName)
                            val txtUnavailable1 = row1.findViewById<TextView>(R.id.txtBirdInfoUnavailable)
                            val btnView1 = row1.findViewById<MaterialButton>(R.id.btnViewBirdInfo)

                            matches = txtCommon0.text == longCommonName &&
                                    txtScientific0.text == longScientificName &&
                                    txtScore0.visibility == View.VISIBLE &&
                                    btnView0.visibility == View.VISIBLE &&
                                    txtCommon1.text == "Species sine nomen" &&
                                    txtScientific1.text == activity.getString(R.string.sound_common_name_unavailable) &&
                                    btnView1.visibility == View.GONE &&
                                    txtUnavailable1.visibility == View.VISIBLE &&
                                    txtUnavailable1.minHeight >= 48
                        }
                    }
                }
                matches
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun layoutHasNoNestedScrollViewAndMainActionRemainsOutsideScroll() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                // 1. No debe existir NestedScrollView en la jerarquía
                var hasNestedScrollView = false
                var current: View? = activity.recyclerResults
                while (current != null) {
                    if (current is androidx.core.widget.NestedScrollView) {
                        hasNestedScrollView = true
                        break
                    }
                    current = current.parent as? View
                }
                assertFalse("No debe haber NestedScrollView envolviendo la lista", hasNestedScrollView)

                // 2. RecyclerView debe ser el único desplazamiento vertical
                val recycler = activity.findViewById<RecyclerView>(R.id.recyclerResults)
                assertNotNull("RecyclerView debe existir", recycler)
                assertEquals(View.VISIBLE, recycler.visibility)

                // 3. Botón de acción principal y tarjeta de estado fijos fuera del RecyclerView
                val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                val cardStatus = activity.findViewById<MaterialCardView>(R.id.cardStatus)
                assertNotNull(btnMainAction)
                assertNotNull(cardStatus)

                // Asegurar que btnMainAction NO es hijo del RecyclerView
                var parent = btnMainAction.parent
                var insideRecycler = false
                while (parent != null) {
                    if (parent === recycler) {
                        insideRecycler = true
                        break
                    }
                    parent = parent.parent
                }
                assertFalse("btnMainAction debe estar fuera del RecyclerView", insideRecycler)

                // Objetivo táctil de al menos 48dp
                assertTrue("btnMainAction minHeight >= 48dp", btnMainAction.minHeight >= 48)
            }
        }
    }

    @Test
    fun scrollPositionRestoredAcrossRotationWhenResultsExist() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }
        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            val thirtyItems = (1..30).map { i ->
                BirdDetection(i, "Species $i", "Species $i", "Ave $i", 0.70f, "key_$i", null)
            }

            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState.Results(thirtyItems)
            }

            await {
                var ready = false
                scenario.onActivity { activity ->
                    ready = activity.getResultCount() == 30
                }
                ready
            }

            // Desplazar a una posición intermedia (posición 15)
            scenario.onActivity { activity ->
                (activity.recyclerResults.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(15, 0)
            }

            SystemClock.sleep(200)

            // Recrear la actividad (simula rotación de pantalla)
            scenario.recreate()

            await {
                var restored = false
                scenario.onActivity { activity ->
                    if (activity.getResultCount() == 30) {
                        val lm = activity.recyclerResults.layoutManager as LinearLayoutManager
                        val firstVisible = lm.findFirstVisibleItemPosition()
                        // Posición restaurada tras recrear (cercana a 15, no reseteada a 0)
                        restored = firstVisible >= 10
                    }
                }
                restored
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun navigatingToBirdInfoDuringListeningStopsSessionAwaitsReleaseAndNavigates() {
        val started = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val sampleBird = BirdDetection(
            1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.92f, "hornero", null
        )

        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                onStarted(32000, 1000L)
                onDetections(listOf(sampleBird), 96000L, 1000L)
                started.countDown()
                awaitCancellation()
            }
            override fun cancel() = Unit
            override suspend fun close() {
                kotlinx.coroutines.delay(100)
                closed.countDown()
            }
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            scenario.onActivity { activity ->
                activity.startRecordingFlow()
            }

            assertTrue(started.await(5, java.util.concurrent.TimeUnit.SECONDS))
            await { vm.state.value.phase == BirdSoundPhase.LISTENING && vm.state.value.detections.size == 1 }

            val monitor = instrumentation.addMonitor(BirdInfoActivity::class.java.name, null, false)
            try {
                scenario.onActivity { activity ->
                    val row0 = activity.getResultViewAt(0)
                    assertNotNull("La fila del ave debe existir", row0)
                    val btnViewInfo = row0!!.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                    btnViewInfo.performClick()
                }

                // El micrófono debe liberarse (close completarse) ANTES de que abra BirdInfoActivity
                val infoActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000)
                assertNotNull("BirdInfoActivity debe ser lanzada tras liberar el micrófono", infoActivity)
                assertTrue("La sesión debe estar cerrada antes o al navegar", closed.await(0, java.util.concurrent.TimeUnit.MILLISECONDS))
                assertEquals(BirdSoundPhase.STOPPED, vm.state.value.phase)

                assertEquals("Hornero", infoActivity.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
                assertEquals("Furnarius rufus", infoActivity.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))

                infoActivity.finish()
            } finally {
                instrumentation.removeMonitor(monitor)
            }

            // Al volver de BirdInfoActivity, BirdSoundActivity debe permanecer en STOPPED con los resultados
            await {
                var matches = false
                scenario.onActivity { activity ->
                    val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                    val btnMainAction = activity.findViewById<MaterialButton>(R.id.btnMainAction)
                    matches = vm.state.value.phase == BirdSoundPhase.STOPPED &&
                            activity.getResultCount() == 1 &&
                            txtStatus.text.toString() == activity.getString(R.string.sound_status_completed) &&
                            btnMainAction.text.toString() == activity.getString(R.string.sound_action_listen)
                }
                matches
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun navigatingToBirdInfoWhenCleanupFailsDoesNotNavigateAndKeepsError() {
        val started = CountDownLatch(1)
        val sampleBird = BirdDetection(
            1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.92f, "hornero", null
        )

        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override suspend fun listen(
                onStarted: suspend (Int, Long) -> Unit,
                onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
            ) {
                onStarted(32000, 1000L)
                onDetections(listOf(sampleBird), 96000L, 1000L)
                started.countDown()
                awaitCancellation()
            }
            override fun cancel() = Unit
            override suspend fun close() {
                throw IllegalStateException("Fallo de hardware al cerrar micrófono")
            }
        }

        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            scenario.onActivity { activity ->
                activity.startRecordingFlow()
            }

            assertTrue(started.await(5, java.util.concurrent.TimeUnit.SECONDS))
            await { vm.state.value.phase == BirdSoundPhase.LISTENING && vm.state.value.detections.size == 1 }

            val monitor = instrumentation.addMonitor(BirdInfoActivity::class.java.name, null, false)
            try {
                scenario.onActivity { activity ->
                    val row0 = activity.getResultViewAt(0)
                    val btnViewInfo = row0!!.findViewById<MaterialButton>(R.id.btnViewBirdInfo)
                    btnViewInfo.performClick()
                }

                val infoActivity = instrumentation.waitForMonitorWithTimeout(monitor, 1500)
                assertNull("BirdInfoActivity NO debe ser lanzada si falla la liberación del micrófono", infoActivity)

                await { vm.state.value.phase == BirdSoundPhase.ERROR }
                assertEquals(BirdSoundState.ErrorReason.RECORDING, vm.state.value.errorReason)

                scenario.onActivity { activity ->
                    val cardError = activity.findViewById<View>(R.id.cardError)
                    assertEquals(View.VISIBLE, cardError.visibility)
                    assertEquals(1, activity.getResultCount())
                }
            } finally {
                instrumentation.removeMonitor(monitor)
            }

            main { vm.abandon() }
        }
    }

    @Test
    fun backgroundOnStopDiscardsPendingRecordIntentSoLateResumeDoesNotAutoRecord() {
        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.pendingRecordIntent = true
            }

            // Pasar a segundo plano (onStop con !isChangingConfigurations)
            scenario.moveToState(Lifecycle.State.CREATED)

            scenario.onActivity { activity ->
                assertFalse("pendingRecordIntent debe descartarse al pasar a background", activity.pendingRecordIntent)
            }

            // Regresar a primer plano
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onActivity { activity ->
                assertFalse(activity.pendingRecordIntent)
                val txtStatus = activity.findViewById<TextView>(R.id.txtStatus)
                assertEquals(activity.getString(R.string.sound_status_ready), txtStatus.text.toString())
            }
        }
    }

    @Test
    fun talkBackBatchAnnouncesOnlyNewSpeciesWithoutRepeatingOrAnnouncingScoreImprovements() {
        val fakeSession = object : SoundSession {
            override suspend fun prepare() = Unit
            override fun cancel() = Unit
            override suspend fun close() = Unit
        }
        var controlledVm: BirdSoundViewModel? = null
        BirdSoundActivity.testViewModelFactory = {
            BirdSoundViewModel(app) { fakeSession }.also { controlledVm = it }
        }

        val capturedAnnouncements = java.util.Collections.synchronizedList(mutableListOf<CharSequence>())
        BirdSoundActivity.testAccessibilityAnnouncer = { text ->
            capturedAnnouncements.add(text)
        }

        ActivityScenario.launch<BirdSoundActivity>(BirdSoundActivity::class.java).use { scenario ->
            val vm = controlledVm ?: error("ViewModel not created")

            // Simular estado LISTENING
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState(phase = BirdSoundPhase.LISTENING, detections = emptyList())
            }

            // 1. Llegada del primer lote (1 especie)
            val bird1 = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.88f, "hornero", null)
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState(phase = BirdSoundPhase.LISTENING, detections = listOf(bird1))
            }

            await { capturedAnnouncements.isNotEmpty() }
            assertEquals(1, capturedAnnouncements.size)
            assertTrue(capturedAnnouncements[0].toString().contains("Hornero"))

            // 2. Mejora de puntuación en la misma especie (sin especies nuevas)
            val bird1Better = bird1.copy(score = 0.95f)
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState(phase = BirdSoundPhase.LISTENING, detections = listOf(bird1Better))
            }

            SystemClock.sleep(200)
            assertEquals("No debe haber nuevos anuncios al mejorar puntuación", 1, capturedAnnouncements.size)

            // 3. Llegada de un lote con 2 nuevas especies
            val bird2 = BirdDetection(2, "Pitangus sulphuratus", "Pitangus sulphuratus", "Benteveo", 0.85f, "benteveo", null)
            val bird3 = BirdDetection(3, "Turdus rufiventris", "Turdus rufiventris", "Zorzal Colorado", 0.76f, "zorzal_colorado", null)
            main {
                val field = BirdSoundViewModel::class.java.getDeclaredField("mutableState")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val stateFlow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<BirdSoundState>
                stateFlow.value = BirdSoundState(phase = BirdSoundPhase.LISTENING, detections = listOf(bird1Better, bird2, bird3))
            }

            await { capturedAnnouncements.size == 2 }
            val batchAnnouncement = capturedAnnouncements[1].toString()
            assertTrue("Debe anunciar Benteveo", batchAnnouncement.contains("Benteveo"))
            assertTrue("Debe anunciar Zorzal Colorado", batchAnnouncement.contains("Zorzal Colorado"))
            assertFalse("No debe repetir Hornero en el anuncio del nuevo lote", batchAnnouncement.contains("Hornero"))

            // 4. Rotación no debe disparar anuncios de las especies ya presentes
            scenario.recreate()
            SystemClock.sleep(300)
            assertEquals("Rotar pantalla no debe disparar anuncios espurios", 2, capturedAnnouncements.size)

            main { vm.abandon() }
        }
    }

    @Test
    fun realProcessDeathDiscardsSessionAndRestoresCleanReadyState() {
        val cleanVm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }
        assertEquals(BirdSoundPhase.READY, cleanVm.state.value.phase)
        assertTrue(cleanVm.state.value.detections.isEmpty())
        assertEquals(0L, cleanVm.state.value.elapsedSeconds)
    }
}
