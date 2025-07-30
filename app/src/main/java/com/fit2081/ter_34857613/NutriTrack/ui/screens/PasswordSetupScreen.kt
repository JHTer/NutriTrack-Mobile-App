package com.fit2081.ter_34857613.NutriTrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Check
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
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import com.fit2081.ter_34857613.NutriTrack.ui.components.*
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.viewmodel.AuthViewModel

/**
 * Composable function for the Password Setup screen.
 *
 * This screen is typically the second step in an account creation or recovery flow, presented after
 * successful phone verification. It allows the user to create a new username and password for their account.
 * The `userId` and `phoneNumber` from the previous verification step are displayed for confirmation.
 *
 * The screen features:
 * - Input fields for Username, New Password, and Confirm Password.
 * - Real-time validation for username length and password complexity (strength requirements are displayed).
 * - An error display area for feedback from the ViewModel or local validation.
 * - A "Set Up Account" button to finalize the account creation with the new credentials.
 *
 * It uses an [AuthViewModel] to handle the account setup logic. Upon successful setup,
 * it invokes the [onSetupComplete] callback.
 *
 * @param userId The verified user ID from the previous step, displayed for confirmation.
 * @param phoneNumber The verified phone number from the previous step, displayed for confirmation.
 * @param onSetupComplete Callback function invoked when the account setup (username and password creation)
 *                        is successfully completed.
 * @param onNavigateBack Callback function to navigate back to the previous screen (e.g., phone verification).
 * @param viewModel The [AuthViewModel] instance used for authentication and account setup logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordSetupScreen(
    userId: String,
    phoneNumber: String,
    onSetupComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Log the values for debugging
    LaunchedEffect(Unit) {
        Log.d("PasswordSetupScreen", "Received userId: '$userId', phoneNumber: '$phoneNumber'")
    }
    
    // Form state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Validation errors
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Local loading state
    var isFormSubmitting by remember { mutableStateOf(false) }
    
    // Password requirements
    val passwordRequirements = listOf(
        "At least 8 characters",
        "At least one uppercase letter",
        "At least one number",
        "At least one special character"
    )
    
    // Show password requirements
    var showPasswordRequirements by remember { mutableStateOf(false) }
    
    // Validation function
    val validateForm = {
        var isValid = true
        
        // Validate username
        if (username.isBlank()) {
            usernameError = "Username is required"
            isValid = false
        } else if (username.length < 4) {
            usernameError = "Username must be at least 4 characters"
            isValid = false
        } else {
            usernameError = null
        }
        
        // Validate password
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        } else if (!password.any { it.isUpperCase() }) {
            passwordError = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!password.any { it.isDigit() }) {
            passwordError = "Password must contain at least one number"
            isValid = false
        } else if (!password.any { !it.isLetterOrDigit() }) {
            passwordError = "Password must contain at least one special character"
            isValid = false
        } else {
            passwordError = null
        }
        
        // Validate confirm password
        if (confirmPassword != password) {
            confirmPasswordError = "Passwords don't match"
            isValid = false
        } else {
            confirmPasswordError = null
        }
        
        isValid
    }
    
    // Handle setup submission
    val handleSetup = {
        if (validateForm()) {
            // Check if userId and phoneNumber are not empty
            if (userId.isBlank() || phoneNumber.isBlank()) {
                viewModel.updateSetupError("Invalid user information. Please restart the verification process.")
            } else {
                isFormSubmitting = true
                viewModel.clearErrors()
                
                // Set up user account using viewModel
                viewModel.setupUserAccount(
                    context = context,
                    userId = userId,
                    username = username,
                    password = password,
                    onSuccess = {
                        isFormSubmitting = false
                        onSetupComplete()
                    },
                    onError = { errorMessage ->
                        isFormSubmitting = false
                        viewModel.updateSetupError(errorMessage)
                    }
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to verification"
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
                title = "Set Up Your Account",
                subtitle = "Create a username and password"
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Display form error if any
                if (viewModel.setupError != null) {
                    ErrorMessage(
                        message = viewModel.setupError!!,
                        icon = Icons.Filled.Error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Display user ID and phone information
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Verified Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green40
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "User ID: $userId",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Phone Number: $phoneNumber",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Username
                AuthTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    isError = usernameError != null,
                    errorMessage = usernameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                // Username hint
                Text(
                    text = "Username must be at least 4 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, bottom = 8.dp)
                )
                
                // Password Field with requirements
                PasswordTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        showPasswordRequirements = true
                    },
                    label = "Password",
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                // Display password requirements
                if (showPasswordRequirements) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "Password requirements:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        passwordRequirements.forEach { requirement ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                // Replace the system checkbox with a custom implementation
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            color = if (isRequirementMet(requirement, password)) 
                                                MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(2.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRequirementMet(requirement, password)) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Requirement met",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = requirement,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isRequirementMet(requirement, password)) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Confirm Password
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    isError = confirmPasswordError != null,
                    errorMessage = confirmPasswordError,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            handleSetup()
                        }
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Setup Button
                AuthButton(
                    text = "Create Account",
                    onClick = handleSetup,
                    isLoading = isFormSubmitting || viewModel.isLoading
                )
            }
        }
    }
}

/**
 * Helper function to check if a password meets a specific requirement
 */
private fun isRequirementMet(requirement: String, password: String): Boolean {
    return when (requirement) {
        "At least 8 characters" -> password.length >= 8
        "At least one uppercase letter" -> password.any { it.isUpperCase() }
        "At least one number" -> password.any { it.isDigit() }
        "At least one special character" -> password.any { !it.isLetterOrDigit() }
        else -> false
    }
} 