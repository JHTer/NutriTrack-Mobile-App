package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import java.util.Locale

/**
 * Service class responsible for translating text between different languages.
 *
 * This service utilizes a [GenerativeModel] (presumably from the Gemini AI SDK) to perform translations.
 * It provides methods for generic text translation and specialized translation for fruit names,
 * handling potential network errors and API issues by returning a [Result] type.
 */
class TranslationService {
    /**
     * Companion object for [TranslationService].
     * Contains constants for logging, API key, supported languages, and error messages.
     */
    companion object {
        private const val TAG = "TranslationService"
        private const val API_KEY = "YOUR-API-KEY" // TODO: Secure API Key
        
        /** A list of language codes supported by the NutriTrack application. */
        val SUPPORTED_LANGUAGES = listOf("en", "ms", "zh", "ja", "fr")
        
        // Error messages
        /** Error message displayed for network connection problems during translation. */
        const val ERROR_NETWORK_CONNECTION = "Please check your internet connection and try again."
        /** General error message for when a translation attempt fails for an unknown reason. */
        const val ERROR_TRANSLATION_FAILED = "Translation failed. Please try again later."
    }
    
    /** The [GenerativeModel] instance used for making API calls to the translation service. */
    // Create the GenerativeModel with minimal configuration
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )
    
    /**
     * Translates a given text string from a source language to a target language using the Gemini AI model.
     *
     * This function constructs a prompt for the AI, specifying the source and target languages (using their
     * display names for clarity in the prompt) and the text to be translated. It expects the AI to return
     * only the translated text.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param text The text string to be translated. If blank, the function will fail.
     * @param sourceLanguage The BCP language code of the source text (e.g., "en" for English).
     * @param targetLanguage The BCP language code for the desired output language (e.g., "fr" for French).
     * @return A [Result<String>] which is [Result.Success] containing the translated text if successful,
     *         or [Result.Failure] containing an exception with an error message if the translation fails
     *         (e.g., empty input, network error, API error, blank translation response).
     *         If `sourceLanguage` and `targetLanguage` are the same, it returns the original text successfully.
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(Exception("Empty text provided for translation"))
        }
        
        if (sourceLanguage == targetLanguage) {
            return@withContext Result.success(text) // No translation needed
        }
        
        try {
            val sourceLangName = Locale(sourceLanguage).displayLanguage
            val targetLangName = Locale(targetLanguage).displayLanguage
            
            val prompt = """
                Translate the following text from $sourceLangName to $targetLangName:
                "$text"
                
                Please return ONLY the translated text, nothing else - no explanations, no headers, no quotes.
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val translatedText = response.text?.trim() ?: ""
            
            if (translatedText.isBlank()) {
                return@withContext Result.failure(Exception(ERROR_TRANSLATION_FAILED))
            }
            
            return@withContext Result.success(translatedText)
        } catch (e: Exception) {
            Log.e(TAG, "Translation error: ${e.message}", e)
            val errorMessage = when (e) {
                is UnknownHostException -> ERROR_NETWORK_CONNECTION
                is IOException -> {
                    if (e.message?.contains("Unable to resolve host") == true || 
                        e.message?.contains("Failed to connect") == true ||
                        e.message?.contains("No address associated") == true) {
                        ERROR_NETWORK_CONNECTION
                    } else {
                        "Error: ${e.message}"
                    }
                }
                else -> "Error: ${e.message}"
            }
            return@withContext Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Translates a given fruit name from a specified source language to English.
     *
     * This method is specialized for translating fruit names, likely for use with an API that expects
     * English fruit names. It constructs a prompt asking the AI to return only the most common English
     * name for the fruit.
     * If the `sourceLanguage` is already English or the `fruitName` is blank, it returns the original name.
     *
     * @param fruitName The name of the fruit to be translated.
     * @param sourceLanguage The BCP language code of the `fruitName` (e.g., "ja" for Japanese).
     * @return A [Result<String>] which is [Result.Success] containing the English translation of the fruit name,
     *         or [Result.Failure] with an exception if translation fails or the result is blank.
     */
    suspend fun translateFruitNameToEnglish(
        fruitName: String,
        sourceLanguage: String
    ): Result<String> {
        if (sourceLanguage == "en" || fruitName.isBlank()) {
            return Result.success(fruitName) // No translation needed
        }
        
        // Create a specialized prompt for fruit translation
        try {
            val sourceLangName = Locale(sourceLanguage).displayLanguage
            
            val prompt = """
                Translate only the following fruit name from $sourceLangName to English:
                "$fruitName"
                
                This will be used for a fruit API lookup, so please return ONLY the most common English name 
                for this fruit, with no additional text. For example, if the input is "pomme" in French, 
                just return "apple".
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val translatedFruit = response.text?.trim() ?: ""
            
            if (translatedFruit.isBlank()) {
                return Result.failure(Exception(ERROR_TRANSLATION_FAILED))
            }
            
            return Result.success(translatedFruit)
        } catch (e: Exception) {
            Log.e(TAG, "Fruit translation error: ${e.message}", e)
            return Result.failure(Exception(e.message ?: ERROR_TRANSLATION_FAILED))
        }
    }
    
    /**
     * Translates a given fruit name from English to a specified target language.
     *
     * This method is specialized for translating English fruit names to another language.
     * It constructs a prompt asking the AI to return only the translated fruit name in the target language.
     * If the `targetLanguage` is already English or the `fruitName` is blank, it returns the original name.
     *
     * @param fruitName The English name of the fruit to be translated.
     * @param targetLanguage The BCP language code for the desired output language (e.g., "ms" for Malay).
     * @return A [Result<String>] which is [Result.Success] containing the translated fruit name,
     *         or [Result.Failure] with an exception if translation fails or the result is blank.
     */
    suspend fun translateFruitNameFromEnglish(
        fruitName: String,
        targetLanguage: String
    ): Result<String> {
        if (targetLanguage == "en" || fruitName.isBlank()) {
            return Result.success(fruitName) // No translation needed
        }
        
        // Create a specialized prompt for fruit translation
        try {
            val targetLangName = Locale(targetLanguage).displayLanguage
            
            val prompt = """
                Translate only the following fruit name from English to $targetLangName:
                "$fruitName"
                
                Please return ONLY the translated fruit name, with no additional text.
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val translatedFruit = response.text?.trim() ?: ""
            
            if (translatedFruit.isBlank()) {
                return Result.failure(Exception(ERROR_TRANSLATION_FAILED))
            }
            
            return Result.success(translatedFruit)
        } catch (e: Exception) {
            Log.e(TAG, "Fruit translation error: ${e.message}", e)
            return Result.failure(Exception(e.message ?: ERROR_TRANSLATION_FAILED))
        }
    }
} 