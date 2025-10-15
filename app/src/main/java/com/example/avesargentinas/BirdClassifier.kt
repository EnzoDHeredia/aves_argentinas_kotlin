package com.example.avesargentinas

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedHashSet
import kotlin.math.exp

/**
 * Clase responsable de la clasificación de aves usando TensorFlow Lite
 */
class BirdClassifier(
    private val assets: AssetManager,
    private val numThreads: Int = 4
) {
    companion object {
        private const val TAG = "BirdClassifier"
        private const val MODEL_FILE = "birds_dynamic.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val LABELS_JSON_FILE = "labels_map.json"

        private val IGNORED_VARIANT_TOKENS = setOf(
            "macho",
            "hembra",
            "juvenil",
            "adulto",
            "juvenile",
            "adult",
            "male",
            "female"
        )
    }

    private lateinit var tflite: Interpreter
    private lateinit var labelInfos: List<LabelInfo>
    private val inputSize = 256
    private var numClasses = 0

    // Configuración de normalización ImageNet
    private val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    /**
     * Inicializa el modelo y carga las etiquetas
     * @return true si la inicialización fue exitosa
     */
    fun initialize(): Result<Unit> {
        return try {
            // Cargar modelo
            val opts = Interpreter.Options().apply { numThreads = this@BirdClassifier.numThreads }
            tflite = Interpreter(loadAssetBytes(MODEL_FILE), opts)
            
            try {
                tflite.resizeInput(0, intArrayOf(1, 3, inputSize, inputSize))
                tflite.allocateTensors()
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo redimensionar input: ${e.message}")
            }

            val regionalNames = loadRegionalNames()
            labelInfos = loadLabels(regionalNames)
            numClasses = labelInfos.size
            
            Log.i(TAG, "Modelo inicializado correctamente: $numClasses clases")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar modelo: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Clasifica una imagen y devuelve los resultados
     */
    fun classify(bitmap: Bitmap, topK: Int = 3): ClassificationResult {
        if (numClasses <= 0) {
            throw IllegalStateException("Classifier not initialized. Did you call initialize()?")
        }

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val input = bitmapToCHWBuffer(resized)
        val output = Array(1) { FloatArray(numClasses) }
        
        tflite.run(input, output)
        resized.recycle()
        
        val logits = output[0]
        val probabilities = softmax(logits)
        val topPredictions = getTopK(probabilities, topK)
        
        return ClassificationResult(
            predictions = topPredictions.map { (idx, prob) ->
                val info = labelInfos.getOrNull(idx)
                Prediction(
                    index = idx,
                    displayName = info?.displayName ?: getCleanLabel(idx),
                    probability = prob,
                    scientificName = info?.scientificName ?: getCleanLabel(idx),
                    regionalName = info?.regionalName,
                    variant = info?.variant
                )
            }
        )
    }

    /**
     * Obtiene el nombre limpio de una etiqueta
     */
    private fun getCleanLabel(index: Int): String {
    if (!::labelInfos.isInitialized || index !in labelInfos.indices) return "Clase $index"

    return labelInfos[index].displayName
    }

    /**
     * Convierte bitmap a ByteBuffer en formato CHW (Channel-Height-Width)
     */
    private fun bitmapToCHWBuffer(bitmap: Bitmap): ByteBuffer {
        val size = bitmap.width
        val buf = ByteBuffer.allocateDirect(1 * 3 * size * size * 4)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(size * size)
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)

        // Canal R
        for (i in pixels.indices) {
            val r = ((pixels[i] ushr 16) and 0xFF) / 255f
            buf.putFloat((r - mean[0]) / std[0])
        }
        // Canal G
        for (i in pixels.indices) {
            val g = ((pixels[i] ushr 8) and 0xFF) / 255f
            buf.putFloat((g - mean[1]) / std[1])
        }
        // Canal B
        for (i in pixels.indices) {
            val b = (pixels[i] and 0xFF) / 255f
            buf.putFloat((b - mean[2]) / std[2])
        }

        buf.rewind()
        return buf
    }

    /**
     * Aplica función softmax a los logits
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    /**
     * Obtiene los top K elementos con mayor probabilidad
     */
    private fun getTopK(probabilities: FloatArray, k: Int): List<Pair<Int, Float>> {
        return probabilities.indices
            .sortedByDescending { probabilities[it] }
            .take(k.coerceAtMost(probabilities.size))
            .map { it to probabilities[it] }
    }

    /**
     * Carga las etiquetas desde assets
     */
    private fun loadLabels(regionalNames: Map<String, String>): List<LabelInfo> {
        return try {
            assets.open(LABELS_FILE).use { input ->
                BufferedReader(InputStreamReader(input)).readLines()
                    .filter { it.isNotBlank() }
                    .map { line -> createLabelInfo(line.trim(), regionalNames) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando labels: ${e.message}")
            emptyList()
        }
    }

    /**
     * Carga un archivo de assets como ByteBuffer
     */
    private fun loadAssetBytes(filename: String): ByteBuffer {
        val bytes = assets.open(filename).use { it.readBytes() }
        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply { put(bytes); rewind() }
    }

    /**
     * Libera recursos del modelo
     */
    fun close() {
        if (::tflite.isInitialized) {
            tflite.close()
        }
    }

    private data class LabelInfo(
        val rawLabel: String,
        val scientificKey: String,
        val scientificName: String,
        val regionalName: String?,
        val variant: String?,
        val displayName: String
    )

    private fun createLabelInfo(
        rawLabel: String,
        regionalNames: Map<String, String>
    ): LabelInfo {
        val labelBody = rawLabel.substringAfter('.', rawLabel).lowercase()
        val tokens = labelBody.split('_').filter { it.isNotBlank() }
        val scientificKey = findRegionalKey(tokens, regionalNames) ?: labelBody
        val scientificTokens = scientificKey.split('_')

        val variantTokens = when {
            tokens.size > scientificTokens.size && tokens.take(scientificTokens.size) == scientificTokens ->
                tokens.drop(scientificTokens.size)
            tokens.size > scientificTokens.size && tokens.takeLast(scientificTokens.size) == scientificTokens ->
                tokens.dropLast(scientificTokens.size)
            else -> emptyList()
        }

        val rawRegionalName = regionalNames[scientificKey]
        val regionalName = rawRegionalName?.let { formatWords(it) }
        val scientificName = formatScientificName(scientificKey)

        val variant = variantTokens.takeIf { it.isNotEmpty() }
            ?.joinToString(" ") { formatWord(it) }
            ?.takeUnless { regionalName != null && it.equals(regionalName, ignoreCase = true) }
            ?.takeUnless { it.equals(scientificName, ignoreCase = true) }

        val displayBase = regionalName ?: scientificName
        val displayName = if (!variant.isNullOrBlank()) "$displayBase ($variant)" else displayBase

        return LabelInfo(
            rawLabel = rawLabel,
            scientificKey = scientificKey,
            scientificName = scientificName,
            regionalName = regionalName,
            variant = variant,
            displayName = displayName
        )
    }

    private fun findRegionalKey(
        tokens: List<String>,
        regionalNames: Map<String, String>
    ): String? {
        if (tokens.isEmpty()) return null

        val candidates = LinkedHashSet<String>()

        for (length in tokens.size downTo 2) {
            for (start in 0..tokens.size - length) {
                candidates += tokens.subList(start, start + length).joinToString("_")
            }
        }

        val filteredTokens = tokens.filterNot { it in IGNORED_VARIANT_TOKENS }
        if (filteredTokens.size >= 2 && filteredTokens != tokens) {
            for (length in filteredTokens.size downTo 2) {
                for (start in 0..filteredTokens.size - length) {
                    candidates += filteredTokens.subList(start, start + length).joinToString("_")
                }
            }
        }

        for (candidate in candidates) {
            if (regionalNames.containsKey(candidate)) return candidate
        }

        return null
    }

    private fun formatScientificName(key: String): String {
        return key.split('_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { formatWord(it) }
    }

    private fun formatWords(text: String): String {
        return text.split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { formatWord(it) }
    }

    private fun formatWord(word: String): String {
        if (word.isBlank()) return word
        val lower = word.lowercase()
        return lower.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase() else ch.toString()
        }
    }

    private fun loadRegionalNames(): Map<String, String> {
        return try {
            assets.open(LABELS_JSON_FILE).use { input ->
                val jsonText = input.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonText)
                val result = mutableMapOf<String, String>()
                jsonObject.keys().forEach { key ->
                    result[key.lowercase()] = jsonObject.getString(key)
                }
                result
            }
        } catch (e: Exception) {
            Log.w(TAG, "Nombres regionales no disponibles: ${e.message}")
            emptyMap()
        }
    }
}

/**
 * Resultado de la clasificación
 */
data class ClassificationResult(
    val predictions: List<Prediction>
) {
    val topPrediction: Prediction?
        get() = predictions.firstOrNull()
}

/**
 * Predicción individual
 */
data class Prediction(
    val index: Int,
    val displayName: String,
    val probability: Float,
    val scientificName: String,
    val regionalName: String?,
    val variant: String?
) {
    val confidencePercentage: Float
        get() = probability * 100f
}
