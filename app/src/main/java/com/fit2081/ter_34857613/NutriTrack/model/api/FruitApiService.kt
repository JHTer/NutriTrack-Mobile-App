package com.fit2081.ter_34857613.NutriTrack.model.api

import com.fit2081.ter_34857613.NutriTrack.model.data.Fruit
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for the FruityVice API.
 * Defines endpoint methods for retrieving fruit information.
 */
interface FruitApiService {
    /**
     * Get information about a specific fruit by name.
     * @param name The name of the fruit to look up
     * @return Response containing fruit information
     */
    @GET("api/fruit/{name}")
    suspend fun getFruitByName(@Path("name") name: String): Response<Fruit>
    
    /**
     * Get information about all available fruits.
     * @return Response containing a list of all fruits
     */
    @GET("api/fruit/all")
    suspend fun getAllFruits(): Response<List<Fruit>>
} 