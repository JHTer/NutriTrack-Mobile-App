package com.fit2081.ter_34857613.NutriTrack.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.LightGreen40
import com.fit2081.ter_34857613.NutriTrack.viewmodel.AiInsight
import com.fit2081.ter_34857613.NutriTrack.viewmodel.ClinicianViewModel
import com.fit2081.ter_34857613.NutriTrack.viewmodel.ComponentAnalysis
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.core.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalLayoutDirection
import java.util.Locale

/**
 * Main composable function for the Clinician Dashboard screen.
 *
 * This screen provides clinicians with an overview of aggregated patient data,
 * including average HEIFA scores, AI-generated insights, and detailed component analysis.
 * It features a top bar for navigation, a welcome message, and sections for displaying
 * various data visualizations and analytical summaries.
 *
 * Users can interact with the screen to:
 * - View average HEIFA scores for male and female patients.
 * - Trigger AI analysis to find data patterns and generate insights.
 * - View detailed breakdowns of HEIFA component scores.
 * - Explore individual AI-generated insights.
 *
 * The screen utilizes a [ClinicianViewModel] to fetch and manage data,
 * and displays information in a [LazyColumn] for scrollable content.
 * Dialogs are used to present detailed views for HEIFA scores and AI insights.
 *
 * @param modifier Modifier for this composable.
 * @param onNavigateBack Callback function to handle back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    // Get ViewModel instance
    val viewModel: ClinicianViewModel = viewModel()
    
    // Initialize the ViewModel with context
    val context = LocalContext.current
    viewModel.initialize(context)
    
    // Get data from ViewModel
    val heifaAverages = viewModel.heifaAverages
    val componentAnalysis = viewModel.componentAnalysis
    val insights = viewModel.insights
    val errorMessage = viewModel.errorMessage
    
    // State for tracking which detailed view is currently visible
    val (expandedDetail, setExpandedDetail) = remember { mutableStateOf<String?>(null) }
    
    // State for tracking selected insight
    val (selectedInsight, setSelectedInsight) = remember { mutableStateOf<AiInsight?>(null) }

    Scaffold(
        topBar = {
            ClinicianTopBar(
                title = stringResource(R.string.clinician_dashboard),
                onNavigateBack = onNavigateBack
            )
        },
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        // Main content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp, bottom = paddingValues.calculateBottomPadding(), 
                         start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                         end = paddingValues.calculateEndPadding(LocalLayoutDirection.current))
                .background(Color(0xFFF5F7FA))
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Welcome Section
            item { WelcomeSection(clinicianName = "") }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            // HEIFA Score Averages
            item { 
                HeifaScoreAveragesSection(
                    maleAvg = heifaAverages?.maleAverage ?: 0.0, 
                    femaleAvg = heifaAverages?.femaleAverage ?: 0.0,
                    isLoading = viewModel.isLoadingAverages,
                    onViewDetails = { setExpandedDetail("heifa_scores") }
                ) 
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            // AI Consultant
            item { AiConsultantSection() }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            // Data Analysis Section with insights
            item {
                DataAnalysisSection(
                    isAnalyzing = viewModel.isAnalyzingData,
                    errorMessage = errorMessage,
                    onAnalyzeClick = { viewModel.findDataPatterns(context) },
                    insights = insights,
                    onInsightClick = { insightId ->
                        // Find the insight with the matching ID
                        val insight = insights.find { it.id == insightId }
                        if (insight != null) {
                            setSelectedInsight(insight)
                            setExpandedDetail("ai_insight")
                        }
                        
                        // Mark the insight as read
                        viewModel.markInsightAsRead(insightId)
                    }
                ) 
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        
        // Display error message if present
        errorMessage?.let {
            ErrorSnackbar(
                message = it,
                onDismiss = { viewModel.clearErrorMessage() }
            )
        }
        
        // Dialog popups for detailed views - they appear on top of the content
        when (expandedDetail) {
            "heifa_scores" -> {
                DetailDialog(onDismiss = { setExpandedDetail(null) }) {
                    HeifaScoreDetailedView(
                        componentAnalysis = componentAnalysis,
                        onClose = { setExpandedDetail(null) }
                    )
                }
            }
            "ai_insight" -> {
                // Only show if we have a selected insight
                selectedInsight?.let { insight ->
                DetailDialog(onDismiss = { setExpandedDetail(null) }) {
                        AiInsightDetailView(
                            insight = insight,
                            onClose = { setExpandedDetail(null) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function for the top app bar of the Clinician screen.
 *
 * Displays a title and a back navigation button.
 *
 * @param title The title to be displayed in the top bar.
 * @param onNavigateBack Callback function invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White)
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.Black
            )
        }
        Text(
            text = title, 
            fontWeight = FontWeight.Bold, 
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

/**
 * Composable function for the welcome section of the Clinician screen.
 *
 * Displays a "Welcome back!" message. The `clinicianName` parameter is currently unused
 * but could be used in the future to personalize the greeting.
 *
 * @param clinicianName The name of the clinician (currently not displayed).
 */
