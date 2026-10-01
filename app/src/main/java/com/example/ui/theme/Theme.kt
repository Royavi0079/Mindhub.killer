package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppThemeUtil {
    fun getAppColors(themeString: String): Triple<Color, Color, Color> {
        if (themeString.startsWith("CUSTOM_THEME:")) {
            try {
                val parts = themeString.split(":")
                val bgHex = parts[1].toLong()
                val secHex = parts[2].toLong()
                val bgColor = Color(bgHex)
                val secColor = Color(secHex)
                
                // Calculate text color dynamically based on background luminance
                val luminance = 0.2126f * bgColor.red + 0.7152f * bgColor.green + 0.0722f * bgColor.blue
                val textColor = if (luminance < 0.5f) Color.White else Color(0xFF0F172A)
                return Triple(bgColor, secColor, textColor)
            } catch (e: Exception) {
                // Fallback to default slate
            }
        }
        
        // Match existing presets
        val bg = when (themeString) {
            "COSMIC_NEBULA" -> Color(0xFF1E1B4B)
            "CYBERPUNK" -> Color(0xFF0F172A)
            "GOLDEN_AURORA" -> Color(0xFF451A03)
            "EMERALD_MATRIX" -> Color(0xFF064E3B)
            "OCEANIC_ABYSS" -> Color(0xFF082F49)
            "SOLAR_FLARE" -> Color(0xFF4C0519)
            "MINIMAL_SLATE" -> Color(0xFF1E293B)
            else -> Color(0xFFF8FAFC) // default SlateBackground
        }
        
        val sec = when (themeString) {
            "COSMIC_NEBULA" -> Color(0xFF312E81)
            "CYBERPUNK" -> Color(0xFF1E293B)
            "GOLDEN_AURORA" -> Color(0xFF78350F)
            "EMERALD_MATRIX" -> Color(0xFF065F46)
            "OCEANIC_ABYSS" -> Color(0xFF0C4A6E)
            "SOLAR_FLARE" -> Color(0xFF881337)
            "MINIMAL_SLATE" -> Color(0xFF334155)
            else -> Color(0xFFFFFFFF) // default WhiteCanvas
        }
        
        val luminance = 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
        val textColor = if (luminance < 0.5f) Color.White else Color(0xFF0F172A)
        
        return Triple(bg, sec, textColor)
    }
}

fun Color.isDark(): Boolean {
    val luminance = 0.2126f * this.red + 0.7152f * this.green + 0.0722f * this.blue
    return luminance < 0.5f
}


private val WhitePuzzleColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = WhiteCanvas,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = PrimaryIndigo,
    secondary = MathBlue,
    onSecondary = WhiteCanvas,
    secondaryContainer = MathBlueContainer,
    onSecondaryContainer = MathBlue,
    tertiary = HistoryAmber,
    onTertiary = WhiteCanvas,
    tertiaryContainer = HistoryAmberContainer,
    onTertiaryContainer = HistoryAmber,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateBackground,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    outlineVariant = SlateBorderStrong
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WhitePuzzleColorScheme,
        typography = Typography,
        content = content
    )
}
