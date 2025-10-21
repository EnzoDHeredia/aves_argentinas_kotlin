package com.example.avesargentinas.ui.bird

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView

class BirdInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird_info)

        setupToolbar()
        bindDataFromIntent()

        val backBtn: ImageButton? = findViewById(R.id.btnBackBirdInfo)
        backBtn?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.bird_info_title)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindDataFromIntent() {
        val commonName = intent.getStringExtra(EXTRA_COMMON_NAME)
        val scientificName = intent.getStringExtra(EXTRA_SCIENTIFIC_NAME)
        val confidence = intent.getFloatExtra(EXTRA_CONFIDENCE, -1f)
        val distUri = intent.getStringExtra(EXTRA_DISTRIBUTION_URI)?.let { Uri.parse(it) }
        val popUri = intent.getStringExtra(EXTRA_POPULATION_URI)?.let { Uri.parse(it) }

        val title: TextView = findViewById(R.id.txtTitle)
        val scientific: TextView = findViewById(R.id.txtScientific)
        val confidenceView: TextView = findViewById(R.id.txtConfidence)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        // Título principal en card
        title.text = commonName ?: "-"
        scientific.text = scientificName?.let { getString(R.string.observation_scientific_format, it) } ?: ""

        if (confidence >= 0) {
            confidenceView.text = getString(R.string.observation_confidence_format, confidence * 100)
            confidenceView.alpha = 1f
        } else {
            confidenceView.text = ""
            confidenceView.alpha = 0.6f
        }

        // Load images if provided; otherwise placeholder
        val placeholder = R.drawable.ic_image_placeholder

        // Resolve distribution image
        val (distSource, popSource) = resolveDefaultImages(scientificName)

        // Preparar pager con dos páginas (Distribución / Abundancia)
        val pages = listOf(distUri ?: distSource, popUri ?: popSource)
        viewPager.adapter = ImagePagerAdapter(pages, placeholder)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.bird_info_distribution) else getString(R.string.bird_info_population)
        }.attach()

        // Poner el nombre común como título del toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = commonName ?: getString(R.string.bird_info_title)
    }

    // Try to resolve images by convention:
    // drawable-nodpi: bird_<scientific>_dist.webp / bird_<scientific>_pop.webp
    // or assets/birds/<scientific>_dist.webp, <scientific>_pop.webp
    private fun resolveDefaultImages(scientificName: String?): Pair<Any?, Any?> {
        val sci = scientificName
            ?.lowercase()
            ?.replace(" ", "_")
            ?.replace("-", "_")
            ?: return Pair(null, null)

        val distCandidates = listOf("bird_${sci}_dist", "${sci}_dist")
        val popCandidates  = listOf("bird_${sci}_pop",  "${sci}_pop")

        fun findResId(candidates: List<String>): Int {
            for (name in candidates) {
                val id = resources.getIdentifier(name, "drawable", packageName)
                if (id != 0) return id
            }
            return 0
        }

        val distResId = findResId(distCandidates)
        val popResId = findResId(popCandidates)

        val dist: Any? = if (distResId != 0) distResId else "file:///android_asset/birds/${sci}_dist.webp"
        val pop: Any? = if (popResId != 0) popResId else "file:///android_asset/birds/${sci}_pop.webp"

        return Pair(dist, pop)
    }

    private class ImagePagerAdapter(
        private val sources: List<Any?>,
        private val placeholder: Int
    ) : RecyclerView.Adapter<ImagePagerAdapter.Holder>() {
        class Holder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val iv = LayoutInflater.from(parent.context).inflate(R.layout.pager_page_image, parent, false) as ImageView
            return Holder(iv)
        }

        override fun getItemCount(): Int = sources.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val ctx = holder.itemView.context
            Glide.with(ctx)
                .load(sources[position])
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.imageView)
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
