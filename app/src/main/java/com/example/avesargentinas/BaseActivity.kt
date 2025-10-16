package com.example.avesargentinas

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Actividad base que configura el modo inmersivo para ocultar las barras del sistema.
 * Todas las actividades principales heredan de esta clase para tener un comportamiento consistente.
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * Permite que las actividades hijas decidan si quieren que el sistema ajuste el contenido
     * automáticamente para el teclado. Por defecto es false para el modo inmersivo completo.
     * Las actividades con campos de texto pueden sobrescribir esto como true.
     */
    protected open val allowKeyboardAdjustments: Boolean = false

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupImmersiveMode()
        }
    }

    /**
     * Configura el modo inmersivo para ocultar las barras de navegación y estado.
     * Compatible con todas las versiones de Android usando AndroidX.
     */
    private fun setupImmersiveMode() {
        // Indicar si la app debe ajustarse para las barras del sistema y teclado
        WindowCompat.setDecorFitsSystemWindows(window, allowKeyboardAdjustments)
        
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let { controller ->
            // Ocultar las barras de navegación y estado
            controller.hide(WindowInsetsCompat.Type.systemBars())
            
            // Configurar comportamiento inmersivo sticky (las barras reaparecen temporalmente al deslizar)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
