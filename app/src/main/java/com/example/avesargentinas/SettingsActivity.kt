package com.example.avesargentinas

import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Actividad de configuración.
 * Permite ajustar nombre de usuario, modo experto, ofuscación de ubicación,
 * captura instantánea y tema oscuro/claro.
 * Los cambios se guardan solo al presionar "Guardar y Cerrar".
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val edtUserName = findViewById<TextInputEditText>(R.id.edtUserName)
        val switchExpert = findViewById<SwitchCompat>(R.id.switchExpertMode)
        val switchObfuscate = findViewById<SwitchCompat>(R.id.switchObfuscateLocation)
        val switchInstant = findViewById<SwitchCompat>(R.id.switchInstantCapture)
        val switchDark = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val btnClose = findViewById<MaterialButton>(R.id.btnCloseSettings)

        // Cargar valores guardados
        edtUserName.setText(SettingsManager.getUserName(this))
        switchExpert.isChecked = SettingsManager.isExpertMode(this)
        switchObfuscate.isChecked = SettingsManager.isObfuscateLocation(this)
        switchInstant.isChecked = SettingsManager.isInstantCapture(this)
        switchDark.isChecked = ThemeManager.isDarkTheme(this)

        // Guardar solo al presionar el botón
        btnClose.setOnClickListener {
            // Guardar todas las preferencias
            SettingsManager.setUserName(this, edtUserName.text?.toString() ?: "")
            SettingsManager.setExpertMode(this, switchExpert.isChecked)
            SettingsManager.setObfuscateLocation(this, switchObfuscate.isChecked)
            SettingsManager.setInstantCapture(this, switchInstant.isChecked)
            
            // Si cambió el tema, aplicarlo
            val newDarkMode = switchDark.isChecked
            val currentDarkMode = ThemeManager.isDarkTheme(this)
            if (newDarkMode != currentDarkMode) {
                ThemeManager.toggleTheme(this)
            }
            
            finish()
        }
    }
}
