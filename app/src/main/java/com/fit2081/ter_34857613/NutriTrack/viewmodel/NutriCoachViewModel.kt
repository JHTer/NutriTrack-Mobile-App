package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.data.Fruit
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriCoachTip
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.repository.FoodIntakeRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.FruitRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.GeminiRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.NutriCoachTipRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.PatientRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.TranslationService
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.io.IOException
import java.util.Locale

/**
 * ViewModel for the NutriCoach screen, responsible for managing fruit searches, AI-generated nutrition tips,
 * and tip history.
 *
 * This ViewModel interacts with several repositories:
 * - [NutriCoachTipRepository]: For saving and retrieving AI-generated nutrition tips.
 * - [PatientRepository]: For fetching patient data to personalize tips (though direct use is limited here, more for context).
 * - [FruitRepository]: For searching fruit information via the FruityVice API.
 * - [FoodIntakeRepository]: For accessing food intake data, which could be used for tip generation context.
 * - [GeminiRepository]: For generating AI nutrition tips.
 * - [TranslationService]: For translating fruit names and AI tips to/from different languages.
 *
 * Key functionalities include:
 * - Searching for fruits by name, with support for translating the search term and results.
 * - Generating personalized AI nutrition tips based on user data (e.g., HEIFA score, food intake averages).
 * - Saving and retrieving these tips for a specific user.
 * - Handling language changes to translate existing tips or search results.
 * - Managing UI state for loading, errors, search results, and current tips through [LiveData] and Compose `State`.
 *
 * @param application The application context, used for database initialization and accessing resources.
 */
class NutriCoachViewModel(application: Application) : AndroidViewModel(application) {
    private val tipRepository: NutriCoachTipRepository
    private val patientRepository: PatientRepository
    private val fruitRepository: FruitRepository
    private val foodIntakeRepository: FoodIntakeRepository
    private val geminiRepository: GeminiRepository
    private val translationService: TranslationService
    
    // Store application context for localization purposes
    /** Application context, updated on language change to reflect the current locale. */
    private var appContext: Context = application.applicationContext
    
    // State to track language changes and force UI updates
    /** A [mutableStateOf] boolean that toggles to signal a language change, primarily used to trigger recomposition in the UI. */
    val languageChanged = mutableStateOf(false)
    
    // LiveData for fruit search results
    /** [LiveData] holding the [Fruit] object from a search result, or `null` if no search or error. */
    private val _searchResultFruit = MutableLiveData<Fruit?>()
    /** Publicly exposed [LiveData] for the fruit search result. */
    val searchResultFruit: LiveData<Fruit?> = _searchResultFruit
    
    // LiveData for translated fruit names
    /** [LiveData] holding the original (potentially non-English) fruit name that was translated for an API search. */
    private val _translatedFruitName = MutableLiveData<String>()
    /** Publicly exposed [LiveData] for the original fruit name that was part of a translated search. */
    val translatedFruitName: LiveData<String> = _translatedFruitName
    
    // LiveData for error messages
    /** [LiveData] holding an error message string if an operation fails (e.g., API call, translation), or `null` otherwise. */
    private val _errorMessage = MutableLiveData<String?>()
    /** Publicly exposed [LiveData] for error messages. */
    val errorMessage: LiveData<String?> = _errorMessage
    
    // LiveData for loading state
    /** [LiveData] indicating whether a data loading operation (e.g., fruit search) is in progress. */
    private val _isLoading = MutableLiveData<Boolean>()
    /** Publicly exposed [LiveData] for the loading state. */
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for AI generated tips
    /** [LiveData] holding the current AI-generated nutrition tip string. */
    private val _currentTip = MutableLiveData<String>()
    /** Publicly exposed [LiveData] for the current AI tip. */
    val currentTip: LiveData<String> = _currentTip
    
    // LiveData for tip generation loading state
    /** [LiveData] indicating whether an AI tip generation operation is in progress. */
    private val _isTipGenerating = MutableLiveData<Boolean>(false)
    /** Publicly exposed [LiveData] for the tip generation loading state. */
    val isTipGenerating: LiveData<Boolean> = _isTipGenerating
    
    init {
        val database = NutriTrackDatabase.getDatabase(application)
        tipRepository = NutriCoachTipRepository(database.nutriCoachTipDao())
        patientRepository = PatientRepository(database.patientDao())
        fruitRepository = FruitRepository()
        foodIntakeRepository = FoodIntakeRepository(database.foodIntakeDao())
        geminiRepository = GeminiRepository()
        translationService = TranslationService()
    }
    
