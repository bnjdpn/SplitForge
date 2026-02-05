package com.splitforge.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    secondary = Amber40,
    onSecondary = Color.Black,
    background = Slate10,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    tertiary = Coral50
)

private val DarkColors = darkColorScheme(
    primary = Teal60,
    onPrimary = Color.Black,
    secondary = Amber60,
    onSecondary = Color.Black,
    background = Navy90,
    onBackground = Color.White,
    surface = Slate90,
    onSurface = Color.White,
    tertiary = Coral30
)

private val DisplayFont = FontFamily.Serif
private val BodyFont = FontFamily.Monospace

private val SplitForgeTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

@Composable
fun SplitForgeTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SplitForgeTypography,
        content = content
    )
}

@Composable
fun AppGradient(): Brush {
    val colors = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        listOf(Navy90, Slate90, Slate80)
    } else {
        listOf(Slate10, Slate20, Color.White)
    }
    return Brush.verticalGradient(colors)
}
