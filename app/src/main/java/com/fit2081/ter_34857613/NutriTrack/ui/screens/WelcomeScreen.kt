package com.fit2081.ter_34857613.NutriTrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.LightGreen80
import com.fit2081.ter_34857613.NutriTrack.ui.theme.NutriTrackTheme

/**
 * Composable function for the application's welcome screen.
 *
 * This screen serves as the initial entry point for users. It displays:
 * - The application logo and name ("NutriTrack").
 * - A brief tagline for the application.
 * - An important medical disclaimer advising users that the app is for educational purposes only
 *   and not a substitute for professional medical advice.
 * - An "About NutriTrack" section with a link to the Monash Nutrition Clinic website.
 * - A prominent "Login" button to proceed to the authentication flow.
 * - Student developer credit information.
 *
 * The screen uses an [AnimatedVisibility] to provide a smooth fade-in and expand animation on entry.
 *
 * @param modifier Optional [Modifier] to be applied to the main column of the screen.
 * @param onNavigateToLogin Callback function invoked when the "Login" button is clicked,
 *                          triggering navigation to the login screen.
 */
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {}
) {
    // Create an animated entry state for smooth fade-in and expand animation
    val animatedVisibilityState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = animatedVisibilityState,
        enter = fadeIn() + expandVertically()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo and Branding section at the top of the screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular logo container with app icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Green40),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_seedling),
                        contentDescription = "NutriTrack Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // App name with emphasized typography
                Text(
                    text = "NutriTrack",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                // App tagline/description
                Text(
                    text = "Your personal nutrition companion",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Main Content section with information and actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Medical Disclaimer Card - important legal information
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LightGreen80 // Light green background for emphasis
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Disclaimer",
                                fontWeight = FontWeight.SemiBold,
                                color = Green40,
                                fontSize = 16.sp
                            )
                            
                            Text(
                                text = "NutriTrack provides general health and nutrition information for educational purposes only. " +
                                        "It is not intended as medical advice, diagnosis, or treatment. " +
                                        "Always consult a qualified healthcare professional " +
                                        "before making any changes to your diet, exercise, or health regimen. Use this app at your own risk.",
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // About section describing the app's purpose
                    Text(
                        text = "About NutriTrack",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                    
                    Text(
                        text = "If you'd like to an Accredited Practicing Dietitian (APD), " +
                                "please visit the Monash Nutrition/Dietetics Clinic" ,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                    
                    // External link to Monash Nutrition Clinic resources
                    val uriHandler = LocalUriHandler.current
                    TextButton(
                        onClick = { 
                            uriHandler.openUri("https://www.monash.edu/medicine/nutrition")
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Green40
                        )
                    ) {
                        Text("Visit Monash Nutrition Clinic")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "External Link",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Login Button and Student Information footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp, top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary action button to begin user journey
                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green40
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Student developer credit information
                    Text(
                        text = "Ter Jing Hao (34857613)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * A Jetpack Compose Preview function for the [WelcomeScreen].
 *
 * This allows for rendering and visualizing the [WelcomeScreen] in Android Studio's design view
 * during development, without needing to run the full application on an emulator or device.
 * It wraps the [WelcomeScreen] with the [NutriTrackTheme].
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