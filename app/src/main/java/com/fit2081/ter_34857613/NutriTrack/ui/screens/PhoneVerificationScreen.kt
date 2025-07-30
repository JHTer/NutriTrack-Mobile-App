package com.fit2081.ter_34857613.NutriTrack.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.ui.components.*
import com.fit2081.ter_34857613.NutriTrack.viewmodel.AuthViewModel

private const val TAG = "PhoneVerificationScreen"

/**
 * Composable function for the Phone Verification screen.
 *
 * This is the first step in an account recovery or initial setup process where the user
 * needs to verify their identity by providing their pre-registered User ID and phone number.
 * It utilizes an [AuthViewModel] to load available User IDs and to perform the verification logic.
 *
 * The screen features:
 * - A dropdown to select the User ID from a list of known IDs.
 * - An input field for the phone number.
 * - Validation for both fields.
 * - An error display area for feedback from the ViewModel or local validation.
 * - A "Continue" button to submit the information for verification.
 *
 * Upon successful verification, it calls [onVerificationSuccess] with the `userId` and `phoneNumber`.
 * It also provides a navigation option back to the login screen via [onNavigateToLogin].
 *
 * @param onVerificationSuccess Callback function invoked when the user's identity is successfully verified.
 *                              It passes the verified `userId` and `phoneNumber`.
 * @param onNavigateToLogin Callback function to navigate back to the login screen.
 * @param viewModel The [AuthViewModel] instance used for authentication logic and data loading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneVerificationScreen(
    onVerificationSuccess: (userId: String, phoneNumber: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Form state
    var userId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    // Local loading state
    var isFormSubmitting by remember { mutableStateOf(false) }
    
    // Validation errors
    var userIdError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    // Track data loading state from ViewModel
    var userIdOptions by remember { mutableStateOf(emptyList<String>()) }
    
    // Initialize data loading
    LaunchedEffect(Unit) {
        Log.d(TAG, "LaunchedEffect: Loading user IDs")
        userIdOptions = viewModel.loadUserIds(context, forceReload = true)
        Log.d(TAG, "Loaded ${userIdOptions.size} user IDs")
    }
    
    // Validation function
    val validateForm = {
        var isValid = true
        
        // Validate user ID
        if (userId.isBlank()) {
            userIdError = "User ID is required"
            isValid = false
        } else {
            userIdError = null
        }
        
        // Validate phone number
        if (phoneNumber.isBlank()) {
            phoneError = "Phone number is required"
            isValid = false
        } else {
            phoneError = null
        }
        
        isValid
    }
    
    // Handle verification submission
    val handleVerification = {
        if (validateForm()) {
            isFormSubmitting = true
            viewModel.clearErrors()
            
            // Debug log
            Log.d(TAG, "Verifying user: $userId with phone: $phoneNumber")
            
            // Verify user using AuthViewModel
            viewModel.verifyUserIdentity(
                context,
                userId,
                phoneNumber,
                onSuccess = {
                    Log.d(TAG, "Verification successful")
                    isFormSubmitting = false
                    Log.d(TAG, "Passing to next screen: userId='$userId', phoneNumber='$phoneNumber'")
                    onVerificationSuccess(userId, phoneNumber)
                },
                onError = { errorMessage ->
                    Log.d(TAG, "Verification error: $errorMessage")
                    isFormSubmitting = false
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Identity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to login"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthCard(
                title = "Account Verification",
                subtitle = "Please verify your identity to continue"
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Display form error if any
                if (viewModel.verificationError != null) {
                    ErrorMessage(
                        message = viewModel.verificationError!!,
                        icon = Icons.Filled.Error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Loading indicator when initially loading user IDs
                if (viewModel.isLoading && userIdOptions.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Loading user IDs...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    // User ID Dropdown
                    DropdownSelector(
                        value = userId,
                        options = userIdOptions,
                        onSelectionChanged = { 
                            Log.d(TAG, "Selected user ID: $it")
                            userId = it 
                        },
                        label = "Select User ID",
                        isError = userIdError != null,
                        errorMessage = userIdError
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone Number Input
                    AuthTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone Number",
                        isError = phoneError != null,
                        errorMessage = phoneError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                handleVerification()
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Verify Button
                    AuthButton(
                        text = "Continue",
                        onClick = handleVerification,
                        isLoading = isFormSubmitting || viewModel.isLoading
                    )
                }
            }
        }
    }
} 