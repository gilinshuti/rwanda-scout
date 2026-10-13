package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.ScoutCard
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel
import java.util.Date

/**
 * 4 Pre-Match Tabs:
 * 1. Hand Icon   -> Section A: Match Readiness
 * 2. Sun Icon    -> Sections B, C, D: Weather, Wind & Pitch
 * 3. T-shirt Icon -> Section E: Team Kit Colors
 * 4. Arrow Icon   -> Kickoff & Team Pitch Sides
 */
enum class PreMatchTab(val title: String, val subtitle: String) {
    READINESS("Section A", "Readiness"),
    CONDITIONS("Sections B–D", "Conditions"),
    COLORS("Section E", "Kit Colors"),
    KICKOFF("Kickoff", "Pitch Sides")
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PreMatchScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit,
    onProceedToKickoff: (Long) -> Unit,
    onKickoffStarted: ((Long) -> Unit)? = null
) {
    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()

    val homeTeamName = match?.homeTeam ?: "Home Team"
    val awayTeamName = match?.awayTeam ?: "Away Team"

    // Section A Readiness States
    val onPitch = match?.prePlayersOnPitch ?: false
    val warmingUp = match?.prePlayersWarmingUp ?: false
    val aboutToStart = match?.preAboutToStart ?: false

    val readyCount = (if (onPitch) 1 else 0) + (if (warmingUp) 1 else 0) + (if (aboutToStart) 1 else 0)
    val allThreeReady = onPitch && warmingUp && aboutToStart

    val now = System.currentTimeMillis()
    val kickoffMillis = match?.kickoffTimeMillis ?: now

    // Section B Weather
    var weather by remember { mutableStateOf(match?.weather ?: "No rain") }
    var weatherIntensity by remember { mutableFloatStateOf((match?.weatherIntensity ?: 50).toFloat()) }
    var fog by remember { mutableStateOf(match?.fog ?: false) }

    // Section C Wind
    var wind by remember { mutableStateOf(match?.wind ?: "None") }
    var windSliderVal by remember {
        mutableFloatStateOf(
            when (match?.wind?.lowercase()) {
                "strong" -> 2f
                "light" -> 1f
                else -> 0f
            }
        )
    }

    // Section D Pitch
    var pitchCondition by remember { mutableStateOf(match?.pitchCondition ?: "Firm") }

    // Section E Team Colors
    var homeColor by remember { mutableStateOf(match?.homeColor ?: "Blue") }
    var awayColor by remember { mutableStateOf(match?.awayColor ?: "Green") }

    // Tab 4 Pitch Sides & Kickoff
    var leftTeamSide by remember { mutableStateOf(match?.leftTeamSide ?: "HOME") }
    var selectedKickoffSide by remember { mutableStateOf("HOME") }
    var showKickoffConfirmDialog by remember { mutableStateOf(false) }

    // Current active tab (default to READINESS)
    var selectedTab by remember { mutableStateOf(PreMatchTab.READINESS) }

    LaunchedEffect(match) {
        match?.let { m ->
            m.weather?.let { weather = it }
            m.weatherIntensity?.let { weatherIntensity = it.toFloat() }
            fog = m.fog
            m.wind?.let {
                wind = it
                windSliderVal = when (it.lowercase()) {
                    "strong" -> 2f
                    "light" -> 1f
                    else -> 0f
                }
            }
            m.pitchCondition?.let { pitchCondition = it }
            m.homeColor?.let { homeColor = it }
            m.awayColor?.let { awayColor = it }
            m.leftTeamSide?.let { leftTeamSide = it }
        }
    }

    val isHomeOnLeft = leftTeamSide.equals("HOME", ignoreCase = true)
    val leftTeamName = if (isHomeOnLeft) homeTeamName else awayTeamName
    val leftTeamColorParsed = parseTeamColor(if (isHomeOnLeft) homeColor else awayColor, isHome = isHomeOnLeft)
    val rightTeamName = if (isHomeOnLeft) awayTeamName else homeTeamName
    val rightTeamColorParsed = parseTeamColor(if (isHomeOnLeft) awayColor else homeColor, isHome = !isHomeOnLeft)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        ScoutTopBar(
            title = "Pre-Match Setup",
            onBackClick = onNavigateBack
        )

        // Compact Match Info Banner
        PreMatchHeaderBanner(
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            homeColor = parseTeamColor(homeColor, isHome = true),
            awayColor = parseTeamColor(awayColor, isHome = false),
            kickoffMillis = kickoffMillis,
            readyCount = readyCount
        )

        // Compact 4-Icon Tabs Row
        PreMatchTabsRow(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            readyCount = readyCount
        )

        // Tab Content Container (Sized to fit on screen without scrolling)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                PreMatchTab.READINESS -> {
                    ReadinessTabContent(
                        onPitch = onPitch,
                        warmingUp = warmingUp,
                        aboutToStart = aboutToStart,
                        kickoffMillis = kickoffMillis,
                        readyCount = readyCount,
                        onUpdateReadiness = { p, w, a ->
                            viewModel.updateMatchReadiness(onPitch = p, warmingUp = w, aboutToStart = a)
                        },
                        onGoToNextTab = { selectedTab = PreMatchTab.CONDITIONS },
                        onGoToKickoff = { selectedTab = PreMatchTab.KICKOFF }
                    )
                }
                PreMatchTab.CONDITIONS -> {
                    ConditionsTabContent(
                        weather = weather,
                        weatherIntensity = weatherIntensity,
                        fog = fog,
                        wind = wind,
                        windSliderVal = windSliderVal,
                        pitchCondition = pitchCondition,
                        onUpdateWeather = { newWeather ->
                            weather = newWeather
                            viewModel.updatePreMatchConditions(weather = newWeather)
                        },
                        onUpdateIntensity = { newIntensity ->
                            weatherIntensity = newIntensity
                            viewModel.updatePreMatchConditions(weatherIntensity = newIntensity.toInt())
                        },
                        onUpdateFog = { newFog ->
                            fog = newFog
                            viewModel.updatePreMatchConditions(fog = newFog)
                        },
                        onUpdateWind = { newWind, newSliderVal ->
                            wind = newWind
                            windSliderVal = newSliderVal
                            viewModel.updatePreMatchConditions(wind = newWind)
                        },
                        onUpdatePitch = { newPitch ->
                            pitchCondition = newPitch
                            viewModel.updatePreMatchConditions(pitchCondition = newPitch)
                        },
                        onGoToPrevTab = { selectedTab = PreMatchTab.READINESS },
                        onGoToNextTab = { selectedTab = PreMatchTab.COLORS }
                    )
                }
                PreMatchTab.COLORS -> {
                    TeamColorsTabContent(
                        homeTeamName = homeTeamName,
                        awayTeamName = awayTeamName,
                        homeColor = homeColor,
                        awayColor = awayColor,
                        onSelectHomeColor = { cName ->
                            homeColor = cName
                            viewModel.updatePreMatchConditions(homeColor = cName)
                        },
                        onSelectAwayColor = { cName ->
                            awayColor = cName
                            viewModel.updatePreMatchConditions(awayColor = cName)
                        },
                        onGoToPrevTab = { selectedTab = PreMatchTab.CONDITIONS },
                        onGoToNextTab = { selectedTab = PreMatchTab.KICKOFF }
                    )
                }
                PreMatchTab.KICKOFF -> {
                    KickoffAndSidesTabContent(
                        homeTeamName = homeTeamName,
                        awayTeamName = awayTeamName,
                        leftTeamName = leftTeamName,
                        rightTeamName = rightTeamName,
                        leftTeamColor = leftTeamColorParsed,
                        rightTeamColor = rightTeamColorParsed,
                        isHomeOnLeft = isHomeOnLeft,
                        selectedKickoffSide = selectedKickoffSide,
                        allThreeReady = allThreeReady,
                        onSwapSides = {
                            val newLeft = if (leftTeamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                            leftTeamSide = newLeft
                            viewModel.toggleSides()
                        },
                        onSelectKickoffSide = { side ->
                            selectedKickoffSide = side
                        },
                        onRequestKickoff = { side ->
                            selectedKickoffSide = side
                            showKickoffConfirmDialog = true
                        },
                        onGoToPrevTab = { selectedTab = PreMatchTab.COLORS }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Kickoff
    if (showKickoffConfirmDialog) {
        val kickingTeamName = if (selectedKickoffSide.equals("HOME", ignoreCase = true)) homeTeamName else awayTeamName
        val kickingTeamColor = parseTeamColor(
            if (selectedKickoffSide.equals("HOME", ignoreCase = true)) homeColor else awayColor,
            isHome = selectedKickoffSide.equals("HOME", ignoreCase = true)
        )

        AlertDialog(
            onDismissRequest = { showKickoffConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(14.dp).background(kickingTeamColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Match Kickoff",
                        style = ScoutTypographyTitleM.copy(fontSize = 18.sp),
                        color = ScoutTextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Initiate official first-half kickoff with $kickingTeamName taking the opening pass?",
                        style = ScoutTypographyBodyM.copy(fontSize = 14.sp),
                        color = ScoutTextPrimary
                    )
                    Text(
                        text = "Pitch: $leftTeamName (Left) vs $rightTeamName (Right).",
                        style = ScoutTypographyCaption.copy(fontSize = 13.sp),
                        color = ScoutTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showKickoffConfirmDialog = false
                        viewModel.startKickoff(kickoffTeam = selectedKickoffSide, leftTeamSide = leftTeamSide)
                        if (onKickoffStarted != null) {
                            onKickoffStarted(matchId)
                        } else {
                            onProceedToKickoff(matchId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONFIRM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showKickoffConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCEL", fontSize = 13.sp, color = ScoutTextSecondary)
                }
            },
            containerColor = ScoutSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 1. COMPACT MATCH HEADER BANNER
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun PreMatchHeaderBanner(
    homeTeamName: String,
    awayTeamName: String,
    homeColor: Color,
    awayColor: Color,
    kickoffMillis: Long,
    readyCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F1A2E),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(9.dp).background(homeColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = homeTeamName,
                    style = ScoutTypographyLabelL.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = " vs ",
                    style = ScoutTypographyCaption.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.6f)
                )
                Box(modifier = Modifier.size(9.dp).background(awayColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = awayTeamName,
                    style = ScoutTypographyLabelL.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                val kickoffTime = DateFormat.format("HH:mm", Date(kickoffMillis)).toString()
                Text(
                    text = "• $kickoffTime",
                    style = ScoutTypographyCaption.copy(fontSize = 11.sp),
                    color = Color(0xFF94A3B8)
                )
            }

            // Compact Readiness Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (readyCount == 3) ScoutSuccessLive else Color(0xFFB7791F)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (readyCount == 3) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (readyCount == 3) "READY" else "$readyCount/3",
                        style = ScoutTypographyCaption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. COMPACT 4-ICON TABS BAR
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun PreMatchTabsRow(
    selectedTab: PreMatchTab,
    onSelectTab: (PreMatchTab) -> Unit,
    readyCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = ScoutSurface,
        border = BorderStroke(1.dp, ScoutDivider),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PreMatchTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val testTag = when (tab) {
                    PreMatchTab.READINESS -> "tab_readiness"
                    PreMatchTab.CONDITIONS -> "tab_conditions"
                    PreMatchTab.COLORS -> "tab_colors"
                    PreMatchTab.KICKOFF -> "tab_kickoff"
                }

                Surface(
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag(testTag)
                        .semantics { contentDescription = "${tab.title} ${tab.subtitle} tab" },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF0F1A2E) else ScoutSurfaceVariant,
                    border = if (isSelected) BorderStroke(1.5.dp, ScoutPrimaryAction) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (tab) {
                                PreMatchTab.READINESS -> {
                                    Icon(
                                        imageVector = Icons.Default.PanTool,
                                        contentDescription = "Hand Icon - Section A",
                                        tint = if (isSelected) Color.White else ScoutTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                PreMatchTab.CONDITIONS -> {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Sun Icon - Sections B, C, D",
                                        tint = if (isSelected) Color(0xFFFBBF24) else Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                PreMatchTab.COLORS -> {
                                    ScoutTShirtIcon(
                                        modifier = Modifier.size(16.dp),
                                        color = if (isSelected) Color(0xFF38BDF8) else ScoutPrimaryAction,
                                        strokeColor = if (isSelected) Color.White else ScoutTextSecondary
                                    )
                                }
                                PreMatchTab.KICKOFF -> {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Arrow Icon - Kickoff & Pitch Side",
                                        tint = if (isSelected) Color.White else ScoutTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = tab.subtitle,
                            style = ScoutTypographyCaption.copy(
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else ScoutTextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. TAB 1: HAND ICON -> SECTION A (MATCH READINESS) - FITS ON SCREEN
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun ReadinessTabContent(
    onPitch: Boolean,
    warmingUp: Boolean,
    aboutToStart: Boolean,
    kickoffMillis: Long,
    readyCount: Int,
    onUpdateReadiness: (Boolean, Boolean, Boolean) -> Unit,
    onGoToNextTab: () -> Unit,
    onGoToKickoff: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Section Header Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = ScoutSurface,
            border = BorderStroke(1.dp, ScoutDivider)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ScoutPrimaryAction.copy(alpha = 0.12f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PanTool,
                                contentDescription = null,
                                tint = ScoutPrimaryAction,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Section A — Match Readiness",
                            style = ScoutTypographyTitleL.copy(fontSize = 15.sp),
                            color = ScoutTextPrimary
                        )
                        Text(
                            text = "Confirm team presence and field preparation",
                            style = ScoutTypographyCaption.copy(fontSize = 11.5.sp),
                            color = ScoutTextSecondary
                        )
                    }
                    Text(
                        text = "$readyCount/3 Done",
                        style = ScoutTypographyCaption.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (readyCount == 3) ScoutSuccessLive else ScoutPrimaryAction
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { readyCount / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (readyCount == 3) ScoutSuccessLive else ScoutPrimaryAction,
                    trackColor = ScoutDivider
                )
            }
        }

        // Compact Checklist Items Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = ScoutSurface,
            border = BorderStroke(1.dp, ScoutDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: Players on pitch
                ReadinessRowItemCompact(
                    checked = onPitch,
                    onCheckedChange = { isChecked ->
                        onUpdateReadiness(isChecked, warmingUp, aboutToStart)
                    },
                    title = "Players on pitch",
                    helperText = "Players lined up on field ready for review",
                    testTag = "readiness_on_pitch"
                )

                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ScoutDivider))

                // Row 2: Players warming up
                ReadinessRowItemCompact(
                    checked = warmingUp,
                    onCheckedChange = { isChecked ->
                        onUpdateReadiness(onPitch, isChecked, aboutToStart)
                    },
                    title = "Players warming up",
                    helperText = "Teams conducting warm-up drills on the pitch",
                    testTag = "readiness_warming_up"
                )

                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ScoutDivider))

                // Row 3: Match about to start
                val kickoffFormatted = DateFormat.format("HH:mm", Date(kickoffMillis)).toString()
                ReadinessRowItemCompact(
                    checked = aboutToStart,
                    onCheckedChange = { isChecked ->
                        onUpdateReadiness(onPitch, warmingUp, isChecked)
                    },
                    title = "Match about to start",
                    helperText = "Referees & captains ready (Scheduled: $kickoffFormatted)",
                    testTag = "readiness_about_to_start"
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Compact Action Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGoToNextTab,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Next: Conditions",
                        style = ScoutTypographyLabelL.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (readyCount == 3) {
                Button(
                    onClick = onGoToKickoff,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutSuccessLive)
                ) {
                    Text(
                        text = "Kickoff ⚡",
                        style = ScoutTypographyLabelL.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadinessRowItemCompact(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    helperText: String,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
            .testTag(testTag)
            .semantics { contentDescription = "$title. $helperText" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(22.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = ScoutSuccessLive,
                uncheckedColor = ScoutTextSecondary
            )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ScoutTypographyLabelL.copy(
                    fontSize = 14.sp,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                ),
                color = ScoutTextPrimary
            )
            Text(
                text = helperText,
                style = ScoutTypographyCaption.copy(fontSize = 11.5.sp),
                color = ScoutTextSecondary
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. TAB 2: SUN ICON -> SECTIONS B, C, D (WEATHER, WIND & PITCH) - FITS ON SCREEN
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun ConditionsTabContent(
    weather: String,
    weatherIntensity: Float,
    fog: Boolean,
    wind: String,
    windSliderVal: Float,
    pitchCondition: String,
    onUpdateWeather: (String) -> Unit,
    onUpdateIntensity: (Float) -> Unit,
    onUpdateFog: (Boolean) -> Unit,
    onUpdateWind: (String, Float) -> Unit,
    onUpdatePitch: (String) -> Unit,
    onGoToPrevTab: () -> Unit,
    onGoToNextTab: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section B — Weather
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Section B — Weather",
                    style = ScoutTypographyTitleM.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // 5 weather buttons compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val weatherOptions = listOf(
                    Triple("No rain", Icons.Default.WbSunny, "No rain"),
                    Triple("Rain", Icons.Default.Grain, "Rain"),
                    Triple("Snow", Icons.Default.AcUnit, "Snow"),
                    Triple("Sleet", Icons.Default.Cloud, "Sleet"),
                    Triple("Hail", Icons.Default.Thunderstorm, "Hail")
                )

                weatherOptions.forEach { (wName, icon, label) ->
                    val isSelected = weather.equals(wName, ignoreCase = true)
                    WeatherIconButtonCompact(
                        name = label,
                        icon = icon,
                        isSelected = isSelected,
                        onClick = { onUpdateWeather(wName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Compact Intensity slider
            if (weather.equals("Rain", ignoreCase = true) || weather.equals("Snow", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ScoutSurfaceVariant
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$weather Intensity",
                                style = ScoutTypographyLabelM.copy(fontSize = 12.sp, color = ScoutTextPrimary)
                            )
                            Text(
                                text = if (weatherIntensity < 50) "Light" else "Strong",
                                style = ScoutTypographyLabelM.copy(
                                    fontSize = 12.sp,
                                    color = ScoutPrimaryAction,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Slider(
                            value = weatherIntensity,
                            onValueChange = onUpdateIntensity,
                            valueRange = 0f..100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = ScoutPrimaryAction,
                                activeTrackColor = ScoutPrimaryAction,
                                inactiveTrackColor = ScoutDivider
                            )
                        )
                    }
                }
            }

            // Compact Fog Row
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                onClick = { onUpdateFog(!fog) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = ScoutSurface,
                border = BorderStroke(1.dp, ScoutDivider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = fog,
                        onCheckedChange = onUpdateFog,
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = ScoutPrimaryAction,
                            uncheckedColor = ScoutTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fog present on the pitch",
                        style = ScoutTypographyLabelL.copy(fontSize = 13.sp, color = ScoutTextPrimary)
                    )
                }
            }
        }

        // Section C — Wind
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Section C — Wind Velocity",
                    style = ScoutTypographyTitleM.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
                Text(
                    text = wind,
                    style = ScoutTypographyLabelM.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScoutPrimaryAction
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = ScoutSurface,
                border = BorderStroke(1.dp, ScoutDivider)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Slider(
                        value = windSliderVal,
                        onValueChange = { newVal ->
                            val wStr = when {
                                newVal >= 1.5f -> "Strong"
                                newVal >= 0.5f -> "Light"
                                else -> "None"
                            }
                            onUpdateWind(wStr, newVal)
                        },
                        valueRange = 0f..2f,
                        steps = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = ScoutPrimaryAction,
                            activeTrackColor = ScoutPrimaryAction,
                            inactiveTrackColor = ScoutDivider
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("None", style = ScoutTypographyCaption.copy(fontSize = 11.sp, color = ScoutTextSecondary))
                        Text("Light", style = ScoutTypographyCaption.copy(fontSize = 11.sp, color = ScoutTextSecondary))
                        Text("Strong", style = ScoutTypographyCaption.copy(fontSize = 11.sp, color = ScoutTextSecondary))
                    }
                }
            }
        }

        // Section D — Pitch Condition
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Section D — Pitch Condition",
                style = ScoutTypographyTitleM.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = ScoutTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isFirm = pitchCondition.equals("Firm", ignoreCase = true)
                val isMuddy = pitchCondition.equals("Muddy", ignoreCase = true)

                Surface(
                    onClick = { onUpdatePitch("Firm") },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("pitch_firm_btn"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFirm) ScoutPrimaryAction else ScoutSurface,
                    border = BorderStroke(1.dp, if (isFirm) ScoutPrimaryAction else ScoutDivider)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Firm Pitch",
                            style = ScoutTypographyLabelM.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isFirm) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFirm) Color.White else ScoutTextPrimary
                            )
                        )
                    }
                }

                Surface(
                    onClick = { onUpdatePitch("Muddy") },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("pitch_muddy_btn"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMuddy) ScoutPrimaryAction else ScoutSurface,
                    border = BorderStroke(1.dp, if (isMuddy) ScoutPrimaryAction else ScoutDivider)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Muddy Pitch",
                            style = ScoutTypographyLabelM.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isMuddy) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMuddy) Color.White else ScoutTextPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Compact Footer Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onGoToPrevTab,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← Readiness", fontSize = 12.5.sp, color = ScoutTextSecondary)
            }

            Button(
                onClick = onGoToNextTab,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Next: Kit Colors",
                        style = ScoutTypographyLabelL.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ScoutTShirtIcon(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherIconButtonCompact(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .border(
                border = if (isSelected) BorderStroke(2.dp, ScoutPrimaryAction) else BorderStroke(1.dp, ScoutDivider),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ScoutSurfaceVariant else ScoutSurface,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ScoutPrimaryAction else ScoutTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = name,
                style = ScoutTypographyBodyM.copy(
                    fontSize = 10.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) ScoutPrimaryAction else ScoutTextPrimary
                ),
                maxLines = 1
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. TAB 3: T-SHIRT ICON -> SECTION E (TEAM COLORS) - FITS ON SCREEN
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun TeamColorsTabContent(
    homeTeamName: String,
    awayTeamName: String,
    homeColor: String,
    awayColor: String,
    onSelectHomeColor: (String) -> Unit,
    onSelectAwayColor: (String) -> Unit,
    onGoToPrevTab: () -> Unit,
    onGoToNextTab: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Live Kit Preview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = ScoutSurface,
            border = BorderStroke(1.dp, ScoutDivider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Kit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScoutTShirtIcon(
                        modifier = Modifier.size(34.dp),
                        color = parseTeamColor(homeColor, isHome = true),
                        strokeColor = Color.Black.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HOME",
                            style = ScoutTypographyCaption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextSecondary
                        )
                        Text(
                            text = homeTeamName,
                            style = ScoutTypographyLabelM.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = homeColor,
                            style = ScoutTypographyCaption.copy(
                                fontSize = 11.5.sp,
                                color = parseTeamColor(homeColor, isHome = true),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // VS Pill
                Surface(
                    shape = CircleShape,
                    color = ScoutSurfaceVariant,
                    border = BorderStroke(1.dp, ScoutDivider)
                ) {
                    Text(
                        text = "VS",
                        style = ScoutTypographyCaption.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoutTextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Away Kit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScoutTShirtIcon(
                        modifier = Modifier.size(34.dp),
                        color = parseTeamColor(awayColor, isHome = false),
                        strokeColor = Color.Black.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AWAY",
                            style = ScoutTypographyCaption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextSecondary
                        )
                        Text(
                            text = awayTeamName,
                            style = ScoutTypographyLabelM.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = awayColor,
                            style = ScoutTypographyCaption.copy(
                                fontSize = 11.5.sp,
                                color = parseTeamColor(awayColor, isHome = false),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Home Color Swatches
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(parseTeamColor(homeColor, isHome = true), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HOME: $homeTeamName",
                    style = ScoutTypographyLabelL.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ColorSwatchesGridCompact(
                selectedColor = homeColor,
                onSelectColor = onSelectHomeColor
            )
        }

        // Away Color Swatches
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(parseTeamColor(awayColor, isHome = false), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AWAY: $awayTeamName",
                    style = ScoutTypographyLabelL.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ColorSwatchesGridCompact(
                selectedColor = awayColor,
                onSelectColor = onSelectAwayColor
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Compact Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onGoToPrevTab,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← Conditions", fontSize = 12.5.sp, color = ScoutTextSecondary)
            }

            Button(
                onClick = onGoToNextTab,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Next: Kickoff & Sides",
                        style = ScoutTypographyLabelL.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. TAB 4: ARROW ICON -> MATCH KICKOFF & PITCH SIDES - FITS ON SCREEN
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun KickoffAndSidesTabContent(
    homeTeamName: String,
    awayTeamName: String,
    leftTeamName: String,
    rightTeamName: String,
    leftTeamColor: Color,
    rightTeamColor: Color,
    isHomeOnLeft: Boolean,
    selectedKickoffSide: String,
    allThreeReady: Boolean,
    onSwapSides: () -> Unit,
    onSelectKickoffSide: (String) -> Unit,
    onRequestKickoff: (String) -> Unit,
    onGoToPrevTab: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Sides Header Pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = ScoutSurfaceVariant,
            border = BorderStroke(1.dp, ScoutDivider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(leftTeamColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "◀ $leftTeamName (Left)",
                        style = ScoutTypographyLabelM.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$rightTeamName (Right) ▶",
                        style = ScoutTypographyLabelM.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(rightTeamColor, CircleShape))
                }
            }
        }

        // Compact Mini Horizontal Pitch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.4f)
                .clip(RoundedCornerShape(12.dp))
                .background(ScoutPitchGrass)
                .padding(6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val strokeW = 1.5.dp.toPx()
                val lineColor = ScoutPitchLines

                // Pitch boundary
                drawRect(
                    color = lineColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h),
                    style = Stroke(width = strokeW)
                )
                // Halfway line
                drawLine(
                    color = lineColor,
                    start = Offset(w / 2f, 0f),
                    end = Offset(w / 2f, h),
                    strokeWidth = strokeW
                )
                // Center circle
                drawCircle(
                    color = lineColor,
                    radius = h * 0.24f,
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(width = strokeW)
                )
                // Center spot
                drawCircle(
                    color = lineColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(w / 2f, h / 2f)
                )
                // Left penalty box
                val boxW = w * 0.16f
                val boxH = h * 0.6f
                drawRect(
                    color = lineColor,
                    topLeft = Offset(0f, (h - boxH) / 2f),
                    size = Size(boxW, boxH),
                    style = Stroke(width = strokeW)
                )
                // Right penalty box
                drawRect(
                    color = lineColor,
                    topLeft = Offset(w - boxW, (h - boxH) / 2f),
                    size = Size(boxW, boxH),
                    style = Stroke(width = strokeW)
                )
            }

            // Team Badges Overlay
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(leftTeamColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = leftTeamName,
                            style = ScoutTypographyCaption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(rightTeamColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rightTeamName,
                            style = ScoutTypographyCaption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Change Pitch Sides Button (Compact)
        OutlinedButton(
            onClick = onSwapSides,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("change_sides_btn"),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, ScoutPrimaryAction),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScoutPrimaryAction)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = ScoutPrimaryAction,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Change Pitch Sides",
                    style = ScoutTypographyLabelL.copy(
                        color = ScoutPrimaryAction,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Compact Kickoff Selection
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Tap arrow to select team to kick off:",
                style = ScoutTypographyLabelM.copy(fontSize = 12.sp, color = ScoutTextSecondary)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val leftSideTeamName = if (isHomeOnLeft) homeTeamName else awayTeamName
            val leftSideTeamCode = if (isHomeOnLeft) "HOME" else "AWAY"
            val rightSideTeamName = if (isHomeOnLeft) awayTeamName else homeTeamName
            val rightSideTeamCode = if (isHomeOnLeft) "AWAY" else "HOME"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow Box (Blue #0B7EB8, compact 60dp)
                val isLeftSelected = selectedKickoffSide == leftSideTeamCode
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B7EB8))
                        .then(
                            if (isLeftSelected) {
                                Modifier.border(2.5.dp, Color.White, RoundedCornerShape(12.dp))
                            } else Modifier
                        )
                        .clickable { onSelectKickoffSide(leftSideTeamCode) }
                        .testTag("kickoff_arrow_left")
                        .semantics { contentDescription = "Select $leftSideTeamName to kick off" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = leftSideTeamName.take(10),
                                style = ScoutTypographyCaption.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                            if (isLeftSelected) {
                                Text(
                                    text = "● KICKOFF",
                                    style = ScoutTypographyCaption.copy(
                                        color = Color.Yellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Right Arrow Box (Red #C53030, compact 60dp)
                val isRightSelected = selectedKickoffSide == rightSideTeamCode
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFC53030))
                        .then(
                            if (isRightSelected) {
                                Modifier.border(2.5.dp, Color.White, RoundedCornerShape(12.dp))
                            } else Modifier
                        )
                        .clickable { onSelectKickoffSide(rightSideTeamCode) }
                        .testTag("kickoff_arrow_right")
                        .semantics { contentDescription = "Select $rightSideTeamName to kick off" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = rightSideTeamName.take(10),
                                style = ScoutTypographyCaption.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                            if (isRightSelected) {
                                Text(
                                    text = "● KICKOFF",
                                    style = ScoutTypographyCaption.copy(
                                        color = Color.Yellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Start Kickoff Button (Compact)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (allThreeReady) {
                        onRequestKickoff(selectedKickoffSide)
                    }
                },
                enabled = allThreeReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("proceed_to_kickoff_btn")
                    .semantics { contentDescription = "Proceed to Kickoff" },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScoutPrimaryAction,
                    contentColor = Color.White,
                    disabledContainerColor = ScoutDivider,
                    disabledContentColor = ScoutTextHint
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = if (allThreeReady) Color.White else ScoutTextHint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proceed to Kickoff",
                        style = ScoutTypographyLabelL.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allThreeReady) Color.White else ScoutTextHint
                        )
                    )
                }
            }

            if (!allThreeReady) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete all 3 checklist items in Section A to proceed",
                    style = ScoutTypographyCaption.copy(fontSize = 11.sp),
                    color = ScoutTextHint
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. COMPACT CUSTOM ICONS & COLOR SWATCHES
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Custom Canvas Football Jersey (T-Shirt) Icon.
 */
@Composable
fun ScoutTShirtIcon(
    modifier: Modifier = Modifier,
    color: Color = ScoutPrimaryAction,
    strokeColor: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.36f, h * 0.12f)
            quadraticTo(w * 0.50f, h * 0.24f, w * 0.64f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.96f, h * 0.40f)
            lineTo(w * 0.82f, h * 0.50f)
            lineTo(w * 0.74f, h * 0.38f)
            lineTo(w * 0.74f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.38f)
            lineTo(w * 0.18f, h * 0.50f)
            lineTo(w * 0.04f, h * 0.40f)
            lineTo(w * 0.18f, h * 0.18f)
            close()
        }

        drawPath(path = path, color = color)
        drawPath(path = path, color = strokeColor, style = Stroke(width = 1.2.dp.toPx()))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatchesGridCompact(
    selectedColor: String,
    onSelectColor: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ScoutTeamSwatches.forEach { (colorName, colorVal) ->
            val isSelected = selectedColor.equals(colorName, ignoreCase = true)

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colorVal, CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.5.dp, ScoutPrimaryAction, CircleShape)
                        } else {
                            Modifier.border(1.dp, ScoutDivider, CircleShape)
                        }
                    )
                    .clickable { onSelectColor(colorName) }
                    .semantics { contentDescription = "Color $colorName ${if (isSelected) "selected" else ""}" },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
