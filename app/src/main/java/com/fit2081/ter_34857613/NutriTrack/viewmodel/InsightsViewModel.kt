package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.repository.InsightData
import com.fit2081.ter_34857613.NutriTrack.model.repository.InsightsRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.TranslationService
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.util.Locale

/**
 * ViewModel for insights screen that handles nutrition data and state management
 * following MVVM architecture
 */
class InsightsViewModel : ViewModel() {
    companion object {
        private const val TAG = "InsightsViewModel"
    }
    
    // Repository to handle data operations
    private val repository = InsightsRepository()
    private val translationService = TranslationService()
    
    // State for insight data
    var insightData by mutableStateOf<InsightData?>(null)
        private set
    
    // State for UI-related values
    var totalScoreInt by mutableStateOf(0)
        private set
    
    var qualityRating by mutableStateOf("")
        private set
        
    var qualityColor by mutableStateOf(Color.Gray)
        private set
        
    var qualityDescription by mutableStateOf("")
        private set
        
    // State for loading and error handling
    var isLoading by mutableStateOf(false)
        private set
        
    var errorMessage by mutableStateOf<String?>(null)
        private set
        
    // This MutableState will force recomposition when language changes
    private var _languageChanged = mutableStateOf(false)
    val languageChanged = _languageChanged
    
    // Application context for localization
    private var appContext: Context? = null
    
    /**
     * Force refresh all data when language changes
     */
    fun onLanguageChanged(context: Context) {
        appContext = context.applicationContext
        _languageChanged.value = !_languageChanged.value // Toggle to force recomposition
        
        // Refresh quality description if data is already loaded
        insightData?.let {
            // Get the original quality description in English
            val englishDescription = getEnglishQualityDescription(qualityRating)
            
            // Get current language and translate if not English
            val currentLanguage = Locale.getDefault().language
            if (currentLanguage != "en" && englishDescription.isNotEmpty()) {
                viewModelScope.launch {
                    translationService.translateText(englishDescription, "en", currentLanguage).fold(
                        onSuccess = { translatedDesc ->
                            qualityDescription = translatedDesc
                        },
                        onFailure = {
                            // If translation fails, fall back to getting description from repository
                            qualityDescription = repository.getQualityDescription(qualityRating, appContext)
                        }
                    )
                }
            } else {
                // For English, just use the resource string
                qualityDescription = repository.getQualityDescription(qualityRating, appContext)
            }
        }
    }
    
    /**
     * Helper to get English quality descriptions for translation
     */
    private fun getEnglishQualityDescription(rating: String): String {
        return when (rating) {
            "Poor" -> "There's significant room for improvement in your dietary choices."
            "Fair" -> "Your diet has some healthy elements, but could use improvement in certain areas."
            "Good" -> "You're making healthy choices with room for improvement."
            else -> "You're making excellent dietary choices that support optimal health."
        }
    }
    
    /**
     * Load user's insight data from repository
     * 
     * @param context Application context used to access the database
     * @param userId Unique identifier for the user
     */
    fun loadInsightData(context: Context, userId: String) {
        // Store application context for localization purposes
        appContext = context.applicationContext
        
        // Reset states before loading
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading insight data for user: $userId")
                
                // Get patient from database
                val patient = repository.getPatientFromDatabase(context, userId)
                
                if (patient == null) {
                    // Handle case where patient is not found
                    Log.e(TAG, "Patient not found in database for user: $userId")
                    errorMessage = appContext?.getString(R.string.error_load_nutrition_data) 
                        ?: "User data not found. Please try again later."
                    isLoading = false
                    return@launch
                }
                
                // Build insight data from patient
                val data = repository.buildInsightData(patient)
                
                if (data == null) {
                    Log.e(TAG, "Failed to build insight data for user: $userId")
                    errorMessage = appContext?.getString(R.string.error_load_nutrition_data)
                        ?: "Could not process nutrition data. Please try again later."
                    isLoading = false
                    return@launch
                }
                
                // Update state with loaded data
                insightData = data
                Log.d(TAG, "Successfully loaded insight data for user: $userId")
                
                // Calculate derived values for UI
                calculateDerivedValues()
                
                // Loading complete
                isLoading = false
            } catch (e: Exception) {
                // Handle any exceptions during loading
                Log.e(TAG, "Error loading insight data: ${e.message}", e)
                errorMessage = appContext?.getString(R.string.error_occurred, e.message ?: "")
                    ?: "An error occurred: ${e.message ?: "Unknown error"}"
                isLoading = false
            }
        }
    }
    
    /**
     * Calculate UI values based on the insight data
     */
    private fun calculateDerivedValues() {
        insightData?.let { data ->
            // Round total score to integer for display
            totalScoreInt = data.totalScore.roundToInt()
            
            // Determine quality rating based on total score ranges
            qualityRating = when {
                totalScoreInt < 25 -> appContext?.getString(R.string.quality_poor) ?: "Poor"
                totalScoreInt < 50 -> appContext?.getString(R.string.quality_fair) ?: "Fair"
                totalScoreInt < 75 -> appContext?.getString(R.string.quality_good) ?: "Good"
                else -> appContext?.getString(R.string.quality_excellent) ?: "Excellent"
            }
            
            // Assign color based on quality rating for visual indicators
            qualityColor = when (qualityRating) {
                appContext?.getString(R.string.quality_poor) -> Color.Red
                appContext?.getString(R.string.quality_fair) -> Color(0xFFFFA500) // Orange
                appContext?.getString(R.string.quality_good) -> Green40
                else -> Green40
            }
            
            // Get quality description
            val englishDescription = getEnglishQualityDescription(qualityRating)
            val currentLanguage = Locale.getDefault().language
            
            if (currentLanguage != "en") {
                // For non-English languages, attempt to translate
                viewModelScope.launch {
                    translationService.translateText(englishDescription, "en", currentLanguage).fold(
                        onSuccess = { translatedDesc ->
                            qualityDescription = translatedDesc
                        },
                        onFailure = {
                            // If translation fails, fall back to localized string resource
                            qualityDescription = repository.getQualityDescription(qualityRating, appContext)
                        }
                    )
                }
            } else {
                // For English, use the string resource
                qualityDescription = repository.getQualityDescription(qualityRating, appContext)
            }
            
            Log.d(TAG, "Calculated derived values: score=$totalScoreInt, rating=$qualityRating")
        }
    }
    
    /**
     * Get sharing text for the user's score
     * 
     * @return Formatted text for sharing
     */
    fun getSharingText(): String {
        return appContext?.getString(R.string.share_score_template, totalScoreInt, qualityRating)
            ?: "Hi, I just got a HEIFA score of $totalScoreInt out of 100 with the NutriTrack app! My diet quality is currently rated as $qualityRating."
    }
    
    /**
     * Retry loading insight data
     * Used when an error occurs and the user wants to try again
     * 
     * @param context Application context used to access the database
     * @param userId Unique identifier for the user
     */
    fun retryLoading(context: Context, userId: String) {
        loadInsightData(context, userId)
    }
}
