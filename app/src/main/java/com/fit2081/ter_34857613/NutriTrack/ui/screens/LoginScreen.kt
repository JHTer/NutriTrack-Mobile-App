package com.fit2081.ter_34857613.NutriTrack.ui.screens

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.AppScreen
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

/**
 * Composable function for the user login screen.
 *
 * This screen allows existing users to log in to the application.
 * It features:
 * - A dropdown to select a pre-registered User ID.
 * - A password input field.
 * - A login button to attempt authentication.
 * - Links to navigate to the signup screen (for first-time users to claim their account)
 *   and to a forgot password screen.
 *
 * The screen uses a [LoginViewModel] to handle user ID loading, input state management,
 * authentication logic, and error display.
 * Upon successful login, it invokes the [onLoginSuccess] callback with the user's ID and the target screen.
 *
 * @param viewModel The [LoginViewModel] instance for this screen.
 * @param onLoginSuccess Callback function invoked upon successful login. It provides the logged-in user's ID
 *                       and the [AppScreen] they should be directed to (e.g., `HomeScreen` or `QuestionnaireScreen`).
 * @param onNavigateToSignup Callback function invoked when the "Sign Up" link is clicked.
 * @param onNavigateToForgotPassword Callback function invoked when the "Forgot Password?" link is clicked.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String, AppScreen) -> Unit,
    onNavigateToSignup: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    // Reference to Android EditText component for keyboard management
    var passwordEditText by remember { mutableStateOf<EditText?>(null) }
    
    // Access to system services and keyboard controller
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Collect user IDs from ViewModel's StateFlow
    val userIds by viewModel.userIdsStateFlow.collectAsStateWithLifecycle()
    
    // Immediately load user IDs on first render - fixed retry mechanism
    LaunchedEffect(Unit) {
        // Initial load attempt
        Log.d("LoginScreen", "Initial loading of user IDs")
        viewModel.loadUserIds(context)
        
        // Only retry if we don't get data after a delay
        delay(800) // Wait longer to ensure state collection happens
        if (userIds.isEmpty()) {
            Log.d("LoginScreen", "User IDs still empty after delay, retrying...")
            viewModel.loadUserIds(context, forceReload = true)
        }
    }
    
    // Automatically show keyboard when login screen appears for better UX
    LaunchedEffect(Unit) {
        // Delay to wait for the EditText to be created
        delay(600)
        passwordEditText?.post {
            // Request focus on the EditText
            passwordEditText?.requestFocus()
            
            // Show the keyboard
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(passwordEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // Define reusable TextField component with floating label
    /**
     * A reusable composable that wraps content (typically a TextField or other input)
     * with a floating label effect.
     *
     * The label appears above the input field's border when the field is not focused or empty,
     * and shrinks or moves to a corner when focused or filled (behavior depends on the specific
     * TextField implementation used within `content`). This specific implementation places
     * a static label text that overlaps the top border of a `Surface` containing the input content.
     *
     * @param label The text to display as the floating label.
     * @param content A composable lambda that renders the actual input field(s) or content
     *                that the label is associated with.
     * @param modifier Modifier for the outer `Box` container of this component.
     */
    @Composable
    fun FloatingLabelTextField(
        label: String,
        content: @Composable () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main input container with rounded corners and border
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), // Space for the floating label
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    content()
                }
            }
            
            // Floating label that overlaps the top border
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(Color.White) // Background to create floating effect
                    .padding(horizontal = 4.dp)
            )
        }
    }

    // Main Login Screen Layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Error message shown when login validation fails
            if (viewModel.showError) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        tint = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Invalid user ID or password. Please try again.",
                        color = Color(0xFFE53935)
                    )
                }
            }

            // Screen title and subtitle
            Text(
                text = "Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            

            // Instructional text for users
            Text(
                text = "This app is only for pre-registered patients. If you are a first-time user, please sign up using your patient ID.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            // User ID selection field with dropdown menu
            FloatingLabelTextField(
                label = "User ID",
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.selectedUserId.ifEmpty { "Select User ID" },
                            fontSize = 16.sp,
                            color = if (viewModel.selectedUserId.isEmpty()) Color.Gray else Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleExpanded() }
                        )
                        
                        IconButton(onClick = { viewModel.toggleExpanded() }) {
                        Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown, 
                            contentDescription = "Select User ID",
                                modifier = Modifier.size(24.dp)
                        )
                }
                
                        // Dropdown menu for user ID selection
                DropdownMenu(
                    expanded = viewModel.expanded,
                    onDismissRequest = { viewModel.toggleExpanded() },
                            offset = DpOffset(0.dp, 8.dp),
                    modifier = Modifier
                                .width(with(LocalDensity.current) { 
                                    (LocalConfiguration.current.screenWidthDp - 64).dp 
                                })
                ) {
                            // Show loading indicator if userIds are being loaded
                    if (userIds.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                    } else {
                                // Show list of user IDs
                        userIds.forEach { userId ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = userId,
                                        fontSize = 16.sp
                                    ) 
                                },
                                onClick = {
                                    viewModel.updateSelectedUserId(userId)
                                    viewModel.toggleExpanded()
                                }
                            )
                        }
                    }
                }
            }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field with AndroidView for better keyboard control
            FloatingLabelTextField(
                label = "Password",
                content = {
                AndroidView(
                        factory = { context ->
                            EditText(context).apply {
                                // Apply styling to match Compose
                                background = null
                                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                                setHint("Enter password")
                                
                                // Set callbacks
                                setOnFocusChangeListener { _, hasFocus ->
                                    // Reset error when field is focused
                                    if (hasFocus) {
                                        viewModel.resetError()
                                    }
                                }
                                
                                // Listen for text changes
                            addTextChangedListener(object : android.text.TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: android.text.Editable?) {
                                        viewModel.updatePassword(s.toString())
                                    }
                                })
                                
                                // Save reference to edit text
                                passwordEditText = this
                                
                                // Set keyboard action for login
                                setOnEditorActionListener { _, actionId, _ ->
                                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                                        keyboardController?.hide()
                                        viewModel.login(
                                            context = context,
                                            onError = { /* Error state handled via ViewModel */ },
                                            onLoginSuccess = onLoginSuccess
                                        )
                                        true
                                    } else {
                                        false
                                    }
                                }
                                
                                // Request focus on initial render
                            post {
                                requestFocus()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login button with custom styling
            Button(
                onClick = {
                    // Hide keyboard before login attempt
                    keyboardController?.hide()
                    
                    // Validate credentials via ViewModel
                    viewModel.login(
                        context = context,
                        onError = { /* Error state handled via ViewModel */ },
                        onLoginSuccess = onLoginSuccess
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Links to signup and forgot password screens
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateToSignup) {
                    Text(
                        text = "First Time Login? Sign Up",
                        color = Green40,
                        fontSize = 14.sp
                    )
                }
                
                TextButton(onClick = onNavigateToForgotPassword) {
                Text(
                    text = "Forgot Password?",
                    color = Green40,
                        fontSize = 14.sp
                )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


