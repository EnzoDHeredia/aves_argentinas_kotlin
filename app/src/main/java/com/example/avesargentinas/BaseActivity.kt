package com.example.avesargentinas

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat

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
        // Mantenemos el ajuste estándar del sistema
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Aplicar padding dinámico inferior solo cuando haya barra de navegación
        applyBottomInsetPaddingToRoot()
    }

    private fun applyBottomInsetPaddingToRoot() {
        val content = findViewById<ViewGroup>(android.R.id.content) ?: return
        val root = content.getChildAt(0) ?: return

        // Guardar paddings iniciales para sumarlos al valor de insets
        val initialStart = root.paddingStart
        val initialTop = root.paddingTop
        val initialEnd = root.paddingEnd
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            // Tomar solo la barra de navegación. Si la actividad permite ajustes
            // por teclado, considerar también el IME para evitar superposición.
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomInset = if (allowKeyboardAdjustments && insets.isVisible(WindowInsetsCompat.Type.ime())) {
                imeInsets.bottom
            } else {
                navInsets.bottom
            }

            // Agregar padding inferior solo si hay barra/IME visible
            val newBottom = initialBottom + bottomInset
            v.setPaddingRelative(initialStart, initialTop, initialEnd, newBottom)
            insets
        }

        // Forzar cálculo de insets
        root.requestApplyInsets()
    }
}
