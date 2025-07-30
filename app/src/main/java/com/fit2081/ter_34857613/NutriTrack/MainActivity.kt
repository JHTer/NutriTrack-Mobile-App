package com.fit2081.ter_34857613.NutriTrack

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import com.fit2081.ter_34857613.NutriTrack.ui.screens.HomeScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.InsightsScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.LoginScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.QuestionnaireScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.WelcomeScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.NutriCoachScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.SettingsScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.ForgotPasswordScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.PhoneVerificationScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.PasswordSetupScreen
import com.fit2081.ter_34857613.NutriTrack.ui.screens.ClinicianScreen
import com.fit2081.ter_34857613.NutriTrack.ui.theme.NutriTrackTheme
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserPreferencesRepository
import com.fit2081.ter_34857613.NutriTrack.model.repository.UserSessionManager
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.LightGreen80
import com.fit2081.ter_34857613.NutriTrack.utils.LocaleHelper
import com.fit2081.ter_34857613.NutriTrack.utils.LocaleProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

/**
 * Enumeration of all available screens in the application.
 * Used for navigation control and current screen state tracking.
 */
enum class AppScreen {
    WELCOME,
    HOME,
    INSIGHTS,
    NUTRICOACH,
    SETTINGS,
    QUESTIONNAIRE,
    CLINICIAN,
    FORGOT_PASSWORD,
    PHONE_VERIFICATION,
    PASSWORD_SETUP
}

