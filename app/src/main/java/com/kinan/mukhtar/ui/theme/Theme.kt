package com.kinan.mukhtar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF0F5132)
private val GreenLight = Color(0xFF2E7D5B)
private val Gold = Color(0xFFB8860B)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    background = Color(0xFFF6F7F5),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color.White,
    secondary = Gold,
    background = Color(0xFF121412),
    surface = Color(0xFF1C1F1C)
)

/** خط النص كله بالاتجاه العربي */
private val ArabicTypography = Typography().let { base ->
    fun TextStyle.rtl() = copy(textDirection = TextDirection.Rtl)
    Typography(
        displayLarge = base.displayLarge.rtl(),
        displayMedium = base.displayMedium.rtl(),
        displaySmall = base.displaySmall.rtl(),
        headlineLarge = base.headlineLarge.rtl(),
        headlineMedium = base.headlineMedium.rtl(),
        headlineSmall = base.headlineSmall.rtl(),
        titleLarge = base.titleLarge.rtl().copy(fontSize = 20.sp),
        titleMedium = base.titleMedium.rtl(),
        titleSmall = base.titleSmall.rtl(),
        bodyLarge = base.bodyLarge.rtl(),
        bodyMedium = base.bodyMedium.rtl(),
        bodySmall = base.bodySmall.rtl(),
        labelLarge = base.labelLarge.rtl(),
        labelMedium = base.labelMedium.rtl(),
        labelSmall = base.labelSmall.rtl()
    )
}

@Composable
fun MukhtarTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ArabicTypography,
        content = content
    )
}
