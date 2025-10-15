package com.example.avesargentinas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.jsibbold.zoomage.ZoomageView

/**
 * ZoomageView con un listener similar al de PhotoView para reaccionar a cambios de matriz.
 */
class InteractiveZoomageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ZoomageView(context, attrs, defStyleAttr) {

    private var matrixChangeListener: ((RectF?) -> Unit)? = null
    private val tmpRect = RectF()
    private val mappedRect = RectF()
    private val tmpMatrix = Matrix()

    fun setOnMatrixChangeListener(listener: ((RectF?) -> Unit)?) {
        matrixChangeListener = listener
        notifyMatrixChanged()
    }

    fun currentDisplayRect(): RectF? {
        val drawable = drawable ?: return null
        tmpRect.set(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        tmpMatrix.set(imageMatrix)
        tmpMatrix.mapRect(mappedRect, tmpRect)
        return RectF(mappedRect)
    }

    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        notifyMatrixChanged()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        notifyMatrixChanged()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        notifyMatrixChanged()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        notifyMatrixChanged()
    }

    override fun setImageURI(uri: android.net.Uri?) {
        super.setImageURI(uri)
        notifyMatrixChanged()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            notifyMatrixChanged()
        }
    }

    private fun notifyMatrixChanged() {
        val listener = matrixChangeListener ?: return
        val rect = currentDisplayRect()
        listener(rect)
    }
}
