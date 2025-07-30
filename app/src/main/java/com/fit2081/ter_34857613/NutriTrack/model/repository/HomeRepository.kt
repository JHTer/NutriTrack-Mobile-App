package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for accessing data required by the home screen.
 * This class handles the retrieval of user-specific nutrition data, such as HEIFA scores,
 * from the underlying database.
 */
class HomeRepository {
    /**
     * Companion object for [HomeRepository].
     * Contains constants like the logging tag.
     */
    companion object {
        private const val TAG = "HomeRepository"
    }
    
    /**
     * Loads a user's nutrition data from the database.
     *
     * This function queries the database for a patient matching the given `userId`.
     * If found, it maps the patient's data to a [NutritionData] object.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used to access the database instance.
     * @param userId The unique identifier for the user whose nutrition data is to be loaded.
     * @return A [NutritionData] object containing the user's name, sex, and HEIFA scores
     *         if the user is found. Returns `null` if no patient data is found for the `userId`
     *         or if an error occurs during database access.
     */
    suspend fun loadNutritionData(context: Context, userId: String): NutritionData? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading nutrition data from database for user: $userId")
            
            // Get the database instance
            val database = NutriTrackDatabase.getDatabase(context)
            
            // Query the patient from the database
            val patient = database.patientDao().getPatientById(userId)
            
            if (patient != null) {
                Log.d(TAG, "Found patient data in database for user: $userId")
                
                // Map the patient entity to NutritionData model
                return@withContext NutritionData(
                    userId = patient.userId,
                    name = patient.name,
                    sex = patient.sex,
                    heifaTotalScoreMale = patient.heifaTotalScoreMale?.toFloat() ?: 0f,
                    heifaTotalScoreFemale = patient.heifaTotalScoreFemale?.toFloat() ?: 0f
                )
            } else {
                Log.d(TAG, "No patient data found in database for user: $userId")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading nutrition data from database: ${e.message}", e)
            return@withContext null
        }
    }
}

/**
 * Data class representing essential user nutrition data for display purposes.
 * This typically includes information shown on a summary screen like the home screen.
 *
 * @property userId The unique identifier for the user.
 * @property name The name of the user.
 * @property sex The gender of the user (e.g., "Male", "Female").
 * @property heifaTotalScoreMale The user's total HEIFA (Healthy Eating Index for Australian Adults) score,
 *                               calculated using male-specific criteria. Defaults to 0f if not available.
 * @property heifaTotalScoreFemale The user's total HEIFA score, calculated using female-specific
 *                                 criteria. Defaults to 0f if not available.
 */
data class NutritionData(
    val userId: String,
    val name: String,
    val sex: String,
    val heifaTotalScoreMale: Float,
    val heifaTotalScoreFemale: Float
) 