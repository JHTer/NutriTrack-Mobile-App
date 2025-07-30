package com.fit2081.ter_34857613.NutriTrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.ter_34857613.NutriTrack.AppScreen
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserPreferencesRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.AuthRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for login screen that handles authentication logic and state management
 * following MVVM architecture principles
 */
class LoginViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    // State holders for user selections
    var selectedUserId by mutableStateOf("")
        private set
        
    var password by mutableStateOf("")
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set
        
    var showError by mutableStateOf(false)
        private set
        
    var expanded by mutableStateOf(false)
        private set
    
    // User ID list management
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIdsStateFlow: StateFlow<List<String>> = _userIds.asStateFlow()
    
    // Track if we've successfully loaded data
    private var dataLoaded = false
    
    // User preferences repository for questionnaire completion status
    private val userPreferencesRepository = UserPreferencesRepository()
    
    /**
     * Load user IDs from the database for dropdown selection
     * 
     * @param context Application context used to access the database
     * @param forceReload Whether to force reload data even if it's already cached
     */
    fun loadUserIds(context: Context, forceReload: Boolean = false) {
        Log.d(TAG, "LoginViewModel.loadUserIds called. forceReload: $forceReload, current userIds count: ${_userIds.value.size}")
        viewModelScope.launch {
            if ((_userIds.value.isEmpty() && !dataLoaded) || forceReload) {
                isLoading = true
                try {
                    // Initialize repository directly without waiting for basic data
                    Log.d(TAG, "Creating AuthRepository and loading user IDs")
                    val authRepository = AuthRepository.create(context)
                    val fetchedUserIds = authRepository.getAllUserIds()
                    _userIds.value = fetchedUserIds
                    dataLoaded = true
                    
                    Log.d(TAG, "Loaded ${_userIds.value.size} user IDs into StateFlow")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading user IDs: ${e.message}", e)
                    _userIds.value = emptyList()
                    dataLoaded = false
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    /**
     * Update the selected user ID
     * 
     * @param userId The ID selected by the user
     */
    fun updateSelectedUserId(userId: String) {
        selectedUserId = userId
    }
    
    /**
     * Update the password entered by the user
     * 
     * @param pass The password entered by the user
     */
    fun updatePassword(pass: String) {
        password = pass
    }
    
    /**
     * Reset error state
     */
    fun resetError() {
        showError = false
    }
    
    /**
     * Toggle the expanded state of the dropdown
     */
    fun toggleExpanded() {
        expanded = !expanded
    }
    
    /**
     * Validate login credentials and authenticate the user
     * 
     * @param context Application context used to access the database
     * @param onError Callback invoked when validation fails
     * @param onLoginSuccess Callback invoked when validation succeeds with userId and target screen
     */
    fun login(
        context: Context,
        onError: () -> Unit,
        onLoginSuccess: (String, AppScreen) -> Unit
    ) {
        isLoading = true
        showError = false
        
        viewModelScope.launch {
            try {
                // Initialize repository from context
                val authRepository = AuthRepository.create(context)
                
                // Authenticate user using the repository with password
                val patient = authRepository.authenticateUser(selectedUserId, password)
                
                if (patient != null) {
                    // Check if user has completed the questionnaire
                    val hasCompletedQuestionnaire = userPreferencesRepository.hasCompletedQuestionnaire(context, selectedUserId)
                    
                    // Determine which screen to navigate to
                    val targetScreen = if (hasCompletedQuestionnaire) {
                        AppScreen.HOME
                    } else {
                        AppScreen.QUESTIONNAIRE
                    }
                    
                    // Save user session for persistent login
                    val sessionManager = UserSessionManager.getInstance(context)
                    sessionManager.saveUserLoginSession(selectedUserId)
                    
                    isLoading = false
                    // Navigate to the appropriate screen
                    onLoginSuccess(selectedUserId, targetScreen)
                } else {
                    showError = true
                    isLoading = false
                    onError()
                }
            } catch (e: Exception) {
                // Handle potential database access errors
                Log.e(TAG, "Login error: ${e.message}", e)
                showError = true
                isLoading = false
                onError()
            }
        }
    }
}
