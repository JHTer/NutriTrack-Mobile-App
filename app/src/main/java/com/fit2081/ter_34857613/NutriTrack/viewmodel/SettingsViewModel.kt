package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.repository.SettingsRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserDetails
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserSessionManager
import com.fit2081.ter_34857613.NutriTrack.model.repository.AuthRepository
import com.fit2081.ter_34857613.NutriTrack.utils.LocaleHelper
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.math.BigInteger
import java.util.Locale

/**
 * ViewModel for settings screen that handles user details and authentication
 * following MVVM architecture
 */
class SettingsViewModel : ViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
    }
    
    // Repository to handle data operations
    private val repository = SettingsRepository()
    
    // State for user details
    var userDetails by mutableStateOf<UserDetails?>(null)
        private set
        
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
        
    // Clinician login error state - Note that this state is public
    var clinicianLoginError by mutableStateOf<String?>(null)
        private set

    // Edit mode state
    var isInEditMode by mutableStateOf(false)
        private set

    // Editable fields
    var editablePhoneNumber by mutableStateOf("")
        private set

    var editableUsername by mutableStateOf("")
        private set
        
    var editablePassword by mutableStateOf("")
        private set
        
    var confirmPassword by mutableStateOf("")
        private set
        
    var currentPassword by mutableStateOf("")
        private set

    // Success/error messages
    var updatePhoneSuccessMessage by mutableStateOf<String?>(null)
        private set
        
    var updateUsernameSuccessMessage by mutableStateOf<String?>(null)
        private set
        
    var updatePasswordSuccessMessage by mutableStateOf<String?>(null)
        private set
        
    var updateProfileErrorMessage by mutableStateOf<String?>(null)
        private set
        
    // Language settings
    var currentLanguage by mutableStateOf("")
        private set
        
    var showLanguageDialog by mutableStateOf(false)
        private set
        
    /**
     * Initialize the view model with the current language
     */
    fun initialize(context: Context) {
        currentLanguage = LocaleHelper.getStoredLocale(context)
    }
    
    /**
     * Show the language selection dialog
     */
    fun showLanguageSelector() {
        showLanguageDialog = true
    }
    
    /**
     * Hide the language selection dialog
     */
    fun hideLanguageSelector() {
        showLanguageDialog = false
    }
    
    /**
     * Change the app language
     * 
     * @param context Application context
     * @param languageCode The language code to change to
     * @return true if language was changed, false otherwise
     */
    fun changeAppLanguage(context: Context, languageCode: String): Boolean {
        if (currentLanguage == languageCode) {
            return false
        }
        
        try {
            currentLanguage = languageCode
            LocaleHelper.setLocale(context, languageCode)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error changing language: ${e.message}", e)
            return false
        }
    }

    /**
     * Load user details from repository
     * 
     * @param context Application context used to access the database
     * @param userId Unique identifier for the user
     */
    fun loadUserDetails(context: Context, userId: String) {
        // Reset states
        clearMessages()
        isLoading = true
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading user details for user: $userId")
                val details = repository.getUserDetails(context, userId)
                
                if (details != null) {
                    Log.d(TAG, "User details loaded successfully for user: $userId")
                    userDetails = details
                    // Initialize editable fields with current values
                    editablePhoneNumber = details.phoneNumber
                    editableUsername = details.name
                } else {
                    Log.e(TAG, "Failed to load user details for user: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user details: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Handle user logout
     * 
     * @param context Application context used to access the session manager
     * @param onLogoutSuccess Callback to be executed after successful logout
     */
    fun logout(context: Context, onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.logout()
                if (result) {
                    Log.d(TAG, "Logout successful")
                    
                    // Clear user session data
                    UserSessionManager.getInstance(context).clearUserSession()
                    
                    onLogoutSuccess()
                } else {
                    Log.e(TAG, "Logout failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout: ${e.message}", e)
            }
        }
    }
    
    /**
     * Verify clinician access key
     * 
     * @param key Key to validate
     * @param onSuccessfulValidation Callback for successful validation
     */
    fun verifyClinician(key: String, onSuccessfulValidation: () -> Unit) {
        clinicianLoginError = null
        
        if (repository.verifyClinicianKey(key)) {
            Log.d(TAG, "Clinician key validated successfully")
            onSuccessfulValidation()
        } else {
            Log.d(TAG, "Invalid clinician key provided")
            clinicianLoginError = "Invalid access key. Please try again."
        }
    }

    /**
     * Enter edit mode for the profile
     * Initializes all editable fields with current values
     */
    fun enterEditMode() {
        editableUsername = userDetails?.name ?: ""
        editablePhoneNumber = userDetails?.phoneNumber ?: ""
        editablePassword = ""
        confirmPassword = ""
        currentPassword = ""
        isInEditMode = true
        clearMessages()
    }

    /**
     * Exit edit mode without saving changes
     */
    fun exitEditMode() {
        // Reset all fields to original values
        userDetails?.let {
            editableUsername = it.name
            editablePhoneNumber = it.phoneNumber
        }
        editablePassword = ""
        confirmPassword = ""
        currentPassword = ""
        isInEditMode = false
        clearMessages()
    }

    /**
     * Clear all success and error messages
     */
    private fun clearMessages() {
        updatePhoneSuccessMessage = null
        updateUsernameSuccessMessage = null
        updatePasswordSuccessMessage = null
        updateProfileErrorMessage = null
    }

    /**
     * Updates the editable username state
     */
    fun onUsernameChange(newUsername: String) {
        editableUsername = newUsername
        updateUsernameSuccessMessage = null
    }

    /**
     * Updates the editable phone number state
     */
    fun onPhoneNumberChange(newNumber: String) {
        editablePhoneNumber = newNumber
        updatePhoneSuccessMessage = null
    }

    /**
     * Updates the editable password state
     */
    fun onPasswordChange(newPassword: String) {
        editablePassword = newPassword
        updatePasswordSuccessMessage = null
    }

    /**
     * Updates the confirm password state
     */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    /**
     * Updates the current password state
     */
    fun onCurrentPasswordChange(newCurrentPassword: String) {
        currentPassword = newCurrentPassword
    }

    /**
     * Save all profile changes
     */
    fun saveProfileChanges(context: Context, userId: String) {
        if (editableUsername.isBlank()) {
            updateProfileErrorMessage = "Username cannot be empty"
            return
        }

        clearMessages()
        isLoading = true

        // Check if password is being changed
        val passwordToUpdate = if (editablePassword.isNotBlank()) {
            // Validate password if it's being changed
            if (!validatePassword()) {
                isLoading = false
                return
            }
            AuthRepository.hashPassword(editablePassword)
        } else {
            null
        }

        viewModelScope.launch {
            try {
                val success = repository.updateUserProfile(
                    context,
                    userId,
                    editableUsername,
                    editablePhoneNumber,
                    passwordToUpdate
                )

                if (success) {
                    if (editableUsername != userDetails?.name) {
                        updateUsernameSuccessMessage = "Username updated successfully!"
                    }
                    if (editablePhoneNumber != userDetails?.phoneNumber) {
                    updatePhoneSuccessMessage = "Phone number updated successfully!"
                    }
                    if (passwordToUpdate != null) {
                        updatePasswordSuccessMessage = "Password updated successfully!"
                    }
                    
                    // Reload user details to confirm changes
                    loadUserDetails(context, userId)
                    
                    // Exit edit mode on success
                    isInEditMode = false
                } else {
                    updateProfileErrorMessage = "Failed to update profile. Please try again."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile: ${e.message}", e)
                updateProfileErrorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Validate the password against requirements
     */
    private fun validatePassword(): Boolean {
        if (editablePassword.length < 8) {
            updateProfileErrorMessage = "Password must be at least 8 characters"
            return false
        }
        
        if (!editablePassword.any { it.isUpperCase() }) {
            updateProfileErrorMessage = "Password must contain at least one uppercase letter"
            return false
        }
        
        if (!editablePassword.any { it.isDigit() }) {
            updateProfileErrorMessage = "Password must contain at least one number"
            return false
        }
        
        if (!editablePassword.any { !it.isLetterOrDigit() }) {
            updateProfileErrorMessage = "Password must contain at least one special character"
            return false
        }
        
        if (editablePassword != confirmPassword) {
            updateProfileErrorMessage = "Passwords do not match"
            return false
        }
        
        return true
    }

    /**
     * Check if a password requirement is met
     */
    fun isPasswordRequirementMet(requirement: String): Boolean {
        return when (requirement) {
            "At least 8 characters" -> editablePassword.length >= 8
            "At least one uppercase letter" -> editablePassword.any { it.isUpperCase() }
            "At least one number" -> editablePassword.any { it.isDigit() }
            "At least one special character" -> editablePassword.any { !it.isLetterOrDigit() }
            "Passwords match" -> editablePassword == confirmPassword && editablePassword.isNotEmpty()
            else -> false
        }
    }
} 