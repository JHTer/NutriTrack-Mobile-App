package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.repository.PatientRepository
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Deferred
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import org.json.JSONObject

/**
 * Data class for HEIFA score averages by gender
 */
data class HeifaAverages(
    val maleAverage: Double? = null,
    val femaleAverage: Double? = null
)

/**
 * Data class for component analysis by gender
 */
data class ComponentAnalysis(
    val component: String,
    val maleScore: Double, // HEIFA component score for males
    val femaleScore: Double, // HEIFA component score for females
    val maxScore: Double = 10.0, // Max possible HEIFA score for this component
    val maleAvgServeSize: Double? = null, // Average serve size/intake for males
    val femaleAvgServeSize: Double? = null, // Average serve size/intake for females
    val serveUnit: String? = null, // Unit for the serve size, e.g., "serves/day", "mL/day"
    val description: String = "" // Textual description, potentially AI-generated
)

/**
 * Data class for AI-generated insights
 */
data class AiInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String,
    val confidence: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val patientCount: Int = 0,
    val isNew: Boolean = true,
    val recommendations: List<String> = emptyList()
)

/**
 * Data class representing a chat message
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val categories: List<String> = emptyList()
)

/**
 * ViewModel for the Clinician screen
 */
class ClinicianViewModel : ViewModel() {
    companion object {
        private const val TAG = "ClinicianViewModel"
    }

    // HEIFA averages
    var heifaAverages by mutableStateOf<HeifaAverages?>(null)
        private set

    // Component Analysis for different HEIFA categories
    private val _componentAnalysis = mutableStateOf<List<ComponentAnalysis>>(emptyList())
    val componentAnalysis: List<ComponentAnalysis> get() = _componentAnalysis.value

    // AI-generated insights
    private val _insights = mutableStateOf<List<AiInsight>>(emptyList())
    val insights: List<AiInsight> get() = _insights.value

    // Chat conversation for AI Consultant
    private val _chatMessages = mutableStateOf<List<ChatMessage>>(emptyList())
    val chatMessages: List<ChatMessage> get() = _chatMessages.value

    // Suggested follow-up questions
    private val _suggestedQuestions = mutableStateOf<List<String>>(emptyList())
    val suggestedQuestions: List<String> get() = _suggestedQuestions.value

    // Cached patient counts (to avoid suspend function calls in non-suspend contexts)
    private var cachedTotalPatientCount: Int = 0
    private var cachedMalePatientCount: Int = 0
    private var cachedFemalePatientCount: Int = 0

    // Loading states
    var isLoadingAverages by mutableStateOf(false)
        private set
    var isLoadingInsights by mutableStateOf(false)
        private set
    var isGeneratingResponse by mutableStateOf(false)
        private set
    var isAnalyzingData by mutableStateOf(false)
        private set

    // Error messages
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Repositories for data access
    private lateinit var patientRepository: PatientRepository
    private val geminiViewModel = GeminiViewModel()

