package com.example.avesargentinas

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.Log

/**
 * Clase responsable de todas las operaciones con imágenes:
 * - Decodificación de URIs
 * - Rotación de bitmaps
 * - Recorte de regiones
 * - Conversión a buffers para el modelo
 */
class ImageProcessor {

    companion object {
        private const val TAG = "ImageProcessor"
    }

    /**
     * Decodifica una imagen desde un URI
     */
    fun decodeBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(src) { dec, _, _ ->
                dec.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
                dec.isMutableRequired = false
            }
        } else {
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)?.copy(Bitmap.Config.ARGB_8888, false)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error decodificando bitmap: ${e.message}")
        null
    }

    /**
     * Rota un bitmap
     */
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Recorta una región específica del bitmap
     * @return El bitmap recortado o null si las coordenadas son inválidas
     */
    fun cropBitmap(
        source: Bitmap,
        left: Int,
        top: Int,
        width: Int,
        height: Int
    ): Bitmap? {
        // Validar coordenadas
        if (left < 0 || top < 0 || width <= 0 || height <= 0 ||
            left + width > source.width || top + height > source.height) {
            Log.w(TAG, "Coordenadas de recorte inválidas")
            return null
        }

        return try {
            Bitmap.createBitmap(source, left, top, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error al recortar bitmap: ${e.message}")
            null
        }
    }

    /**
     * Escala un bitmap al tamaño especificado
     */
    fun scaleBitmap(source: Bitmap, targetSize: Int): Bitmap {
        return Bitmap.createScaledBitmap(source, targetSize, targetSize, true)
    }

    /**
     * Calcula la intersección entre dos rectángulos
     * @return RectF con la intersección o null si no se intersectan
     */
    fun calculateIntersection(rect1: RectF, rect2: RectF): RectF? {
        val intersectLeft = maxOf(rect1.left, rect2.left)
        val intersectTop = maxOf(rect1.top, rect2.top)
        val intersectRight = minOf(rect1.right, rect2.right)
        val intersectBottom = minOf(rect1.bottom, rect2.bottom)

        return if (intersectLeft < intersectRight && intersectTop < intersectBottom) {
            RectF(intersectLeft, intersectTop, intersectRight, intersectBottom)
        } else {
            null
        }
    }

    /**
     * Calcula el porcentaje de cobertura de rect1 sobre rect2
     */
    fun calculateCoveragePercentage(rect1: RectF, rect2: RectF): Float {
        val intersection = calculateIntersection(rect1, rect2) ?: return 0f
        val intersectionArea = intersection.width() * intersection.height()
        val rect1Area = rect1.width() * rect1.height()
        return if (rect1Area > 0) (intersectionArea / rect1Area) * 100f else 0f
    }
}
