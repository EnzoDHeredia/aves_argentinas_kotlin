package com.example.avesargentinas

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_MODE = "theme_mode"

    fun applySavedTheme(context: Context) {
        val mode = getSavedMode(context)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun toggleTheme(context: Context): Int {
        val prefs = prefs(context)
        val current = getSavedMode(context)
        val newMode = when (current) {
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
                if (isSystemInDarkMode(context)) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            }
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        prefs.edit().putInt(KEY_MODE, newMode).apply()
        AppCompatDelegate.setDefaultNightMode(newMode)
        return newMode
    }

    fun isDarkTheme(context: Context): Boolean {
        val current = AppCompatDelegate.getDefaultNightMode()
        return when (current) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemInDarkMode(context)
        }
    }

    private fun getSavedMode(context: Context): Int {
        val stored = prefs(context).getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        return stored
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun isSystemInDarkMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
