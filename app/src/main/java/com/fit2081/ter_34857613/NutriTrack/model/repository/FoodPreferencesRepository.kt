package com.fit2081.ter_34857613.NutriTrack.model.repository

import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.FoodPreferencesDao
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.PatientFoodPreferences

/**
 * Repository for managing user food preferences.
 * This class acts as an abstraction layer over the [FoodPreferencesDao],
 * handling the saving and loading of [PatientFoodPreferences] entities, which represent
 * a user's dietary choices and lifestyle habits (e.g., meal times, sleep schedule).
 *
 * It also provides a way to check if a user has completed the initial food preferences questionnaire.
 *
 * @property foodPreferencesDao The Data Access Object (DAO) for [PatientFoodPreferences] entities.
 */
class FoodPreferencesRepository(private val foodPreferencesDao: FoodPreferencesDao) {
    
    /**
     * Saves a user's food preferences to the database.
     *
     * This function takes a [FoodPreferences] domain object, maps it to a
     * [PatientFoodPreferences] database entity, and then inserts it using the [foodPreferencesDao].
     * If preferences for the user already exist, they will typically be replaced or updated based on the
     * DAO's `insert` conflict strategy (usually `OnConflictStrategy.REPLACE`).
     *
     * @param userId The unique identifier of the user whose preferences are being saved.
     * @param preferences The [FoodPreferences] object containing the user's dietary and lifestyle choices.
     */
    suspend fun savePreferences(userId: String, preferences: FoodPreferences) {
        val dbPreferences = PatientFoodPreferences(
            userId = userId,
            fruits = preferences.fruits,
            vegetables = preferences.vegetables,
            grains = preferences.grains,
            redMeat = preferences.redMeat,
            seafood = preferences.seafood,
            poultry = preferences.poultry,
            fish = preferences.fish,
            eggs = preferences.eggs,
            nutsSeeds = preferences.nutsSeeds,
            personaId = preferences.personaId,
            personaName = preferences.personaName,
            biggestMealTime = preferences.biggestMealTime,
            sleepTime = preferences.sleepTime,
            wakeUpTime = preferences.wakeUpTime
        )
        
        foodPreferencesDao.insert(dbPreferences)
    }

    /**
     * Loads a user's food preferences from the database.
     *
     * This function retrieves the [PatientFoodPreferences] entity for the given `userId`
     * from the database. If found, it maps the entity back to a [FoodPreferences] domain object.
     *
     * @param userId The unique identifier of the user whose preferences are to be loaded.
     * @return A [FoodPreferences] object if preferences are found for the user, otherwise `null`.
     */
    suspend fun loadPreferences(userId: String): FoodPreferences? {
        val dbPreferences = foodPreferencesDao.getPreferencesByUserId(userId) ?: return null
        
        return FoodPreferences(
            fruits = dbPreferences.fruits,
            vegetables = dbPreferences.vegetables,
            grains = dbPreferences.grains,
            redMeat = dbPreferences.redMeat,
            seafood = dbPreferences.seafood,
            poultry = dbPreferences.poultry,
            fish = dbPreferences.fish,
            eggs = dbPreferences.eggs,
            nutsSeeds = dbPreferences.nutsSeeds,
            personaId = dbPreferences.personaId,
            personaName = dbPreferences.personaName,
            biggestMealTime = dbPreferences.biggestMealTime,
            sleepTime = dbPreferences.sleepTime,
            wakeUpTime = dbPreferences.wakeUpTime
        )
    }
    
    /**
     * Checks if a user has previously completed and saved their food preferences questionnaire.
     *
     * This is determined by querying the [foodPreferencesDao] to see if there is any
     * [PatientFoodPreferences] record associated with the given `userId`.
     *
     * @param userId The unique identifier of the user.
     * @return `true` if the user has existing food preferences stored in the database (implying
     *         questionnaire completion), `false` otherwise.
     */
    suspend fun hasCompletedQuestionnaire(userId: String): Boolean {
        return foodPreferencesDao.hasCompletedQuestionnaire(userId)
    }
} 