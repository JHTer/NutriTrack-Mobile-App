package com.fit2081.ter_34857613.NutriTrack.model.repository

import com.fit2081.ter_34857613.NutriTrack.model.api.FruitApiService
import com.fit2081.ter_34857613.NutriTrack.model.api.GeminiHelper
import com.fit2081.ter_34857613.NutriTrack.model.api.RetrofitClient
import com.fit2081.ter_34857613.NutriTrack.model.data.Fruit
import com.fit2081.ter_34857613.NutriTrack.utils.TranslationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repository for fetching fruit data from an external API and providing localized names.
 *
 * This class acts as a layer between the [FruitApiService] (which communicates with the fruit API)
 * and the application. It fetches fruit data and then uses a [TranslationService]
 * to translate fruit names to the device's current default language if it's not English.
 * It follows a singleton pattern for instantiation.
 *
 * @property fruitApiService The service used to make API calls to the fruit data source.
 * @property translationService The service used to translate fruit names.
 */
class LocalizedFruitRepository private constructor(
    private val fruitApiService: FruitApiService,
    private val translationService: TranslationService
) {
    /**
     * Companion object for [LocalizedFruitRepository].
     * Implements the singleton pattern for creating and accessing the repository instance.
     */
    companion object {
        @Volatile
        private var instance: LocalizedFruitRepository? = null
        
        /**
         * Gets the singleton instance of [LocalizedFruitRepository].
         *
         * This method provides a thread-safe way to access the single instance of the repository.
         * If the instance does not exist, it is created. It requires a [GeminiHelper]
         * to initialize its internal [TranslationService].
         *
         * @param fruitApiService The [FruitApiService] to be used for fetching fruit data.
         *                        Defaults to `RetrofitClient.fruitApiService`.
         * @param geminiHelper A [GeminiHelper] instance required for the [TranslationService].
         *                     (Note: The exact nature of `GeminiHelper` is not defined here but is a dependency).
         * @return The singleton [LocalizedFruitRepository] instance.
         */
        fun getInstance(
            fruitApiService: FruitApiService = RetrofitClient.fruitApiService,
            geminiHelper: GeminiHelper
        ): LocalizedFruitRepository {
            return instance ?: synchronized(this) {
                val translationService = TranslationService.getInstance(geminiHelper)
                instance ?: LocalizedFruitRepository(fruitApiService, translationService).also { instance = it }
            }
        }
    }
    
    /**
     * Fetches information about a specific fruit by its name and localizes the fruit's name.
     *
     * This function calls the [fruitApiService] to get fruit data. If successful, it then
     * calls [localizeFruit] to translate the fruit's name to the device's current language.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param name The English name of the fruit to search for.
     * @return A [Fruit] object with its name potentially translated, or `null` if the fruit is not found,
     *         an API error occurs, or translation fails implicitly (though `localizeFruit` handles errors).
     */
    suspend fun getFruitByName(name: String): Fruit? {
        return withContext(Dispatchers.IO) {
            try {
                val response = fruitApiService.getFruitByName(name)
                if (response.isSuccessful && response.body() != null) {
                    localizeFruit(response.body()!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Fetches a list of all available fruits from the API and localizes their names.
     *
     * This function calls [fruitApiService] to get all fruits. For each fruit in the successful response,
     * it calls [localizeFruit] to translate its name.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @return A list of [Fruit] objects, with their names potentially translated.
     *         Returns an empty list if the API call fails or an error occurs.
     */
    suspend fun getAllFruits(): List<Fruit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fruitApiService.getAllFruits()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.map { localizeFruit(it) }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Localizes a given [Fruit] object by translating its `name` property.
     *
     * If the device's current default language ([Locale.getDefault().language]) is English ("en"),
     * the original fruit object is returned without modification.
     * Otherwise, it uses the [translationService] to translate the `fruit.name` to the current language.
     * The translated name then replaces the original name in a copy of the fruit object.
     *
     * @param fruit The [Fruit] object whose name is to be localized.
     * @return A [Fruit] object with its `name` translated if the current language is not English.
     *         If translation is not needed or fails, it might return the fruit with the original name
     *         (depending on `translationService.translate` behavior).
     */
    private suspend fun localizeFruit(fruit: Fruit): Fruit {
        val currentLanguage = Locale.getDefault().language
        if (currentLanguage == "en") {
            return fruit
        }
        
        val translatedName = translationService.translate(fruit.name, currentLanguage)
        return fruit.copy(name = translatedName)
    }
} 