package com.enspm.alumni.ui.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun AlumniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlumniLightColors,
        content = content,
    )
}
