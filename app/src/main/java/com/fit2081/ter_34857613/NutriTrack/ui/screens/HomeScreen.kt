package com.fit2081.ter_34857613.NutriTrack.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.AppScreen
import com.fit2081.ter_34857613.NutriTrack.viewmodel.HomeViewModel

/**
 * Composable function for the Home screen of the application.
 *
 * This screen serves as the main dashboard for the user after logging in.
 * It typically displays:
 * - A personalized welcome message including the user's name.
 * - A summary or visual representation of the user's food quality score (e.g., an image).
 * - The user's calculated HEIFA score (or a similar overall nutritional score).
 * - An indication of whether the user has completed their initial questionnaire.
 * - Navigation options to other key sections of the app, such as:
 *     - Editing their profile/questionnaire ([AppScreen.QUESTIONNAIRE]).
 *     - Viewing detailed nutritional insights ([AppScreen.INSIGHTS]).
 *     - Accessing the NutriCoach for tips and fruit search ([AppScreen.NUTRICOACH]).
 *
 * The screen handles loading and error states when fetching user data.
 * It uses a [HomeViewModel] to load and manage the necessary `NutritionData` for the `userId`.
 *
 * @param userId The unique identifier of the currently logged-in user.
 * @param onNavigate Callback function invoked when a navigation action is triggered (e.g., clicking a button
 *                   to go to another screen). It provides the target [AppScreen].
 * @param viewModel The [HomeViewModel] instance for this screen.
 */
@Composable
fun HomeScreen(
    userId: String,
    onNavigate: (AppScreen) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // Load user's data from repository when userId changes
    LaunchedEffect(userId) {
        viewModel.loadNutritionData(context, userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Loading state
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Green40)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_nutrition_data),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Box
        }
        
        // Error state
        if (viewModel.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.error_unknown),
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.errorMessage ?: stringResource(R.string.error_unknown),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadNutritionData(context, userId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Green40)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_try_again),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_try_again))
                    }
                }
            }
            return@Box
        }
        
        // Content state - show only when data is available
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Card for welcome message and edit button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Welcome message row with edit button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User greeting and food intake status
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.greeting_hello, viewModel.getUserName()),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = stringResource(R.string.questionnaire_completed),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        
                        // Edit button
                        Button(
                            onClick = { onNavigate(AppScreen.QUESTIONNAIRE) },
                            modifier = Modifier
                                .size(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green40
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Food quality score visualization
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fruits),
                            contentDescription = stringResource(R.string.your_food_quality_score),
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // My Score header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.my_score),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // See all scores button aligned with the score below
                        TextButton(
                            onClick = { onNavigate(AppScreen.INSIGHTS) },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.see_all_scores),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.see_all_scores),
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // Food quality score section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side with arrow and text
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.your_food_quality_score),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        
                        // Right side with score
                        Text(
                            text = viewModel.getUserScore(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green40
                        )
                    }

                    // Add divider between score and explanation
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        thickness = 1.dp,
                        color = Color.LightGray
                    )
                    
                    // Add explanation content directly in main card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.what_is_food_quality_score),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.food_score_explanation_1),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.food_score_explanation_2),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Some spacing at the bottom so content is visible
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}
