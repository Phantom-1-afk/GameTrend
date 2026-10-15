package com.example.gametrends

import android.content.Context
import androidx.core.content.ContextCompat

object ColorManager {

    fun getAccentColor(context: Context): Int {
        val colorPalette = SettingsManager.getColorPalette(context)
        val colorRes = when (colorPalette) {
            SettingsManager.COLOR_BLUE -> R.color.accent_blue
            SettingsManager.COLOR_GREEN -> R.color.accent_green
            SettingsManager.COLOR_PURPLE -> R.color.accent_purple
            SettingsManager.COLOR_ORANGE -> R.color.accent_orange
            else -> R.color.accent_red
        }
        return ContextCompat.getColor(context, colorRes)
    }
}