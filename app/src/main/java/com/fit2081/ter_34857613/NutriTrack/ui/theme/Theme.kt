package com.fit2081.ter_34857613.NutriTrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Defines the dark color scheme for the NutriTrack application.
 * This color scheme is used when the system is in dark theme or if explicitly chosen.
 *
 * It sets the primary, secondary, and tertiary colors using predefined green shades:
 * - `primary`: [Green80]
 * - `secondary`: [GreenGrey80]
 * - `tertiary`: [LightGreen80]
 */
private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = LightGreen80
)

/**
 * Defines the light color scheme for the NutriTrack application.
 * This color scheme is used when the system is in light theme or if explicitly chosen,
 * and dynamic color is not available or not enabled.
 *
 * It sets the primary, secondary, and tertiary colors using predefined green shades:
 * - `primary`: [Green40]
 * - `secondary`: [GreenGrey40]
 * - `tertiary`: [LightGreen40]
 *
 * Other default Material 3 colors (like `background`, `surface`, `onPrimary`, etc.)
 * can be overridden here as needed by uncommenting and customizing their definitions.
 */
private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * The main theme composable for the NutriTrack application.
 *
 * This function applies a [MaterialTheme] to its `content`. It determines the appropriate
 * color scheme (light or dark) based on the system settings (`isSystemInDarkTheme`) and
 * whether dynamic coloring is enabled and available (Android 12+).
 *
 * - If `dynamicColor` is true and the device is Android 12+, it uses `dynamicDarkColorScheme`
 *   or `dynamicLightColorScheme` to generate a color scheme from the user's wallpaper.
 * - Otherwise, it falls back to the predefined [DarkColorScheme] or [LightColorScheme].
 *
 * It also includes a [SideEffect] to set the system status bar color and appearance
 * (light/dark icons) to match the chosen theme. This effect is skipped when in preview mode.
 *
 * @param darkTheme A boolean indicating whether to force the dark theme. Defaults to the system's
 *                  dark theme setting (`isSystemInDarkTheme()`).
 * @param dynamicColor A boolean indicating whether to enable dynamic coloring on Android 12+ devices.
 *                     Defaults to `true`. If false, or on older devices, predefined color schemes are used.
 * @param content The composable content to which this theme will be applied.
 */
@Composable
fun NutriTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Update the status bar color using the WindowCompat API instead of directly setting statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Set the status bar color using the window's decorView
            window.decorView.setBackgroundColor(colorScheme.primary.toArgb())
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}