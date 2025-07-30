package com.fit2081.ter_34857613.NutriTrack.model.database.Entity

import android.content.Context
import android.util.Log

/**
 * Singleton manager class to track database initialization status
 * Used to determine if CSV data needs to be loaded on app start
 */
class DatabaseInitManager private constructor(context: Context) {
    // Store application context to avoid memory leaks
    private val appContext = context.applicationContext
    
    companion object {
        private const val TAG = "DatabaseInitManager"
        private const val PREFS_NAME = "nutritrack_db_prefs"
        private const val DB_INITIALIZED_KEY = "db_initialized"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: DatabaseInitManager? = null
        
        /**
         * Get singleton instance of DatabaseInitManager
         * @param context Application or activity context (will be converted to application context)
         * @return The singleton instance of DatabaseInitManager
         */
        fun getInstance(context: Context): DatabaseInitManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseInitManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Check if database has been initialized with CSV data
     * @return true if database has been initialized, false otherwise
     */
    fun isInitialized(): Boolean {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initialized = prefs.getBoolean(DB_INITIALIZED_KEY, false)
        val instanceInfo = System.identityHashCode(this).toString(16)
        Log.d(TAG, "INIT CHECK [instance: $instanceInfo]: Database initialization status: ${if (initialized) "INITIALIZED ✅" else "NOT INITIALIZED ❌"}")
        return initialized
    }
    
    /**
     * Mark database as initialized after CSV data has been loaded
     * Uses commit() for immediate synchronous writes to ensure value is saved
     */
    fun markAsInitialized() {
        val instanceInfo = System.identityHashCode(this).toString(16)
        Log.d(TAG, "INIT [instance: $instanceInfo]: Marking database as INITIALIZED")
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val result = prefs.edit().putBoolean(DB_INITIALIZED_KEY, true).commit()
        Log.d(TAG, "INIT [instance: $instanceInfo]: SharedPreferences commit result: $result")
    }
    
    /**
     * Reset initialization status (for testing purposes)
     * Uses commit() for immediate synchronous writes to ensure value is saved
     */
    fun resetInitializationStatus() {
        val instanceInfo = System.identityHashCode(this).toString(16)
        Log.w(TAG, "INIT RESET [instance: $instanceInfo]: Database initialization status RESET to NOT INITIALIZED")
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val result = prefs.edit().putBoolean(DB_INITIALIZED_KEY, false).commit()
        Log.w(TAG, "INIT RESET [instance: $instanceInfo]: SharedPreferences commit result: $result")
    }

} 