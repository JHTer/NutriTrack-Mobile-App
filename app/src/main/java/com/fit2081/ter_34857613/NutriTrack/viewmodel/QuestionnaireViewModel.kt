package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.repository.FoodPreferences
import com.fit2081.ter_34857613.NutriTrack.model.repository.FoodPreferencesRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.QuestionnaireRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Questionnaire screen, responsible for managing user food preferences, persona selection,
 * and daily routine timings (sleep, wake, biggest meal).
 *
 * This ViewModel handles:
 * - Loading and saving user preferences: It interacts with [FoodPreferencesRepository] (primary) and a legacy
 *   [QuestionnaireRepository] (for SharedPreferences, with migration to database) to persist user choices.
 * - Managing UI state: Exposes various `mutableStateOf` properties for food category selections (e.g., `fruitsSelected`),
 *   selected persona (`selectedPersona`), time inputs (`biggestMealTime`), and UI element states (e.g., `expanded` for dropdowns,
 *   `showPersonaModal` for dialogs).
 * - Providing localized persona data: Dynamically creates a list of [Persona] objects with names and descriptions
 *   localized using the current application context. This list is refreshed if the app's language changes.
 * - Handling language changes: Includes a `languageChanged` state to signal UI recomposition when the locale updates,
 *   ensuring persona lists are re-localized.
 *
 * It uses `viewModelScope` for asynchronous operations like loading and saving preferences.
 */
class QuestionnaireViewModel : ViewModel() {

    // Old repository for backward compatibility during migration
    private val oldRepository = QuestionnaireRepository()
    private val userPreferencesRepository = UserPreferencesRepository()

    // New repository reference to be initialized when needed
    /** Repository for storing and retrieving [FoodPreferences] from the database. Initialized lazily. */
    private var foodPreferencesRepository: FoodPreferencesRepository? = null

    // Application context
    /** Application context, updated on language change and repository initialization. Used for accessing string resources. */
    private var appContext: Context? = null

    // This MutableState will force recomposition when language changes
    /** A [mutableStateOf] boolean that toggles to signal a language change, used to trigger recomposition for localization. */
    private var _languageChanged = mutableStateOf(false)
    /** Publicly exposed state for language changes. */
    val languageChanged = _languageChanged
    
    // Food category states
    /** Represents whether the user has selected 'Fruits' as a preferred food category. */
    var fruitsSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Vegetables' as a preferred food category. */
    var vegetablesSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Grains' as a preferred food category. */
    var grainsSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Red Meat' as a preferred food category. */
    var redMeatSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Seafood' as a preferred food category. */
    var seafoodSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Poultry' as a preferred food category. */
    var poultrySelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Fish' (distinct from general seafood) as a preferred food category. */
    var fishSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Eggs' as a preferred food category. */
    var eggsSelected by mutableStateOf(false)
    /** Represents whether the user has selected 'Nuts & Seeds' as a preferred food category. */
    var nutsSeedsSelected by mutableStateOf(false)

    // Persona selection states
    /** The currently selected [Persona] object by the user, or `null` if none selected. */
    var selectedPersona by mutableStateOf<Persona?>(null)
    /** Controls the expanded/collapsed state of the persona selection dropdown. */
    var expanded by mutableStateOf(false)
    /** Controls the visibility of the modal dialog displaying details of a selected persona. */
    var showPersonaModal by mutableStateOf(false)
    /** Holds the [Persona] object whose details are currently being shown in the modal. */
    var currentPersonaDetails by mutableStateOf<Persona?>(null)

    // Time states
    /** String representation of the user's biggest meal time (e.g., "13:00"). */
    var biggestMealTime by mutableStateOf("")
    /** String representation of the user's typical sleep time (e.g., "22:30"). */
    var sleepTime by mutableStateOf("")
    /** String representation of the user's typical wake-up time (e.g., "06:30"). */
    var wakeUpTime by mutableStateOf("")
    /** Controls the visibility of the time picker for selecting the biggest meal time. */
    var showBiggestMealTimePicker by mutableStateOf(false)
    /** Controls the visibility of the time picker for selecting sleep time. */
    var showSleepTimePicker by mutableStateOf(false)
    /** Controls the visibility of the time picker for selecting wake-up time. */
    var showWakeUpTimePicker by mutableStateOf(false)

    // Lazy initialized personas list
    /** Internal cache for the list of [Persona] objects. Reloaded if language changes. */
    private var _personas: List<Persona>? = null
    /** Stores the language code for which the current `_personas` list was generated. */
    private var currentLanguage: String = ""

