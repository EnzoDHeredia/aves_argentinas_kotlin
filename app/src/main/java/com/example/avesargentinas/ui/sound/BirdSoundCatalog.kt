package com.example.avesargentinas.ui.sound

import android.content.Context
import com.example.avesargentinas.BirdLabelsManager
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
    private val maps: Map<String, String>
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
                BirdDetection(winner.index, winner.modelName, winner.canonicalName,
                    regionalNames[local]?.takeIf { it.isNotBlank() } ?: winner.spanishName?.takeIf { it.isNotBlank() },
                    scores[winner.index], local.takeIf { it in regionalNames && it in maps }, maps[local].takeIf { local in regionalNames })
            }.filter { it.score >= minimumScore }
            .sortedWith(compareByDescending<BirdDetection> { it.score }.thenBy { it.modelIndex })
    }

    fun select(scores: FloatArray, minimumScore: Float = 0.35f, limit: Int = 5): List<BirdDetection> {
        require(limit in 1..5)
        return selectAll(scores, minimumScore).take(limit)
    }

    companion object {
        private fun key(name: String) = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), "_")

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
            return BirdSoundCatalog(entries, regional, availableMaps)
        }
    }
}
