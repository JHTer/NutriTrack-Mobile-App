package com.fit2081.ter_34857613.NutriTrack.model.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a patient in the NutriTrack database.
 * Maps to the "patients" table in the Room database.
 * Based on the CSV data structure from the user.csv file.
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val userId: String,
    val phoneNumber: String,
    val name: String = "", // Added for user registration
    val password: String = "", // Added for user authentication
    val sex: String,
    
    // HEIFA Scores
    val heifaTotalScoreMale: Double? = null,
    val heifaTotalScoreFemale: Double? = null,
    
    // Discretionary scores
    val discretionaryHeifaScoreMale: Double? = null,
    val discretionaryHeifaScoreFemale: Double? = null,
    val discretionaryServeSize: Double? = null,
    
    // Vegetables scores
    val vegetablesHeifaScoreMale: Double? = null,
    val vegetablesHeifaScoreFemale: Double? = null,
    val vegetablesWithLegumesAllocatedServeSize: Double? = null,
    val legumesAllocatedVegetables: Double? = null,
    val vegetablesVariationsScore: Double? = null,
    val vegetablesCruciferous: Double? = null,
    val vegetablesTubeAndBulb: Double? = null,
    val vegetablesOther: Double? = null,
    val legumes: Double? = null,
    val vegetablesGreen: Double? = null,
    val vegetablesRedAndOrange: Double? = null,
    
    // Fruit scores
    val fruitHeifaScoreMale: Double? = null,
    val fruitHeifaScoreFemale: Double? = null,
    val fruitServeSize: Double? = null,
    val fruitVariationsScore: Double? = null,
    val fruitPome: Double? = null,
    val fruitTropicalAndSubtropical: Double? = null,
    val fruitBerry: Double? = null,
    val fruitStone: Double? = null,
    val fruitCitrus: Double? = null,
    val fruitOther: Double? = null,
    
    // Grains and cereals scores
    val grainsAndCerealsHeifaScoreMale: Double? = null,
    val grainsAndCerealsHeifaScoreFemale: Double? = null,
    val grainsAndCerealsServeSize: Double? = null,
    val grainsAndCerealsNonWholeGrains: Double? = null,
    val wholeGrainsHeifaScoreMale: Double? = null,
    val wholeGrainsHeifaScoreFemale: Double? = null,
    val wholeGrainsServeSize: Double? = null,
    
    // Meat and alternatives scores
    val meatAndAlternativesHeifaScoreMale: Double? = null,
    val meatAndAlternativesHeifaScoreFemale: Double? = null,
    val meatAndAlternativesWithLegumesAllocatedServeSize: Double? = null,
    val legumesAllocatedMeatAndAlternatives: Double? = null,
    
    // Dairy and alternatives scores
    val dairyAndAlternativesHeifaScoreMale: Double? = null,
    val dairyAndAlternativesHeifaScoreFemale: Double? = null,
    val dairyAndAlternativesServeSize: Double? = null,
    
    // Sodium scores
    val sodiumHeifaScoreMale: Double? = null,
    val sodiumHeifaScoreFemale: Double? = null,
    val sodiumMgMilligrams: Double? = null,
    
    // Alcohol scores
    val alcoholHeifaScoreMale: Double? = null,
    val alcoholHeifaScoreFemale: Double? = null,
    val alcoholStandardDrinks: Double? = null,
    
    // Water scores
    val waterHeifaScoreMale: Double? = null,
    val waterHeifaScoreFemale: Double? = null,
    val water: Double? = null,
    val waterTotalML: Double? = null,
    val beverageTotalML: Double? = null,
    
    // Sugar scores
    val sugarHeifaScoreMale: Double? = null,
    val sugarHeifaScoreFemale: Double? = null,
    val sugar: Double? = null,
    
    // Fat scores
    val saturatedFatHeifaScoreMale: Double? = null,
    val saturatedFatHeifaScoreFemale: Double? = null,
    val saturatedFat: Double? = null,
    val unsaturatedFatHeifaScoreMale: Double? = null,
    val unsaturatedFatHeifaScoreFemale: Double? = null,
    val unsaturatedFatServeSize: Double? = null
) 