package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BallTapEntity
import com.example.data.model.LineupEntity
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.TacticalJerseyBadge
import com.example.ui.theme.*
import com.example.ui.theme.ScoutTypographyBodyL
import com.example.ui.theme.ScoutTypographyBodyM
import com.example.ui.theme.ScoutTypographyDisplay
import com.example.ui.theme.ScoutTypographyLabelL
import com.example.ui.theme.ScoutTypographyLabelM
import com.example.ui.theme.ScoutTypographyTitleL
import com.example.ui.theme.ScoutTypographyTitleM
import com.example.ui.theme.ScoutTypographyTitleXL
import com.example.ui.theme.ScoutWarning
import com.example.ui.viewmodel.ScoutViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class ActiveWorkflow {
    NONE,
    CARDS,
    CORNER,
    PENALTY,
    FREE_KICK,
    VAR
}

enum class GoalWorkflowState {
    TEAM_SELECTION,
    GOAL_DETAILS,
    CANCEL_REASONS,
    ADVANCED_PLAYER_DATA,
    AFTER_GOAL_KICKOFF
}

@Composable
fun LiveMatchScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReport: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 1. LANDSCAPE LOCK
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()
    val lineups by viewModel.activeLineups.collectAsState()
    val events by viewModel.activeEvents.collectAsState()
    val ballTaps by viewModel.activeBallTaps.collectAsState()
    val currentPossession by viewModel.currentPossession.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val advancedPlayerDataEnabled by viewModel.advancedPlayerDataEnabled.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler {
        onNavigateBack()
    }

    // 2. PORTRAIT OVERLAY
    if (!isLandscape) {
        PortraitRotateOverlay(onBack = onNavigateBack)
        return
    }

    LaunchedEffect(isLandscape, match?.status) {
        if (isLandscape && match?.status == "LIVE" && !isTimerRunning) {
            viewModel.startTimer()
        }
    }

    var showPossessionBanner by remember { mutableStateOf(false) }
    var bannerPossessionTeam by remember { mutableStateOf("HOME") }
    LaunchedEffect(currentPossession) {
        bannerPossessionTeam = currentPossession
        showPossessionBanner = true
        delay(1800L)
        showPossessionBanner = false
    }

    // UI Dialog & Workflow States
    var isEventsDrawerOpen by remember { mutableStateOf(false) }
    var activeWorkflow by remember { mutableStateOf(ActiveWorkflow.NONE) }
    var showInPlayActions by remember { mutableStateOf(false) }
    var showGeneralEventsMenu by remember { mutableStateOf(false) }
    var activeInterruptionReason by remember { mutableStateOf<String?>(null) }
    var goalWorkflowState by remember { mutableStateOf<GoalWorkflowState?>(null) }
    var selectedGoalTeam by remember { mutableStateOf("HOME") }

    // Goal Details form state
    var selectedHowScored by remember { mutableStateOf("Shot on goal") }
    var isDoubtfulMinute by remember { mutableStateOf(false) }
    var isDoubtfulScorer by remember { mutableStateOf(false) }
    var isInsideBox by remember { mutableStateOf(false) }
    var isDoubtfulGoal by remember { mutableStateOf(false) }

    var showPeriodDialog by remember { mutableStateOf(false) }
    var showEndMatchConfirm by remember { mutableStateOf(false) }
    var showMoreEventsDialog by remember { mutableStateOf(false) }

    // Corner indicator state
    var cornerArrowCorner by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    LaunchedEffect(cornerArrowCorner) {
        if (cornerArrowCorner != null) {
            delay(3000L)
            cornerArrowCorner = null
        }
    }

    val homeTeamName = match?.homeTeam ?: "HOME"
    val awayTeamName = match?.awayTeam ?: "AWAY"
    val homeScore = match?.homeScore ?: 0
    val awayScore = match?.awayScore ?: 0
    val currentPeriod = match?.currentPeriod ?: "FIRST_HALF"

    val homeYellows = events.count { it.team.equals("HOME", ignoreCase = true) && (it.eventType == "YELLOW_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Yellow") == true)) }
    val homeReds = events.count { it.team.equals("HOME", ignoreCase = true) && (it.eventType == "RED_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Red") == true)) }
    val awayYellows = events.count { it.team.equals("AWAY", ignoreCase = true) && (it.eventType == "YELLOW_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Yellow") == true)) }
    val awayReds = events.count { it.team.equals("AWAY", ignoreCase = true) && (it.eventType == "RED_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Red") == true)) }

    val homeTapsCount = ballTaps.count { it.side.equals("HOME", ignoreCase = true) }
    val awayTapsCount = ballTaps.count { it.side.equals("AWAY", ignoreCase = true) }
    val totalTaps = homeTapsCount + awayTapsCount
    val homePct = if (totalTaps > 0) ((homeTapsCount * 100f) / totalTaps).toInt().coerceIn(10, 90) else 50
    val awayPct = 100 - homePct

    val isHomeOnLeft = (match?.leftTeamSide ?: "HOME").equals("HOME", ignoreCase = true)

    val latestTap = ballTaps.lastOrNull()
    // Dangerous attack ONLY on the opposite team's dangerous zone (final third), NOT on its own part of the pitch!
    val isDangerousZone = latestTap != null && (
        if (currentPossession.equals("HOME", ignoreCase = true)) {
            if (isHomeOnLeft) latestTap.x >= 0.75f else latestTap.x <= 0.25f
        } else {
            if (isHomeOnLeft) latestTap.x <= 0.25f else latestTap.x >= 0.75f
        }
    )
    val isDangerousAttack = isDangerousZone

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("live_match_landscape_screen")
    ) {
        val leftTeamName = if (isHomeOnLeft) homeTeamName else awayTeamName
        val rightTeamName = if (isHomeOnLeft) awayTeamName else homeTeamName
        val leftScore = if (isHomeOnLeft) homeScore else awayScore
        val rightScore = if (isHomeOnLeft) awayScore else homeScore

        // 1. CLEAN PITCH CANVAS & GESTURE LAYER
        LivePitchCanvas(
            ballTaps = ballTaps,
            isDangerousAttack = isDangerousAttack,
            currentPossession = currentPossession,
            isHomeOnLeft = isHomeOnLeft,
            cornerArrowPos = cornerArrowCorner,
            onPitchTap = { normX, normY ->
                val dangerous = if (currentPossession.equals("HOME", ignoreCase = true)) {
                    if (isHomeOnLeft) normX >= 0.75f else normX <= 0.25f
                } else {
                    if (isHomeOnLeft) normX <= 0.25f else normX >= 0.75f
                }
                viewModel.logBallTap(
                    x = normX,
                    y = normY,
                    side = currentPossession,
                    isDangerous = dangerous
                )
            },
            onPitchDoubleTap = {
                showInPlayActions = true
            },
            onSwipeLeft = {
                if (currentPossession != "AWAY") {
                    viewModel.setPossession("AWAY")
                    Toast.makeText(context, "Possession: AWAY ($awayTeamName)", Toast.LENGTH_SHORT).show()
                }
            },
            onSwipeRight = {
                if (currentPossession != "HOME") {
                    viewModel.setPossession("HOME")
                    Toast.makeText(context, "Possession: HOME ($homeTeamName)", Toast.LENGTH_SHORT).show()
                }
            },
            onSwipeUpFromBottom = {
                isEventsDrawerOpen = true
            }
        )

        val leftTeamColor = parseTeamColor(if (isHomeOnLeft) match?.homeColor else match?.awayColor, isHome = isHomeOnLeft)
        val rightTeamColor = parseTeamColor(if (isHomeOnLeft) match?.awayColor else match?.homeColor, isHome = !isHomeOnLeft)

        // 2. TOP TEAM NAME BARS (Overlay inside pitch, divided by halfway line with kit colors and badges)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(leftTeamColor.copy(alpha = 0.92f), leftTeamColor.copy(alpha = 0.70f))
                        )
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TacticalJerseyBadge(color = leftTeamColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = leftTeamName.uppercase(),
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // Halfway divider line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(rightTeamColor.copy(alpha = 0.70f), rightTeamColor.copy(alpha = 0.92f))
                        )
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = rightTeamName.uppercase(),
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    TacticalJerseyBadge(color = rightTeamColor, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 3. BIG SCORE NUMBERS IN MIDDLE OF EACH HALF (Exact look from Sportradar/Opta IMG_8639)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp, bottom = 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$leftScore",
                    style = TextStyle(
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.88f),
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), blurRadius = 6f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rightScore",
                    style = TextStyle(
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.88f),
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), blurRadius = 6f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 4. CENTER BOTTOM PERIOD & TIMER (Clean layout below center circle, NO ICONS)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val periodDisplay = when (currentPeriod) {
                "FIRST_HALF" -> "1ST HALF"
                "SECOND_HALF" -> "2ND HALF"
                "HALF_TIME" -> "HALF TIME"
                "EXTRA_TIME_1" -> "1ST ET"
                "EXTRA_TIME_2" -> "2ND ET"
                "PENALTIES" -> "PENALTIES"
                "FULL_TIME" -> "FULL TIME"
                else -> currentPeriod.replace("_", " ")
            }
            Text(
                text = periodDisplay,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.7f), blurRadius = 4f)
                ),
                modifier = Modifier.testTag("center_period_label")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatTimer(timerSeconds),
                style = TextStyle(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F8E9),
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.8f), blurRadius = 6f)
                ),
                modifier = Modifier.testTag("center_pitch_timer")
            )
        }

        // ═════════════════════════════════════════════════════════════════════
        // EVENTS DRAWER / FIRST LEVEL SCREEN (Swiped from bottom to top on pitch)
        // ═════════════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = isEventsDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(250)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { isEventsDrawerOpen = false }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {}
                        .testTag("events_drawer_container"),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color.Black,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, ScoutSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Top drag handle pill
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                                .align(Alignment.CenterHorizontally)
                        )

                        // Top header bar with timer, report/end match, exit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        isEventsDrawerOpen = false
                                        showPeriodDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF212121),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = "${formatTimer(timerSeconds)} • ${currentPeriod.replace("_", " ")}",
                                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        isEventsDrawerOpen = false
                                        if (match?.status == "FINISHED") {
                                            onNavigateToReport(matchId)
                                        } else {
                                            showEndMatchConfirm = true
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (match?.status == "FINISHED") ScoutSuccessLive else Color(0xFF333333)
                                ) {
                                    Text(
                                        text = if (match?.status == "FINISHED") "Report" else "End Match",
                                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    onClick = {
                                        isEventsDrawerOpen = false
                                        onNavigateBack()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF333333)
                                ) {
                                    Text(
                                        text = "Exit",
                                        style = TextStyle(fontSize = 13.sp, color = Color.White),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // FIRST LEVEL SCREEN (MATCH EVENTS) — Exact 2-Sides Red & Blue Layout (IMG_8640)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp)
                        ) {
                            MatchEventsFirstLevelScreen(
                                homeTeamName = homeTeamName,
                                awayTeamName = awayTeamName,
                                onCornerKick = { team, isTop ->
                                    isEventsDrawerOpen = false
                                    viewModel.logCornerEvent(
                                        teamSide = team,
                                        takerName = null,
                                        ballX = latestTap?.x,
                                        ballY = latestTap?.y
                                    )
                                    val cornerX = if (team == "HOME") 1.0f else 0.0f
                                    val cornerY = if (isTop) 0.0f else 1.0f
                                    cornerArrowCorner = Pair(cornerX, cornerY)
                                    Toast.makeText(context, "$team Corner Kick awarded", Toast.LENGTH_SHORT).show()
                                },
                                onCards = { team ->
                                    isEventsDrawerOpen = false
                                    selectedGoalTeam = team
                                    activeWorkflow = ActiveWorkflow.CARDS
                                },
                                onGoal = { team ->
                                    isEventsDrawerOpen = false
                                    selectedGoalTeam = team
                                    isInsideBox = if (selectedGoalTeam == "HOME") {
                                        latestTap != null && (latestTap.x >= 0.85f || (latestTap.x * 100f) >= 85f)
                                    } else {
                                        latestTap != null && (latestTap.x <= 0.15f || (latestTap.x * 100f) <= 15f)
                                    }
                                    selectedHowScored = "Shot on goal"
                                    goalWorkflowState = GoalWorkflowState.TEAM_SELECTION
                                },
                                onPenaltyKick = { team ->
                                    isEventsDrawerOpen = false
                                    selectedGoalTeam = team
                                    activeWorkflow = ActiveWorkflow.PENALTY
                                },
                                onDangerousFreeKick = { team ->
                                    isEventsDrawerOpen = false
                                    selectedGoalTeam = team
                                    // A dangerous free kick is made from a dangerous attack!
                                    viewModel.logFreeKickEvent(
                                        teamSide = team,
                                        cause = "Dangerous Attack Foul",
                                        isDangerous = true,
                                        booking = null,
                                        ballX = latestTap?.x,
                                        ballY = latestTap?.y
                                    )
                                    Toast.makeText(context, "Dangerous Free Kick awarded to $team (from dangerous attack)", Toast.LENGTH_SHORT).show()
                                    activeWorkflow = ActiveWorkflow.FREE_KICK
                                },
                                onVideoReferee = {
                                    isEventsDrawerOpen = false
                                    activeWorkflow = ActiveWorkflow.VAR
                                },
                                onGeneralEvents = {
                                    isEventsDrawerOpen = false
                                    showGeneralEventsMenu = true
                                },
                                onClose = {
                                    isEventsDrawerOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // WORKFLOW MODALS
        // ═════════════════════════════════════════════════════════════════════

        // CARDS WORKFLOW
        if (activeWorkflow == ActiveWorkflow.CARDS) {
            CardsWorkflowScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                initialTeam = selectedGoalTeam,
                lineups = lineups,
                yellowCardCountProvider = { playerName -> viewModel.getPlayerYellowCardCount(playerName) },
                onConfirmCard = { team, player, cardType, alsoFreeKick ->
                    viewModel.logCardEvent(
                        teamSide = team,
                        playerName = player,
                        cardType = cardType,
                        alsoAwardFreeKick = alsoFreeKick,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y
                    )
                    Toast.makeText(context, "$cardType Card logged for $player", Toast.LENGTH_SHORT).show()
                    activeWorkflow = ActiveWorkflow.NONE
                },
                onVideoReferee = { activeWorkflow = ActiveWorkflow.VAR },
                onDismiss = { activeWorkflow = ActiveWorkflow.NONE }
            )
        }

        // CORNER WORKFLOW
        if (activeWorkflow == ActiveWorkflow.CORNER) {
            CornerWorkflowScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                initialTeam = currentPossession,
                lineups = lineups,
                isAdvancedMode = advancedPlayerDataEnabled,
                onConfirmCorner = { team, taker, secondaryAction ->
                    viewModel.logCornerEvent(
                        teamSide = team,
                        takerName = taker,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y
                    )
                    // Trigger corner arrow at correct corner
                    val cornerX = if (team == "HOME") 1.0f else 0.0f
                    val cornerY = if ((latestTap?.y ?: 0.5f) < 0.5f) 0.0f else 1.0f
                    cornerArrowCorner = Pair(cornerX, cornerY)

                    if (!secondaryAction.isNullOrBlank()) {
                        viewModel.logEvent(
                            eventType = secondaryAction.uppercase(),
                            teamSide = team,
                            playerName = "$secondaryAction during Corner",
                            ballX = latestTap?.x,
                            ballY = latestTap?.y
                        )
                    }

                    Toast.makeText(context, "Corner awarded to $team", Toast.LENGTH_SHORT).show()
                    activeWorkflow = ActiveWorkflow.NONE
                },
                onVideoReferee = { activeWorkflow = ActiveWorkflow.VAR },
                onDismiss = { activeWorkflow = ActiveWorkflow.NONE }
            )
        }

        // PENALTY WORKFLOW
        if (activeWorkflow == ActiveWorkflow.PENALTY) {
            PenaltyWorkflowScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                initialTeam = selectedGoalTeam,
                onConfirmPenalty = { team, cause, booking, combo ->
                    viewModel.logPenaltyEvent(
                        teamSide = team,
                        cause = cause,
                        booking = booking,
                        playerInvolved = null,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y
                    )
                    if (!combo.isNullOrBlank()) {
                        viewModel.logEvent(
                            eventType = combo.uppercase(),
                            teamSide = team,
                            playerName = "$combo during Penalty call",
                            ballX = latestTap?.x,
                            ballY = latestTap?.y
                        )
                    }
                    Toast.makeText(context, "Penalty awarded to $team", Toast.LENGTH_SHORT).show()
                },
                onResolvePenalty = { team, outcome ->
                    viewModel.logPenaltyResolution(team, outcome, latestTap?.x, latestTap?.y)
                    activeWorkflow = ActiveWorkflow.NONE
                    if (outcome.equals("Goal", ignoreCase = true)) {
                        // Directly award goal from penalty without showing "How was goal scored" screen
                        val defaultScorer = if (team.equals("HOME", ignoreCase = true)) "$homeTeamName Goal (Penalty)" else "$awayTeamName Goal (Penalty)"
                        viewModel.confirmGoalWorkflow(
                            teamSide = team,
                            howScored = "Penalty kick",
                            isDoubtfulMinute = false,
                            isDoubtfulScorer = false,
                            isInsideBox = true,
                            isDoubtfulGoal = false,
                            scorerName = defaultScorer,
                            assistName = null,
                            ballX = latestTap?.x,
                            ballY = latestTap?.y,
                            onSuccess = { timeDisplay, newHomeScore, newAwayScore ->
                                val concedingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                                val concedingTeamName = if (concedingTeam == "HOME") homeTeamName else awayTeamName
                                Toast.makeText(context, "PENALTY GOAL at $timeDisplay! Kickoff possession to $concedingTeamName", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        // Penalty not scored (Miss, GK Save, Off Target, Blocked)
                        // Pitch possession returns to defending team
                        val defendingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                        val defendingTeamName = if (defendingTeam == "HOME") homeTeamName else awayTeamName
                        viewModel.setPossession(defendingTeam)
                        val restartX = if (defendingTeam == "HOME") 0.08f else 0.92f
                        viewModel.logBallTap(x = restartX, y = 0.5f, side = defendingTeam, isDangerous = false)
                        Toast.makeText(context, "Penalty: $outcome — Possession to $defendingTeamName", Toast.LENGTH_LONG).show()
                    }
                },
                onVideoReferee = { activeWorkflow = ActiveWorkflow.VAR },
                onDismiss = { activeWorkflow = ActiveWorkflow.NONE }
            )
        }

        // DANGEROUS / REGULAR FREE KICK WORKFLOW
        if (activeWorkflow == ActiveWorkflow.FREE_KICK) {
            FreeKickWorkflowScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                initialTeam = currentPossession,
                isDangerousZone = isDangerousZone,
                onConfirmFreeKick = { team, cause, isDangerous, booking ->
                    viewModel.logFreeKickEvent(
                        teamSide = team,
                        cause = cause,
                        isDangerous = isDangerous,
                        booking = booking,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y
                    )
                    Toast.makeText(context, if (isDangerous) "Dangerous Free Kick awarded" else "Free Kick awarded", Toast.LENGTH_SHORT).show()
                },
                onResolveDfk = { team, outcome, isGkSave ->
                    viewModel.logDfkResolution(team, outcome, isGkSave, latestTap?.x, latestTap?.y)
                    activeWorkflow = ActiveWorkflow.NONE
                    when (outcome) {
                        "Goal" -> {
                            // Directly award goal from free kick without showing "How was goal scored" screen
                            val defaultScorer = if (team.equals("HOME", ignoreCase = true)) "$homeTeamName Goal (Free kick)" else "$awayTeamName Goal (Free kick)"
                            viewModel.confirmGoalWorkflow(
                                teamSide = team,
                                howScored = "Free kick",
                                isDoubtfulMinute = false,
                                isDoubtfulScorer = false,
                                isInsideBox = false,
                                isDoubtfulGoal = false,
                                scorerName = defaultScorer,
                                assistName = null,
                                ballX = latestTap?.x,
                                ballY = latestTap?.y,
                                onSuccess = { timeDisplay, newHomeScore, newAwayScore ->
                                    val concedingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                                    val concedingTeamName = if (concedingTeam == "HOME") homeTeamName else awayTeamName
                                    Toast.makeText(context, "FREE KICK GOAL at $timeDisplay! Kickoff possession to $concedingTeamName", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                        "Corner" -> {
                            // Award corner kick to the attacking team
                            viewModel.logCornerEvent(
                                teamSide = team,
                                takerName = null,
                                ballX = latestTap?.x,
                                ballY = latestTap?.y
                            )
                            val cornerX = if (team == "HOME") 1.0f else 0.0f
                            val cornerY = if ((latestTap?.y ?: 0.5f) < 0.5f) 0.0f else 1.0f
                            cornerArrowCorner = Pair(cornerX, cornerY)
                            viewModel.setPossession(team)
                            viewModel.logBallTap(x = cornerX, y = cornerY, side = team, isDangerous = true)
                            val attackingTeamName = if (team == "HOME") homeTeamName else awayTeamName
                            Toast.makeText(context, "Corner Kick awarded to $attackingTeamName", Toast.LENGTH_SHORT).show()
                        }
                        "Penalty" -> {
                            // Transitions to Penalty workflow
                            selectedGoalTeam = team
                            activeWorkflow = ActiveWorkflow.PENALTY
                        }
                        "Another card" -> {
                            // Open card selection for the defending team
                            val defendingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                            selectedGoalTeam = defendingTeam
                            activeWorkflow = ActiveWorkflow.CARDS
                        }
                        else -> {
                            // "No action" or any other non-scoring outcome (GK Save, Blocked, Cleared, Off target)
                            // Pitch possession returns to defending team
                            val defendingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                            val defendingTeamName = if (defendingTeam == "HOME") homeTeamName else awayTeamName
                            viewModel.setPossession(defendingTeam)
                            val restartX = if (defendingTeam == "HOME") 0.08f else 0.92f
                            viewModel.logBallTap(x = restartX, y = 0.5f, side = defendingTeam, isDangerous = false)
                            val label = if (isGkSave) "DFK: Saved by GK" else "DFK: $outcome"
                            Toast.makeText(context, "$label — Possession to $defendingTeamName", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onVideoReferee = { activeWorkflow = ActiveWorkflow.VAR },
                onDismiss = { activeWorkflow = ActiveWorkflow.NONE }
            )
        }

        // VAR WORKFLOW
        if (activeWorkflow == ActiveWorkflow.VAR) {
            VarWorkflowScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                initialTeam = currentPossession,
                onConfirmOutcome = { team, reason, outcome ->
                    viewModel.applyVarReviewOutcome(
                        teamSide = team,
                        reviewReason = reason,
                        outcome = outcome,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y,
                        onOutcomeApplied = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                    activeWorkflow = ActiveWorkflow.NONE
                },
                onDismiss = { activeWorkflow = ActiveWorkflow.NONE }
            )
        }

        // IN-PLAY ACTIONS OVERLAY (Double-tap pitch)
        if (showInPlayActions) {
            InPlayActionsScreen(
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                latestTapX = latestTap?.x,
                currentPossession = currentPossession,
                onThrowIn = { team ->
                    viewModel.logThrowIn(team, latestTap?.x, latestTap?.y)
                    Toast.makeText(context, "$team Throw-in logged", Toast.LENGTH_SHORT).show()
                    showInPlayActions = false
                },
                onFreeKick = { team, cause ->
                    viewModel.logFreeKickEvent(
                        teamSide = team,
                        cause = cause,
                        isDangerous = isDangerousZone,
                        booking = null,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y
                    )
                    Toast.makeText(context, "$team Free Kick ($cause) logged", Toast.LENGTH_SHORT).show()
                    showInPlayActions = false
                },
                onShot = { team, category, header, post, bar, insideBox, outcome ->
                    viewModel.logShotEvent(
                        teamSide = team,
                        shotCategory = category,
                        isHeader = header,
                        isPost = post,
                        isBar = bar,
                        isInsideBox = insideBox,
                        outcome = outcome,
                        ballX = latestTap?.x,
                        ballY = latestTap?.y,
                        onGoalScored = {
                            Toast.makeText(context, "GOAL by $team!", Toast.LENGTH_LONG).show()
                        }
                    )
                    Toast.makeText(context, "$team Shot: $category${if (outcome != null) " ($outcome)" else ""}", Toast.LENGTH_SHORT).show()
                    showInPlayActions = false
                },
                onGoalKick = { team ->
                    val switchedPossession = if (team.isNotBlank()) team else (if (currentPossession.equals("HOME", ignoreCase = true)) "AWAY" else "HOME")
                    viewModel.logGoalKick(switchedPossession, latestTap?.x, latestTap?.y)
                    Toast.makeText(context, "$switchedPossession Goal Kick — Possession switched to $switchedPossession", Toast.LENGTH_SHORT).show()
                    showInPlayActions = false
                },
                onOffside = { team ->
                    val defendingTeam = if (team.equals("AWAY", ignoreCase = true)) "HOME" else "AWAY"
                    viewModel.logOffside(team, latestTap?.x, latestTap?.y)
                    Toast.makeText(context, "$team Offside — Possession awarded to $defendingTeam", Toast.LENGTH_SHORT).show()
                    showInPlayActions = false
                },
                onVideoReferee = {
                    showInPlayActions = false
                    activeWorkflow = ActiveWorkflow.VAR
                },
                onDismiss = { showInPlayActions = false }
            )
        }

        // GENERAL EVENTS MENU WORKFLOW OVERLAY
        if (showGeneralEventsMenu && match != null) {
            GeneralEventsContainer(
                viewModel = viewModel,
                match = match!!,
                events = events,
                onDismiss = { showGeneralEventsMenu = false },
                onTriggerInterruption = { reason ->
                    activeInterruptionReason = reason
                    viewModel.startInterruption(reason)
                }
            )
        }

        // RED HAND OVERLAY FOR TEMPORARY INTERRUPTION
        if (activeInterruptionReason != null) {
            RedHandInterruptionOverlay(
                reason = activeInterruptionReason!!,
                onResumeMatch = {
                    activeInterruptionReason = null
                    viewModel.endInterruption()
                    Toast.makeText(context, "Match Resumed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ═════════════════════════════════════════════════════════════════════
        // GOAL WORKFLOW SCREENS
        // ═════════════════════════════════════════════════════════════════════
        when (goalWorkflowState) {
            GoalWorkflowState.TEAM_SELECTION -> {
                GoalTeamSelectionScreen(
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    selectedTeam = selectedGoalTeam,
                    onSelectTeam = { team ->
                        selectedGoalTeam = team
                        val autoInside = if (team == "HOME") {
                            latestTap != null && (latestTap.x >= 0.85f || (latestTap.x * 100f) >= 85f)
                        } else {
                            latestTap != null && (latestTap.x <= 0.15f || (latestTap.x * 100f) <= 15f)
                        }
                        isInsideBox = autoInside
                        goalWorkflowState = GoalWorkflowState.GOAL_DETAILS
                    },
                    onDismiss = { goalWorkflowState = null }
                )
            }

            GoalWorkflowState.GOAL_DETAILS -> {
                GoalDetailsScreen(
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    selectedTeam = selectedGoalTeam,
                    currentHomeScore = homeScore,
                    currentAwayScore = awayScore,
                    selectedHowScored = selectedHowScored,
                    onSelectHowScored = { selectedHowScored = it },
                    isDoubtfulMinute = isDoubtfulMinute,
                    onToggleDoubtfulMinute = { isDoubtfulMinute = it },
                    isDoubtfulScorer = isDoubtfulScorer,
                    onToggleDoubtfulScorer = { isDoubtfulScorer = it },
                    isInsideBox = isInsideBox,
                    onToggleInsideBox = { isInsideBox = it },
                    isDoubtfulGoal = isDoubtfulGoal,
                    onToggleDoubtfulGoal = { isDoubtfulGoal = it },
                    onCancelGoal = { goalWorkflowState = GoalWorkflowState.CANCEL_REASONS },
                    onVideoReferee = {
                        goalWorkflowState = null
                        activeWorkflow = ActiveWorkflow.VAR
                    },
                    onConfirmGoal = {
                        if (advancedPlayerDataEnabled) {
                            goalWorkflowState = GoalWorkflowState.ADVANCED_PLAYER_DATA
                        } else {
                            val defaultScorer = if (selectedGoalTeam == "HOME") "$homeTeamName Goal" else "$awayTeamName Goal"
                            viewModel.confirmGoalWorkflow(
                                teamSide = selectedGoalTeam,
                                howScored = selectedHowScored,
                                isDoubtfulMinute = isDoubtfulMinute,
                                isDoubtfulScorer = isDoubtfulScorer,
                                isInsideBox = isInsideBox,
                                isDoubtfulGoal = isDoubtfulGoal,
                                scorerName = defaultScorer,
                                assistName = null,
                                ballX = latestTap?.x,
                                ballY = latestTap?.y,
                                onSuccess = { timeDisplay, newHomeScore, newAwayScore ->
                                    val nextPossession = if (selectedGoalTeam.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                                    val nextTeamName = if (nextPossession == "HOME") homeTeamName else awayTeamName
                                    Toast.makeText(context, "GOAL at $timeDisplay — Possession to $nextTeamName ($nextPossession)", Toast.LENGTH_LONG).show()
                                    goalWorkflowState = GoalWorkflowState.AFTER_GOAL_KICKOFF
                                }
                            )
                        }
                    }
                )
            }

            GoalWorkflowState.CANCEL_REASONS -> {
                GoalCancelReasonsScreen(
                    teamName = if (selectedGoalTeam == "HOME") homeTeamName else awayTeamName,
                    onSelectReason = { reason ->
                        viewModel.logGoalCancelled(selectedGoalTeam, reason)
                        Toast.makeText(context, "Goal Cancelled: $reason", Toast.LENGTH_SHORT).show()
                        goalWorkflowState = null
                    },
                    onBack = { goalWorkflowState = GoalWorkflowState.GOAL_DETAILS }
                )
            }

            GoalWorkflowState.ADVANCED_PLAYER_DATA -> {
                AdvancedPlayerDataDialog(
                    scoringTeam = selectedGoalTeam,
                    scoringTeamName = if (selectedGoalTeam == "HOME") homeTeamName else awayTeamName,
                    lineups = lineups,
                    onDismiss = { goalWorkflowState = GoalWorkflowState.GOAL_DETAILS },
                    onConfirm = { scorer, assist ->
                        viewModel.confirmGoalWorkflow(
                            teamSide = selectedGoalTeam,
                            howScored = selectedHowScored,
                            isDoubtfulMinute = isDoubtfulMinute,
                            isDoubtfulScorer = isDoubtfulScorer,
                            isInsideBox = isInsideBox,
                            isDoubtfulGoal = isDoubtfulGoal,
                            scorerName = scorer,
                            assistName = assist,
                            ballX = latestTap?.x,
                            ballY = latestTap?.y,
                            onSuccess = { timeDisplay, newHomeScore, newAwayScore ->
                                val nextPossession = if (selectedGoalTeam.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                                val nextTeamName = if (nextPossession == "HOME") homeTeamName else awayTeamName
                                Toast.makeText(context, "GOAL at $timeDisplay — Possession to $nextTeamName ($nextPossession)", Toast.LENGTH_LONG).show()
                                goalWorkflowState = GoalWorkflowState.AFTER_GOAL_KICKOFF
                            }
                        )
                    }
                )
            }

            GoalWorkflowState.AFTER_GOAL_KICKOFF -> {
                AfterGoalKickoffScreen(
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    onRestartPlay = {
                        val nextPossession = if (selectedGoalTeam.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                        val nextTeamName = if (nextPossession == "HOME") homeTeamName else awayTeamName
                        viewModel.setPossession(nextPossession)
                        viewModel.logBallTap(x = 0.5f, y = 0.5f, side = nextPossession, isDangerous = false)
                        viewModel.logAfterGoalKickoff()
                        Toast.makeText(context, "Kickoff restarted from center by $nextTeamName", Toast.LENGTH_SHORT).show()
                        goalWorkflowState = null
                    }
                )
            }

            null -> {}
        }
    }

    if (showPeriodDialog) {
        LivePeriodDialog(
            currentPeriod = currentPeriod,
            onDismiss = { showPeriodDialog = false },
            onSelectPeriod = { newPeriod ->
                viewModel.setPeriod(newPeriod)
                showPeriodDialog = false
            }
        )
    }

    if (showEndMatchConfirm) {
        AlertDialog(
            onDismissRequest = { showEndMatchConfirm = false },
            containerColor = ScoutSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("End Match & Generate Report?", style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary)
            },
            text = {
                Text("This will mark the match as FINISHED and synthesize the scouting dossier.", style = ScoutTypographyBodyL.copy(fontSize = 20.sp), color = ScoutTextSecondary)
            },
            confirmButton = {
                ScoutButton(
                    text = "End Match",
                    style = ScoutButtonStyle.DANGER,
                    onClick = {
                        showEndMatchConfirm = false
                        viewModel.finishMatch()
                        onNavigateToReport(matchId)
                    },
                    testTag = "confirm_end_match_btn"
                )
            },
            dismissButton = {
                ScoutButton(
                    text = "Keep Playing",
                    style = ScoutButtonStyle.SECONDARY,
                    onClick = { showEndMatchConfirm = false }
                )
            }
        )
    }

    if (showMoreEventsDialog) {
        MoreEventsDialog(
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            currentPossession = currentPossession,
            onDismiss = { showMoreEventsDialog = false },
            onEventLogged = { eventType, team, player, extra ->
                viewModel.logEvent(
                    eventType = eventType,
                    teamSide = team,
                    playerName = player,
                    extraInfo = extra,
                    ballX = latestTap?.x,
                    ballY = latestTap?.y
                )
                if (eventType == "OFFSIDE") {
                    val defendingTeam = if (team.equals("AWAY", ignoreCase = true)) "HOME" else "AWAY"
                    val offsideX = latestTap?.x ?: (if (isHomeOnLeft) (if (team.equals("AWAY", ignoreCase = true)) 0.35f else 0.65f) else (if (team.equals("AWAY", ignoreCase = true)) 0.65f else 0.35f))
                    viewModel.logBallTap(x = offsideX, y = 0.5f, side = defendingTeam, isDangerous = false)
                    Toast.makeText(context, "$team Offside — Possession awarded to $defendingTeam", Toast.LENGTH_SHORT).show()
                } else if (eventType == "GOAL") {
                    val concedingTeam = if (team.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                    val concedingTeamName = if (concedingTeam == "HOME") homeTeamName else awayTeamName
                    Toast.makeText(context, "GOAL by $team! Possession to $concedingTeamName ($concedingTeam) — Kickoff at center", Toast.LENGTH_LONG).show()
                } else if (eventType == "GOAL_KICK") {
                    val switchedPossession = if (team.isNotBlank()) team else (if (currentPossession.equals("HOME", ignoreCase = true)) "AWAY" else "HOME")
                    val gkX = if (isHomeOnLeft) (if (switchedPossession == "HOME") 0.08f else 0.92f) else (if (switchedPossession == "HOME") 0.92f else 0.08f)
                    viewModel.logBallTap(x = gkX, y = 0.5f, side = switchedPossession, isDangerous = false)
                    Toast.makeText(context, "$switchedPossession Goal Kick — Possession switched to $switchedPossession", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "$eventType logged", Toast.LENGTH_SHORT).show()
                }
                showMoreEventsDialog = false
            }
        )
    }
}

private data class DrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String,
    val accentColor: Color = ScoutPrimaryAction
)

// ═════════════════════════════════════════════════════════════════════════════
// PITCH CANVAS & SWIPE GESTURES
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun LivePitchCanvas(
    ballTaps: List<BallTapEntity>,
    isDangerousAttack: Boolean,
    currentPossession: String,
    isHomeOnLeft: Boolean,
    cornerArrowPos: Pair<Float, Float>?,
    onPitchTap: (normX: Float, normY: Float) -> Unit,
    onPitchDoubleTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUpFromBottom: () -> Unit
) {
    val density = LocalDensity.current
    val minSwipePx = with(density) { 72.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Interactive landscape football pitch. Swipe left for away possession, swipe right for home possession, swipe up from bottom for menu, tap to log ball location, double tap for in-play actions."
            }
            .pointerInput(Unit) {
                var lastTapTime = 0L
                var lastTapX = 0f
                var lastTapY = 0f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var dragHandled = false
                    val startX = down.position.x
                    val startY = down.position.y

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            if (!dragHandled && abs(totalDragX) < 20f && abs(totalDragY) < 20f) {
                                val now = System.currentTimeMillis()
                                val normX = (startX / size.width).coerceIn(0f, 1f)
                                val normY = (startY / size.height).coerceIn(0f, 1f)
                                onPitchTap(normX, normY)

                                if (now - lastTapTime < 400L && abs(startX - lastTapX) < 100f && abs(startY - lastTapY) < 100f) {
                                    lastTapTime = 0L
                                    onPitchDoubleTap()
                                } else {
                                    lastTapTime = now
                                    lastTapX = startX
                                    lastTapY = startY
                                }
                            }
                            break
                        }

                        val dragDeltaX = change.position.x - change.previousPosition.x
                        val dragDeltaY = change.position.y - change.previousPosition.y
                        totalDragX += dragDeltaX
                        totalDragY += dragDeltaY

                        if (!dragHandled) {
                            if (totalDragY <= -minSwipePx && abs(totalDragY) > abs(totalDragX)) {
                                dragHandled = true
                                onSwipeUpFromBottom()
                            } else if (abs(totalDragX) >= minSwipePx) {
                                dragHandled = true
                                if (totalDragX > 0) {
                                    onSwipeRight()
                                } else {
                                    onSwipeLeft()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeW = 2.5.dp.toPx()
            val lineCol = Color.White

            // 1. Alternating vertical grass mowing stripes (10 bands)
            val stripeCount = 10
            val stripeW = w / stripeCount
            for (i in 0 until stripeCount) {
                val stripeColor = if (i % 2 == 0) Color(0xFF2E9E40) else Color(0xFF268E37)
                drawRect(color = stripeColor, topLeft = Offset(i * stripeW, 0f), size = Size(stripeW, h))
            }

            // 2. Tactical vertical dashed lines (at 25% and 75% width)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.30f),
                start = Offset(w * 0.25f, 0f),
                end = Offset(w * 0.25f, h),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = Color.White.copy(alpha = 0.30f),
                start = Offset(w * 0.75f, 0f),
                end = Offset(w * 0.75f, h),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )

            // 3. Dangerous Zone Overlay (Only on opponent's third)
            val attackingThirdWidth = w * 0.25f
            val isAttackingRight = if (currentPossession.equals("HOME", ignoreCase = true)) isHomeOnLeft else !isHomeOnLeft
            val dangerLeft = if (isAttackingRight) w - attackingThirdWidth else 0f
            if (isDangerousAttack) {
                drawRect(color = ScoutErrorDanger.copy(alpha = 0.22f), topLeft = Offset(dangerLeft, 0f), size = Size(attackingThirdWidth, h))
                drawRect(color = ScoutErrorDanger, topLeft = Offset(dangerLeft, 0f), size = Size(attackingThirdWidth, h), style = Stroke(width = 3.dp.toPx()))
            }

            // 4. White Pitch Lines
            drawRect(color = lineCol, topLeft = Offset.Zero, size = Size(w, h), style = Stroke(width = strokeW))
            drawLine(color = lineCol, start = Offset(w / 2f, 0f), end = Offset(w / 2f, h), strokeWidth = strokeW)

            val centerRadius = h * 0.20f
            drawCircle(color = lineCol, radius = centerRadius, center = Offset(w / 2f, h / 2f), style = Stroke(width = strokeW))
            drawCircle(color = lineCol, radius = 3.5.dp.toPx(), center = Offset(w / 2f, h / 2f))

            val penBoxW = w * (16.5f / 105f)
            val penBoxH = h * (40.3f / 68f)
            val penBoxY = (h - penBoxH) / 2f
            drawRect(color = lineCol, topLeft = Offset(0f, penBoxY), size = Size(penBoxW, penBoxH), style = Stroke(width = strokeW))
            drawRect(color = lineCol, topLeft = Offset(w - penBoxW, penBoxY), size = Size(penBoxW, penBoxH), style = Stroke(width = strokeW))

            val goalBoxW = w * (5.5f / 105f)
            val goalBoxH = h * (18.3f / 68f)
            val goalBoxY = (h - goalBoxH) / 2f
            drawRect(color = lineCol, topLeft = Offset(0f, goalBoxY), size = Size(goalBoxW, goalBoxH), style = Stroke(width = strokeW))
            drawRect(color = lineCol, topLeft = Offset(w - goalBoxW, goalBoxY), size = Size(goalBoxW, goalBoxH), style = Stroke(width = strokeW))

            val penSpotXLeft = w * (11f / 105f)
            val penSpotXRight = w - (w * (11f / 105f))
            drawCircle(color = lineCol, radius = 3.5.dp.toPx(), center = Offset(penSpotXLeft, h / 2f))
            drawCircle(color = lineCol, radius = 3.5.dp.toPx(), center = Offset(penSpotXRight, h / 2f))

            // D-Arcs (penalty arcs outside 18-yard box)
            drawArc(
                color = lineCol,
                startAngle = -53f,
                sweepAngle = 106f,
                useCenter = false,
                topLeft = Offset(penSpotXLeft - centerRadius, (h / 2f) - centerRadius),
                size = Size(centerRadius * 2, centerRadius * 2),
                style = Stroke(strokeW)
            )
            drawArc(
                color = lineCol,
                startAngle = 127f,
                sweepAngle = 106f,
                useCenter = false,
                topLeft = Offset(penSpotXRight - centerRadius, (h / 2f) - centerRadius),
                size = Size(centerRadius * 2, centerRadius * 2),
                style = Stroke(strokeW)
            )

            // Corner arcs
            val cornerR = 12.dp.toPx()
            drawArc(lineCol, 0f, 90f, false, Offset(-cornerR, -cornerR), Size(cornerR * 2, cornerR * 2), style = Stroke(strokeW))
            drawArc(lineCol, 270f, 90f, false, Offset(-cornerR, h - cornerR), Size(cornerR * 2, cornerR * 2), style = Stroke(strokeW))
            drawArc(lineCol, 90f, 90f, false, Offset(w - cornerR, -cornerR), Size(cornerR * 2, cornerR * 2), style = Stroke(strokeW))
            drawArc(lineCol, 180f, 90f, false, Offset(w - cornerR, h - cornerR), Size(cornerR * 2, cornerR * 2), style = Stroke(strokeW))

            // 5. Far right red vertical boundary strip (as seen in IMG_8639)
            drawRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(w - 4.dp.toPx(), 0f),
                size = Size(4.dp.toPx(), h)
            )

            // 6. BALL SPOTTING & PASS TRAIL (Red circle with white directional arrow)
            val recentTaps = ballTaps.takeLast(2)

            val currentTapPos = if (recentTaps.isNotEmpty()) {
                Offset(recentTaps.last().x * w, recentTaps.last().y * h)
            } else {
                Offset(w / 2f, h / 2f)
            }

            // Previous tap pass trail (dotted line and subtle previous dot)
            if (recentTaps.size > 1) {
                val prevTapPos = Offset(recentTaps.first().x * w, recentTaps.first().y * h)
                drawLine(
                    color = Color.Black.copy(alpha = 0.35f),
                    start = prevTapPos,
                    end = currentTapPos,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashEffect
                )
                drawCircle(color = Color(0xFFE53935).copy(alpha = 0.5f), radius = 8.dp.toPx(), center = prevTapPos)
                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 8.dp.toPx(), center = prevTapPos, style = Stroke(width = 1.dp.toPx()))
            }

            // Current ball spot: Vibrant red circle with white border
            val ballRadius = 14.dp.toPx()
            drawCircle(color = Color(0xFFE53935), radius = ballRadius, center = currentTapPos)
            drawCircle(color = Color.White, radius = ballRadius, center = currentTapPos, style = Stroke(width = 2.dp.toPx()))

            // Directional arrow inside red circle indicating attacking direction
            val arrowPath = Path().apply {
                val arrowW = ballRadius * 0.65f
                val arrowH = ballRadius * 0.70f
                if (isAttackingRight) {
                    moveTo(currentTapPos.x + arrowW * 0.6f, currentTapPos.y)
                    lineTo(currentTapPos.x - arrowW * 0.6f, currentTapPos.y - arrowH * 0.6f)
                    lineTo(currentTapPos.x - arrowW * 0.6f, currentTapPos.y + arrowH * 0.6f)
                } else {
                    moveTo(currentTapPos.x - arrowW * 0.6f, currentTapPos.y)
                    lineTo(currentTapPos.x + arrowW * 0.6f, currentTapPos.y - arrowH * 0.6f)
                    lineTo(currentTapPos.x + arrowW * 0.6f, currentTapPos.y + arrowH * 0.6f)
                }
                close()
            }
            drawPath(arrowPath, color = Color.White)

            // Corner flag arrow (if corner kick is pending)
            if (cornerArrowPos != null) {
                val cx = cornerArrowPos.first * w
                val cy = cornerArrowPos.second * h
                drawCircle(color = ScoutWarning, radius = 20.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 20.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// POSSESSION ARROW FEEDBACK
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PossessionArrowIndicator(possession: String, latestTap: BallTapEntity?) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenW = maxWidth
        val screenH = maxHeight

        val targetX = if (latestTap != null) (screenW.value * latestTap.x).coerceIn(40f, screenW.value - 60f).dp else (screenW.value / 2f - 20f).dp
        val targetY = if (latestTap != null) (screenH.value * latestTap.y - 48f).coerceIn(40f, screenH.value - 80f).dp else (screenH.value / 2f + 50f).dp

        Box(
            modifier = Modifier
                .offset { IntOffset(targetX.roundToPx(), targetY.roundToPx()) }
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xD90F1A2E))
                .border(2.dp, if (possession == "HOME") ScoutPrimaryAction else ScoutErrorDanger, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (possession == "HOME") Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Possession arrow",
                tint = if (possession == "HOME") ScoutPrimaryAction else ScoutErrorDanger,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CardBadge(count: Int, isRed: Boolean) {
    Surface(
        color = if (isRed) ScoutErrorDanger else ScoutWarning,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.semantics {
            contentDescription = if (isRed) "$count Red Cards" else "$count Yellow Cards"
        }
    ) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (isRed) "🟥 $count" else "🟨 $count", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
    }
}

@Composable
fun LivePeriodDialog(currentPeriod: String, onDismiss: () -> Unit, onSelectPeriod: (String) -> Unit) {
    val periods = listOf(
        Pair("FIRST_HALF", "1st Half (Running)"),
        Pair("HALF_TIME", "Half Time Interval"),
        Pair("SECOND_HALF", "2nd Half (Running)"),
        Pair("EXTRA_TIME", "Extra Time"),
        Pair("PENALTIES", "Penalty Shootout"),
        Pair("FULL_TIME", "Full Time (End)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ScoutSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Change Match Period", style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                periods.forEach { (periodKey, label) ->
                    val isCurrent = currentPeriod == periodKey
                    Surface(
                        onClick = { onSelectPeriod(periodKey) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) ScoutPrimaryAction else ScoutSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = ScoutTypographyLabelM.copy(fontSize = 18.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium, color = if (isCurrent) Color.White else ScoutTextPrimary))
                            if (isCurrent) Icon(Icons.Default.Check, contentDescription = "Current", tint = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = { ScoutButton(text = "Close", style = ScoutButtonStyle.SECONDARY, onClick = onDismiss) }
    )
}

@Composable
fun PortraitRotateOverlay(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate_animation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation_angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
            .padding(24.dp)
            .testTag("portrait_rotate_overlay"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate phone", tint = ScoutPrimaryAction, modifier = Modifier.size(96.dp).rotate(angle))
        Spacer(modifier = Modifier.height(32.dp))
        Text("Rotate your phone to landscape", style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Match timer starts when you rotate", style = ScoutTypographyBodyL.copy(fontSize = 20.sp), color = ScoutTextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.weight(1f))
        ScoutButton(text = "Back to Match Details", style = ScoutButtonStyle.SECONDARY, onClick = onBack, testTag = "portrait_back_btn")
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// GOAL WORKFLOW SCREENS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun GoalTeamSelectionScreen(homeTeamName: String, awayTeamName: String, selectedTeam: String, onSelectTeam: (String) -> Unit, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize().testTag("goal_team_selection_screen"), color = ScoutBackground) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Select Scoring Team", style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary)
                Surface(onClick = onDismiss, shape = CircleShape, color = ScoutSurfaceVariant, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ScoutTextPrimary) }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier.weight(1f).height(220.dp).clickable { onSelectTeam("HOME") }.testTag("goal_select_home_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(if (selectedTeam == "HOME") 4.dp else 2.dp, ScoutPrimaryAction)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = ScoutPrimaryAction, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(homeTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                        Text("HOME TEAM", style = ScoutTypographyLabelM.copy(color = ScoutPrimaryAction))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).height(220.dp).clickable { onSelectTeam("AWAY") }.testTag("goal_select_away_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(if (selectedTeam == "AWAY") 4.dp else 2.dp, ScoutErrorDanger)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = ScoutErrorDanger, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(awayTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                        Text("AWAY TEAM", style = ScoutTypographyLabelM.copy(color = ScoutErrorDanger))
                    }
                }
            }
        }
    }
}

// GoalDetailsScreen and GoalCancelReasonsScreen are now cleanly extracted into GoalWorkflowScreens.kt

@Composable
fun AfterGoalKickoffScreen(homeTeamName: String, awayTeamName: String, homeScore: Int, awayScore: Int, onRestartPlay: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(ScoutReportCardBg).padding(24.dp).testTag("after_goal_kickoff_screen"), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("$homeTeamName $homeScore — $awayScore $awayTeamName", style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color.White), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(36.dp))
            Surface(onClick = onRestartPlay, shape = CircleShape, color = ScoutPrimaryAction, shadowElevation = 12.dp, modifier = Modifier.size(140.dp).testTag("after_goal_restart_btn")) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(32.dp)) }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text("Tap at the exact moment play restarts", style = ScoutTypographyBodyL.copy(fontSize = 20.sp, color = Color.White.copy(alpha = 0.9f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPlayerDataDialog(scoringTeam: String, scoringTeamName: String, lineups: List<LineupEntity>, onDismiss: () -> Unit, onConfirm: (scorer: String, assist: String?) -> Unit) {
    val teamPlayers = lineups.filter { it.team.equals(scoringTeam, ignoreCase = true) }
    val playerNames = teamPlayers.map { "#${it.jerseyNumber} ${it.playerName}" }
    var scorerExpanded by remember { mutableStateOf(false) }
    var selectedScorer by remember { mutableStateOf(playerNames.firstOrNull() ?: "$scoringTeamName Player") }
    var assistExpanded by remember { mutableStateOf(false) }
    var selectedAssist by remember { mutableStateOf<String?>("None (Unassisted)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).testTag("advanced_player_data_dialog"), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ScoutSurface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Goal Scorer & Assist Details", style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary)
                ExposedDropdownMenuBox(expanded = scorerExpanded, onExpandedChange = { scorerExpanded = !scorerExpanded }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = selectedScorer, onValueChange = {}, readOnly = true, label = { Text("Goal Scorer *", fontSize = 18.sp) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scorerExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth().height(64.dp), shape = RoundedCornerShape(14.dp))
                    ExposedDropdownMenu(expanded = scorerExpanded, onDismissRequest = { scorerExpanded = false }) {
                        playerNames.forEach { name -> DropdownMenuItem(text = { Text(name, fontSize = 18.sp) }, onClick = { selectedScorer = name; scorerExpanded = false }) }
                    }
                }
                ExposedDropdownMenuBox(expanded = assistExpanded, onExpandedChange = { assistExpanded = !assistExpanded }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = selectedAssist ?: "None (Unassisted)", onValueChange = {}, readOnly = true, label = { Text("Assist (Optional)", fontSize = 18.sp) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assistExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth().height(64.dp), shape = RoundedCornerShape(14.dp))
                    ExposedDropdownMenu(expanded = assistExpanded, onDismissRequest = { assistExpanded = false }) {
                        DropdownMenuItem(text = { Text("None (Unassisted)", fontSize = 18.sp) }, onClick = { selectedAssist = null; assistExpanded = false })
                        playerNames.forEach { name -> DropdownMenuItem(text = { Text(name, fontSize = 18.sp) }, onClick = { selectedAssist = name; assistExpanded = false }) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoutButton(text = "Cancel", style = ScoutButtonStyle.SECONDARY, onClick = onDismiss, modifier = Modifier.weight(1f))
                    ScoutButton(text = "Confirm & Save", style = ScoutButtonStyle.PRIMARY, onClick = { onConfirm(selectedScorer, if (selectedAssist == "None (Unassisted)") null else selectedAssist) }, modifier = Modifier.weight(1f), testTag = "confirm_advanced_player_btn")
                }
            }
        }
    }
}

@Composable
fun MoreEventsDialog(homeTeamName: String, awayTeamName: String, currentPossession: String, onDismiss: () -> Unit, onEventLogged: (eventType: String, team: String, player: String, extra: String?) -> Unit) {
    var selectedType by remember { mutableStateOf("SUBSTITUTION") }
    var selectedTeam by remember { mutableStateOf(currentPossession) }
    var detailText by remember { mutableStateOf("") }

    val otherTypes = listOf(
        Pair("SUBSTITUTION", "🔄 Substitution"),
        Pair("FOUL", "⚠️ Foul"),
        Pair("OFFSIDE", "🚩 Offside"),
        Pair("SHOT_ON_TARGET", "🎯 Shot on Target"),
        Pair("SHOT_OFF_TARGET", "❌ Shot off Target"),
        Pair("SAVE", "🧤 Goalkeeper Save")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ScoutSurface)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Log Tactical Event", style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = ScoutTextPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    otherTypes.chunked(2).forEach { rowPair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowPair.forEach { (typeKey, label) ->
                                val isSelected = selectedType == typeKey
                                Surface(onClick = { selectedType = typeKey }, shape = RoundedCornerShape(12.dp), color = if (isSelected) ScoutPrimaryAction else ScoutSurfaceVariant, modifier = Modifier.weight(1f).height(56.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text(label, style = ScoutTypographyLabelM.copy(fontSize = 16.sp, color = if (isSelected) Color.White else ScoutTextPrimary)) }
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(onClick = { selectedTeam = "HOME" }, shape = RoundedCornerShape(12.dp), color = if (selectedTeam == "HOME") ScoutPrimaryAction else ScoutSurfaceVariant, modifier = Modifier.weight(1f).height(56.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("HOME ($homeTeamName)", style = ScoutTypographyLabelM.copy(color = if (selectedTeam == "HOME") Color.White else ScoutTextPrimary)) }
                    }
                    Surface(onClick = { selectedTeam = "AWAY" }, shape = RoundedCornerShape(12.dp), color = if (selectedTeam == "AWAY") ScoutPrimaryAction else ScoutSurfaceVariant, modifier = Modifier.weight(1f).height(56.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("AWAY ($awayTeamName)", style = ScoutTypographyLabelM.copy(color = if (selectedTeam == "AWAY") Color.White else ScoutTextPrimary)) }
                    }
                }
                OutlinedTextField(value = detailText, onValueChange = { detailText = it }, label = { Text("Event Details", fontSize = 16.sp) }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoutButton(text = "Cancel", style = ScoutButtonStyle.SECONDARY, onClick = onDismiss, modifier = Modifier.weight(1f))
                    ScoutButton(text = "Save Event", style = ScoutButtonStyle.PRIMARY, onClick = { onEventLogged(selectedType, selectedTeam, if (selectedTeam == "HOME") homeTeamName else awayTeamName, detailText.ifBlank { null }) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun formatTimer(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
