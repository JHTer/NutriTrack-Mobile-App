package com.fit2081.ter_34857613.NutriTrack.model.repository

import android.content.Context
import android.util.Log
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.PatientDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.math.BigInteger

/**
 * Repository responsible for authentication-related operations.
 * This includes user login, identity verification, password management (hashing, setup, reset),
 * and retrieval of user account information from the database via [PatientDao].
 *
 * @property patientDao The Data Access Object for [Patient] entities, used for database interactions.
 */
class AuthRepository(private val patientDao: PatientDao) {
    
    /**
     * Companion object for [AuthRepository].
     * Contains constants, a factory method for creating repository instances,
     * and a utility function for password hashing.
     */
    companion object {
        private const val TAG = "AuthRepository"
        
        /**
         * Factory method to create an instance of [AuthRepository].
         *
         * This method initializes the [NutriTrackDatabase] to obtain a [PatientDao]
         * instance, which is then provided to the [AuthRepository] constructor.
         *
         * @param context The application [Context] required to get the database instance.
         * @return A new instance of [AuthRepository].
         */
        fun create(context: Context): AuthRepository {
            Log.d(TAG, "AuthRepository.create called. Attempting to get database.")
            val database = NutriTrackDatabase.getDatabase(context)
            return AuthRepository(database.patientDao())
        }
        
        /**
         * Hashes a given password string using the SHA-256 algorithm.
         *
         * The resulting hash is a hexadecimal string. If the hashing process fails,
         * an error is logged and a placeholder error string is returned.
         *
         * @param password The plain-text password string to be hashed.
         * @return A SHA-256 hashed hexadecimal string of the password, or "ERROR_HASHING_PASSWORD"
         *         if an exception occurs during hashing.
         */
        fun hashPassword(password: String): String {
            try {
                val md = MessageDigest.getInstance("SHA-256")
                val messageDigest = md.digest(password.toByteArray())
                val no = BigInteger(1, messageDigest)
                var hashText = no.toString(16)
                
                // Add preceding 0s to ensure 32 byte length
                while (hashText.length < 32) {
                    hashText = "0$hashText"
                }
                
                return hashText
            } catch (e: Exception) {
                Log.e(TAG, "Error hashing password: ${e.message}", e)
                // In case of error, return a placeholder that's clearly invalid
                return "ERROR_HASHING_PASSWORD"
            }
        }
    }
    
