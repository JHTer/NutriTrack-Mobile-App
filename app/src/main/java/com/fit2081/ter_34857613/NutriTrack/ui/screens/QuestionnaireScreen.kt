package com.fit2081.ter_34857613.NutriTrack.ui.screens

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.model.repository.FoodPreferences
import com.fit2081.ter_34857613.NutriTrack.viewmodel.QuestionnaireViewModel
import com.fit2081.ter_34857613.NutriTrack.viewmodel.Persona
import java.util.Calendar
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.layout.WindowInsets
import com.fit2081.ter_34857613.NutriTrack.utils.LocalAppLocale

/**
 * Composable function for the user questionnaire screen.
 *
 * This screen collects information about the user's dietary habits and preferences.
 * It includes sections for:
 * - Selecting preferred food categories (e.g., fruits, vegetables, meats).
 * - Choosing a dietary persona that best represents their eating style.
 * - Specifying typical times for their biggest meal, sleep, and waking up.
 *
 * The screen uses a [QuestionnaireViewModel] to manage the state of selected preferences,
 * load/save data, and handle persona details display. User selections are persisted via
 * SharedPreferences specific to the `userId`.
 * Language changes are detected via [LocalAppLocale] and handled by the ViewModel to reload
 * localized persona names if necessary.
 *
 * @param userId The unique identifier of the user for whom the questionnaire is being filled.
 *               Used for saving and loading preferences.
 * @param onNavigateBack Callback function to navigate to the previous screen.
 * @param onSave Callback function invoked when the user saves their preferences. It provides
 *               the completed [FoodPreferences] object.
 * @param viewModel The [QuestionnaireViewModel] instance for this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onSave: (FoodPreferences) -> Unit,
    viewModel: QuestionnaireViewModel = viewModel()
) {
    // Access the context for SharedPreferences operations
    val context = LocalContext.current
    
    // Track the current locale to detect language changes
    val currentLocale = LocalAppLocale.current
    
    // Effect that runs when locale changes
    LaunchedEffect(currentLocale) {
        viewModel.onLanguageChanged(context)
    }
    
    // Load saved preferences when screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadPreferences(context, userId)
    }


    // Main screen scaffold with app bar and content
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.questionnaire_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                // Set window insets to zero to prevent double spacing
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        // Use zero insets for the scaffold as well
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // Main scrollable content
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Food Categories Section
            Text(
                text = stringResource(id = R.string.food_categories),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Text(
                text = stringResource(id = R.string.food_categories_hint),
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 3x3 grid of food category checkboxes
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                userScrollEnabled = false
            ) {
                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_fruits),
                        checked = viewModel.fruitsSelected,
                        onCheckedChange = { viewModel.fruitsSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_vegetables),
                        checked = viewModel.vegetablesSelected,
                        onCheckedChange = { viewModel.vegetablesSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_grains),
                        checked = viewModel.grainsSelected,
                        onCheckedChange = { viewModel.grainsSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_red_meat),
                        checked = viewModel.redMeatSelected,
                        onCheckedChange = { viewModel.redMeatSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_seafood),
                        checked = viewModel.seafoodSelected,
                        onCheckedChange = { viewModel.seafoodSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_poultry),
                        checked = viewModel.poultrySelected,
                        onCheckedChange = { viewModel.poultrySelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_fish),
                        checked = viewModel.fishSelected,
                        onCheckedChange = { viewModel.fishSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_eggs),
                        checked = viewModel.eggsSelected,
                        onCheckedChange = { viewModel.eggsSelected = it }
                    )
                }

                item {
                    CategoryCheckbox(
                        text = stringResource(id = R.string.food_category_nuts_seeds),
                        checked = viewModel.nutsSeedsSelected,
                        onCheckedChange = { viewModel.nutsSeedsSelected = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Persona Selection Section
            Text(
                text = stringResource(id = R.string.persona_selection),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = stringResource(id = R.string.persona_selection_hint),
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Persona cards grid - 2 columns, 3 rows
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp) // Increased height to ensure all personas are fully visible
                    .clip(RoundedCornerShape(0.dp)),
                userScrollEnabled = false // Disable grid scrolling (parent column scrolls)
            ) {
                items(viewModel.personas.size) { index ->
                    val persona = viewModel.personas[index]
                    PersonaCard(
                        persona = persona,
                        selected = viewModel.selectedPersona?.id == persona.id,
                        onClick = {
                            viewModel.showPersonaDetails(persona)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Best-fitting persona dropdown selector
            Text(
                text = stringResource(id = R.string.best_fitting_persona),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dropdown for selecting persona from a list
            ExposedDropdownMenuBox(
                expanded = viewModel.expanded,
                onExpandedChange = { viewModel.toggleExpanded() }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedPersona?.name ?: stringResource(id = R.string.persona_select_prompt),
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.show_options)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type    = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green40,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                // Dropdown menu options
                ExposedDropdownMenu(
                    expanded = viewModel.expanded,
                    onDismissRequest = { viewModel.expanded = false }
                ) {
                    viewModel.personas.forEach { persona ->
                        DropdownMenuItem(
                            text = { Text(persona.name) },
                            onClick = {
                                viewModel.selectPersona(persona)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Timings Section
            Text(
                text = stringResource(id = R.string.timings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Time input fields for daily schedule
            TimeInputField(
                label = stringResource(id = R.string.meal_time_question),
                value = viewModel.biggestMealTime,
                onShowTimePicker = { viewModel.showBiggestMealTimePicker = true }
            )

            TimeInputField(
                label = stringResource(id = R.string.sleep_time_question),
                value = viewModel.sleepTime,
                onShowTimePicker = { viewModel.showSleepTimePicker = true }
            )

            TimeInputField(
                label = stringResource(id = R.string.wake_time_question),
                value = viewModel.wakeUpTime,
                onShowTimePicker = { viewModel.showWakeUpTimePicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button - collects and stores all preferences
            Button(
                onClick = {
                    // Validate time selections first
                    if (viewModel.validateTimes()) {
                        // Save preferences via ViewModel and notify parent component
                        val preferences = viewModel.savePreferences(context, userId)
                        onSave(preferences)
                    } else {
                        // Display a toast message since we're not changing the UI
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.time_validation_error),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_seedling),
                    contentDescription = stringResource(id = R.string.save),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.save_with_icon),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp)) // Add bottom padding
        }
    }

    // Time picker dialog for biggest meal time selection
    if (viewModel.showBiggestMealTimePicker) {
        // Display native Android time picker dialog
        ShowTimePickerDialog(
            initialTime = viewModel.biggestMealTime,
            onTimeSelected = { time -> viewModel.updateBiggestMealTime(time) },
            onDismiss = { viewModel.showBiggestMealTimePicker = false }
        )
    }

    // Time picker dialog for sleep time selection
    if (viewModel.showSleepTimePicker) {
        ShowTimePickerDialog(
            initialTime = viewModel.sleepTime,
            onTimeSelected = { time -> viewModel.updateSleepTime(time) },
            onDismiss = { viewModel.showSleepTimePicker = false }
        )
    }

    // Time picker dialog for wake-up time selection
    if (viewModel.showWakeUpTimePicker) {
        ShowTimePickerDialog(
            initialTime = viewModel.wakeUpTime,
            onTimeSelected = { time -> viewModel.updateWakeUpTime(time) },
            onDismiss = { viewModel.showWakeUpTimePicker = false }
        )
    }

    // Persona Detail Modal - shows detailed information about selected persona
    if (viewModel.showPersonaModal && viewModel.currentPersonaDetails != null) {
        PersonaDetailModal(
            persona = viewModel.currentPersonaDetails!!,
            onDismiss = { viewModel.hidePersonaModal() },
            onSelect = {
                viewModel.selectPersona(viewModel.currentPersonaDetails!!)
            },
            onUnselect = {
                viewModel.unselectPersona()
            }
        )
    }
}

/**
 * Composable function for a time input field that allows users to select a time.
 *
 * Displays a label, and an input field showing the currently selected time (or a placeholder).
 * Clicking the field triggers the onShowTimePicker callback, which can be used to show a time picker.
 *
 * @param label The text label displayed above the time input field.
 * @param value A string representing the currently selected time (e.g., "10:00"). If empty, a placeholder is shown.
 * @param onShowTimePicker Callback function invoked when the field is clicked to show a time picker.
 */
