package com.fit2081.ter_34857613.NutriTrack.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.AppScreen
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.repository.InsightData
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.viewmodel.InsightsViewModel
import com.fit2081.ter_34857613.NutriTrack.utils.LocalAppLocale

/**
 * Composable function for the Insights screen.
 *
 * This screen displays the user's nutritional insights, including:
 * - An overall Food Quality Score, visualized with a circular progress indicator and textual rating
 *   (e.g., Poor, Fair, Good, Excellent).
 * - A detailed breakdown of scores by various food categories (e.g., Vegetables, Fruits, Grains),
 *   each shown with a [CategoryProgressBar].
 * - A descriptive message summarizing the diet quality.
 * - A button to share the user's score.
 *
 * The screen handles loading and error states. It uses an [InsightsViewModel] to fetch and manage
 * the [InsightData] (scores, ratings, descriptions) for the given `userId`.
 * It also supports multi-language functionality, reacting to changes in [LocalAppLocale]
 * to update displayed text and potentially re-fetch localized data via the ViewModel.
 *
 * @param userId The unique identifier of the user whose insights are to be displayed.
 * @param onNavigate Callback function to navigate to other [AppScreen]s (not actively used in the
 *                   current screen's primary flow but available for future extensions like navigating
 *                   to improvement suggestions).
 * @param viewModel The [InsightsViewModel] instance for this screen.
 */
@Composable
fun InsightsScreen(
    userId: String,
    onNavigate: (AppScreen) -> Unit = {},
    viewModel: InsightsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Track the current locale to detect language changes
    val currentLocale = LocalAppLocale.current
    
    // Effect that runs when locale changes
    LaunchedEffect(currentLocale) {
        viewModel.onLanguageChanged(context)
    }

    // Load user's data from repository when userId changes
    LaunchedEffect(userId) {
        viewModel.loadInsightData(context, userId)
    }
    
    // Force recomposition when language changes
    val forceRecompose = viewModel.languageChanged.value

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // Loading state
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(color = Green40)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_insights),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }
        
        // Error state
        if (viewModel.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.errorMessage ?: "An unknown error occurred",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retryLoading(context, userId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Green40)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Try Again",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_try_again))
                    }
                }
            }
            return@Scaffold
        }
        
        // Content - only shown when data is loaded successfully
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen title
            Text(
                text = stringResource(R.string.insights_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp).padding(horizontal = 16.dp)
            )

            // Food Quality Score Card - displays overall nutritional assessment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.food_quality_score),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Circular Progress Indicator showing overall score
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = {
                                viewModel.insightData?.totalScore?.toFloat()?.div(100f) ?: 0f
                            },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 12.dp,
                            color = viewModel.qualityColor,
                            trackColor = Color.LightGray
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${viewModel.totalScoreInt}",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.score_out_of_100),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Quality Rating labels to indicate score ranges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.quality_poor), fontSize = 14.sp, color = Color.Red)
                        Text(stringResource(R.string.quality_fair), fontSize = 14.sp, color = Color(0xFFFFA500))
                        Text(stringResource(R.string.quality_good), fontSize = 14.sp, color = Green40)
                        Text(stringResource(R.string.quality_excellent), fontSize = 14.sp, color = Green40)
                    }

                    // Linear progress bar showing position in quality spectrum
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(viewModel.insightData?.totalScore?.toFloat()?.div(100f) ?: 0f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(viewModel.qualityColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quality assessment text with personalized feedback
                    Text(
                        text = stringResource(
                            R.string.diet_quality_message, 
                            viewModel.qualityRating,
                            viewModel.qualityDescription
                        ),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
            }

            // Category Breakdown Card - shows detailed scores by food category
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.category_breakdown),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Individual category progress bars - only show when data is loaded
                    viewModel.insightData?.let { data ->
                        // Create category progress bar for vegetables
                        CategoryProgressBar(
                            title = stringResource(R.string.category_vegetables),
                            score = data.vegetablesScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for fruits
                        CategoryProgressBar(
                            title = stringResource(R.string.category_fruits),
                            score = data.fruitsScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for grains
                        CategoryProgressBar(
                            title = stringResource(R.string.category_grains),
                            score = data.grainsScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for whole grains
                        CategoryProgressBar(
                            title = stringResource(R.string.category_whole_grains),
                            score = data.wholeGrainsScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for dairy
                        CategoryProgressBar(
                            title = stringResource(R.string.category_dairy),
                            score = data.dairyScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for meat
                        CategoryProgressBar(
                            title = stringResource(R.string.category_meat),
                            score = data.meatScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for water
                        CategoryProgressBar(
                            title = stringResource(R.string.category_water),
                            score = data.waterScore,
                            maxScore = 5.0
                        )
                        
                        // Create category progress bar for unsaturated fats
                        CategoryProgressBar(
                            title = stringResource(R.string.category_unsaturated_fats),
                            score = data.unsaturatedFatsScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for sodium
                        CategoryProgressBar(
                            title = stringResource(R.string.category_sodium),
                            score = data.sodiumScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for sugar
                        CategoryProgressBar(
                            title = stringResource(R.string.category_sugar),
                            score = data.sugarScore,
                            maxScore = 10.0
                        )
                        
                        // Create category progress bar for alcohol
                        CategoryProgressBar(
                            title = stringResource(R.string.category_alcohol),
                            score = data.alcoholScore,
                            maxScore = 5.0
                        )
                        
                        // Create category progress bar for discretionary
                            CategoryProgressBar(
                            title = stringResource(R.string.category_discretionary),
                            score = data.discretionaryScore,
                            maxScore = 10.0
                            )
                    }
                }
            }
            
            // Action Buttons section for user interaction
            Spacer(modifier = Modifier.height(16.dp))
            
            // Share Button - allows sharing score via platform sharing intent
            Button(
                onClick = {
                    // Use hardcoded strings for sharing to avoid stringResource issues
                    // Create and launch Android share intent
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, viewModel.getSharingText())
                        type = "text/plain"
                    }
                    // Start the sharing activity
                    context.startActivity(Intent.createChooser(sendIntent, "Share your HEIFA score"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Score",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.share_score),
                    fontSize = 16.sp
                )
            }

            // Improve my diet button - navigates to NutriCoach screen
            Button(
                onClick = {
                    onNavigate(AppScreen.NUTRICOACH)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_seedling),
                    contentDescription = "Improve Diet",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.improve_my_diet),
                    fontSize = 16.sp
                )
            }

            // Some spacing at the bottom for better scrolling
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

/**
 * Composable function for displaying a progress bar representing the score for a specific food category.
 *
 * Shows the category name, its score (e.g., "7/10"), and a linear progress bar visually representing
 * this score. The color of the progress bar can change based on the score to indicate quality (e.g., green for good, red for poor).
 *
 * @param categoryName The name of the food category (e.g., "Vegetables", "Fruits").
 * @param score The current score for this category.
 * @param maxScore The maximum possible score for this category.
 * @param progressColor The [Color] to use for the filled portion of the progress bar.
 */
@Composable
fun CategoryProgressBar(title: String, score: Double, maxScore: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${score}/${maxScore}",
                fontSize = 16.sp
            )
        }
        // Linear progress bar showing category score relative to maximum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((score / maxScore).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Green40)
            )
        }
    }
}