    /**
     * Retrieves a list of all users (patients) from the database.
     *
     * This function fetches all [Patient] records synchronously from the [PatientDao].
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @return A list of all [Patient] entities stored in the database.
     */
    suspend fun getAllUsers(): List<Patient> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllUsers called")
        val patients = patientDao.getAllPatientsSync()
        Log.d(TAG, "getAllUsers: Found ${patients.size} patients from DAO.")
        return@withContext patients
    }
    
    /**
     * Retrieves a list of all user IDs from the database.
     *
     * This function first fetches all [Patient] records and then maps them to a list
     * of their `userId` strings.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @return A list of all unique user ID strings present in the database.
     */
    suspend fun getAllUserIds(): List<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllUserIds called")
        val patients = patientDao.getAllPatientsSync()
        Log.d(TAG, "getAllUserIds: Found ${patients.size} patients from DAO before mapping.")
        return@withContext patients.map { it.userId }
    }
    
    /**
     * Verifies a user's identity based on their user ID and phone number.
     *
     * This function queries the database for a patient matching both the provided `userId`
     * and `phoneNumber`.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param userId The user ID to verify.
     * @param phoneNumber The phone number to verify against the user ID.
     * @return The [Patient] object if both `userId` and `phoneNumber` match an existing record.
     *         Returns `null` if no matching user is found or if an error occurs.
     */
    suspend fun verifyUserIdentity(userId: String, phoneNumber: String): Patient? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Verifying user identity: $userId with phone: $phoneNumber")
            return@withContext patientDao.getPatientByIdAndPhone(userId, phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying user identity: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Authenticates a user based on their user ID and password.
     *
     * The provided `password` is first hashed using the [hashPassword] method,
     * and then the database is queried for a patient matching the `userId` and the hashed password.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param userId The user ID for authentication.
     * @param password The plain-text password for authentication.
     * @return The [Patient] object if the `userId` and hashed password match an existing record.
     *         Returns `null` if authentication fails or if an error occurs.
     */
    suspend fun authenticateUser(userId: String, password: String): Patient? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Authenticating user: $userId")
            // Hash the password before comparing with stored password
            val hashedPassword = hashPassword(password)
            Log.d(TAG, "Authenticating with hashed password (first 5 chars): ${hashedPassword.take(5)}...")
            return@withContext patientDao.getPatientByIdAndPassword(userId, hashedPassword)
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating user: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Sets up a user account by updating the name and password for a given user ID.
     *
     * This function retrieves the patient by `userId`, hashes the provided `password`,
     * and then updates the patient's record with the new name and hashed password.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param userId The user ID for the account to be set up.
     * @param name The name to set for the user.
     * @param password The plain-text password to be hashed and set for the user.
     * @return `true` if the account setup (name and password update) was successful.
     *         `false` if the user was not found or if an error occurred during the update.
     */
    suspend fun setupUserAccount(userId: String, name: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Setting up user account for: $userId")
            val patient = patientDao.getPatientById(userId)
            
            if (patient != null) {
                // Hash the password before storing
                val hashedPassword = hashPassword(password)
                Log.d(TAG, "Password hashed for account setup (first 5 chars): ${hashedPassword.take(5)}...")
                
                // Create updated patient with new name and password
                val updatedPatient = patient.copy(name = name, password = hashedPassword)
                patientDao.update(updatedPatient)
                Log.d(TAG, "User account setup successful for: $userId")
                return@withContext true
            } else {
                Log.e(TAG, "User account setup failed: User not found with ID: $userId")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during user account setup: ${e.message}", e)
            e.printStackTrace() // Print full stack trace
            return@withContext false
        }
    }
    
    /**
     * Checks if a user has completed their account setup by setting a password.
     *
     * This function queries the database to determine if the password field for the given `userId`
     * is non-null and not empty, indicating that a password has been set.
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param userId The user ID to check for password setup.
     * @return `true` if the user has a password set, `false` otherwise or if an error occurs.
     */
    suspend fun hasUserSetPassword(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            return@withContext patientDao.hasSetPassword(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user has set password: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Resets a user's password.
     *
     * This function retrieves the patient by `userId`. If found, it updates the patient's
     * password with the `newPassword`. The `newPassword` is hashed before storing, unless it
     * appears to be already hashed (64-character hex string).
     * The operation is performed on an IO-optimized dispatcher.
     *
     * @param userId The user ID for whom the password will be reset.
     * @param newPassword The new plain-text (or pre-hashed) password to set.
     * @return `true` if the password reset was successful. `false` if the user was not found
     *         or if an error occurred during the update.
     */
    suspend fun resetPassword(userId: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Resetting password for user: $userId")
            val patient = patientDao.getPatientById(userId)
            
            if (patient != null) {
                Log.d(TAG, "Found patient for password reset: $userId")
                
                // Check if the password is already hashed (it should be in most cases,
                // but this adds a safety check)
                val passwordToUse = if (newPassword.length == 64 && newPassword.all { it.isLetterOrDigit() }) {
                    // If password is already 64 chars hex (typical for SHA-256), assume it's hashed
                    Log.d(TAG, "Password appears to be pre-hashed, using as is")
                    newPassword
                } else {
                    // Otherwise hash it
                    Log.d(TAG, "Hashing password for reset")
                    hashPassword(newPassword)
                }
                
                // Create updated patient with new password
                val updatedPatient = patient.copy(password = passwordToUse)
                patientDao.update(updatedPatient)
                Log.d(TAG, "Password reset successful for: $userId with password hash (first 5 chars): ${passwordToUse.take(5)}...")
                return@withContext true
            } else {
                Log.e(TAG, "Password reset failed: User not found with ID: $userId")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during password reset: ${e.message}", e)
            e.printStackTrace() // Print full stack trace
            return@withContext false
        }
    }
} 