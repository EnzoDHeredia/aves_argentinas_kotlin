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
     * automÃ¡ticamente para el teclado. Por defecto es false para el modo inmersivo completo.
     * Las actividades con campos de texto pueden sobrescribir esto como true.
     */
    protected open val allowKeyboardAdjustments: Boolean = false

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
    }

    // Nota: no re-aplicamos inmersivo en onWindowFocusChanged para permitir
    // que los gestos del sistema (atrÃ¡s) funcionen en un solo swipe.

    /**
     * Modo estÃ¡ndar sin ocultar barras: el sistema aplica WindowInsets
     * y gestiona gestos de navegaciÃ³n correctamente.
     */
    private fun setupImmersiveMode() {
        // Mantenemos el ajuste estÃ¡ndar del sistema
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Aplicar padding dinÃ¡mico inferior solo cuando haya barra de navegaciÃ³n
        applyBottomInsetPaddingToRoot()
    }

    private fun applyBottomInsetPaddingToRoot() {
        val content = findViewById<ViewGroup>(android.R.id.content) ?: return
        val root = content.getChildAt(0) ?: return
        // Usar contenedor especÃ­fico si el layout lo define
        val target: View = root.findViewById(R.id.insets_container) ?: root

        // Evitar re-adjuntar el listener y acumular padding en cada onResume
        val alreadyAttached = (target.getTag(R.id.tag_insets_listener_attached) as? Boolean) == true
        if (alreadyAttached) {
            target.requestApplyInsets()
            return
        }

        // Guardar paddings iniciales para sumarlos al valor de insets
        val initialStart = target.paddingStart
        val initialTop = target.paddingTop
        val initialEnd = target.paddingEnd
        val initialBottom = target.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(target) { v, insets ->
            // Tomar solo la barra de navegaciÃ³n. Si la actividad permite ajustes
            // por teclado, considerar tambiÃ©n el IME para evitar superposiciÃ³n.
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomInset = if (allowKeyboardAdjustments && insets.isVisible(WindowInsetsCompat.Type.ime())) {
                imeInsets.bottom
            } else {
                navInsets.bottom
            }

            // Padding base + insets de navegaciÃ³n/IME (sin acumulaciÃ³n)
            val adjust = dpToPx(0f); val newBottom = if (bottomInset > 0) (bottomInset - adjust).coerceAtLeast(0) else 0
            v.setPaddingRelative(initialStart, initialTop, initialEnd, newBottom)
            insets
        }

        // Marcar listener adjunto para llamadas futuras
        target.setTag(R.id.tag_insets_listener_attached, true)

        // Forzar cÃ¡lculo de insets
        target.requestApplyInsets()
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}
