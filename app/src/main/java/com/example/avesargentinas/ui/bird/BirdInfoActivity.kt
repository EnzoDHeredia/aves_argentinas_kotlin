package com.example.avesargentinas.ui.bird

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.ui.log.ImageZoomActivity
import com.google.android.material.appbar.MaterialToolbar

class BirdInfoActivity : BaseActivity() {

    private var distributionImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            ThemeManager.applySavedTheme(this)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_bird_info)

            setupToolbar()
            bindDataFromIntent()

            val backBtn: ImageButton? = findViewById(R.id.btnBackBirdInfo)
            backBtn?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        } catch (e: Exception) {
            Log.e("BirdInfoActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error al cargar informacion: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.bird_info_title)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindDataFromIntent() {
        try {
            val commonName = intent.getStringExtra(EXTRA_COMMON_NAME)
            val scientificName = intent.getStringExtra(EXTRA_SCIENTIFIC_NAME)
            val distUri = intent.getStringExtra(EXTRA_DISTRIBUTION_URI)?.let { Uri.parse(it) }

            Log.d("BirdInfoActivity", "Binding data: $commonName / $scientificName")

            val imageDistribution: ImageView = findViewById(R.id.imageDistribution)
            val placeholder = R.drawable.ic_image_placeholder

            val distSource = distUri ?: resolveDefaultDistributionImage(scientificName)
            distributionImageUri = when (distSource) {
                is Uri -> distSource.toString()
                is String -> distSource
                is Int -> "android.resource://$packageName/$distSource"
                else -> null
            }

            Glide.with(this)
                .load(distSource)
                .placeholder(placeholder)
                .error(placeholder)
                .into(imageDistribution)

            imageDistribution.setOnClickListener {
                openImageZoom()
            }

            val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
            toolbar.title = commonName ?: getString(R.string.bird_info_title)

            Log.d("BirdInfoActivity", "Data binding completed successfully")
        } catch (e: Exception) {
            Log.e("BirdInfoActivity", "Error in bindDataFromIntent", e)
            Toast.makeText(this, "Error al configurar la vista: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Try to resolve distribution image by convention:
    // drawable-nodpi: bird_<scientific>_dist.webp
    // or assets/birds/<scientific>_dist.webp
    private fun resolveDefaultDistributionImage(scientificName: String?): Any? {
        val sci = scientificName
            ?.lowercase()
            ?.replace(" ", "_")
            ?.replace("-", "_")
            ?: return null

        val distCandidates = listOf("bird_${sci}_dist", "${sci}_dist")

        for (name in distCandidates) {
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) return id
        }

        return "file:///android_asset/birds/${sci}_dist.webp"
    }

    private fun openImageZoom() {
        val imageUri = distributionImageUri
        if (imageUri != null) {
            Log.d("BirdInfoActivity", "Opening distribution image zoom: $imageUri")
            val intent = Intent(this, ImageZoomActivity::class.java)
            intent.putExtra(ImageZoomActivity.EXTRA_IMAGE_URI, imageUri)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Imagen no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_COMMON_NAME = "extra_common_name"
        const val EXTRA_SCIENTIFIC_NAME = "extra_scientific_name"
        const val EXTRA_CONFIDENCE = "extra_confidence"
        const val EXTRA_DISTRIBUTION_URI = "extra_distribution_uri"
        const val EXTRA_POPULATION_URI = "extra_population_uri"
    }
}