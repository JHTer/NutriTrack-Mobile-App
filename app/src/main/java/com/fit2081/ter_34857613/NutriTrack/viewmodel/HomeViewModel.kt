package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.repository.HomeRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.NutritionData
import kotlinx.coroutines.launch

/**
 * ViewModel for home screen that handles nutrition data and state management
 * following MVVM architecture
 */
class HomeViewModel : ViewModel() {
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    // Repository to handle data operations
    private val repository = HomeRepository()
    
    // State for nutrition data
    var nutritionData by mutableStateOf<NutritionData?>(null)
        private set
        
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
        
    // Error state
    var errorMessage by mutableStateOf<String?>(null)
        private set
        
    // Application context for string resources
    private var appContext: Context? = null
    
    /**
     * Load user's nutrition data from repository
     * 
     * @param context Application context used to access the database
     * @param userId Unique identifier for the user
     */
    fun loadNutritionData(context: Context, userId: String) {
        // Store context for string resources
        this.appContext = context.applicationContext
        
        // Reset states
        errorMessage = null
        isLoading = true
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading nutrition data for user: $userId")
                val data = repository.loadNutritionData(context, userId)
                
                if (data != null) {
                    Log.d(TAG, "Nutrition data loaded successfully for user: $userId")
                    nutritionData = data
                } else {
                    Log.e(TAG, "Failed to load nutrition data for user: $userId")
                    errorMessage = appContext?.getString(R.string.error_load_nutrition_data)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading nutrition data: ${e.message}", e)
                errorMessage = appContext?.getString(R.string.error_occurred, e.message ?: appContext?.getString(R.string.error_unknown) ?: "")
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Calculate the user's score based on their gender
     * 
     * @return Formatted score string or N/A if data is not available
     */
    fun getUserScore(): String {
        return nutritionData?.let { data ->
            if (data.sex.equals(appContext?.getString(R.string.gender_male) ?: "Male", ignoreCase = true)) 
                appContext?.getString(R.string.score_format, data.heifaTotalScoreMale.toInt()) ?: "${data.heifaTotalScoreMale.toInt()}/100" 
            else 
                appContext?.getString(R.string.score_format, data.heifaTotalScoreFemale.toInt()) ?: "${data.heifaTotalScoreFemale.toInt()}/100"
        } ?: appContext?.getString(R.string.not_available) ?: "N/A"
    }

    /**
     * Get the user's name
     * 
     * @return User's name or "User" if data is not available
     */
    fun getUserName(): String {
        return nutritionData?.name?.takeIf { it.isNotBlank() } ?: appContext?.getString(R.string.default_user) ?: "User"
    }
} 