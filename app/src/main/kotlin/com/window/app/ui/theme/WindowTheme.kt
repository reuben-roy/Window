package com.window.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand palette ──────────────────────────────────────────────────────────
val WindowBlue        = Color(0xFF1A73E8)
val WindowBlueDark    = Color(0xFF0D47A1)
val WindowSurface     = Color(0xFFF8F9FA)
val WindowOnSurface   = Color(0xFF202124)
val WindowAccent      = Color(0xFF34A853)

private val LightColorScheme = lightColorScheme(
    primary          = WindowBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    secondary        = WindowAccent,
    onSecondary      = Color.White,
    background       = WindowSurface,
    surface          = Color.White,
    onSurface        = WindowOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF8AB4F8),
    onPrimary        = Color(0xFF003063),
    primaryContainer = WindowBlueDark,
    secondary        = Color(0xFF81C995),
    onSecondary      = Color(0xFF003920),
    background       = Color(0xFF202124),
    surface          = Color(0xFF303134),
    onSurface        = Color(0xFFE8EAED)
)

@Composable
fun WindowTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography(),
        content     = content
    )
}