    // Getter for personas list that initializes if needed
    /**
     * Provides a list of available [Persona] objects.
     * The list is lazily initialized and re-created if the application's language changes,
     * ensuring that persona names and descriptions are correctly localized.
     * It uses [createPersonasList] for actual list creation.
     */
    val personas: List<Persona>
        get() {
            // Check if we need to reload personas due to a language change
            val newLanguage = appContext?.resources?.configuration?.locales?.get(0)?.language ?: ""
            if (_personas == null || newLanguage != currentLanguage) {
                currentLanguage = newLanguage
                _personas = createPersonasList()
            }
            return _personas!!
        }

    /**
     * Creates and returns a list of predefined [Persona] objects.
     * Each persona includes an ID, name, description, image resource, and associated color.
     * Persona names and descriptions are localized using string resources obtained from the [appContext],
     * with fallback to default English strings if the context or resources are unavailable.
     *
     * @return A list of [Persona] objects.
     */
    private fun createPersonasList(): List<Persona> {
        return listOf(
            Persona(
                id = 1,
                name = appContext?.getString(R.string.persona_health_devotee) ?: "Health Devotee",
                description = appContext?.getString(R.string.persona_health_devotee_desc)
                    ?: "I'm passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.",
                imageResource = R.drawable.persona_1,
                color = Color(0xFF6200EA) // Deep purple
            ),
            Persona(
                id = 2,
                name = appContext?.getString(R.string.persona_mindful_eater) ?: "Mindful Eater",
                description = appContext?.getString(R.string.persona_mindful_eater_desc)
                    ?: "I'm health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.",
                imageResource = R.drawable.persona_2,
                color = Color(0xFF7B1FA2) // Purple
            ),
            Persona(
                id = 3,
                name = appContext?.getString(R.string.persona_wellness_striver) ?: "Wellness Striver",
                description = appContext?.getString(R.string.persona_wellness_striver_desc)
                    ?: "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I've tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I'll give it a go.",
                imageResource = R.drawable.persona_3,
                color = Color(0xFF00897B) // Teal
            ),
            Persona(
                id = 4,
                name = appContext?.getString(R.string.persona_balance_seeker) ?: "Balance Seeker",
                description = appContext?.getString(R.string.persona_balance_seeker_desc)
                    ?: "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn't have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.",
                imageResource = R.drawable.persona_4,
                color = Color(0xFF303F9F) // Indigo
            ),
            Persona(
                id = 5,
                name = appContext?.getString(R.string.persona_health_procrastinator) ?: "Health Procrastinator",
                description = appContext?.getString(R.string.persona_health_procrastinator_desc)
                    ?: "I'm contemplating healthy eating but it's not a priority for me right now. I know the basics about what it means to be healthy, but it doesn't seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.",
                imageResource = R.drawable.persona_5,
                color = Color(0xFF00897B) // Teal
            ),
            Persona(
                id = 6,
                name = appContext?.getString(R.string.persona_food_carefree) ?: "Food Carefree",
                description = appContext?.getString(R.string.persona_food_carefree_desc)
                    ?: "I'm not bothered about healthy eating. I don't really see the point and I don't think about it. I don't really notice healthy eating tips or recipes and I don't care what I eat.",
                imageResource = R.drawable.persona_6,
                color = Color(0xFFE64A19) // Deep orange
            )
        )
    }

    /**
     * Initializes the [foodPreferencesRepository] if it hasn't been already, using the provided [context].
     * It also updates the internal [appContext] and forces a refresh of the localized [personas] list
     * by setting `_personas` to `null` (which triggers re-creation on next access).
     *
     * @param context The application context used for database initialization and accessing resources.
     */
    private fun initRepository(context: Context) {
        // Store the application context for string resources
        this.appContext = context.applicationContext

        // Initialize the database repository if needed
        if (foodPreferencesRepository == null) {
            val database = NutriTrackDatabase.getDatabase(context)
            foodPreferencesRepository = FoodPreferencesRepository(database.foodPreferencesDao())
        }

        // Force refresh the personas list with localized strings
        _personas = null
    }

