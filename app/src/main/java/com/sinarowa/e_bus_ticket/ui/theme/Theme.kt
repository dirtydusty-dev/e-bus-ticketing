package com.sinarowa.e_bus_ticket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define theme colors
val SkyBluePrimary = Color(0xFF81D4FA) // Sky blue
val YellowSecondary = Color(0xFFFFC107) // Yellow
val LightBlueBackground = Color(0xFFE1F5FE) // Very light blue for background
val GrayDisabled = Color(0xFFB0BEC5) // Soft gray for disabled states

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SkyBluePrimary,
            secondary = YellowSecondary,
            background = LightBlueBackground,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onSurface = Color.Black,
            onBackground = Color.Black,
            error = Color(0xFFD32F2F)
        ),
        typography = MaterialTheme.typography,
        content = content
    )
}