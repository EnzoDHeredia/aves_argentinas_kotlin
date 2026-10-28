package com.example.avesargentinas.ui.log

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.avesargentinas.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObservationLogActivityTest {
    @Test
    fun backCancelsSelectionBeforeLeavingHistory() {
        ActivityScenario.launch(ObservationLogActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val adapter = activity.findViewById<RecyclerView>(R.id.recyclerObservations).adapter as ObservationAdapter
                val selectionToolbar = activity.findViewById<View>(R.id.cardSelectionToolbar)

                listOf<() -> Unit>(
                    { activity.onBackPressedDispatcher.onBackPressed() },
                    { activity.findViewById<View>(R.id.btnBackLog).performClick() }
                ).forEach { back ->
                    activity.findViewById<View>(R.id.fabSelectMode).performClick()
                    // Exercise selection cleanup without inserting or deleting observations.
                    adapter.toggleSelection(Long.MIN_VALUE)
                    assertEquals(View.VISIBLE, selectionToolbar.visibility)
                    assertEquals(1, adapter.getSelectedCount())

                    back()

                    assertFalse("Back must cancel selection before closing history", activity.isFinishing)
                    assertEquals(View.GONE, selectionToolbar.visibility)
                    assertFalse(adapter.isSelectionMode)
                    assertEquals(0, adapter.getSelectedCount())
                }

                activity.onBackPressedDispatcher.onBackPressed()
            }
            val deadline = SystemClock.uptimeMillis() + 15000
            while (scenario.state != Lifecycle.State.DESTROYED && SystemClock.uptimeMillis() < deadline) {
                Thread.sleep(30)
            }
            assertEquals("Back without selection must close history", Lifecycle.State.DESTROYED, scenario.state)
        }
    }
}
