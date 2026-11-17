package org.umn.kulkaskis.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ---------- Pilihan tema (disimpan di SharedPreferences) ----------

enum class ThemeMode(val label: String) { System("System"), Light("Light"), Dark("Dark") }

class ThemeController(context: Context) {
    private val prefs = context.getSharedPreferences("kulkaskis_theme", Context.MODE_PRIVATE)

    var mode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("mode", null) ?: "System") }
            .getOrDefault(ThemeMode.System)
    )
        private set

    fun set(newMode: ThemeMode) {
        mode = newMode
        prefs.edit().putString("mode", newMode.name).apply()
    }
}

// ---------- Skema warna ----------

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2F5D3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EEDF),
    onPrimaryContainer = Color(0xFF1F3D27),
    secondary = Color(0xFF4F7C59),
    tertiary = Color(0xFFF4A261),
    onTertiary = Color(0xFF3A2200),
    background = Color(0xFFF6F7F3),
    onBackground = Color(0xFF1F2A22),
    surface = Color.White,
    onSurface = Color(0xFF1F2A22),
    surfaceVariant = Color(0xFFEEF1EA),
    onSurfaceVariant = Color(0xFF6B7A70),
    outline = Color(0xFFDDE3DA),
    outlineVariant = Color(0xFFDDE3DA),
    error = Color(0xFFB02A2A),
    errorContainer = Color(0xFFF8DADA),
    onErrorContainer = Color(0xFF7A1B1B)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8FC49A),            // hijau lebih terang supaya kontras di latar gelap
    onPrimary = Color(0xFF0F2417),
    primaryContainer = Color(0xFF2F5D3A),
    onPrimaryContainer = Color(0xFFE3EEDF),
    secondary = Color(0xFFA9D6B2),
    tertiary = Color(0xFFF4A261),
    onTertiary = Color(0xFF3A2200),
    background = Color(0xFF101713),
    onBackground = Color(0xFFE6EDE7),
    surface = Color(0xFF1A231D),
    onSurface = Color(0xFFE6EDE7),
    surfaceVariant = Color(0xFF243029),
    onSurfaceVariant = Color(0xFF9FB0A5),
    outline = Color(0xFF3A4A40),
    outlineVariant = Color(0xFF33423A),
    error = Color(0xFFF4A3A3),
    errorContainer = Color(0xFF4A2222),
    onErrorContainer = Color(0xFFF8DADA)
)

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

/** Akses cepat: `Kulkaskis.status.amberBg` di layar mana pun. */
object Kulkaskis {
    val status: StatusColors
        @Composable get() = LocalStatusColors.current
}

@Composable
fun KulkaskisTheme(
    mode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    // Ikon status bar & navigation bar gelap/terang mengikuti tema aplikasi, bukan tema HP
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalStatusColors provides if (dark) DarkStatus else LightStatus) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = Shapes(
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(14.dp),
                large = RoundedCornerShape(20.dp)
            ),
            content = content
        )
    }
}