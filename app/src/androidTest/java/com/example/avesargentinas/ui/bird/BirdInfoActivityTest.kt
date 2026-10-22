package com.example.avesargentinas.ui.bird

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.bumptech.glide.Glide
import com.example.avesargentinas.R
import com.example.avesargentinas.MainActivity
import com.example.avesargentinas.Prediction
import com.example.avesargentinas.ui.log.ImageZoomActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BirdInfoActivityTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test fun backControlHasAnAccessibleTouchTarget() {
        launch("Vanellus chilensis").use { scenario ->
            await(scenario) { image(it).isEnabled }
            scenario.onActivity {
                val back = it.findViewById<View>(R.id.btnBackBirdInfo)
                val minimum = 48 * it.resources.displayMetrics.density
                assertTrue("Back target must be at least 48dp in both dimensions", back.width >= minimum && back.height >= minimum)
            }
        }
    }

    @Test fun systemBarIconsFollowTheBirdInfoTheme() {
        launch("Vanellus chilensis").use { scenario ->
            listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                scenario.onActivity { it.delegate.localNightMode = theme }
                await(scenario) { it.resources.configuration.uiMode and 48 == (if (theme == 2) 32 else 16) && image(it).isEnabled }
                scenario.onActivity {
                    val bars = androidx.core.view.WindowCompat.getInsetsController(it.window, it.window.decorView)
                    assertEquals("Status icons must contrast with the surface",
                        theme == AppCompatDelegate.MODE_NIGHT_NO && android.os.Build.VERSION.SDK_INT >= 35, bars.isAppearanceLightStatusBars)
                    if (android.os.Build.VERSION.SDK_INT >= 26)
                        assertEquals(theme == AppCompatDelegate.MODE_NIGHT_NO, bars.isAppearanceLightNavigationBars)
                }
            }
        }
    }

    @Test fun smallLandscapeAtDoubleTextKeepsMapNoticeAndLegendReachable() {
        val oldFont = shell("settings get system font_scale").trim()
        val oldSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")?.trim()
        try {
            shell("wm size 720x1280")
            shell("settings put system font_scale 2.0")
            listOf("Vanellus chilensis", "Aramides ypecaha").forEach { name ->
                launch(name).use { scenario ->
                    listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                        scenario.onActivity { it.delegate.localNightMode = theme; it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                        await(scenario) { it.resources.configuration.orientation == 2 && it.resources.configuration.fontScale == 2f &&
                            it.resources.configuration.uiMode and 48 == (if (theme == 2) 32 else 16) && !it.window.decorView.isLayoutRequested }
                        scenario.onActivity { assertTrue("The map must retain a usable area", image(it).height >= 48 * it.resources.displayMetrics.density) }
                        await(scenario) { if (name.startsWith("Vanellus")) image(it).isEnabled else notice(it).isShown }
                        listOf(if (name.startsWith("Vanellus")) R.id.imageDistribution else R.id.distributionUnavailable, R.id.txtLegendDistribution).forEach { id ->
                            scenario.onActivity {
                                val view = it.findViewById<View>(id)
                                view.requestRectangleOnScreen(android.graphics.Rect(0, 0, view.width, view.height), true)
                            }
                            instrumentation.waitForIdleSync()
                            scenario.onActivity {
                                val view = it.findViewById<View>(id)
                                val rect = android.graphics.Rect()
                                assertTrue(view.getGlobalVisibleRect(rect))
                                assertEquals("Whole map, notice or legend is reachable", view.height, rect.height())
                                if (view is TextView) assertTrue(view.layout.height <= view.height - view.compoundPaddingTop - view.compoundPaddingBottom)
                            }
                            capture(scenario, "t6-small-${name.substringBefore(' ')}-$theme-$id")
                        }
                    }
                }
            }
        } finally {
            shell(if (oldSize == null) "wm size reset" else "wm size $oldSize")
            shell(if (oldFont == "null") "settings delete system font_scale" else "settings put system font_scale $oldFont")
        }
    }

    @Test fun loadingBlocksZoomUntilTheRealRequestSucceeds() {
        val application = context.applicationContext as Application
        var checkedLoading = false
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, state: Bundle?) {
                if (activity is BirdInfoActivity) Glide.with(activity).pauseRequests()
            }
            override fun onActivityStarted(activity: Activity) {
                if (activity is BirdInfoActivity && !checkedLoading) {
                    assertNull("No usable URI while loading", uri(activity))
                    assertFalse(image(activity).isEnabled)
                    assertFalse(image(activity).isClickable)
                    assertEquals(View.GONE, notice(activity).visibility)
                    image(activity).performClick()
                    checkedLoading = true
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
            launch("Vanellus chilensis").use { scenario ->
                assertTrue(checkedLoading)
                await(scenario) { image(it).isEnabled }
                verifyZoomAndReturn(scenario, resourceUri(R.drawable.vanellus_chilensis_dist))
            }
        } finally { application.unregisterActivityLifecycleCallbacks(callback) }
    }

    @Test fun originalNamesAndAllThreeAliasesOpenTheExpectedLocalMap() {
        listOf(
            "Vanellus chilensis" to R.drawable.vanellus_chilensis_dist,
            "Anas platalea" to R.drawable.anas_platalea_dist,
            "Anas versicolor" to R.drawable.spatula_versicolor_dist,
            "Anas cyanoptera" to R.drawable.spatula_cyanoptera_dist,
            "Anas sibilatrix" to R.drawable.mareca_sibilatrix_dist
        ).forEach { (name, resource) ->
            launch(name).use { scenario ->
                await(scenario) { image(it).isEnabled }
                scenario.onActivity {
                    assertEquals(resourceUri(resource), uri(it))
                    assertEquals(View.GONE, notice(it).visibility)
                    assertNotNull(image(it).drawable)
                }
                verifyZoomAndReturn(scenario, resourceUri(resource))
            }
        }
    }

    @Test fun explicitUriWinsOverAnExistingOriginalOrAlias() {
        listOf("Vanellus chilensis", "Anas versicolor").forEach { name ->
            val explicit = resourceUri(R.drawable.anas_platalea_dist)
            launch(name, explicit).use { scenario ->
                await(scenario) { image(it).isEnabled }
                verifyZoomAndReturn(scenario, explicit)
            }
        }
    }

    @Test fun identificationStillOpensThePredictedSpeciesAndReturns() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            dismissIdentificationPermissions()
            scenario.onActivity {
                // Seed only the classifier output; exercise the existing information button and intent.
                MainActivity::class.java.getDeclaredField("lastAnyPrediction").apply { isAccessible = true }
                    .set(it, Prediction(0, "Tero", 0.95f, "Vanellus chilensis", null, null))
                it.findViewById<View>(R.id.resultHeader).visibility = View.VISIBLE
                it.findViewById<View>(R.id.btnBirdInfo).apply { visibility = View.VISIBLE; performClick() }
            }
            waitUntil { (foreground() as? BirdInfoActivity)?.let { image(it).isEnabled } == true }
            instrumentation.runOnMainSync {
                val bird = foreground() as BirdInfoActivity
                assertEquals("Tero", bird.intent.getStringExtra(BirdInfoActivity.EXTRA_COMMON_NAME))
                assertEquals("Vanellus chilensis", bird.intent.getStringExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME))
                assertEquals(0.95f, bird.intent.getFloatExtra(BirdInfoActivity.EXTRA_CONFIDENCE, 0f), 0f)
                assertEquals(resourceUri(R.drawable.vanellus_chilensis_dist), uri(bird))
                bird.findViewById<View>(R.id.btnBackBirdInfo).performClick()
            }
            waitUntil { foreground() is MainActivity }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun missingSourcesAndFailedExplicitUrisKeepBackAndNeverOpenZoom() {
        val corrupt = File(context.cacheDir, "t3-undecodable.webp")
        val deleted = File(context.cacheDir, "t3-unreadable.webp")
        try {
            corrupt.writeText("This is not an image")
            deleted.writeText("Deleted before Glide opens it")
            assertTrue(deleted.delete())
            listOf(
                "Aramides ypecaha" to null,
                null to null,
                "Vanellus chilensis" to "content://com.example.avesargentinas.missing/map",
                "Anas versicolor" to android.net.Uri.fromFile(deleted).toString(),
                "Anas cyanoptera" to android.net.Uri.fromFile(corrupt).toString()
            ).forEach { (name, source) ->
                launch(name, source).use { scenario ->
                    await(scenario) { notice(it).isShown }
                    scenario.onActivity {
                        assertNull(uri(it))
                        assertFalse(image(it).isEnabled)
                        assertFalse(image(it).isClickable)
                        assertFalse(image(it).isFocusable)
                        val notice = notice(it)
                        assertTrue(notice.text.isNotBlank())
                        assertEquals(View.ACCESSIBILITY_LIVE_REGION_POLITE, notice.accessibilityLiveRegion)
                        val node = notice.createAccessibilityNodeInfo()
                        assertEquals(notice.text.toString(), node.text.toString())
                        assertTrue(node.isVisibleToUser)
                        node.recycle()
                        image(it).performClick()
                        assertTrue(foreground() is BirdInfoActivity)
                        assertTrue(it.findViewById<View>(R.id.btnBackBirdInfo).isShown)
                        it.findViewById<View>(R.id.btnBackBirdInfo).performClick()
                    }
                    awaitDestroyed(scenario)
                }
            }
        } finally { corrupt.delete(); deleted.delete() }
    }

    @Test fun mapAndUnavailableNoticeVisualMatrix() {
        val oldFont = shell("settings get system font_scale").trim()
        val animations = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
            .associateWith { shell("settings get global $it").trim() }
        try {
            animations.keys.forEach { shell("settings put global $it 0") }
            listOf("1.0", "2.0").forEach { font ->
                shell("settings put system font_scale $font")
                listOf("Vanellus chilensis", "Aramides ypecaha").forEach { name ->
                    launch(name).use { scenario ->
                        listOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES).forEach { theme ->
                            scenario.onActivity { it.delegate.localNightMode = theme }
                            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).forEach { orientation ->
                                scenario.onActivity { it.requestedOrientation = orientation }
                                await(scenario) {
                                    it.resources.configuration.orientation == (if (orientation == 1) 1 else 2) &&
                                        it.resources.configuration.uiMode and 48 == (if (theme == AppCompatDelegate.MODE_NIGHT_YES) 32 else 16) &&
                                        it.resources.configuration.fontScale == font.toFloat() &&
                                        (if (name.startsWith("Vanellus")) image(it).isEnabled else notice(it).isShown) &&
                                        !it.window.decorView.isLayoutRequested
                                }
                                scenario.onActivity {
                                    assertTrue(it.findViewById<View>(R.id.btnBackBirdInfo).isShown)
                                    if (name.startsWith("Aramides")) {
                                        val text = notice(it)
                                        assertTrue("Notice fits vertically", text.layout.height <= text.height - text.compoundPaddingTop - text.compoundPaddingBottom)
                                        repeat(text.layout.lineCount) { line -> assertEquals(0, text.layout.getEllipsisCount(line)) }
                                        val rect = android.graphics.Rect()
                                        assertTrue(text.getGlobalVisibleRect(rect))
                                        assertEquals(text.height, rect.height())
                                        assertFalse(image(it).isEnabled)
                                    }
                                }
                                capture(scenario, "${name.substringBefore(' ')}-$font-$theme-$orientation")
                            }
                        }
                    }
                }
            }
        } finally {
            shell(if (oldFont == "null") "settings delete system font_scale" else "settings put system font_scale $oldFont")
            animations.forEach { (key, value) -> shell(if (value == "null") "settings delete global $key" else "settings put global $key $value") }
        }
    }

    private fun launch(name: String?, explicit: String? = null) = ActivityScenario.launch<BirdInfoActivity>(
        Intent(context, BirdInfoActivity::class.java)
            .putExtra(BirdInfoActivity.EXTRA_COMMON_NAME, "Ave de prueba")
            .putExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, name)
            .apply { if (explicit != null) putExtra(BirdInfoActivity.EXTRA_DISTRIBUTION_URI, explicit) })

    private fun verifyZoomAndReturn(scenario: ActivityScenario<BirdInfoActivity>, expected: String) {
        scenario.onActivity { assertEquals(expected, uri(it)); image(it).performClick() }
        waitUntil { foreground() is ImageZoomActivity }
        instrumentation.runOnMainSync {
            val zoom = foreground() as ImageZoomActivity
            assertEquals(expected, zoom.intent.getStringExtra(ImageZoomActivity.EXTRA_IMAGE_URI))
        }
        waitUntil { (foreground() as? ImageZoomActivity)?.findViewById<ImageView>(R.id.imgZoom)?.drawable != null }
        instrumentation.runOnMainSync { (foreground() as ImageZoomActivity).onBackPressedDispatcher.onBackPressed() }
        waitUntil { foreground() is BirdInfoActivity }
        scenario.onActivity {
            assertEquals(expected, uri(it))
            assertTrue(image(it).isEnabled)
            it.findViewById<View>(R.id.btnBackBirdInfo).performClick()
        }
        awaitDestroyed(scenario)
    }

    private fun awaitDestroyed(scenario: ActivityScenario<BirdInfoActivity>) {
        val deadline = android.os.SystemClock.uptimeMillis() + 15000
        while (scenario.state != Lifecycle.State.DESTROYED && android.os.SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(30)
        }
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }

    private fun dismissIdentificationPermissions() {
        val automation = instrumentation.uiAutomation
        fun nodes(node: android.view.accessibility.AccessibilityNodeInfo): Sequence<android.view.accessibility.AccessibilityNodeInfo> =
            sequence {
                yield(node)
                repeat(node.childCount) { index -> node.getChild(index)?.let { yieldAll(nodes(it)) } }
            }
        repeat(4) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            val deny = automation.rootInActiveWindow?.let(::nodes)?.firstOrNull {
                it.viewIdResourceName?.let { id -> id.endsWith(":id/permission_deny_button") ||
                    id.endsWith(":id/permission_deny_and_dont_ask_again_button") } == true
            }
            if (deny != null) {
                deny.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                try { automation.waitForIdle(100, 2000) } catch (_: java.util.concurrent.TimeoutException) { }
            }
        }
    }

    private fun image(activity: BirdInfoActivity) = activity.findViewById<ImageView>(R.id.imageDistribution)
    private fun notice(activity: BirdInfoActivity): TextView = requireNotNull(activity.findViewById(
        activity.resources.getIdentifier("distributionUnavailable", "id", context.packageName)))
    private fun uri(activity: BirdInfoActivity) = BirdInfoActivity::class.java.getDeclaredField("distributionImageUri")
        .apply { isAccessible = true }.get(activity) as String?
    private fun resourceUri(id: Int) = "android.resource://${context.packageName}/$id"
    private fun foreground() = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).firstOrNull()
    private fun await(scenario: ActivityScenario<BirdInfoActivity>, predicate: (BirdInfoActivity) -> Boolean) = waitUntil {
        var result = false
        scenario.onActivity { result = predicate(it) }
        result
    }
    private fun waitUntil(predicate: () -> Boolean) {
        val deadline = android.os.SystemClock.uptimeMillis() + 15000
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            var done = false
            instrumentation.runOnMainSync { done = predicate() }
            if (done) return
            Thread.sleep(30)
        }
        fail("Timed out waiting for real image/UI state")
    }
    private fun shell(command: String) = instrumentation.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText()
    }
    private fun capture(scenario: ActivityScenario<BirdInfoActivity>, name: String) {
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        repeat(2) {
            val frame = CountDownLatch(1)
            scenario.onActivity {
                val view = it.window.decorView
                if (android.os.Build.VERSION.SDK_INT >= 29 && view.isHardwareAccelerated) {
                    view.viewTreeObserver.registerFrameCommitCallback { frame.countDown() }
                    view.invalidate()
                } else view.postOnAnimation { view.postOnAnimation { frame.countDown() } }
            }
            assertTrue(frame.await(10, TimeUnit.SECONDS))
        }
        val file = File(context.getExternalFilesDir(null), "t3-$name.png")
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        shell("mkdir -p /data/local/tmp/aves-t3-captures")
        shell("cp ${file.absolutePath} /data/local/tmp/aves-t3-captures/${file.name}")
    }
}
