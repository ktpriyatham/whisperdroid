package com.whisperdroid.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WhisperDroidExtraColors(
    val success: Color = Color.Unspecified,
    val recording: Color = Color.Unspecified
)

val LocalWhisperDroidExtraColors = staticCompositionLocalOf {
    WhisperDroidExtraColors()
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkExtraColors = WhisperDroidExtraColors(
    success = SuccessGreen,
    recording = RecordingRed
)

private val LightExtraColors = WhisperDroidExtraColors(
    success = SuccessGreen,
    recording = RecordingRed
)

object WhisperDroidTheme {
    val extraColors: WhisperDroidExtraColors
        @Composable
        get() = LocalWhisperDroidExtraColors.current
}

@Composable
fun WhisperDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalWhisperDroidExtraColors provides extraColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
