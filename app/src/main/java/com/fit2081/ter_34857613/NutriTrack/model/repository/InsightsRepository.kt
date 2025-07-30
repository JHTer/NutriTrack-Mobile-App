package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for fetching and processing data for the Insights screen.
 *
 * This class handles retrieving [Patient] data from the database, transforming it into
 * a more display-friendly [InsightData] object (which includes gender-specific HEIFA scores),
 * and providing localized descriptions based on overall diet quality ratings.
 */
class InsightsRepository {
    /**
     * Companion object for [InsightsRepository].
     * Contains constants like the logging tag.
     */
    companion object {
        private const val TAG = "InsightsRepository"
    }
    
    /**
     * Retrieves a [Patient] entity from the database based on the provided user ID.
     *
     * This function accesses the database via [PatientDao] on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used to obtain the database instance.
     * @param userId The unique identifier of the user whose data is to be retrieved.
     * @return The [Patient] object if found, or `null` if no patient matches the `userId`
     *         or if a database error occurs.
     */
    suspend fun getPatientFromDatabase(context: Context, userId: String): Patient? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Retrieving patient data from database for user: $userId")
            
            // Get the database instance
            val database = NutriTrackDatabase.getDatabase(context)
            
            // Query the patient from the database
            val patient = database.patientDao().getPatientById(userId)
            