@Composable
fun WelcomeSection(clinicianName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.welcome_back),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

    }
}

/**
 * Composable function for displaying the HEIFA score averages section.
 *
 * Shows the average HEIFA scores for male and female patients, along with a button
 * to view more details. Includes a loading indicator when data is being fetched.
 *
 * @param maleAvg The average HEIFA score for male patients.
 * @param femaleAvg The average HEIFA score for female patients.
 * @param isLoading A boolean indicating whether the average scores are currently being loaded.
 * @param onViewDetails Callback function invoked when the "View Details" button is pressed.
 */
@Composable
fun HeifaScoreAveragesSection(
    maleAvg: Double, 
    femaleAvg: Double,
    isLoading: Boolean = false,
    onViewDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = stringResource(R.string.heifa_score_averages),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
            TextButton(onClick = onViewDetails) {
                Text(stringResource(R.string.view_details), color = Green40)
            }
        }
        
    Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                // Show loading indicators when data is being fetched
                LoadingHeifaCard(
                    gender = stringResource(R.string.male),
                    icon = Icons.Default.Male,
                    color = Green40,
                    modifier = Modifier.weight(1f)
                )
                LoadingHeifaCard(
                    gender = stringResource(R.string.female),
                    icon = Icons.Default.Female,
                    color = LightGreen40,
                    modifier = Modifier.weight(1f)
                )
            } else {
            HeifaScoreCard(
                gender = stringResource(R.string.male),
                score = maleAvg,
                icon = Icons.Default.Male,
                color = Green40,
                modifier = Modifier.weight(1f)
            )
            HeifaScoreCard(
                gender = stringResource(R.string.female),
                score = femaleAvg,
                icon = Icons.Default.Female,
                color = LightGreen40,
                modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Composable function for displaying a loading state card for HEIFA scores.
 *
 * This card is shown while the HEIFA score data for a specific gender is being fetched.
 * It displays the gender, an icon, and a circular progress indicator.
 *
 * @param gender The gender for which the score is loading (e.g., "Male", "Female").
 * @param icon The [ImageVector] representing the gender.
 * @param color The primary color associated with this gender for UI theming.
 * @param modifier Modifier for this composable.
 */
@Composable
fun LoadingHeifaCard(
    gender: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = gender,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(gender, fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Loading animation
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
            }
            
            Text(
                text = stringResource(R.string.loading_score),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Composable function for displaying a card with a HEIFA score for a specific gender.
 *
 * This card shows the gender, an icon, the calculated average HEIFA score, and a descriptive label.
 *
 * @param gender The gender for which the score is displayed (e.g., "Male", "Female").
 * @param score The average HEIFA score to display.
 * @param icon The [ImageVector] representing the gender.
 * @param color The primary color associated with this gender for UI theming.
 * @param modifier Modifier for this composable.
 */
@Composable
fun HeifaScoreCard(
    gender: String,
    score: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = gender,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(gender, fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f", score),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = stringResource(R.string.average_heifa_score),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Composable function for the AI Consultant section of the Clinician screen.
 *
 * This section provides an interface for the clinician to interact with an AI assistant.
 * It displays a chat history, an input field for sending messages to the AI,
 * and suggested questions. It also shows a loading indicator when the AI is generating a response.
 * An information dialog can be displayed to provide more context about the AI consultant feature.
 */
@Composable
fun AiConsultantSection() {
    val viewModel: ClinicianViewModel = viewModel()
    val chatMessages = viewModel.chatMessages
    val suggestedQuestions = viewModel.suggestedQuestions
    val isGeneratingResponse = viewModel.isGeneratingResponse
    
    val userMessageState = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // State for information dialog
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Green40.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = stringResource(R.string.ai_consultant),
                    tint = Green40,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ai_consultant),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Information button
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.about_ai_consultant),
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Information Dialog
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.about_ai_consultant),
                        fontWeight = FontWeight.Bold,
                        color = Green40
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.ai_consultant_info),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(stringResource(R.string.got_it), color = Green40)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Chat Interface
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Chat History
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Fixed height for scrollable area
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false
                    ) {
                        items(chatMessages) { message ->
                            if (message.isFromUser) {
                                ChatMessageUser(message = message.content)
                            } else {
                            ChatMessageAI(
                                    message = message.content,
                                    categories = message.categories,
                                    isFirst = message == chatMessages.firstOrNull { !it.isFromUser }
                            )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Show typing indicator if generating response
                        if (isGeneratingResponse) {
                        item {
                                ChatMessageTyping()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        }
                        
                        // Show suggested questions if not generating response and we have messages
                        if (!isGeneratingResponse && chatMessages.size > 1 && suggestedQuestions.isNotEmpty()) {
                        item {
                                SuggestedQuestions(
                                    questions = suggestedQuestions,
                                    onQuestionSelected = { question ->
                                        viewModel.sendMessage(question)
                                    }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Chat Input with Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userMessageState.value,
                        onValueChange = { userMessageState.value = it },
                        placeholder = { Text(stringResource(R.string.ask_about_heifa)) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Green40,
                            unfocusedContainerColor = Color(0xFFF5F7FA),
                            focusedContainerColor = Color(0xFFF5F7FA)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (userMessageState.value.isNotBlank() && !isGeneratingResponse) {
                                    viewModel.sendMessage(userMessageState.value)
                                    userMessageState.value = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        maxLines = 3,
                        enabled = !isGeneratingResponse
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Send button
                    IconButton(
                        onClick = {
                            if (userMessageState.value.isNotBlank() && !isGeneratingResponse) {
                                viewModel.sendMessage(userMessageState.value)
                                userMessageState.value = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Green40)
                            .size(48.dp),
                        enabled = !isGeneratingResponse
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Help text
                Text(
                    text = stringResource(R.string.only_nutrition_questions),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Composable function for displaying a chat message from the AI assistant.
 *
 * This message includes the AI's text response and optional category tags related to the message.
 * It has a distinct visual style to differentiate it from user messages, including an AI icon
 * and a speech bubble shape.
 *
 * @param message The text content of the AI's message.
 * @param categories A list of string categories associated with the AI's message. These are displayed as tags.
 * @param isFirst A boolean indicating if this is the first AI message in the current sequence,
 *                used to adjust the shape of the chat bubble (e.g., remove the top-start corner radius).
 */
@Composable
fun ChatMessageAI(
    message: String,
    categories: List<String> = emptyList(),
    isFirst: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Green40.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = stringResource(R.string.ai_assistant),
                tint = Green40,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isFirst) 0.dp else 12.dp,
                topEnd = 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            color = Color(0xFFF5F7FA),
                modifier = Modifier.widthIn(max = 280.dp)
        ) {
                Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
                    
                    // Only show categories if there are any
                    if (categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            categories.take(2).forEach { category ->
                                CategoryTag(category = category)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // AI label and timestamp
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ai_assistant),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "•",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Composable function for displaying a chat message from the user.
 *
 * This message has a distinct visual style to differentiate it from AI messages,
 * typically aligned to the end of the chat row and using a different background color.
 *
 * @param message The text content of the user's message.
 */
@Composable
fun ChatMessageUser(message: String) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.weight(0.15f))
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 0.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            color = Green40,
            modifier = Modifier.weight(0.85f)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Composable function for displaying a typing indicator for the AI assistant.
 *
 * Shows an animated "Typing..." message with an AI icon, indicating that the AI is processing
 * and generating a response.
 */
@Composable
fun ChatMessageTyping() {
    Row(
        verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Green40.copy(alpha = 0.1f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = stringResource(R.string.ai_assistant),
                            tint = Green40,
                            modifier = Modifier.size(16.dp)
                        )
        }
        
                        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F7FA),
            modifier = Modifier.widthIn(max = 120.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simple typing animation with 3 dots
                val infiniteTransition = rememberInfiniteTransition(label = "typing")
                val dotAlpha1 = infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(0)
                    ),
                    label = "dot1"
                )
                val dotAlpha2 = infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(150)
                    ),
                    label = "dot2"
                )
                val dotAlpha3 = infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(300)
                    ),
                    label = "dot3"
                )
                
                Text(
                    text = stringResource(R.string.typing),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                
                Text(
                    text = ".",
                    fontSize = 14.sp,
                    color = Color.DarkGray.copy(alpha = dotAlpha1.value),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ".",
                    fontSize = 14.sp,
                    color = Color.DarkGray.copy(alpha = dotAlpha2.value),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ".",
                    fontSize = 14.sp,
                    color = Color.DarkGray.copy(alpha = dotAlpha3.value),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Composable function for displaying a category tag.
 *
 * These tags are used to visually label AI chat messages with relevant categories.
 * The tag has a rounded background and displays the capitalized category name.
 *
 * @param category The category string to display (e.g., "data_analysis", "heifa_scores").
 *                 This string will be formatted (underscores replaced with spaces, words capitalized).
 */
@Composable
fun CategoryTag(category: String) {
    // Use a consistent color scheme for all categories
    val backgroundColor = Green40.copy(alpha = 0.2f)
    val textColor = Green40
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = category.capitalizeWords(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Extension function for the [String] class to capitalize each word in a string.
 *
 * Words are assumed to be separated by underscores. Each word is then capitalized.
 * For example, "hello_world" becomes "Hello World".
 *
 * @return The string with each underscore-separated word capitalized.
 */
private fun String.capitalizeWords(): String {
    return split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
}

/**
 * Composable function for displaying a list of suggested questions.
 *
 * These questions are typically provided by the AI to guide the user's interaction.
 * Each question is displayed as a button, and clicking a question triggers the [onQuestionSelected] callback.
 *
 * @param questions A list of suggested question strings.
 * @param onQuestionSelected Callback function invoked when a suggested question button is clicked,
 *                           passing the selected question string as an argument.
 */
@Composable
fun SuggestedQuestions(
    questions: List<String>,
    onQuestionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp) // Align with AI message body
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE3F2FD),
            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.suggested_questions),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1565C0)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questions.forEach { question ->
                        Button(
                            onClick = { onQuestionSelected(question) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Green40
                            ),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = question,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function for the Data Analysis section of the Clinician screen.
 *
 * This section allows the clinician to trigger an AI-powered analysis of patient data.
 * It displays a button to start the analysis, shows a loading indicator while analysis is in progress,
 * and presents any generated [AiInsight]s in a list of [InsightCard]s.
 * Error messages from the analysis process are also displayed here.
 *
 * @param isAnalyzing A boolean indicating whether data analysis is currently in progress.
 * @param errorMessage An optional string containing an error message if the analysis failed.
 * @param onAnalyzeClick Callback function invoked when the "Discover Patterns" button is clicked.
 * @param insights A list of [AiInsight] objects representing the results of the analysis.
 * @param onInsightClick Callback function invoked when an individual insight card is clicked,
 *                       passing the ID of the selected insight.
 */
@Composable
fun DataAnalysisSection(
    isAnalyzing: Boolean = false,
    errorMessage: String? = null,
    onAnalyzeClick: () -> Unit,
    insights: List<AiInsight> = emptyList(),
    onInsightClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ClinicianViewModel = viewModel()
    // Add a rememberable state to track button clicks for debug
    val (buttonClicked, setButtonClicked) = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Green40.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = stringResource(R.string.ai_powered_analysis),
                    tint = Green40,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ai_powered_analysis),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Analysis Button - made more obvious as a regular Button
        Button(
            onClick = { 
                Log.d("ClinicianScreen", "Analyze button clicked")
                setButtonClicked(true)
                viewModel.clearErrorMessage()
                // Pass the context directly from here
                viewModel.findDataPatterns(context)
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green40),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnalyzing
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.discover_patterns),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = if (isAnalyzing) stringResource(R.string.analyzing_data) else stringResource(R.string.discover_patterns),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        
        // Feedback indicator for debug
        if (buttonClicked && !isAnalyzing) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.button_pressed),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // Display error message if present
        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                border = BorderStroke(1.dp, Color(0xFFE57373)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.analysis_error),
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(24.dp)
                        )
                
                        Spacer(modifier = Modifier.width(8.dp))
                
                        Text(
                            text = stringResource(R.string.analysis_error),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE57373)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
                    
        // If insights are available, show all of them directly
        if (insights.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
            // Analysis Results - now showing all insights with direct click handler
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = stringResource(R.string.ai_analysis_insights),
                            tint = Green40,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = stringResource(R.string.ai_analysis_insights),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Green40
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
            Text(
                            text = stringResource(R.string.insights_found, insights.size),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display all insights directly with click handlers
                    insights.forEach { insight ->
                        InsightCard(
                            title = insight.title,
                            description = insight.description,
                            isNew = insight.isNew,
                            onViewDetails = { onInsightClick(insight.id) },
                            category = insight.category
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } else {
            // Empty state when no insights are available
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (errorMessage != null) stringResource(R.string.analysis_error) else stringResource(R.string.no_insights_available),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage ?: stringResource(R.string.no_insights_description),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                // Clear error before trying again
                                viewModel.clearErrorMessage()
                                onAnalyzeClick() 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Green40)
                        ) {
                            Text(stringResource(R.string.try_again))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function for displaying an individual AI-generated insight card.
 *
 * This card shows the insight's title, a snippet of its description, its category (with a corresponding
 * icon and color), and an indicator if it's a new insight. It also provides a button to view
 * more details about the insight.
 *
 * A `developerMode` flag is included, which, if true, would display additional raw information about
 * the insight. This is currently hardcoded to `false`.
 *
 * @param title The title of the AI insight.
 * @param description A brief description of the AI insight. This will be truncated if too long.
 * @param isNew A boolean indicating whether this insight is new (has not been viewed by the clinician yet).
 * @param onViewDetails Callback function invoked when the "View Details" button is pressed.
 * @param category The category of the insight (e.g., "vegetables", "water"). Defaults to "nutrition".
 *                 This determines the icon and color theming for the card.
 */
@Composable
fun InsightCard(
    title: String,
    description: String,
    isNew: Boolean,
    onViewDetails: () -> Unit,
    category: String = "nutrition"
) {
    // Developer mode flag - set to false for production
    val developerMode = false
    
    // Determine category color
    val categoryColor = when (category.lowercase()) {
        "vegetables" -> Color(0xFF4CAF50) // Green
        "fruits" -> Color(0xFFE91E63) // Pink/Red
        "protein" -> Color(0xFF9C27B0) // Purple
        "water" -> Color(0xFF2196F3) // Blue
        "discretionary" -> Color(0xFFFF9800) // Orange
        else -> Green40 // Default green
    }
    
    // Determine category icon - use available Material icons
    val categoryIcon = when (category.lowercase()) {
        "vegetables" -> Icons.Default.EmojiNature
        "fruits" -> Icons.Default.Spa
        "protein" -> Icons.Default.FitnessCenter
        "water" -> Icons.Default.Water
        "discretionary" -> Icons.Default.Restaurant
        else -> Icons.Default.MenuBook
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isNew) BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                // Category indicator at the top
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = categoryColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = category.capitalize(),
                        color = categoryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show more detailed info in developer mode
                if (developerMode) {
                    Text(
                        text = "Raw description value:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (developerMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Title length: ${title.length} chars | Description length: ${description.length} chars",
                        fontSize = 10.sp,
                        color = Color.Red
                    )
                }
                
                if (isNew) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = categoryColor.copy(alpha = 0.1f),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.new_insight),
                                fontSize = 12.sp,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        TextButton(onClick = onViewDetails) {
                            Text(
                                text = stringResource(R.string.view_details_button),
                                fontSize = 12.sp,
                                color = categoryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onViewDetails) {
                            Text(
                                text = stringResource(R.string.view_details_button),
                                fontSize = 12.sp,
                                color = categoryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            if (isNew) {
                Surface(
                    color = categoryColor,
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.new_label),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Add String extension function to capitalize first letter
/**
 * Private extension function for the [String] class to capitalize the first letter of the string.
 *
 * If the first character is lowercase, it is converted to its title case version.
 * Otherwise, the string is returned unchanged.
 *
 * @return The string with its first letter capitalized, or the original string if it's already capitalized
 *         or empty.
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
        else it.toString() 
    }
}

/**
 * Composable function for displaying a customizable Snackbar to show error messages.
 *
 * The Snackbar includes the error message text, a dismiss button with an icon, and an optional
 * action button (though its text is currently hardcoded to "Dismiss" and might be redundant
 * with the icon button if not differentiated).
 *
 * @param message The error message string to display in the Snackbar.
 * @param onDismiss Callback function invoked when the Snackbar is dismissed, either by the
 *                  action button or the dismiss icon.
 */
@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
            modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        action = {
            TextButton(onClick = onDismiss) {
        Text(
                    text = stringResource(R.string.dismiss),
                    color = Color.White
                )
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
        Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.dismiss),
                    tint = Color.White
                )
            }
        }
    ) {
        Text(text = message)
    }
}

// Reusable components for detailed views

/**
 * A reusable card composable for displaying detailed analysis content within a dialog or section.
 *
 * This card provides a consistent structure with a title, a horizontal divider, and a content area
 * where custom composable content can be injected. An "Close" button is also part of the standard layout.
 *
 * @param title The title to be displayed at the top of the card.
 * @param onClose Callback function invoked when the close button (implicitly part of the dialog structure
 *                that would use this card) is pressed. This parameter is passed down but not directly used
 *                by `DetailedAnalysisCard` itself for rendering a close button.
 * @param titleColor The color for the title text. Defaults to `Green40`.
 * @param content A composable lambda function that defines the main content to be displayed within the card,
 *                below the title and divider.
 */
@Composable
fun DetailedAnalysisCard(
    title: String,
    onClose: () -> Unit,
    titleColor: Color = Green40,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
            
            content()
        }
    }
}

/**
 * A simple composable for displaying a section title with consistent styling.
 *
 * @param title The text of the section title.
 * @param textColor The color of the title text. Defaults to `Green40`.
 */
@Composable
fun SectionTitle(title: String, textColor: Color = Green40) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/**
 * Composable function for displaying a comparative analysis bar for a specific HEIFA component.
 *
 * Shows the component name and visual progress bars representing average scores for male and female patients
 * relative to a maximum possible score for that component. Includes textual display of the scores.
 *
 * @param component The name of the HEIFA component being analyzed (e.g., "Vegetables", "Fruit").
 * @param maleScore The average score for male patients for this component.
 * @param femaleScore The average score for female patients for this component.
 * @param maxScore The maximum possible score for this component.
 */
@Composable
fun ComponentAnalysisBar(
    component: String,
    maleScore: Double,
    femaleScore: Double,
    maxScore: Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = component,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = String.format("%.2f / %.2f (max: %.2f)", maleScore, femaleScore, maxScore),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        
        // Container for both bars with vertical arrangement
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Male score bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(8.dp)
            ) {
                // Male indicator
            Box(
                modifier = Modifier
                        .size(8.dp)
                        .background(Green40, CircleShape)
                ) { }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Male progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                            .fillMaxWidth((maleScore / maxScore).toFloat().coerceIn(0f, 1f))
                            .background(Green40, RoundedCornerShape(4.dp))
                    ) { }
                }
            }
            
            // Female score bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(8.dp)
            ) {
                // Female indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(LightGreen40, CircleShape)
                ) { }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Female progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((femaleScore / maxScore).toFloat().coerceIn(0f, 1f))
                            .background(LightGreen40, RoundedCornerShape(4.dp))
                    ) { }
                }
            }
        }
    }
}

/**
 * Composable function to display a loading or empty state message within a detailed view.
 *
 * Shows an icon, a primary status message (e.g., "Data Unavailable"), and a more detailed secondary message.
 * This is typically used when data for a detailed view (like HEIFA component analysis or AI insight details)
 * is still loading or could not be fetched.
 *
 * @param message The detailed message to display below the primary status text.
 */
@Composable
fun LoadingDetailContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.data_unavailable),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * A general-purpose dialog composable for displaying detailed content.
 *
 * This dialog provides a consistent modal appearance (scrim, rounded surface) and manages dismissal.
 * The actual content of the dialog is provided via a composable lambda.
 * The dialog is scrollable if the content exceeds its maximum height.
 *
 * @param onDismiss Callback function invoked when the dialog is dismissed (e.g., by clicking outside
 *                  or pressing the back button).
 * @param content A composable lambda function that defines the content to be displayed within the dialog.
 */
