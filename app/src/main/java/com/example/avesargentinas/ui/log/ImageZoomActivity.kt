package com.example.avesargentinas.ui.log

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.avesargentinas.R
import com.jsibbold.zoomage.ZoomageView

/**
 * Activity para visualizar una imagen con zoom.
 * Permite hacer zoom con gestos táctiles.
 * Presiona el botón atrás para cerrar.
 */
class ImageZoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_zoom)

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUriString == null) {
            finish()
            return
        }

        val imgZoom: ZoomageView = findViewById(R.id.imgZoom)

        // Cargar imagen
        Glide.with(this)
            .load(Uri.parse(imageUriString))
            .into(imgZoom)

        // Configurar zoom
        imgZoom.apply {
            setScaleRange(1f, 10f)
            setDoubleTapToZoomScaleFactor(3f)
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