    /**
     * Loads the user's saved food preferences and questionnaire responses for a given `userId`.
     *
     * It first attempts to load preferences from the primary data source (database via [foodPreferencesRepository]).
     * If not found in the database, it tries to load them from a legacy SharedPreferences storage via `oldRepository`.
     * If loaded from SharedPreferences, these legacy preferences are then migrated to the database for future use.
     * The loaded preferences (from either source) are used to update the ViewModel's state variables via [updateStateFromPreferences].
     * Logs the source of loaded preferences or if no preferences are found.
     *
     * @param context The application context, used to initialize repositories and access SharedPreferences.
     * @param userId The unique identifier of the user whose preferences are to be loaded.
     */
    fun loadPreferences(context: Context, userId: String) {
        initRepository(context)

        viewModelScope.launch {
            try {
                // First try to load from database
                val dbPrefs = foodPreferencesRepository?.loadPreferences(userId)

                if (dbPrefs != null) {
                    // Data exists in database, use it
                    updateStateFromPreferences(dbPrefs)
                    Log.d("QuestionnaireVM", "Loaded preferences from database for user $userId")
                } else {
                    // Try to load from SharedPreferences (legacy)
                    val legacyPrefs = oldRepository.loadPreferences(context, userId)
                    if (legacyPrefs != null) {
                        updateStateFromPreferences(legacyPrefs)
                        Log.d("QuestionnaireVM", "Loaded preferences from SharedPreferences for user $userId")

                        // Migrate to database for next time
                        foodPreferencesRepository?.savePreferences(userId, legacyPrefs)
                        Log.d("QuestionnaireVM", "Migrated preferences to database for user $userId")
                    } else {
                        Log.d("QuestionnaireVM", "No preferences found for user $userId")
                    }
                }
            } catch (e: Exception) {
                Log.e("QuestionnaireVM", "Error loading preferences: ${e.message}", e)
            }
        }
    }

    /**
     * Updates the ViewModel's state variables (food selections, persona, times) based on a loaded [FoodPreferences] object.
     *
     * This helper method is called internally after preferences are successfully loaded from either the database or SharedPreferences.
     * It maps the fields from the `prefs` object to the corresponding `mutableStateOf` properties in the ViewModel.
     * For `selectedPersona`, it finds the matching [Persona] object from the `personas` list based on `prefs.personaId`.
     *
     * @param prefs The [FoodPreferences] object containing the data to update the state with.
     */
    private fun updateStateFromPreferences(prefs: FoodPreferences) {
        fruitsSelected = prefs.fruits
        vegetablesSelected = prefs.vegetables
        grainsSelected = prefs.grains
        redMeatSelected = prefs.redMeat
        seafoodSelected = prefs.seafood
        poultrySelected = prefs.poultry
        fishSelected = prefs.fish
        eggsSelected = prefs.eggs
        nutsSeedsSelected = prefs.nutsSeeds

        // Find matching persona by ID
        selectedPersona = personas.find { it.id == prefs.personaId }

        // Restore time preferences
        biggestMealTime = prefs.biggestMealTime
        sleepTime = prefs.sleepTime
        wakeUpTime = prefs.wakeUpTime
    }

    /**
     * Saves the current user preferences (food selections, persona, times) to persistent storage for the given `userId`.
     *
     * It first constructs a [FoodPreferences] object from the current state of the ViewModel's properties.
     * Then, it saves this object to both the database (via [foodPreferencesRepository]) and, for backward compatibility,
     * to SharedPreferences (via `oldRepository`).
     * Finally, it marks that the user has completed the questionnaire using [UserPreferencesRepository].
     * Logs the successful saving to the database.
     *
     * @param context The application context, used to initialize repositories.
     * @param userId The unique identifier of the user whose preferences are being saved.
     * @return The created [FoodPreferences] object that was saved.
     */
    fun savePreferences(context: Context, userId: String): FoodPreferences {
        initRepository(context)

        val preferences = FoodPreferences(
            fruits = fruitsSelected,
            vegetables = vegetablesSelected,
            grains = grainsSelected,
            redMeat = redMeatSelected,
            seafood = seafoodSelected,
            poultry = poultrySelected,
            fish = fishSelected,
            eggs = eggsSelected,
            nutsSeeds = nutsSeedsSelected,
            personaId = selectedPersona?.id ?: 0,
            personaName = selectedPersona?.name ?: "",
            biggestMealTime = biggestMealTime,
            sleepTime = sleepTime,
            wakeUpTime = wakeUpTime
        )

        viewModelScope.launch {
            try {
                // Save to database
                foodPreferencesRepository?.savePreferences(userId, preferences)
                Log.d("QuestionnaireVM", "Saved preferences to database for user $userId")

                // For backward compatibility - keep the SharedPreferences updated
                oldRepository.savePreferences(context, userId, preferences)

                // Mark that the user has completed the questionnaire
                userPreferencesRepository.markQuestionnaireCompleted(context, userId)
            } catch (e: Exception) {
                Log.e("QuestionnaireVM", "Error saving preferences: ${e.message}", e)
            }
        }

        return preferences
    }

