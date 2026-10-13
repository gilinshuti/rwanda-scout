package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.MatchEntity
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.ScoutBackground
import com.example.ui.theme.ScoutDivider
import com.example.ui.theme.ScoutPitchGrass
import com.example.ui.theme.ScoutPitchLines
import com.example.ui.theme.ScoutPrimaryAction
import com.example.ui.theme.ScoutReportAccents
import com.example.ui.theme.ScoutReportCardBg
import com.example.ui.theme.ScoutSurface
import com.example.ui.theme.ScoutTextPrimary
import com.example.ui.theme.ScoutTextSecondary
import com.example.ui.viewmodel.ScoutViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Date

data class TeamStatsCalculations(
    val possessionPercent: Int,
    val totalShots: Int,
    val shotsOnTarget: Int,
    val shotsOffTarget: Int,
    val corners: Int,
    val offsides: Int,
    val fouls: Int,
    val yellowCards: Int,
    val redCards: Int
)

data class CalculatedMatchStats(
    val home: TeamStatsCalculations,
    val away: TeamStatsCalculations
)

@Composable
fun MatchReportScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // PORTRAIT ONLY: release landscape lock if entered from pitch
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()
    val events by viewModel.activeEvents.collectAsState()
    val ballTaps by viewModel.activeBallTaps.collectAsState()
    val summary by viewModel.activeSummary.collectAsState()
    val isAiSummaryEnabled by viewModel.isAiSummaryEnabled.collectAsState()
    val isGeneratingSummary by viewModel.isGeneratingSummary.collectAsState()

    val currentMatch = match

    // Calculate stats according to specified Room rules
    val stats = remember(events, ballTaps) {
        calculateMatchStats(events, ballTaps)
    }

    val homeTeamName = currentMatch?.homeTeam ?: "Home"
    val awayTeamName = currentMatch?.awayTeam ?: "Away"
    val homeScore = currentMatch?.homeScore ?: 0
    val awayScore = currentMatch?.awayScore ?: 0

    val dateFormatted = remember(currentMatch?.matchDateMillis) {
        currentMatch?.matchDateMillis?.let {
            DateFormat.format("EEEE, d MMMM yyyy", Date(it)).toString()
        } ?: "Match Date"
    }

    val competitionText = remember(currentMatch?.competition, currentMatch?.venue) {
        val comp = currentMatch?.competition ?: "Rwanda Premier League"
        val ven = currentMatch?.venue?.let { " • $it" } ?: ""
        "$comp$ven"
    }

    // Goals & Cards filtered lists
    val goalEvents = remember(events) {
        events.filter { it.eventType == "GOAL" }
            .sortedBy { it.matchTimeSeconds }
    }

    val cardEvents = remember(events) {
        events.filter { isCardEvent(it) }
            .sortedBy { it.matchTimeSeconds }
    }

    val timelineEvents = remember(events) {
        events.sortedBy { it.matchTimeSeconds }
    }

    // Penalty shootout condition
    val hasPenaltyShootout = remember(currentMatch, events) {
        (currentMatch != null && (currentMatch.homePenScore > 0 || currentMatch.awayPenScore > 0)) ||
                events.any { it.isPenaltyShootout }
    }
    val homePenScore = remember(currentMatch, events) {
        if (currentMatch != null && (currentMatch.homePenScore > 0 || currentMatch.awayPenScore > 0)) {
            currentMatch.homePenScore
        } else {
            events.count { it.isPenaltyShootout && it.eventType == "GOAL" && it.team.equals("HOME", true) }
        }
    }
    val awayPenScore = remember(currentMatch, events) {
        if (currentMatch != null && (currentMatch.homePenScore > 0 || currentMatch.awayPenScore > 0)) {
            currentMatch.awayPenScore
        } else {
            events.count { it.isPenaltyShootout && it.eventType == "GOAL" && it.team.equals("AWAY", true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
            .testTag("match_report_screen")
    ) {
        ScoutTopBar(
            title = "Match Report",
            onBackClick = onNavigateBack,
            actions = {
                IconButton(
                    onClick = {
                        if (currentMatch != null) {
                            copySummaryToClipboard(context, currentMatch, stats, events)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Summary",
                        tint = ScoutPrimaryAction,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ═══════════════════════════════════════════════
            // HEADER (bright area)
            // - team names + score 40sp Bold #0F1A2E
            // - Competition/date 20sp #4A5568
            // ═══════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_header_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$homeTeamName $homeScore - $awayScore $awayTeamName",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E),
                    textAlign = TextAlign.Center,
                    lineHeight = 46.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_header_teams_score")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$competitionText • $dateFormatted",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5568),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_header_competition_date")
                )
            }

            // ═══════════════════════════════════════════════
            // AI MATCH SUMMARY SECTION (ABOVE STATS CARD)
            // ═══════════════════════════════════════════════
            if (summary != null) {
                // Bright Card:
                // - White bg, 6dp #6B46C1 left border, 20dp padding, 16dp radius
                // - "MATCH SUMMARY" 28sp Bold #0F1A2E + sparkle icon 32dp #6B46C1
                // - AI text 20sp Regular #0F1A2E, line height 1.7 (34sp)
                // - Footer: "Generated by AI • [date]" 16sp #718096
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // 6dp #6B46C1 left border
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .drawBehind {
                                    drawRect(
                                        color = Color(0xFF6B46C1),
                                        topLeft = Offset.Zero,
                                        size = Size(6.dp.toPx(), size.height)
                                    )
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 26.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // "MATCH SUMMARY" 28sp Bold #0F1A2E + sparkle icon 20dp #6B46C1
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Sparkle",
                                    tint = Color(0xFF6B46C1),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "MATCH SUMMARY",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F1A2E),
                                    modifier = Modifier.testTag("ai_summary_title")
                                )
                            }

                            // AI text 20sp Regular #0F1A2E, line height 1.7
                            Text(
                                text = summary!!.summaryText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF0F1A2E),
                                lineHeight = 34.sp,
                                modifier = Modifier.testTag("ai_summary_text")
                            )

                            // Footer: "Generated by AI • [date]" 16sp #718096
                            val formattedGenDate = remember(summary!!.generatedAtMillis) {
                                DateFormat.format("d MMMM yyyy, HH:mm", Date(summary!!.generatedAtMillis)).toString()
                            }
                            Text(
                                text = "Generated by AI • $formattedGenDate",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF718096),
                                modifier = Modifier.testTag("ai_summary_footer")
                            )

                            // Action Buttons stacked:
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // "Regenerate Summary" 48dp outlined #6B46C1 (if enabled)
                                if (isAiSummaryEnabled) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.generateAiSummary(
                                                matchId = matchId,
                                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() },
                                                onSuccess = { Toast.makeText(context, "Summary regenerated successfully", Toast.LENGTH_SHORT).show() }
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("regenerate_summary_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, Color(0xFF6B46C1)),
                                        enabled = !isGeneratingSummary,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF6B46C1)
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Regenerate",
                                                tint = Color(0xFF6B46C1),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (isGeneratingSummary) "Regenerating..." else "Regenerate Summary",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6B46C1)
                                            )
                                        }
                                    }
                                }

                                // "Copy Summary" 48dp outlined #4A5568
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Match Summary", summary!!.summaryText))
                                        Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("copy_summary_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFF4A5568)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF4A5568)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Summary",
                                            tint = Color(0xFF4A5568),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Copy Summary",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4A5568)
                                        )
                                    }
                                }

                                // "Download as TXT" 48dp outlined #4A5568
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val file = File(context.cacheDir, "MatchSummary_${matchId}.txt")
                                            file.writeText(summary!!.summaryText)
                                            Toast.makeText(context, "Downloaded to ${file.name}", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Summary saved as TXT", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("download_txt_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFF4A5568)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF4A5568)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "Download as TXT",
                                            tint = Color(0xFF4A5568),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Download as TXT",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4A5568)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isAiSummaryEnabled) {
                // If toggle ON and no summary yet:
                // "✨ Generate AI Summary" 48dp filled #6B46C1 with sparkle icon 20dp + 14sp Bold label.
                Button(
                    onClick = {
                        viewModel.generateAiSummary(
                            matchId = matchId,
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() },
                            onSuccess = { Toast.makeText(context, "AI Summary generated!", Toast.LENGTH_SHORT).show() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_ai_summary_btn"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isGeneratingSummary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B46C1),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sparkle Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isGeneratingSummary) "Generating AI Summary..." else "✨ Generate AI Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            // If toggle OFF and summary == null -> button hidden, app fully offline

            // ═══════════════════════════════════════════════
            // STATS CARD (dark #0F1A2E, accents #E8C46A)
            // ═══════════════════════════════════════════════
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutReportCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // "STATS" 22sp Bold #FFFFFF
                    Text(
                        text = "STATS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                        modifier = Modifier.testTag("stats_title")
                    )

                    // "[HOME] 2 - 1 [AWAY]" 26sp Bold #FFFFFF
                    Text(
                        text = "$homeTeamName $homeScore - $awayScore $awayTeamName",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        modifier = Modifier.testTag("stats_scoreline")
                    )

                    // If penalty shootout: "PENALTIES: 4 - 3" 20sp #E8C46A
                    if (hasPenaltyShootout) {
                        Text(
                            text = "PENALTIES: $homePenScore - $awayPenScore",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoutReportAccents,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("stats_penalties")
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Stats rows in required order:
                    // 1. BALL POSSESSION
                    StatsRow(
                        label = "BALL POSSESSION",
                        homeValue = "${stats.home.possessionPercent}%",
                        awayValue = "${stats.away.possessionPercent}%",
                        homeWeight = stats.home.possessionPercent.toFloat().coerceAtLeast(0.01f),
                        awayWeight = stats.away.possessionPercent.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_ball_possession"
                    )

                    // 2. TOTAL SHOTS
                    StatsRow(
                        label = "TOTAL SHOTS",
                        homeValue = "${stats.home.totalShots}",
                        awayValue = "${stats.away.totalShots}",
                        homeWeight = if (stats.home.totalShots == 0 && stats.away.totalShots == 0) 1f else stats.home.totalShots.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.totalShots == 0 && stats.away.totalShots == 0) 1f else stats.away.totalShots.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_total_shots"
                    )

                    // 3. SHOTS ON TARGET
                    StatsRow(
                        label = "SHOTS ON TARGET",
                        homeValue = "${stats.home.shotsOnTarget}",
                        awayValue = "${stats.away.shotsOnTarget}",
                        homeWeight = if (stats.home.shotsOnTarget == 0 && stats.away.shotsOnTarget == 0) 1f else stats.home.shotsOnTarget.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.shotsOnTarget == 0 && stats.away.shotsOnTarget == 0) 1f else stats.away.shotsOnTarget.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_shots_on_target"
                    )

                    // 4. SHOTS OFF TARGET
                    StatsRow(
                        label = "SHOTS OFF TARGET",
                        homeValue = "${stats.home.shotsOffTarget}",
                        awayValue = "${stats.away.shotsOffTarget}",
                        homeWeight = if (stats.home.shotsOffTarget == 0 && stats.away.shotsOffTarget == 0) 1f else stats.home.shotsOffTarget.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.shotsOffTarget == 0 && stats.away.shotsOffTarget == 0) 1f else stats.away.shotsOffTarget.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_shots_off_target"
                    )

                    // 5. CORNERS
                    StatsRow(
                        label = "CORNERS",
                        homeValue = "${stats.home.corners}",
                        awayValue = "${stats.away.corners}",
                        homeWeight = if (stats.home.corners == 0 && stats.away.corners == 0) 1f else stats.home.corners.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.corners == 0 && stats.away.corners == 0) 1f else stats.away.corners.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_corners"
                    )

                    // 6. OFFSIDES
                    StatsRow(
                        label = "OFFSIDES",
                        homeValue = "${stats.home.offsides}",
                        awayValue = "${stats.away.offsides}",
                        homeWeight = if (stats.home.offsides == 0 && stats.away.offsides == 0) 1f else stats.home.offsides.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.offsides == 0 && stats.away.offsides == 0) 1f else stats.away.offsides.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_offsides"
                    )

                    // 7. FOULS
                    StatsRow(
                        label = "FOULS",
                        homeValue = "${stats.home.fouls}",
                        awayValue = "${stats.away.fouls}",
                        homeWeight = if (stats.home.fouls == 0 && stats.away.fouls == 0) 1f else stats.home.fouls.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.fouls == 0 && stats.away.fouls == 0) 1f else stats.away.fouls.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_fouls"
                    )

                    // 8. YELLOW CARDS
                    StatsRow(
                        label = "YELLOW CARDS",
                        homeValue = "${stats.home.yellowCards}",
                        awayValue = "${stats.away.yellowCards}",
                        homeWeight = if (stats.home.yellowCards == 0 && stats.away.yellowCards == 0) 1f else stats.home.yellowCards.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.yellowCards == 0 && stats.away.yellowCards == 0) 1f else stats.away.yellowCards.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_yellow_cards"
                    )

                    // 9. RED CARDS
                    StatsRow(
                        label = "RED CARDS",
                        homeValue = "${stats.home.redCards}",
                        awayValue = "${stats.away.redCards}",
                        homeWeight = if (stats.home.redCards == 0 && stats.away.redCards == 0) 1f else stats.home.redCards.toFloat().coerceAtLeast(0.01f),
                        awayWeight = if (stats.home.redCards == 0 && stats.away.redCards == 0) 1f else stats.away.redCards.toFloat().coerceAtLeast(0.01f),
                        testTag = "stats_row_red_cards"
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Footer: "COLLECTED BY RWANDA PREMIER LEAGUE" 18sp Medium #E8C46A centered
                    Text(
                        text = "COLLECTED BY RWANDA PREMIER LEAGUE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ScoutReportAccents,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("stats_footer")
                    )
                }
            }

            // ═══════════════════════════════════════════════
            // BRIGHT SECTIONS BELOW
            // ═══════════════════════════════════════════════

            // Section 1: Goals
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goals_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header "Goals" 20sp Bold #0F1A2E with ⚽ icon 20dp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Goals Icon",
                        tint = Color(0xFF0F1A2E),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Goals",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1A2E)
                    )
                }

                if (goalEvents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                        border = BorderStroke(1.dp, ScoutDivider),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = "No goals scored in this match",
                            fontSize = 15.sp,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    goalEvents.forEach { goal ->
                        val assistName = extractAssist(goal.extraDataJson)
                        val teamSideLabel = if (goal.team.equals("HOME", ignoreCase = true)) "HOME" else "AWAY"
                        val rowText = buildString {
                            append("${goal.matchTimeDisplay} ⚽ $teamSideLabel — ${goal.playerName}")
                            if (!assistName.isNullOrBlank()) {
                                append(" (assist: $assistName)")
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_row_${goal.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(1.dp, ScoutDivider),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = rowText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F1A2E),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            // Section 2: Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cards_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header "Cards" 20sp Bold with card icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Square,
                        contentDescription = "Cards Icon",
                        tint = Color(0xFFB7791F),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cards",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1A2E)
                    )
                }

                if (cardEvents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                        border = BorderStroke(1.dp, ScoutDivider),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = "No cards issued in this match",
                            fontSize = 15.sp,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    cardEvents.forEach { card ->
                        val isRed = isRedCard(card)
                        val cardEmoji = if (isRed) "🟥" else "🟨"
                        val teamSideLabel = if (card.team.equals("HOME", ignoreCase = true)) "HOME" else "AWAY"
                        val rowText = "${card.matchTimeDisplay} $cardEmoji $teamSideLabel — ${card.playerName}"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_row_${card.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(1.dp, ScoutDivider),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = rowText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F1A2E),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            // Section 3: Timeline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timeline_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header "Timeline" 20sp Bold
                Text(
                    text = "Timeline",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E)
                )

                if (timelineEvents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                        border = BorderStroke(1.dp, ScoutDivider),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = "No events logged for this match",
                            fontSize = 15.sp,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    timelineEvents.forEach { event ->
                        val icon = getTimelineEventIcon(event.eventType)
                        val iconTint = getTimelineEventTint(event.eventType)
                        val teamName = if (event.team.equals("HOME", ignoreCase = true)) homeTeamName else awayTeamName

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timeline_row_${event.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(1.dp, ScoutDivider),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = event.matchTimeDisplay,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B7EB8),
                                    modifier = Modifier.widthIn(min = 54.dp)
                                )

                                Icon(
                                    imageVector = icon,
                                    contentDescription = event.eventType,
                                    tint = iconTint,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.playerName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F1A2E)
                                    )
                                    Text(
                                        text = "$teamName • ${event.eventType.replace("_", " ")}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF718096)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Possession Map
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("possession_map_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header "Possession Map" 20sp Bold
                Text(
                    text = "Possession Map",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E)
                )

                // Small pitch SVG/Canvas with dots for each ball tap
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ScoutPitchGrass)
                                .padding(10.dp)
                                .testTag("possession_pitch_canvas")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val strokeW = 2.dp.toPx()
                                val lineCol = ScoutPitchLines

                                // Outer Boundary
                                drawRect(
                                    color = lineCol,
                                    topLeft = Offset.Zero,
                                    size = Size(w, h),
                                    style = Stroke(width = strokeW)
                                )

                                // Halfway line
                                drawLine(
                                    color = lineCol,
                                    start = Offset(w / 2f, 0f),
                                    end = Offset(w / 2f, h),
                                    strokeWidth = strokeW
                                )

                                // Center Circle
                                drawCircle(
                                    color = lineCol,
                                    radius = h * 0.22f,
                                    center = Offset(w / 2f, h / 2f),
                                    style = Stroke(width = strokeW)
                                )

                                // Center spot
                                drawCircle(
                                    color = lineCol,
                                    radius = 3.dp.toPx(),
                                    center = Offset(w / 2f, h / 2f)
                                )

                                // Left penalty box
                                val boxW = w * 0.18f
                                val boxH = h * 0.56f
                                val boxTopY = (h - boxH) / 2f
                                drawRect(
                                    color = lineCol,
                                    topLeft = Offset(0f, boxTopY),
                                    size = Size(boxW, boxH),
                                    style = Stroke(width = strokeW)
                                )

                                // Right penalty box
                                drawRect(
                                    color = lineCol,
                                    topLeft = Offset(w - boxW, boxTopY),
                                    size = Size(boxW, boxH),
                                    style = Stroke(width = strokeW)
                                )

                                // Draw dots for ball taps
                                ballTaps.forEach { tap ->
                                    val tapX = tap.x * w
                                    val tapY = tap.y * h
                                    val isHomeSide = tap.side.equals("HOME", ignoreCase = true)
                                    val dotColor = if (isHomeSide) Color(0xFF1E88E5) else Color(0xFFE53935)

                                    drawCircle(
                                        color = dotColor,
                                        radius = 6.dp.toPx(),
                                        center = Offset(tapX, tapY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(tapX, tapY),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Legend: "Blue dot = Home side" / "Red dot = Away side"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("possession_legend"),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFF1E88E5), CircleShape)
                                        .border(1.dp, Color(0xFF0F1A2E), CircleShape)
                                )
                                Text(
                                    text = "Blue dot = Home side",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F1A2E)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFE53935), CircleShape)
                                        .border(1.dp, Color(0xFF0F1A2E), CircleShape)
                                )
                                Text(
                                    text = "Red dot = Away side",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F1A2E)
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════
            // EXPORT BUTTONS (bottom, stacked vertically)
            // ═══════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("export_buttons_section"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. "Export PDF" 64dp filled #0B7EB8, 20sp Medium + PDF icon 28dp
                Button(
                    onClick = {
                        if (currentMatch != null) {
                            exportPdfReport(context, currentMatch, stats, events)
                        } else {
                            Toast.makeText(context, "Match data not loaded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("export_pdf_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B7EB8),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Export PDF",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                // 2. "Export JSON" 64dp outlined #0B7EB8 + code icon
                OutlinedButton(
                    onClick = {
                        if (currentMatch != null) {
                            exportJsonReport(context, currentMatch, stats, events, ballTaps)
                        } else {
                            Toast.makeText(context, "Match data not loaded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("export_json_btn"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF0B7EB8)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0B7EB8)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Export JSON",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF0B7EB8)
                        )
                        Text(
                            text = "Export JSON",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0B7EB8)
                        )
                    }
                }

                // 3. "Export CSV" 64dp outlined #0B7EB8 + grid icon
                OutlinedButton(
                    onClick = {
                        if (currentMatch != null) {
                            exportCsvReport(context, currentMatch, events)
                        } else {
                            Toast.makeText(context, "Match data not loaded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("export_csv_btn"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF0B7EB8)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0B7EB8)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Export CSV",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF0B7EB8)
                        )
                        Text(
                            text = "Export CSV",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0B7EB8)
                        )
                    }
                }

                // 4. "Copy Summary Text" 64dp outlined #4A5568 + copy icon
                OutlinedButton(
                    onClick = {
                        if (currentMatch != null) {
                            copySummaryToClipboard(context, currentMatch, stats, events)
                        } else {
                            Toast.makeText(context, "Match data not loaded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("copy_summary_text_btn"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF4A5568)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A5568)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Summary Text",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF4A5568)
                        )
                        Text(
                            text = "Copy Summary Text",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A5568)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatsRow(
    label: String,
    homeValue: String,
    awayValue: String,
    homeWeight: Float,
    awayWeight: Float,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label 18sp SemiBold uppercase #FFFFFF centered
        Text(
            text = label.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Below: value on left 28sp Bold #FFFFFF | bar | value right 28sp Bold #FFFFFF
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home value left
            Text(
                text = homeValue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.widthIn(min = 60.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Bar 10dp tall, radius 5dp
            // HOME portion: #E8C46A (warm gold)
            // AWAY portion: #FFFFFF (white)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(homeWeight)
                            .fillMaxHeight()
                            .background(Color(0xFFE8C46A))
                    )
                    Box(
                        modifier = Modifier
                            .weight(awayWeight)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Away value right
            Text(
                text = awayValue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.widthIn(min = 60.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

private fun calculateMatchStats(
    events: List<EventEntity>,
    ballTaps: List<BallTapEntity>
): CalculatedMatchStats {
    // 1. Possession: ballTaps count by side
    val homeTaps = ballTaps.count { it.side.equals("HOME", ignoreCase = true) }
    val awayTaps = ballTaps.count { it.side.equals("AWAY", ignoreCase = true) }
    val totalTaps = homeTaps + awayTaps
    val homePossession = if (totalTaps > 0) {
        Math.round((homeTaps.toDouble() / totalTaps) * 100).toInt()
    } else {
        50
    }
    val awayPossession = if (totalTaps > 0) 100 - homePossession else 50

    fun calculateForTeam(teamSide: String, possession: Int): TeamStatsCalculations {
        // Total shots: events where eventType='SHOT' (or legacy SHOT_ON_TARGET / SHOT_OFF_TARGET)
        val teamEvents = events.filter { it.team.equals(teamSide, ignoreCase = true) }

        val totalShots = teamEvents.count {
            it.eventType == "SHOT" || it.eventType == "SHOT_ON_TARGET" || it.eventType == "SHOT_OFF_TARGET"
        }

        // Shots on target: SHOT + outcome IN ('On Target','Goal','Saved')
        val shotsOnTarget = teamEvents.count { event ->
            if (event.eventType == "SHOT_ON_TARGET") true
            else if (event.eventType == "SHOT") {
                val outcome = extractOutcome(event.extraDataJson)
                outcome.equals("On Target", ignoreCase = true) ||
                        outcome.equals("Goal", ignoreCase = true) ||
                        outcome.equals("Saved", ignoreCase = true)
            } else false
        }

        // Shots off target: SHOT + outcome IN ('Off Target','Wide','Bar','Post')
        val shotsOffTarget = teamEvents.count { event ->
            if (event.eventType == "SHOT_OFF_TARGET") true
            else if (event.eventType == "SHOT") {
                val outcome = extractOutcome(event.extraDataJson)
                outcome.equals("Off Target", ignoreCase = true) ||
                        outcome.equals("Wide", ignoreCase = true) ||
                        outcome.equals("Bar", ignoreCase = true) ||
                        outcome.equals("Post", ignoreCase = true)
            } else false
        }

        // Corners: eventType='CORNER'
        val corners = teamEvents.count { it.eventType == "CORNER" }

        // Offsides: eventType='OFFSIDE'
        val offsides = teamEvents.count { it.eventType == "OFFSIDE" }

        // Fouls: eventType IN ('FREE_KICK','DANGEROUS_FREE_KICK') or 'FOUL'
        val fouls = teamEvents.count {
            it.eventType in listOf("FREE_KICK", "DANGEROUS_FREE_KICK", "FOUL")
        }

        // Yellow: CARD + cardType='Yellow' or YELLOW_CARD
        val yellowCards = teamEvents.count { event ->
            if (event.eventType == "YELLOW_CARD") true
            else if (event.eventType == "CARD") {
                val cardType = extractCardType(event.extraDataJson)
                cardType.equals("Yellow", ignoreCase = true)
            } else false
        }

        // Red: CARD + cardType IN ('Red','Yellow-Red') or RED_CARD
        val redCards = teamEvents.count { event ->
            if (event.eventType == "RED_CARD") true
            else if (event.eventType == "CARD") {
                val cardType = extractCardType(event.extraDataJson)
                cardType.equals("Red", ignoreCase = true) || cardType.equals("Yellow-Red", ignoreCase = true)
            } else false
        }

        return TeamStatsCalculations(
            possessionPercent = possession,
            totalShots = totalShots,
            shotsOnTarget = shotsOnTarget,
            shotsOffTarget = shotsOffTarget,
            corners = corners,
            offsides = offsides,
            fouls = fouls,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }

    return CalculatedMatchStats(
        home = calculateForTeam("HOME", homePossession),
        away = calculateForTeam("AWAY", awayPossession)
    )
}

private fun extractOutcome(extraDataJson: String?): String {
    if (extraDataJson.isNullOrBlank()) return ""
    val regex = "\"outcome\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    return regex.find(extraDataJson)?.groupValues?.get(1)?.trim() ?: ""
}

private fun extractCardType(extraDataJson: String?): String {
    if (extraDataJson.isNullOrBlank()) return ""
    val regex = "\"cardType\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    return regex.find(extraDataJson)?.groupValues?.get(1)?.trim() ?: ""
}

private fun extractAssist(extraDataJson: String?): String? {
    if (extraDataJson.isNullOrBlank()) return null
    val regex = "\"assist\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val assist = regex.find(extraDataJson)?.groupValues?.get(1)?.trim()
    return if (assist.isNullOrBlank() || assist.equals("None", ignoreCase = true)) null else assist
}

private fun isCardEvent(event: EventEntity): Boolean {
    return event.eventType in listOf("CARD", "YELLOW_CARD", "RED_CARD")
}

private fun isRedCard(event: EventEntity): Boolean {
    if (event.eventType == "RED_CARD") return true
    if (event.eventType == "CARD") {
        val type = extractCardType(event.extraDataJson)
        return type.equals("Red", ignoreCase = true) || type.equals("Yellow-Red", ignoreCase = true)
    }
    return false
}

private fun getTimelineEventIcon(eventType: String): ImageVector {
    return when (eventType) {
        "GOAL" -> Icons.Default.SportsSoccer
        "CARD", "YELLOW_CARD", "RED_CARD" -> Icons.Default.Square
        "SUBSTITUTION" -> Icons.Default.SwapHoriz
        "INJURY", "INJURY_RETURN" -> Icons.Default.Warning
        "CORNER" -> Icons.Default.Flag
        "STATUS_CHANGE" -> Icons.Default.AccessTime
        "SNAPSHOT" -> Icons.Default.PhotoCamera
        "ATTENDANCE" -> Icons.Default.People
        "INTERRUPTION", "INTERRUPTION_END" -> Icons.Default.PauseCircle
        "REQUEST_ASSISTANCE" -> Icons.Default.Help
        else -> Icons.Default.SportsSoccer
    }
}

private fun getTimelineEventTint(eventType: String): Color {
    return when (eventType) {
        "GOAL" -> Color(0xFF1B7F3A)
        "CARD", "YELLOW_CARD" -> Color(0xFFB7791F)
        "RED_CARD" -> Color(0xFFC53030)
        "SUBSTITUTION" -> Color(0xFF0B7EB8)
        "INJURY" -> Color(0xFFC53030)
        "CORNER" -> Color(0xFF6B46C1)
        else -> Color(0xFF0F1A2E)
    }
}

// ═══════════════════════════════════════════════
// EXPORT HANDLERS
// ═══════════════════════════════════════════════

private fun exportPdfReport(
    context: Context,
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Header Title
        paint.color = android.graphics.Color.parseColor("#0F1A2E")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("RWANDA PREMIER LEAGUE - MATCH REPORT", 40f, 50f, paint)

        // Subtitle
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.parseColor("#4A5568")
        canvas.drawText("${match.competition} • ${match.venue}", 40f, 75f, paint)

        // Scoreline
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0F1A2E")
        canvas.drawText("${match.homeTeam}  ${match.homeScore} - ${match.awayScore}  ${match.awayTeam}", 40f, 115f, paint)

        if (match.homePenScore > 0 || match.awayPenScore > 0) {
            paint.textSize = 14f
            paint.color = android.graphics.Color.parseColor("#B7791F")
            canvas.drawText("PENALTIES: ${match.homePenScore} - ${match.awayPenScore}", 40f, 138f, paint)
        }

        // Stats Section
        var y = 170f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0B7EB8")
        canvas.drawText("MATCH STATISTICS", 40f, y, paint)
        y += 24f

        paint.isFakeBoldText = false
        paint.textSize = 11f
        paint.color = android.graphics.Color.BLACK

        val statItems = listOf(
            "BALL POSSESSION" to "${stats.home.possessionPercent}%  |  ${stats.away.possessionPercent}%",
            "TOTAL SHOTS" to "${stats.home.totalShots}  |  ${stats.away.totalShots}",
            "SHOTS ON TARGET" to "${stats.home.shotsOnTarget}  |  ${stats.away.shotsOnTarget}",
            "SHOTS OFF TARGET" to "${stats.home.shotsOffTarget}  |  ${stats.away.shotsOffTarget}",
            "CORNERS" to "${stats.home.corners}  |  ${stats.away.corners}",
            "OFFSIDES" to "${stats.home.offsides}  |  ${stats.away.offsides}",
            "FOULS" to "${stats.home.fouls}  |  ${stats.away.fouls}",
            "YELLOW CARDS" to "${stats.home.yellowCards}  |  ${stats.away.yellowCards}",
            "RED CARDS" to "${stats.home.redCards}  |  ${stats.away.redCards}"
        )

        for ((label, value) in statItems) {
            canvas.drawText(label, 40f, y, paint)
            canvas.drawText(value, 280f, y, paint)
            y += 20f
        }

        // Goals & Cards Section
        y += 20f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0B7EB8")
        canvas.drawText("GOALS & CARDS", 40f, y, paint)
        y += 24f

        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.BLACK
        val keyEvents = events.filter { it.eventType in listOf("GOAL", "CARD", "YELLOW_CARD", "RED_CARD") }.take(15)
        if (keyEvents.isEmpty()) {
            canvas.drawText("No goals or cards logged", 40f, y, paint)
            y += 20f
        } else {
            for (ev in keyEvents) {
                canvas.drawText("${ev.matchTimeDisplay} [${ev.team}] ${ev.eventType}: ${ev.playerName}", 40f, y, paint)
                y += 18f
            }
        }

        // Footer
        y += 30f
        paint.color = android.graphics.Color.parseColor("#718096")
        paint.textSize = 10f
        canvas.drawText("COLLECTED BY RWANDA PREMIER LEAGUE", 40f, y, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "MatchReport_${match.id}.pdf")
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        stream.close()
        pdfDocument.close()

        Toast.makeText(context, "PDF Report exported (${file.name})", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Exported PDF to app storage", Toast.LENGTH_SHORT).show()
    }
}

private fun exportJsonReport(
    context: Context,
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>,
    ballTaps: List<BallTapEntity>
) {
    try {
        val json = buildString {
            append("{\n")
            append("  \"matchId\": ${match.id},\n")
            append("  \"homeTeam\": \"${match.homeTeam}\",\n")
            append("  \"awayTeam\": \"${match.awayTeam}\",\n")
            append("  \"homeScore\": ${match.homeScore},\n")
            append("  \"awayScore\": ${match.awayScore},\n")
            append("  \"homePenScore\": ${match.homePenScore},\n")
            append("  \"awayPenScore\": ${match.awayPenScore},\n")
            append("  \"competition\": \"${match.competition}\",\n")
            append("  \"venue\": \"${match.venue}\",\n")
            append("  \"stats\": {\n")
            append("    \"ballPossession\": {\"home\": ${stats.home.possessionPercent}, \"away\": ${stats.away.possessionPercent}},\n")
            append("    \"totalShots\": {\"home\": ${stats.home.totalShots}, \"away\": ${stats.away.totalShots}},\n")
            append("    \"shotsOnTarget\": {\"home\": ${stats.home.shotsOnTarget}, \"away\": ${stats.away.shotsOnTarget}},\n")
            append("    \"shotsOffTarget\": {\"home\": ${stats.home.shotsOffTarget}, \"away\": ${stats.away.shotsOffTarget}},\n")
            append("    \"corners\": {\"home\": ${stats.home.corners}, \"away\": ${stats.away.corners}},\n")
            append("    \"offsides\": {\"home\": ${stats.home.offsides}, \"away\": ${stats.away.offsides}},\n")
            append("    \"fouls\": {\"home\": ${stats.home.fouls}, \"away\": ${stats.away.fouls}},\n")
            append("    \"yellowCards\": {\"home\": ${stats.home.yellowCards}, \"away\": ${stats.away.yellowCards}},\n")
            append("    \"redCards\": {\"home\": ${stats.home.redCards}, \"away\": ${stats.away.redCards}}\n")
            append("  },\n")
            append("  \"events\": [\n")
            events.forEachIndexed { idx, ev ->
                append("    {\"id\": ${ev.id}, \"time\": \"${ev.matchTimeDisplay}\", \"team\": \"${ev.team}\", \"type\": \"${ev.eventType}\", \"player\": \"${ev.playerName.replace("\"", "\\\"")}\"}${if (idx < events.size - 1) "," else ""}\n")
            }
            append("  ],\n")
            append("  \"ballTapsCount\": ${ballTaps.size}\n")
            append("}")
        }
        val file = File(context.cacheDir, "MatchReport_${match.id}.json")
        file.writeText(json)
        Toast.makeText(context, "JSON exported (${file.name})", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Exported JSON to app storage", Toast.LENGTH_SHORT).show()
    }
}

private fun exportCsvReport(
    context: Context,
    match: MatchEntity,
    events: List<EventEntity>
) {
    try {
        val csv = buildString {
            append("MatchId,Time,Team,EventType,Player,ExtraData\n")
            events.forEach { ev ->
                val safePlayer = ev.playerName.replace("\"", "\"\"")
                val safeExtra = (ev.extraDataJson ?: "").replace("\"", "\"\"")
                append("${match.id},\"${ev.matchTimeDisplay}\",\"${ev.team}\",\"${ev.eventType}\",\"$safePlayer\",\"$safeExtra\"\n")
            }
        }
        val file = File(context.cacheDir, "MatchReport_${match.id}.csv")
        file.writeText(csv)
        Toast.makeText(context, "CSV exported (${file.name})", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Exported CSV to app storage", Toast.LENGTH_SHORT).show()
    }
}

private fun copySummaryToClipboard(
    context: Context,
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>
) {
    val summary = buildString {
        append("STATS\n")
        append("${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}\n")
        if (match.homePenScore > 0 || match.awayPenScore > 0) {
            append("PENALTIES: ${match.homePenScore} - ${match.awayPenScore}\n")
        }
        append("${match.competition} • ${match.venue}\n\n")

        append("BALL POSSESSION: ${stats.home.possessionPercent}% | ${stats.away.possessionPercent}%\n")
        append("TOTAL SHOTS: ${stats.home.totalShots} | ${stats.away.totalShots}\n")
        append("SHOTS ON TARGET: ${stats.home.shotsOnTarget} | ${stats.away.shotsOnTarget}\n")
        append("SHOTS OFF TARGET: ${stats.home.shotsOffTarget} | ${stats.away.shotsOffTarget}\n")
        append("CORNERS: ${stats.home.corners} | ${stats.away.corners}\n")
        append("OFFSIDES: ${stats.home.offsides} | ${stats.away.offsides}\n")
        append("FOULS: ${stats.home.fouls} | ${stats.away.fouls}\n")
        append("YELLOW CARDS: ${stats.home.yellowCards} | ${stats.away.yellowCards}\n")
        append("RED CARDS: ${stats.home.redCards} | ${stats.away.redCards}\n\n")

        val goals = events.filter { it.eventType == "GOAL" }
        append("Goals:\n")
        if (goals.isEmpty()) {
            append("None\n")
        } else {
            goals.forEach { g ->
                val assist = extractAssist(g.extraDataJson)
                val teamSideLabel = if (g.team.equals("HOME", ignoreCase = true)) "HOME" else "AWAY"
                append("${g.matchTimeDisplay} ⚽ $teamSideLabel — ${g.playerName}${if (assist != null) " (assist: $assist)" else ""}\n")
            }
        }

        val cards = events.filter { isCardEvent(it) }
        append("\nCards:\n")
        if (cards.isEmpty()) {
            append("None\n")
        } else {
            cards.forEach { c ->
                val emoji = if (isRedCard(c)) "🟥" else "🟨"
                val teamSideLabel = if (c.team.equals("HOME", ignoreCase = true)) "HOME" else "AWAY"
                append("${c.matchTimeDisplay} $emoji $teamSideLabel — ${c.playerName}\n")
            }
        }

        append("\nCOLLECTED BY RWANDA PREMIER LEAGUE\n")
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Match Report Summary", summary))
    Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
}