            if (patient != null) {
                Log.d(TAG, "Successfully found patient data in database for user: $userId")
                return@withContext patient
            } else {
                Log.d(TAG, "No patient data found in database for user: $userId")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving patient data from database: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Transforms a [Patient] entity into an [InsightData] object.
     *
     * This function extracts various HEIFA (Healthy Eating Index for Australian Adults) component scores
     * from the [Patient] object. It considers the patient's sex to select the appropriate
     * gender-specific scores (e.g., `heifaTotalScoreMale` vs. `heifaTotalScoreFemale`).
     * If any score is `null` in the [Patient] object, it defaults to `0.0` in the [InsightData].
     *
     * @param patient The [Patient] entity obtained from the database. If `null`, this function
     *                will also return `null`.
     * @return An [InsightData] object populated with the patient's nutrition scores, or `null`
     *         if the input `patient` is `null`.
     */
    fun buildInsightData(patient: Patient?): InsightData? {
        if (patient == null) {
            Log.d(TAG, "Cannot build insight data: patient is null")
            return null
        }
        
        // Get user's sex from Patient entity for gender-specific scoring
        val userSex = patient.sex
        val isUserMale = userSex.equals("Male", ignoreCase = true)
    
        // Extract scores from Patient entity based on biological sex
        // Using safe null handling with Elvis operator for all values
        val totalScore = if (isUserMale) patient.heifaTotalScoreMale ?: 0.0
                         else patient.heifaTotalScoreFemale ?: 0.0
                         
        val vegetablesScore = if (isUserMale) patient.vegetablesHeifaScoreMale ?: 0.0
                              else patient.vegetablesHeifaScoreFemale ?: 0.0
                              
        val fruitsScore = if (isUserMale) patient.fruitHeifaScoreMale ?: 0.0
                          else patient.fruitHeifaScoreFemale ?: 0.0
                          
        val grainsScore = if (isUserMale) patient.grainsAndCerealsHeifaScoreMale ?: 0.0
                          else patient.grainsAndCerealsHeifaScoreFemale ?: 0.0
                          
        val wholeGrainsScore = if (isUserMale) patient.wholeGrainsHeifaScoreMale ?: 0.0
                               else patient.wholeGrainsHeifaScoreFemale ?: 0.0
                               
        val meatScore = if (isUserMale) patient.meatAndAlternativesHeifaScoreMale ?: 0.0
                        else patient.meatAndAlternativesHeifaScoreFemale ?: 0.0
                        
        val dairyScore = if (isUserMale) patient.dairyAndAlternativesHeifaScoreMale ?: 0.0
                         else patient.dairyAndAlternativesHeifaScoreFemale ?: 0.0
                         
        val waterScore = if (isUserMale) patient.waterHeifaScoreMale ?: 0.0
                         else patient.waterHeifaScoreFemale ?: 0.0
                         
        val unsaturatedFatsScore = if (isUserMale) patient.unsaturatedFatHeifaScoreMale ?: 0.0
                                   else patient.unsaturatedFatHeifaScoreFemale ?: 0.0
                                   
        val sodiumScore = if (isUserMale) patient.sodiumHeifaScoreMale ?: 0.0
                          else patient.sodiumHeifaScoreFemale ?: 0.0
                          
        val sugarScore = if (isUserMale) patient.sugarHeifaScoreMale ?: 0.0
                         else patient.sugarHeifaScoreFemale ?: 0.0
                         
        val alcoholScore = if (isUserMale) patient.alcoholHeifaScoreMale ?: 0.0
                           else patient.alcoholHeifaScoreFemale ?: 0.0
                           
        val discretionaryScore = if (isUserMale) patient.discretionaryHeifaScoreMale ?: 0.0
                                 else patient.discretionaryHeifaScoreFemale ?: 0.0
    
        Log.d(TAG, "Successfully built insight data for user with sex: $userSex")
        
        return InsightData(
            sex = userSex,
            totalScore = totalScore,
            vegetablesScore = vegetablesScore,
            fruitsScore = fruitsScore,
            grainsScore = grainsScore,
            wholeGrainsScore = wholeGrainsScore,
            meatScore = meatScore,
            dairyScore = dairyScore,
            waterScore = waterScore,
            unsaturatedFatsScore = unsaturatedFatsScore,
            sodiumScore = sodiumScore,
            sugarScore = sugarScore,
            alcoholScore = alcoholScore,
            discretionaryScore = discretionaryScore
        )
    }
    
    /**
     * Provides a localized, descriptive text based on a diet quality rating string.
     *
     * This function maps a simple rating (e.g., "Poor", "Fair", "Good", "Excellent") to a more
     * detailed, user-friendly description. If a [Context] is provided, it attempts to load
     * these descriptions from string resources (e.g., [R.string.quality_poor_desc]) to support
     * localization. If no [Context] is available, it falls back to hardcoded English strings.
     *
     * @param rating A string representing the diet quality (expected values: "Poor", "Fair", "Good", "Excellent").
     *               Other values will default to the "Excellent" description.
     * @param context An optional [Context] used to access localized string resources. If `null`,
     *                hardcoded English descriptions are used.
     * @return A [String] containing the personalized feedback description corresponding to the rating.
     */
    fun getQualityDescription(rating: String, context: Context? = null): String {
        // If context is available, use string resources
        context?.let {
            return when (rating) {
                "Poor" -> context.getString(R.string.quality_poor_desc)
                "Fair" -> context.getString(R.string.quality_fair_desc)
                "Good" -> context.getString(R.string.quality_good_desc)
                else -> context.getString(R.string.quality_excellent_desc)
            }
        }
        
        // Fallback to hardcoded strings if context not available
        return when (rating) {
            "Poor" -> "There's significant room for improvement in your dietary choices."
            "Fair" -> "Your diet has some healthy elements, but could use improvement in certain areas."
            "Good" -> "You're making healthy choices with room for improvement."
            else -> "You're making excellent dietary choices that support optimal health."
        }
    }
}

/**
 * Data class representing processed nutrition insight data for a user.
 *
 * This class holds various HEIFA (Healthy Eating Index for Australian Adults) component scores
 * and the total score, adjusted for the user's sex. It serves as a structured container
 * for displaying detailed nutritional insights.
 *
 * @property sex The sex of the user (e.g., "Male", "Female"), used to determine which set of HEIFA scores to apply.
 * @property totalScore The overall HEIFA total score.
 * @property vegetablesScore Score for vegetable intake.
 * @property fruitsScore Score for fruit intake.
 * @property grainsScore Score for general grain intake.
 * @property wholeGrainsScore Score specifically for whole grain intake.
 * @property meatScore Score for meat and alternatives intake.
 * @property dairyScore Score for dairy and alternatives intake.
 * @property waterScore Score for water intake.
 * @property unsaturatedFatsScore Score for unsaturated fat intake.
 * @property sodiumScore Score for sodium intake (lower is generally better but score reflects adherence to guidelines).
 * @property sugarScore Score related to added sugar intake (lower is generally better).
 * @property alcoholScore Score for alcohol consumption (lower is generally better).
 * @property discretionaryScore Score for discretionary food choices (e.g., snacks, sweets; lower is generally better).
 */
data class InsightData(
    val sex: String,
    val totalScore: Double,
    val vegetablesScore: Double,
    val fruitsScore: Double,
    val grainsScore: Double,
    val wholeGrainsScore: Double,
    val meatScore: Double,
    val dairyScore: Double,
    val waterScore: Double,
    val unsaturatedFatsScore: Double,
    val sodiumScore: Double,
    val sugarScore: Double,
    val alcoholScore: Double,
    val discretionaryScore: Double
) 