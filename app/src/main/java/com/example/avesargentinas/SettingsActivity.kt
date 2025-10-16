package com.example.avesargentinas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Actividad de configuración.
 * Permite ajustar nombre de usuario, modo experto, ofuscación de ubicación,
 * captura instantánea y tema oscuro/claro.
 */
class SettingsActivity : AppCompatActivity() {

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

        // Listeners para guardar cambios
        edtUserName.doAfterTextChanged { text ->
            SettingsManager.setUserName(this, text?.toString() ?: "")
        }

        switchExpert.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setExpertMode(this, isChecked)
        }

        switchObfuscate.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setObfuscateLocation(this, isChecked)
        }

        switchInstant.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setInstantCapture(this, isChecked)
        }

        switchDark.setOnCheckedChangeListener { _, _ ->
            // Alternar tema y actualizar el estado del switch
            ThemeManager.toggleTheme(this)
            // El tema cambiará y recreará la activity, así que no es necesario actualizar manualmente
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}
