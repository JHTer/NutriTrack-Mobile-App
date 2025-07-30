package com.fit2081.ter_34857613.NutriTrack.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit2081.ter_34857613.NutriTrack.ui.theme.Green40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.GreenGrey40
import com.fit2081.ter_34857613.NutriTrack.ui.theme.LightGreen40

/**
 * A reusable card composable designed for authentication screens (e.g., Login, Sign Up, Verification).
 *
 * This card provides a consistent visual structure with a prominent title, an optional subtitle,
 * and a content area where specific input fields and buttons for the authentication flow can be placed.
 * It features rounded corners, elevation, and themed colors.
 *
 * @param title The main title text to be displayed at the top of the card (e.g., "Login", "Create Account").
 * @param subtitle An optional subtitle text displayed below the main title for additional context.
 * @param modifier Optional [Modifier] to be applied to the `Card`.
 * @param content A composable lambda function that defines the content to be displayed within the card,
 *                typically including input fields ([AuthTextField], [PasswordTextField]), buttons ([AuthButton]),
 *                and error messages ([ErrorMessage]).
 */
@Composable
fun AuthCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Title and subtitle
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Green40
                )
            )
            
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main content
            content()
        }
    }
}

/**
 * A styled `OutlinedTextField` wrapper for use in authentication forms.
 *
 * This component provides a consistent look and feel for text input fields across various
 * authentication screens. It includes a label, placeholder, error display, and customizable
 * keyboard options, actions, and icons.
 *
 * @param value The current text value of the input field.
 * @param onValueChange Callback function invoked when the text value changes.
 * @param label The text label displayed floating above or inside the field.
 * @param modifier Optional [Modifier] to be applied to the `Column` wrapping the `OutlinedTextField`.
 * @param placeholder Optional placeholder text displayed when the field is empty.
 * @param isError A boolean indicating whether the current input value is invalid.
 * @param errorMessage An optional error message string to display below the field when `isError` is true.
 * @param keyboardOptions [KeyboardOptions] to configure the keyboard type (e.g., Text, Email, Password)
 *                        and IME action (e.g., Next, Done).
 * @param keyboardActions [KeyboardActions] to define actions triggered by IME actions (e.g., moving focus,
 *                        submitting the form).
 * @param leadingIcon An optional composable lambda to display a leading icon within the text field.
 * @param trailingIcon An optional composable lambda to display a trailing icon (e.g., password visibility toggle).
 * @param visualTransformation [VisualTransformation] for masking input, typically used for passwords.
 *                             Defaults to [VisualTransformation.None].
 * @param focusRequester An optional [FocusRequester] to programmatically control focus on this field.
 * @param readOnly A boolean indicating whether the field should be read-only. Defaults to `false`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester? = null,
    readOnly: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                ),
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green40,
                unfocusedBorderColor = GreenGrey40,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            readOnly = readOnly
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * A specialized version of [AuthTextField] specifically for password input.
 *
 * This component extends [OutlinedTextField] and includes a built-in trailing icon (TextButton)
 * to toggle password visibility (showing either "Show" or "Hide").
 * It uses [PasswordVisualTransformation] by default to mask the input.
 *
 * @param value The current password string value.
 * @param onValueChange Callback function invoked when the password value changes.
 * @param label The text label for the password field.
 * @param modifier Optional [Modifier] to be applied to the `Column` wrapping the `OutlinedTextField`.
 * @param isError A boolean indicating whether the current password input is invalid.
 * @param errorMessage An optional error message string to display below the field when `isError` is true.
 * @param keyboardActions [KeyboardActions] to define actions triggered by IME actions.
 * @param imeAction The IME action for the keyboard (e.g., Next, Done). Defaults to [ImeAction.Done].
 * @param focusRequester An optional [FocusRequester] to programmatically control focus on this field.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done,
    focusRequester: FocusRequester? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                ),
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = GreenGrey40,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green40,
                unfocusedBorderColor = GreenGrey40,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * A composable that provides a dropdown menu for selecting from a list of options, styled for authentication screens.
 *
 * This component displays the currently selected value (or a placeholder if none is selected) in an
 * [OutlinedTextField]-like container. Clicking it reveals a dropdown menu ([ExposedDropdownMenu])
 * with the provided `options`. It also supports displaying an error state.
 *
 * @param value The currently selected string value from the `options` list.
 * @param options A list of string options to be displayed in the dropdown menu.
 * @param onSelectionChanged Callback function invoked when a new option is selected from the dropdown.
 *                           It provides the selected string value.
 * @param label The text label displayed for the dropdown selector.
 * @param modifier Optional [Modifier] to be applied to the `Column` wrapping the `ExposedDropdownMenuBox`.
 * @param isError A boolean indicating whether the selector is in an error state (e.g., no selection made when required).
 * @param errorMessage An optional error message string to display below the selector when `isError` is true.
 * @param placeholder The placeholder text to display when no option is selected. Defaults to "Select an option".
 * @param enabled A boolean indicating whether the dropdown selector is enabled. Defaults to `true`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    value: String,
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Log available options for debugging
        LaunchedEffect(options) {
            Log.d("DropdownSelector", "Available options: ${options.joinToString()}")
        }
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = GreenGrey40
                    )
                },
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green40,
                    unfocusedBorderColor = GreenGrey40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            
            // Transparent clickable surface above the TextField
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = { expanded = true })
            )
        }
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No options available") },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelectionChanged(option)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

/**
 * A styled [Button] composable for primary actions on authentication screens (e.g., Login, Sign Up, Continue).
 *
 * This button features consistent styling (background color, shape, text style) and includes a loading
 * indicator ([CircularProgressIndicator]) that is displayed when `isLoading` is true, disabling the button
 * during the loading state.
 *
 * @param text The text to be displayed on the button.
 * @param onClick Callback function invoked when the button is clicked.
 * @param modifier Optional [Modifier] to be applied to the `Button`.
 * @param isLoading A boolean indicating whether an operation is in progress (e.g., network request).
 *                  When true, a loading indicator is shown, and the button is disabled.
 * @param enabled A boolean indicating whether the button is enabled. Defaults to `true`.
 *                This is overridden by `isLoading` (if `isLoading` is true, the button is always disabled).
 */
