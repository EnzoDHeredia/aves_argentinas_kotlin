package com.example.avesargentinas.ui.learning

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import com.example.avesargentinas.BirdLabelsManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

class LearningContentLoader(
    private val assets: AssetManager,
    private val speciesNames: Map<String, String>
) {
    constructor(context: Context) : this(
        context.applicationContext.assets,
        BirdLabelsManager.loadLabels(context.applicationContext)
    )

    fun load(
        questionsPath: String = "learning/questions.json",
        photosPath: String = "learning/photo_cards.json"
    ): List<LearningCard> = loadFile(questionsPath, ::parseQuestions) +
        loadFile(photosPath, ::parsePhotos)

    fun parseQuestions(text: String): List<LearningCard.Question> = parseEntries(text) { entry ->
        val sources = entry.getJSONArray("sources")
        if (sources.length() == 0) throw JSONException("Fuentes vacías")
        LearningCard.Question(
            id = entry.identifier(),
            topic = entry.requiredText("topic"),
            level = entry.requiredText("level"),
            question = entry.requiredText("question"),
            answer = entry.requiredText("answer"),
            clarification = if (entry.isNull("clarification")) null else entry.requiredText("clarification"),
            sources = (0 until sources.length()).map { index ->
                sources.opt(index) as? String ?: throw JSONException("Fuente no textual")
            }.onEach { if (it.isBlank()) throw JSONException("Fuente vacía") }
        )
    }

    fun parsePhotos(text: String): List<LearningCard.Photo> = parseEntries(text) { entry ->
        val speciesKey = entry.requiredText("speciesKey")
        val commonName = speciesNames[speciesKey]?.takeIf { it.isNotBlank() }
            ?: throw JSONException("Especie desconocida o sin nombre: $speciesKey")
        val imagePath = entry.requiredText("imagePath")
        if (!imagePath.matches(Regex("learning/photos/(?:[A-Za-z0-9_-]+/)*[A-Za-z0-9_-]+\\.[A-Za-z0-9]+"))) {
            throw JSONException("Ruta fuera de las fotos de aprendizaje: $imagePath")
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(imagePath).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw JSONException("Fotografía ilegible: $imagePath")
        }
        LearningCard.Photo(
            id = entry.identifier(),
            speciesKey = speciesKey,
            commonName = commonName.trim(),
            scientificName = speciesKey.replace('_', ' ').replaceFirstChar { it.uppercaseChar() },
            imagePath = imagePath
        )
    }

    private fun <T : LearningCard> loadFile(path: String, parse: (String) -> List<T>): List<T> = try {
        assets.open(path).bufferedReader(Charsets.UTF_8).use { parse(it.readText()) }
    } catch (error: IOException) {
        Log.d(TAG, "Contenido no disponible: $path", error)
        emptyList()
    }

    private fun <T : LearningCard> parseEntries(text: String, parse: (JSONObject) -> T): List<T> {
        val entries = try {
            val tokener = JSONTokener(text)
            val array = tokener.nextValue() as? JSONArray ?: throw JSONException("Se esperaba una lista")
            if (tokener.nextClean() != '\u0000') throw JSONException("Contenido después de la lista")
            array
        } catch (error: JSONException) {
            Log.d(TAG, "Documento de aprendizaje inválido", error)
            return emptyList()
        }
        val identifiers = mutableSetOf<String>()
        return buildList {
            for (index in 0 until entries.length()) {
                try {
                    val card = parse(entries.getJSONObject(index))
                    if (identifiers.add(card.id)) add(card)
                    else Log.d(TAG, "Identificador de aprendizaje duplicado: ${card.id}")
                } catch (error: JSONException) {
                    Log.d(TAG, "Entrada de aprendizaje inválida: $index", error)
                } catch (error: IOException) {
                    Log.d(TAG, "Imagen de aprendizaje no disponible: $index", error)
                }
            }
        }
    }

    private fun JSONObject.requiredText(field: String): String =
        (opt(field) as? String)?.takeIf { it.isNotBlank() }
            ?: throw JSONException("Campo obligatorio inválido: $field")

    private fun JSONObject.identifier(): String = requiredText("id").also {
        if (!it.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]*"))) throw JSONException("Identificador inválido")
    }

    private companion object {
        const val TAG = "LearningContentLoader"
    }
}