@Composable
fun DetailDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
                modifier = Modifier
                .fillMaxWidth(0.95f) // Use more screen width
                .heightIn(max = 650.dp) // Increase max height
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp) // Increase internal padding
            ) {
                content()
            }
        }
    }
}

// New Detailed View Components

/**
 * Composable function for displaying a detailed view of HEIFA score analysis.
 *
 * This view is typically shown within a [DetailDialog]. It presents a breakdown of HEIFA component
 * scores for male and female patients, using [ComponentAnalysisBar] for each component.
 * It includes a section title, a description of score distribution, and a legend for the gender-specific bars.
 * If no component analysis data is available, it shows a loading/empty state message.
 *
 * @param componentAnalysis A list of [ComponentAnalysis] objects containing the data for each HEIFA component.
 * @param onClose Callback function invoked when the close action is triggered (though the close button
 *                is usually part of the containing [DetailDialog] or [DetailedAnalysisCard]).
 */
@Composable
fun HeifaScoreDetailedView(
    componentAnalysis: List<ComponentAnalysis>,
    onClose: () -> Unit
) {
    DetailedAnalysisCard(
        title = stringResource(R.string.heifa_score_analysis),
        onClose = onClose
    ) {
        if (componentAnalysis.isNotEmpty()) {
            // Display real data from componentAnalysis
            // Distribution
            SectionTitle(title = stringResource(R.string.score_distribution))
            Text(
                text = stringResource(R.string.score_distribution_desc),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Component breakdown
            SectionTitle(title = stringResource(R.string.component_analysis))
            
            // Add legend at the top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Green40, CircleShape)
                    ) { }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.male),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(LightGreen40, CircleShape)
                    ) { }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.female),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // List of components we want to display in order
            val componentsToShow = listOf("Vegetables", "Fruits", "Grains", "Protein", "Dairy", "Water", "Sodium")
            
            // Get max scores based on component name
            val getMaxScore = { component: String ->
                when (component) {
                    "Protein", "Dairy", "Sodium" -> 10.0
                    else -> 5.0
                }
            }
            
            // Filter and sort components according to our preferred order
            componentAnalysis.filter { component -> 
                componentsToShow.contains(component.component)
            }.sortedBy { component ->
                componentsToShow.indexOf(component.component)
            }.forEach { component ->
                val maxScore = getMaxScore(component.component)
                ComponentAnalysisBar(
                    component = component.component,
                    maleScore = component.maleScore,
                    femaleScore = component.femaleScore,
                    maxScore = maxScore
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            // No data available
            LoadingDetailContent(
                message = stringResource(R.string.data_unavailable_message)
            )
        }
    }
}

