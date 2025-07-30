package com.fit2081.ter_34857613.NutriTrack.model.repository

import androidx.lifecycle.LiveData
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.FoodIntake
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.FoodIntakeDao

/**
 * Repository for handling all [FoodIntake]-related database operations.
 * This class serves as an abstraction layer between the [FoodIntakeDao] (database access)
 * and the ViewModels, providing a clean API for data manipulation concerning food intake records.
 *
 * @property foodIntakeDao The Data Access Object (DAO) for [FoodIntake] entities.
 */
class FoodIntakeRepository(private val foodIntakeDao: FoodIntakeDao) {
    
    /**
     * Retrieves all food intake records for a specific user, ordered by date descending.
     *
     * This method returns a [LiveData] list of [FoodIntake] objects, allowing UI components
     * to observe changes to the user's food intake history in real-time.
     *
     * @param userId The unique identifier of the user whose food intake records are to be retrieved.
     * @return A [LiveData] list of [FoodIntake] entities associated with the given user ID.
     */
    fun getFoodIntakesByUserId(userId: String): LiveData<List<FoodIntake>> {
        return foodIntakeDao.getFoodIntakesByUserId(userId)
    }
    
    /**
     * Inserts a new food intake record into the database.
     *
     * This is a suspend function, intended to be called from a coroutine or another suspend function.
     *
     * @param foodIntake The [FoodIntake] entity to be inserted.
     * @return The row ID of the newly inserted record as a [Long]. If insertion fails, the return value
     *         might be -1, depending on the underlying database implementation.
     */
    suspend fun insert(foodIntake: FoodIntake): Long {
        return foodIntakeDao.insert(foodIntake)
    }
    
    /**
     * Updates an existing food intake record in the database.
     *
     * This is a suspend function.
     *
     * @param foodIntake The [FoodIntake] entity to be updated. Its primary key (ID) must be set
     *                   to identify the record for updating.
     */
    suspend fun update(foodIntake: FoodIntake) {
        foodIntakeDao.update(foodIntake)
    }
    
    /**
     * Deletes a specific food intake record from the database.
     *
     * This is a suspend function.
     *
     * @param foodIntake The [FoodIntake] entity to be deleted. Its primary key (ID) will be used
     *                   to identify the record for deletion.
     */
    suspend fun delete(foodIntake: FoodIntake) {
        foodIntakeDao.delete(foodIntake)
    }
    
    /**
     * Retrieves a specific food intake record by its unique ID.
     *
     * This is a suspend function.
     *
     * @param id The unique [Long] identifier of the food intake record to be retrieved.
     * @return The [FoodIntake] entity if found, or `null` if no record with the given ID exists.
     */
    suspend fun getFoodIntakeById(id: Long): FoodIntake? {
        return foodIntakeDao.getFoodIntakeById(id)
    }
    
    /**
     * Retrieves the most recent food intake record for a specific user.
     *
     * "Most recent" is determined by the latest date recorded in the [FoodIntake] entries.
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user whose latest food intake is to be retrieved.
     * @return The most recent [FoodIntake] entity for the user, or `null` if the user has no intake records.
     */
    suspend fun getLatestFoodIntake(userId: String): FoodIntake? {
        return foodIntakeDao.getLatestFoodIntake(userId)
    }
    
    /**
     * Gets the total count of food intake records for a specific user.
     *
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user.
     * @return An [Int] representing the total number of food intake records for the user.
     */
    suspend fun getIntakeCount(userId: String): Int {
        return foodIntakeDao.getIntakeCount(userId)
    }
    
    /**
     * Calculates the average number of vegetable servings across all food intake records for a specific user.
     *
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user.
     * @return A [Float] representing the average vegetable servings. Returns `null` if the user has no
     *         intake records or if the average cannot be calculated (e.g., all servings are null).
     */
    suspend fun getAverageVegetableServings(userId: String): Float? {
        return foodIntakeDao.getAverageVegetableServings(userId)
    }
    
    /**
     * Calculates the average number of fruit servings across all food intake records for a specific user.
     *
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user.
     * @return A [Float] representing the average fruit servings. Returns `null` if the user has no
     *         intake records or if the average cannot be calculated.
     */
    suspend fun getAverageFruitServings(userId: String): Float? {
        return foodIntakeDao.getAverageFruitServings(userId)
    }
} 