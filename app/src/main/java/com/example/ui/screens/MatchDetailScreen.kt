package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.ScoutCard
import com.example.ui.components.ScoutStatusPill
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel
import java.util.Date

@Composable
fun MatchDetailScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLineups: (Long) -> Unit,
    onNavigateToPreMatch: (Long) -> Unit,
    onNavigateToKickoff: (Long) -> Unit,
    onNavigateToLiveMatch: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit
) {
    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()
    val lineups by viewModel.activeLineups.collectAsState()
    val events by viewModel.activeEvents.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        ScoutTopBar(
            title = "Match Detail",
            onBackClick = onNavigateBack
        )

        if (match == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading match information...",
                    style = ScoutTypographyBodyL,
                    color = ScoutTextSecondary
                )
            }
        } else {
            val m = match!!
            val dateFormatted = DateFormat.format("EEEE, d MMMM yyyy", Date(m.matchDateMillis)).toString()
            val timeFormatted = DateFormat.format("HH:mm", Date(m.kickoffTimeMillis)).toString()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Match Header with Team Jerseys and Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(1.dp, ScoutDivider),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$dateFormatted • $timeFormatted CAT",
                                style = ScoutTypographyCaption.copy(
                                    color = ScoutTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            ScoutStatusPill(status = m.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val homeKitColor = parseTeamColor(m.homeColor, isHome = true)
                        val awayKitColor = parseTeamColor(m.awayColor, isHome = false)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home Team
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                com.example.ui.components.TacticalJerseyBadge(color = homeKitColor, modifier = Modifier.size(28.dp))
                                Column {
                                    Text(
                                        text = m.homeTeam,
                                        style = ScoutTypographyTitleL.copy(fontWeight = FontWeight.Bold),
                                        color = ScoutTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "HOME",
                                        style = ScoutTypographyCaption.copy(
                                            color = ScoutTextHint,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            // Score or VS Pill
                            if (m.status.equals("LIVE", ignoreCase = true) || m.status.equals("FINISHED", ignoreCase = true)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (m.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive.copy(alpha = 0.12f) else ScoutSurfaceVariant,
                                    border = BorderStroke(1.dp, if (m.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive.copy(alpha = 0.4f) else ScoutDivider)
                                ) {
                                    Text(
                                        text = "${m.homeScore} : ${m.awayScore}",
                                        style = ScoutTypographyDisplay.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (m.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive else ScoutTextPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = ScoutSurfaceVariant,
                                    border = BorderStroke(1.dp, ScoutDivider)
                                ) {
                                    Text(
                                        text = "VS",
                                        style = ScoutTypographyCaption.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ScoutTextSecondary
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Away Team
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = m.awayTeam,
                                        style = ScoutTypographyTitleL.copy(fontWeight = FontWeight.Bold),
                                        color = ScoutTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "AWAY",
                                        style = ScoutTypographyCaption.copy(
                                            color = ScoutTextHint,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                                com.example.ui.components.TacticalJerseyBadge(color = awayKitColor, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }

                // Info Card (competition, venue, date, time 20sp)
                ScoutCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Fixture Details",
                            style = ScoutTypographyTitleM,
                            color = ScoutTextPrimary
                        )

                        InfoRow(label = "Competition", value = m.competition)
                        InfoRow(label = "Venue", value = m.venue)
                        InfoRow(label = "Date", value = dateFormatted)
                        InfoRow(label = "Kickoff", value = "$timeFormatted CAT")

                        if (m.weather != null) {
                            InfoRow(label = "Pitch & Weather", value = "${m.weather} • ${m.pitchCondition ?: "Natural grass"}")
                        }

                        val homeCount = lineups.filter { it.team.equals("HOME", ignoreCase = true) }.size
                        val awayCount = lineups.filter { it.team.equals("AWAY", ignoreCase = true) }.size
                        InfoRow(label = "Registered Players", value = "${m.homeTeam} ($homeCount) • ${m.awayTeam} ($awayCount)")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons based on status
                when (m.status.uppercase()) {
                    "SCHEDULED" -> {
                        // "Edit Lineups" (secondary) + "Start Scouting" (primary)
                        ScoutButton(
                            text = "Edit Lineups",
                            icon = Icons.Default.Edit,
                            style = ScoutButtonStyle.SECONDARY,
                            onClick = { onNavigateToLineups(m.id) },
                            testTag = "detail_edit_lineups_btn"
                        )

                        ScoutButton(
                            text = "Start Scouting",
                            icon = Icons.Default.PlayArrow,
                            style = ScoutButtonStyle.PRIMARY,
                            onClick = { onNavigateToPreMatch(m.id) },
                            testTag = "detail_start_scouting_btn"
                        )
                    }

                    "LIVE" -> {
                        // "Open Live Match" (success)
                        ScoutButton(
                            text = "Open Live Match",
                            icon = Icons.Default.PlayArrow,
                            style = ScoutButtonStyle.SUCCESS,
                            onClick = { onNavigateToLiveMatch(m.id) },
                            testTag = "detail_open_live_btn"
                        )

                        ScoutButton(
                            text = "View Interim Report",
                            icon = Icons.Default.Assessment,
                            style = ScoutButtonStyle.SECONDARY,
                            onClick = { onNavigateToReport(m.id) },
                            testTag = "detail_view_report_btn"
                        )
                    }

                    "FINISHED" -> {
                        // "View Report" (primary)
                        ScoutButton(
                            text = "View Report",
                            icon = Icons.Default.Assessment,
                            style = ScoutButtonStyle.PRIMARY,
                            onClick = { onNavigateToReport(m.id) },
                            testTag = "detail_view_report_btn"
                        )

                        ScoutButton(
                            text = "Review Events (${events.size} logged)",
                            icon = Icons.Default.SportsSoccer,
                            style = ScoutButtonStyle.SECONDARY,
                            onClick = { onNavigateToLiveMatch(m.id) },
                            testTag = "detail_review_events_btn"
                        )
                    }
                }

                // Delete Match (64dp outlined #C53030)
                ScoutButton(
                    text = "Delete Match",
                    icon = Icons.Default.Delete,
                    style = ScoutButtonStyle.DANGER,
                    onClick = { showDeleteConfirm = true },
                    testTag = "detail_delete_match_btn"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete this match?",
                    style = ScoutTypographyTitleM,
                    color = ScoutTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all lineups, live events, ball taps, and reports recorded for this fixture.",
                    style = ScoutTypographyBodyM,
                    color = ScoutTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteMatch(matchId)
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = "DELETE",
                        style = ScoutTypographyLabelL.copy(color = ScoutErrorDanger, fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = "CANCEL",
                        style = ScoutTypographyLabelL.copy(color = ScoutTextSecondary)
                    )
                }
            },
            containerColor = ScoutSurface
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = ScoutTypographyBodyL,
            color = ScoutTextPrimary
        )
    }
}
