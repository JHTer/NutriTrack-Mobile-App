package com.fit2081.ter_34857613.NutriTrack.model.database.DAO

import androidx.room.*
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.PatientFoodPreferences

/**
 * Data Access Object for PatientFoodPreferences entity.
 * Provides methods to query, insert, update and delete food preferences in the database.
 */
@Dao
interface FoodPreferencesDao {
    /**
     * Insert new preferences into the database.
     * If there's a conflict (same userId), replace the existing record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: PatientFoodPreferences)
    
    /**
     * Update existing preferences.
     */
    @Update
    suspend fun update(preferences: PatientFoodPreferences)
    
    /**
     * Get preferences by userId.
     */
    @Query("SELECT * FROM food_preferences WHERE userId = :userId")
    suspend fun getPreferencesByUserId(userId: String): PatientFoodPreferences?
    
    /**
     * Delete preferences by userId.
     */
    @Query("DELETE FROM food_preferences WHERE userId = :userId")
    suspend fun deletePreferencesByUserId(userId: String)
    
    /**
     * Check if a user has completed the questionnaire.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM food_preferences WHERE userId = :userId)")
    suspend fun hasCompletedQuestionnaire(userId: String): Boolean
} 