@Composable
fun TimeInputField(label: String, value: String, onShowTimePicker: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Button styled as input field that triggers time picker
        Button(
            onClick = onShowTimePicker,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isEmpty()) stringResource(id = R.string.select_time) else value,
                    color = if (value.isEmpty()) Color.Gray else Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = stringResource(id = R.string.select_time),
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Helper composable to display time picker dialog
 *
 * @param initialTime Initial time value in format "HH:MM"
 * @param onTimeSelected Callback when time is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
private fun ShowTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parse initial time if available
    var hour = calendar.get(Calendar.HOUR_OF_DAY)
    var minute = calendar.get(Calendar.MINUTE)

    if (initialTime.isNotEmpty() && initialTime.contains(":")) {
        try {
            val parts = initialTime.split(":")
            hour = parts[0].toInt()
            minute = parts[1].toInt()
        } catch (_: Exception) {
            // Use default if parsing fails
        }
    }

    // Create and show time picker dialog
    val dialog = TimePickerDialog(
        context,
        android.R.style.Theme_Material_Dialog_NoActionBar,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
        },
        hour,
        minute,
        DateFormat.is24HourFormat(context)
    )

    // Show the dialog when the composable enters composition
    LaunchedEffect(Unit) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Ensure dialog is dismissed when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            dialog.dismiss()
        }
    }
}