/**
 * Composable function for displaying a detailed view of a specific AI-generated insight.
 *
 * This view is typically shown within a [DetailDialog]. It presents the full title and description
 * of the [AiInsight], along with its category (displayed with an icon and themed color).
 * A `developerMode` flag (currently hardcoded to `false`) can enable the display of additional raw data.
 *
 * @param insight The [AiInsight] object containing the data to be displayed.
 * @param onClose Callback function invoked when the close action is triggered (though the close button
 *                is usually part of the containing [DetailDialog] or [DetailedAnalysisCard]).
 */
@Composable
fun AiInsightDetailView(
    insight: AiInsight,
    onClose: () -> Unit
) {
    // Developer mode flag - set to false for production
    val developerMode = false
    
    // Determine category color
    val categoryColor = when (insight.category.lowercase()) {
        "vegetables" -> Color(0xFF4CAF50) // Green
        "fruits" -> Color(0xFFE91E63) // Pink/Red
        "protein" -> Color(0xFF9C27B0) // Purple
        "water" -> Color(0xFF2196F3) // Blue
        "discretionary" -> Color(0xFFFF9800) // Orange
        else -> Green40 // Default green
    }
    
    // Determine category icon - use available Material icons
    val categoryIcon = when (insight.category.lowercase()) {
        "vegetables" -> Icons.Default.EmojiNature
        "fruits" -> Icons.Default.Spa
        "protein" -> Icons.Default.FitnessCenter
        "water" -> Icons.Default.Water
        "discretionary" -> Icons.Default.Restaurant
        else -> Icons.Default.MenuBook
    }
    
    DetailedAnalysisCard(
        title = insight.title,
        onClose = onClose,
        titleColor = categoryColor
    ) {
        // Category tag with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = categoryColor.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.padding(6.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                color = categoryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = insight.category.capitalize(),
                    color = categoryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        
        // Debug info in developer mode
        if (developerMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFE57373)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "DEBUG INFO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "ID: ${insight.id}",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Title length: ${insight.title.length}",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Description length: ${insight.description.length}",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Category: ${insight.category}",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Recommendations: ${insight.recommendations.size}",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        SectionTitle(title = stringResource(R.string.analysis), textColor = categoryColor)
        Text(
            text = insight.description,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Patient count
        Text(
            text = stringResource(R.string.based_on_data, insight.patientCount),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommendations
        if (insight.recommendations.isNotEmpty()) {
            SectionTitle(title = stringResource(R.string.recommendations), textColor = categoryColor)
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                insight.recommendations.forEach { recommendation ->
                    RecommendationItem(text = recommendation, iconColor = categoryColor)
                }
            }
        } else if (developerMode) {
            Text(
                text = stringResource(R.string.no_recommendations),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Red
            )
        }
    }
}

@Composable
fun RecommendationItem(text: String, iconColor: Color = Green40) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
} 