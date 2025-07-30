package com.fit2081.ter_34857613.NutriTrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Error
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
import com.fit2081.ter_34857613.NutriTrack.viewmodel.ForgotPasswordViewModel
import com.fit2081.ter_34857613.NutriTrack.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Forgot password screen composable that allows users to reset their password
 * 
 * @param onPasswordResetSuccess Callback function invoked when password reset is successful
 * @param onNavigateToLogin Callback function invoked when user wants to navigate back to login
 * @param viewModel ForgotPasswordViewModel instance for handling password reset logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onPasswordResetSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    
    // Form state
    var userId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // User ID list state
    var userIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingUserIds by remember { mutableStateOf(true) }
    
    // Load user IDs on first composition
    LaunchedEffect(Unit) {
        isLoadingUserIds = true
        userIds = authViewModel.loadUserIds(context)
        isLoadingUserIds = false
    }
    
    // Verification state
    // Use the ViewModel's isVerified state
    val isVerified = viewModel.isVerified
    
    // Validation errors
    var userIdError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Overall form error
    var formError by remember { mutableStateOf<String?>(null) }
    
    // Loading state from ViewModel
    val isLoading = viewModel.isLoading
    
    // Use effects to update form error from ViewModel errors
    LaunchedEffect(viewModel.verificationError) {
        viewModel.verificationError?.let { formError = it }
    }
    
    LaunchedEffect(viewModel.resetError) {
        viewModel.resetError?.let { formError = it }
    }
    
    // Validate verification info
    val validateVerification = {
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
        } else if (phoneNumber.length < 10) {
            phoneError = "Please enter a valid phone number"
            isValid = false
        } else {
            phoneError = null
        }
        
        isValid
    }
    
    // Validate reset form
    val validateReset = {
        var isValid = true
        
        // Validate password
        if (newPassword.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (newPassword.length < 8) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        } else if (!newPassword.any { it.isUpperCase() }) {
            passwordError = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!newPassword.any { it.isDigit() }) {
            passwordError = "Password must contain at least one number"
            isValid = false
        } else if (!newPassword.any { !it.isLetterOrDigit() }) {
            passwordError = "Password must contain at least one special character"
            isValid = false
        } else {
            passwordError = null
        }
        
        // Validate confirm password
        if (confirmPassword != newPassword) {
            confirmPasswordError = "Passwords don't match"
            isValid = false
        } else {
            confirmPasswordError = null
        }
        
        isValid
    }
    
    // Handle verification submission
    val handleVerify = {
        formError = null
        if (validateVerification()) {
            // Call verifyIdentity on the ViewModel
            viewModel.verifyIdentity(
                context = context,
                userId = userId,
                phoneNumber = phoneNumber,
                onSuccess = {
                    // Success is handled internally by ViewModel updating isVerified
                },
                onError = { error ->
                    formError = error
                }
            )
        }
    }
    
    // Handle password reset submission
    val handleReset = {
        formError = null
        if (validateReset()) {
            // Call resetPassword on the ViewModel
            viewModel.resetPassword(
                context = context,
                newPassword = newPassword,
                onSuccess = {
            onPasswordResetSuccess()
                },
                onError = { error ->
                    formError = error
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to login"
                        )
                    }
                }
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
            // First step: Verify identity
            if (!isVerified) {
                AuthCard(
                    title = "Verify Identity",
                    subtitle = "Enter your user ID and phone number to reset your password"
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display form error if any
                    if (formError != null) {
                        ErrorMessage(
                            message = formError!!,
                            icon = Icons.Outlined.Error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // User ID Dropdown
                    if (isLoadingUserIds) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        DropdownSelector(
                            value = userId,
                            options = userIds,
                            onSelectionChanged = { userId = it },
                            label = "User ID",
                            isError = userIdError != null,
                            errorMessage = userIdError
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone Number
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
                                handleVerify()
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Verify Button
                    AuthButton(
                        text = "Verify",
                        onClick = handleVerify,
                        isLoading = isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Back to login link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Remember your password?")
                        Spacer(modifier = Modifier.width(4.dp))
                        TextActionLink(
                            text = "Login",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            } 
            // Second step: Create new password
            else {
                AuthCard(
                    title = "Reset Password",
                    subtitle = "Create a new password for your account"
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display form error if any
                    if (formError != null) {
                        ErrorMessage(
                            message = formError!!,
                            icon = Icons.Outlined.Error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // New Password
                    PasswordTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password",
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Password requirements indicators
                    if (newPassword.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Password requirements:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // List of requirements
                            val requirements = listOf(
                                "At least 8 characters",
                                "At least one uppercase letter",
                                "At least one number",
                                "At least one special character"
                            )
                            
                            requirements.forEach { requirement ->
                                val isMet = isRequirementMet(requirement, newPassword)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Checkbox or indicator
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = if (isMet) MaterialTheme.colorScheme.primary 
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(2.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isMet) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Requirement met",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Requirement text
                                    Text(
                                        text = requirement,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMet) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                                handleReset()
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Reset Button
                    AuthButton(
                        text = "Reset Password",
                        onClick = handleReset,
                        isLoading = isLoading
                    )
                }
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