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
        if (allowKeyboardAdjustments) {
            // En actividades con campos de texto, permitir que el sistema maneje
            // el ajuste del layout para el teclado. Esto es CRÍTICO para que
            // adjustResize funcione correctamente.
            WindowCompat.setDecorFitsSystemWindows(window, true)
            
            // No ocultamos las barras del sistema en estas pantallas para mejor UX
            // y para evitar conflictos con el ajuste del teclado
        } else {
            // En actividades sin campos de texto, usar modo inmersivo completo
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.let { controller ->
                // Ocultar las barras de navegación y estado
                controller.hide(WindowInsetsCompat.Type.systemBars())
                
                // Configurar comportamiento inmersivo sticky (las barras reaparecen temporalmente al deslizar)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}
