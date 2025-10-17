package com.example.avesargentinas

import android.graphics.Bitmap
import com.example.avesargentinas.LocationProvider.Coordinates
import com.example.avesargentinas.data.Observation
import com.example.avesargentinas.data.ObservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationLogger(
    private val repository: ObservationRepository,
    private val imageSaver: ImageSaver
) {

    sealed class Result {
        data class Success(val observationId: Long) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun saveObservation(
        bitmap: Bitmap,
        prediction: Prediction,
        coordinates: Coordinates?,
        individualCount: Int,
        notes: String? = null
    ): Result = withContext(Dispatchers.Default) {
        val savedUri = imageSaver.saveBitmap(bitmap, APP_FOLDER_NAME)
            ?: return@withContext Result.Error("No se pudo guardar la imagen en la galería")

        val timestamp = System.currentTimeMillis()
        val cleanNotes = notes?.takeIf { it.isNotBlank() }

        val observation = Observation(
            imageUri = savedUri.toString(),
            displayName = prediction.displayName,
            scientificName = prediction.scientificName,
            confidence = prediction.confidencePercentage,
            regionalName = prediction.regionalName,
            individualCount = individualCount,
            latitude = coordinates?.latitude,
            longitude = coordinates?.longitude,
            capturedAt = timestamp,
            notes = cleanNotes
        )

        val id = repository.insert(observation)
        Result.Success(id)
    }

    companion object {
        private const val APP_FOLDER_NAME = "AvesArgentinas"

        private fun formatTimestamp(timestamp: Long): String {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return format.format(Date(timestamp))
        }

        fun buildShareText(observation: Observation): String {
            val builder = StringBuilder()
            builder.appendLine("Observación de ave")
            builder.appendLine("Nombre común: ${observation.displayName}")
            builder.appendLine("Nombre científico: ${observation.scientificName}")
            builder.appendLine("Confianza: ${"%.1f".format(Locale.getDefault(), observation.confidence)}%")
            val countLabel = if (observation.individualCount == 1) {
                "1 individuo"
            } else {
                "${observation.individualCount} individuos"
            }
            builder.appendLine("Individuos observados: $countLabel")
            observation.regionalName?.let {
                builder.appendLine("Nombre regional: $it")
            }
            if (observation.latitude != null && observation.longitude != null) {
                builder.appendLine(
                    "Coordenadas: %.5f, %.5f".format(Locale.getDefault(), observation.latitude, observation.longitude)
                )
            }
            builder.appendLine(
                "Registrado: ${formatTimestamp(observation.capturedAt)}"
            )
            observation.notes?.let {
                builder.appendLine()
                builder.appendLine("Notas: $it")
            }
            return builder.toString()
        }
    }
}