    /**
     * Handles language changes detected in the UI.
     *
     * Updates the internal [appContext] with the new context (which should reflect the new locale).
     * If there's a current AI tip and the new language is not English, it attempts to translate the tip
     * to the new language using [TranslationService].
     * Toggles [languageChanged] to trigger UI recomposition.
     *
     * @param context The new application context reflecting the updated locale.
     */
    fun onLanguageChanged(context: Context) {
        // Store the new context with updated locale
        this.appContext = context
        
        // Get current locale language code
        val currentLanguage = Locale.getDefault().language
        
        // Only attempt translation if not English and we have a tip
        val currentTipValue = _currentTip.value
        if (currentLanguage != "en" && !currentTipValue.isNullOrEmpty() && 
            !currentTipValue.startsWith("ERROR")) {
            viewModelScope.launch {
                translationService.translateText(currentTipValue, "en", currentLanguage).fold(
                    onSuccess = { translatedTip ->
                        _currentTip.value = translatedTip
                    },
                    onFailure = { /* Keep original tip if translation fails */ }
                )
            }
        }
        
        // Toggle state value to force recomposition of observing composables
        languageChanged.value = !languageChanged.value
    }
    
    /**
     * Searches for a fruit by its name using the FruityVice API via [FruitRepository].
     *
     * If the current device language is not English, it first attempts to translate the `name` to English
     * using [TranslationService] before making the API call. The original non-English name is stored in
     * [_translatedFruitName] for potential UI display.
     * After fetching the fruit data (which is expected to be in English from the API), if the device language
     * is not English, it attempts to translate the fruit's name from English to the device language for display.
     * The result (possibly with a translated name) is posted to [_searchResultFruit].
     * Handles loading states via [_isLoading] and error messages (including network errors and translation failures)
     * via [_errorMessage].
     *
     * @param name The name of the fruit to search for. This can be in the user's current device language.
     */
    fun searchFruit(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _translatedFruitName.value = ""
            
            try {
                // Get the current device language
                val currentLanguage = Locale.getDefault().language
                
                // If not English, translate the fruit name to English for the API call
                val searchTerm = if (currentLanguage != "en") {
                    translationService.translateFruitNameToEnglish(name, currentLanguage).fold(
                        onSuccess = { translatedName ->
                            // Store the original name for UI display if needed
                            _translatedFruitName.value = name
                            translatedName
                        },
                        onFailure = { error ->
                            _errorMessage.value = "Translation error: ${error.message}"
                            _isLoading.value = false
                            return@launch
                        }
                    )
                } else {
                    name
                }
                
                // Use the English name (or original if already English) to search
                val result = fruitRepository.searchFruit(searchTerm)
                result.fold(
                    onSuccess = { fruit ->
                        // If the current language is not English, try to translate the fruit name
                        // for display purposes
                        if (currentLanguage != "en") {
                            try {
                                translationService.translateFruitNameFromEnglish(fruit.name, currentLanguage).fold(
                                    onSuccess = { translatedName ->
                                        val translatedFruit = fruit.copy(
                                            name = "$translatedName (${fruit.name})"
                                        )
                                        _searchResultFruit.value = translatedFruit
                                    },
                                    onFailure = {
                                        // If translation fails, just use the original fruit
                                        _searchResultFruit.value = fruit
                                    }
                                )
                            } catch (e: Exception) {
                                // If any translation error occurs, just use the original fruit
                                _searchResultFruit.value = fruit
                            }
                        } else {
                            // No translation needed for English locale
                        _searchResultFruit.value = fruit
                        }
                    },
                    onFailure = { error ->
                        // Check for network connectivity errors
                        _errorMessage.value = when (error) {
                            is UnknownHostException -> GeminiRepository.ERROR_NETWORK_CONNECTION
                            is IOException -> {
                                if (error.message?.contains("Unable to resolve host") == true || 
                                    error.message?.contains("Failed to connect") == true ||
                                    error.message?.contains("No address associated") == true) {
                                    GeminiRepository.ERROR_NETWORK_CONNECTION
                                } else {
                                    error.message ?: "Unknown error occurred"
                                }
                            }
                            else -> error.message ?: "Unknown error occurred"
                        }
                        _searchResultFruit.value = null
                    }
                )
            } catch (e: Exception) {
                // Check for network connectivity errors
                _errorMessage.value = when (e) {
                    is UnknownHostException -> GeminiRepository.ERROR_NETWORK_CONNECTION
                    is IOException -> {
                        if (e.message?.contains("Unable to resolve host") == true || 
                            e.message?.contains("Failed to connect") == true ||
                            e.message?.contains("No address associated") == true) {
                            GeminiRepository.ERROR_NETWORK_CONNECTION
                        } else {
                            "Error: ${e.message}"
                        }
                    }
                    else -> "Error: ${e.message}"
                }
                _searchResultFruit.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clears the current fruit search results and any associated error messages or translated names.
     *
     * Resets [_searchResultFruit], [_errorMessage], and [_translatedFruitName] to their default/empty states.
     */
    fun clearSearch() {
        _searchResultFruit.value = null
        _errorMessage.value = null
        _translatedFruitName.value = ""
    }
    
    /**
     * Retrieves all saved [NutriCoachTip]s for a specific user as [LiveData].
     *
     * @param userId The unique identifier of the user whose tips are to be fetched.
     * @return A [LiveData] list of [NutriCoachTip] objects for the specified user.
     */
    fun getTipsByUserId(userId: String): LiveData<List<NutriCoachTip>> {
        return tipRepository.getTipsByUserId(userId)
    }
    
    /**
     * Retrieves all saved [NutriCoachTip]s for a specific user, filtered by a given category, as [LiveData].
     *
     * @param userId The unique identifier of the user whose tips are to be fetched.
     * @param category The category to filter the tips by (e.g., "General", "Vegetables").
     * @return A [LiveData] list of [NutriCoachTip] objects for the specified user and category.
     */
    fun getTipsByUserIdAndCategory(userId: String, category: String): LiveData<List<NutriCoachTip>> {
        return tipRepository.getTipsByUserIdAndCategory(userId, category)
    }
    
    /**
     * Saves a new nutrition tip to the database for a specific user.
     *
     * This operation is performed asynchronously within the `viewModelScope`.
     *
     * @param userId The unique identifier of the user for whom the tip is being saved.
     * @param content The textual content of the nutrition tip.
     * @param category The category of the tip (e.g., "General", "Protein Intake").
     */
    fun saveNewTip(userId: String, content: String, category: String) = viewModelScope.launch {
        tipRepository.saveNewTip(userId, content, category)
    }
    
    /**
     * Deletes a given [NutriCoachTip] from the database.
     *
     * This operation is performed asynchronously within the `viewModelScope`.
     *
     * @param tip The [NutriCoachTip] object to be deleted.
     */
    fun deleteTip(tip: NutriCoachTip) = viewModelScope.launch {
        tipRepository.delete(tip)
    }
    
    /**
     * Retrieves a [Patient] object by their unique user ID.
     *
     * This suspending function is primarily used to fetch patient data (like HEIFA scores) that can serve
     * as context for generating personalized AI nutrition tips.
     *
     * @param userId The unique identifier of the patient to retrieve.
     * @return The [Patient] object if found, or `null` if no patient exists with that ID.
     */
    suspend fun getPatientById(userId: String): Patient? {
        return patientRepository.getPatientById(userId)
    }
    
    /**
     * Checks if a user's fruit intake score is considered optimal.
     *
     * An optimal fruit score is determined by checking two aspects of the patient's HEIFA data:
     * - `fruitVariationsScore`: Should be greater than 2.0.
     * - `fruitServeSize`: Should be greater than 2.0.
     * Both conditions must be met for the score to be considered optimal.
     *
     * This information can be used by the UI to decide whether to display general fruit facts
     * (if optimal) or to prompt for generating more specific AI-driven tips (if not optimal).
     *
     * @param userId The unique identifier of the user (patient) whose fruit score is to be checked.
     * @return `true` if the patient is found and both fruit variation score and serve size are greater than 2.0,
     *         `false` otherwise (including if the patient is not found or scores are missing/null).
     */
    suspend fun isFruitScoreOptimal(userId: String): Boolean {
        val patient = patientRepository.getPatientById(userId)
        
        // Check if the patient exists and has fruit scores
        if (patient != null) {
            // Consider score optimal if variations score > 2 AND serve size > 2
            val hasOptimalVariations = patient.fruitVariationsScore != null && patient.fruitVariationsScore!! > 2.0
            val hasOptimalServeSize = patient.fruitServeSize != null && patient.fruitServeSize!! > 2.0
            
            return hasOptimalVariations && hasOptimalServeSize
        }
        
        // Default to non-optimal if patient not found or no score
        return false
    }
    
    /**
     * Generates a personalized nutrition tip for a given user using the Gemini AI model.
     *
     * This function performs the following steps:
     * 1. Fetches the [Patient] data using [patientRepository].
     * 2. Fetches the latest [FoodIntake] record for the user via [foodIntakeRepository] (if available).
     * 3. Calls [GeminiRepository.generateNutritionTip] with the patient and food intake data to get an AI-generated tip.
     * 4. Validates the AI tip: If it appears to be an error message (e.g., network error, "Unable to generate"),
     *    it updates [_currentTip] with an appropriate error message and does not save the tip.
     * 5. If the tip is valid, it saves the tip to the local database using [tipRepository] under the "nutrition" category.
     * 6. If the current device language is not English, it attempts to translate the (English) tip to the device language
     *    using [TranslationService].
     * 7. Updates [_currentTip] with the (potentially translated) valid tip or an error message.
     *
     * Manages loading state via [_isTipGenerating] and error/tip display via [_currentTip].
     *
     * @param userId The unique identifier of the user for whom the tip is to be generated.
     */
    fun generateNutritionTip(userId: String) = viewModelScope.launch {
        try {
            _isTipGenerating.value = true
            
            // 1. Get patient data
            val patient = patientRepository.getPatientById(userId)
            if (patient == null) {
                _currentTip.value = "Unable to find user data"
                _isTipGenerating.value = false
                return@launch
            }
            
            // 2. Get the latest food intake data if available
            val latestFoodIntake = foodIntakeRepository.getLatestFoodIntake(userId)
            
            // 3. Generate the tip using Gemini API
            val tip = geminiRepository.generateNutritionTip(patient, latestFoodIntake)
            
            // Check if the tip contains any error messages and don't save to database
            if (tip == GeminiRepository.ERROR_NETWORK_CONNECTION || 
                tip.startsWith("Error:") ||
                tip.contains("Unable to") ||
                tip.contains("No internet") ||
                tip.contains("check your internet") ||
                tip.contains("ERROR")) {
                
                // Display appropriate error message to user
                if (tip.contains("internet") || tip.contains("Unable to resolve host") || 
                    tip == GeminiRepository.ERROR_NETWORK_CONNECTION) {
                    _currentTip.value = "ERROR No internet connection. Please connect to Wi-Fi or mobile data and try again."
                } else {
                    _currentTip.value = tip
                }
                return@launch
            }
            
            // 4. Save the tip to the database only if it's valid content
            tipRepository.saveNewTip(userId, tip, "nutrition")
            
            // 5. Get the current language and translate if not English
            val currentLanguage = Locale.getDefault().language
            if (currentLanguage != "en") {
                translationService.translateText(tip, "en", currentLanguage).fold(
                    onSuccess = { translatedTip ->
                        // Update the LiveData with the translated tip
                        _currentTip.value = translatedTip
                    },
                    onFailure = {
                        // If translation fails, use the original tip
                        _currentTip.value = tip
                    }
                )
            } else {
                // Use original English tip
                _currentTip.value = tip
            }
        } catch (e: Exception) {
            // Check for network connectivity errors
            _currentTip.value = when (e) {
                is UnknownHostException -> "ERROR No internet connection. Please connect to Wi-Fi or mobile data and try again."
                is IOException -> {
                    if (e.message?.contains("Unable to resolve host") == true || 
                        e.message?.contains("Failed to connect") == true ||
                        e.message?.contains("No address associated") == true) {
                        "ERROR No internet connection. Please connect to Wi-Fi or mobile data and try again."
                    } else {
                        "Unable to generate a tip at this time. Please try again later."
                    }
                }
                else -> "Unable to generate a tip at this time. Please try again later."
            }
            // Don't save error messages to database
        } finally {
            _isTipGenerating.value = false
        }
    }
    
    /**
     * Translates a given text string from English to a specified target language.
     *
     * This suspending function acts as a wrapper around the [TranslationService.translateText] method,
     * assuming the input `text` is in English ("en").
     *
     * @param text The text string to be translated (assumed to be in English).
     * @param targetLanguage The ISO language code of the language to translate the text into (e.g., "ja", "fr").
     * @return A [Result] object containing the translated string on success, or an exception on failure.
     */
    suspend fun translateText(text: String, targetLanguage: String): Result<String> {
        val sourceLanguage = "en" // Assuming the source is English
        return translationService.translateText(text, sourceLanguage, targetLanguage)
    }
} 