    /**
     * Initialize repositories and load initial data
     */
    fun initialize(context: Context) {
        val patientDao = NutriTrackDatabase.getDatabase(context).patientDao()
        patientRepository = PatientRepository(patientDao)
        
        // Get device language
        val deviceLanguage = getDeviceLanguageCode()
        
        // Initialize chat with welcome message
        if (_chatMessages.value.isEmpty()) {
            // Create a localized welcome message based on device language
            val welcomeMessage = when (deviceLanguage) {
                "ja" -> "こんにちは、NutriAssistです。HEIFAスコア分析のお手伝いをどのようにすればよいですか？"
                "zh" -> "你好，我是NutriAssist。我能如何帮助您进行HEIFA评分分析？"
                "ms" -> "Halo, saya NutriAssist. Bagaimana saya boleh membantu dengan analisis skor HEIFA anda hari ini?"
                "fr" -> "Bonjour, je suis NutriAssist. Comment puis-je vous aider avec votre analyse de score HEIFA aujourd'hui ?"
                else -> "Hello, I'm NutriAssist. How can I help with your HEIFA score analysis today?"
            }
            
            _chatMessages.value = listOf(
                ChatMessage(
                    content = welcomeMessage,
                    isFromUser = false,
                    categories = listOf("welcome")
                )
            )
            
            // Initial suggested questions - these will be translated by the GeminiViewModel
            _suggestedQuestions.value = listOf(
                "What differences exist between male and female vegetable scores?",
                "Which HEIFA components show the most significant gender differences?",
                "What common nutritional deficiencies appear in the patient population?"
            )
            
            // Generate localized suggested questions
            viewModelScope.launch {
                try {
                    val localizedQuestionsPrompt = """
                        Translate these nutrition-related questions to ${getLanguageName(deviceLanguage)}:
                        
                        1. What differences exist between male and female vegetable scores?
                        2. Which HEIFA components show the most significant gender differences?
                        3. What common nutritional deficiencies appear in the patient population?
                        
                        Reply with ONLY the translated questions in numbered format (1. 2. 3.).
                    """.trimIndent()
                    
                    // Only translate if not English
                    if (deviceLanguage != "en") {
                        val translatedQuestionsText = geminiViewModel.generateContent(localizedQuestionsPrompt)
                        
                        // Parse the numbered list
                        val questionPattern = Regex("\\d+\\.\\s*(.+)")
                        val translatedQuestions = questionPattern.findAll(translatedQuestionsText)
                            .map { it.groupValues[1].trim() }
                            .filter { it.isNotEmpty() }
                            .toList()
                        
                        // Update suggested questions if we parsed any
                        if (translatedQuestions.isNotEmpty()) {
                            _suggestedQuestions.value = translatedQuestions
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating localized suggested questions: ${e.message}", e)
                    // Keep the default English questions on error
                }
            }
        }
        
        // Load HEIFA averages
        loadHeifaAverages(context)
        
        // Load component analysis
        loadComponentAnalysis(context)
        
        // Cache patient counts
        cachePatientCounts()
    }
    
    /**
     * Cache patient counts to avoid suspend function calls in non-suspend contexts
     */
    private fun cachePatientCounts() {
        viewModelScope.launch {
            try {
                cachedTotalPatientCount = patientRepository.getTotalPatientCount()
                cachedMalePatientCount = patientRepository.getMalePatientCount()
                cachedFemalePatientCount = patientRepository.getFemalePatientCount()
                
                Log.d(TAG, "Cached patient counts: total=$cachedTotalPatientCount, male=$cachedMalePatientCount, female=$cachedFemalePatientCount")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching patient counts: ${e.message}", e)
            }
        }
    }

    /**
     * Load HEIFA score averages by gender from the database
     */
    fun loadHeifaAverages(context: Context) {
        isLoadingAverages = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val maleAvg = patientRepository.getAverageHeifaScoreMale()
                val femaleAvg = patientRepository.getAverageHeifaScoreFemale()
                
                heifaAverages = HeifaAverages(maleAverage = maleAvg, femaleAverage = femaleAvg)
                Log.d(TAG, "Loaded HEIFA Averages: Male - $maleAvg, Female - $femaleAvg")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading HEIFA averages: ${e.message}", e)
                errorMessage = "Failed to load HEIFA averages."
                
                // Set to null to indicate loading error
                heifaAverages = null
            } finally {
                isLoadingAverages = false
            }
        }
    }

    /**
     * Load component analysis data from the database
     */
    private fun loadComponentAnalysis(context: Context) {
        viewModelScope.launch {
            try {
                // Load component scores from the database
                val components = listOf(
                    "vegetables", "fruits", "grains", "protein", 
                    "dairy", "water", "sodium", "unsaturated_fat"
                )
                
                val analysisItems = components.mapNotNull { component ->
                    val maleScore = patientRepository.getAverageComponentScoreMale(component) ?: 0.0 // Use 0.0 as a fallback if null
                    val femaleScore = patientRepository.getAverageComponentScoreFemale(component) ?: 0.0 // Use 0.0 as a fallback if null
                    
                    var maleAvgServeSize: Double? = null
                    var femaleAvgServeSize: Double? = null
                    var serveUnit: String? = null

                    when (component) {
                        "vegetables" -> {
                            maleAvgServeSize = patientRepository.getAverageVegetableServesMale()
                            femaleAvgServeSize = patientRepository.getAverageVegetableServesFemale()
                            serveUnit = "serves/day"
                        }
                        "fruits" -> {
                            maleAvgServeSize = patientRepository.getAverageFruitServesMale()
                            femaleAvgServeSize = patientRepository.getAverageFruitServesFemale()
                            serveUnit = "serves/day"
                        }
                        "protein" -> {
                            maleAvgServeSize = patientRepository.getAverageProteinServesMale()
                            femaleAvgServeSize = patientRepository.getAverageProteinServesFemale()
                            serveUnit = "serves/day"
                        }
                        "water" -> {
                            maleAvgServeSize = patientRepository.getAverageWaterIntakeMLMale()
                            femaleAvgServeSize = patientRepository.getAverageWaterIntakeMLFemale()
                            serveUnit = "mL/day"
                        }
                        // For other components like grains, dairy, sodium, unsaturated_fat, we don't have specific serve size methods yet.
                        // They will have null serve sizes and units for now.
                    }

                    val maxScore = when (component) {
                        "vegetables", "fruits", "grains", "protein", 
                        "dairy", "water", "sodium", "unsaturated_fat" -> 5.0 
                        // HEIFA 2023: Most components are 0-10, but some sub-scores might be 0-5. 
                        // Let's assume a common scale for now, or make it component-specific if HEIFA guidelines dictate.
                        // For simplicity, if the original data has specific max scores, use that. Here, it was 5.0.
                        else -> 10.0 // Default max score if not specified
                    }
                    
                    ComponentAnalysis(
                        component = component.capitalize(),
                        maleScore = maleScore,
                        femaleScore = femaleScore,
                        maxScore = maxScore, 
                        maleAvgServeSize = maleAvgServeSize,
                        femaleAvgServeSize = femaleAvgServeSize,
                        serveUnit = serveUnit,
                        description = getComponentDescription(component, maleScore, femaleScore)
                    )
                }
                
                if (analysisItems.isNotEmpty()) {
                    _componentAnalysis.value = analysisItems
                    Log.d(TAG, "Loaded component analysis data with ${analysisItems.size} components")
                } else {
                    Log.w(TAG, "No component analysis data was loaded")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading component analysis: ${e.message}", e)
                errorMessage = "Failed to load component analysis data."
            }
        }
    }
    
    /**
     * Generate component description based on scores
     */
    private fun getComponentDescription(component: String, maleScore: Double, femaleScore: Double): String {
        // This function will no longer generate AI descriptions.
        // Descriptions will be part of AiInsight, generated by generateInsightsFromComponentAnalysis.
        return "" // Return empty or a very basic placeholder if needed by UI immediately.
    }

    /**
     * Format a double to a specified number of decimal places
     */
    private fun Double.format(digits: Int = 2): String {
        return "%.${digits}f".format(this)
    }

    /**
     * Extension function to capitalize first letter of a string
     */
    private fun String.capitalize(): String {
        return replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
            else it.toString() 
        }.replace("_", " ")
    }

