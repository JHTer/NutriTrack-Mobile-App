package com.fit2081.ter_34857613.NutriTrack.model.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String?
)

/**
 * Data class for nutrition insights returned from the AI
 */
data class NutritionInsight(
    val title: String,
    val description: String,
    val category: String,
    val confidence: Int,
    @SerializedName("patient_count")
    val patientCount: Int,
    val recommendation: String
)

/**
 * Interface for Gemini API interactions
 */
interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
    
    /**
     * Creates a nutrition-focused prompt with system instructions and sends to the API
     * @param apiKey Gemini API key
     * @param query The user's nutrition-related question
     * @return Response containing AI-generated content
     */
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateNutritionAdvice(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
    
    /**
     * Analyzes patient nutrition data to generate insights
     * @param apiKey Gemini API key
     * @param patientData JSON string of patient nutrition data
     * @return Response containing AI-generated insights
     */
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateNutritionInsights(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
} 