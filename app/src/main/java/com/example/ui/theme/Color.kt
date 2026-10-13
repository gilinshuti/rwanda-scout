package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Accessibility-First Design System Colors
// Light Theme (all screens except pitch and report stats card)
val ScoutBackground = Color(0xFFF8FAFC)
val ScoutSurface = Color(0xFFFFFFFF)
val ScoutSurfaceVariant = Color(0xFFF1F5F9)
val ScoutDivider = Color(0xFFE2E8F0)

val ScoutTextPrimary = Color(0xFF0F172A)
val ScoutTextSecondary = Color(0xFF475569)
val ScoutTextHint = Color(0xFF94A3B8)

val ScoutPrimaryAction = Color(0xFF0284C7)
val ScoutPrimaryActionHover = Color(0xFF0369A1)
val ScoutSuccessLive = Color(0xFF16A34A)
val ScoutWarning = Color(0xFFD97706)
val ScoutErrorDanger = Color(0xFFDC2626)
val ScoutInfo = Color(0xFF6366F1)

// Dark Theme / Realistic Tactical Pitch
val ScoutPitchGrass = Color(0xFF1B7A35)
val ScoutPitchGrassStripe = Color(0xFF16682D) // Alternating mowed lawn stripe for realism
val ScoutPitchLines = Color(0xFFFFFFFF)
val ScoutTacticalCard = Color(0xFF0F172A)
val ScoutTacticalCardBorder = Color(0xFF334155)
val ScoutScoreboardPillBg = Color(0xCC0F172A) // Sleek tactical glass pill
val ScoutReportCardBg = Color(0xFF0F172A)
val ScoutReportAccents = Color(0xFFF59E0B)
val ScoutReportTextMuted = Color(0xFF94A3B8)

// Rwanda Flag Color Palette & Broadcast Graphics Tokens
val RwandaSkyBlue = Color(0xFF00A1DE)          // Rwandan Flag Sky Blue
val RwandaSkyBlueDark = Color(0xFF0077C8)      // Broadcast Gradient Deep Blue
val RwandaSunGold = Color(0xFFFAD201)          // Rwandan Flag Golden Yellow & 24-ray Sun
val RwandaSunGoldAccent = Color(0xFFE5B800)    // High-contrast gold accent
val RwandaForestGreen = Color(0xFF20603D)      // Rwandan Flag Forest Green
val RwandaForestGreenLight = Color(0xFF2E8B57) // Fresh turf / forest green highlight
val RwandaBroadcastNavy = Color(0xFF0A1324)     // Broadcast TV card deep background
val RwandaBroadcastSurface = Color(0xFF132238)  // Broadcast panel surface
val RwandaBroadcastBorder = Color(0xFF1E3A60)   // Broadcast card metallic border

// 12 Swatches for Team Kit Colors (Section E)
val ScoutTeamSwatches = listOf(
    Pair("Blue", Color(0xFF0B7EB8)),
    Pair("Green", Color(0xFF1B7F3A)),
    Pair("Red", Color(0xFFC53030)),
    Pair("Amber", Color(0xFFB7791F)),
    Pair("Navy", Color(0xFF0F1A2E)),
    Pair("Purple", Color(0xFF6B46C1)),
    Pair("Sky", Color(0xFF0284C7)),
    Pair("Emerald", Color(0xFF059669)),
    Pair("Orange", Color(0xFFEA580C)),
    Pair("Slate", Color(0xFF475569)),
    Pair("Maroon", Color(0xFF881337)),
    Pair("Black", Color(0xFF18181B))
)

fun parseTeamColor(colorString: String?, isHome: Boolean): Color {
    if (colorString != null) {
        val found = ScoutTeamSwatches.firstOrNull { it.first.equals(colorString, ignoreCase = true) }
        if (found != null) return found.second
        if (colorString.startsWith("#") && (colorString.length == 7 || colorString.length == 9)) {
            try {
                val clean = colorString.removePrefix("#")
                val parsedLong = clean.toLong(16)
                val fullColor = if (clean.length == 6) 0xFF000000L or parsedLong else parsedLong
                return Color(fullColor)
            } catch (_: Exception) { }
        }
    }
    return if (isHome) Color(0xFF0B7EB8) else Color(0xFF1B7F3A)
}
