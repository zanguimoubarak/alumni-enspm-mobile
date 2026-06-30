package com.enspm.alumni.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlumniLightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF4F46E5),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF020817),
    onSurface = Color(0xFF020817),
    outline = Color(0xFFE2E8F0),
)

private val AlumniDarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    secondary = Color(0xFF818CF8),
    background = Color(0xFF020817),
    surface = Color(0xFF0F172A),
    onPrimary = Color(0xFF020817),
    onSecondary = Color(0xFF020817),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF334155),
)

@Composable
fun AlumniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AlumniDarkColors else AlumniLightColors,
        content = content,
    )
}
