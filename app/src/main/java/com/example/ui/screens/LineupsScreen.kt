package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LineupEntity
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel

@Composable
fun LineupsScreen(
    matchId: Long,
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.activeMatch.collectAsState()
    val allLineups by viewModel.activeLineups.collectAsState()

    var showAddDialogForTeam by remember { mutableStateOf<String?>(null) }

    val homeTeamName = match?.homeTeam ?: "Home Team"
    val awayTeamName = match?.awayTeam ?: "Away Team"

    val homeColor = parseTeamColor(match?.homeColor, isHome = true)
    val awayColor = parseTeamColor(match?.awayColor, isHome = false)

    val homeLineups = allLineups.filter { it.team.equals("HOME", ignoreCase = true) }
    val awayLineups = allLineups.filter { it.team.equals("AWAY", ignoreCase = true) }

    val homeStarters = homeLineups.filter { it.isStarter }
    val homeSubs = homeLineups.filter { !it.isStarter }

    val awayStarters = awayLineups.filter { it.isStarter }
    val awaySubs = awayLineups.filter { !it.isStarter }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        ScoutTopBar(
            title = "Match Lineups",
            onBackClick = onNavigateBack
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            val isTwoColumn = maxWidth >= 600.dp

            if (isTwoColumn) {
                // Two Column Layout (>= 600dp)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TeamLineupSection(
                            teamSide = "HOME",
                            teamName = homeTeamName,
                            teamColor = homeColor,
                            players = homeLineups,
                            startersCount = homeStarters.size,
                            subsCount = homeSubs.size,
                            onAddPlayer = { showAddDialogForTeam = "HOME" },
                            onDeletePlayer = { viewModel.deletePlayer(it.id) },
                            onToggleCaptain = { viewModel.toggleCaptain(it) },
                            onAutoFill = { viewModel.autoFillLineup(matchId, "HOME", homeTeamName) }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TeamLineupSection(
                            teamSide = "AWAY",
                            teamName = awayTeamName,
                            teamColor = awayColor,
                            players = awayLineups,
                            startersCount = awayStarters.size,
                            subsCount = awaySubs.size,
                            onAddPlayer = { showAddDialogForTeam = "AWAY" },
                            onDeletePlayer = { viewModel.deletePlayer(it.id) },
                            onToggleCaptain = { viewModel.toggleCaptain(it) },
                            onAutoFill = { viewModel.autoFillLineup(matchId, "AWAY", awayTeamName) }
                        )
                    }
                }
            } else {
                // Single Column Layout (< 600dp): HOME section stacked above AWAY section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Home Section
                    TeamLineupSection(
                        teamSide = "HOME",
                        teamName = homeTeamName,
                        teamColor = homeColor,
                        players = homeLineups,
                        startersCount = homeStarters.size,
                        subsCount = homeSubs.size,
                        onAddPlayer = { showAddDialogForTeam = "HOME" },
                        onDeletePlayer = { viewModel.deletePlayer(it.id) },
                        onToggleCaptain = { viewModel.toggleCaptain(it) },
                        onAutoFill = { viewModel.autoFillLineup(matchId, "HOME", homeTeamName) }
                    )

                    // Away Section
                    TeamLineupSection(
                        teamSide = "AWAY",
                        teamName = awayTeamName,
                        teamColor = awayColor,
                        players = awayLineups,
                        startersCount = awayStarters.size,
                        subsCount = awaySubs.size,
                        onAddPlayer = { showAddDialogForTeam = "AWAY" },
                        onDeletePlayer = { viewModel.deletePlayer(it.id) },
                        onToggleCaptain = { viewModel.toggleCaptain(it) },
                        onAutoFill = { viewModel.autoFillLineup(matchId, "AWAY", awayTeamName) }
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // Add Player Dialog
    if (showAddDialogForTeam != null) {
        val targetTeamSide = showAddDialogForTeam!!
        val targetTeamName = if (targetTeamSide == "HOME") homeTeamName else awayTeamName
        val existingPlayers = allLineups.filter { it.team.equals(targetTeamSide, ignoreCase = true) }

        AddPlayerDialog(
            teamSide = targetTeamSide,
            teamName = targetTeamName,
            existingPlayers = existingPlayers,
            onDismiss = { showAddDialogForTeam = null },
            onSave = { name, jersey, pos, isCapt, isStart ->
                viewModel.addPlayer(
                    matchId = matchId,
                    teamSide = targetTeamSide,
                    name = name,
                    jersey = jersey,
                    position = pos,
                    isCaptain = isCapt,
                    isStarter = isStart
                )
                showAddDialogForTeam = null
            }
        )
    }
}

@Composable
private fun TeamLineupSection(
    teamSide: String,
    teamName: String,
    teamColor: Color,
    players: List<LineupEntity>,
    startersCount: Int,
    subsCount: Int,
    onAddPlayer: () -> Unit,
    onDeletePlayer: (LineupEntity) -> Unit,
    onToggleCaptain: (LineupEntity) -> Unit,
    onAutoFill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lineup_section_${teamSide.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header: Team Name 28sp Bold + count "11 starters • 7 subs" 16sp #718096
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teamName,
                    style = ScoutTypographyTitleL,
                    color = ScoutTextPrimary
                )
                Text(
                    text = "$startersCount starters • $subsCount subs",
                    style = ScoutTypographyCaption.copy(fontSize = 13.5.sp),
                    color = ScoutTextHint
                )
            }

            if (players.isEmpty()) {
                OutlinedButton(
                    onClick = onAutoFill,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ScoutPrimaryAction)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = ScoutPrimaryAction,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Auto-Fill",
                        style = ScoutTypographyLabelM.copy(color = ScoutPrimaryAction, fontSize = 16.sp)
                    )
                }
            }
        }

        // Player Cards (20dp padding, white, 16dp radius, elevation 2dp)
        if (players.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No players registered. Tap '+ Add Player' below or 'Auto-Fill' above.",
                        style = ScoutTypographyBodyM,
                        color = ScoutTextSecondary
                    )
                }
            }
        } else {
            players.forEach { player ->
                PlayerCardItem(
                    player = player,
                    teamColor = teamColor,
                    onDelete = { onDeletePlayer(player) },
                    onToggleCaptain = { onToggleCaptain(player) }
                )
            }
        }

        // "+ Add Player" Button (48dp outlined #0284C7, 14.5sp Bold, "+" icon 20dp)
        OutlinedButton(
            onClick = onAddPlayer,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_player_btn_${teamSide.lowercase()}"),
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
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = ScoutPrimaryAction,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Player",
                    style = ScoutTypographyLabelL.copy(
                        color = ScoutPrimaryAction,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Bottom of section: "Starters: X/11" and "Subs: Y/7"
        val startersComplete = startersCount == 11
        val subsComplete = subsCount >= 7

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Starters: $startersCount/11",
                style = ScoutTypographyLabelM.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (startersComplete) ScoutSuccessLive else ScoutErrorDanger,
                    fontSize = 13.sp
                )
            )

            Text(
                text = "Subs: $subsCount/7",
                style = ScoutTypographyLabelM.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (subsComplete) ScoutSuccessLive else ScoutErrorDanger,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun PlayerCardItem(
    player: LineupEntity,
    teamColor: Color,
    onDelete: () -> Unit,
    onToggleCaptain: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_card_${player.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
        border = BorderStroke(1.dp, ScoutDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Jersey circle with team color, white number
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(teamColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${player.jerseyNumber}",
                        style = ScoutTypographyTitleM.copy(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.playerName,
                            style = ScoutTypographyLabelL.copy(
                                color = ScoutTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )

                        // Captain ★ icon
                        if (player.isCaptain) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Captain",
                                tint = ScoutWarning,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(onClick = onToggleCaptain)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Position 18sp #4A5568
                        Text(
                            text = player.position,
                            style = ScoutTypographyBodyM.copy(
                                color = ScoutTextSecondary,
                                fontSize = 18.sp
                            )
                        )

                        // "SUB" pill: amber bg #B7791F, white 16sp Medium text, if !isStarter
                        if (!player.isStarter) {
                            Box(
                                modifier = Modifier
                                    .background(ScoutWarning, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .semantics { contentDescription = "Substitute player" }
                            ) {
                                Text(
                                    text = "SUB",
                                    style = ScoutTypographyCaption.copy(
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Delete icon 22dp #C53030 with contentDescription "Delete player"
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("delete_player_${player.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete player",
                    tint = ScoutErrorDanger,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPlayerDialog(
    teamSide: String,
    teamName: String,
    existingPlayers: List<LineupEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, jersey: Int, position: String, isCaptain: Boolean, isStarter: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var jerseyStr by remember { mutableStateOf("") }
    var jerseyError by remember { mutableStateOf<String?>(null) }

    var selectedPosition by remember { mutableStateOf("MID") }
    var isCaptain by remember { mutableStateOf(false) }
    var isStarter by remember { mutableStateOf(true) } // default ON

    val positions = listOf("GK", "DEF", "MID", "FWD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Player to $teamName",
                style = ScoutTypographyTitleM,
                color = ScoutTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Name (required) — 64dp, label above 18sp #4A5568, error 16sp #C53030 below if empty
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Player Name *",
                        style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("input_player_name"),
                        placeholder = {
                            Text("e.g. Fitina Omborenga", style = ScoutTypographyBodyM.copy(color = ScoutTextHint))
                        },
                        textStyle = ScoutTypographyBodyL,
                        shape = RoundedCornerShape(14.dp),
                        isError = nameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScoutPrimaryAction,
                            unfocusedBorderColor = ScoutDivider,
                            errorBorderColor = ScoutErrorDanger
                        ),
                        singleLine = true
                    )
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            style = ScoutTypographyCaption.copy(color = ScoutErrorDanger, fontSize = 16.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Jersey Number (1–99, unique within team) — 64dp, label above 18sp #4A5568, error if duplicate
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Jersey Number (1–99) *",
                        style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = jerseyStr,
                        onValueChange = {
                            jerseyStr = it
                            jerseyError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("input_jersey_number"),
                        placeholder = {
                            Text("1 to 99", style = ScoutTypographyBodyM.copy(color = ScoutTextHint))
                        },
                        textStyle = ScoutTypographyBodyL,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        isError = jerseyError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScoutPrimaryAction,
                            unfocusedBorderColor = ScoutDivider,
                            errorBorderColor = ScoutErrorDanger
                        ),
                        singleLine = true
                    )
                    if (jerseyError != null) {
                        Text(
                            text = jerseyError!!,
                            style = ScoutTypographyCaption.copy(color = ScoutErrorDanger, fontSize = 16.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Position radio 4 options 20sp labels, 32dp radio circles
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Position",
                        style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    positions.forEach { pos ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = (selectedPosition == pos),
                                    onClick = { selectedPosition = pos },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedPosition == pos),
                                onClick = null,
                                modifier = Modifier.size(32.dp),
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ScoutPrimaryAction,
                                    unselectedColor = ScoutTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pos,
                                style = ScoutTypographyLabelL.copy(fontSize = 20.sp, color = ScoutTextPrimary)
                            )
                        }
                    }
                }

                // Starter switch 32dp (default ON)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Starting XI",
                            style = ScoutTypographyLabelL.copy(fontSize = 20.sp, color = ScoutTextPrimary)
                        )
                        Text(
                            text = if (isStarter) "Player starts on pitch" else "Player begins on bench",
                            style = ScoutTypographyCaption.copy(fontSize = 16.sp, color = ScoutTextSecondary)
                        )
                    }
                    Switch(
                        checked = isStarter,
                        onCheckedChange = { isStarter = it },
                        modifier = Modifier.size(width = 56.dp, height = 32.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ScoutPrimaryAction
                        )
                    )
                }

                // Captain switch 32dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Team Captain (C)",
                            style = ScoutTypographyLabelL.copy(fontSize = 20.sp, color = ScoutTextPrimary)
                        )
                        Text(
                            text = "Only one captain per team",
                            style = ScoutTypographyCaption.copy(fontSize = 16.sp, color = ScoutTextSecondary)
                        )
                    }
                    Switch(
                        checked = isCaptain,
                        onCheckedChange = { isCaptain = it },
                        modifier = Modifier.size(width = 56.dp, height = 32.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ScoutWarning
                        )
                    )
                }
            }
        },
        confirmButton = {
            // Save button 64dp filled #0B7EB8
            ScoutButton(
                text = "Save Player",
                style = ScoutButtonStyle.PRIMARY,
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "Player name is required."
                        hasError = true
                    }

                    val jNum = jerseyStr.toIntOrNull()
                    if (jNum == null || jNum < 1 || jNum > 99) {
                        jerseyError = "Enter a valid jersey number (1–99)."
                        hasError = true
                    } else if (existingPlayers.any { it.jerseyNumber == jNum }) {
                        jerseyError = "Jersey #$jNum is already taken by another player on this team."
                        hasError = true
                    }

                    if (!hasError && jNum != null) {
                        onSave(name.trim(), jNum, selectedPosition, isCaptain, isStarter)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                testTag = "save_player_modal_btn"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = ScoutTypographyLabelL.copy(color = ScoutTextSecondary)
                )
            }
        },
        containerColor = ScoutSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
