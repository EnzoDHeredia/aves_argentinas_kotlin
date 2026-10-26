package com.example.avesargentinas.ui.bird

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.view.ViewGroup
import android.util.TypedValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.view.WindowCompat
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.ui.log.ImageZoomActivity
import com.google.android.material.appbar.MaterialToolbar

class BirdInfoActivity : BaseActivity() {

    private var distributionImageUri: String? = null
    private var distributionPage: View? = null
    private var informationPage: View? = null
    private var informationLoadId = 0
    private var photoUri: String? = null
    private lateinit var imageRequests: RequestManager
    private lateinit var pager: ViewPager2
    private var mediator: TabLayoutMediator? = null
    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            (pager.adapter as BirdInfoPagerAdapter).selectedPage = position
        }
    }

    override fun onResume() {
        super.onResume()
        // Desde API 35 la barra de estado es transparente; antes conserva el fondo oscuro del tema.
        if (Build.VERSION.SDK_INT < 35) return
        val light = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            ThemeManager.applySavedTheme(this)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_bird_info)

            imageRequests = Glide.with(this)
            setupToolbar()
            pager = findViewById(R.id.birdInfoPager)
            pager.adapter = BirdInfoPagerAdapter(::bindDistribution, ::clearDistribution, ::bindInformation, ::clearInformation)
            pager.offscreenPageLimit = 1
            pager.registerOnPageChangeCallback(pageCallback)
            val tabs: TabLayout = findViewById(R.id.birdInfoTabs)
            // Measure multiline labels before reducing the initial space for enlarged text.
            tabs.layoutParams.height = (48 * resources.displayMetrics.density *
                resources.configuration.fontScale.coerceAtLeast(1f)).toInt()
            val initialTab = savedInstanceState?.getInt(STATE_TAB) ?: 0
            mediator = TabLayoutMediator(tabs, pager) { tab, position ->
                val label = getString(if (position == 0) R.string.bird_info_information else R.string.bird_info_distribution)
                tab.text = label
                tab.customView = TextView(this).apply {
                    text = label
                    textSize = 15f
                    gravity = Gravity.CENTER
                    setTextColor(tabs.tabTextColors)
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    setTypeface(typeface, if (position == initialTab) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                }
            }.also { it.attach() }
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    (tab?.customView as? TextView)?.setTypeface(null, android.graphics.Typeface.BOLD)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    (tab?.customView as? TextView)?.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
            tabs.doOnLayout {
                val density = resources.displayMetrics.density
                val labelHeight = (0 until tabs.tabCount).maxOf { index ->
                    (tabs.getTabAt(index)!!.customView as TextView).layout.height
                }
                tabs.layoutParams = tabs.layoutParams.apply {
                    height = maxOf((48 * density).toInt(), labelHeight + (16 * density).toInt())
                }
            }
            pager.setCurrentItem(initialTab, false)
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
        toolbar.title = null
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_TAB, pager.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mediator?.detach()
        if (::pager.isInitialized) {
            pager.unregisterOnPageChangeCallback(pageCallback)
            pager.adapter = null
        }
        super.onDestroy()
    }

    private fun bindDataFromIntent() {
        findViewById<TextView>(R.id.birdInfoName).text =
            intent.getStringExtra(EXTRA_COMMON_NAME) ?: getString(R.string.bird_info_title)
        val variant = intent.getStringExtra(EXTRA_VARIANT)?.trim()?.lowercase(java.util.Locale.ROOT)
        val sexLabel = when (variant) {
            "macho" -> "Macho"
            "hembra" -> "Hembra"
            else -> null
        }
        findViewById<TextView>(R.id.birdInfoName).text = buildString {
            append(intent.getStringExtra(EXTRA_COMMON_NAME) ?: getString(R.string.bird_info_title))
            sexLabel?.let { append("\n"); append(it) }
            if (!variant.isNullOrEmpty() && sexLabel == null) append("\n$variant")
        }
        informationPage?.let(::bindInformation)
        distributionPage?.let(::bindDistribution)
    }

    private fun bindInformation(page: View) {
        informationPage = page
        val content = page.findViewById<LinearLayout>(R.id.informationContent)
        val message = page.findViewById<TextView>(R.id.informationUnavailable)
        val image = page.findViewById<ImageView>(R.id.speciesPhoto)
        val common = intent.getStringExtra(EXTRA_COMMON_NAME).orEmpty()
        val scientific = intent.getStringExtra(EXTRA_SCIENTIFIC_NAME).orEmpty()
        val variant = intent.getStringExtra(EXTRA_VARIANT)
        imageRequests.clear(image)
        image.setOnClickListener(null)
        image.isEnabled = false
        image.isClickable = false
        photoUri = null
        content.visibility = View.GONE
        message.visibility = View.VISIBLE
        message.text = getString(R.string.bird_info_loading)
        val requestId = ++informationLoadId
        lifecycleScope.launch {
            val catalog = withContext(Dispatchers.IO) { SpeciesInfoLoader(this@BirdInfoActivity).load() }
            if (requestId != informationLoadId || informationPage !== page) return@launch
            val species = catalog.find(scientific)
            if (species == null) {
                message.text = if (catalog.issues.isEmpty()) getString(R.string.bird_info_information_unavailable)
                    else getString(R.string.bird_info_biological_error)
                return@launch
            }
            content.visibility = View.VISIBLE
            message.visibility = View.GONE
            val fields = page.findViewById<LinearLayout>(R.id.speciesFields)
            fields.removeAllViews()
            val photo = species.photoForVariant(variant)
            val specimen = page.findViewById<TextView>(R.id.specimenSex)
            val zoomHint = page.findViewById<View>(R.id.photoZoomHint)
            val selectedVariant = variant?.trim()?.lowercase(java.util.Locale.ROOT)
            val sexText = when {
                selectedVariant == "macho" || selectedVariant == "hembra" -> getString(R.string.bird_info_consulted_variant,
                    if (selectedVariant == "macho") "Macho" else "Hembra")
                photo?.sex != null -> getString(R.string.bird_info_photo_sex,
                    if (photo.sex == PhotoSex.MACHO) "Macho" else "Hembra")
                else -> ""
            }
            specimen.text = sexText
            specimen.visibility = if (sexText.isNotEmpty()) View.VISIBLE else View.GONE
            zoomHint?.setOnClickListener { if (photoUri != null) openPhotoZoom() }

            if (photo == null) {
                zoomHint?.visibility = View.GONE
                page.findViewById<TextView>(R.id.photoUnavailable)?.let { it.visibility = View.VISIBLE }
                message.visibility = View.GONE
            } else {
                zoomHint?.visibility = View.VISIBLE
                page.findViewById<TextView>(R.id.photoUnavailable)?.visibility = View.GONE
                val uri = "file:///android_asset/${photo.imagePath}"
                image.contentDescription = getString(R.string.bird_info_photo_zoom)
                imageRequests.load(uri).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            if (informationPage === page && requestId == informationLoadId) {
                                photoUri = null
                                image.isEnabled = false
                                image.isClickable = false
                                zoomHint?.visibility = View.GONE
                                page.findViewById<TextView>(R.id.photoUnavailable).visibility = View.VISIBLE
                                page.findViewById<TextView>(R.id.photoUnavailable).text = getString(R.string.bird_info_photo_unavailable)
                            }
                            return false
                        }
                        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            if (informationPage === page && requestId == informationLoadId) {
                                photoUri = uri
                                image.isEnabled = true
                                image.isClickable = true
                                zoomHint?.visibility = View.VISIBLE
                                image.setOnClickListener { openPhotoZoom() }
                            }
                            return false
                        }
                    }).into(image)
            }

            // 1. Identificación y taxonomía
            val (taxCard, taxContainer) = createSectionCard(getString(R.string.bird_info_section_taxonomy))
            val taxonomyFields = listOf(
                Pair(R.string.bird_info_field_common_name, species.commonName ?: common),
                Pair(R.string.bird_info_field_scientific_name, species.scientificName),
                Pair(R.string.bird_info_field_english_name, species.englishName),
                Pair(R.string.bird_info_field_family, species.family),
                Pair(R.string.bird_info_field_group, species.group),
                Pair(R.string.bird_info_field_order, species.order)
            )
            taxonomyFields.forEachIndexed { i, (labelRes, valStr) ->
                addFieldToSection(
                    taxContainer,
                    getString(labelRes),
                    valStr,
                    isItalic = (labelRes == R.string.bird_info_field_scientific_name),
                    addDividerAbove = (i > 0)
                )
            }
            fields.addView(taxCard)

            // 2. Distribución geográfica
            val (regCard, regContainer) = createSectionCard(getString(R.string.bird_info_section_regions))
            addFieldToSection(
                regContainer,
                getString(R.string.bird_info_field_regions),
                species.argentineRegions,
                addDividerAbove = false
            )
            fields.addView(regCard)

            // 3. Historia natural y ecología
            val (bioCard, bioContainer) = createSectionCard(getString(R.string.bird_info_section_biology))
            val bioFields = listOf(
                Pair(R.string.bird_info_field_description, species.description),
                Pair(R.string.bird_info_field_habitat, species.habitat),
                Pair(R.string.bird_info_field_diet, species.diet),
                Pair(R.string.bird_info_field_movements, species.seasonalMovements)
            )
            bioFields.forEachIndexed { i, (labelRes, valStr) ->
                addFieldToSection(
                    bioContainer,
                    getString(labelRes),
                    valStr,
                    addDividerAbove = (i > 0)
                )
            }
            fields.addView(bioCard)

            // 4. Estado de conservación
            val (consCard, consContainer) = createSectionCard(getString(R.string.bird_info_section_conservation))
            val globalText = species.conservationGlobal?.let { c ->
                buildString {
                    append(c.category)
                    append(" · ")
                    append(c.assessmentYear?.let { getString(R.string.bird_info_assessment_year, it) }
                        ?: getString(R.string.bird_info_information_unavailable))
                }
            }
            val nationalText = species.conservationNational?.let { c ->
                buildString {
                    append(c.category)
                    append(" · ")
                    append(c.assessmentYear?.let { getString(R.string.bird_info_assessment_year, it) }
                        ?: getString(R.string.bird_info_information_unavailable))
                }
            }
            addFieldToSection(consContainer, getString(R.string.bird_info_global_conservation), globalText, addDividerAbove = false)
            addFieldToSection(consContainer, getString(R.string.bird_info_national_conservation), nationalText, addDividerAbove = true)
            fields.addView(consCard)
        }
    }

    private fun createSectionCard(title: String): Pair<com.google.android.material.card.MaterialCardView, LinearLayout> {
        val density = resources.displayMetrics.density
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (12 * density).toInt()
            }
            setCardBackgroundColor(getColor(R.color.brand_surface_variant))
            radius = 16 * density
            strokeColor = getColor(R.color.brand_outline)
            strokeWidth = density.toInt().coerceAtLeast(1)
            cardElevation = 0f
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val header = TextView(this).apply {
            text = title
            setTextColor(getColor(R.color.brand_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            isAccessibilityHeading = true
            setPadding(
                (16 * density).toInt(),
                (14 * density).toInt(),
                (16 * density).toInt(),
                (8 * density).toInt()
            )
        }
        container.addView(header)
        card.addView(container)
        return Pair(card, container)
    }

    private fun addFieldToSection(
        sectionContainer: LinearLayout,
        label: String,
        value: String?,
        isItalic: Boolean = false,
        addDividerAbove: Boolean = true
    ) {
        val density = resources.displayMetrics.density
        if (addDividerAbove) {
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (1 * density).toInt().coerceAtLeast(1)
                ).apply {
                    marginStart = (16 * density).toInt()
                    marginEnd = (16 * density).toInt()
                }
                setBackgroundColor(getColor(R.color.brand_outline))
                alpha = 0.35f
            }
            sectionContainer.addView(divider)
        }
        val itemBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (16 * density).toInt(),
                (10 * density).toInt(),
                (16 * density).toInt(),
                (10 * density).toInt()
            )
        }
        itemBox.addView(TextView(this).apply {
            text = label
            setTextColor(getColor(R.color.text_secondary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        itemBox.addView(TextView(this).apply {
            text = value ?: getString(R.string.bird_info_information_unavailable)
            setTextColor(getColor(R.color.text_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            if (isItalic) {
                setTypeface(typeface, android.graphics.Typeface.ITALIC)
            }
            setPadding(0, (2 * density).toInt(), 0, 0)
        })
        sectionContainer.addView(itemBox)
    }

    private fun openPhotoZoom() {
        photoUri?.let { uri -> startActivity(Intent(this, ImageZoomActivity::class.java)
            .putExtra(ImageZoomActivity.EXTRA_IMAGE_URI, uri)) }
    }

    private fun clearDistribution(page: View) {
        if (distributionPage === page) {
            distributionPage = null
            distributionImageUri = null
        }
        page.findViewById<View>(R.id.mapZoomHint)?.setOnClickListener(null)
        page.findViewById<ImageView>(R.id.imageDistribution)?.let {
            it.setOnClickListener(null)
            it.isEnabled = false
            it.isClickable = false
            it.isFocusable = false
            imageRequests.clear(it)
        }
    }

    private fun clearInformation(page: View) {
        if (informationPage === page) {
            informationPage = null
            photoUri = null
            informationLoadId++
        }
        page.findViewById<ImageView>(R.id.speciesPhoto)?.let {
            it.setOnClickListener(null)
            it.isEnabled = false
            it.isClickable = false
            imageRequests.clear(it)
        }
    }

    private fun bindDistribution(page: View) {
        distributionPage = page
        try {
            val commonName = intent.getStringExtra(EXTRA_COMMON_NAME)
            val scientificName = intent.getStringExtra(EXTRA_SCIENTIFIC_NAME)
            val distUri = intent.getStringExtra(EXTRA_DISTRIBUTION_URI)?.let { Uri.parse(it) }

            Log.d("BirdInfoActivity", "Binding data: $commonName / $scientificName")

            val imageDistribution: ImageView = page.findViewById(R.id.imageDistribution)
            val unavailable: View = page.findViewById(R.id.distributionUnavailable)
            val placeholder = R.drawable.ic_image_placeholder

            val distSource = distUri ?: resolveDefaultDistributionImage(scientificName)
            val sourceUri = when (distSource) {
                is Uri -> distSource.toString()
                is String -> distSource
                is Int -> "android.resource://$packageName/$distSource"
                else -> null
            }

            val zoomHint = page.findViewById<View>(R.id.mapZoomHint)
            zoomHint?.setOnClickListener { openImageZoom() }

            imageRequests.clear(imageDistribution)
            distributionImageUri = null
            unavailable.visibility = View.GONE
            imageDistribution.contentDescription = getString(R.string.bird_info_map_zoom)
            imageDistribution.setOnClickListener { openImageZoom() }
            imageDistribution.isEnabled = false
            imageDistribution.isClickable = false
            imageDistribution.isFocusable = false
            imageDistribution.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            imageRequests
                .load(distSource)
                .placeholder(placeholder)
                .error(placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                    ): Boolean {
                        if (distributionPage !== page) return false
                        distributionImageUri = null
                        unavailable.visibility = View.VISIBLE
                        zoomHint?.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable, model: Any, target: Target<Drawable>?,
                        dataSource: DataSource, isFirstResource: Boolean
                    ): Boolean {
                        if (distributionPage !== page) return false
                        distributionImageUri = sourceUri
                        imageDistribution.isEnabled = sourceUri != null
                        imageDistribution.isClickable = sourceUri != null
                        imageDistribution.isFocusable = sourceUri != null
                        imageDistribution.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                        zoomHint?.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(imageDistribution)

            Log.d("BirdInfoActivity", "Data binding completed successfully")
        } catch (e: Exception) {
            Log.e("BirdInfoActivity", "Error in bindDataFromIntent", e)
            distributionImageUri = null
            page.findViewById<View>(R.id.distributionUnavailable).visibility = View.VISIBLE
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

        val alternate = when (sci) {
            "anas_versicolor" -> "spatula_versicolor_dist"
            "anas_cyanoptera" -> "spatula_cyanoptera_dist"
            "anas_sibilatrix" -> "mareca_sibilatrix_dist"
            else -> null
        }
        val distCandidates = listOfNotNull("bird_${sci}_dist", "${sci}_dist", alternate)

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
        private const val STATE_TAB = "bird_info_tab"
        const val EXTRA_COMMON_NAME = "extra_common_name"
        const val EXTRA_VARIANT = "extra_variant"
        const val EXTRA_SCIENTIFIC_NAME = "extra_scientific_name"
        const val EXTRA_CONFIDENCE = "extra_confidence"
        const val EXTRA_DISTRIBUTION_URI = "extra_distribution_uri"
        const val EXTRA_POPULATION_URI = "extra_population_uri"
    }
}