    /**
     * Get the device language code
     */
    private fun getDeviceLanguageCode(): String {
        val locale = Locale.getDefault()
        return locale.language // Returns language code like "en", "ja", "zh", etc.
    }

    /**
     * Get the full language name from a language code
     */
    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "ja" -> "Japanese"
            "zh" -> "Chinese"
            "ms" -> "Malay"
            "fr" -> "French"
            else -> Locale(languageCode).displayLanguage // Fallback to system language name
        }
    }

    /**
     * Send a message to the AI assistant and get a response
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return
        
        // Get device language
        val deviceLanguage = getDeviceLanguageCode()
        
        // Add user message to chat
        val newUserMessage = ChatMessage(
            content = userMessage,
            isFromUser = true
        )
        _chatMessages.value = _chatMessages.value + newUserMessage
        
        // Generate AI response
        isGeneratingResponse = true
        
        // Use GeminiViewModel to generate the chat response
        geminiViewModel.generateChatResponse(
            userMessage = userMessage,
            language = deviceLanguage,
            onSuccess = { response, suggestedQuestions ->
                // Add AI response to chat
                val newAiMessage = ChatMessage(
                    content = response,
                    isFromUser = false,
                    categories = extractCategories(response)
                )
                _chatMessages.value = _chatMessages.value + newAiMessage
                
                // Update suggested questions
                _suggestedQuestions.value = suggestedQuestions
                
                isGeneratingResponse = false
            },
            onError = { errorMsg ->
                Log.e(TAG, "Error generating AI response: $errorMsg")
                
                // Add error message to chat
                val errorAiMessage = ChatMessage(
                    content = "I'm sorry, I couldn't generate a response. Please try again later.",
                    isFromUser = false
                )
                _chatMessages.value = _chatMessages.value + errorAiMessage
                
                isGeneratingResponse = false
            }
        )
    }
    
    /**
     * Extract categories from AI response
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
     * Remove category tags from the response
     */
    private fun removeCategories(response: String): String {
        return response.replace(Regex("#[a-zA-Z_]+"), "").trim()
    }
    
    /**
     * Run AI-powered analysis on patient data from the database
     */
    fun findDataPatterns(context: Context) {
        Log.d(TAG, "findDataPatterns: Triggered by UI. Current _componentAnalysis size: ${_componentAnalysis.value.size}")
        if (_componentAnalysis.value.isEmpty()) {
            Log.e(TAG, "findDataPatterns: ComponentAnalysis data is not loaded yet. Cannot generate insights.")
            errorMessage = "Component data not ready. Please try again shortly."
            return
        }
        viewModelScope.launch {
            generateInsightsFromComponentAnalysis()
        }
    }

    private suspend fun generateInsightsFromComponentAnalysis() {
        isAnalyzingData = true
        errorMessage = null
        Log.d(TAG, "generateInsightsFromComponentAnalysis: Starting generation from component data.")

        try {
            val patientContext = "Data based on approximately ${cachedMalePatientCount} male and ${cachedFemalePatientCount} female patients from a total of ${cachedTotalPatientCount} patients."
            val relevantComponents = _componentAnalysis.value.filter {
                // Filter for components we want to generate insights for
                it.component.lowercase() in listOf("vegetables", "fruits", "protein", "water", "grains", "dairy", "sodium", "unsaturated_fat")
            }

            if (relevantComponents.isEmpty()) {
                Log.w(TAG, "No relevant components found in _componentAnalysis to generate insights for.")
                _insights.value = emptyList() // Clear any previous insights
                isAnalyzingData = false
                return
            }

            // Get device language
            val deviceLanguage = getDeviceLanguageCode()

            val deferredInsights = mutableListOf<Deferred<AiInsight?>>()

            for (analysisItem in relevantComponents) {
                val deferred = viewModelScope.async {
                    try {
                        val category = analysisItem.component.lowercase()
                        val findings = mutableListOf<String>()
                        findings.add("Category: ${analysisItem.component}")
                        findings.add("Male HEIFA Score: ${analysisItem.maleScore.format()}/${analysisItem.maxScore.format()}")
                        findings.add("Female HEIFA Score: ${analysisItem.femaleScore.format()}/${analysisItem.maxScore.format()}")
                        analysisItem.maleAvgServeSize?.let { findings.add("Male Avg Intake: ${it.format()} ${analysisItem.serveUnit ?: ""}") }
                        analysisItem.femaleAvgServeSize?.let { findings.add("Female Avg Intake: ${it.format()} ${analysisItem.serveUnit ?: ""}") }
                        val calculatedFindingsString = findings.joinToString("\n")

                        Log.d(TAG, "Calling generateInsightText (suspend) for category: $category")
                        val jsonResponse = geminiViewModel.generateInsightText(
                            categoryName = analysisItem.component,
                            calculatedFindings = calculatedFindingsString,
                            patientContext = patientContext,
                            language = deviceLanguage
                        )

                        Log.d(TAG, "Successfully received insight text for $category: $jsonResponse")
                        val jsonObject = JSONObject(jsonResponse)
                        val description = jsonObject.optString("description", "No description provided.")
                        val recommendationsArray = jsonObject.optJSONArray("recommendations")
                        val recommendations = mutableListOf<String>()
                        if (recommendationsArray != null) {
                            for (i in 0 until recommendationsArray.length()) {
                                recommendations.add(recommendationsArray.getString(i))
                            }
                        }
                        AiInsight(
                            title = "${analysisItem.component} Analysis",
                            description = description,
                            category = category,
                            recommendations = recommendations,
                            patientCount = cachedTotalPatientCount
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error generating or parsing insight for ${analysisItem.component}: ${e.message}", e)
                        null // Return null if this specific insight generation failed
                    }
                }
                deferredInsights.add(deferred)
            }

            // Wait for all async calls to complete and collect results
            val results = deferredInsights.awaitAll()
            val newInsightsList = results.filterNotNull().toMutableList()

            _insights.value = newInsightsList

        } catch (e: Exception) {
            Log.e(TAG, "Error in generateInsightsFromComponentAnalysis: ${e.message}", e)
            errorMessage = "An error occurred while generating insights."
            _insights.value = emptyList() // Clear insights on major error
        } finally {
            isAnalyzingData = false
            Log.d(TAG, "generateInsightsFromComponentAnalysis: Finished. Insights count: ${_insights.value.size}")
        }
    }

    /**
     * Add or update an insight in the insights list
     */
    private fun addOrUpdateInsight(insight: AiInsight) {
        val existingIndex = _insights.value.indexOfFirst { it.id == insight.id }
        
        if (existingIndex >= 0) {
            // Update existing insight
            val updatedInsights = _insights.value.toMutableList()
            updatedInsights[existingIndex] = insight
            _insights.value = updatedInsights
        } else {
            // Add new insight
            _insights.value = _insights.value + insight
        }
    }
    
    /**
     * Mark an insight as read (not new)
     */
    fun markInsightAsRead(insightId: String) {
        val existingIndex = _insights.value.indexOfFirst { it.id == insightId }
        
        if (existingIndex >= 0) {
            // Update the isNew flag
            val updatedInsights = _insights.value.toMutableList()
            val currentInsight = updatedInsights[existingIndex]
            updatedInsights[existingIndex] = currentInsight.copy(isNew = false)
            _insights.value = updatedInsights
        }
    }
    
    /**
     * Get insights filtered by category
     */
    fun getInsightsByCategory(category: String): List<AiInsight> {
        return _insights.value.filter { it.category == category }
    }
    
    /**
     * Format date for display
     */
    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Clear the error message
     */
    fun clearErrorMessage() {
        errorMessage = null
    }
} 