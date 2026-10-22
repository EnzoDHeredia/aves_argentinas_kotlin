package com.example.avesargentinas.ui.learning

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import com.example.avesargentinas.R
import com.example.avesargentinas.MainActivity
import com.example.avesargentinas.MainMenuActivity
import com.example.avesargentinas.SettingsActivity
import com.example.avesargentinas.SettingsManager
import com.example.avesargentinas.ui.log.ObservationLogActivity
import com.example.avesargentinas.ui.bird.BirdInfoActivity
import com.example.avesargentinas.ui.log.ImageZoomActivity
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.io.File
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatDelegate

@RunWith(AndroidJUnit4::class)
class LearningGameActivityTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var automationFlags = 0
    private val automation get() = instrumentation.getUiAutomation(automationFlags)

    @Test
    fun summaryPrioritizesTheAvailableActionForZeroOneAndSeveralMarkedCards() = animatorScale("0") {
        launch(questions(10)).use { scenario ->
            listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                scenario.onActivity { it.delegate.localNightMode = theme }
                await(scenario) { it.resources.configuration.uiMode and 48 == if (theme == 2) 32 else 16 }
                start(scenario, R.id.learning_questions)
                val chosen = state(scenario).cards
                listOf(LearningSummary(10, 7, 3), LearningSummary(3, 2, 1), LearningSummary(1, 1, 0)).forEach { summary ->
                    repeat(summary.total) { index ->
                        flip(scenario)
                        tap(scenario, if (index < summary.known) R.id.learning_known else R.id.learning_mark)
                    }
                    assertSummaryPresentation(scenario, summary)
                    if (summary.review > 0) {
                        touch(scenario, R.id.learning_review)
                        awaitCard(scenario)
                        assertEquals(chosen.takeLast(summary.review), state(scenario).cards)
                    }
                }
                // The same views must also restore the alternative style on a new round.
                touch(scenario, R.id.learning_again)
                awaitCard(scenario)
                assertEquals(10, state(scenario).total)
                assertTrue(state(scenario).assessments.isEmpty())
                repeat(10) { flip(scenario); tap(scenario, R.id.learning_mark) }
                assertSummaryPresentation(scenario, LearningSummary(10, 0, 10))
                touch(scenario, R.id.learning_choose_mode)
                assertEquals(LearningState(), state(scenario))
            }
        }
    }

    @Test
    fun photoSummariesKeepTheTenThreeOneReviewSequenceAndConsultation() = animatorScale("0") {
        launch().use { scenario ->
            start(scenario, R.id.learning_species)
            val chosen = state(scenario).cards
            listOf(LearningSummary(10, 7, 3), LearningSummary(3, 2, 1), LearningSummary(1, 1, 0)).forEach { expected ->
                if (state(scenario).pass == LearningPass.REVIEW) consultReviewPhoto(scenario, "t5-sequence-${expected.total}")
                repeat(expected.total) { index ->
                    if (state(scenario).face == CardFace.FRONT) flip(scenario)
                    scenario.onActivity {
                        assertTrue(it.findViewById<View>(R.id.learning_photo).isShown)
                        assertNotNull(it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable)
                    }
                    tap(scenario, if (index < expected.known) R.id.learning_known else R.id.learning_mark)
                }
                assertSummaryPresentation(scenario, expected)
                if (expected.review > 0) {
                    touch(scenario, R.id.learning_review)
                    awaitCard(scenario)
                    assertEquals(chosen.takeLast(expected.review), state(scenario).cards)
                }
            }
        }
    }

    @Test
    fun summaryAndReviewVisualMatrixCentersOrScrollsWithBothThemesAndDoubleText() {
        val oldFont = shell("settings get system font_scale").trim().takeUnless { it == "null" }
        val oldSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")?.trim()
        val rotation = listOf("accelerometer_rotation", "user_rotation")
            .associateWith { shell("settings get system $it").trim().takeUnless { value -> value == "null" } }
        val animations = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
            .associateWith { shell("settings get global $it").trim().takeUnless { value -> value == "null" } }
        val fixtures = listOf(
            LearningCard.Photo("bigua", "phalacrocorax_brasilianus", "Biguá", "Phalacrocorax brasilianus", "learning/photos/photo-001.webp"),
            LearningCard.Photo("carau", "aramus_guarauna", "Carau", "Aramus guarauna", "learning/photos/photo-002.webp"),
            LearningCard.Photo("chaja", "chauna_torquata", "Chajá", "Chauna torquata", "learning/photos/photo-003.webp"))
        val consultationFailures = mutableListOf<String>()
        try {
            animations.keys.forEach { shell("settings put global $it 0") }
            listOf("1.0", "2.0").forEach { font ->
                shell(if (font == "2.0") "wm size 720x1280" else if (oldSize == null) "wm size reset" else "wm size $oldSize")
                shell("settings put system font_scale $font")
                listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                    listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                        launch(fixtures).use { scenario ->
                            scenario.onActivity { it.delegate.localNightMode = theme; it.requestedOrientation = orientation }
                            await(scenario) { it.resources.configuration.fontScale == font.toFloat() &&
                                it.resources.configuration.uiMode and 48 == (if (theme == 2) 32 else 16) &&
                                it.resources.configuration.orientation == (if (orientation == 1) 1 else 2) }
                            start(scenario, R.id.learning_species)
                            repeat(3) { flip(scenario); tap(scenario, R.id.learning_mark) }
                            listOf(LearningSummary(3, 0, 3), LearningSummary(3, 2, 1), LearningSummary(1, 1, 0)).forEach { expected ->
                                val prefix = "t5-$font-$theme-$orientation-${expected.review}"
                                assertSummaryPresentation(scenario, expected)
                                await(scenario) { !it.findViewById<View>(R.id.learning_content).isLayoutRequested }
                                scenario.onActivity { activity ->
                                    val scroll = activity.findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll)
                                    scroll.scrollTo(0, 0)
                                    val block = activity.findViewById<View>(R.id.learning_content)
                                    val padding = activity.resources.getDimensionPixelSize(R.dimen.screen_padding_large)
                                    val viewport = IntArray(2).also { scroll.getLocationOnScreen(it) }
                                    val position = IntArray(2).also { block.getLocationOnScreen(it) }
                                    val available = scroll.height - 2 * padding
                                    val top = position[1] - viewport[1] - padding
                                    if (block.height <= available) assertTrue("Summary centered", kotlin.math.abs(available - block.height - 2 * top) <=
                                        2 * activity.resources.displayMetrics.density)
                                    else assertEquals("Overflow begins at top", 0, top)
                                }
                                capture(scenario, "$prefix-start")
                                listOf(R.id.learning_total, R.id.learning_known_total, R.id.learning_review_total,
                                    R.id.learning_again, R.id.learning_choose_mode, R.id.learning_exit).forEach { id ->
                                    reachable(scenario, id)
                                    scenario.onActivity { activity ->
                                        val text = activity.findViewById<TextView>(id)
                                        assertTrue(text.layout.height <= text.height - text.compoundPaddingTop - text.compoundPaddingBottom)
                                        repeat(text.layout.lineCount) { assertEquals(0, text.layout.getEllipsisCount(it)) }
                                        if (text is com.google.android.material.button.MaterialButton)
                                            assertTrue(text.height >= 48 * activity.resources.displayMetrics.density)
                                    }
                                    if (font == "2.0" && id in listOf(R.id.learning_review_total, R.id.learning_again))
                                        capture(scenario, "$prefix-${if (id == R.id.learning_again) "again" else "totals"}")
                                }
                                capture(scenario, "$prefix-end")
                                reachable(scenario, R.id.learning_total)
                                if (expected.review > 0) {
                                    reachable(scenario, R.id.learning_review)
                                    if (font == "2.0") capture(scenario, "$prefix-review-action")
                                    touch(scenario, R.id.learning_review)
                                    awaitCard(scenario)
                                    try { consultReviewPhoto(scenario, prefix) }
                                    catch (failure: AssertionError) {
                                        // Record every configuration before reporting a consultation failure.
                                        if (failure.cause !is AssertionError) throw failure
                                        consultationFailures.add("$prefix: ${failure.message}")
                                    }
                                    repeat(expected.review) { index ->
                                        if (state(scenario).face == CardFace.FRONT) flip(scenario)
                                        tap(scenario, if (expected.review == 1 || index < 2) R.id.learning_known else R.id.learning_mark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            assertTrue(consultationFailures.joinToString("\n"), consultationFailures.isEmpty())
        } finally {
            shell(if (oldSize == null) "wm size reset" else "wm size $oldSize")
            restoreSetting("system", "font_scale", oldFont)
            rotation.forEach { (key, value) -> restoreSetting("system", key, value) }
            animations.forEach { (key, value) -> restoreSetting("global", key, value) }
        }
    }

    private fun consultReviewPhoto(scenario: ActivityScenario<LearningGameActivity>, prefix: String) {
        assertEquals(LearningPass.REVIEW, state(scenario).pass)
        var image: android.graphics.drawable.Drawable? = null
        var theme = AppCompatDelegate.MODE_NIGHT_NO
        var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        scenario.onActivity {
            image = it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable
            theme = if (it.resources.configuration.uiMode and 48 == 32) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            orientation = if (it.resources.configuration.orientation == 2) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        flip(scenario)
        val before = state(scenario)
        val current = before.currentCard as LearningCard.Photo
        scenario.onActivity {
            val photo = it.findViewById<android.widget.ImageView>(R.id.learning_photo)
            assertTrue(photo.isShown)
            assertSame(image, photo.drawable)
            assertEquals(android.widget.ImageView.ScaleType.CENTER_CROP, photo.scaleType)
        }
        capture(scenario, "$prefix-review-answer")
        touch(scenario, R.id.learning_bird_info)
        val openedBird = foreground(BirdInfoActivity::class.java)
        instrumentation.runOnMainSync {
            openedBird.delegate.localNightMode = theme
            openedBird.requestedOrientation = orientation
        }
        try {
            waitUi {
                val currentBird = foregroundActivity() as? BirdInfoActivity
                currentBird != null && currentBird.resources.configuration.uiMode and 48 == (if (theme == 2) 32 else 16) &&
                    currentBird.resources.configuration.orientation == (if (orientation == 1) 1 else 2) &&
                    (currentBird.findViewById<View>(R.id.imageDistribution).isEnabled ||
                        currentBird.findViewById<View>(R.id.distributionUnavailable).isShown)
            }
        } catch (failure: AssertionError) {
            val currentBird = foreground(BirdInfoActivity::class.java)
            captureActivity(currentBird, "$prefix-review-failure")
            var diagnostic = ""
            instrumentation.runOnMainSync {
                val config = currentBird.resources.configuration
                val map = currentBird.findViewById<View>(R.id.imageDistribution)
                diagnostic = "Expected theme=$theme orientation=$orientation; actual uiMode=${config.uiMode and 48} " +
                    "orientation=${config.orientation} font=${config.fontScale}; map=${map.width}x${map.height} enabled=${map.isEnabled} " +
                    "unavailable=${currentBird.findViewById<View>(R.id.distributionUnavailable).isShown}"
            }
            back(currentBird)
            foreground(LearningGameActivity::class.java)
            awaitCard(scenario)
            assertEquals(before, state(scenario))
            capture(scenario, "$prefix-review-return")
            throw AssertionError(diagnostic, failure)
        }
        val bird = foreground(BirdInfoActivity::class.java)
        instrumentation.runOnMainSync {
            assertEquals(current.commonName, bird.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
            assertEquals(current.scientificName, bird.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))
        }
        captureActivity(bird, "$prefix-review-info")
        back(bird)
        foreground(LearningGameActivity::class.java)
        awaitCard(scenario)
        assertEquals(before, state(scenario))
        scenario.onActivity { assertSame(image, it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable) }
        reachable(scenario, R.id.learning_mark)
        capture(scenario, "$prefix-review-return")
    }

    private fun assertSummaryPresentation(scenario: ActivityScenario<LearningGameActivity>, expected: LearningSummary) {
        assertEquals(expected, state(scenario).summary)
        scenario.onActivity { activity ->
            listOf(R.id.learning_total to activity.getString(R.string.learning_total, expected.total),
                R.id.learning_known_total to activity.getString(R.string.learning_known_total, expected.known),
                R.id.learning_review_total to activity.getString(R.string.learning_review_total, expected.review)).forEach { (id, text) ->
                assertEquals(text, activity.findViewById<TextView>(id).text.toString())
            }
            val groupId = activity.resources.getIdentifier("learning_summary_results", "id", activity.packageName)
            assertTrue("Self-assessments need a shared summary surface", groupId != 0)
            val group = activity.findViewById<View>(groupId)
            listOf(R.id.learning_total, R.id.learning_known_total, R.id.learning_review_total).forEach { id ->
                var parent = activity.findViewById<View>(id).parent
                while (parent is View && parent !== group) parent = parent.parent
                assertSame(group, parent)
            }
            val review = activity.findViewById<View>(R.id.learning_review)
            assertEquals(if (expected.review > 0) View.VISIBLE else View.GONE, review.visibility)
            listOf(R.id.learning_again to (expected.review == 0), R.id.learning_review to true).forEach { (id, primary) ->
                val button = activity.findViewById<com.google.android.material.button.MaterialButton>(id)
                val reference = activity.findViewById<com.google.android.material.button.MaterialButton>(
                    if (primary) R.id.learning_review else R.id.learning_species)
                assertEquals("Background for $id", reference.backgroundTintList, button.backgroundTintList)
                assertEquals("Text for $id", reference.textColors, button.textColors)
                assertEquals("Border for $id", reference.strokeColor, button.strokeColor)
                assertEquals(reference.strokeWidth, button.strokeWidth)
                assertEquals(reference.cornerRadius, button.cornerRadius)
                assertEquals(reference.rippleColor, button.rippleColor)
            }
        }
    }

    @Test
    fun birdInfoIsAnIndependentPhotoAnswerAction(): Unit = launch(listOf(
        LearningCard.Photo("tero", "vanellus_chilensis", "Tero", "Vanellus chilensis", "learning/photos/photo-001.webp"),
        questions(1).single()
    )).use { scenario ->
        start(scenario, R.id.learning_species)
        var infoId = 0
        scenario.onActivity { activity ->
            infoId = activity.resources.getIdentifier("learning_bird_info", "id", activity.packageName)
            assertTrue("Photo answers need their own bird information action", infoId != 0)
            assertFalse(activity.findViewById<View>(infoId).isShown)
            val promptPos = IntArray(2).also { activity.findViewById<View>(R.id.learning_card_text).getLocationOnScreen(it) }
            val photoPos = IntArray(2).also { activity.findViewById<View>(R.id.learning_photo).getLocationOnScreen(it) }
            assertTrue("Prompt is inside the card area", promptPos[1] >= photoPos[1])
        }
        flip(scenario)
        val before = state(scenario)
        scenario.onActivity {
            assertTrue(it.findViewById<View>(infoId).isEnabled)
            assertEquals(it.getString(R.string.bird_info_view), it.findViewById<View>(infoId).contentDescription.toString())
            val answerView = it.findViewById<TextView>(R.id.learning_back_text)
            val photoView = it.findViewById<View>(R.id.learning_photo)
            val answerPos = IntArray(2).also { pos -> answerView.getLocationOnScreen(pos) }
            val photoPos = IntArray(2).also { pos -> photoView.getLocationOnScreen(pos) }
            assertTrue("Answer is inside the card area", answerPos[1] >= photoPos[1])
            assertTrue("Common name words must start with uppercase",
                answerView.text.toString().split(" ").all { word -> word.isNotEmpty() && word[0].isUpperCase() })
        }
        touch(scenario, R.id.learning_photo)
        val zoom = foreground(ImageZoomActivity::class.java)
        instrumentation.runOnMainSync {
            assertEquals("file:///android_asset/learning/photos/photo-001.webp", zoom.intent.getStringExtra(ImageZoomActivity.EXTRA_IMAGE_URI))
            zoom.onBackPressedDispatcher.onBackPressed()
        }
        foreground(LearningGameActivity::class.java)
        awaitCard(scenario)
        assertEquals(before, state(scenario))
        touch(scenario, infoId)
        val bird = foreground(BirdInfoActivity::class.java)
        instrumentation.runOnMainSync {
            assertEquals("Tero", bird.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
            assertEquals("Vanellus chilensis", bird.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))
            bird.onBackPressedDispatcher.onBackPressed()
        }
        foreground(LearningGameActivity::class.java)
        awaitCard(scenario)
        assertEquals(before, state(scenario))
        flip(scenario)
        scenario.onActivity { assertFalse(it.findViewById<View>(infoId).isShown) }
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        start(scenario, R.id.learning_questions)
        flip(scenario)
        scenario.onActivity { assertFalse(it.findViewById<View>(infoId).isShown) }
    }

    @Test
    fun consultationKeepsTheWholeRoundAndReviewThroughZoomRotationAndBackground() {
        val fixture = LearningCard.Photo("tero", "vanellus_chilensis", "Tero", "Vanellus chilensis", "learning/photos/photo-001.webp")
        launch(listOf(fixture, fixture.copy(id = "pato", speciesKey = "anas_versicolor",
            commonName = "Pato capuchino", scientificName = "Anas versicolor", imagePath = "learning/photos/photo-002.webp"))).use { scenario ->
            start(scenario, R.id.learning_species)
            // Leave a real partial assessment before consulting the second card.
            flip(scenario)
            tap(scenario, R.id.learning_mark)
            awaitCard(scenario)
            repeat(2) { pass ->
                flip(scenario)
                val before = state(scenario)
                val card = before.currentCard as LearningCard.Photo
                val gameModel = model(scenario)
                var originalGame: LearningGameActivity? = null
                scenario.onActivity { originalGame = it }
                touch(scenario, R.id.learning_bird_info)
                var bird = foreground(BirdInfoActivity::class.java)
                waitUi { bird.findViewById<View>(R.id.imageDistribution).isEnabled }
                val expected = "android.resource://${instrumentation.targetContext.packageName}/${
                    if (card.speciesKey == "anas_versicolor") R.drawable.spatula_versicolor_dist else R.drawable.vanellus_chilensis_dist}"
                instrumentation.runOnMainSync {
                    assertEquals(card.commonName, bird.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
                    assertEquals(card.scientificName, bird.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))
                    assertNull(bird.intent.getStringExtra(BirdInfoActivity.EXTRA_DISTRIBUTION_URI))
                    bird.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                waitUi {
                    val current = foregroundActivity() as? BirdInfoActivity
                    current?.resources?.configuration?.orientation == 2 && current.findViewById<View>(R.id.imageDistribution).isEnabled
                }
                bird = foreground(BirdInfoActivity::class.java)
                activityTouch(bird, R.id.imageDistribution)
                val zoom = foreground(ImageZoomActivity::class.java)
                waitUi { zoom.findViewById<android.widget.ImageView>(R.id.imgZoom).drawable != null }
                instrumentation.runOnMainSync {
                    assertEquals(expected, zoom.intent.getStringExtra(ImageZoomActivity.EXTRA_IMAGE_URI))
                    zoom.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    originalGame!!.recreate()
                }
                waitUi {
                    val current = foregroundActivity() as? ImageZoomActivity
                    current?.resources?.configuration?.orientation == 1 && current.findViewById<android.widget.ImageView>(R.id.imgZoom).drawable != null
                }
                // Home and return to the existing task while zoom remains at the top.
                automation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                waitUi { foregroundActivity() == null }
                val taskId = zoom.taskId
                instrumentation.runOnMainSync {
                    val manager = instrumentation.targetContext.getSystemService(android.app.ActivityManager::class.java)
                    @Suppress("DEPRECATION") // RecentTaskInfo.id also exists on API 24–28.
                    val task = manager.appTasks.first { it.taskInfo.id == taskId }
                    task.moveToFront()
                }
                back(foreground(ImageZoomActivity::class.java))
                back(foreground(BirdInfoActivity::class.java))
                val returned = foreground(LearningGameActivity::class.java)
                awaitCard(scenario)
                assertNotSame(originalGame, returned)
                assertSame(gameModel, model(scenario))
                assertEquals(before, state(scenario))
                scenario.onActivity {
                    assertTrue(it.findViewById<View>(R.id.learning_bird_info).isEnabled)
                    assertNotNull(it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable)
                }
                assertRecreated(scenario)
                assertEquals(before, state(scenario))
                scenario.onActivity {
                    val token = before.cardToken!!
                    val assess = LearningGameActivity::class.java.getDeclaredMethod("assess", java.lang.Long.TYPE, SelfAssessment::class.java)
                        .apply { isAccessible = true }
                    assess.invoke(it, token, SelfAssessment.REVIEW)
                    assess.invoke(it, token, SelfAssessment.KNOWN)
                }
                assertEquals(before.assessments.size + 1, state(scenario).assessments.size)
                if (pass == 0) {
                    assertEquals(LearningScreen.SUMMARY, state(scenario).screen)
                    tap(scenario, R.id.learning_review)
                    awaitCard(scenario)
                    assertEquals(LearningPass.REVIEW, state(scenario).pass)
                    flip(scenario)
                    tap(scenario, R.id.learning_known)
                    awaitCard(scenario)
                }
            }
        }
    }

    @Test
    fun staleLoadingFlippingAndSimultaneousActionsCannotOpenOrChangeTheCard() = animatorScale("10") {
        launch(listOf(
            LearningCard.Photo("tero", "vanellus_chilensis", "Tero", "Vanellus chilensis", "learning/photos/photo-001.webp"),
            LearningCard.Photo("pato", "anas_versicolor", "Pato", "Anas versicolor", "learning/photos/photo-002.webp")
        )).use { scenario ->
            val monitor = instrumentation.addMonitor(BirdInfoActivity::class.java.name, null, false)
            try {
                start(scenario, R.id.learning_species)
                val oldToken = state(scenario).cardToken!!
                scenario.onActivity { invokeBirdInfo(it, oldToken) }
                assertEquals(0, monitor.hits)
                // Invoke in the same main-thread turn, before the flip can finish.
                scenario.onActivity {
                    it.findViewById<View>(R.id.learning_reveal).performClick()
                    invokeBirdInfo(it, oldToken)
                }
                assertEquals(0, monitor.hits)
                awaitCard(scenario)
                tap(scenario, R.id.learning_known)
                awaitCard(scenario)
                flip(scenario)
                val before = state(scenario)
                scenario.onActivity {
                    invokeBirdInfo(it, oldToken)
                    val ready = LearningGameActivity::class.java.getDeclaredField("photoReady").apply { isAccessible = true }
                    ready.setBoolean(it, false)
                    invokeBirdInfo(it, before.cardToken!!)
                    ready.setBoolean(it, true)
                }
                assertEquals(0, monitor.hits)
                scenario.onActivity {
                    val button = it.findViewById<View>(R.id.learning_bird_info)
                    button.performClick()
                    button.performClick()
                    invokeBirdInfo(it, before.cardToken!!)
                    it.findViewById<View>(R.id.learning_card).performClick()
                    it.findViewById<View>(R.id.learning_known).performClick()
                    it.findViewById<View>(R.id.learning_mark).performClick()
                    it.onBackPressedDispatcher.onBackPressed()
                }
                back(foreground(BirdInfoActivity::class.java))
                foreground(LearningGameActivity::class.java)
                awaitCard(scenario)
                assertEquals(1, monitor.hits)
                assertEquals(before, state(scenario))
                // A second independent consultation is allowed after onResume.
                touch(scenario, R.id.learning_bird_info)
                back(foreground(BirdInfoActivity::class.java))
                foreground(LearningGameActivity::class.java)
                awaitCard(scenario)
                assertEquals(2, monitor.hits)
                assertEquals(before, state(scenario))
            } finally { instrumentation.removeMonitor(monitor) }
        }
    }

    @Test
    fun failedLaunchRestoresTheAnswerAndAllowsRetry(): Unit = launch(listOf(
        LearningCard.Photo("tero", "vanellus_chilensis", "Tero", "Vanellus chilensis", "learning/photos/photo-001.webp")
    )).use { scenario ->
        start(scenario, R.id.learning_species)
        flip(scenario)
        val before = state(scenario)
        val pm = instrumentation.targetContext.packageManager
        val component = android.content.ComponentName(instrumentation.targetContext, BirdInfoActivity::class.java)
        val old = pm.getComponentEnabledSetting(component)
        try {
            pm.setComponentEnabledSetting(component, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP)
            tap(scenario, R.id.learning_bird_info)
            assertEquals(before, state(scenario))
            scenario.onActivity {
                listOf(R.id.learning_card, R.id.learning_bird_info, R.id.learning_known, R.id.learning_mark).forEach { id ->
                    assertTrue(it.findViewById<View>(id).isEnabled)
                }
            }
        } finally { pm.setComponentEnabledSetting(component, old, android.content.pm.PackageManager.DONT_KILL_APP) }
        touch(scenario, R.id.learning_bird_info)
        back(foreground(BirdInfoActivity::class.java))
        foreground(LearningGameActivity::class.java)
        assertEquals(before, state(scenario))
    }

    @Test
    fun absentAndDeliberatelyInvalidMapsReturnWithoutAssessmentOrDiscard() {
        val application = instrumentation.targetContext.applicationContext as Application
        var invalidate = false
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, state: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                if (activity is BirdInfoActivity && invalidate) {
                    activity.intent.putExtra(BirdInfoActivity.EXTRA_DISTRIBUTION_URI, "content://invalid.test/missing")
                    com.bumptech.glide.Glide.with(activity).clear(activity.findViewById<android.widget.ImageView>(R.id.imageDistribution))
                    BirdInfoActivity::class.java.getDeclaredMethod("bindDataFromIntent")
                        .apply { isAccessible = true }.invoke(activity)
                }
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        application.registerActivityLifecycleCallbacks(callback)
        try {
            listOf("Aramides ypecaha", "Vanellus chilensis").forEachIndexed { index, scientific ->
                invalidate = index == 1
                launch(listOf(LearningCard.Photo("bird", "bird", "Ave de prueba", scientific, "learning/photos/photo-001.webp"))).use { scenario ->
                    start(scenario, R.id.learning_species)
                    repeat(2) { pass ->
                        flip(scenario)
                        val before = state(scenario)
                        touch(scenario, R.id.learning_bird_info)
                        val bird = foreground(BirdInfoActivity::class.java)
                        waitUi { bird.findViewById<View>(R.id.distributionUnavailable).isShown }
                        instrumentation.runOnMainSync {
                            assertFalse(bird.findViewById<View>(R.id.imageDistribution).isEnabled)
                            bird.findViewById<View>(R.id.imageDistribution).performClick()
                        }
                        assertSame(bird, foreground(BirdInfoActivity::class.java))
                        captureActivity(bird, "t4-failed-map-$index-$pass")
                        back(bird)
                        foreground(LearningGameActivity::class.java)
                        awaitCard(scenario)
                        assertEquals(before, state(scenario))
                        tap(scenario, if (pass == 0) R.id.learning_mark else R.id.learning_known)
                        assertEquals(1, state(scenario).assessments.size)
                        if (pass == 0) {
                            tap(scenario, R.id.learning_review)
                            awaitCard(scenario)
                        }
                    }
                }
            }
        } finally { application.unregisterActivityLifecycleCallbacks(callback) }
    }

    private fun invokeBirdInfo(activity: LearningGameActivity, token: Long) {
        LearningGameActivity::class.java.getDeclaredMethod("openBirdInfo", java.lang.Long.TYPE)
            .apply { isAccessible = true }.invoke(activity, token)
    }

    @Test
    fun consultationVisualMatrixKeepsItsOwnAccessibleActionAndStableComposition() {
        val fixture = LearningCard.Photo("tero", "vanellus_chilensis", "Tero de nombre largo para comprobar la lectura",
            "Vanellus chilensis", "learning/photos/photo-001.webp")
        val oldFont = shell("settings get system font_scale").trim().takeUnless { it == "null" }
        val rotation = listOf("accelerometer_rotation", "user_rotation")
            .associateWith { shell("settings get system $it").trim().takeUnless { value -> value == "null" } }
        val animations = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
            .associateWith { shell("settings get global $it").trim().takeUnless { value -> value == "null" } }
        try {
            animations.keys.forEach { shell("settings put global $it 0") }
            listOf("1.0", "2.0").forEach { font ->
                shell("settings put system font_scale $font")
                launch(listOf(fixture)).use { scenario ->
                    start(scenario, R.id.learning_species)
                    listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                        scenario.onActivity { it.delegate.localNightMode = theme }
                        await(scenario) { it.resources.configuration.uiMode and 48 == if (theme == 2) 32 else 16 }
                        listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                            scenario.onActivity { it.requestedOrientation = orientation }
                            await(scenario) { it.resources.configuration.orientation == if (orientation == 1) 1 else 2 }
                            awaitCard(scenario)
                            if (state(scenario).face == CardFace.BACK) flip(scenario)
                            assertFrontTreeHidesAnswer(fixture)
                            val geometry = cardGeometry(scenario)
                            flip(scenario)
                            val revealedGeometry = cardGeometry(scenario)
                            scenario.onActivity {
                                geometry.zip(revealedGeometry).forEach { (a, b) ->
                                    val tolerance = 2 * it.resources.displayMetrics.density
                                    assertTrue("Stable composition with information action: $a / $b",
                                        listOf(a.left - b.left, a.top - b.top, a.right - b.right, a.bottom - b.bottom)
                                            .all { delta -> kotlin.math.abs(delta) <= tolerance })
                                }
                            }
                            val before = state(scenario)
                            val prefix = "t4-$font-$theme-$orientation"
                            capture(scenario, "$prefix-answer")
                            reachable(scenario, R.id.learning_bird_info)
                            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                            val nodes = accessibleNodes(requireNotNull(automation.rootInActiveWindow))
                            val info = nodes.single { it.viewIdResourceName?.endsWith(":id/learning_bird_info") == true }
                            assertTrue(info.isClickable && info.isEnabled)
                            assertEquals("Ver información del ave", info.contentDescription.toString())
                            val rect = android.graphics.Rect().also { info.getBoundsInScreen(it) }
                            scenario.onActivity {
                                assertTrue(rect.width() >= 48 * it.resources.displayMetrics.density)
                                assertTrue(rect.height() >= 48 * it.resources.displayMetrics.density)
                                val button = it.findViewById<android.widget.ImageButton>(R.id.learning_bird_info)
                                assertNotNull(button.drawable)
                            }
                            capture(scenario, "$prefix-actions")
                            touch(scenario, R.id.learning_bird_info)
                            val bird = foreground(BirdInfoActivity::class.java)
                            waitUi { bird.findViewById<View>(R.id.imageDistribution).isEnabled }
                            instrumentation.runOnMainSync {
                                bird.delegate.localNightMode = theme
                                bird.requestedOrientation = orientation
                            }
                            waitUi {
                                val current = foregroundActivity() as? BirdInfoActivity
                                current != null && current.resources.configuration.uiMode and 48 == (if (theme == 2) 32 else 16) &&
                                    current.resources.configuration.orientation == (if (orientation == 1) 1 else 2)
                            }
                            val themedBird = foreground(BirdInfoActivity::class.java)
                            waitUi { themedBird.findViewById<View>(R.id.imageDistribution).isEnabled }
                            captureActivity(themedBird, "$prefix-info")
                            activityTouch(themedBird, R.id.imageDistribution)
                            val initialZoom = foreground(ImageZoomActivity::class.java)
                            instrumentation.runOnMainSync { initialZoom.requestedOrientation = orientation }
                            waitUi { (foregroundActivity() as? ImageZoomActivity)?.resources?.configuration?.orientation ==
                                if (orientation == 1) 1 else 2 }
                            val zoom = foreground(ImageZoomActivity::class.java)
                            waitUi { zoom.findViewById<android.widget.ImageView>(R.id.imgZoom).drawable != null }
                            captureActivity(zoom, "$prefix-zoom")
                            back(zoom)
                            back(foreground(BirdInfoActivity::class.java))
                            foreground(LearningGameActivity::class.java)
                            awaitCard(scenario)
                            await(scenario) { it.resources.configuration.orientation == if (orientation == 1) 1 else 2 }
                            assertEquals(before, state(scenario))
                            reachable(scenario, R.id.learning_mark)
                            capture(scenario, "$prefix-return")
                        }
                    }
                }
            }
        } finally {
            restoreSetting("system", "font_scale", oldFont)
            rotation.forEach { (key, value) -> restoreSetting("system", key, value) }
            animations.forEach { (key, value) -> restoreSetting("global", key, value) }
        }
    }

    @Test
    fun captureExistingBirdHeaderAsVisualReference() {
        val context = instrumentation.targetContext
        val type = BirdInfoActivity::class.java
        ActivityScenario.launch<BirdInfoActivity>(Intent(context, type)
            .putExtra(BirdInfoActivity.EXTRA_COMMON_NAME, "Tero")
            .putExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, "Vanellus chilensis")).use { scenario ->
            listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                scenario.onActivity {
                    it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    it.delegate.localNightMode = theme
                }
                waitUi {
                    val current = foregroundActivity()
                    type.isInstance(current) && current!!.resources.configuration.uiMode and 48 ==
                        if (theme == AppCompatDelegate.MODE_NIGHT_YES) 32 else 16
                }
                captureActivity(foreground(type), "t1-reference-bird-$theme")
            }
        }
    }

    @Test
    fun loadingKeepsBothModeActionsDisabledUntilContentArrives() {
        val loaded = CountDownLatch(1)
        try {
            launch(questions(10), loaded).use { scenario ->
                scenario.onActivity { activity ->
                    listOf(R.id.learning_questions, R.id.learning_species).forEach {
                        assertFalse(activity.findViewById<View>(it).isEnabled)
                        activity.findViewById<View>(it).performClick()
                    }
                    listOf(R.id.learning_questions_availability, R.id.learning_species_availability).forEach {
                        assertEquals(activity.getString(R.string.learning_loading), activity.findViewById<TextView>(it).text)
                    }
                    assertEquals(LearningScreen.SELECTOR, vm(activity).state.value.session.screen)
                }
                loaded.countDown()
                await(scenario) { it.findViewById<View>(R.id.learning_questions).isEnabled }
                scenario.onActivity {
                    assertEquals(it.getString(R.string.learning_available),
                        it.findViewById<TextView>(R.id.learning_questions_availability).text)
                    assertFalse(it.findViewById<View>(R.id.learning_species).isEnabled)
                }
            }
        } finally { loaded.countDown() }
    }

    @Test
    fun selectorVisualMatrixKeepsInsetsAndAllContentReachable() {
        val oldFont = Settings.System.getString(instrumentation.targetContext.contentResolver, Settings.System.FONT_SCALE)
        val oldSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")?.trim()
        val animationSettings = listOf("window_animation_scale", "transition_animation_scale")
            .associateWith { shell("settings get global $it").trim().takeUnless { value -> value == "null" } }
        try {
            animationSettings.keys.forEach { shell("settings put global $it 0") }
            listOf(false, true).forEach { small ->
                shell(if (small) "wm size 720x1280" else if (oldSize == null) "wm size reset" else "wm size $oldSize")
                shell("settings put system font_scale ${if (small) "2.0" else "1.0"}")
                launch().use { scenario ->
                    listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                        scenario.onActivity { it.delegate.localNightMode = theme }
                        await(scenario) {
                            val expected = if (theme == AppCompatDelegate.MODE_NIGHT_YES) 32 else 16
                            it.resources.configuration.uiMode and 48 == expected
                        }
                        listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                            scenario.onActivity { it.requestedOrientation = orientation }
                            await(scenario) { it.resources.configuration.orientation ==
                                if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) 1 else 2 }
                            await(scenario) { !it.findViewById<View>(R.id.learning_content).isLayoutRequested }
                            val prefix = "t1-${if (small) "small-200" else "normal"}-$theme-$orientation"
                            scenario.onActivity { activity ->
                                val root = activity.findViewById<View>(R.id.insets_container)
                                val insets = requireNotNull(androidx.core.view.ViewCompat.getRootWindowInsets(root))
                                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() or androidx.core.view.WindowInsetsCompat.Type.displayCutout())
                                assertEquals(insets.top, root.paddingTop)
                                assertEquals(insets.bottom, root.paddingBottom)
                                assertEquals(insets.left, root.paddingLeft)
                                assertEquals(insets.right, root.paddingRight)
                                assertEquals(theme == AppCompatDelegate.MODE_NIGHT_NO,
                                    androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                                        .isAppearanceLightStatusBars)
                                assertEquals(theme == AppCompatDelegate.MODE_NIGHT_NO,
                                    androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                                        .isAppearanceLightNavigationBars)
                                val header = activity.findViewById<TextView>(R.id.learning_header_title)
                                val textLayout = requireNotNull(header.layout)
                                assertTrue(textLayout.height <= header.height - header.compoundPaddingTop - header.compoundPaddingBottom)
                                repeat(textLayout.lineCount) { assertEquals(0, textLayout.getEllipsisCount(it)) }
                                val scroll = activity.findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll)
                                scroll.scrollTo(0, 0)
                                val block = activity.findViewById<View>(R.id.learning_content)
                                val origin = IntArray(2)
                                val position = IntArray(2)
                                scroll.getLocationOnScreen(origin)
                                block.getLocationOnScreen(position)
                                val available = scroll.height - 2 * activity.resources.getDimensionPixelSize(R.dimen.screen_padding_large)
                                val top = position[1] - origin[1] - activity.resources.getDimensionPixelSize(R.dimen.screen_padding_large)
                                if (block.height <= available) assertTrue(kotlin.math.abs(available - block.height - 2 * top) <=
                                    2 * activity.resources.displayMetrics.density)
                                else assertEquals(0, top)
                            }
                            instrumentation.waitForIdleSync()
                            lateinit var current: LearningGameActivity
                            scenario.onActivity { current = it }
                            captureActivity(current, "$prefix-start")
                            reachable(scenario, R.id.learning_species_availability)
                            captureActivity(current, "$prefix-end")
                            reachable(scenario, R.id.learning_questions)
                        }
                    }
                }
            }
        } finally {
            shell(if (oldSize == null) "wm size reset" else "wm size $oldSize")
            restoreSetting("system", "font_scale", oldFont)
            animationSettings.forEach { (name, value) -> restoreSetting("global", name, value) }
        }
    }

    @Test
    fun selectorCentersWhenItFitsAndScrollsBothEndsWhenItDoesNot(): Unit = launch(questions(1)).use { scenario ->
        await(scenario) { !it.findViewById<View>(R.id.learning_selector).isLayoutRequested }
        scenario.onActivity { activity ->
            val scroll = activity.findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll)
            val selector = activity.findViewById<View>(R.id.learning_selector)
            val viewport = scroll.height - scroll.paddingTop - scroll.paddingBottom
            val position = IntArray(2)
            val origin = IntArray(2)
            selector.getLocationOnScreen(position)
            scroll.getLocationOnScreen(origin)
            if (selector.height <= viewport - 2 * activity.resources.getDimensionPixelSize(R.dimen.screen_padding_large)) {
                val above = position[1] - origin[1] - scroll.paddingTop
                val below = viewport - above - selector.height
                assertTrue("Free space must be balanced: $above / $below",
                    kotlin.math.abs(above - below) <= 2 * activity.resources.displayMetrics.density)
            }
            // Force overflow without changing device settings; retain the real measured controls.
            scroll.layoutParams = scroll.layoutParams.apply { height = 240; (this as android.widget.LinearLayout.LayoutParams).weight = 0f }
        }
        await(scenario) { it.findViewById<View>(R.id.learning_scroll).height == 240 }
        reachable(scenario, R.id.learning_species)
        reachable(scenario, R.id.learning_questions)
        scenario.onActivity { activity ->
            val scroll = activity.findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll)
            scroll.scrollTo(0, 0)
        }
        instrumentation.waitForIdleSync()
        scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.learning_questions_description)
            assertTrue(title.text.isNotEmpty())
            assertEquals(0, activity.findViewById<View>(R.id.learning_scroll).scrollY)
        }
    }

    @Test
    fun bothModesCompleteRoundAndSuccessiveReviewsOfflineWithoutPermissions() {
        val context = instrumentation.targetContext
        val permissions = listOf(android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
            if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES
            else android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.forEach { assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED, context.checkSelfPermission(it)) }
        val resolver = context.contentResolver
        val airplane = Settings.Global.getString(resolver, "airplane_mode_on")
        val wifi = Settings.Global.getString(resolver, "wifi_on")
        val data = Settings.Global.getString(resolver, "mobile_data")
        listOf(airplane, wifi, data).forEach { assertTrue("Radio state must be restorable", it in listOf("0", "1")) }
        try {
            shell("cmd connectivity airplane-mode enable")
            shell("svc wifi disable")
            shell("svc data disable")
            assertEquals("1", shell("settings get global airplane_mode_on").trim())
            assertEquals("0", shell("settings get global wifi_on").trim())
            assertEquals("0", shell("settings get global mobile_data").trim())
            var connectivity = ""
            waitUi(beforeCheck = { connectivity = shell("dumpsys connectivity") }) {
                connectivity.lineSequence().any { it.trim() == "Active default network: none" }
            }
            val networkEvidence = File(context.getExternalFilesDir(null), "offline-connectivity.txt")
            networkEvidence.writeText(connectivity)
            preserveCapture(networkEvidence)
            listOf(R.id.learning_questions, R.id.learning_species).forEach { mode ->
                launch().use { scenario ->
                    start(scenario, mode)
                    val chosen = state(scenario).cards
                    repeat(10) { index ->
                        flip(scenario)
                        tap(scenario, if (index < 7) R.id.learning_known else R.id.learning_mark)
                    }
                    assertEquals(LearningSummary(10, 7, 3), state(scenario).summary)
                    tap(scenario, R.id.learning_review)
                    awaitCard(scenario)
                    assertEquals(chosen.takeLast(3), state(scenario).cards)
                    flip(scenario)
                    assertRecreated(scenario)
                    val before = state(scenario)
                    scenario.moveToState(Lifecycle.State.CREATED)
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    awaitCard(scenario)
                    assertEquals(before, state(scenario))
                    tap(scenario, R.id.learning_known)
                    repeat(2) { index ->
                        flip(scenario)
                        tap(scenario, if (index == 0) R.id.learning_known else R.id.learning_mark)
                    }
                    assertEquals(LearningSummary(3, 2, 1), state(scenario).summary)
                    tap(scenario, R.id.learning_review)
                    awaitCard(scenario)
                    assertEquals(chosen.takeLast(1), state(scenario).cards)
                    flip(scenario)
                    tap(scenario, R.id.learning_known)
                    assertEquals(LearningSummary(1, 1, 0), state(scenario).summary)
                    scenario.onActivity { assertEquals(View.GONE, it.findViewById<View>(R.id.learning_review).visibility) }
                    permissions.forEach { assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED, context.checkSelfPermission(it)) }
                }
            }
        } finally {
            shell("cmd connectivity airplane-mode ${if (airplane == "1") "enable" else "disable"}")
            shell("svc wifi ${if (wifi == "1") "enable" else "disable"}")
            shell("svc data ${if (data == "1") "enable" else "disable"}")
        }
    }

    @Test
    fun menuOpensBothModesAndReentryDiscardsRoundsAndSummaries() = instantCapture(false) {
        val context = instrumentation.targetContext
        val permissions = listOf(android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION,
            if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val grants = permissions.map { context.checkSelfPermission(it) }
        ActivityScenario.launch(MainMenuActivity::class.java).use {
            captureActivity(foreground(MainMenuActivity::class.java), "menu-default")
            listOf(R.id.learning_questions, R.id.learning_species).forEach { mode ->
                menuTouch("btnLearning")
                val game = foreground(LearningGameActivity::class.java)
                awaitGame(game) { !vm(it).state.value.isLoading }
                assertEquals(context.packageName, automation.rootInActiveWindow?.packageName?.toString())
                assertEquals(grants, permissions.map { context.checkSelfPermission(it) })
                instrumentation.runOnMainSync { assertEquals(LearningState(), vm(game).state.value.session) }
                activityTouch(game, mode)
                awaitGame(game) { vm(it).state.value.session.screen == LearningScreen.CARD && it.findViewById<View>(R.id.learning_card).isEnabled }
                instrumentation.runOnMainSync { assertEquals(if (mode == R.id.learning_questions) LearningMode.QUESTIONS else LearningMode.SPECIES,
                    vm(game).state.value.session.mode) }
                activityTouch(game, R.id.learning_reveal)
                awaitGame(game) { it.findViewById<View>(R.id.learning_mark).isShown && it.findViewById<View>(R.id.learning_card).isEnabled }
                activityTouch(game, R.id.learning_mark)
                instrumentation.runOnMainSync { assertEquals(1, vm(game).state.value.session.assessments.size) }
                back(game)
                instrumentation.runOnMainSync { assertEquals(LearningState(), vm(game).state.value.session) }
                back(game)
                foreground(MainMenuActivity::class.java)

                menuTouch("btnLearning")
                val fresh = foreground(LearningGameActivity::class.java)
                awaitGame(fresh) { !vm(it).state.value.isLoading }
                instrumentation.runOnMainSync { assertEquals(LearningState(), vm(fresh).state.value.session) }
                activityTouch(fresh, mode)
                repeat(10) { index ->
                    awaitGame(fresh) { it.findViewById<View>(R.id.learning_round).isShown && it.findViewById<View>(R.id.learning_card).isEnabled }
                    activityTouch(fresh, R.id.learning_reveal)
                    awaitGame(fresh) { it.findViewById<View>(R.id.learning_mark).isShown && it.findViewById<View>(R.id.learning_card).isEnabled }
                    activityTouch(fresh, if (index < 7) R.id.learning_known else R.id.learning_mark)
                }
                awaitGame(fresh) { vm(it).state.value.session.screen == LearningScreen.SUMMARY }
                instrumentation.runOnMainSync { assertEquals(LearningSummary(10, 7, 3), vm(fresh).state.value.session.summary) }
                activityTouch(fresh, R.id.learning_exit)
                foreground(MainMenuActivity::class.java)
                menuTouch("btnLearning")
                val afterSummary = foreground(LearningGameActivity::class.java)
                awaitGame(afterSummary) { !vm(it).state.value.isLoading }
                instrumentation.runOnMainSync { assertEquals(LearningState(), vm(afterSummary).state.value.session) }
                back(afterSummary)
                foreground(MainMenuActivity::class.java)
            }
        }
    }

    @Test
    fun instantCaptureReturnsToMenuAndKeepsExistingDestinations() = instantCapture(true) {
        ActivityScenario.launch(MainMenuActivity::class.java).use {
            val capture = foreground(MainActivity::class.java)
            back(capture)
            foreground(MainMenuActivity::class.java)
            menuTouch("btnLearning")
            val game = foreground(LearningGameActivity::class.java)
            awaitGame(game) { !vm(it).state.value.isLoading }
            back(game)
            foreground(MainMenuActivity::class.java)
            listOf("btnObservationLog" to ObservationLogActivity::class.java,
                "btnSettings" to SettingsActivity::class.java,
                "btnCapture" to MainActivity::class.java).forEach { (button, destination) ->
                menuTouch(button)
                val opened = foreground(destination)
                back(opened)
                foreground(MainMenuActivity::class.java)
            }
            instrumentation.runOnMainSync { foregroundActivity()?.finish() }
        }
    }

    @Test
    fun smallMenuAtDoubleTextKeepsEveryAccessFullyReachableInBothOrientations() = instantCapture(false) {
        val resolver = instrumentation.targetContext.contentResolver
        val oldFont = Settings.System.getString(resolver, Settings.System.FONT_SCALE)
        val oldSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")?.trim()
        val oldRotation = Settings.System.getString(resolver, Settings.System.ACCELEROMETER_ROTATION)
        val oldUserRotation = Settings.System.getString(resolver, Settings.System.USER_ROTATION)
        try {
            shell("wm size 720x1280")
            shell("settings put system font_scale 2.0")
            ActivityScenario.launch(MainMenuActivity::class.java).use { scenario ->
                listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                    scenario.onActivity { it.requestedOrientation = orientation }
                    waitUi {
                        val current = foregroundActivity() as? MainMenuActivity
                        current != null && current.resources.configuration.fontScale == 2f &&
                            current.resources.configuration.orientation == if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                android.content.res.Configuration.ORIENTATION_PORTRAIT else android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    }
                    val current = foreground(MainMenuActivity::class.java)
                    listOf("btnCapture", "btnObservationLog", "btnLearning", "btnSettings").forEach { name ->
                        val id = current.resources.getIdentifier(name, "id", instrumentation.targetContext.packageName)
                        assertTrue("Menu access missing: $name", id != 0)
                        val rect = activityReachable(current, id)
                        instrumentation.runOnMainSync {
                            val text = current.findViewById<TextView>(id)
                            val layout = requireNotNull(text.layout)
                            assertTrue("Text clipped: $name", layout.height <= text.height - text.compoundPaddingTop - text.compoundPaddingBottom)
                            repeat(layout.lineCount) { assertEquals("Text ellipsized: $name", 0, layout.getEllipsisCount(it)) }
                            assertTrue("Touch target: $name", rect.height() >= 48 * current.resources.displayMetrics.density)
                        }
                        captureActivity(current, "menu-${orientation}-$name")
                    }
                    activityTouch(current, current.resources.getIdentifier("btnLearning", "id", instrumentation.targetContext.packageName))
                    val game = foreground(LearningGameActivity::class.java)
                    awaitGame(game) { !vm(it).state.value.isLoading }
                    back(game)
                    foreground(MainMenuActivity::class.java)
                }
            }
        } finally {
            shell(if (oldSize == null) "wm size reset" else "wm size $oldSize")
            restoreSetting("system", "font_scale", oldFont)
            restoreSetting("system", "accelerometer_rotation", oldRotation)
            restoreSetting("system", "user_rotation", oldUserRotation)
        }
    }

    private fun instantCapture(enabled: Boolean, block: () -> Unit) {
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val existed = prefs.contains("instant_capture")
        val old = SettingsManager.isInstantCapture(context)
        try {
            SettingsManager.setInstantCapture(context, enabled)
            block()
        } finally {
            if (existed) SettingsManager.setInstantCapture(context, old)
            else prefs.edit().remove("instant_capture").commit()
        }
    }

    private fun foregroundActivity(): Activity? = ActivityLifecycleMonitorRegistry.getInstance()
        .getActivitiesInStage(Stage.RESUMED).singleOrNull()

    private fun waitUi(beforeCheck: () -> Unit = {}, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15)
        do {
            beforeCheck()
            var ready = false
            instrumentation.runOnMainSync { ready = condition() }
            if (ready) return
            try { automation.waitForIdle(100, 500) } catch (_: java.util.concurrent.TimeoutException) { }
        } while (System.nanoTime() < deadline)
        throw AssertionError("UI condition timed out")
    }

    private fun <T : Activity> foreground(type: Class<T>): T {
        lateinit var activity: T
        waitUi(beforeCheck = { if (type == MainActivity::class.java) dismissIdentificationPermissions() }) {
            val current = foregroundActivity()
            if (type.isInstance(current)) activity = type.cast(current)!!
            type.isInstance(current) && current!!.window.decorView.hasWindowFocus()
        }
        return activity
    }

    private fun awaitGame(activity: LearningGameActivity, condition: (LearningGameActivity) -> Boolean) = waitUi { condition(activity) }
    private fun back(activity: Activity) {
        instrumentation.runOnMainSync { (activity as androidx.activity.ComponentActivity).onBackPressedDispatcher.onBackPressed() }
    }
    private fun menuTouch(name: String) {
        val menu = foreground(MainMenuActivity::class.java)
        val id = menu.resources.getIdentifier(name, "id", instrumentation.targetContext.packageName)
        assertTrue("Menu access missing: $name", id != 0)
        activityTouch(menu, id)
    }
    private fun activityReachable(activity: Activity, id: Int): android.graphics.Rect {
        var initialSize = ""
        instrumentation.runOnMainSync {
            val view = requireNotNull(activity.findViewById<View>(id))
            initialSize = "${view.width}x${view.height}"
            view.requestRectangleOnScreen(android.graphics.Rect(0, 0, view.width, view.height), true)
        }
        val rect = android.graphics.Rect()
        try {
            waitUi {
                val view = activity.findViewById<View>(id)
                !activity.window.decorView.isLayoutRequested && !view.isLayoutRequested &&
                    view.isShown && view.isEnabled && view.getGlobalVisibleRect(rect) && rect.height() == view.height && rect.width() == view.width
            }
        } catch (failure: AssertionError) {
            captureActivity(activity, "unreachable-$id")
            var detail = ""
            instrumentation.runOnMainSync {
                val view = activity.findViewById<View>(id)
                detail = "${activity.resources.getResourceEntryName(id)} initially $initialSize, now ${view.width}x${view.height}, visible $rect"
            }
            throw AssertionError(detail, failure)
        }
        return rect
    }
    private fun activityTouch(activity: Activity, id: Int) {
        val rect = activityReachable(activity, id)
        val time = android.os.SystemClock.uptimeMillis()
        listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP).forEach { action ->
            val event = android.view.MotionEvent.obtain(time, android.os.SystemClock.uptimeMillis(), action, rect.exactCenterX(), rect.exactCenterY(), 0)
            event.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
            assertTrue(automation.injectInputEvent(event, true))
            event.recycle()
        }
        instrumentation.waitForIdleSync()
    }
    private fun dismissIdentificationPermissions() {
        repeat(4) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            val deny = automation.rootInActiveWindow?.let(::accessibleNodes)?.firstOrNull {
                it.viewIdResourceName?.let { id -> id.endsWith(":id/permission_deny_button") ||
                    id.endsWith(":id/permission_deny_and_dont_ask_again_button") } == true
            }
            if (deny != null) {
                deny.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                try { automation.waitForIdle(100, 2000) } catch (_: java.util.concurrent.TimeoutException) { }
            }
        }
    }
    private fun restoreSetting(namespace: String, name: String, value: String?) {
        shell(if (value == null) "settings delete $namespace $name" else "settings put $namespace $name $value")
    }
    private fun captureActivity(activity: Activity, name: String) {
        // Dos cuadros compuestos evitan capturar la posición anterior al desplazamiento.
        repeat(2) {
            val frame = CountDownLatch(1)
            instrumentation.runOnMainSync {
                val view = activity.window.decorView
                if (android.os.Build.VERSION.SDK_INT >= 29 && view.isHardwareAccelerated) {
                    view.viewTreeObserver.registerFrameCommitCallback { frame.countDown() }
                    view.invalidate()
                } else view.postOnAnimation { view.postOnAnimation { frame.countDown() } }
            }
            assertTrue(frame.await(10, TimeUnit.SECONDS))
        }
        val bitmap = requireNotNull(automation.takeScreenshot())
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        shell("mkdir -p /data/local/tmp/aves-t7-captures")
        shell("cp ${file.absolutePath} /data/local/tmp/aves-t7-captures/${file.name}")
    }

    @Test
    fun explicitLaunchOffersBothModes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent().setClassName(context, "com.example.avesargentinas.ui.learning.LearningGameActivity")
        ActivityScenario.launch<Activity>(intent).use { scenario ->
            scenario.onActivity {
                assertEquals("Preguntas sobre aves", it.findViewById<TextView>(R.id.learning_questions).text.toString())
                assertEquals("Reconocé la especie", it.findViewById<TextView>(R.id.learning_species).text.toString())
                assertTrue(it.findViewById<View>(R.id.learning_selector).isShown)
                assertTrue(it.findViewById<View>(R.id.learning_questions_description).top < it.findViewById<View>(R.id.learning_questions_availability).top)
                assertTrue(it.findViewById<View>(R.id.learning_questions_availability).top < it.findViewById<View>(R.id.learning_questions).top)
                assertTrue(it.findViewById<View>(R.id.learning_species_description).top < it.findViewById<View>(R.id.learning_species_availability).top)
                assertTrue(it.findViewById<View>(R.id.learning_species_availability).top < it.findViewById<View>(R.id.learning_species).top)
                assertEquals(0, it.resources.getIdentifier("learning_flip_hint", "id", it.packageName))
                assertEquals(0, it.resources.getIdentifier("learning_front", "id", it.packageName))
                val toolbar = it.findViewById<View>(R.id.learning_toolbar)
                val header = it.findViewById<View>(R.id.learning_header_title)
                val toolbarCenter = toolbar.width / 2f
                val headerCenter = (header.left + header.right) / 2f
                assertTrue("Toolbar title must be horizontally centered",
                    kotlin.math.abs(toolbarCenter - headerCenter) <= 2 * it.resources.displayMetrics.density)
            }
        }
    }

    @Test
    fun tinderSwipeRightMarksKnownAndSwipeLeftMarksReview(): Unit = animatorScale("0") {
        launch(questions(2)).use { scenario ->
            start(scenario, R.id.learning_questions)
            val firstCard = state(scenario).cards[0]
            val secondCard = state(scenario).cards[1]
            // Swiping right on CardFace.BACK marks KNOWN and advances
            flip(scenario)
            scenario.onActivity { activity ->
                val mark = activity.findViewById<View>(R.id.learning_mark)
                val known = activity.findViewById<View>(R.id.learning_known)
                assertTrue("Review button must be on the left and Known on the right", mark.left < known.left)
            }
            swipeCard(scenario, left = false)
            awaitCard(scenario)
            assertEquals(mapOf(firstCard.id to SelfAssessment.KNOWN), state(scenario).assessments)
            assertEquals(2, state(scenario).position)
            // Swiping left on CardFace.BACK marks REVIEW and completes round
            flip(scenario)
            swipeCard(scenario, left = true)
            assertEquals(LearningScreen.SUMMARY, state(scenario).screen)
            assertEquals(
                mapOf(firstCard.id to SelfAssessment.KNOWN, secondCard.id to SelfAssessment.REVIEW),
                state(scenario).assessments
            )
            scenario.onActivity { activity ->
                assertTrue(activity.findViewById<View>(R.id.learning_donut_chart).isShown)
                assertNotNull(activity.findViewById<View>(R.id.learning_confetti))
            }
        }
    }

    private fun swipeCard(scenario: ActivityScenario<LearningGameActivity>, left: Boolean) {
        scenario.onActivity { activity ->
            val card = activity.findViewById<View>(R.id.learning_card)
            val downTime = android.os.SystemClock.uptimeMillis()
            val startX = card.width / 2f
            val endX = if (left) startX - card.width * 0.45f else startX + card.width * 0.45f
            val y = card.height / 2f
            listOf(
                android.view.MotionEvent.ACTION_DOWN to startX,
                android.view.MotionEvent.ACTION_MOVE to (startX + endX) / 2f,
                android.view.MotionEvent.ACTION_MOVE to endX,
                android.view.MotionEvent.ACTION_UP to endX
            ).forEachIndexed { idx, (action, x) ->
                val ev = android.view.MotionEvent.obtain(downTime, downTime + idx * 16L, action, x, y, 0)
                card.dispatchTouchEvent(ev)
                ev.recycle()
            }
        }
    }

    @Test
    fun questionsFlipBackAndAssessOnlyOnceThenReviewTheSameCards() = launch().use { scenario ->
        start(scenario, R.id.learning_questions)
        val chosen = state(scenario).cards
        scenario.onActivity { activity ->
            val card = activity.findViewById<View>(R.id.learning_card)
            assertTrue(card.contentDescription.contains((chosen.first() as LearningCard.Question).question))
            assertFalse(card.contentDescription.contains((chosen.first() as LearningCard.Question).answer))
            assertEquals(View.INVISIBLE, activity.findViewById<View>(R.id.learning_assessment).visibility)
        }
        flip(scenario)
        flip(scenario)
        assertEquals(CardFace.FRONT, state(scenario).face)
        assertTrue(state(scenario).assessments.isEmpty())
        repeat(10) { index ->
            flip(scenario)
            scenario.onActivity { activity ->
                val button = activity.findViewById<View>(if (index < 7) R.id.learning_known else R.id.learning_mark)
                button.performClick()
                button.performClick()
            }
            assertEquals(index + 1, state(scenario).assessments.size)
        }
        assertEquals(LearningSummary(10, 7, 3), state(scenario).summary)
        assertRecreated(scenario)
        touch(scenario, R.id.learning_review)
        awaitCard(scenario)
        assertEquals(chosen.takeLast(3), state(scenario).cards)
        assertEquals(LearningPass.REVIEW, state(scenario).pass)
        assertRecreated(scenario)
        repeat(3) { index ->
            flip(scenario)
            tap(scenario, if (index < 2) R.id.learning_known else R.id.learning_mark)
        }
        assertEquals(LearningSummary(3, 2, 1), state(scenario).summary)
        tap(scenario, R.id.learning_review)
        awaitCard(scenario)
        assertEquals(chosen.takeLast(1), state(scenario).cards)
        flip(scenario)
        tap(scenario, R.id.learning_known)
        assertEquals(LearningSummary(1, 1, 0), state(scenario).summary)
        scenario.onActivity { assertEquals(View.GONE, it.findViewById<View>(R.id.learning_review).visibility) }
        tap(scenario, R.id.learning_again)
        awaitCard(scenario)
        assertEquals(10, state(scenario).total)
        assertTrue(state(scenario).assessments.isEmpty())
    }

    @Test
    fun recreationRetainsRevealedCardPartialRoundAndBackgroundState() = launch().use { scenario ->
        start(scenario, R.id.learning_questions)
        flip(scenario)
        assertRecreated(scenario)
        tap(scenario, R.id.learning_mark)
        flip(scenario)
        assertRecreated(scenario)
        val before = state(scenario)
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        awaitCard(scenario)
        assertEquals(before, state(scenario))
        assertEquals(1, state(scenario).assessments.size)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun latePhotoCallbacksAndOldActionsCannotChangeTheNextCard(): Unit = launch().use { scenario ->
        start(scenario, R.id.learning_species)
        val oldToken = state(scenario).cardToken!!
        lateinit var listener: com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>
        lateinit var target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>
        lateinit var oldImage: android.graphics.drawable.Drawable
        scenario.onActivity { activity ->
            val image = activity.findViewById<android.widget.ImageView>(R.id.learning_photo)
            oldImage = requireNotNull(image.drawable)
            val request = requireNotNull(image.getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag))
            // Retain the real request's callbacks before Glide clears it on advance.
            listener = (request.javaClass.getDeclaredField("requestListeners").apply { isAccessible = true }
                .get(request) as List<com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>>).single()
            target = request.javaClass.getDeclaredField("target").apply { isAccessible = true }
                .get(request) as com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>
        }
        flip(scenario)
        tap(scenario, R.id.learning_known)
        awaitCard(scenario)
        val next = state(scenario)
        var nextImage: android.graphics.drawable.Drawable? = null
        scenario.onActivity { activity ->
            nextImage = activity.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable
            listener.onLoadFailed(null, null, target, false)
            listener.onResourceReady(oldImage, "old", target, com.bumptech.glide.load.DataSource.LOCAL, false)
            LearningGameActivity::class.java.getDeclaredMethod("flip", java.lang.Long.TYPE)
                .apply { isAccessible = true }.invoke(activity, oldToken)
            LearningGameActivity::class.java.getDeclaredMethod("assess", java.lang.Long.TYPE, SelfAssessment::class.java)
                .apply { isAccessible = true }.invoke(activity, oldToken, SelfAssessment.REVIEW)
        }
        instrumentation.waitForIdleSync()
        assertEquals(next, state(scenario))
        scenario.onActivity {
            assertSame(nextImage, it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable)
            assertTrue(it.findViewById<View>(R.id.learning_card).isEnabled)
        }
    }

    @Test
    fun cardVisualMatrixReservesBothFacesAndKeepsHiddenAnswersInaccessible() {
        val oldFont = Settings.System.getString(instrumentation.targetContext.contentResolver, Settings.System.FONT_SCALE)
        val oldSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")?.trim()
        val rotation = listOf("accelerometer_rotation", "user_rotation")
            .associateWith { shell("settings get system $it").trim().takeUnless { value -> value == "null" } }
        val fixtures = listOf(
            LearningCard.Photo("wide", "wide", "Nombre común deliberadamente largo para comprobar varias líneas",
                "Vanellus chilensis", "learning/photos/photo-001.webp"),
            LearningCard.Photo("tall", "tall", "Nombre común deliberadamente largo para comprobar varias líneas",
                "Spatula versicolor", "learning/photos/photo-002.webp"),
            questions(1).single().copy(clarification = "Una aclaración extensa que debe mantenerse completa y legible al volver a revelar la tarjeta.")
        )
        try {
            listOf(false, true).forEach { small ->
                shell(if (small) "wm size 720x1280" else if (oldSize == null) "wm size reset" else "wm size $oldSize")
                shell("settings put system font_scale ${if (small) "2.0" else "1.0"}")
                fixtures.forEach { fixture ->
                    launch(listOf(fixture)).use { scenario ->
                        start(scenario, if (fixture is LearningCard.Photo) R.id.learning_species else R.id.learning_questions)
                        listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                            scenario.onActivity { it.delegate.localNightMode = theme }
                            await(scenario) { it.resources.configuration.uiMode and 48 == if (theme == 2) 32 else 16 }
                            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                                scenario.onActivity { it.requestedOrientation = orientation }
                                await(scenario) { it.resources.configuration.orientation == if (orientation == 1) 1 else 2 }
                                awaitCard(scenario)
                                if (state(scenario).face == CardFace.BACK) flip(scenario)
                                val prefix = "t2-${fixture.id}-${if (small) "small-200" else "normal"}-$theme-$orientation"
                                val before = state(scenario)
                                val front = cardGeometry(scenario)
                                assertFrontTreeHidesAnswer(fixture)
                                capture(scenario, "$prefix-front")
                                if (small) captureReading(scenario, "$prefix-front", listOf(R.id.learning_photo, R.id.learning_card_text))
                                tap(scenario, R.id.learning_reveal)
                                awaitCard(scenario)
                                assertEquals(CardFace.BACK, state(scenario).face)
                                val back = cardGeometry(scenario)
                                scenario.onActivity { activity ->
                                    val tolerance = 2 * activity.resources.displayMetrics.density
                                    front.zip(back).forEach { (a, b) ->
                                        assertTrue("Stable card geometry: $a / $b", listOf(a.left - b.left, a.top - b.top,
                                            a.right - b.right, a.bottom - b.bottom).all { kotlin.math.abs(it) <= tolerance })
                                    }
                                    val description = activity.findViewById<View>(R.id.learning_card).contentDescription.toString()
                                    if (fixture is LearningCard.Photo) {
                                        assertTrue(description.contains(fixture.commonName, ignoreCase = true))
                                        val image = activity.findViewById<android.widget.ImageView>(R.id.learning_photo)
                                        assertTrue(image.isShown)
                                        assertTrue(image.height >= activity.resources.getDimensionPixelSize(R.dimen.learning_photo_min_height))
                                        val drawable = requireNotNull(image.drawable)
                                        assertEquals(fixture.id == "wide", drawable.intrinsicWidth > drawable.intrinsicHeight)
                                        val drawn = android.graphics.RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
                                        image.imageMatrix.mapRect(drawn)
                                        assertTrue("Photo covers the card", drawn.left <= 1 && drawn.top <= 1 &&
                                            drawn.right >= image.width - 1 && drawn.bottom >= image.height - 1)
                                        val detail = activity.findViewById<TextView>(R.id.learning_card_detail)
                                        assertTrue(detail.typeface.isItalic)
                                        assertEquals(fixture.scientificName, detail.text.toString())
                                    } else {
                                        assertFalse(activity.findViewById<View>(R.id.learning_photo).isShown)
                                        assertTrue(description.contains((fixture as LearningCard.Question).clarification!!))
                                    }
                                }
                                capture(scenario, "$prefix-back")
                                if (small) captureReading(scenario, "$prefix-back", listOf(R.id.learning_photo, R.id.learning_back_text, R.id.learning_card_detail))
                                reachable(scenario, R.id.learning_mark)
                                capture(scenario, "$prefix-back-actions")
                                flip(scenario)
                                assertEquals(CardFace.FRONT, state(scenario).face)
                                assertFrontTreeHidesAnswer(fixture)
                                tap(scenario, R.id.learning_reveal)
                                awaitCard(scenario)
                                assertEquals(before.copy(face = CardFace.BACK), state(scenario))
                            }
                        }
                        touch(scenario, R.id.learning_mark)
                        assertEquals(LearningSummary(1, 0, 1), state(scenario).summary)
                        tap(scenario, R.id.learning_review)
                        awaitCard(scenario)
                        assertEquals(LearningPass.REVIEW, state(scenario).pass)
                        assertEquals(fixture, state(scenario).currentCard)
                        assertFrontTreeHidesAnswer(fixture)
                        touch(scenario, R.id.learning_reveal)
                        awaitCard(scenario)
                        assertEquals(CardFace.BACK, state(scenario).face)
                        scenario.onActivity {
                            assertEquals(fixture is LearningCard.Photo, it.findViewById<View>(R.id.learning_photo).isShown)
                        }
                        capture(scenario, "t2-${fixture.id}-${if (small) "200" else "100"}-review")
                        touch(scenario, R.id.learning_known)
                        assertEquals(LearningSummary(1, 1, 0), state(scenario).summary)
                    }
                }
            }
        } finally {
            shell(if (oldSize == null) "wm size reset" else "wm size $oldSize")
            restoreSetting("system", "font_scale", oldFont)
            rotation.forEach { (key, value) -> restoreSetting("system", key, value) }
        }
    }

    private fun cardGeometry(scenario: ActivityScenario<LearningGameActivity>): List<android.graphics.Rect> {
        scenario.onActivity { it.findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll).scrollTo(0, 0) }
        await(scenario) { !it.findViewById<View>(R.id.learning_content).isLayoutRequested }
        var result = emptyList<android.graphics.Rect>()
        scenario.onActivity { activity ->
            result = listOf(R.id.learning_photo, R.id.learning_card, R.id.learning_round, R.id.learning_actions).map { id ->
                val view = activity.findViewById<View>(id)
                val position = IntArray(2)
                view.getLocationOnScreen(position)
                android.graphics.Rect(position[0], position[1], position[0] + view.width, position[1] + view.height)
            }
        }
        return result
    }

    private fun captureReading(scenario: ActivityScenario<LearningGameActivity>, prefix: String, ids: List<Int>) {
        ids.forEach { id ->
            var height = 0
            var step = 0
            var label = ""
            scenario.onActivity { activity ->
                val view = activity.findViewById<View>(id)
                if (view.isShown) {
                    height = view.height
                    step = activity.findViewById<View>(R.id.learning_scroll).height / 2
                    label = activity.resources.getResourceEntryName(id)
                    if (view is TextView) {
                        assertTrue("All text is laid out", view.layout.height <= view.height - view.compoundPaddingTop - view.compoundPaddingBottom)
                        repeat(view.layout.lineCount) { assertEquals(0, view.layout.getEllipsisCount(it)) }
                    }
                }
            }
            if (height == 0) return@forEach
            check(step > 0)
            for (offset in 0 until height step step) {
                scenario.onActivity { activity ->
                    val view = activity.findViewById<View>(id)
                    view.requestRectangleOnScreen(android.graphics.Rect(0, offset, view.width, minOf(offset + step, height)), true)
                }
                capture(scenario, "$prefix-$label-$offset")
            }
        }
    }

    private fun assertFrontTreeHidesAnswer(fixture: LearningCard) {
        if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
        val nodes = accessibleNodes(requireNotNull(automation.rootInActiveWindow))
        val hidden = when (fixture) {
            is LearningCard.Photo -> listOf(fixture.commonName, fixture.scientificName, fixture.imagePath)
            is LearningCard.Question -> listOfNotNull(fixture.answer, fixture.clarification)
        }
        hidden.forEach { answer -> assertFalse("Hidden answer: $answer", nodes.any {
            it.text?.contains(answer) == true || it.contentDescription?.contains(answer) == true
        }) }
        assertFalse(nodes.any { it.viewIdResourceName?.substringAfter(":id/") in
            listOf("learning_known", "learning_mark", "learning_front", "learning_bird_info") })
    }

    @Test
    fun photoRemainsVisibleAndStableAcrossReversibleFlipsWithoutAssessment(): Unit = launch().use { scenario ->
        start(scenario, R.id.learning_species)
        val before = state(scenario)
        var drawable: android.graphics.drawable.Drawable? = null
        var bounds = emptyList<android.graphics.Rect>()
        fun geometry(activity: LearningGameActivity) = listOf(R.id.learning_photo, R.id.learning_round).map { id ->
            val view = activity.findViewById<View>(id)
            val point = IntArray(2)
            view.getLocationOnScreen(point)
            android.graphics.Rect(point[0], point[1], point[0] + view.width, point[1] + view.height)
        }
        scenario.onActivity {
            drawable = it.findViewById<android.widget.ImageView>(R.id.learning_photo).drawable
            bounds = geometry(it)
        }
        repeat(3) {
            flip(scenario)
            scenario.onActivity { activity ->
                val image = activity.findViewById<android.widget.ImageView>(R.id.learning_photo)
                assertEquals("Shared photo stays visible on both faces", View.VISIBLE, image.visibility)
                assertSame(drawable, image.drawable)
                assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER, image.scaleType)
                geometry(activity).zip(bounds).forEach { (actual, expected) ->
                    val tolerance = 2 * activity.resources.displayMetrics.density
                    assertTrue("Stable bounds: $expected / $actual", listOf(actual.left - expected.left,
                        actual.top - expected.top, actual.right - expected.right, actual.bottom - expected.bottom)
                        .all { kotlin.math.abs(it) <= tolerance })
                }
            }
            assertEquals(before.currentCard, state(scenario).currentCard)
            assertEquals(before.cardToken, state(scenario).cardToken)
            assertEquals(before.position, state(scenario).position)
            assertTrue(state(scenario).assessments.isEmpty())
        }
    }

    @Test
    fun speciesFrontAccessibilityDoesNotExposeAnswerAndBackDoes() = launch().use { scenario ->
        start(scenario, R.id.learning_species)
        val photo = state(scenario).currentCard as LearningCard.Photo
        scenario.onActivity { activity ->
            val card = activity.findViewById<View>(R.id.learning_card)
            val info = card.createAccessibilityNodeInfo()
            assertFalse(info.contentDescription.contains(photo.commonName))
            assertFalse(info.contentDescription.contains(photo.scientificName))
            assertFalse(info.contentDescription.contains(photo.imagePath))
            assertEquals(0, info.childCount)
            assertEquals("¿Qué especie es?", activity.findViewById<TextView>(R.id.learning_card_text).text.toString())
            assertFalse(activity.findViewById<View>(R.id.learning_card_detail).isShown)
            assertTrue(card.performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null))
        }
        awaitCard(scenario)
        assertEquals(CardFace.BACK, state(scenario).face)
        scenario.onActivity { activity ->
            val description = activity.findViewById<View>(R.id.learning_card).createAccessibilityNodeInfo().contentDescription.toString()
            assertTrue(description.contains(photo.commonName, ignoreCase = true))
            assertTrue(description.contains(photo.scientificName))
            val displayedName = activity.findViewById<TextView>(R.id.learning_back_text).text.toString()
            assertTrue(displayedName.split(" ").all { it.isNotEmpty() && it[0].isUpperCase() })
        }
        assertRecreated(scenario)
        tap(scenario, R.id.learning_known)
        assertEquals(1, state(scenario).assessments.size)
    }

    @Test
    fun talkBackServiceCanRevealSpeciesWithoutAnAnswerInTheFrontTree() {
        val resolver = instrumentation.targetContext.contentResolver
        val oldServices = Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val oldEnabled = Settings.Secure.getString(resolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        val manager = instrumentation.targetContext.getSystemService(android.view.accessibility.AccessibilityManager::class.java)
        val service = manager.installedAccessibilityServiceList.firstOrNull {
            it.id.contains("com.google.android.marvin.talkback/")
        }
        org.junit.Assume.assumeNotNull(service)
        automationFlags = android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
        try {
            val enabledServices = oldServices.orEmpty().split(':').filter { it.isNotEmpty() }.toMutableSet()
            enabledServices.add(service!!.id)
            shell("settings put secure enabled_accessibility_services ${enabledServices.joinToString(":")}")
            shell("settings put secure accessibility_enabled 1")
            val serviceInfo = automation.serviceInfo
            serviceInfo.flags = (serviceInfo.flags and android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS.inv()) or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            automation.serviceInfo = serviceInfo
            launch().use { scenario ->
                start(scenario, R.id.learning_species)
                await(scenario) { manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN)
                    .any { it.id == service.id } }
                val photo = state(scenario).currentCard as LearningCard.Photo
                val front = accessibilityCard("¿Qué especie es?")
                capture(scenario, "talkback-species-front")
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                val frontNodes = accessibleNodes(requireNotNull(automation.rootInActiveWindow))
                val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "t6-captures").apply { mkdirs() }
                val frontTree = File(folder, "talkback-front-tree.txt")
                frontTree.writeText(frontNodes.joinToString("\n") { "${it.className}: ${it.text} | ${it.contentDescription}" })
                preserveCapture(frontTree)
                val services = File(folder, "talkback-services.txt")
                services.writeText(shell("dumpsys accessibility"))
                preserveCapture(services)
                assertEquals(0, front.childCount)
                listOf(photo.commonName, photo.scientificName, photo.imagePath).forEach { hidden ->
                    assertFalse(frontNodes.any { it.contentDescription?.contains(hidden) == true || it.text?.contains(hidden) == true })
                }
                assertTrue(front.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                awaitCard(scenario)
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                val back = accessibilityCard(photo.scientificName)
                assertTrue(back.contentDescription.toString().contains(photo.commonName, ignoreCase = true))
                assertTrue(back.contentDescription.contains(photo.scientificName))
                capture(scenario, "talkback-species-back")
                val before = state(scenario)
                reachable(scenario, R.id.learning_bird_info)
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                val info = accessibleNodes(requireNotNull(automation.rootInActiveWindow)).single {
                    it.viewIdResourceName?.endsWith(":id/learning_bird_info") == true
                }
                assertEquals("Ver información del ave", info.contentDescription.toString())
                assertTrue(info.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS))
                assertTrue(info.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                val bird = foreground(BirdInfoActivity::class.java)
                waitUi { bird.findViewById<View>(R.id.imageDistribution).isEnabled ||
                    bird.findViewById<View>(R.id.distributionUnavailable).isShown }
                captureActivity(bird, "t4-talkback-info")
                this@LearningGameActivityTest.back(bird)
                foreground(LearningGameActivity::class.java)
                awaitCard(scenario)
                assertEquals(before, state(scenario))
                flip(scenario)
                assertFrontTreeHidesAnswer(photo)
                assertTrue(accessibilityCard("¿Qué especie es?").performAction(AccessibilityNodeInfo.ACTION_CLICK))
                awaitCard(scenario)
                reachable(scenario, R.id.learning_known)
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                val known = accessibleNodes(requireNotNull(automation.rootInActiveWindow)).single {
                    it.viewIdResourceName?.endsWith(":id/learning_known") == true
                }
                assertTrue(known.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                assertEquals(before.assessments.size + 1, state(scenario).assessments.size)
            }
        } finally {
            shell(if (oldServices == null) "settings delete secure enabled_accessibility_services" else
                "settings put secure enabled_accessibility_services ${oldServices.ifEmpty { "''" }}")
            shell(if (oldEnabled == null) "settings delete secure accessibility_enabled" else
                "settings put secure accessibility_enabled $oldEnabled")
            automationFlags = 0
        }
    }

    @Test
    fun backDiscardsRoundAndSummaryAndSelectorFinishes() = launch(questions(1)).use { scenario ->
        start(scenario, R.id.learning_questions)
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        assertEquals(LearningState(), state(scenario))
        start(scenario, R.id.learning_questions)
        flip(scenario)
        tap(scenario, R.id.learning_mark)
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        assertEquals(LearningState(), state(scenario))
        assertFinishes(scenario) { it.onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun summaryActionsDiscardSessionAndExitFinishes() = launch(questions(1)).use { scenario ->
        start(scenario, R.id.learning_questions)
        flip(scenario)
        tap(scenario, R.id.learning_known)
        tap(scenario, R.id.learning_choose_mode)
        assertEquals(LearningState(), state(scenario))
        start(scenario, R.id.learning_questions)
        flip(scenario)
        tap(scenario, R.id.learning_known)
        assertFinishes(scenario) { it.findViewById<View>(R.id.learning_exit).performClick() }
    }

    @Test
    fun insufficientAndEmptyContentRemainNavigable(): Unit = launch(questions(1)).use { scenario ->
        scenario.onActivity { activity ->
            assertFalse(activity.findViewById<View>(R.id.learning_species).isEnabled)
            assertTrue(activity.findViewById<TextView>(R.id.learning_questions_availability).text.contains("1"))
            assertTrue(activity.findViewById<TextView>(R.id.learning_species_availability).text.isNotEmpty())
        }
        start(scenario, R.id.learning_questions)
        assertEquals(1, state(scenario).total)
        scenario.onActivity { assertEquals(View.VISIBLE, it.findViewById<View>(R.id.learning_notice).visibility) }
    }

    @Test
    fun emptyCatalogDisablesBothModesAndAllowsBack() = launch(emptyList()).use { scenario ->
        scenario.onActivity { activity ->
            assertFalse(activity.findViewById<View>(R.id.learning_questions).isEnabled)
            assertFalse(activity.findViewById<View>(R.id.learning_species).isEnabled)
        }
        assertFinishes(scenario) { it.onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun enlargedTextAndLandscapeKeepAllActionsReachable() {
        val oldScale = Settings.System.getString(instrumentation.targetContext.contentResolver, Settings.System.FONT_SCALE)
        try {
            shell("settings put system font_scale 2.0")
            launch(questions(1)).use { scenario ->
                await(scenario) { it.resources.configuration.fontScale == 2f }
                capture(scenario, "large-selector")
                reachable(scenario, R.id.learning_species)
                touch(scenario, R.id.learning_questions)
                awaitCard(scenario)
                capture(scenario, "large-question-front")
                flip(scenario)
                reachable(scenario, R.id.learning_mark)
                capture(scenario, "large-question-back-actions")
                lateinit var portraitActivity: LearningGameActivity
                scenario.onActivity { portraitActivity = it; it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                await(scenario) { it !== portraitActivity &&
                    it.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE }
                awaitCard(scenario)
                assertEquals(CardFace.BACK, state(scenario).face)
                reachable(scenario, R.id.learning_mark)
                capture(scenario, "large-landscape-actions")
                touch(scenario, R.id.learning_known)
                reachable(scenario, R.id.learning_exit)
                capture(scenario, "large-landscape-summary-actions")
                lateinit var lightActivity: LearningGameActivity
                scenario.onActivity { lightActivity = it; it.delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES }
                await(scenario) { it !== lightActivity &&
                    it.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES }
                assertEquals(LearningSummary(1, 1, 0), state(scenario).summary)
                reachable(scenario, R.id.learning_again)
                capture(scenario, "large-dark-summary")
            }
        } finally {
            shell(if (oldScale == null) "settings delete system font_scale" else "settings put system font_scale $oldScale")
        }
    }

    @Test
    fun captureBothModesAndSummaryForVisualReview() = launch().use { scenario ->
        capture(scenario, "selector")
        start(scenario, R.id.learning_species)
        capture(scenario, "species-front")
        flip(scenario)
        capture(scenario, "species-back")
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        start(scenario, R.id.learning_questions)
        capture(scenario, "question-front")
        flip(scenario)
        capture(scenario, "question-back")
        repeat(10) { index ->
            if (index != 0) flip(scenario)
            tap(scenario, if (index < 7) R.id.learning_known else R.id.learning_mark)
        }
        capture(scenario, "summary")
    }

    @Test
    fun failedPhotoIsSkippedWithoutAssessmentOrFictitiousSummary(): Unit = launch(listOf(
        LearningCard.Photo("broken", "test_species", "Nombre oculto", "Test species", "learning/photos/missing.webp")
    )).use { scenario ->
        tap(scenario, R.id.learning_species)
        await(scenario) { vm(it).state.value.session.notice == LearningNotice.UNAVAILABLE_CONTENT }
        assertEquals(LearningScreen.SELECTOR, state(scenario).screen)
        assertTrue(state(scenario).assessments.isEmpty())
        assertNull(state(scenario).summary)
        scenario.onActivity {
            assertFalse(it.findViewById<View>(R.id.learning_species).isEnabled)
            assertEquals(View.VISIBLE, it.findViewById<View>(R.id.learning_notice).visibility)
        }
    }

    @Test
    fun interruptedFlipRestoresConfirmedFaceWithoutAssessment() = animatorScale("10") {
        launch().use { scenario ->
            start(scenario, R.id.learning_questions)
            scenario.onActivity { activity ->
                val card = activity.findViewById<View>(R.id.learning_card)
                val frontText = activity.findViewById<TextView>(R.id.learning_card_text).text.toString()
                card.performClick()
                card.performClick()
                activity.findViewById<View>(R.id.learning_known).performClick()
                assertEquals(CardFace.BACK, vm(activity).state.value.session.face)
                assertFalse(card.isEnabled)
                assertEquals("Content changes only at the edge of the flip", frontText,
                    activity.findViewById<TextView>(R.id.learning_card_text).text.toString())
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitCard(scenario)
            assertEquals(CardFace.BACK, state(scenario).face)
            assertTrue(state(scenario).assessments.isEmpty())
            scenario.onActivity {
                assertEquals(0f, it.findViewById<View>(R.id.learning_card).rotationY)
                assertEquals(View.VISIBLE, it.findViewById<View>(R.id.learning_back_panel).visibility)
                assertEquals((vm(it).state.value.session.currentCard as LearningCard.Question).answer,
                    it.findViewById<TextView>(R.id.learning_back_text).text.toString())
            }
            scenario.onActivity { it.findViewById<View>(R.id.learning_card).performClick() }
            scenario.recreate()
            awaitCard(scenario)
            assertEquals(CardFace.FRONT, state(scenario).face)
            assertTrue(state(scenario).assessments.isEmpty())
        }
    }

    @Test
    fun disabledAnimationsRevealImmediatelyAndControlsHaveMinimumTouchArea() = animatorScale("0") {
        launch().use { scenario ->
            start(scenario, R.id.learning_questions)
            scenario.onActivity { activity ->
                val card = activity.findViewById<View>(R.id.learning_card)
                card.performClick()
                assertEquals(CardFace.BACK, vm(activity).state.value.session.face)
                assertTrue(card.isEnabled)
                assertEquals(0f, card.rotationY)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.learning_assessment).visibility)
            }
            await(scenario) { it.findViewById<View>(R.id.learning_known).height > 0 }
            scenario.onActivity { activity ->
                val minimum = 48 * activity.resources.displayMetrics.density
                listOf(R.id.learning_card, R.id.learning_reveal, R.id.learning_known, R.id.learning_mark,
                    R.id.learning_questions, R.id.learning_species).forEach { id ->
                    val view = activity.findViewById<View>(id)
                    assertTrue("touch width $id", view.width >= minimum)
                    assertTrue("touch height $id", view.height >= minimum)
                }
            }
        }
    }

    private fun launch(content: List<LearningCard>? = null, loadingGate: CountDownLatch? = null): ActivityScenario<LearningGameActivity> {
        val application = instrumentation.targetContext.applicationContext as Application
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, state: Bundle?) {
                if (activity is LearningGameActivity && content != null) {
                    ViewModelProvider(activity, object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            LearningGameViewModel(application) {
                                check(loadingGate?.await(30, TimeUnit.SECONDS) != false)
                                content
                            } as T
                    })[LearningGameViewModel::class.java]
                }
            }
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        application.registerActivityLifecycleCallbacks(callback)
        val scenario = try {
            ActivityScenario.launch<LearningGameActivity>(Intent(application, LearningGameActivity::class.java))
        } finally {
            application.unregisterActivityLifecycleCallbacks(callback)
        }
        if (loadingGate != null) return scenario
        val model = model(scenario)
        runBlocking { withTimeout(10_000) { model.state.first { !it.isLoading } } }
        await(scenario) { it.findViewById<View>(R.id.learning_questions).isEnabled ||
            it.findViewById<View>(R.id.learning_species).isEnabled || content?.isEmpty() == true }
        return scenario
    }

    private fun vm(activity: Activity) = ViewModelProvider(activity as LearningGameActivity)[LearningGameViewModel::class.java]
    private fun model(scenario: ActivityScenario<LearningGameActivity>): LearningGameViewModel {
        lateinit var model: LearningGameViewModel
        scenario.onActivity { model = vm(it) }
        return model
    }
    private fun state(scenario: ActivityScenario<LearningGameActivity>) = model(scenario).state.value.session
    private fun tap(scenario: ActivityScenario<LearningGameActivity>, id: Int) {
        scenario.onActivity { it.findViewById<View>(id).performClick() }
    }
    private fun start(scenario: ActivityScenario<LearningGameActivity>, id: Int) {
        tap(scenario, id)
        awaitCard(scenario)
    }
    private fun flip(scenario: ActivityScenario<LearningGameActivity>) {
        awaitCard(scenario)
        tap(scenario, R.id.learning_card)
        awaitCard(scenario)
    }
    private fun awaitCard(scenario: ActivityScenario<LearningGameActivity>) = await(scenario) {
        it.findViewById<View>(R.id.learning_round).visibility == View.VISIBLE &&
            it.findViewById<View>(R.id.learning_card).isEnabled
    }
    private fun await(scenario: ActivityScenario<LearningGameActivity>, predicate: (LearningGameActivity) -> Boolean) {
        val ready = CountDownLatch(1)
        scenario.onActivity { activity ->
            val root = activity.findViewById<View>(android.R.id.content)
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            root.post(object : Runnable {
                override fun run() {
                    if (predicate(activity)) ready.countDown()
                    else if (System.nanoTime() < deadline) root.postOnAnimation(this)
                }
            })
        }
        assertTrue("UI did not reach expected state", ready.await(12, TimeUnit.SECONDS))
        instrumentation.waitForIdleSync()
    }
    private fun assertRecreated(scenario: ActivityScenario<LearningGameActivity>) {
        val before = state(scenario)
        val previous = model(scenario)
        scenario.recreate()
        await(scenario) { vm(it).state.value.session == before }
        assertSame(previous, model(scenario))
        assertEquals(before, state(scenario))
        if (before.screen == LearningScreen.CARD) awaitCard(scenario)
    }
    private fun assertFinishes(scenario: ActivityScenario<LearningGameActivity>, action: (LearningGameActivity) -> Unit) {
        val destroyed = CountDownLatch(1)
        scenario.onActivity { activity ->
            activity.lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) destroyed.countDown()
            })
            action(activity)
            assertTrue(activity.isFinishing)
        }
        assertTrue("Activity did not finish", destroyed.await(30, TimeUnit.SECONDS))
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }
    private fun questions(total: Int) = (1..total).map {
        LearningCard.Question("q$it", "Tema", "Inicial", "¿Pregunta $it?", "Respuesta $it", "Aclaración $it")
    }
    private fun animatorScale(value: String, block: () -> Unit) {
        val old = Settings.Global.getString(instrumentation.targetContext.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
        fun set(scale: String?) {
            val command = if (scale == null) "settings delete global animator_duration_scale" else
                "settings put global animator_duration_scale $scale"
            automation.executeShellCommand(command).use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).readBytes()
            }
            instrumentation.waitForIdleSync()
        }
        try { set(value); block() } finally { set(old) }
    }

    private fun shell(command: String): String {
        val output = automation.executeShellCommand(command).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).readBytes().toString(Charsets.UTF_8)
        }
        instrumentation.waitForIdleSync()
        return output
    }

    private fun capture(scenario: ActivityScenario<LearningGameActivity>, name: String) {
        instrumentation.waitForIdleSync()
        // Igual que captureActivity: dos cuadros evitan guardar el scroll o la cara anterior.
        repeat(2) {
            val frame = CountDownLatch(1)
            scenario.onActivity { activity ->
                val view = activity.window.decorView
                if (android.os.Build.VERSION.SDK_INT >= 29 && view.isHardwareAccelerated) {
                    view.viewTreeObserver.registerFrameCommitCallback { frame.countDown() }
                    view.invalidate()
                } else view.postOnAnimation { view.postOnAnimation { frame.countDown() } }
            }
            assertTrue("Frame was not committed", frame.await(10, TimeUnit.SECONDS))
        }
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "t6-captures").apply { mkdirs() }
        val bitmap = automation.takeScreenshot()
        requireNotNull(bitmap) { "Screenshot unavailable" }
        val output = File(folder, "$name.png")
        output.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        preserveCapture(output)
    }

    private fun preserveCapture(file: File) {
        shell("mkdir -p /data/local/tmp/aves-t6-captures")
        shell("cp ${file.absolutePath} /data/local/tmp/aves-t6-captures/${file.name}")
    }

    private fun accessibleNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> = buildList {
        add(root)
        for (index in 0 until root.childCount) root.getChild(index)?.let { addAll(accessibleNodes(it)) }
    }

    private fun accessibilityCard(description: String): AccessibilityNodeInfo {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (System.nanoTime() < deadline) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            val nodes = automation.rootInActiveWindow?.let(::accessibleNodes).orEmpty()
            // La primera activación de TalkBack puede abrir su aviso de notificaciones.
            val deny = nodes.firstOrNull { it.viewIdResourceName?.let { id ->
                id.endsWith(":id/permission_deny_button") || id.endsWith(":id/permission_deny_and_dont_ask_again_button") } == true }
            if (deny != null) {
                deny.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else nodes.firstOrNull { it.isClickable && it.contentDescription?.contains(description) == true }?.let { return it }
            try { automation.waitForIdle(100, 1000) } catch (_: java.util.concurrent.TimeoutException) { }
        }
        val diagnostic = File(instrumentation.targetContext.getExternalFilesDir(null), "talkback-unavailable-tree.txt")
        diagnostic.writeText(automation.rootInActiveWindow?.let(::accessibleNodes).orEmpty()
            .joinToString("\n") { "${it.viewIdResourceName}: ${it.text} | ${it.contentDescription}" })
        preserveCapture(diagnostic)
        throw AssertionError("Accessible card unavailable: $description")
    }

    private fun reachable(scenario: ActivityScenario<LearningGameActivity>, id: Int): android.graphics.Rect {
        scenario.onActivity { activity ->
            val view = activity.findViewById<View>(id)
            view.requestRectangleOnScreen(android.graphics.Rect(0, 0, view.width, view.height), true)
        }
        instrumentation.waitForIdleSync()
        val visible = android.graphics.Rect()
        scenario.onActivity { activity ->
            val view = activity.findViewById<View>(id)
            assertTrue(view.isShown)
            assertTrue(view.getGlobalVisibleRect(visible))
            assertEquals("Whole action is reachable", view.height, visible.height())
        }
        return visible
    }

    private fun touch(scenario: ActivityScenario<LearningGameActivity>, id: Int) {
        val visible = reachable(scenario, id)
        val time = android.os.SystemClock.uptimeMillis()
        listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP).forEach { action ->
            val event = android.view.MotionEvent.obtain(time, android.os.SystemClock.uptimeMillis(), action,
                visible.exactCenterX(), visible.exactCenterY(), 0)
            event.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
            assertTrue(automation.injectInputEvent(event, true))
            event.recycle()
        }
        instrumentation.waitForIdleSync()
    }
}