/**
 * Composable function for a checkbox item used for selecting food categories.
 *
 * Displays a text label and a checkbox. The layout is designed to fit well within a grid.
 * The background color changes based on the checked state.
 *
 * @param text The label for the food category (e.g., "Fruits", "Vegetables").
 * @param checked A boolean indicating whether this category is currently selected.
 * @param onCheckedChange Callback function invoked when the checked state of the checkbox changes.
 * @param modifier Modifier for this composable.
 */
@Composable
fun CategoryCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = Green40,
                uncheckedColor = Color.Gray
            )
        )

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 4.dp),
            maxLines = 1
        )
    }
}

/**
 * Composable function for displaying a card representing a dietary persona.
 *
 * Each card shows the persona's name, an image, and a brief description.
 * The card has a visual indication (border and background) if it is currently selected.
 * Clicking the card triggers the [onClick] callback, typically to show more details or select the persona.
 *
 * @param persona The [Persona] object containing data for this card (name, description, image resource ID).
 * @param selected A boolean indicating whether this persona is currently selected.
 * @param onClick Callback function invoked when the persona card is clicked.
 */
@Composable
fun PersonaCard(
    persona: Persona,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Get fresh translation for each persona name using stringResource
    val translatedName = when (persona.id) {
        1 -> stringResource(id = R.string.persona_health_devotee)
        2 -> stringResource(id = R.string.persona_mindful_eater)
        3 -> stringResource(id = R.string.persona_wellness_striver)
        4 -> stringResource(id = R.string.persona_balance_seeker)
        5 -> stringResource(id = R.string.persona_health_procrastinator)
        6 -> stringResource(id = R.string.persona_food_carefree)
        else -> stringResource(id = R.string.not_available)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) Green40 else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Persona avatar image
            Image(
                painter = painterResource(id = persona.imageResource),
                contentDescription = translatedName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Use translated name instead of persona.name
            Text(
                text = translatedName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Composable function for displaying a dialog with detailed information about a selected persona.
 *
 * The dialog shows the persona's image, name, and full description. It provides a button
 * to select this persona and a button to close the dialog.
 *
 * @param persona The [Persona] object whose details are to be displayed.
 * @param onDismiss Callback function invoked when the dialog is dismissed (e.g., by clicking outside
 *                  or the "Close" button).
 * @param onSelect Callback function invoked when the "Select this Persona" button is clicked.
 */
@Composable
fun PersonaDetailModal(
    persona: Persona,
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    onUnselect: () -> Unit
) {
    // Get fresh translations for persona name and description
    val translatedName = when (persona.id) {
        1 -> stringResource(id = R.string.persona_health_devotee)
        2 -> stringResource(id = R.string.persona_mindful_eater)
        3 -> stringResource(id = R.string.persona_wellness_striver)
        4 -> stringResource(id = R.string.persona_balance_seeker)
        5 -> stringResource(id = R.string.persona_health_procrastinator)
        6 -> stringResource(id = R.string.persona_food_carefree)
        else -> stringResource(id = R.string.not_available)
    }

    val translatedDescription = when (persona.id) {
        1 -> stringResource(id = R.string.persona_health_devotee_desc)
        2 -> stringResource(id = R.string.persona_mindful_eater_desc)
        3 -> stringResource(id = R.string.persona_wellness_striver_desc)
        4 -> stringResource(id = R.string.persona_balance_seeker_desc)
        5 -> stringResource(id = R.string.persona_health_procrastinator_desc)
        6 -> stringResource(id = R.string.persona_food_carefree_desc)
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Colored header with persona name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(persona.color)
                        .padding(16.dp)
                ) {
                    Text(
                        text = translatedName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Dialog content with persona details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Persona avatar
                    Image(
                        painter = painterResource(id = persona.imageResource),
                        contentDescription = translatedName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Persona description
                    Text(
                        text = translatedDescription,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Select button
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = persona.color
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.select_persona))
                    }

                    // Dismiss button
                    OutlinedButton(
                        onClick = onUnselect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            }
        }
    }
}