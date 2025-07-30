package com.fit2081.ter_34857613.NutriTrack.model.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for interacting with the Gemini API.
 * Provides simplified methods for common AI tasks in the NutriTrack app.
 */
class GeminiHelper {
    private val apiService = RetrofitClient.geminiApiService
    private val apiKey = RetrofitClient.getGeminiApiKey()
    private val gson = Gson()
    
    /**
     * Generates content using the Gemini API with a simple text prompt
     * @param prompt The text prompt to send to the API
     * @return Result containing the generated text or an error
     */
    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = apiService.generateContent(apiKey, request)
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API call failed with code ${response.code()}: ${response.message()}")
                )
            }
            
            val body = response.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }
            
            val generatedText = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            Result.success(generatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate nutrition-focused advice using the system prompt from clinician-plan.md
     * @param query The nutrition-related question to answer
     * @return Result containing the generated advice or an error
     */
    suspend fun generateNutritionAdvice(query: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are NutriAssist, an AI nutrition consultant specializing in analyzing HEIFA scores and providing evidence-based nutrition guidance. You are part of the NutriTrack application for clinicians.
                
                TOPIC RESTRICTIONS:
                - You MUST ONLY answer questions related to nutrition, dietary guidelines, HEIFA scores, and food-related health topics
                - You MUST REFUSE to answer questions about physics, economics, politics, history, entertainment, or any non-nutrition topics
                
                CAPABILITIES:
                - Interpret HEIFA scores and their components (vegetables, fruits, grains, etc.)
                - Explain nutrition concepts using scientific evidence
                - Compare nutrition patterns across demographic groups
                - Suggest specific interventions to improve patient nutrition outcomes
                
                RESPONSE GUIDELINES:
                1. Keep responses focused on HEIFA scoring and nutritional guidance
                2. Use simple, clear language appropriate for healthcare professionals
                3. When uncertain, clearly state the limitations of your knowledge
                4. Base all answers on Australian dietary guidelines and HEIFA methodology
            """.trimIndent()
            
            val fullPrompt = "$systemPrompt\n\nQuestion: $query"
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = fullPrompt))
                    )
                )
            )
            
            val response = apiService.generateNutritionAdvice(apiKey, request)
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API call failed with code ${response.code()}: ${response.message()}")
                )
            }
            
            val body = response.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }
            
            val generatedText = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            Result.success(generatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate nutrition insights based on patient data
     * @param patientData JSON string containing patient nutrition data
     * @return Result containing a list of nutrition insights or an error
     */
    suspend fun generateNutritionInsights(patientData: String): Result<List<NutritionInsight>> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are HealthInsight, an analytical AI system designed to identify meaningful patterns in HEIFA nutrition data within the NutriTrack application.
                
                TASK:
                Analyze the following anonymized patient HEIFA score data to identify 3-5 significant patterns or insights.
                
                DATA:
                $patientData
                
                INSTRUCTIONS:
                1. Identify 3-5 statistically significant patterns in this data
                2. For each pattern, create an insight following this format:
                   - Title: (10 words max)
                   - Description: (100 words max, include statistical evidence)
                   - Category: (select from: Vegetables, Fruits, Grains, Meat/Alternatives, Dairy, Water, Sodium, Fat, Alcohol, Discretionary)
                   - Confidence: (1-100 score with justification)
                   - Patient_Count: (number of patients exhibiting this pattern)
                   - Recommendation: (50 words max, actionable advice)
                
                3. Focus exclusively on nutrition patterns and HEIFA components
                4. Provide confidence scores based on statistical significance
                5. Ensure recommendations are evidence-based and actionable
                
                Format your entire response as a valid JSON array of insight objects.
            """.trimIndent()
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = systemPrompt))
                    )
                )
            )
            
            val response = apiService.generateNutritionInsights(apiKey, request)
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API call failed with code ${response.code()}: ${response.message()}")
                )
            }
            
            val body = response.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }
            
            val jsonResponse = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("No text in response"))
            
            try {
                val insights = gson.fromJson(jsonResponse, Array<NutritionInsight>::class.java).toList()
                Result.success(insights)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to parse insights from API response: ${e.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 