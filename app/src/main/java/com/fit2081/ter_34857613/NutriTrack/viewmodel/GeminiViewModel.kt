package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.repository.GeminiRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.PatientRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for interacting with the Gemini AI model and managing AI-related data for the UI.
 *
 * This ViewModel facilitates two main AI functionalities:
 * 1. Generating patient insights: It takes patient data from [PatientRepository],
 *    processes it, and sends it to the [GeminiRepository] to obtain AI-driven analysis
 *    and insights about the patient cohort.
 * 2. Handling chat interactions: It receives user messages, constructs appropriate prompts
 *    (including system instructions for language and topic restrictions), sends them to the
 *    [GeminiRepository] for a chat completion, and processes the response to extract
 *    the main content and any relevant category tags. It also generates suggested follow-up questions.
 *
 * It manages UI state through properties like `isLoading` (to indicate ongoing AI operations)
 * and `errorMessage` (to display errors).
 * All AI operations are performed asynchronously using Kotlin coroutines launched in the `viewModelScope`.
 */
class GeminiViewModel : ViewModel() {
    companion object {
        private const val TAG = "GeminiViewModel"
    }

    // Repository for Gemini API interactions
    private val geminiRepository = GeminiRepository()

    // State variables
    /** Indicates whether an AI-related operation (insight generation or chat response) is currently in progress. */
    var isLoading by mutableStateOf(false)
        private set

    /** Stores an error message if an AI operation fails, or `null` if there is no error or no operation has failed. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Generates AI-powered insights based on aggregated patient data.
     *
     * This function orchestrates the process of fetching patient statistics (total, male, female counts)
     * and detailed data for all patients from the [patientRepository]. It performs several data integrity checks,
     * such as ensuring patients exist, have nutrition data, and that key nutritional data points are present.
     * If data is valid, it calls the [GeminiRepository.performAiDataAnalysis] method to get insights.
     *
     * Updates [isLoading] and [errorMessage] states accordingly.
     * Invokes [onSuccess] with the AI-generated insight string (expected to be JSON) or [onError] with an error message.
     *
     * @param context The application context.
     * @param patientRepository Repository for accessing patient data.
     * @param onSuccess Callback function invoked upon successful insight generation, providing the AI response string.
     * @param onError Callback function invoked if an error occurs, providing an error message string.
     */
    fun generatePatientInsights(
        context: Context,
        patientRepository: PatientRepository,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "===== STARTING INSIGHT GENERATION =====")
                
                // Get patient statistics for context
                val totalPatients = patientRepository.getTotalPatientCount()
                val malePatients = patientRepository.getMalePatientCount()
                val femalePatients = patientRepository.getFemalePatientCount()
                
                // Get all patients for AI analysis
                val allPatients = patientRepository.getAllPatientsSync()
                
                Log.d(TAG, "Retrieved ${allPatients.size} patients for analysis (Male: $malePatients, Female: $femalePatients)")
                
                if (allPatients.isEmpty()) {
                    Log.e(TAG, "DEBUG: No patients found in database for analysis")
                    onError("No patient data available for analysis. Please check database configuration.")
                    isLoading = false
                    return@launch
                }
                
                // Check if patients have nutrition data
                val patientsWithNutritionData = allPatients.count { patient ->
                    val hasMaleData = patient.sex.equals("Male", ignoreCase = true) && patient.heifaTotalScoreMale != null
                    val hasFemaleData = patient.sex.equals("Female", ignoreCase = true) && patient.heifaTotalScoreFemale != null
                    hasMaleData || hasFemaleData
                }
                
                Log.d(TAG, "Patients with nutrition data: $patientsWithNutritionData out of ${allPatients.size}")
                
                if (patientsWithNutritionData == 0) {
                    Log.e(TAG, "DEBUG: No patients have nutrition data")
                    onError("Database issue: Patients exist but have no nutrition data. Check CSV format.")
                    isLoading = false
                    return@launch
                }
                
