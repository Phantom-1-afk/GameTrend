package com.example.gametrends

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREF_NAME = "GameTrendsPrefs"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_COLOR = "color_palette"

    const val LANG_SYSTEM = 0
    const val LANG_RUSSIAN = 1
    const val LANG_ENGLISH = 2

    const val COLOR_RED = 0
    const val COLOR_BLUE = 1
    const val COLOR_GREEN = 2
    const val COLOR_PURPLE = 3
    const val COLOR_ORANGE = 4

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLanguage(context: Context, lang: Int) {
        getPrefs(context).edit().putInt(KEY_LANGUAGE, lang).apply()
    }

    fun getLanguage(context: Context): Int {
        return getPrefs(context).getInt(KEY_LANGUAGE, LANG_SYSTEM)
    }

    fun saveColorPalette(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_COLOR, color).apply()
    }

    fun getColorPalette(context: Context): Int {
        return getPrefs(context).getInt(KEY_COLOR, COLOR_RED)
    }
}