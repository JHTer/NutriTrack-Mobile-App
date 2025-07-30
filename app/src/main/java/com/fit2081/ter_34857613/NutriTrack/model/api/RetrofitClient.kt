package com.fit2081.ter_34857613.NutriTrack.model.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for making API requests.
 */
object RetrofitClient {
    private const val FRUIT_BASE_URL = "https://www.fruityvice.com/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val GEMINI_API_KEY = "Your-API-key"
    
    // Create an OkHttpClient with timeout settings
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Get a Retrofit instance configured for the FruityVice API.
     */
    private val fruitRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FRUIT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Get a Retrofit instance configured for the Google Gemini API
     */
    private val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Create an implementation of the FruitApiService interface.
     */
    val fruitApiService: FruitApiService by lazy {
        fruitRetrofit.create(FruitApiService::class.java)
    }
    
    /**
     * Create an implementation of the GeminiApiService interface.
     */
    val geminiApiService: GeminiApiService by lazy {
        geminiRetrofit.create(GeminiApiService::class.java)
    }
    
    /**
     * Get the Gemini API key
     */
    fun getGeminiApiKey(): String {
        return GEMINI_API_KEY
    }
} 