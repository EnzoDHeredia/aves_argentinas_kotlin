package com.example.avesargentinas

import android.content.Context

/**
 * Gestor de configuraciones de la aplicación.
 * Persiste preferencias del usuario usando SharedPreferences.
 */
object SettingsManager {
    private const val PREFS = "app_settings"
    private const val KEY_USER = "user_name"
    private const val KEY_EXPERT = "expert_mode"
    private const val KEY_OBFUSCATE = "obfuscate_location"
    private const val KEY_INSTANT = "instant_capture"

    private fun prefs(context: Context) = 
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Nombre de usuario
    fun getUserName(context: Context): String = 
        prefs(context).getString(KEY_USER, "") ?: ""
    
    fun setUserName(context: Context, name: String) = 
        prefs(context).edit().putString(KEY_USER, name).apply()

    // Modo experto (guardar sin importar confianza del modelo)
    fun isExpertMode(context: Context): Boolean = 
        prefs(context).getBoolean(KEY_EXPERT, false)
    
    fun setExpertMode(context: Context, value: Boolean) = 
        prefs(context).edit().putBoolean(KEY_EXPERT, value).apply()

    // Ofuscar ubicación exacta (privacidad)
    fun isObfuscateLocation(context: Context): Boolean = 
        prefs(context).getBoolean(KEY_OBFUSCATE, false)
    
    fun setObfuscateLocation(context: Context, value: Boolean) = 
        prefs(context).edit().putBoolean(KEY_OBFUSCATE, value).apply()

    // Captura instantánea (abrir directamente en identificación)
    fun isInstantCapture(context: Context): Boolean = 
        prefs(context).getBoolean(KEY_INSTANT, false)
    
    fun setInstantCapture(context: Context, value: Boolean) = 
        prefs(context).edit().putBoolean(KEY_INSTANT, value).apply()
}
