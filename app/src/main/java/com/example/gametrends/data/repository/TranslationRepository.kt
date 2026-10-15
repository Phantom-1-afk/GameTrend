package com.example.gametrends.data.repository

import android.content.Context
import com.example.gametrends.SettingsManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TranslationRepository(private val context: Context) {

    private var translator: Translator? = null

    private fun getTargetLanguage(): String {
        val language = SettingsManager.getLanguage(context)
        return when (language) {
            SettingsManager.LANG_RUSSIAN -> TranslateLanguage.RUSSIAN
            else -> TranslateLanguage.ENGLISH
        }
    }

    suspend fun translateText(text: String): String = suspendCancellableCoroutine { continuation ->
        val targetLanguage = getTargetLanguage()

        if (targetLanguage == TranslateLanguage.ENGLISH) {
            continuation.resume(text)
            return@suspendCancellableCoroutine
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = Translation.getClient(options)

        translator?.translate(text)
            ?.addOnSuccessListener { translatedText ->
                continuation.resume(translatedText)
            }
            ?.addOnFailureListener {
                continuation.resume(text)
            }
    }

    fun close() {
        translator?.close()
    }
}