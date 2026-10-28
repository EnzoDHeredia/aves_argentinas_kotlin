package com.example.avesargentinas

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.avesargentinas.ui.learning.LearningGameActivity
import com.example.avesargentinas.ui.log.ObservationLogActivity
import com.example.avesargentinas.ui.sound.BirdSoundActivity
import com.google.android.material.button.MaterialButton

/**
 * Actividad principal del menú.
 * Permite navegar a identificación fotográfica, identificación por sonido, observaciones, aprendizaje y opciones.
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
        val btnSoundCapture = findViewById<MaterialButton>(R.id.btnSoundCapture)
        val btnObservationLog = findViewById<MaterialButton>(R.id.btnObservationLog)
        val btnLearning = findViewById<MaterialButton>(R.id.btnLearning)
        val btnSettings = findViewById<MaterialButton>(R.id.btnSettings)

        ViewCompat.setOnApplyWindowInsetsListener(btnSettings) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val lp = v.layoutParams as? FrameLayout.LayoutParams
            if (lp != null) {
                val baseMargin = (16 * resources.displayMetrics.density).toInt()
                lp.topMargin = statusBars.top + baseMargin
                v.layoutParams = lp
            }
            insets
        }

        btnCapture.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnSoundCapture?.setOnClickListener {
            val intent = Intent(this, BirdSoundActivity::class.java)
            startActivity(intent)
        }

        btnObservationLog.setOnClickListener {
            val intent = Intent(this, ObservationLogActivity::class.java)
            startActivity(intent)
        }

        btnLearning.setOnClickListener {
            startActivity(Intent(this, LearningGameActivity::class.java))
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
