package com.example.avesargentinas

import android.content.Context
import org.json.JSONObject

/**
 * Gestiona el mapeo de nombres científicos a nombres comunes de aves
 * desde el archivo labels_map.json
 */
object BirdLabelsManager {
    
    private var labelsMap: Map<String, String>? = null
    
    /**
     * Data class para representar un ave con ambos nombres
     */
    data class BirdInfo(
        val scientificName: String,
        val commonName: String
    ) {
        override fun toString(): String = commonName
    }
    
    /**
     * Carga el mapeo desde el archivo JSON
     */
    fun loadLabels(context: Context): Map<String, String> {
        if (labelsMap == null) {
            try {
                val json = context.assets.open("labels_map.json").bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(json)
                val map = mutableMapOf<String, String>()
                
                jsonObject.keys().forEach { key ->
                    map[key] = jsonObject.getString(key)
                }
                
                labelsMap = map
            } catch (e: Exception) {
                e.printStackTrace()
                labelsMap = emptyMap()
            }
        }
        return labelsMap!!
    }
    
    /**
     * Obtiene la lista de aves ordenada por nombre común
     */
    fun getBirdsList(context: Context): List<BirdInfo> {
        val map = loadLabels(context)
        return map.map { BirdInfo(it.key, it.value) }
            .sortedBy { it.commonName.lowercase() }
    }
    
    /**
     * Obtiene los nombres comunes como array para el adapter, capitalizados
     */
    fun getCommonNamesArray(context: Context): Array<String> {
        return getBirdsList(context).map { capitalizeWords(it.commonName) }.toTypedArray()
    }
    
    /**
     * Capitaliza cada palabra de un string
     */
    private fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }
    
    /**
     * Busca el nombre científico dado el nombre común (case-insensitive)
     */
    fun getScientificName(context: Context, commonName: String): String? {
        val map = loadLabels(context)
        return map.entries.find { 
            it.value.equals(commonName.trim(), ignoreCase = true) 
        }?.key
    }
    
    /**
     * Obtiene el nombre común original (sin capitalizar) dado el nombre capitalizado
     */
    fun getOriginalCommonName(context: Context, capitalizedName: String): String? {
        val map = loadLabels(context)
        return map.values.find { 
            it.equals(capitalizedName, ignoreCase = true) 
        }
    }
    
    /**
     * Busca el nombre común dado el nombre científico
     */
    fun getCommonName(context: Context, scientificName: String): String? {
        return loadLabels(context)[scientificName]
    }
}
