package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.FoodIntake
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.io.IOException

/**
 * Repository responsible for interacting with the Gemini AI model.
 * This class handles sending prompts to the Gemini API and processing the responses,
 * including parsing JSON data and handling potential errors.
 */
class GeminiRepository {
    /**
     * Companion object for [GeminiRepository].
     * Contains constants used within the repository, such as API keys and error messages.
     */
    companion object {
        private const val TAG = "GeminiRepository"
        private const val API_KEY = "YOUR-API-KEY" // TODO: Secure API Key

        
        // Error messages
        /** Error message for network connection issues. */
        const val ERROR_NETWORK_CONNECTION = "Please check your internet connection and try again."
        /** Error message for unknown or unexpected errors. */
        const val ERROR_UNKNOWN = "An unexpected error occurred. Please try again later."
    }
    
    /** The generative model instance used to interact with the Gemini API. */
    // Create the GenerativeModel with minimal configuration
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = API_KEY
    )
    
    /**
     * Helper function to add appropriate HEIFA scores to a JSONObject based on patient gender.
     *
     * This function populates a `JSONObject` with HEIFA component scores and the total HEIFA score
     * for a given patient, differentiating between male and female scoring criteria.
     *
     * @param patient The [Patient] entity whose scores are to be added.
     * @param componentScores The `JSONObject` to which individual HEIFA component scores will be added.
     * @param patientJson The `JSONObject` to which the total HEIFA score will be added.
     * @param isMale A boolean indicating whether to use male scoring (true) or female scoring (false).
     */
    private fun addHeifaScoresToJson(
        patient: Patient,
        componentScores: JSONObject,
        patientJson: JSONObject,
        isMale: Boolean
    ) {
        if (isMale) {
            // Add all non-null male component scores
            patient.vegetablesHeifaScoreMale?.let { componentScores.put("vegetables", it) }
            patient.fruitHeifaScoreMale?.let { componentScores.put("fruits", it) }
            patient.grainsAndCerealsHeifaScoreMale?.let { componentScores.put("grains", it) }
            patient.meatAndAlternativesHeifaScoreMale?.let { componentScores.put("meat_alternatives", it) }
            patient.dairyAndAlternativesHeifaScoreMale?.let { componentScores.put("dairy", it) }
            patient.waterHeifaScoreMale?.let { componentScores.put("water", it) }
            patient.sodiumHeifaScoreMale?.let { componentScores.put("sodium", it) }
            patient.unsaturatedFatHeifaScoreMale?.let { componentScores.put("unsaturated_fat", it) }
            patient.alcoholHeifaScoreMale?.let { componentScores.put("alcohol", it) }
            patient.discretionaryHeifaScoreMale?.let { componentScores.put("discretionary", it) }
            patient.heifaTotalScoreMale?.let { patientJson.put("heifa_total_score", it) }
        } else {
            // Add all non-null female component scores
            patient.vegetablesHeifaScoreFemale?.let { componentScores.put("vegetables", it) }
            patient.fruitHeifaScoreFemale?.let { componentScores.put("fruits", it) }
            patient.grainsAndCerealsHeifaScoreFemale?.let { componentScores.put("grains", it) }
            patient.meatAndAlternativesHeifaScoreFemale?.let { componentScores.put("meat_alternatives", it) }
            patient.dairyAndAlternativesHeifaScoreFemale?.let { componentScores.put("dairy", it) }
            patient.waterHeifaScoreFemale?.let { componentScores.put("water", it) }
            patient.sodiumHeifaScoreFemale?.let { componentScores.put("sodium", it) }
            patient.unsaturatedFatHeifaScoreFemale?.let { componentScores.put("unsaturated_fat", it) }
            patient.alcoholHeifaScoreFemale?.let { componentScores.put("alcohol", it) }
            patient.discretionaryHeifaScoreFemale?.let { componentScores.put("discretionary", it) }
            patient.heifaTotalScoreFemale?.let { patientJson.put("heifa_total_score", it) }
        }
    }
    
    /**
     * Helper function to add HEIFA scores to a map based on patient gender.
     *
     * This function populates a `MutableMap` with HEIFA component scores for a given patient,
     * differentiating between male and female scoring criteria.
     *
     * @param patient The [Patient] entity whose scores are to be added.
     * @param scoresMap The `MutableMap<String, Double>` to be populated with component scores.
     * The keys are the HEIFA component names (e.g., "Vegetables", "Fruits") and the values are the scores.
     * @param isMale A boolean indicating whether to use male scoring (true) or female scoring (false).
     */
    private fun addHeifaScoresToMap(
        patient: Patient,
        scoresMap: MutableMap<String, Double>,
        isMale: Boolean
    ) {
        if (isMale) {
            scoresMap["Discretionary Foods"] = patient.discretionaryHeifaScoreMale ?: 0.0
            scoresMap["Vegetables"] = patient.vegetablesHeifaScoreMale ?: 0.0
            scoresMap["Fruits"] = patient.fruitHeifaScoreMale ?: 0.0
            scoresMap["Grains"] = patient.grainsAndCerealsHeifaScoreMale ?: 0.0
            scoresMap["Protein"] = patient.meatAndAlternativesHeifaScoreMale ?: 0.0
            scoresMap["Dairy"] = patient.dairyAndAlternativesHeifaScoreMale ?: 0.0
            scoresMap["Water"] = patient.waterHeifaScoreMale ?: 0.0
            scoresMap["Sodium"] = patient.sodiumHeifaScoreMale ?: 0.0
            scoresMap["Sugar"] = patient.sugarHeifaScoreMale ?: 0.0
            scoresMap["Saturated Fat"] = patient.saturatedFatHeifaScoreMale ?: 0.0
            scoresMap["Unsaturated Fat"] = patient.unsaturatedFatHeifaScoreMale ?: 0.0
            scoresMap["Alcohol"] = patient.alcoholHeifaScoreMale ?: 0.0
        } else {
            scoresMap["Discretionary Foods"] = patient.discretionaryHeifaScoreFemale ?: 0.0
            scoresMap["Vegetables"] = patient.vegetablesHeifaScoreFemale ?: 0.0
            scoresMap["Fruits"] = patient.fruitHeifaScoreFemale ?: 0.0
            scoresMap["Grains"] = patient.grainsAndCerealsHeifaScoreFemale ?: 0.0
            scoresMap["Protein"] = patient.meatAndAlternativesHeifaScoreFemale ?: 0.0
            scoresMap["Dairy"] = patient.dairyAndAlternativesHeifaScoreFemale ?: 0.0
            scoresMap["Water"] = patient.waterHeifaScoreFemale ?: 0.0
            scoresMap["Sodium"] = patient.sodiumHeifaScoreFemale ?: 0.0
            scoresMap["Sugar"] = patient.sugarHeifaScoreFemale ?: 0.0
            scoresMap["Saturated Fat"] = patient.saturatedFatHeifaScoreFemale ?: 0.0
            scoresMap["Unsaturated Fat"] = patient.unsaturatedFatHeifaScoreFemale ?: 0.0
            scoresMap["Alcohol"] = patient.alcoholHeifaScoreFemale ?: 0.0
        }
    }
    
    /**
     * Generates content using the Gemini AI model based on the provided prompt.
     *
     * This function sends a prompt to the configured `generativeModel` and returns the
     * model's text response. It includes error handling for network issues and other
     * exceptions that may occur during the API call.
     *
     * @param prompt The input string prompt to send to the Gemini AI model.
     * @return A string containing the AI-generated content, or an error message if the
     *         generation fails or an error occurs.
     */
    suspend fun generateContent(prompt: String): String {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: "No response generated"
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content: ${e.message}", e)
            return when (e) {
                is UnknownHostException -> ERROR_NETWORK_CONNECTION
                is IOException -> {
                    if (e.message?.contains("Unable to resolve host") == true || 
                        e.message?.contains("Failed to connect") == true ||
                        e.message?.contains("No address associated") == true) {
                        ERROR_NETWORK_CONNECTION
                    } else {
                        "Error: ${e.message}"
                    }
                }
                else -> "Error: ${e.message}"
            }
        }
    }
    
    /**
     * Extracts a valid JSON string from a potentially malformed response string.
     *
     * This function attempts to find a JSON array structure (e.g., `[{...}]` or `[...]`)
     * within the input `response` string. It logs the process and returns the extracted
     * JSON string if successful, or an error message if no valid JSON can be found.
     *
     * @param response The string response from which to extract JSON. This may contain
     *                 other text or be slightly malformed.
     * @return A string containing the extracted JSON array, or an error message indicating
     *         failure to parse, along with a snippet of the raw response.
     */
    private fun extractJsonFromResponse(response: String): String {
        // Log the entire response for debugging
        Log.d(TAG, "Attempting to extract JSON from: $response")
        
        // First try to find array with more typical object syntax
        val jsonPattern = Regex("\\[\\s*\\{.*\\}\\s*\\]", RegexOption.DOT_MATCHES_ALL)
        val match = jsonPattern.find(response)
        
        if (match != null) {
            val extractedJson = match.value
            Log.d(TAG, "Successfully extracted JSON: ${extractedJson.take(100)}...")
            return extractedJson
        }
        
        // If that fails, try to extract any JSON array
        val arrayPattern = Regex("\\[.*\\]", RegexOption.DOT_MATCHES_ALL)
        val arrayMatch = arrayPattern.find(response)
        
        if (arrayMatch != null) {
            val extractedArray = arrayMatch.value
            Log.d(TAG, "Found JSON array: ${extractedArray.take(100)}...")
            return extractedArray
        }
        
        // If all extraction fails, log the failure and return an error message
        Log.e(TAG, "Failed to extract JSON from response: ${response.take(200)}...")
        
        return "Error: Failed to parse AI response. Raw response: ${response.take(100)}..."
    }
    
    /**
     * Performs AI-driven analysis of patient data to discover patterns and generate insights.
     *
     * This function prepares anonymized patient data, constructs a detailed prompt for the
     * Gemini AI model, and then calls
     * the AI to analyze the data. The goal is to identify meaningful nutritional patterns
     * and generate insights across several key areas (Vegetables, Fruits, Protein, Water,
     * Discretionary Foods).
     *
     * The function expects the AI to return a JSON array of insights, each containing a title,
     * description, category, patient count, and recommendation.
     *
     * @param patients A list of [Patient] objects containing HEIFA scores and other relevant data.
     * @param maleCount The total count of male patients in the dataset.
     * @param femaleCount The total count of female patients in the dataset.
     * @param totalCount The total number of patients in the dataset.
     * @return A string containing a JSON array of AI-generated insights if successful.
     *         Returns an error message if no patient data is available, if data preparation fails,
     *         or if the AI interaction results in an error.
     */
    suspend fun performAiDataAnalysis(
        patients: List<Patient>,
        maleCount: Int,
        femaleCount: Int,
        totalCount: Int
    ): String {
        if (patients.isEmpty()) {
            return "Error: No patient data available for analysis"
        }
        
        try {
            // Create anonymized dataset from patients
            val anonymizedData = prepareAnonymizedPatientData(patients)
            
            // Check if anonymization failed
            if (anonymizedData.isBlank() || anonymizedData == "[]") {
                return "Error: Failed to prepare patient data for analysis"
            }
            
            // Construct the analytical prompt following clinician-plan.md
            val systemPrompt = """
                You are HealthInsight, an analytical AI system designed to identify meaningful patterns in HEIFA nutrition data within the NutriTrack application.
                
                TOPIC RESTRICTIONS:
                - You MUST ONLY analyze and report on nutrition-related data and patterns
                - You MUST NOT include any insights or analysis about economics, physics, politics, or any non-nutrition topics
                - Any data patterns that appear unrelated to nutrition should be excluded from your insights
                - Do not attempt to draw connections between nutrition data and non-nutrition fields like economics or politics
                
                ANALYSIS PARAMETERS:
                - Focus on identifying statistically meaningful patterns in nutrition data
                - Consider correlations between different HEIFA components
                - Analyze demographic differences in nutrition patterns
                - Detect anomalies or concerning patterns in aggregate data
                
                REQUIRED OUTPUT FORMAT:
                1. Title: A concise description of the insight (10 words maximum)
                2. Description: Detailed explanation with supporting evidence (100 words maximum)
                3. Category: One of [Vegetables, Fruits, Protein, Water, Discretionary]
                4. Patient_Count: Number of patients the insight is based on
                5. Recommendation: Actionable suggestion for clinicians (50 words maximum)
                
                ANALYSIS CONSTRAINTS:
                - Only report patterns with statistical significance
                - Clearly distinguish correlation from causation
                - Indicate sample size for all observations
                - Specify which HEIFA components were analyzed
                - Note any limitations in the available data
                
                DEMOGRAPHIC FACTORS TO CONSIDER:
                - Gender differences in nutrition patterns
                - Age-related nutrition patterns (if available)
                - Regional variations (if available)
                - Changes over time (if longitudinal data available)
                
                IMPORTANT: You MUST format your ENTIRE response as a valid JSON array. Start your response with '[' and end with ']'. Each insight must be a JSON object.
                
                FOCUS ON THESE 5 KEY NUTRITIONAL AREAS (YOU MUST PROVIDE ONE INSIGHT FOR EACH):
                1. Vegetables - Essential for micronutrients and fiber, includes all vegetable intake
                2. Fruits - Key source of vitamins and antioxidants, includes all fruit consumption
                3. Protein - Critical macronutrient for body function, includes meat, fish, eggs, legumes, and plant proteins
                4. Water - Fundamental for hydration and metabolism, includes water intake levels
                5. Discretionary Foods - High impact on overall diet quality, includes processed foods, snacks, desserts, and other non-essential foods
            """.trimIndent()
            
            // Add context about HEIFA scoring system
            val contextPrompt = """
                The HEIFA (Healthy Eating Index for Australian Adults) scoring system evaluates nutritional quality across several components:
                
                - Component scores range from 0-5 or 0-10, with higher being better
                - Discretionary foods score: lower consumption is better (reverse scoring)
                - Vegetables, Fruits, Grains, Protein, Dairy: higher consumption is better
                - Water intake: higher consumption is better
                - Sodium: lower consumption is better (reverse scoring)
                - Unsaturated fat: higher consumption of healthy fats is better
                - Alcohol: lower consumption is better (reverse scoring)
                
                Total HEIFA scores range approximately from 0-100, with higher scores indicating better overall diet quality.
            """.trimIndent()
            
            // Summary statistics to help the AI understand the dataset
            val demographicSummary = """
                PATIENT POPULATION:
                - Total patients: $totalCount
                - Male patients: $maleCount
                - Female patients: $femaleCount
            """.trimIndent()
            
            // Combine all parts of the prompt
            val fullPrompt = """
                $systemPrompt
                
                $contextPrompt
                
                $demographicSummary
                
                ANONYMIZED PATIENT DATA:
                $anonymizedData
                
                TASK:
                Analyze this data to identify 5 statistically significant patterns, correlations, or anomalies specifically related to the 5 key nutritional areas mentioned above.
                For each pattern, create a structured insight following the required format.
                
                YOUR RESPONSE MUST BE A VALID JSON ARRAY OF OBJECTS. The entire response should start with '[' and end with ']'. Each insight should be a JSON object with the required fields.
                
                IMPORTANT: You MUST return exactly 5 insights, one for each of the key nutritional areas: Vegetables, Fruits, Protein, Water, and Discretionary Foods.
            """.trimIndent()
            
            Log.d(TAG, "Sending AI analysis prompt with ${patients.size} patients")
            
            // Send to AI for analysis
            val aiResponse = generateContent(fullPrompt)
            
            // Check for error response
            if (aiResponse.contains("Error:") || 
                aiResponse.contains(ERROR_NETWORK_CONNECTION) || 
                aiResponse.contains(ERROR_UNKNOWN)) {
                Log.e(TAG, "API error: $aiResponse")
                return aiResponse  // Return the error message directly
            }
            
            // Try to extract valid JSON
            try {
                // Basic validation - try to parse as JSON
                val jsonArray = JSONArray(aiResponse)
                Log.d(TAG, "Successfully parsed AI response as JSON with ${jsonArray.length()} insights")
                return aiResponse
            } catch (e: Exception) {
                // If parsing fails, try to extract JSON portion
                Log.e(TAG, "Error parsing AI response as JSON: ${e.message}", e)
                val extractedJson = extractJsonFromResponse(aiResponse)
                
                return extractedJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in AI data analysis: ${e.message}", e)
            return "Error: Failed to analyze patient data: ${e.message}"
        }
    }
    
    /**
     * Prepares an anonymized dataset from a list of [Patient] objects for AI analysis.
     *
     * This function takes a list of patients, samples a subset (at most 100 patients)
     * to avoid overloading the API, and then creates a JSON array of anonymized patient data.
     * For each patient, it includes an anonymized ID, non-identifiable demographic data (gender),
     * HEIFA component scores (gender-specific), and additional non-PII nutrition information
     * such as water intake, sodium levels, alcohol consumption, and food variety scores if available.
     *
     * @param patients The list of [Patient] entities to be anonymized and formatted.
     * @return A pretty-printed JSON string representing the anonymized dataset.
     *         Each object in the JSON array corresponds to a patient and contains their
     *         anonymized data and HEIFA scores.
     */
    private fun prepareAnonymizedPatientData(patients: List<Patient>): String {
        // Sample at most 100 patients to avoid overloading the API
        val sampleSize = minOf(100, patients.size)
        val sampledPatients = if (patients.size <= sampleSize) {
            patients
        } else {
            patients.shuffled().take(sampleSize)
        }
        
        // Create anonymized JSON dataset
        val datasetJson = JSONArray()
        
        sampledPatients.forEachIndexed { index, patient ->
            val patientJson = JSONObject()
            
            // Anonymized ID
            patientJson.put("anonymous_id", "patient_$index")
            
            // Demographic data (non-identifiable)
            patientJson.put("gender", patient.sex)
            
            // HEIFA component scores based on gender
            val componentScores = JSONObject()
            
            // Use the appropriate scores based on gender
            addHeifaScoresToJson(patient, componentScores, patientJson, patient.sex.equals("Male", ignoreCase = true))
            
            // Add additional nutrition info (non-PII)
            patient.waterTotalML?.let { patientJson.put("water_ml", it) }
            patient.sodiumMgMilligrams?.let { patientJson.put("sodium_mg", it) }
            patient.alcoholStandardDrinks?.let { patientJson.put("alcohol_drinks", it) }
            
            // Add component variation data if available
            patient.vegetablesVariationsScore?.let { patientJson.put("vegetable_variety", it) }
            patient.fruitVariationsScore?.let { patientJson.put("fruit_variety", it) }
            
            // Add the component scores to the patient
            patientJson.put("components", componentScores)
            
            // Add to dataset
            datasetJson.put(patientJson)
        }
        
        return datasetJson.toString(2) // Pretty-print with 2-space indentation
    }
    
    /**
     * Generates a personalized nutrition tip based on the patient's HEIFA scores and food intake data.
     *
     * This function constructs a focused set of data points for a given patient, including basic info,
     * their lowest HEIFA scoring areas (normalized to a 0-5 scale), their overall HEIFA score, and
     * recent food intake details if available. It then sends this focused data to the Gemini AI model
     * with a prompt designed to elicit a concise, actionable, and personalized nutrition tip.
     *
     * The tip should be tailored to the patient's specific areas for improvement and offer
     * practical advice.
     *
     * @param patient The [Patient] entity containing HEIFA scores and demographic information.
     * @param foodIntake An optional [FoodIntake] entity representing the patient's recent food consumption.
     *                   This can be null if no recent intake data is available.
     * @return A string containing a personalized nutrition tip generated by the AI.
     *         If an error occurs during generation (e.g., network issue, AI error), an error message
     *         string is returned instead.
     */
    suspend fun generateNutritionTip(patient: Patient, foodIntake: FoodIntake?): String {
        try {
            // Extract only the top 5 most important data points for focused AI advice
            val focusedData = mutableMapOf<String, Any>()
            
            // 1. Basic user info
            focusedData["Gender"] = patient.sex
            
            // 2. Extract and calculate HEIFA scores based on gender
            val isMale = patient.sex.equals("Male", ignoreCase = true)
            
            // Create a map of nutrition areas to their scores
            val nutritionScores = mutableMapOf<String, Double>()
            
            // Add scores based on gender
            addHeifaScoresToMap(patient, nutritionScores, isMale)
            
            // 3. Find lowest scoring areas (normalize to 0-5 scale)
            val normalizedScores = nutritionScores.mapValues { (_, value) -> 
                (value / 10.0) * 5.0 // Convert from 0-10 scale to 0-5 scale
            }
            
            // Get the lowest 3 scores
            val lowestScores = normalizedScores.entries
                .sortedBy { it.value }
                .take(3)
            
            // Add lowest scores to focused data
            lowestScores.forEachIndexed { index, entry ->
                focusedData["LowArea${index+1}"] = "${entry.key}: ${String.format("%.1f", entry.value)}/5"
            }
            
            // 4. Add overall score
            val overallScore = normalizedScores.values.average()
            focusedData["OverallScore"] = String.format("%.1f", overallScore)
            
            // 5. Add food intake data if available
            if (foodIntake != null) {
                focusedData["RecentVegetableServings"] = foodIntake.vegetablesServings
                focusedData["RecentFruitServings"] = foodIntake.fruitServings
                focusedData["RecentWaterAmount"] = foodIntake.waterAmount
                focusedData["RecentAlcoholAmount"] = foodIntake.alcoholAmount
            }
            
            // Create a concise system prompt
            val systemPrompt = """
                You are NutriCoach, a nutrition expert providing motivational tips to help improve diet.
                
                GUIDELINES:
                - Keep responses under 2-3 sentences
                - Be encouraging and positive
                - Provide specific, actionable advice based on their lowest scoring areas
                - End with a single encouraging emoji
                - DO NOT mention HEIFA scores or calculations directly
                - DO NOT make up information not provided in the data
                
                FOCUS ON THE USER'S LOWEST SCORING AREAS.
            """.trimIndent()
            
            // Create focused user prompt
            val userPrompt = """
                Based on this nutrition data, provide ONE specific tip to improve diet:
                
                ${focusedData.entries.joinToString("\n") { "${it.key}: ${it.value}" }}
                
                Give a friendly, practical suggestion addressing their lowest scoring area.
            """.trimIndent()
            
            val finalPrompt = "$systemPrompt\n\n$userPrompt"
            
            return generateContent(finalPrompt)
        } catch (e: Exception) {
            return when (e) {
                is UnknownHostException -> ERROR_NETWORK_CONNECTION
                is IOException -> {
                    if (e.message?.contains("Unable to resolve host") == true || 
                        e.message?.contains("Failed to connect") == true ||
                        e.message?.contains("No address associated") == true) {
                        ERROR_NETWORK_CONNECTION
                    } else {
                        "Error generating nutrition tip: ${e.message}"
                    }
                }
                else -> "Error generating nutrition tip: ${e.message}"
            }
        }
    }
} 