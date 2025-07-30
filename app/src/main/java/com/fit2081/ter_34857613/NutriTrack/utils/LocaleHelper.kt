package com.fit2081.ter_34857613.NutriTrack.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fit2081.ter_34857613.NutriTrack.MainActivity
import java.util.Locale

/**
 * CompositionLocal to provide the current locale throughout the app
 */
val LocalAppLocale = compositionLocalOf { Locale.getDefault() }

/**
 * Helper class for managing localization in the app
 */
object LocaleHelper {
    private const val PREF_NAME = "language_prefs"
    private const val SELECTED_LANGUAGE = "selected_language"
    
    /**
     * Set the locale for the given context
     */
    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguagePreference(context, languageCode)
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * Get the stored locale from preferences
     */
    fun getStoredLocale(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, Locale.getDefault().language) ?: Locale.getDefault().language
    }
    
    /**
     * Save the selected language to preferences
     */
    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Restart the activity with the new locale
     */
    fun restartApp(context: Context) {
        // Create a new intent with proper flags to clear the activity stack
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK or
                       Intent.FLAG_ACTIVITY_CLEAR_TOP)
        
        // Add a special flag to indicate this is a language change restart
        intent.putExtra("LANGUAGE_CHANGE", true)
        
        // Start the new activity
        context.startActivity(intent)
        
        // If the context is an activity, finish it to prevent going back to it with back button
        if (context is Activity) {
            context.finish()
        }
    }
    
    /**
     * Get a display name for the given language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "ms" -> "Bahasa Melayu"
            "zh" -> "中文"
            "ja" -> "日本語"
            "fr" -> "Français"
            else -> "English"
        }
    }
}

/**
 * Composable to provide the current locale to the composition
 */
@Composable
fun LocaleProvider(
    language: String = Locale.getDefault().language,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Use the passed language parameter to force recomposition when it changes
    val locale = remember(language) { Locale(language) }
    
    CompositionLocalProvider(LocalAppLocale provides locale) {
        content()
    }
} 