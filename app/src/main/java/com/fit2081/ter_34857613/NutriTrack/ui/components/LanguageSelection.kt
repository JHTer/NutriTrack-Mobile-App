package com.fit2081.ter_34857613.NutriTrack.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fit2081.ter_34857613.NutriTrack.R
import com.fit2081.ter_34857613.NutriTrack.utils.LocaleHelper

/**
 * A composable function that displays a dialog for selecting the application language.
 *
 * The dialog presents a list of available languages (English, Malay, Chinese, Japanese, French).
 * The currently selected language is indicated with a radio button.
 * Users can select a new language, and the [onLanguageSelected] callback will be invoked with the
 * language code (e.g., "en", "ms"). The dialog can be dismissed using a "Cancel" button or by
 * clicking outside the dialog.
 *
 * This dialog uses the [LanguageOptions] composable to display the list of languages.
 *
 * @param showDialog A boolean state indicating whether the dialog should be shown.
 * @param onDismiss Callback function invoked when the dialog is dismissed (e.g., by clicking cancel
 *                  or outside the dialog).
 * @param onLanguageSelected Callback function invoked when a language is selected from the list.
 *                           It provides the selected language code (e.g., "en", "ms").
 */
@Composable
fun LanguageSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Text(stringResource(R.string.select_language))
            },
            text = {
                LanguageOptions(
                    onLanguageSelected = onLanguageSelected
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
            dismissButton = null
        )
    }
}

/**
 * A private composable function that displays a scrollable list of language options.
 *
 * Each language is presented using the [LanguageOption] composable. It determines the currently
 * stored locale using [LocaleHelper] to correctly indicate the active language.
 * The list includes predefined languages: English, Bahasa Melayu, 中文 (Chinese), 日本語 (Japanese),
 * and Français (French).
 *
 * @param onLanguageSelected Callback function passed down to each [LanguageOption] to handle
 *                           the selection of a language. It provides the language code.
 */
@Composable
private fun LanguageOptions(
    onLanguageSelected: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLanguage = remember { LocaleHelper.getStoredLocale(context) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LanguageOption(
            name = "English",
            code = "en",
            currentLanguage = currentLanguage,
            onSelected = { 
                onLanguageSelected(it)
            }
        )
        
        LanguageOption(
            name = "Bahasa Melayu",
            code = "ms",
            currentLanguage = currentLanguage,
            onSelected = {
                onLanguageSelected(it)
            }
        )
        
        LanguageOption(
            name = "中文",
            code = "zh",
            currentLanguage = currentLanguage,
            onSelected = {
                onLanguageSelected(it)
            }
        )
        
        LanguageOption(
            name = "日本語",
            code = "ja",
            currentLanguage = currentLanguage,
            onSelected = {
                onLanguageSelected(it)
            }
        )
        
        LanguageOption(
            name = "Français",
            code = "fr",
            currentLanguage = currentLanguage,
            onSelected = {
                onLanguageSelected(it)
            }
        )
    }
}

/**
 * A private composable function that displays a single language option item.
 *
 * This item consists of a [RadioButton] to indicate selection and a [Text] label showing the
 * language name (e.g., "English", "日本語"). It becomes selected if its `code` matches the `currentLanguage`.
 * Clicking on the row triggers the [onSelected] callback with the language `code`.
 *
 * @param name The display name of the language (e.g., "English", "Français").
 * @param code The language code for this option (e.g., "en", "fr").
 * @param currentLanguage The language code of the currently active application language.
 * @param onSelected Callback function invoked when this language option is clicked.
 *                   It provides the language `code` of this option.
 */
@Composable
private fun LanguageOption(
    name: String,
    code: String,
    currentLanguage: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(code) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = code == currentLanguage,
            onClick = { onSelected(code) }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(text = name)
    }
} 