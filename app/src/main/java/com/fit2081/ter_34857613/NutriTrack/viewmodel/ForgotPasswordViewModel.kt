package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.repository.PatientRepository
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.NutriTrackDatabase
import com.fit2081.ter_34857613.NutriTrack.model.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the "Forgot Password" screen, responsible for handling the logic of user identity
 * verification and password reset.
 *
 * This ViewModel manages the UI state related to loading, errors, and verification status.
 * It interacts with [PatientRepository] and [AuthRepository] to verify user credentials
 * (User ID and phone number) and then to update the user's password in the database.
 *
 * Key functionalities include:
 * - Verifying user identity: Checks if the provided User ID and phone number match an existing,
 *   registered user who has already set up a password.
 * - Resetting the password: Allows a verified user to set a new password, which is then hashed
 *   and stored securely.
 * - Managing UI state: Exposes observable properties for loading indicators (`isLoading`),
 *   error messages (`verificationError`, `resetError`), and verification status (`isVerified`).
 */
class ForgotPasswordViewModel : ViewModel() {
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
    
    // UI state
    /** Indicates whether a long-running operation (verification or reset) is in progress. */
    var isLoading by mutableStateOf(false)
        private set
        
    /** Stores an error message related to the identity verification step, or `null` if no error. */
    var verificationError by mutableStateOf<String?>(null)
        private set
        
    /** Stores an error message related to the password reset step, or `null` if no error. */
    var resetError by mutableStateOf<String?>(null)
        private set
        
    /** Indicates whether the user's identity has been successfully verified. */
    var isVerified by mutableStateOf(false)
        private set
        
    // Store user info after verification
    /** Stores the [Patient] object of the user whose identity has been verified. `null` otherwise. */
    private var verifiedUser: Patient? = null
    
    /**
     * Verifies the user's identity using their User ID and phone number.
     *
     * It first checks if the credentials match an existing user and then ensures that the user
     * is not a new user (i.e., they have already set a password before and are eligible for a reset).
     * Updates [isLoading], [verificationError], and [isVerified] states.
     * Invokes [onSuccess] or [onError] callbacks based on the outcome.
     *
     * @param context The application context, used to access the database.
     * @param userId The User ID provided by the user for verification.
     * @param phoneNumber The phone number provided by the user for verification.
     * @param onSuccess Callback function invoked if verification is successful.
     * @param onError Callback function invoked if verification fails, providing an error message string.
     */
    fun verifyIdentity(
        context: Context,
        userId: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Reset errors
        verificationError = null
        isLoading = true
        
        viewModelScope.launch {
            try {
                // Get database access
                val database = NutriTrackDatabase.getDatabase(context)
                val repository = PatientRepository(database.patientDao())
                val authRepository = AuthRepository.create(context)
                
                // First check if the user exists and credentials are valid
                val user = authRepository.verifyUserIdentity(userId, phoneNumber)
                
                if (user == null) {
                    verificationError = "Invalid user ID or phone number. Please check your details."
                    isLoading = false
                    onError(verificationError!!)
                    return@launch
                }
                
                // Then check if the user has already set a password (not a new user)
                val hasSetPassword = authRepository.hasUserSetPassword(userId)
                
                if (!hasSetPassword) {
                    verificationError = "New users cannot reset their password. Please sign up first."
                    isLoading = false
                    onError(verificationError!!)
                    return@launch
                }
                
                // Store verified user for reset operation
                verifiedUser = user
                isVerified = true
                isLoading = false
                onSuccess()
                
            } catch (e: Exception) {
                // Handle errors
                Log.e(TAG, "Verification error: ${e.message}", e)
                verificationError = e.message ?: "An unknown error occurred"
                isLoading = false
                onError(verificationError!!)
            }
        }
    }
    
    /**
     * Resets the password for a previously verified user.
     *
     * This function should only be called after [verifyIdentity] has successfully completed.
     * It hashes the `newPassword` and updates the user's record in the database via [AuthRepository].
     * Updates [isLoading] and [resetError] states.
     * Invokes [onSuccess] or [onError] callbacks based on the outcome.
     *
     * @param context The application context, used to access the database.
     * @param newPassword The new password to be set for the user.
     * @param onSuccess Callback function invoked if the password reset is successful.
     * @param onError Callback function invoked if the password reset fails, providing an error message string.
     */
    fun resetPassword(
        context: Context,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Check if user is verified
        if (!isVerified || verifiedUser == null) {
            resetError = "User identity not verified"
            onError(resetError!!)
            return
        }
        
        // Reset errors
        resetError = null
        isLoading = true
        
        viewModelScope.launch {
            try {
                // Hash the password for security using the repository's hash function
                Log.d(TAG, "Hashing password for user: ${verifiedUser!!.userId}")
                // Use the repository's hash function for consistency
                val hashedPassword = AuthRepository.hashPassword(newPassword)
                Log.d(TAG, "Password hashed successfully, first 5 chars: ${hashedPassword.take(5)}...")
                
                // Update user password in database
                val authRepository = AuthRepository.create(context)
                Log.d(TAG, "Calling resetPassword on repository with userId: ${verifiedUser!!.userId}")
                val success = authRepository.resetPassword(verifiedUser!!.userId, hashedPassword)
                
                if (success) {
                    // Password reset successful
                    Log.d(TAG, "Password reset successful for user: ${verifiedUser!!.userId}")
                    isLoading = false
                    onSuccess()
                } else {
                    // Password reset failed
                    Log.e(TAG, "Password reset failed for user: ${verifiedUser!!.userId}")
                    resetError = "Failed to reset password. Please try again."
                    isLoading = false
                    onError(resetError!!)
                }
                
            } catch (e: Exception) {
                // Handle errors
                Log.e(TAG, "Password reset error: ${e.message}", e)
                e.printStackTrace() // Print full stack trace
                resetError = e.message ?: "An unknown error occurred"
                isLoading = false
                onError(resetError!!)
            }
        }
    }
    
    /**
     * Clears the current verification state.
     *
     * Resets [isVerified] to `false` and [verifiedUser] to `null`.
     * This is typically called when the user navigates away from the password reset flow
     * or if a new verification attempt is started.
     */
    fun clearVerification() {
        isVerified = false
        verifiedUser = null
    }
    
    /**
     * Clears any existing error messages for both verification and reset steps.
     *
     * Sets [verificationError] and [resetError] to `null`.
     * This is useful for clearing stale error messages when the user makes new input
     * or retries an operation.
     */
    fun clearErrors() {
        verificationError = null
        resetError = null
    }
} 