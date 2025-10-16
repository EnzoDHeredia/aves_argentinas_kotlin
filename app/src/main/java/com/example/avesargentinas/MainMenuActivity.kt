package com.example.avesargentinas

import android.content.Intent
import android.os.Bundle
import com.example.avesargentinas.ui.log.ObservationLogActivity
import com.google.android.material.button.MaterialButton

/**
 * Actividad principal del menú.
 * Permite navegar a identificación, historial de observaciones y opciones.
 */
class MainMenuActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        
        // Verificar si venimos de un back press (para evitar loop infinito)
        val fromBackPress = intent.getBooleanExtra("from_back_press", false)
        
        // Si captura instantánea está habilitada Y NO venimos de un back press,
        // saltar directamente a MainActivity
        if (SettingsManager.isInstantCapture(this) && !fromBackPress) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cerrar el menú para que no quede en el stack
            return
        }
        
        setContentView(R.layout.activity_main_menu)

        val btnCapture = findViewById<MaterialButton>(R.id.btnCapture)
        val btnObservationLog = findViewById<MaterialButton>(R.id.btnObservationLog)
        val btnSettings = findViewById<MaterialButton>(R.id.btnSettings)

        btnCapture.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnObservationLog.setOnClickListener {
            val intent = Intent(this, ObservationLogActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
