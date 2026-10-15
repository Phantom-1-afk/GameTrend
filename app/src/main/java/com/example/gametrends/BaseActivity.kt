package com.example.gametrends

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = SettingsManager.getLanguage(newBase)
        val context = when (language) {
            SettingsManager.LANG_RUSSIAN -> setLocale(newBase, "ru")
            SettingsManager.LANG_ENGLISH -> setLocale(newBase, "en")
            else -> newBase
        }
        super.attachBaseContext(context)
    }

    private fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GameTrends_Dark)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
        setupPopupMenuStyle()
    }

    open fun applyColorsToElements() {
    }

    private fun setupPopupMenuStyle() {
        try {
        } catch (e: Exception) {
        }
    }
}