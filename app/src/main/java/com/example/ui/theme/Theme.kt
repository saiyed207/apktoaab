package com.example.ui.theme

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
    primary = PrimaryBlueLight,
    onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = PrimaryCyanLight,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFA6EEFF),
    tertiary = AccentEmeraldLight,
    onTertiary = Color(0xFF00391C),
    tertiaryContainer = Color(0xFF00532B),
    onTertiaryContainer = Color(0xFF8CF8B4),
    background = PlayBlueDark,
    onBackground = TextPrimaryDark,
    surface = PlaySurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = PlayCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = PlayCardBorderDark,
    outlineVariant = Color(0xFF26334D),
    error = AccentRoseLight,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = PrimaryCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F3F6),
    onSecondaryContainer = Color(0xFF002024),
    tertiary = AccentEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCEF5DF),
    onTertiaryContainer = Color(0xFF00210E),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = AccentRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