                // Additional check for the specific data areas we need
                val requiredDataPoints = listOf(
                    "Vegetables" to listOf("vegetablesHeifaScoreMale", "vegetablesHeifaScoreFemale"),
                    "Fruits" to listOf("fruitHeifaScoreMale", "fruitHeifaScoreFemale"),
                    "Protein" to listOf("meatAndAlternativesHeifaScoreMale", "meatAndAlternativesHeifaScoreFemale"),
                    "Water" to listOf("waterHeifaScoreMale", "waterHeifaScoreFemale"),
                    "Discretionary" to listOf("discretionaryHeifaScoreMale", "discretionaryHeifaScoreFemale")
                )
                
                // Check each key nutritional area
                var missingDataAreas = mutableListOf<String>()
                
                for ((area, fields) in requiredDataPoints) {
                    val patientsWithThisData = allPatients.count { patient ->
                        // Use reflection to check fields
                        val hasData = fields.any { field ->
                            try {
                                val value = Patient::class.java.getDeclaredField(field).get(patient)
                                value != null
                            } catch (e: Exception) {
                                false
                            }
                        }
                        hasData
                    }
                    
                    Log.d(TAG, "Patients with $area data: $patientsWithThisData")
                    
                    if (patientsWithThisData == 0) {
                        Log.e(TAG, "DEBUG: No patients have $area nutrition data")
                        missingDataAreas.add(area)
                    }
                }
                
                if (missingDataAreas.isNotEmpty()) {
                    onError("Database issue: Missing nutritional data for: ${missingDataAreas.joinToString(", ")}. Check CSV format.")
                    isLoading = false
                    return@launch
                }
                
                Log.d(TAG, "All nutritional area checks passed, proceeding with API call")
                
