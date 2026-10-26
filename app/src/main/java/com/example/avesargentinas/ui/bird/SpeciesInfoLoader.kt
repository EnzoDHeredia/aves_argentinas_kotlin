package com.example.avesargentinas.ui.bird

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Locale

class SpeciesInfoLoader(private val assets: AssetManager) {
    constructor(context: Context) : this(context.applicationContext.assets)

    fun load(path: String = "species_info.json"): SpeciesInfoCatalog = try {
        assets.open(path).bufferedReader(Charsets.UTF_8).use { parse(it.readText()) }
    } catch (_: IOException) {
        Log.d(TAG, "Catálogo editorial no disponible")
        SpeciesInfoCatalog(issues = listOf("Catálogo editorial no disponible"))
    }

    fun parse(text: String): SpeciesInfoCatalog {
        val issues = mutableListOf<String>()
        fun report(location: String) {
            issues.add(location)
            Log.d(TAG, "Contenido editorial inválido: $location")
        }

        val document: JSONObject
        val sourceEntries: JSONArray
        val speciesEntries: JSONArray
        try {
            val tokener = JSONTokener(text)
            document = tokener.nextValue() as? JSONObject ?: throw JSONException("Objeto requerido")
            if (tokener.nextClean() != '\u0000') throw JSONException("Contenido adicional")
            sourceEntries = document.getJSONArray("sources")
            speciesEntries = document.getJSONArray("species")
        } catch (_: JSONException) {
            report("documento")
            return SpeciesInfoCatalog(issues = issues)
        }

        fun JSONObject.optionalText(field: String, location: String): String? {
            if (isNull(field)) return null
            return (opt(field) as? String)?.trim()?.takeIf { it.isNotEmpty() }
                .also { if (it == null) report("$location.$field") }
        }

        val sources = linkedMapOf<String, SpeciesSource>()
        for (index in 0 until sourceEntries.length()) {
            try {
                val entry = sourceEntries.getJSONObject(index)
                val id = entry.requiredText("id")
                if (!id.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]*")) || id in sources) {
                    throw JSONException("Identidad inválida o duplicada")
                }
                val url = entry.requiredText("url")
                val uri = try { URI(url) } catch (_: URISyntaxException) { null }
                if (uri?.scheme !in listOf("https", "http") || uri?.host.isNullOrBlank()) {
                    throw JSONException("URL inválida")
                }
                val date = entry.requiredText("consultedOn")
                val validDate = date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) && try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }.parse(date) != null
                } catch (_: java.text.ParseException) { false }
                if (!validDate) throw JSONException("Fecha inválida")
                sources[id] = SpeciesSource(id, entry.requiredText("title"),
                    entry.requiredText("organization"), url, date)
            } catch (_: JSONException) {
                report("sources[$index]")
            }
        }

        fun references(value: Any?, location: String): List<String> {
            if (value == null || value == JSONObject.NULL) return emptyList()
            if (value !is JSONArray) {
                report(location)
                return emptyList()
            }
            return buildList {
                for (index in 0 until value.length()) {
                    val id = value.opt(index) as? String
                    if (id != null && id in sources) {
                        if (id !in this) add(id)
                    } else report("$location[$index]")
                }
            }
        }

        fun JSONObject.conservation(field: String, key: String): SpeciesConservation? {
            if (isNull(field)) return null
            val entry = optJSONObject(field)
            val category = entry?.optionalText("category", "$key.$field")
            if (category == null) {
                report("$key.$field")
                return null
            }
            val rawYear = entry.opt("assessmentYear")
            val year = (rawYear as? Int)?.takeIf { it in 1..9999 }
            if (rawYear != null && rawYear != JSONObject.NULL && year == null) {
                report("$key.$field.assessmentYear")
            }
            return SpeciesConservation(category, year,
                references(entry.opt("sourceIds"), "$key.$field.sourceIds"))
        }

        val species = linkedMapOf<String, SpeciesInfo>()
        for (index in 0 until speciesEntries.length()) {
            try {
                val entry = speciesEntries.getJSONObject(index)
                val key = entry.requiredText("speciesKey")
                if (!key.matches(Regex("[a-z][a-z0-9]*(?:_[a-z0-9]+)+")) || key in species) {
                    throw JSONException("Identidad inválida o duplicada")
                }
                val fieldSources = linkedMapOf<String, List<String>>()
                val fields = entry.optJSONObject("fieldSources")
                if (!entry.isNull("fieldSources") && fields == null) report("$key.fieldSources")
                fields?.keys()?.forEach { field ->
                    fieldSources[field] = references(fields.opt(field), "$key.fieldSources.$field")
                }
                val photos = mutableListOf<SpeciesPhoto>()
                val photoEntries = entry.optJSONArray("photos")
                if (!entry.isNull("photos") && photoEntries == null) report("$key.photos")
                for (photoIndex in 0 until (photoEntries?.length() ?: 0)) {
                    val location = "$key.photos[$photoIndex]"
                    try {
                        val photo = photoEntries!!.getJSONObject(photoIndex)
                        val path = photo.requiredText("imagePath")
                        if (!path.matches(Regex("(?:learning|birds)/photos/(?:[A-Za-z0-9_-]+/)*[A-Za-z0-9_-]+\\.[A-Za-z0-9]+")) ||
                            photos.any { it.imagePath == path }) {
                            throw JSONException("Ruta inválida o duplicada")
                        }
                        val sex = if (photo.isNull("sex")) null else when (photo.requiredText("sex").lowercase(Locale.ROOT)) {
                            "macho" -> PhotoSex.MACHO
                            "hembra" -> PhotoSex.HEMBRA
                            else -> throw JSONException("Sexo inválido")
                        }
                        val representative = photo.opt("representative") as? Boolean
                            ?: throw JSONException("Representativa inválida")
                        photos.add(SpeciesPhoto(path, sex, representative,
                            photo.optionalText("author", location), photo.optionalText("license", location),
                            references(photo.opt("sourceIds"), "$location.sourceIds")))
                    } catch (_: JSONException) {
                        report(location)
                    }
                }
                if (photos.count { it.representative } != 1) report("$key.photos.representative")
                if (photos.filter { it.sex != null }.groupBy { it.sex }.any { it.value.size > 1 }) {
                    report("$key.photos.sex")
                }
                species[key] = SpeciesInfo(
                    speciesKey = key,
                    commonName = entry.optionalText("commonName", key),
                    scientificName = entry.optionalText("scientificName", key),
                    englishName = entry.optionalText("englishName", key),
                    family = entry.optionalText("family", key),
                    group = entry.optionalText("group", key),
                    order = entry.optionalText("order", key),
                    argentineRegions = entry.optionalText("argentineRegions", key),
                    description = entry.optionalText("description", key),
                    habitat = entry.optionalText("habitat", key),
                    diet = entry.optionalText("diet", key),
                    seasonalMovements = entry.optionalText("seasonalMovements", key),
                    conservationGlobal = entry.conservation("conservationGlobal", key),
                    conservationNational = entry.conservation("conservationNational", key),
                    fieldSources = fieldSources,
                    photos = photos
                )
            } catch (_: JSONException) {
                report("species[$index]")
            }
        }
        return SpeciesInfoCatalog(sources, species, issues)
    }

    private fun JSONObject.requiredText(field: String): String =
        (opt(field) as? String)?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw JSONException("Campo obligatorio inválido")

    private companion object {
        const val TAG = "SpeciesInfoLoader"
    }
}
