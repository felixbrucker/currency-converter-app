package com.felixbrucker.currencyconverter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurpleDark,
    onPrimary = OnPrimaryPurpleDark,
    primaryContainer = PrimaryPurpleContainerDark,
    onPrimaryContainer = OnPrimaryPurpleContainerDark,
    secondary = SecondaryLilacDark,
    onSecondary = OnSecondaryLilacDark,
    secondaryContainer = SecondaryLilacContainerDark,
    onSecondaryContainer = OnSecondaryLilacContainerDark,
    background = SurfaceDark,
    surface = CardBackgroundDark,
    surfaceVariant = SurfaceContainerDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = OnPrimaryPurple,
    primaryContainer = PrimaryPurpleContainer,
    onPrimaryContainer = OnPrimaryPurpleContainer,
    secondary = SecondaryLilac,
    onSecondary = OnSecondaryLilac,
    secondaryContainer = SecondaryLilacContainer,
    onSecondaryContainer = OnSecondaryLilacContainer,
    background = SurfaceMinimal,
    surface = CardBackgroundMinimal,
    surfaceVariant = SurfaceContainerMinimal,
    onBackground = TextPrimaryMinimal,
    onSurface = TextPrimaryMinimal,
    onSurfaceVariant = TextSecondaryMinimal,
    outline = BorderMinimal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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

