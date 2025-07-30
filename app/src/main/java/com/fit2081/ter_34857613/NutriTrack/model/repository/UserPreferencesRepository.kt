package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import kotlinx.coroutines.runBlocking

/**
 * Repository for managing user-specific preferences and application state, primarily related to questionnaires.
 *
 * This class uses SharedPreferences for storing simple boolean flags (like questionnaire completion status)
 * and also interacts with the database ([FoodPreferencesDao]) to check or reset questionnaire data.
 * It provides a consolidated way to manage whether a user has completed their initial setup questionnaires.
 */
class UserPreferencesRepository {
    
    /**
     * Companion object for [UserPreferencesRepository].
     * Contains constants for SharedPreferences names and keys, and the logging tag.
     */
    companion object {
        private const val PREFERENCES_NAME = "NutriTrack_UserPreferences"
        private const val KEY_COMPLETED_QUESTIONNAIRE_PREFIX = "completed_questionnaire_"
        private const val TAG = "UserPrefsRepository"
    }
    
    /**
     * Marks the initial questionnaire as completed for a specific user in SharedPreferences.
     *
     * @param context The application [Context] used to access SharedPreferences.
     * @param userId The unique identifier of the user for whom the questionnaire is being marked as completed.
     */
    fun markQuestionnaireCompleted(context: Context, userId: String) {
        val sharedPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_COMPLETED_QUESTIONNAIRE_PREFIX + userId, true).apply()
        Log.d(TAG, "Marked questionnaire as completed for user $userId")
    }
    
    /**
     * Checks if a user has completed their initial questionnaire.
     *
     * This method first queries the database via [FoodPreferencesDao] to see if the user
     * has food preferences stored, which implies questionnaire completion.
     * If not found in the database or if a database error occurs, it falls back to checking
     * SharedPreferences for an explicit completion flag.
     * As a final fallback, it checks for legacy questionnaire data in a user-specific SharedPreferences file.
     *
     * @param context The application [Context] used for database and SharedPreferences access.
     * @param userId The unique identifier of the user.
     * @return `true` if the user is considered to have completed the questionnaire (based on database, current
     *         SharedPreferences, or legacy SharedPreferences), `false` otherwise.
     */
    fun hasCompletedQuestionnaire(context: Context, userId: String): Boolean {
        // First check the database
        try {
            val database = NutriTrackDatabase.getDatabase(context)
            val hasCompletedInDb = runBlocking {
                database.foodPreferencesDao().hasCompletedQuestionnaire(userId)
            }
            
            if (hasCompletedInDb) {
                Log.d(TAG, "User $userId has completed questionnaire according to database")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking database for completed questionnaire: ${e.message}", e)
        }
        
        // Fall back to SharedPreferences
        val sharedPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        
        // First check in our preferences if we've explicitly marked it completed
        if (sharedPrefs.contains(KEY_COMPLETED_QUESTIONNAIRE_PREFIX + userId)) {
            val completed = sharedPrefs.getBoolean(KEY_COMPLETED_QUESTIONNAIRE_PREFIX + userId, false)
            Log.d(TAG, "User $userId has completed questionnaire according to preferences: $completed")
            return completed
        }
        
        // If not marked in preferences, check if questionnaire data exists in the user's preferences file
        val questionnairePrefs = context.getSharedPreferences("NutriTrackPrefs_$userId", Context.MODE_PRIVATE)
        val hasLegacyData = questionnairePrefs.contains("personaId")
        Log.d(TAG, "User $userId has legacy questionnaire data: $hasLegacyData")
        return hasLegacyData
    }
    
    /**
     * Resets the questionnaire completion status for a specific user.
     *
     * This function removes the completion flag from the main SharedPreferences, clears any data
     * from legacy user-specific SharedPreferences files, and deletes any food preferences
     * for the user from the database via [FoodPreferencesDao].
     * This is typically used for testing or allowing a user to retake the questionnaire.
     *
     * @param context The application [Context] for SharedPreferences and database access.
     * @param userId The unique identifier of the user whose questionnaire status is to be reset.
     */
    fun resetQuestionnaireStatus(context: Context, userId: String) {
        // Clear from SharedPreferences
        val sharedPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_COMPLETED_QUESTIONNAIRE_PREFIX + userId).apply()
        
        // Clear from legacy SharedPreferences
        val questionnairePrefs = context.getSharedPreferences("NutriTrackPrefs_$userId", Context.MODE_PRIVATE)
        questionnairePrefs.edit().clear().apply()
        
        // Remove from database
        try {
            val database = NutriTrackDatabase.getDatabase(context)
            runBlocking {
                database.foodPreferencesDao().deletePreferencesByUserId(userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from database: ${e.message}", e)
        }
        
        Log.d(TAG, "Reset questionnaire status for user $userId")
    }
} 