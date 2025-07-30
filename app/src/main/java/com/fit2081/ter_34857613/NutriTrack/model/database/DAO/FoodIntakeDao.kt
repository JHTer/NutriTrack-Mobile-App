package com.fit2081.ter_34857613.NutriTrack.model.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.FoodIntake

/**
 * Data Access Object for FoodIntake entity.
 * Provides methods to query, insert, update and delete food intake records.
 */
@Dao
interface FoodIntakeDao {
    /**
     * Insert a new food intake record.
     */
    @Insert
    suspend fun insert(foodIntake: FoodIntake): Long
    
    /**
     * Update an existing food intake record.
     */
    @Update
    suspend fun update(foodIntake: FoodIntake)
    
    /**
     * Delete a food intake record.
     */
    @Delete
    suspend fun delete(foodIntake: FoodIntake)
    
    /**
     * Delete all food intake records.
     */
    @Query("DELETE FROM food_intakes")
    suspend fun deleteAllFoodIntakes()
    
    /**
     * Get all food intake records for a specific user.
     * Returns LiveData for observing changes.
     */
    @Query("SELECT * FROM food_intakes WHERE userId = :userId ORDER BY date DESC")
    fun getFoodIntakesByUserId(userId: String): LiveData<List<FoodIntake>>
    
    /**
     * Get a specific food intake record by its ID.
     */
    @Query("SELECT * FROM food_intakes WHERE id = :id")
    suspend fun getFoodIntakeById(id: Long): FoodIntake?
    
    /**
     * Get the latest food intake record for a user.
     */
    @Query("SELECT * FROM food_intakes WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestFoodIntake(userId: String): FoodIntake?
    
    /**
     * Get the count of food intake records for a specific user.
     */
    @Query("SELECT COUNT(*) FROM food_intakes WHERE userId = :userId")
    suspend fun getIntakeCount(userId: String): Int
    
    /**
     * Get the average vegetable servings across all food intake records for a user.
     */
    @Query("SELECT AVG(vegetablesServings) FROM food_intakes WHERE userId = :userId")
    suspend fun getAverageVegetableServings(userId: String): Float?
    
    /**
     * Get the average fruit servings across all food intake records for a user.
     */
    @Query("SELECT AVG(fruitServings) FROM food_intakes WHERE userId = :userId")
    suspend fun getAverageFruitServings(userId: String): Float?
} 