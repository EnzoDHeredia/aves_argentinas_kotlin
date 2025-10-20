package com.example.avesargentinas.ui.log

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.HistoryExporter
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.data.ObservationRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ObservationLogActivity : BaseActivity() {

    private lateinit var repository: ObservationRepository
    private lateinit var adapter: ObservationAdapter
    private lateinit var fabExport: FloatingActionButton
    private lateinit var fabSelectMode: FloatingActionButton
    private lateinit var historyExporter: HistoryExporter
    private var isSelectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_log)

        repository = ObservationRepository.getInstance(applicationContext)
        historyExporter = HistoryExporter(this)
        
        setupToolbar()
        setupRecycler()
        setupExportButton()
        setupSelectionMode()
        observeData()

        // Sin listener de insets; el layout usa m�rgenes fijos.

        val btnBackLog: android.widget.ImageButton? = findViewById(R.id.btnBackLog)
        btnBackLog?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.observation_log_title)
        // Único handler para navigation icon
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        val recycler: RecyclerView = findViewById(R.id.recyclerObservations)
        adapter = ObservationAdapter(
            onObservationSelected = { observation ->
                val intent = Intent(this, ObservationDetailActivity::class.java)
                intent.putExtra(ObservationDetailActivity.EXTRA_OBSERVATION_ID, observation.id)
                startActivity(intent)
            },
            onSelectionChanged = {
                updateSelectionCount()
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun setupExportButton() {
        fabExport = findViewById(R.id.fabExport)
        fabExport.setOnClickListener {
            showExportConfirmationDialog()
        }
    }

    private fun setupSelectionMode() {
        fabSelectMode = findViewById(R.id.fabSelectMode)
        val cardSelectionToolbar = findViewById<View>(R.id.cardSelectionToolbar)
        val txtSelectionCount = findViewById<android.widget.TextView>(R.id.txtSelectionCount)
        val btnCancelSelection = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelSelection)
        val btnDeleteSelected = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeleteSelected)

        // Activar modo selección
        fabSelectMode.setOnClickListener {
            isSelectionMode = true
            adapter.isSelectionMode = true
            cardSelectionToolbar.visibility = View.VISIBLE
            fabSelectMode.visibility = View.GONE
            fabExport.visibility = View.GONE
            updateSelectionCount()
        }

        // Cancelar selección
        btnCancelSelection.setOnClickListener {
            exitSelectionMode()
        }

        // Eliminar seleccionados
        btnDeleteSelected.setOnClickListener {
            val selectedCount = adapter.getSelectedCount()
            if (selectedCount > 0) {
                showDeleteConfirmationDialog(selectedCount)
            } else {
                Toast.makeText(this, "Selecciona al menos una observación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        adapter.isSelectionMode = false
        findViewById<View>(R.id.cardSelectionToolbar).visibility = View.GONE
        // Mostrar FABs solo si hay observaciones
        if (adapter.currentList.isNotEmpty()) {
            fabSelectMode.visibility = View.VISIBLE
            fabExport.visibility = View.VISIBLE
        }
    }

    private fun updateSelectionCount() {
        val txtSelectionCount = findViewById<android.widget.TextView>(R.id.txtSelectionCount)
        val count = adapter.getSelectedCount()
        txtSelectionCount.text = if (count == 1) "1 seleccionado" else "$count seleccionados"
    }

    private fun observeData() {
        val emptyView: View = findViewById(R.id.txtEmpty)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeAll().collect { observations ->
                    adapter.submitList(observations)
                    emptyView.visibility = if (observations.isEmpty()) View.VISIBLE else View.GONE
                    // Mostrar/ocultar FABs según si hay observaciones
                    if (observations.isEmpty()) {
                        fabExport.visibility = View.GONE
                        fabSelectMode.visibility = View.GONE
                    } else {
                        fabExport.visibility = View.VISIBLE
                        fabSelectMode.visibility = View.VISIBLE
                    }
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

        val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_AvesArgentinas_Dialog)
            .setTitle("Exportar Historial Completo")
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
            .create()
        
        dialog.show()
        styleDialogText(dialog)
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

    /**
     * Muestra diálogo de confirmación para eliminar observaciones seleccionadas
     */
    private fun showDeleteConfirmationDialog(count: Int) {
        val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_AvesArgentinas_Dialog)
            .setTitle("Eliminar observaciones")
            .setMessage(
                if (count == 1) {
                    "Se eliminará 1 observación permanentemente."
                } else {
                    "Se eliminarán $count observaciones permanentemente."
                }
            )
            .setPositiveButton("Eliminar") { _, _ ->
                deleteSelectedObservations()
            }
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
        styleDialogText(dialog)
    }

    /**
     * Elimina las observaciones seleccionadas
     */
    private fun deleteSelectedObservations() {
        val selectedObservations = adapter.getSelectedObservations()
        val count = selectedObservations.size

        lifecycleScope.launch {
            try {
                selectedObservations.forEach { observation ->
                    repository.delete(observation)
                }
                
                val message = if (count == 1) {
                    "Observación eliminada"
                } else {
                    "$count observaciones eliminadas"
                }
                Toast.makeText(this@ObservationLogActivity, message, Toast.LENGTH_SHORT).show()
                
                exitSelectionMode()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@ObservationLogActivity,
                    "Error al eliminar",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ObservationLog", "Error al eliminar observaciones", e)
            }
        }
    }

    /**
     * Aplica estilos personalizados al texto del diálogo
     */
    private fun styleDialogText(dialog: AlertDialog) {
        // Estilizar título
        dialog.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        }
        
        // Estilizar mensaje
        dialog.window?.findViewById<TextView>(android.R.id.message)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            lineHeight = (textSize * 1.4f).toInt()
        }
        
        // Estilizar botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
        }
        
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
        }
    }

    override fun onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }
}
