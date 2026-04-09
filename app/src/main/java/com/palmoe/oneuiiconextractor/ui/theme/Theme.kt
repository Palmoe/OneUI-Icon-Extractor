package com.palmoe.oneuiiconextractor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealBright,
    onPrimary = Dawn,
    secondary = Sage,
    onSecondary = Night,
    tertiary = Coral,
    background = Night,
    onBackground = Dawn,
    surface = NightSurface,
    onSurface = Dawn,
    surfaceVariant = Ink,
    onSurfaceVariant = Color(0xFFC7D0D7),
    primaryContainer = Color(0xFF214B50),
    onPrimaryContainer = Dawn,
    secondaryContainer = Color(0xFF38483F),
    onSecondaryContainer = Dawn,
    outline = Color(0xFF4E5D66),
    outlineVariant = Color(0xFF2A3840)
)

private val LightColorScheme = lightColorScheme(
    primary = TealDeep,
    onPrimary = Paper,
    secondary = Moss,
    onSecondary = Paper,
    tertiary = Coral,
    onTertiary = Paper,
    background = Dawn,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Slate,
    primaryContainer = Color(0xFFCEE7E7),
    onPrimaryContainer = Ink,
    secondaryContainer = Color(0xFFDCE6DE),
    onSecondaryContainer = Ink,
    outline = Color(0xFFB5C1B7),
    outlineVariant = Color(0xFFD6DED7)
)

@Composable
fun OneUIIconExtractorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
