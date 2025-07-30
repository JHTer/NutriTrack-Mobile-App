package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.FoodIntake
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.repository.FoodIntakeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing and providing data related to food intake for the UI.
 *
 * This ViewModel interacts with the [FoodIntakeRepository] to perform CRUD (Create, Read, Update, Delete)
 * operations on [FoodIntake] entities. It provides methods to fetch food intake records for a user,
 * insert new records, update existing ones, and delete records.
 * It also includes a method to add a new food intake record with various nutritional details and
 * methods to calculate average servings for specific food categories (vegetables, fruits).
 *
 * All database operations are performed asynchronously using Kotlin coroutines launched in the `viewModelScope`.
 *
 * @param application The application context, used to initialize the database and repository.
 */
class FoodIntakeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodIntakeRepository
    
    init {
        val database = NutriTrackDatabase.getDatabase(application)
        repository = FoodIntakeRepository(database.foodIntakeDao())
    }
    
    /**
     * Retrieves all food intake records for a specific user as [LiveData].
     *
     * The returned [LiveData] will observe changes in the underlying data and update the UI accordingly.
     *
     * @param userId The unique identifier of the user whose food intake records are to be fetched.
     * @return A [LiveData] list of [FoodIntake] objects for the specified user.
     */
    fun getFoodIntakesByUserId(userId: String): LiveData<List<FoodIntake>> {
        return repository.getFoodIntakesByUserId(userId)
    }
    
    /**
     * Inserts a new food intake record into the database.
     *
     * This operation is performed asynchronously within the `viewModelScope`.
     *
     * @param foodIntake The [FoodIntake] object to be inserted.
     */
    fun insert(foodIntake: FoodIntake) = viewModelScope.launch {
        repository.insert(foodIntake)
    }
    
    /**
     * Updates an existing food intake record in the database.
     *
     * This operation is performed asynchronously within the `viewModelScope`.
     *
     * @param foodIntake The [FoodIntake] object to be updated. It should have a valid ID that matches an existing record.
     */
    fun update(foodIntake: FoodIntake) = viewModelScope.launch {
        repository.update(foodIntake)
    }
    
    /**
     * Deletes a food intake record from the database.
     *
     * This operation is performed asynchronously within the `viewModelScope`.
     *
     * @param foodIntake The [FoodIntake] object to be deleted.
     */
    fun delete(foodIntake: FoodIntake) = viewModelScope.launch {
        repository.delete(foodIntake)
    }
    
    /**
     * Adds a new food intake record to the database with the current system time as the date.
     *
     * This method constructs a [FoodIntake] object from the provided nutritional details
     * (servings of vegetables, fruits, grains, protein, dairy; amounts of water, alcohol,
     * processed food, sugar, salt) and then inserts it into the database.
     * The operation is performed asynchronously within the `viewModelScope`.
     *
     * @param userId The unique identifier of the user for whom this record is being added.
     * @param vegetablesServings Number of vegetable servings.
     * @param fruitServings Number of fruit servings.
     * @param grainsServings Number of grain servings.
     * @param proteinServings Number of protein servings.
     * @param dairyServings Number of dairy servings.
     * @param waterAmount Amount of water consumed (e.g., in ml or glasses).
     * @param alcoholAmount Amount of alcohol consumed (e.g., in standard drinks).
     * @param processedFoodAmount Amount or frequency of processed food consumption.
     * @param sugarAmount Amount of added sugar consumed.
     * @param saltAmount Amount of added salt or high-sodium food consumption.
     */
    fun addNewFoodIntake(
        userId: String,
        vegetablesServings: Int,
        fruitServings: Int,
        grainsServings: Int,
        proteinServings: Int,
        dairyServings: Int,
        waterAmount: Int,
        alcoholAmount: Int,
        processedFoodAmount: Int,
        sugarAmount: Int,
        saltAmount: Int
    ) = viewModelScope.launch {
        val newFoodIntake = FoodIntake(
            userId = userId,
            date = System.currentTimeMillis(),
            vegetablesServings = vegetablesServings,
            fruitServings = fruitServings,
            grainsServings = grainsServings,
            proteinServings = proteinServings,
            dairyServings = dairyServings,
            waterAmount = waterAmount,
            alcoholAmount = alcoholAmount,
            processedFoodAmount = processedFoodAmount,
            sugarAmount = sugarAmount,
            saltAmount = saltAmount
        )
        repository.insert(newFoodIntake)
    }
    
    /**
     * Calculates and returns the average number of vegetable servings for a specific user.
     *
     * This is a suspending function, meaning it should be called from a coroutine or another suspending function.
     * It fetches the data from the repository.
     *
     * @param userId The unique identifier of the user.
     * @return The average number of vegetable servings as a [Float], or `null` if no data is available or an error occurs.
     */
    suspend fun getAverageVegetableServings(userId: String): Float? {
        return repository.getAverageVegetableServings(userId)
    }
    
    /**
     * Calculates and returns the average number of fruit servings for a specific user.
     *
     * This is a suspending function, meaning it should be called from a coroutine or another suspending function.
     * It fetches the data from the repository.
     *
     * @param userId The unique identifier of the user.
     * @return The average number of fruit servings as a [Float], or `null` if no data is available or an error occurs.
     */
    suspend fun getAverageFruitServings(userId: String): Float? {
        return repository.getAverageFruitServings(userId)
    }
} 