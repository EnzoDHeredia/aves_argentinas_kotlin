package com.example.avesargentinas.ui.sound

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.avesargentinas.BirdLabelsManager
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import org.json.JSONArray

internal data class SoundSpecies(
    val index: Int,
    val modelName: String,
    val canonicalName: String,
    val group: String,
    val spanishName: String?
)

class BirdSoundCatalog internal constructor(
    private val species: List<SoundSpecies>,
    private val regionalNames: Map<String, String>,
    private val maps: Map<String, String>,
    private val soundNames: Map<String, String> = emptyMap()
) {
    init {
        require(species.isNotEmpty() && species.withIndex().all { (index, item) ->
            item.index == index && item.modelName.isNotBlank() && item.canonicalName.isNotBlank() && item.group.isNotBlank()
        }) { "Catálogo incompatible" }
    }

    fun selectAll(scores: FloatArray, minimumScore: Float = 0.35f): List<BirdDetection> {
        require(scores.size == species.size && scores.all { it.isFinite() && it in 0f..1f }) { "Puntuaciones incompatibles" }
        require(minimumScore.isFinite() && minimumScore in 0f..1f)
        return species.asSequence().filter { it.group == "Aves" }
            .groupBy { key(it.canonicalName) }.values.map { group ->
                val winner = group.minWith(compareByDescending<SoundSpecies> { scores[it.index] }.thenBy { it.index })
                val local = key(winner.canonicalName)
                val commonName = soundNames[local]?.takeIf { it.isNotBlank() }
                    ?: regionalNames[local]?.takeIf { it.isNotBlank() }
                    ?: winner.spanishName?.takeIf { it.isNotBlank() }
                BirdDetection(
                    winner.index,
                    winner.modelName,
                    winner.canonicalName,
                    commonName,
                    scores[winner.index],
                    local.takeIf { it in regionalNames && it in maps },
                    maps[local].takeIf { local in regionalNames }
                )
            }.filter { it.score >= minimumScore }
            .sortedWith(compareByDescending<BirdDetection> { it.score }.thenBy { it.modelIndex })
    }

    fun select(scores: FloatArray, minimumScore: Float = 0.35f, limit: Int = 5): List<BirdDetection> {
        require(limit in 1..5)
        return selectAll(scores, minimumScore).take(limit)
    }

    companion object {
        private const val TAG = "BirdSoundCatalog"
        internal const val SOUND_NAMES_PATH = "birdnet/sound_names_ar.json"

        @VisibleForTesting
        internal var lastLoadError: Throwable? = null

        @VisibleForTesting
        internal var logger: (String, Throwable?) -> Unit = { msg, tr ->
            try {
                android.util.Log.w(TAG, msg, tr)
            } catch (_: RuntimeException) {
                System.err.println("$TAG: $msg: ${tr?.message}")
            }
        }

        internal fun key(name: String) = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), "_")

        internal fun parseSoundNames(jsonText: String): Map<String, String> {
            val trimmed = jsonText.trim()
            require(trimmed.startsWith("{") && trimmed.endsWith("}")) {
                "El catálogo de nombres debe ser un objeto JSON"
            }
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return emptyMap()

            val map = LinkedHashMap<String, String>()
            val entryRegex = Regex("\"([^\"]*)\"\\s*:\\s*\"([^\"]*)\"")
            var lastIndex = 0
            for (match in entryRegex.findAll(inner)) {
                val gap = inner.substring(lastIndex, match.range.first).trim()
                if (lastIndex == 0) {
                    require(gap.isEmpty()) { "Sintaxis JSON inválida antes de primera entrada: '$gap'" }
                } else {
                    require(gap == ",") { "Sintaxis JSON inválida entre entradas: '$gap'" }
                }
                val rawKey = match.groupValues[1]
                val k = key(rawKey)
                require(k.isNotBlank()) { "Clave vacía en catálogo de sonido" }
                val rawVal = match.groupValues[2].trim()
                require(rawVal.isNotBlank()) { "Nombre común vacío para especie: $rawKey" }
                require(map.put(k, rawVal) == null) { "Clave duplicada en catálogo de sonido: $rawKey" }
                lastIndex = match.range.last + 1
            }
            require(lastIndex > 0 && inner.substring(lastIndex).trim().isEmpty()) {
                "Sintaxis JSON inválida o contenido residual"
            }
            return map
        }

        internal fun loadSoundNames(
            openStream: (String) -> InputStream,
            path: String = SOUND_NAMES_PATH
        ): Map<String, String> {
            return try {
                val text = openStream(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
                parseSoundNames(text).also {
                    lastLoadError = null
                }
            } catch (error: Exception) {
                lastLoadError = error
                logger("No se pudo cargar el catálogo de nombres de sonido '$path'", error)
                emptyMap()
            }
        }

        internal fun loadSoundNames(context: Context, path: String = SOUND_NAMES_PATH): Map<String, String> =
            loadSoundNames({ context.assets.open(it) }, path)

        fun load(context: Context): BirdSoundCatalog {
            val app = context.applicationContext
            val bytes = app.assets.open("birdnet/species.json").use { it.readBytes() }
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            require(hash == "5e3598bdbc08a0684ba4edc47e7760c44515ce6febb6e5cfd0b0668f778244b7") { "Catálogo incompatible" }
            val json = JSONArray(bytes.toString(Charsets.UTF_8))
            require(json.length() == 9789) { "Catálogo incompatible" }
            val entries = (0 until json.length()).map { index ->
                val item = json.getJSONObject(index)
                SoundSpecies(item.getInt("idx"), item.getString("scientific_name"), item.getString("canonical_scientific_name"),
                    item.getString("class"), if (item.isNull("common_name_es")) null else item.getString("common_name_es"))
            }
            val regional = BirdLabelsManager.loadLabels(app)
            val availableMaps = regional.keys.mapNotNull { local ->
                val id = app.resources.getIdentifier("bird_${local}_dist", "drawable", app.packageName)
                    .takeIf { it != 0 } ?: app.resources.getIdentifier("${local}_dist", "drawable", app.packageName)
                val uri = if (id != 0) "android.resource://${app.packageName}/$id" else {
                    val path = "birds/${local}_dist.webp"
                    try { app.assets.open(path).use { }; "file:///android_asset/$path" } catch (_: java.io.IOException) { null }
                }
                uri?.let { local to it }
            }.toMap()
            val soundNames = loadSoundNames(app)
            return BirdSoundCatalog(entries, regional, availableMaps, soundNames)
        }
    }
}
