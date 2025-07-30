package com.fit2081.ter_34857613.NutriTrack.model.repository

import com.fit2081.ter_34857613.NutriTrack.model.api.RetrofitClient
import com.fit2081.ter_34857613.NutriTrack.model.data.Fruit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching fruit data directly from the FruityVice API.
 *
 * This class provides methods to search for a specific fruit by its name or retrieve a list
 * of all available fruits. It uses [RetrofitClient.fruitApiService] to interact with the API
 * and returns results wrapped in a [Result] object to encapsulate success or failure states.
 * All network operations are performed on an IO-optimized dispatcher.
 */
class FruitRepository {
    /**
     * Searches for a specific fruit by its name using the FruityVice API.
     *
     * The search is performed on an IO-optimized dispatcher.
     *
     * @param name The name of the fruit to search for (e.g., "Apple", "Banana").
     * @return A [Result<Fruit>] which is [Result.Success] containing the [Fruit] data if found,
     *         or [Result.Failure] containing an exception if the fruit is not found, an API error occurs
     *         (e.g., non-2xx HTTP status), or a network error happens.
     */
    suspend fun searchFruit(name: String): Result<Fruit> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.fruitApiService.getFruitByName(name)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to find fruit: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
    
    /**
     * Retrieves a list of all available fruits from the FruityVice API.
     *
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @return A [Result<List<Fruit>>] which is [Result.Success] containing a list of all [Fruit] objects
     *         if the API call is successful. It returns [Result.Failure] containing an exception
     *         if an API error occurs (e.g., non-2xx HTTP status) or a network error happens.
     */
    suspend fun getAllFruits(): Result<List<Fruit>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.fruitApiService.getAllFruits()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get fruits: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
} 