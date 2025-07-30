package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing user settings and preferences.
 * This class handles operations related to user account details, authentication (logout),
 * and verification of special access keys (e.g., clinician key).
 * It interacts with the database to retrieve and update user information.
 */
class SettingsRepository {
    /**
     * Companion object for [SettingsRepository].
     * Contains constants such as the logging tag.
     */
    companion object {
        private const val TAG = "SettingsRepository"
    }
    
    /**
     * Retrieves detailed information for a specific user from the database.
     *
     * This function fetches the patient record associated with the given `userId`
     * and maps it to a [UserDetails] object. If the patient's name is empty,
     * a fallback name "User [userId]" is used.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] for accessing the database.
     * @param userId The unique identifier of the user whose details are to be retrieved.
     * @return A [UserDetails] object containing the user's ID, name, phone number, and gender
     *         if found. Returns `null` if the user is not found or if an error occurs.
     */
    suspend fun getUserDetails(context: Context, userId: String): UserDetails? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading user details from database for user: $userId")
            
            // Get the database instance
            val database = NutriTrackDatabase.getDatabase(context)
            
            // Query the patient from the database
            val patient = database.patientDao().getPatientById(userId)
            
            if (patient != null) {
                Log.d(TAG, "Found user details in database for user: $userId")
                
                // Map the patient entity to UserDetails model
                return@withContext UserDetails(
                    userId = patient.userId,
                    name = patient.name.ifEmpty { "User $userId" }, // Fallback if name is empty
                    phoneNumber = patient.phoneNumber,
                    gender = patient.sex
                )
            } else {
                Log.d(TAG, "No user data found in database for user: $userId")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user details from database: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Simulates a user logout process.
     *
     * In a real application, this function would be responsible for clearing session tokens,
     * cookies, or any other authentication-related data stored locally.
     * Currently, it only logs the action and returns a success status.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @return `true` if the logout process is considered successful (currently always true),
     *         `false` if an error occurs (currently only if an exception is caught).
     */
    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "User logged out successfully")
            // In a real implementation, this would clear tokens, cookies, etc.
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Verifies a clinician access key against a predefined value.
     *
     * This method checks if the provided `key` matches the hardcoded clinician access key
     * ("dollar-entry-apples") as per application requirements.
     *
     * @param key The clinician access key string to verify.
     * @return `true` if the provided `key` is valid, `false` otherwise.
     */
    fun verifyClinicianKey(key: String): Boolean {
        // Simple key verification as specified in requirements
        return key == "dollar-entry-apples"
    }

    /**
     * Updates the user's display name in the database.
     *
     * This function retrieves the specified user, updates their name, and then persists
     * the changes back to the database via the [PatientRepository].
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] for database access.
     * @param userId The unique identifier of the user whose name is to be updated.
     * @param newName The new display name for the user.
     * @return `true` if the username was successfully updated, `false` if the user was not found
     *         or if a database error occurred.
     */
    suspend fun updateUserName(context: Context, userId: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating username for user: $userId")
            val database = NutriTrackDatabase.getDatabase(context)
            val patientRepository = PatientRepository(database.patientDao())
            
            val patient = patientRepository.getPatient(userId)
            if (patient != null) {
                // Create updated patient with new name
                val updatedPatient = patient.copy(name = newName)
                patientRepository.update(updatedPatient)
                Log.d(TAG, "Username updated successfully for user: $userId")
                return@withContext true
            } else {
                Log.d(TAG, "Failed to update username for user: $userId, user not found")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating username: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Updates the user's password in the database.
     *
     * This function retrieves the user, updates their password field with the `newPassword`
     * (which should ideally be pre-hashed), and saves the changes using [PatientRepository].
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] for database access.
     * @param userId The unique identifier of the user whose password is to be updated.
     * @param newPassword The new password (preferably hashed) to set for the user.
     * @return `true` if the password was successfully updated, `false` if the user was not found
     *         or if a database error occurred.
     */
    suspend fun updateUserPassword(context: Context, userId: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating password for user: $userId")
            val database = NutriTrackDatabase.getDatabase(context)
            val patientRepository = PatientRepository(database.patientDao())
            
            val patient = patientRepository.getPatient(userId)
            if (patient != null) {
                // Create updated patient with new password
                val updatedPatient = patient.copy(password = newPassword)
                patientRepository.update(updatedPatient)
                Log.d(TAG, "Password updated successfully for user: $userId")
                return@withContext true
            } else {
                Log.d(TAG, "Failed to update password for user: $userId, user not found")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Updates the user's phone number in the database.
     *
     * This function directly calls the `updatePhoneNumber` method of the [PatientRepository]
     * to update the phone number for the specified user.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] for database access.
     * @param userId The unique identifier of the user whose phone number is to be updated.
     * @param newPhoneNumber The new phone number to set for the user.
     * @return `true` if the phone number was successfully updated, `false` if the user was not found
     *         or if a database error occurred (as reported by [PatientRepository]).
     */
    suspend fun updateUserPhoneNumber(context: Context, userId: String, newPhoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating phone number for user: $userId")
            val database = NutriTrackDatabase.getDatabase(context)
            val patientRepository = PatientRepository(database.patientDao())
            val success = patientRepository.updatePhoneNumber(userId, newPhoneNumber)
            if (success) {
                Log.d(TAG, "Phone number updated successfully for user: $userId")
            } else {
                Log.d(TAG, "Failed to update phone number for user: $userId, user not found or DB error.")
            }
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating phone number: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Updates multiple fields of a user's profile information in the database.
     *
     * This function allows for updating the user's name, phone number, and password simultaneously.
     * If any of the optional parameters (`name`, `phoneNumber`, `password`) are `null`,
     * the corresponding field for the user will not be changed.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param context The application [Context] for database access.
     * @param userId The unique identifier of the user whose profile is to be updated.
     * @param name An optional new display name for the user. If `null`, the name is not updated.
     * @param phoneNumber An optional new phone number for the user. If `null`, the phone number is not updated.
     * @param password An optional new password (preferably hashed) for the user. If `null`, the password is not updated.
     * @return `true` if the profile was successfully updated, `false` if the user was not found
     *         or if a database error occurred.
     */
    suspend fun updateUserProfile(
        context: Context, 
        userId: String, 
        name: String? = null, 
        phoneNumber: String? = null, 
        password: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating profile for user: $userId")
            val database = NutriTrackDatabase.getDatabase(context)
            val patientRepository = PatientRepository(database.patientDao())
            
            val patient = patientRepository.getPatient(userId)
            if (patient != null) {
                // Create updated patient with new information
                val updatedPatient = patient.copy(
                    name = name ?: patient.name,
                    phoneNumber = phoneNumber ?: patient.phoneNumber,
                    password = password ?: patient.password
                )
                patientRepository.update(updatedPatient)
                Log.d(TAG, "Profile updated successfully for user: $userId")
                return@withContext true
            } else {
                Log.d(TAG, "Failed to update profile for user: $userId, user not found")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile: ${e.message}", e)
            return@withContext false
        }
    }
}

/**
 * Data class representing a summarized view of user details, often used in settings or profile screens.
 *
 * @property userId The unique identifier for the user.
 * @property name The user's display name. This may include a fallback if the original name is empty.
 * @property phoneNumber The user's registered phone number.
 * @property gender The user's gender or sex (e.g., "Male", "Female").
 */
data class UserDetails(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val gender: String
) 