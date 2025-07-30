package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context

/**
 * Repository responsible for managing questionnaire data, specifically user food preferences.
 *
 * This class handles the saving and loading of [FoodPreferences] data to and from
 * SharedPreferences. It uses user-specific SharedPreferences files to ensure data isolation
 * between different users of the application.
 */
class QuestionnaireRepository {
    
    /**
     * Saves a user's food preferences to a user-specific SharedPreferences file.
     *
     * Each user's preferences are stored in a separate file named `NutriTrackPrefs_[userId]`.
     * This method stores boolean flags for various food categories, persona information (ID and name),
     * and string values for meal timing and sleep habits.
     *
     * @param context The application [Context] used to access SharedPreferences.
     * @param userId The unique identifier of the user whose preferences are being saved.
     *               This ID is incorporated into the SharedPreferences filename.
     * @param preferences The [FoodPreferences] object containing the data to be saved.
     */
    fun savePreferences(context: Context, userId: String, preferences: FoodPreferences) {
        val sharedPrefs = context.getSharedPreferences("NutriTrackPrefs_$userId", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            // Save all food category selections
            putBoolean("fruits", preferences.fruits)
            putBoolean("vegetables", preferences.vegetables)
            putBoolean("grains", preferences.grains)
            putBoolean("redMeat", preferences.redMeat)
            putBoolean("seafood", preferences.seafood)
            putBoolean("poultry", preferences.poultry)
            putBoolean("fish", preferences.fish)
            putBoolean("eggs", preferences.eggs)
            putBoolean("nutsSeeds", preferences.nutsSeeds)
            
            // Save persona information
            putInt("personaId", preferences.personaId)
            putString("personaName", preferences.personaName)
            
            // Save time preferences
            putString("biggestMealTime", preferences.biggestMealTime)
            putString("sleepTime", preferences.sleepTime)
            putString("wakeUpTime", preferences.wakeUpTime)
            
            // Commit changes
            apply()
        }
    }

    /**
     * Loads a user's food preferences from their specific SharedPreferences file.
     *
     * It attempts to read data from `NutriTrackPrefs_[userId]`. If the file does not exist
     * or does not contain a key indicating previous data storage (e.g., "personaId"),
     * it assumes no preferences have been saved for this user and returns `null`.
     * Otherwise, it reconstructs and returns a [FoodPreferences] object.
     *
     * @param context The application [Context] used to access SharedPreferences.
     * @param userId The unique identifier of the user whose preferences are to be loaded.
     * @return The loaded [FoodPreferences] object if data exists for the user, or `null` otherwise.
     */
    fun loadPreferences(context: Context, userId: String): FoodPreferences? {
        val sharedPrefs = context.getSharedPreferences("NutriTrackPrefs_$userId", Context.MODE_PRIVATE)
        
        // Check if preferences exist for this specific user
        if (!sharedPrefs.contains("personaId")) {
            return null
        }
        
        // Reconstruct preferences object from stored values
        return FoodPreferences(
            fruits = sharedPrefs.getBoolean("fruits", false),
            vegetables = sharedPrefs.getBoolean("vegetables", false),
            grains = sharedPrefs.getBoolean("grains", false),
            redMeat = sharedPrefs.getBoolean("redMeat", false),
            seafood = sharedPrefs.getBoolean("seafood", false),
            poultry = sharedPrefs.getBoolean("poultry", false),
            fish = sharedPrefs.getBoolean("fish", false),
            eggs = sharedPrefs.getBoolean("eggs", false),
            nutsSeeds = sharedPrefs.getBoolean("nutsSeeds", false),
            personaId = sharedPrefs.getInt("personaId", 0),
            personaName = sharedPrefs.getString("personaName", "") ?: "",
            biggestMealTime = sharedPrefs.getString("biggestMealTime", "") ?: "",
            sleepTime = sharedPrefs.getString("sleepTime", "") ?: "",
            wakeUpTime = sharedPrefs.getString("wakeUpTime", "") ?: ""
        )
    }
}

/**
 * Data class representing a user's comprehensive food preferences and related lifestyle information.
 *
 * This class encapsulates boolean selections for various food categories (fruits, vegetables, proteins, etc.),
 * the user's chosen dietary persona (ID and name), and their typical meal, sleep, and wake-up times.
 *
 * @property fruits Indicates whether the user consumes fruits.
 * @property vegetables Indicates whether the user consumes vegetables.
 * @property grains Indicates whether the user consumes grains.
 * @property redMeat Indicates whether the user consumes red meat.
 * @property seafood Indicates whether the user consumes seafood.
 * @property poultry Indicates whether the user consumes poultry.
 * @property fish Indicates whether the user consumes fish.
 * @property eggs Indicates whether the user consumes eggs.
 * @property nutsSeeds Indicates whether the user consumes nuts and seeds.
 * @property personaId The identifier for the user's selected dietary persona.
 * @property personaName The name associated with the selected dietary persona.
 * @property biggestMealTime A string representing the time user typically has their largest meal (e.g., "Lunch", "1 PM").
 * @property sleepTime A string representing the user's typical bedtime (e.g., "10:00 PM").
 * @property wakeUpTime A string representing the user's typical wake-up time (e.g., "6:00 AM").
 */
data class FoodPreferences(
    val fruits: Boolean,
    val vegetables: Boolean,
    val grains: Boolean,
    val redMeat: Boolean,
    val seafood: Boolean,
    val poultry: Boolean,
    val fish: Boolean,
    val eggs: Boolean,
    val nutsSeeds: Boolean,
    val personaId: Int,
    val personaName: String,
    val biggestMealTime: String,
    val sleepTime: String,
    val wakeUpTime: String
) 