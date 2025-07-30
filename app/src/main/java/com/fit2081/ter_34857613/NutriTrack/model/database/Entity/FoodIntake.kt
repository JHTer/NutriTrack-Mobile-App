package com.fit2081.ter_34857613.NutriTrack.model.database.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing food intake records in the NutriTrack database.
 * Maps to the "food_intakes" table in the Room database.
 * Contains foreign key relationship to the Patient entity.
 */
@Entity(
    tableName = "food_intakes",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class FoodIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // Foreign key to Patient
    val date: Long, // Timestamp of the intake
    
    // Questionnaire responses
    val vegetablesServings: Int,
    val fruitServings: Int,
    val grainsServings: Int,
    val proteinServings: Int,
    val dairyServings: Int,
    val waterAmount: Int,
    val alcoholAmount: Int,
    val processedFoodAmount: Int,
    val sugarAmount: Int,
    val saltAmount: Int
) 