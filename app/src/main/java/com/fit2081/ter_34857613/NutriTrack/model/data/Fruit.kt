package com.fit2081.ter_34857613.NutriTrack.model.data

/**
 * Data class representing a fruit and its nutritional information from the FruityVice API.
 */
data class Fruit(
    val id: Int,
    val name: String,
    val family: String,
    val genus: String,
    val order: String,
    val nutritions: Nutritions
)

/**
 * Data class representing nutritional information for a fruit.
 */
data class Nutritions(
    val calories: Double,
    val fat: Double,
    val sugar: Double,
    val carbohydrates: Double,
    val protein: Double,
    val fiber: Double
) 