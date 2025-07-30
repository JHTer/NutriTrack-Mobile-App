package com.fit2081.ter_34857613.NutriTrack.model.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a patient's food preferences in the NutriTrack database.
 * Maps to the "food_preferences" table in the Room database.
 * Stores data from the questionnaire previously kept in SharedPreferences.
 */
@Entity(tableName = "food_preferences")
data class PatientFoodPreferences(
    @PrimaryKey
    val userId: String,  // Foreign key to Patient table
    
    // Food categories
    val fruits: Boolean = false,
    val vegetables: Boolean = false,
    val grains: Boolean = false,
    val redMeat: Boolean = false,
    val seafood: Boolean = false,
    val poultry: Boolean = false,
    val fish: Boolean = false,
    val eggs: Boolean = false,
    val nutsSeeds: Boolean = false,
    
    // Persona information
    val personaId: Int = 0,
    val personaName: String = "",
    
    // Time preferences
    val biggestMealTime: String = "",
    val sleepTime: String = "",
    val wakeUpTime: String = ""
) 