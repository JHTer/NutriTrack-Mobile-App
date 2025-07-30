package com.fit2081.ter_34857613.NutriTrack.model.repository

import androidx.lifecycle.LiveData
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriCoachTip
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.NutriCoachTipDao

/**
 * Repository for handling all NutriCoachTip-related database operations.
 * Acts as an abstraction layer between the [NutriCoachTipDao] and the ViewModel,
 * providing a clean API for data access and manipulation related to nutrition coach tips.
 *
 * @property nutriCoachTipDao The Data Access Object (DAO) for [NutriCoachTip] entities.
 */
class NutriCoachTipRepository(private val nutriCoachTipDao: NutriCoachTipDao) {
    
    /**
     * Retrieves all nutrition coach tips for a specific user, ordered by timestamp descending.
     *
     * This method returns a [LiveData] list of [NutriCoachTip] objects, allowing UI components
     * to observe changes to the user's tips in real-time.
     *
     * @param userId The unique identifier of the user whose tips are to be retrieved.
     * @return A [LiveData] list of [NutriCoachTip] entities associated with the given user ID.
     */
    fun getTipsByUserId(userId: String): LiveData<List<NutriCoachTip>> {
        return nutriCoachTipDao.getTipsByUserId(userId)
    }
    
    /**
     * Retrieves tips for a specific user that belong to a particular category, ordered by timestamp descending.
     *
     * This method allows filtering of tips based on their category (e.g., "hydration", "protein intake").
     * It returns a [LiveData] list, enabling real-time updates in the UI.
     *
     * @param userId The unique identifier of the user.
     * @param category The category name to filter the tips by.
     * @return A [LiveData] list of [NutriCoachTip] entities for the specified user and category.
     */
    fun getTipsByUserIdAndCategory(userId: String, category: String): LiveData<List<NutriCoachTip>> {
        return nutriCoachTipDao.getTipsByUserIdAndCategory(userId, category)
    }
    
    /**
     * Inserts a new nutrition coach tip into the database.
     *
     * This is a suspend function, indicating it should be called from a coroutine or another suspend function.
     *
     * @param tip The [NutriCoachTip] entity to be inserted.
     * @return The row ID of the newly inserted tip as a [Long]. If the insertion fails, it might return -1
     *         depending on the underlying database implementation.
     */
    suspend fun insert(tip: NutriCoachTip): Long {
        return nutriCoachTipDao.insert(tip)
    }
    
    /**
     * Updates an existing nutrition coach tip in the database.
     *
     * This is a suspend function.
     *
     * @param tip The [NutriCoachTip] entity to be updated. The entity should have its primary key (ID) set
     *            to identify the record to update.
     */
    suspend fun update(tip: NutriCoachTip) {
        nutriCoachTipDao.update(tip)
    }
    
    /**
     * Deletes a specific nutrition coach tip from the database.
     *
     * This is a suspend function.
     *
     * @param tip The [NutriCoachTip] entity to be deleted. The entity's primary key (ID) will be used
     *            to identify the record for deletion.
     */
    suspend fun delete(tip: NutriCoachTip) {
        nutriCoachTipDao.delete(tip)
    }
    
    /**
     * Retrieves a specific nutrition coach tip by its unique ID.
     *
     * This is a suspend function.
     *
     * @param id The unique [Long] identifier of the tip to be retrieved.
     * @return The [NutriCoachTip] entity if found, or `null` if no tip with the given ID exists.
     */
    suspend fun getTipById(id: Long): NutriCoachTip? {
        return nutriCoachTipDao.getTipById(id)
    }
    
    /**
     * Retrieves the most recent nutrition coach tip for a specific user.
     *
     * "Most recent" is determined by the highest timestamp value.
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user whose most recent tip is to be retrieved.
     * @return The most recent [NutriCoachTip] entity for the user, or `null` if the user has no tips.
     */
    suspend fun getMostRecentTip(userId: String): NutriCoachTip? {
        return nutriCoachTipDao.getMostRecentTip(userId)
    }
    
    /**
     * Gets the total count of nutrition coach tips for a specific user.
     *
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user.
     * @return An [Int] representing the total number of tips associated with the user.
     */
    suspend fun getTipCount(userId: String): Int {
        return nutriCoachTipDao.getTipCount(userId)
    }
    
    /**
     * Creates and saves a new nutrition coach tip with the current system timestamp.
     *
     * This helper method simplifies the creation of a new [NutriCoachTip] object
     * by automatically setting its timestamp and then inserting it into the database.
     * This is a suspend function.
     *
     * @param userId The unique identifier of the user for whom the tip is being saved.
     * @param content The textual content of the nutrition tip.
     * @param category The category to which this tip belongs (e.g., "general", "hydration").
     * @return The row ID of the newly inserted tip as a [Long].
     */
    suspend fun saveNewTip(userId: String, content: String, category: String): Long {
        val timestamp = System.currentTimeMillis()
        val newTip = NutriCoachTip(
            userId = userId,
            timestamp = timestamp,
            content = content,
            category = category
        )
        return insert(newTip)
    }
} 