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

    // Nota: no re-aplicamos inmersivo en onWindowFocusChanged para permitir
    // que los gestos del sistema (atrás) funcionen en un solo swipe.

    /**
     * Modo estándar sin ocultar barras: el sistema aplica WindowInsets
     * y gestiona gestos de navegación correctamente.
     */
    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }
}
