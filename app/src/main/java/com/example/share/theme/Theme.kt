package com.example.share.theme

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
    primary = GreenDarkPrimary,
    secondary = Meadow,
    tertiary = AccentGold,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF24313D),
    outline = Color(0xFF314253),
    onPrimary = SurfaceLight,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = Color(0xFFB8C4D1)
)

private val LightColorScheme = lightColorScheme(
    primary = Evergreen,
    primaryContainer = MintWash,
    secondary = EvergreenDeep,
    tertiary = AccentGold,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Linen,
    outline = BorderSoft,
    onPrimary = SurfaceLight,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = InkSoft
)

@Composable
fun SHARETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamicColor to preserve branding consistency
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
