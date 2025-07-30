package com.fit2081.ter_34857613.NutriTrack.model.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriCoachTip

/**
 * Data Access Object for NutriCoachTip entity.
 * Provides methods to query, insert, update and delete nutrition coach tips.
 */
@Dao
interface NutriCoachTipDao {
    /**
     * Insert a new nutrition coach tip.
     */
    @Insert
    suspend fun insert(tip: NutriCoachTip): Long
    
    /**
     * Update an existing nutrition coach tip.
     */
    @Update
    suspend fun update(tip: NutriCoachTip)
    
    /**
     * Delete a nutrition coach tip.
     */
    @Delete
    suspend fun delete(tip: NutriCoachTip)
    
    /**
     * Delete all nutrition coach tips.
     */
    @Query("DELETE FROM nutri_coach_tips")
    suspend fun deleteAllTips()
    
    /**
     * Get all nutrition coach tips for a specific user.
     * Returns LiveData for observing changes.
     */
    @Query("SELECT * FROM nutri_coach_tips WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTipsByUserId(userId: String): LiveData<List<NutriCoachTip>>
    
    /**
     * Get tips for a specific user filtered by category.
     */
    @Query("SELECT * FROM nutri_coach_tips WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    fun getTipsByUserIdAndCategory(userId: String, category: String): LiveData<List<NutriCoachTip>>
    
    /**
     * Get a specific tip by its ID.
     */
    @Query("SELECT * FROM nutri_coach_tips WHERE id = :id")
    suspend fun getTipById(id: Long): NutriCoachTip?
    
    /**
     * Get the most recent tip for a user.
     */
    @Query("SELECT * FROM nutri_coach_tips WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentTip(userId: String): NutriCoachTip?
    
    /**
     * Get the count of tips for a specific user.
     */
    @Query("SELECT COUNT(*) FROM nutri_coach_tips WHERE userId = :userId")
    suspend fun getTipCount(userId: String): Int
} 