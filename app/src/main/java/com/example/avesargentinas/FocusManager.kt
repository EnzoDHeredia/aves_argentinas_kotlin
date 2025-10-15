package com.example.avesargentinas

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log

/**
 * Clase responsable de gestionar el recuadro de enfoque y recorte de imágenes
 */
class FocusManager(
    private val photoView: InteractiveZoomageView,
    private val focusOverlay: FocusOverlayView,
    private val imageProcessor: ImageProcessor
) {
    companion object {
        private const val TAG = "FocusManager"
        private const val FOCUS_BOX_SIZE_RATIO = 0.7f
    }

    /**
     * Actualiza la posición y tamaño del overlay de enfoque
     */
    fun updateOverlay() {
        focusOverlay.post {
            val width = focusOverlay.width.toFloat()
            val height = focusOverlay.height.toFloat()
            
            if (width <= 0 || height <= 0) return@post
            
            // Recuadro cuadrado centrado
            val size = minOf(width, height) * FOCUS_BOX_SIZE_RATIO
            val left = (width - size) / 2f
            val top = (height - size) / 2f
            
            focusOverlay.focusRect = RectF(left, top, left + size, top + size)
        }
    }

    /**
     * Recorta el bitmap original según el recuadro de enfoque
     * @return CropResult con el bitmap recortado y metadatos
     */
    fun cropToFocus(originalBitmap: Bitmap): CropResult {
        val focusRect = focusOverlay.focusRect
            ?: return CropResult.Error("Recuadro de enfoque no inicializado")

    val displayRect = photoView.currentDisplayRect()
        ?: return CropResult.Error("Imagen no visible")

        Log.d(TAG, "=== RECORTE SEGÚN RECUADRO ===")
        Log.d(TAG, "Bitmap: ${originalBitmap.width}x${originalBitmap.height}")
        Log.d(TAG, "FocusRect: $focusRect")
        Log.d(TAG, "DisplayRect: $displayRect")

        // Calcular intersección entre recuadro e imagen
        val intersection = imageProcessor.calculateIntersection(focusRect, displayRect)
        
        // Si no hay intersección, informar que el recuadro debe cubrir la imagen
        if (intersection == null) {
            Log.d(TAG, "Sin intersección entre recuadro e imagen")
            return CropResult.Error("Ajustá la imagen para que quede dentro del recuadro")
        }

        // Calcular cobertura (solo informativo)
        val coveragePercentage = imageProcessor.calculateCoveragePercentage(focusRect, displayRect)
        Log.d(TAG, "Cobertura: ${coveragePercentage.toInt()}%")

        // Convertir coordenadas de vista a bitmap
        val displayWidth = displayRect.width()
        val displayHeight = displayRect.height()

        if (displayWidth <= 0 || displayHeight <= 0) {
            return CropResult.Error("DisplayRect inválido")
        }

        val offsetXInDisplay = intersection.left - displayRect.left
        val offsetYInDisplay = intersection.top - displayRect.top
        val intersectWidth = intersection.width()
        val intersectHeight = intersection.height()

        // Escala
        val scaleX = originalBitmap.width.toFloat() / displayWidth
        val scaleY = originalBitmap.height.toFloat() / displayHeight

        // Coordenadas en bitmap
        val bitmapLeft = (offsetXInDisplay * scaleX).toInt()
            .coerceIn(0, originalBitmap.width - 1)
        val bitmapTop = (offsetYInDisplay * scaleY).toInt()
            .coerceIn(0, originalBitmap.height - 1)
        val bitmapWidth = (intersectWidth * scaleX).toInt()
            .coerceIn(1, originalBitmap.width - bitmapLeft)
        val bitmapHeight = (intersectHeight * scaleY).toInt()
            .coerceIn(1, originalBitmap.height - bitmapTop)

        Log.d(TAG, "Crop: x=$bitmapLeft, y=$bitmapTop, w=$bitmapWidth, h=$bitmapHeight")

        val cropped = imageProcessor.cropBitmap(
            originalBitmap,
            bitmapLeft,
            bitmapTop,
            bitmapWidth,
            bitmapHeight
        ) ?: return CropResult.Error("Error al recortar bitmap")

        val percentage = (bitmapWidth * bitmapHeight * 100f) / 
                        (originalBitmap.width * originalBitmap.height)

        Log.d(TAG, "✅ Recorte exitoso: ${cropped.width}x${cropped.height}")

        return CropResult.Success(
            bitmap = cropped,
            coveragePercentage = coveragePercentage,
            areaPercentage = percentage
        )
    }

    /**
     * Muestra el overlay
     */
    fun showOverlay() {
        focusOverlay.visibility = android.view.View.VISIBLE
        updateOverlay()
    }

    /**
     * Oculta el overlay
     */
    fun hideOverlay() {
        focusOverlay.visibility = android.view.View.GONE
    }
}

/**
 * Resultado del recorte según el recuadro de enfoque
 */
sealed class CropResult {
    data class Success(
        val bitmap: Bitmap,
        val coveragePercentage: Float,
        val areaPercentage: Float
    ) : CropResult()

    data class Error(val message: String) : CropResult()
}
