package com.example.avesargentinas.ui.log

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.data.ObservationRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class ObservationLogActivity : AppCompatActivity() {

    private lateinit var repository: ObservationRepository
    private lateinit var adapter: ObservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_log)

        repository = ObservationRepository.getInstance(applicationContext)
        setupToolbar()
        setupRecycler()
        observeData()
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.observation_log_title)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        val recycler: RecyclerView = findViewById(R.id.recyclerObservations)
        adapter = ObservationAdapter { observation ->
            val intent = Intent(this, ObservationDetailActivity::class.java)
            intent.putExtra(ObservationDetailActivity.EXTRA_OBSERVATION_ID, observation.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun observeData() {
        val emptyView: View = findViewById(R.id.txtEmpty)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeAll().collect { observations ->
                    adapter.submitList(observations)
                    emptyView.visibility = if (observations.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
