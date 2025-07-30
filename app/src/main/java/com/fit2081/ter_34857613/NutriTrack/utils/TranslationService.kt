package com.fit2081.ter_34857613.NutriTrack.utils

import com.fit2081.ter_34857613.NutriTrack.model.api.GeminiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Service to handle translations using the Gemini API
 */
class TranslationService private constructor(
    private val geminiHelper: GeminiHelper
) {
    // Cache to prevent repeated translations of the same text
    private val translationCache = ConcurrentHashMap<String, String>()
    
    companion object {
        @Volatile
        private var instance: TranslationService? = null
        
        fun getInstance(geminiHelper: GeminiHelper): TranslationService {
            return instance ?: synchronized(this) {
                instance ?: TranslationService(geminiHelper).also { instance = it }
            }
        }
    }
    
    /**
     * Translate a single text from English to the target language
     */
    suspend fun translate(text: String, targetLanguage: String = Locale.getDefault().language): String {
        // No need to translate if text is empty or target language is English
        if (text.isBlank() || targetLanguage == "en") {
            return text
        }
        
        // Check cache first
        val cacheKey = "$text-$targetLanguage"
        translationCache[cacheKey]?.let { return it }
        
        // Use Gemini API to translate
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Translate the following text to $targetLanguage. " +
                        "Only return the translation, no explanations: $text"
                
                val result = geminiHelper.generateContent(prompt)
                val translation = if (result.isSuccess) {
                    result.getOrDefault("").trim()
                } else {
                    text // Return original text if translation fails
                }
                
                // Cache result for future use
                if (translation.isNotEmpty()) {
                    translationCache[cacheKey] = translation
                }
                
                translation
            } catch (e: Exception) {
                // If translation fails, return original text
                text
            }
        }
    }
    
    /**
     * Translate multiple texts at once for efficiency
     */
    suspend fun batchTranslate(
        texts: List<String>,
        targetLanguage: String = Locale.getDefault().language
    ): List<String> {
        // No need to translate if texts are empty or target language is English
        if (texts.isEmpty() || targetLanguage == "en") {
            return texts
        }
        
        // Filter out texts that are already in the cache
        val cacheKeys = texts.map { "$it-$targetLanguage" }
        val cachedTexts = cacheKeys.mapIndexedNotNull { index, key ->
            translationCache[key]?.let { index to it }
        }.toMap()
        
        // If all texts are cached, return them in the correct order
        if (cachedTexts.size == texts.size) {
            return texts.indices.map { cachedTexts[it]!! }
        }
        
        // Get indices and texts that need translation
        val uncachedIndices = texts.indices.filter { it !in cachedTexts.keys }
        val uncachedTexts = uncachedIndices.map { texts[it] }
        
        // Use Gemini API to translate uncached texts
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Translate these items to $targetLanguage. Return ONLY the translations in the same order, one per line:\n" +
                        uncachedTexts.joinToString("\n")
                
                val result = geminiHelper.generateContent(prompt)
                val translationResponse = if (result.isSuccess) {
                    result.getOrDefault("")
                } else {
                    return@withContext texts // Return original texts if translation fails
                }
                
                val translations = translationResponse.split("\n")
                    .take(uncachedTexts.size)
                    .map { it.trim() }
                
                // Cache the new translations
                uncachedIndices.forEachIndexed { index, originalIndex ->
                    val originalText = texts[originalIndex]
                    val translation = translations.getOrElse(index) { originalText }
                    translationCache["$originalText-$targetLanguage"] = translation
                }
                
                // Combine cached and new translations in the original order
                texts.mapIndexed { index, originalText ->
                    cachedTexts[index] ?: translationCache["$originalText-$targetLanguage"] ?: originalText
                }
            } catch (e: Exception) {
                // If translation fails, return original texts
                texts
            }
        }
    }
    
    /**
     * Clear the translation cache
     */
    fun clearCache() {
        translationCache.clear()
    }
} 