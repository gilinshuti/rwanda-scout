package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel

@Composable
fun KickoffScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit,
    onKickoffStarted: (Long) -> Unit
) {
    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()
    val scrollState = rememberScrollState()

    val homeTeamName = match?.homeTeam ?: "Home Team"
    val awayTeamName = match?.awayTeam ?: "Away Team"

    val homeColor = parseTeamColor(match?.homeColor, isHome = true)
    val awayColor = parseTeamColor(match?.awayColor, isHome = false)

    // Left Team side: initially "HOME"
    var leftTeamSide by remember { mutableStateOf("HOME") }
    val isHomeOnLeft = leftTeamSide == "HOME"

    val leftTeamName = if (isHomeOnLeft) homeTeamName else awayTeamName
    val leftTeamColor = if (isHomeOnLeft) homeColor else awayColor

    val rightTeamName = if (isHomeOnLeft) awayTeamName else homeTeamName
    val rightTeamColor = if (isHomeOnLeft) awayColor else homeColor

    // Dialog state for confirming kickoff
    var confirmingKickoffSide by remember { mutableStateOf<String?>(null) } // "HOME" or "AWAY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        ScoutTopBar(
            title = "Match Kickoff",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // "KICKOFF" 32sp Bold #0F1A2E, "NOT STARTED" 20sp #718096
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KICKOFF",
                    style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NOT STARTED",
                    style = ScoutTypographyLabelL.copy(fontSize = 20.sp),
                    color = ScoutTextHint
                )
            }

            // Team names with TacticalJerseyBadge
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Home Team Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.ui.components.TacticalJerseyBadge(color = homeColor, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = homeTeamName,
                        style = ScoutTypographyTitleL.copy(fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                }

                // Away Team Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.ui.components.TacticalJerseyBadge(color = awayColor, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = awayTeamName,
                        style = ScoutTypographyTitleL.copy(fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Sides Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(14.dp).background(leftTeamColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "◀ $leftTeamName (Left)",
                        style = ScoutTypographyLabelM.copy(fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$rightTeamName (Right) ▶",
                        style = ScoutTypographyLabelM.copy(fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(14.dp).background(rightTeamColor, CircleShape))
                }
            }

            // Mini horizontal pitch with realistic turf stripes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScoutPitchGrass)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeW = 2.dp.toPx()
                    val lineColor = ScoutPitchLines

                    // Alternating lawn stripes
                    val bandsCount = 8
                    val bandWidth = w / bandsCount
                    for (i in 0 until bandsCount) {
                        val bandColor = if (i % 2 == 0) ScoutPitchGrass else ScoutPitchGrassStripe
                        drawRect(
                            color = bandColor,
                            topLeft = Offset(i * bandWidth, 0f),
                            size = Size(bandWidth, h)
                        )
                    }

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
                        radius = h * 0.22f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = strokeW)
                    )

                    // Center spot
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = Offset(w / 2f, h / 2f)
                    )

                    // Left box
                    val boxW = w * 0.16f
                    val boxH = h * 0.6f
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(0f, (h - boxH) / 2f),
                        size = Size(boxW, boxH),
                        style = Stroke(width = strokeW)
                    )

                    // Right box
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(w - boxW, (h - boxH) / 2f),
                        size = Size(boxW, boxH),
                        style = Stroke(width = strokeW)
                    )
                }
            }

            // Two large arrows 96dp: blue #0B7EB8 left, red #C53030 right
            // both with contentDescription "Select [team] to kick off"
            Text(
                text = "Tap arrow to select team to kick off:",
                style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow (Blue #0B7EB8, 96dp)
                val leftSideTeamName = if (isHomeOnLeft) homeTeamName else awayTeamName
                val leftSideTeamCode = if (isHomeOnLeft) "HOME" else "AWAY"

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0B7EB8))
                        .clickable { confirmingKickoffSide = leftSideTeamCode }
                        .testTag("kickoff_arrow_left")
                        .semantics { contentDescription = "Select $leftSideTeamName to kick off" },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Select $leftSideTeamName to kick off",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = leftSideTeamName.take(8),
                            style = ScoutTypographyCaption.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Right Arrow (Red #C53030, 96dp)
                val rightSideTeamName = if (isHomeOnLeft) awayTeamName else homeTeamName
                val rightSideTeamCode = if (isHomeOnLeft) "AWAY" else "HOME"

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFC53030))
                        .clickable { confirmingKickoffSide = rightSideTeamCode }
                        .testTag("kickoff_arrow_right")
                        .semantics { contentDescription = "Select $rightSideTeamName to kick off" },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Select $rightSideTeamName to kick off",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = rightSideTeamName.take(8),
                            style = ScoutTypographyCaption.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Change Sides" 48dp outlined, 14.5sp Bold + swap icon
            OutlinedButton(
                onClick = {
                    leftTeamSide = if (leftTeamSide == "HOME") "AWAY" else "HOME"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("change_sides_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, ScoutPrimaryAction),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = ScoutSurface,
                    contentColor = ScoutPrimaryAction
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = ScoutPrimaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change Sides",
                        style = ScoutTypographyLabelL.copy(
                            color = ScoutPrimaryAction,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Tap arrow -> dialog
    if (confirmingKickoffSide != null) {
        val selectedSide = confirmingKickoffSide!!
        val selectedTeamName = if (selectedSide == "HOME") homeTeamName else awayTeamName

        AlertDialog(
            onDismissRequest = { confirmingKickoffSide = null },
            title = {
                Text(
                    text = "Confirm 1st Half Kickoff?",
                    style = ScoutTypographyTitleM.copy(fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
            },
            text = {
                Text(
                    text = "$selectedTeamName has been selected to take the opening kickoff. Start the official match timer and begin live scouting?",
                    style = ScoutTypographyBodyM,
                    color = ScoutTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kickoffSide = selectedSide
                        confirmingKickoffSide = null
                        viewModel.startKickoff(
                            kickoffTeam = kickoffSide,
                            leftTeamSide = leftTeamSide
                        )
                        onKickoffStarted(matchId)
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("confirm_kickoff_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScoutSuccessLive,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "CONFIRM",
                        style = ScoutTypographyLabelM.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmingKickoffSide = null },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("cancel_kickoff_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, ScoutDivider),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = ScoutTextSecondary
                    )
                ) {
                    Text(
                        text = "CANCEL",
                        style = ScoutTypographyLabelM.copy(
                            color = ScoutTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            containerColor = ScoutSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
