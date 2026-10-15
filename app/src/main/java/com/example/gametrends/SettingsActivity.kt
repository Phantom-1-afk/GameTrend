package com.example.gametrends

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

class SettingsActivity : BaseActivity() {

    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var langSystem: RadioButton
    private lateinit var langRussian: RadioButton
    private lateinit var langEnglish: RadioButton

    private lateinit var colorRadioGroup: RadioGroup
    private lateinit var colorRed: RadioButton
    private lateinit var colorBlue: RadioButton
    private lateinit var colorGreen: RadioButton
    private lateinit var colorPurple: RadioButton
    private lateinit var colorOrange: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun initViews() {
        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        langSystem = findViewById(R.id.langSystem)
        langRussian = findViewById(R.id.langRussian)
        langEnglish = findViewById(R.id.langEnglish)

        colorRadioGroup = findViewById(R.id.colorRadioGroup)
        colorRed = findViewById(R.id.colorRed)
        colorBlue = findViewById(R.id.colorBlue)
        colorGreen = findViewById(R.id.colorGreen)
        colorPurple = findViewById(R.id.colorPurple)
        colorOrange = findViewById(R.id.colorOrange)
    }

    private fun loadCurrentSettings() {
        when (SettingsManager.getLanguage(this)) {
            SettingsManager.LANG_RUSSIAN -> langRussian.isChecked = true
            SettingsManager.LANG_ENGLISH -> langEnglish.isChecked = true
            else -> langSystem.isChecked = true
        }

        when (SettingsManager.getColorPalette(this)) {
            SettingsManager.COLOR_BLUE -> colorBlue.isChecked = true
            SettingsManager.COLOR_GREEN -> colorGreen.isChecked = true
            SettingsManager.COLOR_PURPLE -> colorPurple.isChecked = true
            SettingsManager.COLOR_ORANGE -> colorOrange.isChecked = true
            else -> colorRed.isChecked = true
        }
    }

    private fun setupListeners() {
        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newLang = when (checkedId) {
                R.id.langRussian -> SettingsManager.LANG_RUSSIAN
                R.id.langEnglish -> SettingsManager.LANG_ENGLISH
                else -> SettingsManager.LANG_SYSTEM
            }

            val currentLang = SettingsManager.getLanguage(this)
            if (newLang != currentLang) {
                SettingsManager.saveLanguage(this, newLang)
                Toast.makeText(this, "Язык изменён. Перезапустите приложение", Toast.LENGTH_SHORT).show()
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finishAffinity()
            }
        }

        colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newColor = when (checkedId) {
                R.id.colorBlue -> SettingsManager.COLOR_BLUE
                R.id.colorGreen -> SettingsManager.COLOR_GREEN
                R.id.colorPurple -> SettingsManager.COLOR_PURPLE
                R.id.colorOrange -> SettingsManager.COLOR_ORANGE
                else -> SettingsManager.COLOR_RED
            }

            val currentColor = SettingsManager.getColorPalette(this)
            if (newColor != currentColor) {
                SettingsManager.saveColorPalette(this, newColor)
                Toast.makeText(this, "Цветовая палитра изменена", Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(accentColor)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}