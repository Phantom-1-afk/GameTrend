package com.example.gametrends

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gametrends.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        applyLanguage()

        lifecycleScope.launch {
            val repository = GameRepository()
            try {
                repository.getTopGames(5)
                repository.getMostPopularGame()
                repository.getMostWishedGames(5)
            } catch (e: Exception) {
            }

            delay(2000)

            startActivity(Intent(this@SplashActivity, MainScreenActivity::class.java))
            finish()
        }
    }

    private fun applyLanguage() {
        val language = SettingsManager.getLanguage(this)
        val locale = when (language) {
            SettingsManager.LANG_RUSSIAN -> java.util.Locale("ru")
            SettingsManager.LANG_ENGLISH -> java.util.Locale("en")
            else -> java.util.Locale.getDefault()
        }

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val loadingText = findViewById<TextView>(R.id.loadingText)
        loadingText.text = getString(R.string.loading)
    }
}