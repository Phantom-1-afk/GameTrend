package com.example.gametrends

import android.os.Bundle
import android.widget.TextView

class AboutActivity : BaseActivity() {

    private lateinit var appNameText: TextView
    private lateinit var rawgText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about)

        appNameText = findViewById(R.id.appNameText)
        rawgText = findViewById(R.id.rawgText)

        val versionText = findViewById<TextView>(R.id.versionText)
        versionText.text = "Версия 1.0"
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)

        appNameText.setTextColor(accentColor)
        rawgText.setTextColor(accentColor)

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