@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green40,
            disabledContainerColor = Green40.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Secondary action text button with text and callback
 * 
 * @param text Text to display
 * @param onClick Callback when text is clicked
 */
@Composable
fun TextActionLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Green40,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}

/**
 * Divider with text in the middle, commonly used for "or" dividers
 * 
 * @param text Text to display in divider
 */
@Composable
fun TextDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

/**
 * A composable for displaying an error message, typically within authentication forms.
 *
 * This component shows an error icon and the error message text with specific styling
 * (e.g., error color) to clearly indicate an issue to the user.
 *
 * @param message The error message string to be displayed.
 * @param icon The [ImageVector] to be displayed as an error icon next to the message.
 * @param modifier Optional [Modifier] to be applied to the `Row` containing the icon and text.
 */
@Composable
fun ErrorMessage(
    message: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Helper text display for additional information
 * 
 * @param text Text to display
 */
@Composable
fun HelperText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * A composable that displays a list of password requirements and indicates whether each is met by the current password input.
 *
 * This is used to guide users when creating a new password, showing criteria such as minimum length,
 * inclusion of uppercase letters, numbers, and special characters. Each requirement is visually marked
 * (e.g., with a checkmark or different color) if the provided `password` string satisfies it.
 *
 * @param requirements A list of strings, where each string describes a password requirement (e.g., "At least 8 characters").
 * @param password The current password string being validated against the requirements.
 * @param isPasswordValid A lambda function that takes a requirement string and the current password string,
 *                        and returns `true` if the password meets that specific requirement, `false` otherwise.
 *                        This allows for flexible validation logic defined externally.
 * @param modifier Optional [Modifier] to be applied to the `Column` containing the list of requirements.
 */
@Composable
fun PasswordRequirements(
    requirements: List<String>,
    password: String,
    isPasswordValid: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    // Implementation of PasswordRequirements composable
} 