                try {
                    // Use repository to perform AI data analysis
                    Log.d(TAG, "Calling Gemini API for analysis...")
                    val aiResponse = geminiRepository.performAiDataAnalysis(
                        patients = allPatients,
                        maleCount = malePatients,
                        femaleCount = femalePatients,
                        totalCount = totalPatients
                    )
                    
                    Log.d(TAG, "===== API RESPONSE RECEIVED =====")
                    Log.d(TAG, "Response length: ${aiResponse.length} characters")
                    Log.d(TAG, "First 200 chars: ${aiResponse.take(200)}")
                    
                    // Check for errors
                    if (aiResponse.contains("Error:")) {
                        Log.e(TAG, "Error in AI response: $aiResponse")
                        onError(aiResponse.replace("Error: ", ""))
                        isLoading = false
                        return@launch
                    }
                    
                    // Return the JSON response
                    onSuccess(aiResponse)
                } catch (e: Exception) {
                    Log.e(TAG, "API call failed: ${e.message}", e)
                    onError("Failed to retrieve insights from AI service: ${e.message ?: "Unknown API error"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating insights: ${e.message}", e)
                Log.e(TAG, "Stack trace:", e)
                onError("Error analyzing data: ${e.message ?: "Unknown error"}")
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Generates a chat response from the Gemini AI model based on a user's message and preferred language.
     *
     * It constructs a detailed system prompt that instructs the AI (NutriAssist) on its persona,
     * language requirements (must respond in the specified `language`), topic restrictions (nutrition-only),
     * capabilities, limitations, and response format (including category tags).
     * The user's message is appended to this prompt.
     *
     * After receiving the raw AI response from [GeminiRepository.generateContent], it extracts category tags
     * (e.g., #vegetables) and cleans the main response body. It then calls [generateSuggestedQuestions]
     * to get follow-up questions.
     *
     * Updates [isLoading] and [errorMessage] states.
     * Invokes [onSuccess] with the cleaned AI response and a list of suggested questions, or [onError] with an error message.
     *
     * @param userMessage The user's question or message to the AI.
     * @param language The ISO language code (e.g., "en", "ja", "zh", "ms", "fr") for the desired response language. Defaults to "en".
     * @param onSuccess Callback function invoked upon successful response generation, providing the cleaned AI message
     *                  and a list of suggested follow-up questions.
     * @param onError Callback function invoked if an error occurs, providing an error message string.
     */
    fun generateChatResponse(
        userMessage: String,
        language: String = "en",
        onSuccess: (String, List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userMessage.isBlank()) {
            onError("Please enter a message")
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                // Create a system prompt that restricts the AI to nutrition topics
                val systemPrompt = """
                    You are NutriAssist, an AI nutrition consultant specializing in analyzing HEIFA (Healthy Eating Index for Australian Adults) scores and providing evidence-based nutrition guidance. You are part of the NutriTrack application for clinicians.
                    
                    LANGUAGE INSTRUCTIONS:
                    - You MUST respond in the language with code: "$language"
                    - If the language is "en", respond in English
                    - If the language is "ja", respond in Japanese
                    - If the language is "zh", respond in Chinese
                    - If the language is "ms", respond in Malay
                    - If the language is "fr", respond in French
                    - For other language codes, try to respond in that language, falling back to English if necessary
                    
                    TOPIC RESTRICTIONS:
                    - You MUST ONLY answer questions related to nutrition, dietary guidelines, HEIFA scores, and food-related health topics
                    - You MUST REFUSE to answer questions about physics, economics, politics, history, entertainment, or any non-nutrition topics
                    - Health care, Physical activities, etc.(nutrition questions) can be answered
                    - When asked about non-nutrition topics, respond with: "I'm specialized in nutrition guidance only. I cannot provide information about [topic]. Would you like to know something about HEIFA scores or dietary recommendations instead?"
                    
                    CAPABILITIES:
                    - Interpret HEIFA scores and their components (vegetables, fruits, grains, etc.)
                    - Explain nutrition concepts using scientific evidence
                    - Compare nutrition patterns across demographic groups
                    - Suggest specific interventions to improve patient nutrition outcomes
                    
                    LIMITATIONS:
                    - You cannot diagnose medical conditions or prescribe medications
                    - You cannot access or reference individual patient data or identifiable information
                    - You should cite general statistics based on aggregated, anonymized data only
                    
                    FORMAT:
                    - Keep responses concise (2-3 paragraphs maximum)
                    - Use professional but accessible language suitable for healthcare professionals
                    - Add relevant category tags after your response (e.g., #vegetables, #gender_differences, #water_intake)
                    
                    USER QUESTION: $userMessage
                """.trimIndent()
                
                // Get AI response
                val aiResponse = geminiRepository.generateContent(systemPrompt)
                
                // Check for errors
                if (aiResponse.contains("Error: ")) {
                    Log.e(TAG, "Error in AI response: $aiResponse")
                    onError(aiResponse.replace("Error: ", ""))
                    isLoading = false
                    return@launch
                }
                
                // Extract categories from the response
                val categories = extractCategories(aiResponse)
                val cleanedResponse = removeCategories(aiResponse)
                
                // Generate suggested follow-up questions
                generateSuggestedQuestions(userMessage, cleanedResponse, categories, language) { suggestions ->
                    onSuccess(cleanedResponse, suggestions)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating chat response: ${e.message}", e)
                onError(e.message ?: "Unknown error generating response")
                isLoading = false
            }
        }
    }
    
    /**
     * Extracts category tags (hashtags) from an AI-generated response string.
     *
     * It first looks for explicit hashtags (e.g., `#vegetables`, `#gender_differences`).
     * If no hashtags are found, it falls back to searching for predefined keywords related to nutrition
     * (e.g., "vegetable", "fruit", "gender") within the response text to infer potential categories.
     *
     * @param response The AI-generated response string from which to extract categories.
     * @return A list of lowercase category strings. Returns an empty list if no categories are found.
     */
    private fun extractCategories(response: String): List<String> {
        // Look for hashtags in the response
        val hashtagPattern = Regex("#([a-zA-Z_]+)")
        return hashtagPattern.findAll(response)
            .map { it.groupValues[1].lowercase() }
            .toList()
            .ifEmpty {
                // If no hashtags are found, extract potential categories based on content
                val keywords = listOf("vegetable", "fruit", "grain", "protein", "dairy", 
                    "water", "sodium", "fat", "alcohol", "sugar", "male", "female", "gender")
                keywords.filter { response.contains(it, ignoreCase = true) }
            }
    }
    
    /**
     * Removes category tags (hashtags) from an AI-generated response string.
     *
     * This method uses a regular expression to find and remove all occurrences of patterns like `#sometag`.
     * The resulting string is then trimmed of any leading or trailing whitespace.
     *
     * @param response The AI-generated response string containing hashtags.
     * @return The response string with all hashtags removed.
     */
    private fun removeCategories(response: String): String {
        return response.replace(Regex("#[a-zA-Z_]+"), "").trim()
    }
    
    /**
     * Generates a list of suggested follow-up questions based on the current user question and AI response.
     *
     * This function sends a prompt to the Gemini model, providing the context of the current conversation turn
     * (user question and AI response) and relevant categories. It asks the AI to generate 3 concise, relevant
     * follow-up questions in the specified language, formatted as a numbered list.
     *
     * If the AI successfully returns questions in the expected format, they are parsed and passed to the [onComplete] callback.
     * If parsing fails or an error occurs during generation, a list of generic fallback questions is provided.
     * Sets [isLoading] to `false` upon completion or error.
     *
     * @param userQuestion The original question asked by the user.
     * @param aiResponse The AI's response to the user's question.
     * @param categories A list of categories relevant to the current conversation turn.
     * @param language The ISO language code for the desired language of the suggested questions. Defaults to "en".
     * @param onComplete Callback function invoked with a list of suggested question strings.
     */
    private fun generateSuggestedQuestions(
        userQuestion: String, 
        aiResponse: String,
        categories: List<String>,
        language: String = "en",
        onComplete: (List<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val prompt = """
                    Based on this nutrition conversation, generate 3 relevant follow-up questions the clinician might want to ask next:
                    
                    USER: $userQuestion
                    AI: $aiResponse
                    
                    LANGUAGE INSTRUCTIONS:
                    - You MUST generate questions in the language with code: "$language"
                    - If the language is "en", generate in English
                    - If the language is "ja", generate in Japanese
                    - If the language is "zh", generate in Chinese
                    - If the language is "ms", generate in Malay
                    - If the language is "fr", generate in French
                    - For other language codes, try to generate in that language, falling back to English if necessary
                    
                    The questions should:
                    1. Be directly related to the topics discussed
                    2. Focus on ${categories.joinToString(", ")} if relevant
                    3. Be concise (10 words or less each)
                    4. Not repeat the original question
                    5. Be formatted as a numbered list (1. 2. 3.)
                    6. Use professional clinical language from a clinician perspective
                    7. NEVER use personal pronouns (I, you, we, they)
                    8. Phrase questions in third-person or passive voice (e.g., "How does HEIFA score affect patient outcomes?" or "What factors influence vegetable consumption in female patients?")
                    9. Focus on the medical/nutritional topic, not the person asking or being asked
                """.trimIndent()
                
                val suggestions = geminiRepository.generateContent(prompt)
                
                // Parse the numbered list
                val questionPattern = Regex("\\d+\\.\\s*(.+)")
                var questions = questionPattern.findAll(suggestions)
                    .map { it.groupValues[1].trim() }
                    .filter { it.isNotEmpty() }
                    .toList()
                
                // Fallback if parsing fails
                if (questions.isEmpty()) {
                    questions = listOf(
                        "How do these nutritional aspects impact patient outcomes?",
                        "What are the recommended daily values for this category?",
                        "How do these metrics compare across patient demographics?"
                    )
                }
                
                onComplete(questions)
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error generating suggested questions: ${e.message}", e)
                // Use fallback questions
                onComplete(listOf(
                    "How do these nutritional aspects impact patient outcomes?",
                    "What are the recommended daily values for this category?",
                    "How do these metrics compare across patient demographics?"
                ))
                isLoading = false
            }
        }
    }

    /**
     * A wrapper function that directly calls the [GeminiRepository.generateContent] method.
     *
     * This suspending function allows other parts of the application (or other ViewModels if appropriate)
     * to send a raw prompt string to the Gemini model and receive its generated content.
     * It does not handle loading states or error messages; that responsibility lies with the caller.
     *
     * @param prompt The prompt string to send to the Gemini model.
     * @return The AI-generated content string.
     */
    suspend fun generateContent(prompt: String): String {
        return geminiRepository.generateContent(prompt)
    }

    /**
     * Generates a textual description and actionable recommendations for a specific nutritional category
     * using the Gemini AI model, based on pre-calculated findings and patient context.
     *
     * This suspending function constructs a detailed system prompt for the AI. The prompt includes:
     * - The nutritional category name (e.g., "Vegetables").
     * - A string of `calculatedFindings` (e.g., statistics about intake for that category).
     * - `patientContext` (e.g., number of male/female patients in the dataset).
     * - Instructions to respond in a specific `language`.
     * - A strict requirement for the output to be a JSON object with "description" (summary insight) and
     *   "recommendations" (array of actionable advice) keys.
     *
     * It then calls [GeminiRepository.generateContent] to get the AI's response.
     * Loading and error states are expected to be managed by the calling ViewModel (e.g., [ClinicianViewModel]).
     *
     * @param categoryName The name of the nutritional category being analyzed.
     * @param calculatedFindings A string summarizing pre-calculated statistics for this category.
     * @param patientContext A string providing context about the patient dataset from which findings were derived.
     * @param language The ISO language code (e.g., "en", "ja") for the desired output language. Defaults to "en".
     * @return A JSON string containing "description" and "recommendations" on success.
     * @throws Exception if the AI service call fails or if the response is not in the expected JSON format.
     */
    suspend fun generateInsightText(
        categoryName: String,
        calculatedFindings: String,
        patientContext: String,
        language: String = "en"
    ): String {
        // isLoading and errorMessage states are managed by the calling ViewModel (ClinicianViewModel)
        try {
            val systemPrompt = """
            You are a nutritional analysis assistant for the NutriTrack application, aiding clinicians.
            Based on the following calculated findings for the nutritional category "${categoryName}", provide a concise textual description and 2-3 actionable recommendations for clinicians.
            The findings are derived from: ${patientContext}.

            LANGUAGE INSTRUCTIONS:
            - You MUST generate content in the language with code: "$language"
            - If the language is "en", generate in English
            - If the language is "ja", generate in Japanese
            - If the language is "zh", generate in Chinese
            - If the language is "ms", generate in Malay
            - If the language is "fr", generate in French
            - For other language codes, try to generate in that language, falling back to English if necessary

            Calculated Findings:
            ${calculatedFindings}

            Respond ONLY with a valid JSON object containing two keys:
            1. "description": A string (max 40 words) summarizing the key insight from the findings.
            2. "recommendations": An array of strings, where each string is an actionable recommendation (max 20 words per recommendation).

            Example of your JSON output:
            {
              "description": "Male patients show significantly lower vegetable intake scores and serve sizes compared to females, both falling short of daily guidelines.",
              "recommendations": [
                "Discuss strategies to increase daily vegetable serves for all patients, especially males.",
                "Explore barriers to vegetable consumption with male patients specifically.",
                "Provide tailored recipes and meal planning tips to incorporate more vegetables."
              ]
            }
            """.trimIndent()

            Log.d(TAG, "generateInsightText: Sending prompt for category $categoryName to Gemini...")

            var aiResponse = geminiRepository.generateContent(systemPrompt)

            Log.d(TAG, "generateInsightText: Received raw response for $categoryName: ${aiResponse.take(250)}...") // Log more to see markers

            // Attempt to strip markdown code block markers
            val jsonMarkdownPrefix = "```json"
            val jsonMarkdownSuffix = "```"

            var cleanedResponse = aiResponse.trim() // Trim whitespace first

            if (cleanedResponse.startsWith(jsonMarkdownPrefix)) {
                cleanedResponse = cleanedResponse.substring(jsonMarkdownPrefix.length)
                if (cleanedResponse.endsWith(jsonMarkdownSuffix)) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length - jsonMarkdownSuffix.length)
                }
                cleanedResponse = cleanedResponse.trim() // Trim again after stripping markers
                Log.d(TAG, "generateInsightText: Cleaned response after attempting to strip markdown: ${cleanedResponse.take(250)}...")
            }


            if (cleanedResponse.startsWith("{") && cleanedResponse.endsWith("}")) {
                return cleanedResponse
            } else {
                Log.e(TAG, "generateInsightText: Invalid JSON response for $categoryName even after cleaning attempts. Final checked response: $cleanedResponse")
                throw Exception("Received invalid format from AI for $categoryName. Expected JSON. Raw response: $aiResponse")
            }

        } catch (e: Exception) {
            Log.e(TAG, "generateInsightText: Error generating insight text for $categoryName: ${e.message}", e)
            // Re-throw the exception to be caught by the caller
            throw Exception("Error generating insight for $categoryName: ${e.message ?: "Unknown AI error"}", e)
        }
        // isLoading is managed by the caller
    }
} 