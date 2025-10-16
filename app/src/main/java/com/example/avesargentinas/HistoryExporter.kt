package com.example.avesargentinas

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.avesargentinas.data.Observation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Clase responsable de exportar el historial completo de observaciones
 * en formato ZIP conteniendo:
 * - observations.csv con todos los datos
 * - carpeta images/ con todas las fotos
 */
class HistoryExporter(private val context: Context) {

    companion object {
        private const val TAG = "HistoryExporter"
        private const val CSV_FILENAME = "observations.csv"
        private const val IMAGES_FOLDER = "images/"
    }

    /**
     * Exporta todas las observaciones a un archivo ZIP.
     * @param observations Lista de observaciones a exportar
     * @return Uri del archivo ZIP creado o null si falla
     */
    suspend fun exportToZip(observations: List<Observation>): Uri? = withContext(Dispatchers.IO) {
        try {
            if (observations.isEmpty()) {
                Log.w(TAG, "No hay observaciones para exportar")
                return@withContext null
            }

            // Crear nombre del archivo ZIP
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFileName = "AvesArgentinas_Export_$timestamp.zip"

            // Crear URI para el archivo ZIP
            val zipUri = createZipFile(zipFileName) ?: return@withContext null

            // Escribir el ZIP
            context.contentResolver.openOutputStream(zipUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // 1. Añadir CSV con los datos
                    addCsvToZip(zipOut, observations)

                    // 2. Añadir imágenes
                    addImagesToZip(zipOut, observations)
                }
            }

            Log.i(TAG, "Exportación completada: $zipFileName")
            zipUri

        } catch (e: Exception) {
            Log.e(TAG, "Error al exportar historial", e)
            null
        }
    }

    /**
     * Crea el archivo ZIP en la carpeta de Descargas
     */
    private fun createZipFile(fileName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - usar MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // Android 9 y anteriores - usar File
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            Uri.fromFile(file)
        }
    }

    /**
     * Añade el CSV con todos los datos de observaciones al ZIP
     */
    private fun addCsvToZip(zipOut: ZipOutputStream, observations: List<Observation>) {
        val csvData = generateCsv(observations)
        
        zipOut.putNextEntry(ZipEntry(CSV_FILENAME))
        zipOut.write(csvData.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
        
        Log.d(TAG, "CSV añadido al ZIP: ${observations.size} observaciones")
    }

    /**
     * Genera el contenido CSV con todas las observaciones
     */
    private fun generateCsv(observations: List<Observation>): String {
        val csv = StringBuilder()
        
        // Obtener nombre de usuario desde configuración
        val userName = SettingsManager.getUserName(context)
        
        // Encabezados
        csv.append("ID,Especie,Nombre Común,Nombre Científico,Confianza (%),Conteo,Fecha,Hora,Latitud,Longitud,Usuario,Notas,Nombre Imagen\n")
        
        // Datos
        observations.forEach { obs ->
            val imageName = obs.imageUri?.let { extractImageName(it) } ?: "sin_imagen"
            
            // Convertir timestamp a fecha y hora
            val dateTime = Date(obs.capturedAt)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = dateFormat.format(dateTime)
            val time = timeFormat.format(dateTime)
            
            csv.append("${obs.id},")
            csv.append("\"${escapeCsv(obs.displayName)}\",")
            csv.append("\"${escapeCsv(obs.regionalName ?: "")}\",")
            csv.append("\"${escapeCsv(obs.scientificName)}\",")
            csv.append("${String.format(Locale.US, "%.2f", obs.confidence * 100)},")
            csv.append("${obs.individualCount},")
            csv.append("\"$date\",")
            csv.append("\"$time\",")
            csv.append("${obs.latitude ?: ""},")
            csv.append("${obs.longitude ?: ""},")
            csv.append("\"${escapeCsv(userName)}\",")
            csv.append("\"${escapeCsv(obs.notes ?: "")}\",")
            csv.append("\"$imageName\"\n")
        }
        
        return csv.toString()
    }

    /**
     * Escapa caracteres especiales en CSV
     */
    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    /**
     * Añade todas las imágenes al ZIP
     */
    private fun addImagesToZip(zipOut: ZipOutputStream, observations: List<Observation>) {
        var successCount = 0
        var errorCount = 0

        observations.forEach { obs ->
            obs.imageUri?.let { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val imageName = extractImageName(uriString)
                    
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        zipOut.putNextEntry(ZipEntry("$IMAGES_FOLDER$imageName"))
                        inputStream.copyTo(zipOut)
                        zipOut.closeEntry()
                        successCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al copiar imagen: $uriString", e)
                    errorCount++
                }
            }
        }

        Log.d(TAG, "Imágenes procesadas: $successCount exitosas, $errorCount fallidas")
    }

    /**
     * Extrae el nombre del archivo de la URI y añade extensión .jpg si no tiene ninguna
     */
    private fun extractImageName(uriString: String): String {
        return try {
            val uri = Uri.parse(uriString)
            val path = uri.lastPathSegment ?: "imagen_${System.currentTimeMillis()}.jpg"
            
            // Si el path contiene '/', tomar solo el nombre del archivo
            var fileName = if (path.contains("/")) {
                path.substring(path.lastIndexOf("/") + 1)
            } else {
                path
            }
            
            // Solo añadir .jpg si no tiene ninguna extensión
            if (!fileName.contains(".")) {
                fileName = "$fileName.jpg"
            }
            
            fileName
        } catch (e: Exception) {
            "imagen_${System.currentTimeMillis()}.jpg"
        }
    }
}
