package com.example.ui.screens

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.MatchEntity
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.ScoutStatusPill
import com.example.ui.components.TacticalJerseyBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatchFilter
import com.example.ui.viewmodel.ScoutViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    viewModel: ScoutViewModel,
    onNavigateToNewMatch: () -> Unit,
    onNavigateToMatchDetail: (Long) -> Unit
) {
    val allMatches by viewModel.allMatches.collectAsState()
    val filteredMatches by viewModel.filteredMatches.collectAsState()
    val currentFilter by viewModel.selectedFilter.collectAsState()
    val storageSize by viewModel.storageSizeMb.collectAsState()

    var selectedMatchForMenu by remember { mutableStateOf<MatchEntity?>(null) }
    var matchToDelete by remember { mutableStateOf<MatchEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Screen Header
            Text(
                text = "My Matches",
                style = ScoutTypographyTitleXL,
                color = ScoutTextPrimary,
                modifier = Modifier.testTag("matches_title")
            )
            Spacer(modifier = Modifier.height(4.dp))
            // STORAGE INDICATOR: small text 16sp #718096: "N matches • X.X MB used"
            Text(
                text = "${allMatches.size} matches • $storageSize used",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF718096),
                modifier = Modifier.testTag("matches_subtitle")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Filter Chips (56dp height, 18sp Medium)
            FilterChipsRow(
                currentFilter = currentFilter,
                onSelectFilter = { viewModel.setFilter(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Matches Content or Empty State
            if (filteredMatches.isEmpty()) {
                EmptyMatchesView(
                    isFiltered = allMatches.isNotEmpty(),
                    onResetFilter = { viewModel.setFilter(MatchFilter.ALL) }
                )
            } else {
                MatchesListView(
                    matches = filteredMatches,
                    onMatchClick = { onNavigateToMatchDetail(it.id) },
                    onMatchLongClick = { selectedMatchForMenu = it }
                )
            }
        }

        // Floating Action Button (56dp #0284C7, "+" icon 24dp)
        FloatingActionButton(
            onClick = onNavigateToNewMatch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .testTag("create_match_fab")
                .semantics { contentDescription = "Create New Match" },
            shape = CircleShape,
            containerColor = ScoutPrimaryAction,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }

    // Long press ModalBottomSheet
    if (selectedMatchForMenu != null) {
        val match = selectedMatchForMenu!!
        ModalBottomSheet(
            onDismissRequest = { selectedMatchForMenu = null },
            sheetState = sheetState,
            containerColor = ScoutSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${match.homeTeam} vs ${match.awayTeam}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutTextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 1. "Open" #0284C7 + folder icon, 48dp row, 14.5sp Bold
                Surface(
                    onClick = {
                        val id = match.id
                        selectedMatchForMenu = null
                        onNavigateToMatchDetail(id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bottom_sheet_open_btn"),
                    shape = RoundedCornerShape(10.dp),
                    color = ScoutPrimaryAction.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open",
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Open Match",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoutPrimaryAction
                        )
                    }
                }

                // 2. "Delete Match" #DC2626 + trash icon, 48dp row, 14.5sp Bold
                Surface(
                    onClick = {
                        matchToDelete = match
                        selectedMatchForMenu = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bottom_sheet_delete_btn"),
                    shape = RoundedCornerShape(10.dp),
                    color = ScoutErrorDanger.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Match",
                            tint = ScoutErrorDanger,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Delete Match",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoutErrorDanger
                        )
                    }
                }

                // 3. "Cancel" #4A5568 + X icon, 72dp row, 20sp Medium
                Surface(
                    onClick = { selectedMatchForMenu = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("bottom_sheet_cancel_btn"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF4A5568).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color(0xFF4A5568),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Cancel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A5568)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Match → confirm dialog 24sp Bold:
    // "Delete this match? All events, lineups, and reports will be removed."
    // [CANCEL] [DELETE #C53030]
    // On confirm: Room cascade delete → toast "Match deleted"
    if (matchToDelete != null) {
        val targetMatch = matchToDelete!!
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete this match?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E),
                    modifier = Modifier.testTag("delete_match_dialog_title")
                )
            },
            text = {
                Text(
                    text = "Delete this match? All events, lineups, and reports will be removed.",
                    fontSize = 18.sp,
                    color = Color(0xFF4A5568),
                    lineHeight = 26.sp,
                    modifier = Modifier.testTag("delete_match_dialog_body")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetMatch.id
                        matchToDelete = null
                        viewModel.deleteMatch(id) {
                            Toast.makeText(context, "Match deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .testTag("dialog_confirm_delete_match_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC53030),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "DELETE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { matchToDelete = null },
                    modifier = Modifier
                        .height(64.dp)
                        .testTag("dialog_cancel_delete_match_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, Color(0xFF4A5568))
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A5568)
                    )
                }
            }
        )
    }
}

@Composable
private fun FilterChipsRow(
    currentFilter: MatchFilter,
    onSelectFilter: (MatchFilter) -> Unit
) {
    val filters = listOf(
        Pair(MatchFilter.ALL, "All"),
        Pair(MatchFilter.SCHEDULED, "Scheduled"),
        Pair(MatchFilter.LIVE, "Live"),
        Pair(MatchFilter.FINISHED, "Finished")
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (filter, title) ->
            val isSelected = currentFilter == filter
            Surface(
                onClick = { onSelectFilter(filter) },
                modifier = Modifier
                    .height(36.dp)
                    .testTag("filter_${filter.name.lowercase()}"),
                shape = CircleShape,
                color = if (isSelected) ScoutPrimaryAction else ScoutSurface,
                border = if (isSelected) null else BorderStroke(1.dp, ScoutDivider),
                shadowElevation = if (isSelected) 1.dp else 0.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = ScoutTypographyLabelM.copy(
                            color = if (isSelected) Color.White else ScoutTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchesListView(
    matches: List<MatchEntity>,
    onMatchClick: (MatchEntity) -> Unit,
    onMatchLongClick: (MatchEntity) -> Unit
) {
    // Categorize into Today, Upcoming, and Completed sections
    val now = Calendar.getInstance()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todayEnd = todayStart + (24 * 60 * 60 * 1000L)

    val todayMatches = matches.filter {
        it.status.equals("LIVE", ignoreCase = true) ||
                (it.matchDateMillis in todayStart until todayEnd)
    }

    val upcomingMatches = matches.filter {
        !todayMatches.contains(it) &&
                it.status.equals("SCHEDULED", ignoreCase = true) &&
                it.matchDateMillis >= todayEnd
    }

    val completedMatches = matches.filter {
        !todayMatches.contains(it) &&
                it.status.equals("FINISHED", ignoreCase = true)
    }

    val otherMatches = matches.filter {
        !todayMatches.contains(it) && !upcomingMatches.contains(it) && !completedMatches.contains(it)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("matches_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        if (todayMatches.isNotEmpty()) {
            item {
                Text(
                    text = "Today",
                    style = ScoutTypographyTitleL,
                    color = ScoutTextPrimary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(todayMatches, key = { it.id }) { match ->
                MatchCardItem(
                    match = match,
                    onClick = { onMatchClick(match) },
                    onLongClick = { onMatchLongClick(match) }
                )
            }
        }

        if (upcomingMatches.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming",
                    style = ScoutTypographyTitleL,
                    color = ScoutTextPrimary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(upcomingMatches, key = { it.id }) { match ->
                MatchCardItem(
                    match = match,
                    onClick = { onMatchClick(match) },
                    onLongClick = { onMatchLongClick(match) }
                )
            }
        }

        if (completedMatches.isNotEmpty()) {
            item {
                Text(
                    text = "Completed",
                    style = ScoutTypographyTitleL,
                    color = ScoutTextPrimary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(completedMatches, key = { it.id }) { match ->
                MatchCardItem(
                    match = match,
                    onClick = { onMatchClick(match) },
                    onLongClick = { onMatchLongClick(match) }
                )
            }
        }

        if (otherMatches.isNotEmpty()) {
            item {
                Text(
                    text = "All Scheduled Fixtures",
                    style = ScoutTypographyTitleL,
                    color = ScoutTextPrimary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(otherMatches, key = { it.id }) { match ->
                MatchCardItem(
                    match = match,
                    onClick = { onMatchClick(match) },
                    onLongClick = { onMatchLongClick(match) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchCardItem(
    match: MatchEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateStr = DateFormat.format("EEEE, d MMM yyyy", Date(match.matchDateMillis)).toString()
    val timeStr = DateFormat.format("HH:mm", Date(match.kickoffTimeMillis)).toString()

    val homeKitColor = parseTeamColor(match.homeColor, isHome = true)
    val awayKitColor = parseTeamColor(match.awayColor, isHome = false)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
        border = BorderStroke(1.dp, ScoutDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kickoff date & time
                Text(
                    text = "$dateStr • $timeStr",
                    style = ScoutTypographyBodyM.copy(
                        fontWeight = FontWeight.Medium,
                        color = ScoutTextSecondary,
                        fontSize = 12.5.sp
                    )
                )
                ScoutStatusPill(status = match.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams with visual realistic jerseys & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Home Team Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalJerseyBadge(color = homeKitColor, modifier = Modifier.size(22.dp))
                        Text(
                            text = match.homeTeam,
                            style = ScoutTypographyTitleM.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = ScoutTextPrimary,
                            maxLines = 1
                        )
                    }

                    // Away Team Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalJerseyBadge(color = awayKitColor, modifier = Modifier.size(22.dp))
                        Text(
                            text = match.awayTeam,
                            style = ScoutTypographyTitleM.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = ScoutTextPrimary,
                            maxLines = 1
                        )
                    }
                }

                // Score pill for live or finished matches
                if (match.status.equals("LIVE", ignoreCase = true) ||
                    match.status.equals("FINISHED", ignoreCase = true)) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (match.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive.copy(alpha = 0.12f) else ScoutSurfaceVariant,
                        border = BorderStroke(1.dp, if (match.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive.copy(alpha = 0.4f) else ScoutDivider)
                    ) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = ScoutTypographyTitleM.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (match.status.equals("LIVE", ignoreCase = true)) ScoutSuccessLive else ScoutTextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Competition • Venue
            Text(
                text = "${match.competition} • ${match.venue}",
                style = ScoutTypographyCaption.copy(
                    color = ScoutTextHint,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyMatchesView(
    isFiltered: Boolean,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SportsSoccer,
            contentDescription = null,
            tint = ScoutTextHint,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFiltered) "No matches match this filter" else "No matches yet",
            style = ScoutTypographyTitleM,
            color = ScoutTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isFiltered) "Tap below to view all matches" else "Tap + to create your first match",
            style = ScoutTypographyBodyM,
            color = ScoutTextSecondary
        )
        if (isFiltered) {
            Spacer(modifier = Modifier.height(20.dp))
            ScoutButton(
                text = "Show All Matches",
                style = ScoutButtonStyle.SECONDARY,
                onClick = onResetFilter,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}
