package com.example.avesargentinas.ui.log

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avesargentinas.HistoryExporter
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.data.ObservationRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ObservationLogActivity : AppCompatActivity() {

    private lateinit var repository: ObservationRepository
    private lateinit var adapter: ObservationAdapter
    private lateinit var fabExport: FloatingActionButton
    private lateinit var historyExporter: HistoryExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_log)

        repository = ObservationRepository.getInstance(applicationContext)
        historyExporter = HistoryExporter(this)
        
        setupToolbar()
        setupRecycler()
        setupExportButton()
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

    private fun setupExportButton() {
        fabExport = findViewById(R.id.fabExport)
        fabExport.setOnClickListener {
            showExportConfirmationDialog()
        }
    }

    private fun observeData() {
        val emptyView: View = findViewById(R.id.txtEmpty)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeAll().collect { observations ->
                    adapter.submitList(observations)
                    emptyView.visibility = if (observations.isEmpty()) View.VISIBLE else View.GONE
                    // Mostrar/ocultar FAB según si hay observaciones
                    fabExport.visibility = if (observations.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de exportar
     */
    private fun showExportConfirmationDialog() {
        val observationCount = adapter.currentList.size
        
        if (observationCount == 0) {
            Toast.makeText(this, "No hay observaciones para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("📦 Exportar Historial Completo")
            .setMessage(
                "Se exportarán $observationCount observaciones con sus imágenes en formato ZIP.\n\n" +
                "El archivo incluirá:\n" +
                "• CSV con todos los datos\n" +
                "• Carpeta con todas las imágenes\n\n" +
                "¿Deseas continuar?"
            )
            .setPositiveButton("Exportar") { _, _ ->
                exportHistory()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Ejecuta la exportación del historial
     */
    private fun exportHistory() {
        // Mostrar progreso
        val progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Exportando...")
            .setMessage("Por favor espera mientras se genera el archivo ZIP")
            .setCancelable(false)
            .create()
        
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val observations = adapter.currentList
                val zipUri = historyExporter.exportToZip(observations)
                
                progressDialog.dismiss()
                
                if (zipUri != null) {
                    showExportSuccessDialog(zipUri, observations.size)
                } else {
                    showExportErrorDialog()
                }
                
            } catch (e: Exception) {
                progressDialog.dismiss()
                showExportErrorDialog()
                Log.e("ObservationLog", "Error en exportación", e)
            }
        }
    }

    /**
     * Muestra diálogo de éxito con opciones para compartir o abrir
     */
    private fun showExportSuccessDialog(zipUri: Uri, count: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Exportación Completada")
            .setMessage(
                "Se han exportado $count observaciones exitosamente.\n\n" +
                "El archivo se guardó en Descargas."
            )
            .setPositiveButton("Compartir") { _, _ ->
                shareZipFile(zipUri)
            }
            .setNegativeButton("Cerrar", null)
            .setNeutralButton("Abrir carpeta") { _, _ ->
                openDownloadsFolder()
            }
            .show()
    }

    /**
     * Muestra diálogo de error
     */
    private fun showExportErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("❌ Error")
            .setMessage("No se pudo exportar el historial. Verifica los permisos de almacenamiento.")
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Comparte el archivo ZIP usando el sistema de compartir de Android
     */
    private fun shareZipFile(zipUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, zipUri)
            putExtra(Intent.EXTRA_SUBJECT, "Historial de Observaciones - Aves Argentinas")
            putExtra(Intent.EXTRA_TEXT, "Historial completo de observaciones de aves")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Compartir historial"))
    }

    /**
     * Abre la carpeta de Descargas
     */
    private fun openDownloadsFolder() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"), "resource/folder")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir Descargas", Toast.LENGTH_SHORT).show()
        }
    }
}
