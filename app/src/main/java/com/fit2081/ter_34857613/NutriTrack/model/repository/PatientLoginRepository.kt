package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for patient login and initial database setup.
 *
 * This class handles loading user IDs for login selection, validating credentials against
 * the database, and managing the initial data import from CSV to the Room database
 * by interacting with SharedPreferences flags and the [NutriTrackDatabase] instance.
 * All credential validation is performed against the database, not the original CSV data.
 */
class PatientLoginRepository {
    
    /**
     * Companion object for [PatientLoginRepository].
     * Contains constants for logging, SharedPreferences names, and keys.
     */
    companion object {
        private const val TAG = "PatientLoginRepo"
        private const val PREF_NAME = "nutritrack_prefs"
        private const val PREF_IS_FIRST_LAUNCH = "is_first_launch"
    }
    
    /**
     * Loads a list of all available user IDs from the database.
     *
     * This method queries the [PatientDao] to retrieve all stored [Patient] records
     * and then extracts their `userId`s into a list.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used to access the database.
     * @return A list of user ID strings retrieved from the database. Returns an empty list
     *         if no users are found or if an error occurs during database access.
     */
    suspend fun loadUserIds(context: Context): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to load user IDs from database (not from CSV)")
            val patientDao = NutriTrackDatabase.getDatabase(context).patientDao()
            val patients = patientDao.getAllPatientsSync()
            Log.d(TAG, "Loaded ${patients.size} patients from database")
            
            // Debug logging for each patient found
            patients.forEach { patient ->
                Log.d(TAG, "Patient found in database - ID: ${patient.userId}, Phone: ${patient.phoneNumber}")
            }
            
            return@withContext patients.map { it.userId }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user IDs from database: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Forces the re-initialization of the database, primarily to trigger the CSV data import process.
     *
     * This method achieves re-initialization by:
     * 1. Setting a SharedPreferences flag (`PREF_IS_FIRST_LAUNCH`) to true, which the
     *    [NutriTrackDatabase] checks upon creation to determine if data import is needed.
     * 2. Attempting to clear the existing singleton instance of the [NutriTrackDatabase] using reflection.
     *    This ensures that the next call to `NutriTrackDatabase.getDatabase()` will create a new
     *    instance and re-evaluate the first launch flag.
     * 3. Calling `NutriTrackDatabase.getDatabase()` to trigger the new instance creation and potential data import.
     *
     * Note: This method is intended for scenarios where a fresh import from CSV is required, such as
     * development, testing, or data reset functionality.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used for SharedPreferences and database access.
     */
    suspend fun initializeDatabase(context: Context) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to force database initialization (CSV data import)")
            
            // Reset first launch preference to trigger CSV import
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val currentValue = prefs.getBoolean(PREF_IS_FIRST_LAUNCH, true)
            Log.d(TAG, "Current first launch flag: $currentValue")
            
            prefs.edit().putBoolean(PREF_IS_FIRST_LAUNCH, true).apply()
            Log.d(TAG, "Reset first launch flag to true to trigger CSV data import to database")
            
            // Clear the database instance to force rebuild
            try {
                val databaseField = NutriTrackDatabase::class.java.getDeclaredField("INSTANCE")
                databaseField.isAccessible = true
                databaseField.set(null, null)
                Log.d(TAG, "Cleared database instance to force fresh initialization")
            } catch (e: Exception) {
                Log.e(TAG, "Could not clear database instance: ${e.message}")
            }
            
            // Get database instance to trigger initialization
            val database = NutriTrackDatabase.getDatabase(context)
            Log.d(TAG, "Forced database initialization - triggered CSV data import")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database from CSV: ${e.message}", e)
        }
    }
    
    /**
     * Validates user login credentials (user ID and phone number) against the database.
     *
     * Authentication is performed solely based on records stored in the Room database ([PatientDao]),
     * not against the original CSV file.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used to access the database.
     * @param userId The user ID entered by the user for login.
     * @param phoneNumber The phone number entered by the user for login.
     * @return `true` if a [Patient] record exists in the database matching both the provided
     *         `userId` and `phoneNumber`. Returns `false` if credentials are invalid, if fields are empty,
     *         or if a database error occurs.
     */
    suspend fun validateCredentials(context: Context, userId: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Basic validation for empty fields
            if (userId.isEmpty() || phoneNumber.isEmpty()) {
                Log.d(TAG, "Validation failed: userId or phoneNumber is empty")
                return@withContext false
            }
            
            Log.d(TAG, "Validating credentials using database for userId: $userId, phoneNumber: $phoneNumber")
            
            val patientDao = NutriTrackDatabase.getDatabase(context).patientDao()
            val patient = patientDao.getPatientByIdAndPhone(userId, phoneNumber)
            
            // Additional logging for debugging
            if (patient == null) {
                Log.d(TAG, "Database validation: No matching patient found for userId: $userId, phoneNumber: $phoneNumber")
                
                // For debugging only - check if the user exists with a different phone number
                val patientWithId = patientDao.getPatientById(userId) 
                if (patientWithId != null) {
                    Log.d(TAG, "Database check: Patient with ID $userId exists but has different phone number")
                } else {
                    Log.d(TAG, "Database check: No patient with ID $userId exists in database")
                }
                
                // Also try to list all patients for debugging
                val allPatients = patientDao.getAllPatientsSync()
                Log.d(TAG, "Database contains ${allPatients.size} patients")
                allPatients.take(5).forEach { 
                    Log.d(TAG, "Database sample patient: ID=${it.userId}, Phone=${it.phoneNumber}")
                }
            } else {
                Log.d(TAG, "Database validation successful: found patient with userId: $userId")
            }
            
            // Return true if patient was found with matching credentials
            return@withContext patient != null
        } catch (e: Exception) {
            Log.e(TAG, "Error during database validation: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Retrieves a specific patient's details from the database by their user ID.
     *
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] used to access the database.
     * @param userId The unique identifier of the patient to retrieve.
     * @return The [Patient] object if found, or `null` if no patient with the given `userId` exists
     *         or if a database error occurs.
     */
    suspend fun getPatientById(context: Context, userId: String): Patient? = withContext(Dispatchers.IO) {
        val patientDao = NutriTrackDatabase.getDatabase(context).patientDao()
        return@withContext patientDao.getPatientById(userId)
    }
} 