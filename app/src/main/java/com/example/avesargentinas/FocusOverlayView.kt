package com.example.avesargentinas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class FocusOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#99000000") // Negro semitransparente
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f) // Línea punteada
    }

    private val cornerPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private var _focusRect: RectF? = null
    var focusRect: RectF?
        get() = _focusRect
        set(value) {
            // Solo invalidar si realmente cambió
            if (_focusRect != value) {
                _focusRect = value
                invalidate()
            }
        }

    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean {
        return false // Deja que la interacción gestual ocurra sobre la imagen subyacente
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rect = _focusRect ?: return

        // Dibujar overlay oscuro en las 4 regiones fuera del recuadro
        // Arriba
        canvas.drawRect(0f, 0f, width.toFloat(), rect.top, overlayPaint)
        // Abajo
        canvas.drawRect(0f, rect.bottom, width.toFloat(), height.toFloat(), overlayPaint)
        // Izquierda
        canvas.drawRect(0f, rect.top, rect.left, rect.bottom, overlayPaint)
        // Derecha
        canvas.drawRect(rect.right, rect.top, width.toFloat(), rect.bottom, overlayPaint)

        // Dibujar borde punteado del recuadro
        canvas.drawRect(rect, borderPaint)

        // Dibujar esquinas decorativas
        val cornerLength = 40f

        // Esquina superior izquierda
        canvas.drawLine(rect.left, rect.top, rect.left + cornerLength, rect.top, cornerPaint)
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + cornerLength, cornerPaint)

        // Esquina superior derecha
        canvas.drawLine(rect.right, rect.top, rect.right - cornerLength, rect.top, cornerPaint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + cornerLength, cornerPaint)

        // Esquina inferior izquierda
        canvas.drawLine(rect.left, rect.bottom, rect.left + cornerLength, rect.bottom, cornerPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - cornerLength, cornerPaint)

        // Esquina inferior derecha
        canvas.drawLine(rect.right, rect.bottom, rect.right - cornerLength, rect.bottom, cornerPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - cornerLength, cornerPaint)
    }
}