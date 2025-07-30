package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.model.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the authentication flow.
 * Handles user verification and account setup.
 */
class AuthViewModel : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
        
    // Error state
    var verificationError by mutableStateOf<String?>(null)
        private set
        
    var setupError by mutableStateOf<String?>(null)
        private set
        
    // User data
    private var userIds = listOf<String>()
    var dataLoaded by mutableStateOf(false)
        private set
    
    /**
     * Set error message for account setup
     */
    fun updateSetupError(message: String?) {
        setupError = message
    }
    
    /**
     * Load available user IDs from the database
     * 
     * @param context Application context used to access the database
     * @param forceReload Whether to force reload data even if it's already cached
     * @return List of user IDs retrieved from the data source
     */
    suspend fun loadUserIds(context: Context, forceReload: Boolean = false): List<String> {
        if (userIds.isEmpty() || forceReload) {
            isLoading = true
            try {
                // Initialize repository from context
                val repository = AuthRepository.create(context)
                
                // Load user IDs from repository
                userIds = repository.getAllUserIds()
                dataLoaded = true
                
                Log.d(TAG, "Loaded ${userIds.size} user IDs: ${userIds.joinToString()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user IDs: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
        return userIds
    }
    
    /**
     * Verify user identity by user ID and phone number
     * 
     * @param context Application context used to access the database
     * @param userId User ID to verify
     * @param phoneNumber Phone number to verify
     * @param onSuccess Callback for successful verification
     * @param onError Callback for verification error
     */
    fun verifyUserIdentity(
        context: Context,
        userId: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        verificationError = null
        isLoading = true
        
        viewModelScope.launch {
            try {
                // Initialize repository from context
                val repository = AuthRepository.create(context)
                
                // Verify credentials using the repository
                val patient = repository.verifyUserIdentity(userId, phoneNumber)
                
                if (patient != null) {
                    // Check if the user has already set a password (already registered)
                    val hasSetPassword = repository.hasUserSetPassword(userId)
                    
                    if (hasSetPassword) {
                        verificationError = "This account has already been registered. Please login instead."
                        isLoading = false
                        onError(verificationError!!)
                    } else {
                        isLoading = false
                        onSuccess()
                    }
                } else {
                    verificationError = "Invalid user ID or phone number. Please try again."
                    isLoading = false
                    onError(verificationError!!)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Verification error: ${e.message}", e)
                verificationError = e.message ?: "An unknown error occurred"
                isLoading = false
                onError(verificationError!!)
            }
        }
    }
    
    /**
     * Set up user account with username and password
     * 
     * @param context Application context used to access the database
     * @param userId User ID
     * @param username Username to set
     * @param password Password to set
     * @param onSuccess Callback for successful setup
     * @param onError Callback for setup error
     */
    fun setupUserAccount(
        context: Context,
        userId: String,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        setupError = null
        isLoading = true
        
        viewModelScope.launch {
            try {
                // Initialize repository from context
                val repository = AuthRepository.create(context)
                
                // Set up the user account
                val success = repository.setupUserAccount(userId, username, password)
                
                if (success) {
                    isLoading = false
                    onSuccess()
                } else {
                    setupError = "Failed to set up account. Please try again."
                    isLoading = false
                    onError(setupError!!)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Account setup error: ${e.message}", e)
                setupError = e.message ?: "An unknown error occurred"
                isLoading = false
                onError(setupError!!)
            }
        }
    }
    
    /**
     * Clear any authentication errors
     */
    fun clearErrors() {
        verificationError = null
        setupError = null
    }
} 