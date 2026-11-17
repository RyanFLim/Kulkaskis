package org.umn.kulkaskis.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Warna status (badge) yang ikut berganti saat dark mode. Jangan hardcode Color(0xFF...) di layar. */
@Immutable
data class StatusColors(
    val amberBg: Color, val amberFg: Color,   // "expiring soon"
    val redBg: Color, val redFg: Color,       // "expired"
    val greenBg: Color, val greenFg: Color,   // "fresh" / banner hijau
    val border: Color,                         // garis kartu
    val bannerOkBg: Color, val bannerBadBg: Color, val bannerText: Color, // banner Home
    val navIndicator: Color, val navIcon: Color // tab yang dipilih
)

val LightStatus = StatusColors(
    amberBg = Color(0xFFFBEBCB), amberFg = Color(0xFF9A6100),
    redBg = Color(0xFFF8DADA), redFg = Color(0xFFB02A2A),
    greenBg = Color(0xFFE3EEDF), greenFg = Color(0xFF2F5D3A),
    border = Color(0xFFDDE3DA),
    bannerOkBg = Color(0xFF2F5D3A), bannerBadBg = Color(0xFFB02A2A), bannerText = Color.White,
    navIndicator = Color(0xFF2F5D3A), navIcon = Color.White
)

val DarkStatus = StatusColors(
    amberBg = Color(0xFF3D3016), amberFg = Color(0xFFF2C46D),
    redBg = Color(0xFF4A2222), redFg = Color(0xFFF4A3A3),
    greenBg = Color(0xFF1F3326), greenFg = Color(0xFFA9D6B2),
    border = Color(0xFF33423A),
    bannerOkBg = Color(0xFF284A31), bannerBadBg = Color(0xFF6B2A2A), bannerText = Color(0xFFE3EEDF),
    navIndicator = Color(0xFF2F5D3A), navIcon = Color(0xFFE3EEDF)
)