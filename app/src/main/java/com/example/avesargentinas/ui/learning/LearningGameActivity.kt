package com.example.avesargentinas.ui.learning

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.avesargentinas.BaseActivity
import com.example.avesargentinas.R
import com.example.avesargentinas.ThemeManager
import com.example.avesargentinas.ui.bird.BirdInfoActivity
import com.example.avesargentinas.ui.log.ImageZoomActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class LearningGameActivity : BaseActivity() {
    private lateinit var model: LearningGameViewModel
    private lateinit var card: View
    private lateinit var photo: ImageView
    private var flipAnimation: AnimatorSet? = null
    private var busy = false
    private var openingBirdInfo = false
    private var imageToken: Long? = null
    private var photoReady = false
    private var celebratedSummary: LearningSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning_game)
        findViewById<View>(R.id.insets_container).isSaveFromParentEnabled = false
        model = ViewModelProvider(this)[LearningGameViewModel::class.java]
        card = findViewById(R.id.learning_card)
        photo = findViewById(R.id.learning_photo)
        card.cameraDistance = 8000 * resources.displayMetrics.density
        bindCardSwipe()

        findViewById<ImageView?>(R.id.learning_species_preview_photo)?.let { preview ->
            Glide.with(this).load("file:///android_asset/learning/photos/photo-001.webp")
                .centerCrop().into(preview)
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.learning_toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val root = findViewById<View>(R.id.insets_container)
        // Esta pantalla distribuye todos los insets en la raíz, una sola vez.
        root.setTag(R.id.tag_insets_listener_attached, true)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (openingBirdInfo) return
                val selector = model.state.value.session.screen == LearningScreen.SELECTOR
                cancelFlip()
                model.clearSession()
                if (selector) finish() else render(model.state.value)
            }
        })
        findViewById<View>(R.id.learning_questions).setOnClickListener { start(LearningMode.QUESTIONS) }
        findViewById<View>(R.id.learning_species).setOnClickListener { start(LearningMode.SPECIES) }
        findViewById<View>(R.id.learning_review).setOnClickListener {
            if (model.state.value.session.canReview) { model.startReview(); render(model.state.value) }
        }
        findViewById<View>(R.id.learning_again).setOnClickListener {
            val state = model.state.value.session
            if (state.screen == LearningScreen.SUMMARY) { model.startRound(state.mode!!); render(model.state.value) }
        }
        findViewById<View>(R.id.learning_choose_mode).setOnClickListener {
            model.clearSession()
            render(model.state.value)
        }
        findViewById<View>(R.id.learning_exit).setOnClickListener {
            model.clearSession()
            finish()
        }
        render(model.state.value)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { if (!busy) render(it) }
            }
        }
    }

    private fun start(mode: LearningMode) {
        if (model.state.value.isLoading || model.state.value.session.screen != LearningScreen.SELECTOR) return
        model.startRound(mode)
        render(model.state.value)
    }

    private fun render(game: LearningGameState) {
        val state = game.session
        findViewById<View>(R.id.learning_selector).isVisible = state.screen == LearningScreen.SELECTOR
        findViewById<View>(R.id.learning_round).isVisible = state.screen == LearningScreen.CARD
        findViewById<View>(R.id.learning_summary).isVisible = state.screen == LearningScreen.SUMMARY
        listOf(
            Triple(LearningMode.QUESTIONS, R.id.learning_questions, R.id.learning_questions_availability),
            Triple(LearningMode.SPECIES, R.id.learning_species, R.id.learning_species_availability)
        ).forEach { (mode, buttonId, availabilityId) ->
            val count = if (state.mode == mode && state.notice != null) state.availableCount
                else game.availableCounts[mode] ?: 0
            findViewById<View>(buttonId).isEnabled = !game.isLoading && count > 0
            findViewById<TextView>(availabilityId).text = when {
                game.isLoading -> getString(R.string.learning_loading)
                count == 0 -> getString(R.string.learning_unavailable)
                count < 10 -> getString(R.string.learning_insufficient, count)
                else -> getString(R.string.learning_available)
            }
        }
        val notice = when {
            game.loadFailed -> getString(R.string.learning_load_failed)
            state.notice == LearningNotice.UNAVAILABLE_CONTENT -> getString(R.string.learning_unavailable)
            state.notice == LearningNotice.CARD_UNAVAILABLE -> getString(R.string.learning_card_unavailable)
            state.notice == LearningNotice.INSUFFICIENT_CONTENT -> getString(R.string.learning_insufficient, state.availableCount)
            else -> ""
        }
        findViewById<TextView>(R.id.learning_notice).apply { text = notice; isVisible = notice.isNotEmpty() }
        if (state.screen == LearningScreen.CARD) renderCard(state) else {
            imageToken = null
            resetCardSwipe()
            Glide.with(this).clear(photo)
            photo.setImageDrawable(null)
            findViewById<ImageView>(R.id.learning_photo_backdrop)?.let {
                Glide.with(this).clear(it)
                it.setImageDrawable(null)
            }
            // No conservar una respuesta abandonada en una vista oculta.
            findViewById<TextView>(R.id.learning_card_text).text = ""
            findViewById<TextView>(R.id.learning_back_text).text = ""
            findViewById<TextView>(R.id.learning_card_detail).text = ""
            card.contentDescription = null
        }
        if (state.screen != LearningScreen.SUMMARY) celebratedSummary = null
        state.summary?.let { summary ->
            findViewById<TextView>(R.id.learning_total).text = getString(R.string.learning_total, summary.total)
            findViewById<TextView>(R.id.learning_known_total).text = getString(R.string.learning_known_total, summary.known)
            findViewById<TextView>(R.id.learning_review_total).text = getString(R.string.learning_review_total, summary.review)
            renderSummaryChartAndConfetti(summary)
            findViewById<View>(R.id.learning_review).isVisible = state.canReview
            val style = if (state.canReview) R.style.Widget_AvesArgentinas_Button_Outlined
                else R.style.Widget_AvesArgentinas_Button_Primary
            val appearance = obtainStyledAttributes(style, com.google.android.material.R.styleable.MaterialButton)
            val textAppearance = obtainStyledAttributes(style, intArrayOf(android.R.attr.textColor))
            findViewById<MaterialButton>(R.id.learning_again).apply {
                backgroundTintList = appearance.getColorStateList(com.google.android.material.R.styleable.MaterialButton_backgroundTint)
                setTextColor(textAppearance.getColorStateList(0))
                strokeColor = appearance.getColorStateList(com.google.android.material.R.styleable.MaterialButton_strokeColor)
                strokeWidth = appearance.getDimensionPixelSize(com.google.android.material.R.styleable.MaterialButton_strokeWidth, 0)
                cornerRadius = appearance.getDimensionPixelSize(com.google.android.material.R.styleable.MaterialButton_cornerRadius, 0)
                rippleColor = appearance.getColorStateList(com.google.android.material.R.styleable.MaterialButton_rippleColor)
            }
            appearance.recycle()
            textAppearance.recycle()
        }
    }

    private fun renderSummaryChartAndConfetti(summary: LearningSummary) {
        val chart = findViewById<PieChart>(R.id.learning_donut_chart)
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        if (summary.known > 0) {
            entries.add(PieEntry(summary.known.toFloat(), getString(R.string.learning_known)))
            colors.add(getColor(R.color.brand_primary))
        }
        if (summary.review > 0) {
            entries.add(PieEntry(summary.review.toFloat(), getString(R.string.learning_mark)))
            colors.add(getColor(R.color.brand_secondary))
        }
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, ""))
            colors.add(getColor(R.color.brand_outline))
        }
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = if (entries.size > 1) 4f else 0f
            setDrawValues(false)
        }
        val pct = if (summary.total > 0) (summary.known * 100) / summary.total else 0
        findViewById<TextView>(R.id.learning_summary_congrats).text = when {
            pct >= 80 -> getString(R.string.learning_summary_excellent)
            pct >= 50 -> getString(R.string.learning_summary_great)
            else -> getString(R.string.learning_summary_keep_practicing)
        }
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 72f
            transparentCircleRadius = 76f
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(false)
            setTouchEnabled(false)
            isRotationEnabled = false
            centerText = "$pct%\n${summary.known}/${summary.total}"
            setCenterTextSize(18f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setCenterTextColor(getColor(R.color.text_primary))
            data = PieData(dataSet)
            invalidate()
        }
        if (celebratedSummary != summary) {
            celebratedSummary = summary
            if (animationsEnabled()) {
                chart.animateY(600)
                findViewById<KonfettiView>(R.id.learning_confetti).start(
                    Party(
                        speed = 0f,
                        maxSpeed = 28f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(
                            getColor(R.color.brand_primary),
                            getColor(R.color.brand_secondary),
                            getColor(R.color.brand_tertiary),
                            0xFF4CAF50.toInt()
                        ),
                        emitter = Emitter(duration = 1600, TimeUnit.MILLISECONDS).max(65),
                        position = Position.Relative(0.5, 0.18)
                    )
                )
            }
        }
    }

    private fun renderCard(state: LearningState) {
        val current = state.currentCard ?: return
        val token = state.cardToken ?: return
        val back = state.face == CardFace.BACK
        findViewById<TextView>(R.id.learning_progress).text = getString(
            if (state.pass == LearningPass.REVIEW) R.string.learning_review_progress else R.string.learning_progress,
            state.position, state.total
        )
        findViewById<View>(R.id.learning_card_header).minimumHeight = resources.getDimensionPixelSize(
            if (current is LearningCard.Photo) R.dimen.learning_species_header_min_height
            else R.dimen.learning_question_header_min_height
        )
        val isPhoto = current is LearningCard.Photo
        findViewById<View>(R.id.learning_photo_container).isVisible = isPhoto
        photo.isVisible = isPhoto
        findViewById<View>(R.id.learning_photo_backdrop)?.isVisible = isPhoto
        findViewById<View>(R.id.learning_scrim).isVisible = isPhoto
        findViewById<View>(R.id.learning_question_background).isVisible = !isPhoto

        val title = findViewById<TextView>(R.id.learning_card_text)
        val answer = findViewById<TextView>(R.id.learning_back_text)
        val detail = findViewById<TextView>(R.id.learning_card_detail)
        title.text = when (current) {
            is LearningCard.Question -> current.question
            is LearningCard.Photo -> getString(R.string.learning_species_prompt)
        }
        answer.text = when (current) {
            is LearningCard.Question -> current.answer
            is LearningCard.Photo -> formatCommonName(current.commonName)
        }
        detail.text = when (current) {
            is LearningCard.Question -> current.clarification.orEmpty()
            is LearningCard.Photo -> current.scientificName
        }
        detail.isVisible = detail.text.isNotEmpty()
        detail.setTypeface(null, if (isPhoto) Typeface.ITALIC else Typeface.NORMAL)
        val hint = getString(if (back) R.string.learning_front else R.string.learning_reveal)
        findViewById<View>(R.id.learning_front_panel).visibility = if (!back) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.learning_back_panel).visibility = if (back) View.VISIBLE else View.INVISIBLE
        card.contentDescription = listOf(
            if (isPhoto) getString(R.string.learning_photo_description) else "",
            if (back) answer.text else title.text, if (back) detail.text else "", hint
        ).filter { it.isNotEmpty() }.joinToString(". ")
        ViewCompat.replaceAccessibilityAction(card, androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
            hint, null)
        card.setOnClickListener { flip(token) }
        photo.setOnClickListener { openPhotoZoom(token) }
        photo.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        findViewById<View>(R.id.learning_reveal).setOnClickListener { flip(token) }
        findViewById<View>(R.id.learning_bird_info).setOnClickListener { openBirdInfo(token) }
        findViewById<View>(R.id.learning_known).setOnClickListener { assess(token, SelfAssessment.KNOWN) }
        findViewById<View>(R.id.learning_mark).setOnClickListener { assess(token, SelfAssessment.REVIEW) }
        if (imageToken != token) {
            imageToken = token
            resetCardSwipe()
            photoReady = !isPhoto
            Glide.with(this).clear(photo)
            photo.setImageDrawable(null)
            findViewById<ImageView>(R.id.learning_photo_backdrop)?.let {
                Glide.with(this).clear(it)
                it.setImageDrawable(null)
            }
            if (isPhoto) loadPhoto(current as LearningCard.Photo, token)
            findViewById<androidx.core.widget.NestedScrollView>(R.id.learning_scroll).scrollTo(0, 0)
        }
        updateInteraction()
    }

    private fun formatCommonName(name: String): String =
        name.trim().split(Regex("\\s+")).joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

    private fun bindCardSwipe() {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var downX = 0f
        var downY = 0f
        var swiping = false

        val touchHandler: (View, MotionEvent) -> Boolean = { view, event ->
            val state = model.state.value.session
            val canAssessSwipe = !busy && !openingBirdInfo && photoReady &&
                state.screen == LearningScreen.CARD && state.face == CardFace.BACK && state.canAssess
            val isPhotoTouch = view === photo

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    swiping = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!swiping && abs(dx) > slop && abs(dx) > abs(dy)) {
                        if (canAssessSwipe) {
                            swiping = true
                            card.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    if (swiping) {
                        val width = card.width.coerceAtLeast(1)
                        val progress = (abs(dx) / (width * 0.35f)).coerceIn(0f, 1f)
                        card.translationX = dx
                        card.rotation = (dx / width) * 14f
                        card.alpha = 1f - progress * 0.12f

                        val stampKnown = findViewById<View>(R.id.learning_stamp_known)
                        val stampReview = findViewById<View>(R.id.learning_stamp_review)
                        val knownBtn = findViewById<View>(R.id.learning_known)
                        val markBtn = findViewById<View>(R.id.learning_mark)
                        if (dx > 0f) {
                            stampKnown?.alpha = progress
                            stampReview?.alpha = 0f
                            knownBtn?.scaleX = 1f + 0.12f * progress
                            knownBtn?.scaleY = 1f + 0.12f * progress
                            markBtn?.scaleX = 1f
                            markBtn?.scaleY = 1f
                            markBtn?.alpha = 1f - 0.45f * progress
                            knownBtn?.alpha = 1f
                        } else {
                            stampReview?.alpha = progress
                            stampKnown?.alpha = 0f
                            markBtn?.scaleX = 1f + 0.12f * progress
                            markBtn?.scaleY = 1f + 0.12f * progress
                            knownBtn?.scaleX = 1f
                            knownBtn?.scaleY = 1f
                            knownBtn?.alpha = 1f - 0.45f * progress
                            markBtn?.alpha = 1f
                        }
                        true
                    } else false
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    val token = state.cardToken
                    resetAssessmentButtonFeedback()

                    if (swiping) {
                        swiping = false
                        val threshold = (card.width * 0.25f).coerceAtLeast(48f * resources.displayMetrics.density)
                        if (canAssessSwipe && token != null && abs(dx) >= threshold) {
                            val assessment = if (dx > 0f) SelfAssessment.KNOWN else SelfAssessment.REVIEW
                            if (!animationsEnabled()) {
                                resetCardSwipe()
                                assess(token, assessment)
                            } else {
                                busy = true
                                updateInteraction()
                                val targetX = if (dx > 0f) card.width * 1.25f else -card.width * 1.25f
                                val targetRot = if (dx > 0f) 20f else -20f
                                card.animate()
                                    .translationX(targetX)
                                    .rotation(targetRot)
                                    .alpha(0f)
                                    .setDuration(160)
                                    .withEndAction {
                                        resetCardSwipe()
                                        busy = false
                                        assess(token, assessment)
                                    }.start()
                            }
                        } else if (animationsEnabled()) {
                            card.animate().alpha(1f).setDuration(120).start()
                            SpringAnimation(card, DynamicAnimation.TRANSLATION_X, 0f).apply {
                                spring = SpringForce(0f).apply {
                                    dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                                    stiffness = SpringForce.STIFFNESS_LOW
                                }
                                start()
                            }
                            SpringAnimation(card, DynamicAnimation.ROTATION, 0f).apply {
                                spring = SpringForce(0f).apply {
                                    dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                                    stiffness = SpringForce.STIFFNESS_LOW
                                }
                                start()
                            }
                            findViewById<View>(R.id.learning_stamp_known)?.animate()?.alpha(0f)?.setDuration(120)?.start()
                            findViewById<View>(R.id.learning_stamp_review)?.animate()?.alpha(0f)?.setDuration(120)?.start()
                        } else {
                            resetCardSwipe()
                        }
                        true
                    } else {
                        // Toque simple (tap)
                        if (abs(dx) <= slop && abs(dy) <= slop && token != null) {
                            view.performClick()
                            if (isPhotoTouch || (state.currentCard is LearningCard.Photo && view !== card)) {
                                openPhotoZoom(token)
                            } else {
                                flip(token)
                            }
                            true
                        } else false
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (swiping) {
                        swiping = false
                        resetCardSwipe()
                        true
                    } else false
                }
                else -> false
            }
        }

        card.setOnTouchListener { v, e -> touchHandler(v, e) }
        photo.setOnTouchListener { v, e -> touchHandler(v, e) }
    }

    private fun resetAssessmentButtonFeedback() {
        listOf(R.id.learning_known, R.id.learning_mark).forEach { id ->
            findViewById<View>(id)?.apply {
                scaleX = 1f
                scaleY = 1f
            }
        }
    }

    private fun resetCardSwipe() {
        card.animate().cancel()
        card.translationX = 0f
        card.rotation = 0f
        card.alpha = 1f
        findViewById<View>(R.id.learning_stamp_known)?.let { it.animate().cancel(); it.alpha = 0f }
        findViewById<View>(R.id.learning_stamp_review)?.let { it.animate().cancel(); it.alpha = 0f }
        resetAssessmentButtonFeedback()
    }

    private fun setFaceVisibility(id: Int, active: Boolean) {
        findViewById<View>(id)?.apply {
            visibility = if (active) View.VISIBLE else View.INVISIBLE
            importantForAccessibility = if (active) View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
    }

    private fun loadPhoto(current: LearningCard.Photo, token: Long) {
        val assetUri = "file:///android_asset/${current.imagePath}"
        findViewById<ImageView>(R.id.learning_photo_backdrop)?.let { backdrop ->
            Glide.with(this).load(assetUri)
                .override(120, 180)
                .centerCrop()
                .dontAnimate()
                .into(backdrop)
        }

        Glide.with(this).load(assetUri)
            .override(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            .fitCenter()
            .dontAnimate()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(error: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    photo.post {
                        if (!isDestroyed && imageToken == token && this@LearningGameActivity.model.state.value.session.cardToken == token) {
                            cancelFlip()
                            this@LearningGameActivity.model.discardCard(token)
                            render(this@LearningGameActivity.model.state.value)
                        }
                    }
                    return false
                }
                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, source: DataSource, isFirstResource: Boolean): Boolean {
                    if (imageToken == token) { photoReady = true; updateInteraction() }
                    return false
                }
            }).into(photo)
    }

    private fun updateInteraction() {
        val state = model.state.value.session
        val ready = !busy && !openingBirdInfo && photoReady && state.screen == LearningScreen.CARD
        card.isEnabled = ready
        card.importantForAccessibility = if (busy) View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS else View.IMPORTANT_FOR_ACCESSIBILITY_YES
        photo.isEnabled = ready && state.currentCard is LearningCard.Photo
        photo.isClickable = ready && state.currentCard is LearningCard.Photo

        val isPhoto = state.currentCard is LearningCard.Photo
        val isBack = state.face == CardFace.BACK
        val showBirdInfo = isBack && isPhoto

        findViewById<View>(R.id.learning_bird_info).apply {
            visibility = when {
                showBirdInfo -> View.VISIBLE
                isPhoto -> View.INVISIBLE
                else -> View.GONE
            }
            importantForAccessibility = if (showBirdInfo && !busy) View.IMPORTANT_FOR_ACCESSIBILITY_YES
                else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            isEnabled = ready && showBirdInfo
        }

        setFaceVisibility(R.id.learning_reveal, !isBack)
        setFaceVisibility(R.id.learning_assessment, isBack)

        findViewById<View>(R.id.learning_reveal)?.apply {
            isEnabled = ready && !isBack
            contentDescription = getString(R.string.learning_reveal)
        }

        val assessReady = ready && isBack && state.canAssess
        findViewById<View>(R.id.learning_known)?.apply {
            isEnabled = assessReady
            importantForAccessibility = if (isBack && !busy) View.IMPORTANT_FOR_ACCESSIBILITY_YES
                else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }

        findViewById<View>(R.id.learning_mark)?.apply {
            isEnabled = assessReady
            importantForAccessibility = if (isBack && !busy) View.IMPORTANT_FOR_ACCESSIBILITY_YES
                else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
    }

    private fun assess(token: Long, assessment: SelfAssessment) {
        val state = model.state.value.session
        if (busy || openingBirdInfo || !photoReady || !state.canAssess || state.cardToken != token) return
        busy = true
        updateInteraction()
        model.assess(token, assessment)
        busy = false
        render(model.state.value)
    }

    private fun flip(token: Long) {
        val state = model.state.value.session
        if (busy || openingBirdInfo || !photoReady || state.screen != LearningScreen.CARD || state.cardToken != token) return
        busy = true
        model.flip(token)
        if (!animationsEnabled()) {
            busy = false
            render(model.state.value)
            focusCard()
            return
        }
        updateInteraction()
        val first = ObjectAnimator.ofFloat(card, View.ROTATION_Y, 0f, 90f).setDuration(150)
        val second = ObjectAnimator.ofFloat(card, View.ROTATION_Y, -90f, 0f).setDuration(150)
        val animation = AnimatorSet()
        flipAnimation = animation
        first.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator) {
                if (flipAnimation === animation) render(model.state.value)
            }
        })
        animation.playSequentially(first, second)
        animation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator) {
                if (flipAnimation !== animation) return
                flipAnimation = null
                busy = false
                card.rotationY = 0f
                render(model.state.value)
                focusCard()
            }
        })
        animation.start()
    }

    private fun focusCard() {
        card.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
    }

    private fun openPhotoZoom(expectedToken: Long) {
        val state = model.state.value.session
        val current = state.currentCard as? LearningCard.Photo ?: return
        if (busy || openingBirdInfo || !photoReady || state.screen != LearningScreen.CARD ||
            state.cardToken != expectedToken) return
        openingBirdInfo = true
        updateInteraction()
        try {
            startActivity(Intent(this, ImageZoomActivity::class.java)
                .putExtra(ImageZoomActivity.EXTRA_IMAGE_URI, "file:///android_asset/${current.imagePath}"))
        } catch (_: RuntimeException) {
            openingBirdInfo = false
            updateInteraction()
        }
    }

    private fun openBirdInfo(expectedToken: Long) {
        val state = model.state.value.session
        val current = state.currentCard as? LearningCard.Photo ?: return
        if (busy || openingBirdInfo || !photoReady || state.screen != LearningScreen.CARD ||
            state.face != CardFace.BACK || state.cardToken != expectedToken) return
        openingBirdInfo = true
        updateInteraction()
        try {
            startActivity(Intent(this, BirdInfoActivity::class.java)
                .putExtra(BirdInfoActivity.EXTRA_COMMON_NAME, current.commonName)
                .putExtra(BirdInfoActivity.EXTRA_SCIENTIFIC_NAME, current.scientificName))
        } catch (_: RuntimeException) {
            openingBirdInfo = false
            updateInteraction()
            Toast.makeText(this, R.string.learning_bird_info_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun animationsEnabled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f) > 0f
    } catch (_: SecurityException) { false }

    private fun cancelFlip() {
        val animation = flipAnimation
        flipAnimation = null
        animation?.cancel()
        busy = false
        card.rotationY = 0f
        resetCardSwipe()
    }

    override fun onStop() {
        cancelFlip()
        render(model.state.value)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        openingBirdInfo = false
        updateInteraction()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val lightBars = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightBars
            isAppearanceLightNavigationBars = lightBars
        }
        ViewCompat.requestApplyInsets(findViewById(R.id.insets_container))
    }
}
