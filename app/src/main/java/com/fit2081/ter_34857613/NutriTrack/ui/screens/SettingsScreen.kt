package com.fit2081.ter_34857613.NutriTrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.AppScreen
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.ui.components.LanguageSelectionDialog
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.GreenGrey40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.LightGreen80
import com.fit2081.ter_34857613.NutriTrack.utils.LocaleHelper
import com.fit2081.ter_34857613.NutriTrack.viewmodel.SettingsViewModel
import androidx.compose.foundation.clickable

/**
 * Composable function for the application settings screen.
 *
 * This screen allows users to:
 * - View and edit their profile information (username, phone number).
 * - Change their password.
 * - Select the application language.
 * - Log out of the application.
 * - Access a clinician-specific login (via a dialog).
 *
 * The screen utilizes a [SettingsViewModel] to manage user details, input states, edit mode,
 * language changes, and interactions with backend services for updates and authentication.
 * It displays user information in a card format, with editable fields when in "edit mode".
 * Language selection is handled via a [LanguageSelectionDialog].
 *
 * @param userId The unique identifier of the currently logged-in user.
 * @param onNavigate Callback function to navigate to other [AppScreen]s (e.g., for clinician login success).
 * @param onLogout Callback function invoked when the user chooses to log out.
 * @param viewModel The [SettingsViewModel] instance for this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userId: String,
    onNavigate: (AppScreen) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // State for clinician login dialog
    var showClinicianDialog by remember { mutableStateOf(false) }
    var clinicianKey by remember { mutableStateOf("") }
    
    // State for password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    // Load user details when the component is first composed
    LaunchedEffect(userId) {
        viewModel.loadUserDetails(context, userId)
        viewModel.initialize(context)
    }
    
    // Language selection dialog
    LanguageSelectionDialog(
        showDialog = viewModel.showLanguageDialog,
        onDismiss = { viewModel.hideLanguageSelector() },
        onLanguageSelected = { languageCode ->
            // Apply language change
            if (viewModel.changeAppLanguage(context, languageCode)) {
                // Show a toast that the language is changing
                android.widget.Toast.makeText(
                    context,
                    "Changing language to ${LocaleHelper.getLanguageDisplayName(languageCode)}...",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Restart app to apply changes after a short delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    LocaleHelper.restartApp(context)
                }, 500) // 500ms delay to ensure the toast is visible
            }
            viewModel.hideLanguageSelector()
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Green40,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Box(
        modifier = Modifier
            .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Loading state
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green40)
                }
            }
            // Content state - show only when data is available or as fallback
            else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // User Profile Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Edit Section Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_information),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Green40,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                                // Edit icon inside card
                                if (!viewModel.isInEditMode) {
                                    IconButton(
                                        onClick = { viewModel.enterEditMode() },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                color = LightGreen80,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit),
                                            tint = Green40,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            // User Icon
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(LightGreen80),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Profile",
                                    modifier = Modifier.size(80.dp),
                                    tint = Green40
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Error message
                            AnimatedVisibility(
                                visible = viewModel.updateProfileErrorMessage != null,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                viewModel.updateProfileErrorMessage?.let { errorMessage ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                            Text(
                                            text = errorMessage,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            // User Information Fields
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                // Username Field
                                ProfileField(
                                    label = stringResource(R.string.username),
                                    icon = Icons.Default.Person,
                                    value = viewModel.userDetails?.name ?: stringResource(R.string.default_user) + " $userId",
                                    editable = viewModel.isInEditMode,
                                    editableValue = viewModel.editableUsername,
                                    onValueChange = { viewModel.onUsernameChange(it) },
                                    successMessage = viewModel.updateUsernameSuccessMessage
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Phone Number Field
                                ProfileField(
                                    label = stringResource(R.string.phone_number),
                                    icon = Icons.Default.Phone,
                                    value = viewModel.userDetails?.phoneNumber ?: stringResource(R.string.not_available),
                                    editable = viewModel.isInEditMode,
                                    editableValue = viewModel.editablePhoneNumber,
                                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                                    successMessage = viewModel.updatePhoneSuccessMessage
                                )
                                
                                // Password Fields (Only shown in edit mode)
                                AnimatedVisibility(
                                    visible = viewModel.isInEditMode,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = stringResource(R.string.change_password),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Green40,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // Password Field
                                        OutlinedTextField(
                                            value = viewModel.editablePassword,
                                            onValueChange = { viewModel.onPasswordChange(it) },
                                            label = { Text(stringResource(R.string.new_password)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Password"
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                                    )
                                                }
                                            },
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                            ),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Green40,
                                                unfocusedBorderColor = GreenGrey40,
                                                cursorColor = Green40
                                            )
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Confirm Password Field
                                            OutlinedTextField(
                                            value = viewModel.confirmPassword,
                                            onValueChange = { viewModel.onConfirmPasswordChange(it) },
                                            label = { Text(stringResource(R.string.confirm_password)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Confirm Password"
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                                    Icon(
                                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = if (confirmPasswordVisible) "Hide Password" else "Show Password"
                                                    )
                                                }
                                            },
                                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                            ),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Green40,
                                                    unfocusedBorderColor = GreenGrey40,
                                                    cursorColor = Green40
                                                )
                                            )
                                        
                                        // Success message for password change
                                        AnimatedVisibility(
                                            visible = viewModel.updatePasswordSuccessMessage != null,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            viewModel.updatePasswordSuccessMessage?.let { message ->
                                            Text(
                                                    text = message,
                                                    color = Green40,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                        
                                        // Password Requirements
                                        if (viewModel.editablePassword.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                            ) {
                                    Text(
                                                    text = stringResource(R.string.password_requirements),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                                
                                                // List of requirements
                                                val requirements = listOf(
                                                    stringResource(R.string.req_min_length),
                                                    stringResource(R.string.req_uppercase),
                                                    stringResource(R.string.req_number),
                                                    stringResource(R.string.req_special),
                                                    stringResource(R.string.req_match)
                                                )
                                                
                                                requirements.forEach { requirement ->
                                                    val isMet = viewModel.isPasswordRequirementMet(requirement)
                                                    
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
                                                                    color = if (isMet) Green40 else MaterialTheme.colorScheme.surfaceVariant,
                                                                    shape = RoundedCornerShape(2.dp)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (isMet) {
                                    Icon(
                                                                    imageVector = Icons.Default.Check,
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
                                                            color = if (isMet) Green40 else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // User ID (non-editable)
                                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    
                                // Save/Cancel buttons before the Account Details section (only when in edit mode)
                                AnimatedVisibility(
                                    visible = viewModel.isInEditMode,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp, bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Cancel button
                                            OutlinedButton(
                                                onClick = { viewModel.exitEditMode() },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                            Text(
                                                    text = stringResource(R.string.cancel),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            
                                            // Save button
                                            Button(
                                                onClick = { viewModel.saveProfileChanges(context, userId) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Green40
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                            Text(
                                                    text = stringResource(R.string.save_changes),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = stringResource(R.string.account_details),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Green40,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                ProfileField(
                                    label = stringResource(R.string.user_id),
                                    icon = Icons.Default.Person,
                                    value = userId,
                                    editable = false
                                )
                                
                                // Only show Gender if available
                                if (viewModel.userDetails?.gender != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    ProfileField(
                                        label = stringResource(R.string.gender),
                                        icon = Icons.Default.Person,
                                        value = viewModel.userDetails?.gender ?: stringResource(R.string.not_specified),
                                        editable = false
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Language selection card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Language settings header
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Green40,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Language selection option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightGreen80)
                                    .clickable { viewModel.showLanguageSelector() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = stringResource(R.string.language),
                                    tint = Green40
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.select_language),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    
                                    Text(
                                        text = LocaleHelper.getLanguageDisplayName(viewModel.currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Language",
                                    tint = Green40
                                )
                            }
                        }
                    }
                    
                    // Actions Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Green40,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Logout Button
                            Button(
                                onClick = { viewModel.logout(context, onLogout) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green40
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = stringResource(R.string.logout)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.logout))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Admin View Button
                            OutlinedButton(
                                onClick = { showClinicianDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Green40
                                ),
                                border = BorderStroke(1.dp, Green40)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.clinician_view)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.clinician_view))
                            }
                        }
                    }
                    
                    // Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.about_nutritrack),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Green40,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
        
        Text(
                                text = stringResource(R.string.about_nutritrack_desc),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = stringResource(R.string.version),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // Bottom spacing
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Clinician Login Dialog
    if (showClinicianDialog) {
        var localClinicianError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                showClinicianDialog = false
                clinicianKey = ""
                localClinicianError = null
            },
            title = {
                Text(
                    text = stringResource(R.string.clinician_login),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Green40,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.clinician_access_key),
                        style = MaterialTheme.typography.bodyMedium
        )
        
                    Spacer(modifier = Modifier.height(16.dp))
        
                    OutlinedTextField(
                        value = clinicianKey,
                        onValueChange = { clinicianKey = it },
                        label = { Text(stringResource(R.string.access_key)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green40,
                            unfocusedBorderColor = GreenGrey40
                        ),
                        isError = localClinicianError != null
                    )
                    
                    // Error message
                    if (localClinicianError != null) {
        Text(
                            text = localClinicianError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clinicianKey == "dollar-entry-apples") {
                            showClinicianDialog = false
                            clinicianKey = ""
                            localClinicianError = null
                            // Navigate to admin/clinician view
                            onNavigate(AppScreen.CLINICIAN)
                        } else {
                            // Store the error message in a variable first rather than calling stringResource directly
                            val errorMessage = context.getString(R.string.invalid_key)
                            localClinicianError = errorMessage
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green40
                    )
                ) {
                    Text(stringResource(R.string.login))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showClinicianDialog = false
                        clinicianKey = ""
                        localClinicianError = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * A reusable composable for displaying a single profile field within the settings screen.
 *
 * This field can be in a display mode (showing a static value) or an edit mode (showing an
 * [OutlinedTextField] for input). It includes a label, an icon, and the value itself.
 * It can also display a success message below the field.
 *
 * @param label The label for the profile field (e.g., "Username", "Phone Number").
 * @param icon The [ImageVector] to display as a leading icon for the field.
 * @param value The current value of the field (displayed when not in edit mode).
 * @param editable A boolean indicating whether the field is currently in edit mode.
 * @param editableValue The current value of the input when in edit mode.
 * @param onValueChange Callback function invoked when the value of the input changes in edit mode.
 * @param keyboardType The keyboard type to be used for the input field when in edit mode.
 *                     Defaults to [androidx.compose.ui.text.input.KeyboardType.Text].
 * @param successMessage An optional success message to display below the field (e.g., after a successful update).
 * @param isSensitive A boolean indicating if the field is sensitive (e.g. password), to allow for masking. Defaults to false.
 */
@Composable
fun ProfileField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    editable: Boolean = false,
    editableValue: String = "",
    onValueChange: (String) -> Unit = {},
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    successMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GreenGrey40,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                if (editable) {
                    OutlinedTextField(
                        value = editableValue,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = keyboardType
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green40,
                            unfocusedBorderColor = GreenGrey40,
                            cursorColor = Green40
                        )
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Success message
        AnimatedVisibility(
            visible = successMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            successMessage?.let {
                Text(
                    text = it,
                    color = Green40,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                )
            }
        }
    }
}
