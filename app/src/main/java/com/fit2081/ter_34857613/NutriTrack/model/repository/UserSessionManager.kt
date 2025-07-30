package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages user session state using SharedPreferences.
 *
 * This class provides a singleton instance to globally access and persist user session information,
 * such as the logged-in user's ID and their login status. It handles saving the session upon login
 * and clearing it upon logout.
 *
 * @constructor Creates a UserSessionManager. The constructor is private to enforce the singleton pattern.
 *              Use [UserSessionManager.getInstance] to obtain an instance.
 * @param context The application [Context], used to access [SharedPreferences].
 */
class UserSessionManager private constructor(context: Context) {
    /**
     * Companion object for [UserSessionManager].
     * Implements the singleton pattern for accessing the session manager and holds constants
     * for SharedPreferences keys and names.
     */
    companion object {
        private const val TAG = "UserSessionManager"
        private const val PREF_NAME = "NutriTrackUserSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        
        @Volatile
        private var instance: UserSessionManager? = null
        
        /**
         * Gets the singleton instance of [UserSessionManager].
         *
         * This method provides a thread-safe way to access the single instance of the session manager.
         * If the instance does not exist, it is created using the application context.
         *
         * @param context The application [Context]. It's recommended to use `context.applicationContext`
         *                to avoid potential memory leaks.
         * @return The singleton [UserSessionManager] instance.
         */
        fun getInstance(context: Context): UserSessionManager {
            return instance ?: synchronized(this) {
                instance ?: UserSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Saves the user's login session by storing their user ID and marking them as logged in
     * in SharedPreferences.
     *
     * @param userId The unique identifier of the user who has logged in.
     */
    fun saveUserLoginSession(userId: String) {
        Log.d(TAG, "Saving user session for userId: $userId")
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Checks if there is a user currently logged in.
     *
     * @return `true` if a user session is active (i.e., [KEY_IS_LOGGED_IN] is true), `false` otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Retrieves the user ID of the currently logged-in user.
     *
     * @return The stored user ID as a [String]. Returns an empty string if no user is logged in
     *         or if the user ID was not found (though this should not happen if [isUserLoggedIn] is true).
     */
    fun getUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, "") ?: ""
    }
    
    /**
     * Clears all stored user session data from SharedPreferences.
     * This is typically called during a logout process.
     */
    fun clearUserSession() {
        Log.d(TAG, "Clearing user session")
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }
} 