    /**
     * Validates the user-selected times (biggest meal, sleep, wake-up) for logical consistency.
     *
     * Checks the following conditions:
     * - If any time is not set, validation is skipped (returns `true`).
     * - Meal time should not be within 1 hour of sleep time.
     * - Calculated sleep duration (from sleep time to wake-up time, accounting for overnight) should be at least 4 hours (240 minutes).
     * - Meal time should not fall within the calculated sleep period.
     *
     * Logs specific validation failures. If an error occurs during time parsing or calculation,
     * it defaults to returning `true` to avoid blocking the user from saving due to an internal validation issue.
     *
     * @return `true` if the times are considered valid or if validation is skipped/fails internally, `false` if a clear inconsistency is detected.
     */
    fun validateTimes(): Boolean {
        if (biggestMealTime.isEmpty() || sleepTime.isEmpty() || wakeUpTime.isEmpty()) {
            return true  // Skip validation if times aren't set
        }

        try {
            // Convert times to minutes since midnight
            val mealTimeMinutes = convertTimeToMinutes(biggestMealTime)
            val sleepTimeMinutes = convertTimeToMinutes(sleepTime)
            val wakeUpTimeMinutes = convertTimeToMinutes(wakeUpTime)

            // Check if meal time is too close to sleep time (within 1 hour)
            val mealToSleepDifference = Math.abs(mealTimeMinutes - sleepTimeMinutes)
            if (mealToSleepDifference < 60) {
                Log.d("TimeValidation", "Meal time too close to sleep time")
                return false  // Can't eat and sleep at the same time or within 1 hour
            }

            // Check if wake up time is after sleep time (accounting for overnight)
            val sleepDuration = calculateSleepDuration(sleepTimeMinutes, wakeUpTimeMinutes)
            if (sleepDuration < 240) {
                Log.d("TimeValidation", "Sleep duration too short: $sleepDuration minutes")
                return false  // Sleep duration should be at least 4 hours
            }

            // Check if meal is during sleep period
            if (isMealDuringSleep(mealTimeMinutes, sleepTimeMinutes, wakeUpTimeMinutes)) {
                Log.d("TimeValidation", "Meal time during sleep period")
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e("TimeValidation", "Error validating times: ${e.message}")
            return true  // If there's an error in validation, allow saving to avoid blocking the user
        }
    }

    /**
     * Converts a time string in "HH:MM" format to the total number of minutes since midnight.
     *
     * Example: "13:30" becomes `810` (13 * 60 + 30).
     * If the time string does not contain ":", it returns `0`.
     *
     * @param time The time string to convert.
     * @return The total minutes since midnight, or `0` if the format is invalid.
     */
    private fun convertTimeToMinutes(time: String): Int {
        if (!time.contains(":")) return 0
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    /**
     * Calculates the duration of sleep in minutes, correctly handling overnight sleep periods.
     *
     * For example, if sleep time is 22:00 (1320 minutes) and wake time is 06:00 (360 minutes),
     * it calculates the duration as (1440 - 1320) + 360 = 120 + 360 = 480 minutes.
     * If wake time is later on the same day (e.g., sleep 01:00, wake 08:00), it's a simple subtraction.
     *
     * @param sleepTime The sleep time in total minutes since midnight.
     * @param wakeTime The wake-up time in total minutes since midnight.
     * @return The total sleep duration in minutes.
     */
    private fun calculateSleepDuration(sleepTime: Int, wakeTime: Int): Int {
        return if (wakeTime > sleepTime) {
            wakeTime - sleepTime
        } else {
            (1440 - sleepTime) + wakeTime  // Handle overnight sleep (1440 minutes in a day)
        }
    }

    /**
     * Checks if a given meal time falls within the calculated sleep period.
     *
     * Considers both normal (same-day) sleep and overnight sleep scenarios.
     * - Normal sleep (e.g., sleep at 01:00, wake at 08:00): meal is during sleep if `sleepTime <= mealTime < wakeTime`.
     * - Overnight sleep (e.g., sleep at 22:00, wake at 06:00 next day): meal is during sleep if `mealTime >= sleepTime` OR `mealTime < wakeTime`.
     *
     * @param mealTime The meal time in total minutes since midnight.
     * @param sleepTime The sleep time in total minutes since midnight.
     * @param wakeTime The wake-up time in total minutes since midnight.
     * @return `true` if the meal time is within the sleep period, `false` otherwise.
     */
    private fun isMealDuringSleep(mealTime: Int, sleepTime: Int, wakeTime: Int): Boolean {
        return if (sleepTime < wakeTime) {
            // Normal sleep (e.g., 22:00 to 06:00 on same day)
            mealTime in sleepTime until wakeTime
        } else {
            // Overnight sleep (e.g., 22:00 to 06:00 next day)
            mealTime >= sleepTime || mealTime < wakeTime
        }
    }

    /** Toggles the expanded/collapsed state of the persona selection dropdown. */
    fun toggleExpanded() {
        expanded = !expanded
    }

    /**
     * Sets the [currentPersonaDetails] to the given [persona] and sets [showPersonaModal] to `true`
     * to display the persona details dialog.
     *
     * @param persona The [Persona] whose details are to be shown.
     */
    fun showPersonaDetails(persona: Persona) {
        currentPersonaDetails = persona
        showPersonaModal = true
    }

    /** Sets [showPersonaModal] to `false` to hide the persona details dialog. */
    fun hidePersonaModal() {
        showPersonaModal = false
    }

    /**
     * Sets the [selectedPersona] to the given [persona].
     * Also collapses the persona dropdown ([expanded] = `false`) and hides the persona details modal ([showPersonaModal] = `false`).
     *
     * @param persona The [Persona] to be selected.
     */
    fun selectPersona(persona: Persona) {
        selectedPersona = persona
        expanded = false
        showPersonaModal = false
    }

    /**
     * Clears the current persona selection by setting [selectedPersona] to `null`.
     * Also hides the persona details modal ([showPersonaModal] = `false`).
     */
    fun unselectPersona() {
        selectedPersona = null
        showPersonaModal = false
    }

    /**
     * Updates the [biggestMealTime] state with the provided `time` string.
     * Also hides the biggest meal time picker ([showBiggestMealTimePicker] = `false`).
     *
     * @param time The selected time string (e.g., "HH:MM").
     */
    fun updateBiggestMealTime(time: String) {
        biggestMealTime = time
        showBiggestMealTimePicker = false
    }

    /**
     * Updates the [sleepTime] state with the provided `time` string.
     * Also hides the sleep time picker ([showSleepTimePicker] = `false`).
     *
     * @param time The selected time string (e.g., "HH:MM").
     */
    fun updateSleepTime(time: String) {
        sleepTime = time
        showSleepTimePicker = false
    }

    /**
     * Updates the [wakeUpTime] state with the provided `time` string.
     * Also hides the wake-up time picker ([showWakeUpTimePicker] = `false`).
     *
     * @param time The selected time string (e.g., "HH:MM").
     */
    fun updateWakeUpTime(time: String) {
        wakeUpTime = time
        showWakeUpTimePicker = false
    }

    fun refreshPersonas() {
        if (appContext != null) {
            _personas = null
            // This will force reload on next access
        }
    }

    /**
     * Called when the UI language changes.
     * It updates the internal [appContext], sets `_personas` to `null` to force re-localization
     * of the persona list on next access, and toggles the [_languageChanged] state to trigger UI recomposition.
     *
     * @param context The new application context reflecting the updated locale.
     */
    fun onLanguageChanged(context: Context) {
        this.appContext = context.applicationContext
        _personas = null // Force reload of personas with new language
        _languageChanged.value = !_languageChanged.value // Trigger recomposition
    }
}

/**
 * Data class representing a user persona for the questionnaire.
 *
 * Each persona has a unique identifier, a display name, a detailed description,
 * an associated drawable resource for an image/icon, and a representative color.
 * These are used in the UI to allow users to select a persona that best describes their
 * attitudes and behaviors towards food and health.
 *
 * @property id Unique integer identifier for the persona.
 * @property name The display name of the persona (e.g., "Health Devotee"). This should be localized.
 * @property description A more detailed description of the persona's characteristics. This should be localized.
 * @property imageResource The drawable resource ID for an image representing the persona.
 * @property color A [Color] object associated with the persona, used for UI theming or visual distinction.
 */
data class Persona(
    val id: Int,
    val name: String,
    val description: String,
    val imageResource: Int,
    val color: Color
)