/**
 * Main activity class for the NutriTrack application.
 * Handles initialization and setting up the Compose UI environment.
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Override attachBaseContext to apply the saved locale before the Activity is created
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LocaleHelper.getStoredLocale(newBase)
        val updatedContext = LocaleHelper.setLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }
    
    /**
     * Handle configuration changes to ensure locale is preserved
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val savedLanguage = LocaleHelper.getStoredLocale(this)
        LocaleHelper.setLocale(this, savedLanguage)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for a more immersive UI
        enableEdgeToEdge()
        
        // Configure window to allow system bars to overlay the app content
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            // Get the current locale for recomposition trigger
            val currentLanguage = LocaleHelper.getStoredLocale(this)
            
            // Wrap app content with LocaleProvider for localization support
            // Pass language as key to force recomposition when it changes
            LocaleProvider(language = currentLanguage) {
                NutriTrackTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainContent()
                    }
                }
            }
        }
    }
}

/**
 * Main composable function that controls the application's UI structure.
 * Manages navigation state, screen transitions, and the bottom navigation bar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainContent() {
    // Get the context for accessing UserSessionManager
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository() }
    
    // Use a loading state to prevent briefly showing welcome screen
    var isLoading by remember { mutableStateOf(true) }
    
    // State management for current screen and user authentication
    var currentScreen by remember { mutableStateOf<AppScreen?>(null) }
    var previousScreen by remember { mutableStateOf<AppScreen?>(null) }
    var currentUserId by remember { mutableStateOf("") }
    
    // State for controlling the modal login sheet visibility
    var showLoginSheet by remember { mutableStateOf(false) }
    
    // Temporary state for storing verification data
    var tempVerifiedUserId by remember { mutableStateOf("") }
    var tempVerifiedPhoneNumber by remember { mutableStateOf("") }
    
    // Animation state
    var exitTransition by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Function to handle navigation with animation
    val navigateTo = { targetScreen: AppScreen ->
        previousScreen = currentScreen
        exitTransition = true
        // Add delay for exit animation to complete
        scope.launch {
            kotlinx.coroutines.delay(300)
            currentScreen = targetScreen
            exitTransition = false
        }
    }
    
    // Check for existing user session when the app starts and determine initial screen
    LaunchedEffect(Unit) {
        val sessionManager = UserSessionManager.getInstance(context)
        if (sessionManager.isUserLoggedIn()) {
            val userId = sessionManager.getUserId()
            currentUserId = userId
            
            // Check if user has completed the questionnaire
            val hasCompletedQuestionnaire = userPreferencesRepository.hasCompletedQuestionnaire(context, userId)
            
            // Set initial screen based on questionnaire completion
            currentScreen = if (hasCompletedQuestionnaire) {
                AppScreen.HOME
            } else {
                AppScreen.QUESTIONNAIRE
            }
        } else {
            // User not logged in, show welcome screen
            currentScreen = AppScreen.WELCOME
        }
        
        // Done loading, ready to show content
        isLoading = false
    }
    
    // If still loading, show nothing or a loading indicator
    if (isLoading || currentScreen == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    
    // Determine when to show the bottom navigation bar
    // Only shows when user is logged in and not on welcome/questionnaire screens
    val showBottomNav = currentUserId.isNotEmpty() && 
                         currentScreen != AppScreen.WELCOME && 
                         currentScreen != AppScreen.QUESTIONNAIRE &&
                         currentScreen != AppScreen.FORGOT_PASSWORD &&
                         currentScreen != AppScreen.PHONE_VERIFICATION &&
                         currentScreen != AppScreen.PASSWORD_SETUP
    
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Home tab navigation
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                        label = { Text(stringResource(R.string.home)) },
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { navigateTo(AppScreen.HOME) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green40,
                            selectedTextColor = Green40,
                            indicatorColor = LightGreen80
                        )
                    )
                    // Insights tab navigation
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.insights)) },
                        label = { Text(stringResource(R.string.insights)) },
                        selected = currentScreen == AppScreen.INSIGHTS,
                        onClick = { navigateTo(AppScreen.INSIGHTS) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green40,
                            selectedTextColor = Green40,
                            indicatorColor = LightGreen80
                        )
                    )
                    // NutriCoach tab navigation with custom icon
                    NavigationBarItem(
                        icon = { Icon(painter = painterResource(id = R.drawable.ic_seedling),
                            contentDescription = stringResource(R.string.nutricoach)
                            ,modifier = Modifier.size(24.dp) )},
                        label = { Text(stringResource(R.string.nutricoach)) },
                        selected = currentScreen == AppScreen.NUTRICOACH,
                        onClick = { navigateTo(AppScreen.NUTRICOACH) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green40,
                            selectedTextColor = Green40,
                            indicatorColor = LightGreen80
                        )
                    )
                    // Settings tab navigation
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) },
                        label = { Text(stringResource(R.string.settings)) },
                        selected = currentScreen == AppScreen.SETTINGS || currentScreen == AppScreen.CLINICIAN,
                        onClick = { navigateTo(AppScreen.SETTINGS) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green40,
                            selectedTextColor = Green40,
                            indicatorColor = LightGreen80
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Main content container adjusted for bottom navigation bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen content switcher with animation
            AnimatedContent(
                targetState = currentScreen!!,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)).togetherWith(
                        fadeOut(animationSpec = tween(300))
                    )
                },
                modifier = Modifier.fillMaxSize(),
                label = "Screen Transitions"
            ) { screen ->
                // Screen content switcher based on navigation state
                when (screen) {
                    AppScreen.WELCOME -> {
                        WelcomeScreen(
                            onNavigateToLogin = { 
                                // Display login sheet as a modal rather than changing screens
                                showLoginSheet = true
                            }
                        )
                        
                        // Modal Bottom Sheet implementation for Login UI
                        if (showLoginSheet) {
                            // Animation state tracking variables
                            var isDismissing by remember { mutableStateOf(false) }
                            var offsetY by remember { mutableStateOf(0f) }
                            
                            // Animated offset calculation for smooth sheet animations
                            val animatedOffsetY by animateFloatAsState(
                                targetValue = if (isDismissing) 1000f else offsetY,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = FastOutSlowInEasing
                                ),
                                finishedListener = {
                                    // Close the sheet when dismissal animation completes
                                    if (isDismissing) {
                                        showLoginSheet = false
                                    }
                                }
                            )
                            
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Sheet container with rounded top corners
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.88f)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = animatedOffsetY.dp) // Apply vertical animation offset
                                        .pointerInput(Unit) {
                                            // Gesture detection for swipe-to-dismiss behavior
                                            detectDragGestures(
                                                onDragEnd = {
                                                    // Start dismissal if dragged far enough
                                                    if (offsetY > 100f) {
                                                        isDismissing = true
                                                    } else {
                                                        // Return to original position if not dragged enough
                                                        scope.launch {
                                                            offsetY = 0f
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    // Reset position when drag is canceled
                                                    scope.launch {
                                                        offsetY = 0f
                                                    }
                                                }
                                            ) { change, dragAmount ->
                                                // Only allow downward drag with resistance
                                                if (dragAmount.y > 0) {
                                                    offsetY += dragAmount.y * 0.5f // Apply resistance factor
                                                    
                                                    // Trigger dismissal if dragged very far
                                                    if (offsetY > 300f) {
                                                        isDismissing = true
                                                    }
                                                }
                                                // Consume the drag event
                                                change.consume()
                                            }
                                        },
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    color = Color.White,
                                    tonalElevation = 8.dp
                                ) {
                                    // Login sheet content container
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Drag handle indicator at top of sheet
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 30.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(32.dp)
                                                        .height(4.dp)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(2.dp)
                                                        )
                                                )
                                            }
                                            
                                            // Login screen content embedded in the sheet
                                            LoginScreen(
                                                onLoginSuccess = { userId, targetScreen ->
                                                    showLoginSheet = false
                                                    currentUserId = userId
                                                    navigateTo(targetScreen)
                                                },
                                                onNavigateToSignup = {
                                                    showLoginSheet = false
                                                    // Direct navigation to auth screen (Phone Verification) without changing to welcome first
                                                    navigateTo(AppScreen.PHONE_VERIFICATION)
                                                },
                                                onNavigateToForgotPassword = {
                                                    showLoginSheet = false
                                                    // Direct navigation to forgot password screen
                                                    navigateTo(AppScreen.FORGOT_PASSWORD)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    AppScreen.HOME -> {
                        HomeScreen(
                            userId = currentUserId,
                            onNavigate = { screen ->
                                navigateTo(screen)
                            }
                        )
                    }
                    
                    AppScreen.INSIGHTS -> {
                        InsightsScreen(
                            userId = currentUserId,
                            onNavigate = { screen ->
                                navigateTo(screen)
                            }
                        )
                    }
                    
                    AppScreen.NUTRICOACH -> {
                        // NutriCoach screen for personalized nutrition advice
                        NutriCoachScreen(
                            userId = currentUserId,
                            onNavigate = { screen ->
                                navigateTo(screen)
                            }
                        )
                    }
                    
                    AppScreen.QUESTIONNAIRE -> {
                        QuestionnaireScreen(
                            userId = currentUserId,
                            onNavigateBack = { 
                                navigateTo(AppScreen.HOME) 
                            },
                            onSave = { _ ->
                                // Navigate to home screen after questionnaire completion
                                navigateTo(AppScreen.HOME)
                            }
                        )
                    }
                    
                    AppScreen.SETTINGS -> {
                        // Settings screen for user preferences and configuration
                        SettingsScreen(
                            userId = currentUserId,
                            onNavigate = { screen ->
                                navigateTo(screen)
                            },
                            onLogout = {
                                // Clear user session and return to welcome screen
                                currentUserId = ""
                                navigateTo(AppScreen.WELCOME)
                            }
                        )
                    }
                    
                    AppScreen.FORGOT_PASSWORD -> {
                        // Create a new ViewModel instance each time to ensure fresh state
                        val forgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.fit2081.ter_34857613.NutriTrack.viewmodel.ForgotPasswordViewModel>()
                        
                        // Reset verification state to ensure we start at the verification step
                        forgotPasswordViewModel.clearVerification()
                        
                        ForgotPasswordScreen(
                            onPasswordResetSuccess = {
                                // First change the screen to welcome screen
                                currentScreen = AppScreen.WELCOME
                                
                                // Then show the login sheet with a slight delay for smoother transition
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(150) // Short delay for smooth transition
                                    showLoginSheet = true
                                }
                            },
                            onNavigateToLogin = {
                                // Navigate directly to welcome screen with animation
                                navigateTo(AppScreen.WELCOME)
                            },
                            viewModel = forgotPasswordViewModel
                        )
                    }
                    
                    AppScreen.PHONE_VERIFICATION -> {
                        // Phone verification screen - first step of the new signup flow
                        PhoneVerificationScreen(
                            onVerificationSuccess = { userId, phoneNumber ->
                                // Store the verification data in the activity state for the next screen
                                tempVerifiedUserId = userId
                                tempVerifiedPhoneNumber = phoneNumber
                                // Direct navigation to next auth screen
                                navigateTo(AppScreen.PASSWORD_SETUP)
                            },
                            onNavigateToLogin = {
                                // First change the screen to welcome screen
                                navigateTo(AppScreen.WELCOME)
                                
                                // Then show the login sheet with a slight delay for smoother transition
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(150) // Short delay for smooth transition
                                    showLoginSheet = true
                                }
                            }
                        )
                    }
                    
                    AppScreen.PASSWORD_SETUP -> {
                        // Password setup screen - second step of the new signup flow
                        PasswordSetupScreen(
                            userId = tempVerifiedUserId,
                            phoneNumber = tempVerifiedPhoneNumber,
                            onSetupComplete = {
                                // First change the screen to welcome screen
                                navigateTo(AppScreen.WELCOME)
                                
                                // Then show the login sheet with a slight delay for smoother transition
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(150) // Short delay for smooth transition
                                    showLoginSheet = true
                                }
                            },
                            onNavigateBack = {
                                // Direct navigation back to previous auth screen
                                navigateTo(AppScreen.PHONE_VERIFICATION)
                            }
                        )
                    }
                    
                    AppScreen.CLINICIAN -> {
                        ClinicianScreen(
                            onNavigateBack = {
                                navigateTo(AppScreen.SETTINGS)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview composable for the Welcome Screen.
 * Used for design-time preview in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    NutriTrackTheme {
        WelcomeScreen(
            onNavigateToLogin = {}
        )
    }
}

/**
 * Preview composable for the Login Screen.
 * Used for design-time preview in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NutriTrackTheme {
        LoginScreen(
            onLoginSuccess = { _, _ -> }
        )
    }
}

/**
 * Preview composable for the Home Screen.
 * Used for design-time preview in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NutriTrackTheme {
        HomeScreen(userId = "1", onNavigate = {})
    }
}
