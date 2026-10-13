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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LineupEntity
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.theme.ScoutBackground
import com.example.ui.theme.ScoutDivider
import com.example.ui.theme.ScoutErrorDanger
import com.example.ui.theme.ScoutInfo
import com.example.ui.theme.ScoutPrimaryAction
import com.example.ui.theme.ScoutSuccessLive
import com.example.ui.theme.ScoutSurface
import com.example.ui.theme.ScoutSurfaceVariant
import com.example.ui.theme.ScoutTextHint
import com.example.ui.theme.ScoutTextPrimary
import com.example.ui.theme.ScoutTextSecondary
import com.example.ui.theme.ScoutTypographyBodyL
import com.example.ui.theme.ScoutTypographyBodyM
import com.example.ui.theme.ScoutTypographyLabelL
import com.example.ui.theme.ScoutTypographyLabelM
import com.example.ui.theme.ScoutTypographyTitleL
import com.example.ui.theme.ScoutTypographyTitleM
import com.example.ui.theme.ScoutTypographyTitleXL
import com.example.ui.theme.ScoutWarning

// ═════════════════════════════════════════════════════════════════════════════
// REUSABLE VIDEO REFEREE BUTTON (64dp, #6B46C1 outlined, TV icon 28dp + 20sp label)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun VideoRefereeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = ScoutSurface,
        border = BorderStroke(2.dp, ScoutInfo),
        modifier = modifier
            .height(64.dp)
            .testTag("video_referee_btn")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LiveTv,
                contentDescription = "Video Referee",
                tint = ScoutInfo,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "VIDEO REFEREE",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutInfo
                )
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 1. CARDS WORKFLOW
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    initialTeam: String,
    lineups: List<LineupEntity>,
    yellowCardCountProvider: (String) -> Int,
    onConfirmCard: (team: String, player: String, cardType: String, alsoFreeKick: Boolean) -> Unit,
    onVideoReferee: () -> Unit,
    onDismiss: () -> Unit
) {
    // Stage: 1 = Team selection (if not already set), 2 = Cards Selection (IMG_8644), 3 = Booked Player Selection (after Confirm)
    var step by remember { mutableStateOf(if (initialTeam == "HOME" || initialTeam == "AWAY") 2 else 1) }
    var selectedTeam by remember { mutableStateOf(if (initialTeam == "HOME" || initialTeam == "AWAY") initialTeam else "HOME") }
    var selectedCardType by remember { mutableStateOf("Yellow") }
    var alsoFreeKick by remember { mutableStateOf(false) }

    val activeTeamName = if (selectedTeam == "HOME") homeTeamName else awayTeamName
    val teamPlayers = lineups.filter { it.team.equals(selectedTeam, ignoreCase = true) }
    var selectedPlayer by remember(selectedTeam) {
        mutableStateOf(teamPlayers.firstOrNull()?.playerName ?: "$activeTeamName Player")
    }

    val yellowCount = yellowCardCountProvider(selectedPlayer)
    val isAlreadyCautioned = yellowCount >= 1

    if (step == 1) {
        // Step 1: Team selection if opened neutrally
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cards_workflow_screen"),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CARDS: SELECT TEAM",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color(0xFF212121),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .clickable {
                                selectedTeam = "HOME"
                                step = 2
                            }
                            .testTag("card_select_home"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFFC62828))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            YellowRedCardsIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(homeTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("HOME", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .clickable {
                                selectedTeam = "AWAY"
                                step = 2
                            }
                            .testTag("card_select_away"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFF0D47A1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            YellowRedCardsIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(awayTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("AWAY", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5)))
                        }
                    }
                }
            }
        }
    } else if (step == 2) {
        // Step 2: EXACT DESIGN OF IMG_8644
        // Red gradient top header bar with team capsule on left, CARDS in center, cards graphic on right.
        // Pure black body with centered "Cards" title and 3 rounded dark grey tiles for Yellow, Yellow/red, Red.
        // Dark grey bottom bar with CANCEL on left, Whistle & TV in center, and CONFIRM > on right.
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cards_workflow_screen"),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TOP HEADER: Dark red gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF8B0000), Color(0xFF5B0000))
                            )
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left team name pill
                    Surface(
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = activeTeamName.uppercase(),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Center: "CARDS" title
                    Text(
                        text = "CARDS",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )

                    // Right: Small Yellow/Red cards icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    ) {
                        YellowRedCardsIcon(modifier = Modifier.size(26.dp))
                    }
                }

                // CENTER CONTENT: "Cards" title & 3 Tiles
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // "Cards" section title
                    Text(
                        text = "Cards",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Horizontal Tiles: Yellow card | Yellow/red card | Red card
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TILE 1: Yellow card
                        val isYellowSelected = selectedCardType == "Yellow"
                        Surface(
                            onClick = { selectedCardType = "Yellow" },
                            modifier = Modifier
                                .size(width = 96.dp, height = 114.dp)
                                .testTag("card_tile_yellow"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF262626),
                            border = if (isYellowSelected) BorderStroke(2.dp, Color(0xFFFFD600)) else BorderStroke(1.dp, Color(0xFF383838))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Yellow card rectangle
                                Surface(
                                    modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD600)
                                ) {}

                                Text(
                                    text = "Yellow\ncard",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }

                        // TILE 2: Yellow/red card
                        val isYellowRedSelected = selectedCardType == "Yellow-Red"
                        Surface(
                            onClick = { selectedCardType = "Yellow-Red" },
                            modifier = Modifier
                                .size(width = 96.dp, height = 114.dp)
                                .testTag("card_tile_yellow_red"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF262626),
                            border = if (isYellowRedSelected) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Overlapping Yellow and Red cards
                                Box(
                                    modifier = Modifier.size(width = 36.dp, height = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Yellow back card
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .size(width = 24.dp, height = 36.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFFFFD600)
                                    ) {}
                                    // Red front card
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(width = 24.dp, height = 36.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFFFF1744)
                                    ) {}
                                }

                                Text(
                                    text = "Yellow/\nred card",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }

                        // TILE 3: Red card
                        val isRedSelected = selectedCardType == "Red"
                        Surface(
                            onClick = { selectedCardType = "Red" },
                            modifier = Modifier
                                .size(width = 96.dp, height = 114.dp)
                                .testTag("card_tile_red"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF262626),
                            border = if (isRedSelected) BorderStroke(2.dp, Color(0xFFFF1744)) else BorderStroke(1.dp, Color(0xFF383838))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Red card rectangle
                                Surface(
                                    modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFF1744)
                                ) {}

                                Text(
                                    text = "Red card",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(bottom = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Optional "Also award free kick" checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { alsoFreeKick = !alsoFreeKick }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    ) {
                        Checkbox(
                            checked = alsoFreeKick,
                            onCheckedChange = { alsoFreeKick = it },
                            modifier = Modifier.size(24.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFE53935),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also award free kick",
                            style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }

                // BOTTOM ACTION BAR: Dark bar matching IMG_8644
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: CANCEL with red X
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("cards_cancel_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✕",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANCEL",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                    }

                    // Center: Circular Green Whistle Icon and White Screen/VAR Outline Icon
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green circular whistle
                        Surface(
                            onClick = { onVideoReferee() },
                            shape = CircleShape,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val wp = Path().apply {
                                        moveTo(w * 0.70f, h * 0.35f)
                                        lineTo(w * 0.70f, h * 0.50f)
                                        lineTo(w * 0.45f, h * 0.50f)
                                        cubicTo(w * 0.45f, h * 0.75f, w * 0.15f, h * 0.75f, w * 0.15f, h * 0.45f)
                                        cubicTo(w * 0.15f, h * 0.25f, w * 0.45f, h * 0.25f, w * 0.45f, h * 0.35f)
                                        close()
                                    }
                                    drawPath(wp, color = Color.White)
                                }
                            }
                        }

                        // Outline screen / whistle icon
                        Surface(
                            onClick = { onVideoReferee() },
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier.size(width = 24.dp, height = 18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(color = Color.White, radius = 2.5.dp.toPx())
                                }
                            }
                        }
                    }

                    // Right: CONFIRM >
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                // Transition to Step 3 (Booked Player Selection)
                                step = 3
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("cards_confirm_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONFIRM",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "›",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }
    } else {
        // Step 3: BOOKED PLAYER SELECTION (after confirming card given)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cards_workflow_screen"),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = { step = 2 },
                            shape = CircleShape,
                            color = Color(0xFF262626),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                        Column {
                            Text(
                                text = "Select Booked Player",
                                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$selectedCardType Card for $activeTeamName",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedCardType == "Yellow") Color(0xFFFFD600) else Color(0xFFFF5252)
                                    )
                                )
                                if (alsoFreeKick) {
                                    Text(
                                        text = " • Free Kick Awarded",
                                        style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color(0xFF262626),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caution Warning if selected player has a prior caution
                if (isAlreadyCautioned) {
                    Surface(
                        color = Color(0xFF332A00),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD600)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Player already has a caution ($yellowCount yellow). Confirming may warrant Yellow-Red dismissal.",
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD600))
                            )
                        }
                    }
                }

                // Player List Grid / Selection
                Text(
                    text = "SELECT SQUAD MEMBER",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teamPlayers) { player ->
                        val isSelected = selectedPlayer == player.playerName
                        val playerYellowCount = yellowCardCountProvider(player.playerName)
                        Surface(
                            onClick = { selectedPlayer = player.playerName },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF2C3E50) else Color(0xFF1E1E1E),
                            border = if (isSelected) BorderStroke(2.dp, Color(0xFF3498DB)) else BorderStroke(1.dp, Color(0xFF2A2A2A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Color(0xFF3498DB) else Color(0xFF333333),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = player.jerseyNumber.toString(),
                                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = player.playerName,
                                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        )
                                        Text(
                                            text = player.position.uppercase(),
                                            style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (playerYellowCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFD600),
                                            modifier = Modifier.size(width = 16.dp, height = 22.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF2ECC71),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm Booked Player Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ScoutButton(
                        text = "Back",
                        style = ScoutButtonStyle.SECONDARY,
                        onClick = { step = 2 },
                        modifier = Modifier.weight(1f)
                    )
                    ScoutButton(
                        text = "Log Card For ${selectedPlayer.take(15)}",
                        style = ScoutButtonStyle.PRIMARY,
                        onClick = {
                            onConfirmCard(selectedTeam, selectedPlayer, selectedCardType, alsoFreeKick)
                        },
                        modifier = Modifier.weight(2f),
                        testTag = "confirm_card_btn"
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. CORNER WORKFLOW
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CornerWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    initialTeam: String,
    lineups: List<LineupEntity>,
    isAdvancedMode: Boolean,
    onConfirmCorner: (team: String, taker: String?, secondaryAction: String?) -> Unit,
    onVideoReferee: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Team, 2: Confirm
    var selectedTeam by remember { mutableStateOf(initialTeam) }
    var selectedSecondaryAction by remember { mutableStateOf<String?>(null) }

    val teamPlayers = lineups.filter { it.team.equals(selectedTeam, ignoreCase = true) }
    var selectedTaker by remember(selectedTeam) {
        mutableStateOf(teamPlayers.firstOrNull()?.playerName ?: "")
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("corner_workflow_screen"),
        color = ScoutBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (step == 1) "Corner: Select Team" else "Confirm Corner Kick",
                    style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = ScoutTextPrimary
                )
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = ScoutSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScoutTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (step == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .clickable {
                                selectedTeam = "HOME"
                                step = 2
                            }
                            .testTag("corner_select_home"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                        border = BorderStroke(3.dp, ScoutPrimaryAction)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = ScoutPrimaryAction, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(homeTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                            Text("HOME CORNER", style = ScoutTypographyLabelM.copy(color = ScoutPrimaryAction))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .clickable {
                                selectedTeam = "AWAY"
                                step = 2
                            }
                            .testTag("corner_select_away"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                        border = BorderStroke(3.dp, ScoutErrorDanger)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = ScoutErrorDanger, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(awayTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                            Text("AWAY CORNER", style = ScoutTypographyLabelM.copy(color = ScoutErrorDanger))
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Heading "Confirm corner?" 32sp Bold
                    Text(
                        text = "Confirm corner for ${if (selectedTeam == "HOME") homeTeamName else awayTeamName}?",
                        style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Small combo icons: Cards / Substitution / Injury
                    Text("Related Incidents (Optional):", style = ScoutTypographyBodyM.copy(color = ScoutTextSecondary))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(
                            Triple("Cards", Icons.Default.Square, ScoutWarning),
                            Triple("Substitution", Icons.Default.SwapHoriz, ScoutPrimaryAction),
                            Triple("Injury", Icons.Default.Healing, ScoutErrorDanger)
                        ).forEach { (actionName, icon, color) ->
                            val isSelected = selectedSecondaryAction == actionName
                            Card(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(80.dp)
                                    .clickable {
                                        selectedSecondaryAction = if (isSelected) null else actionName
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ScoutSurfaceVariant else ScoutSurface
                                ),
                                border = BorderStroke(if (isSelected) 3.dp else 1.dp, color)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(icon, contentDescription = actionName, tint = color, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(actionName, style = ScoutTypographyLabelM.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium))
                                }
                            }
                        }
                    }

                    if (isAdvancedMode && teamPlayers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            OutlinedTextField(
                                value = selectedTaker,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Corner Taker", fontSize = 18.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                teamPlayers.forEach { player ->
                                    DropdownMenuItem(
                                        text = { Text("#${player.jerseyNumber} ${player.playerName}", fontSize = 18.sp) },
                                        onClick = {
                                            selectedTaker = player.playerName
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom: [CANCEL #C53030 outlined] [VIDEO REFEREE] [CONFIRM #1B7F3A filled] 64dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScoutButton(
                        text = "CANCEL",
                        style = ScoutButtonStyle.DANGER,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    VideoRefereeButton(
                        onClick = onVideoReferee,
                        modifier = Modifier.weight(1.2f)
                    )
                    ScoutButton(
                        text = "CONFIRM CORNER",
                        style = ScoutButtonStyle.SUCCESS,
                        onClick = {
                            onConfirmCorner(selectedTeam, selectedTaker.ifBlank { null }, selectedSecondaryAction)
                        },
                        modifier = Modifier.weight(1.2f),
                        testTag = "confirm_corner_btn"
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. PENALTY WORKFLOW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun PenaltyWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    initialTeam: String,
    onConfirmPenalty: (team: String, cause: String, booking: String, comboAction: String?) -> Unit,
    onResolvePenalty: (team: String, outcome: String) -> Unit,
    onVideoReferee: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTeamPreSelected = initialTeam.equals("HOME", ignoreCase = true) || initialTeam.equals("AWAY", ignoreCase = true)
    // Steps: 0: Team (if not set), 1: How was penalty caused (IMG_8649), 2: Was there any booking (IMG_8650), 3: Referee Whistled (IMG_8651), 4: Resolution
    var step by remember { mutableStateOf(if (isTeamPreSelected) 1 else 0) }
    var selectedTeam by remember { mutableStateOf(if (isTeamPreSelected) initialTeam.uppercase() else "HOME") }
    var selectedCause by remember { mutableStateOf("Foul") }
    var selectedBooking by remember { mutableStateOf("No card") }
    var selectedResolution by remember { mutableStateOf("Goal") }

    val activeTeamName = if (selectedTeam == "HOME") homeTeamName else awayTeamName
    val headerGradient = if (selectedTeam == "HOME") {
        Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFF5B0000))) // Rich dark red (IMG_8649)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF002171))) // Deep royal blue (IMG_8650)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("penalty_workflow_screen"),
        color = Color.Black
    ) {
        if (step == 0) {
            // STEP 0: Neutral Team Selection (if opened without initial team)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PENALTY: SELECT TEAM",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color(0xFF212121),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .clickable {
                                selectedTeam = "HOME"
                                step = 1
                            }
                            .testTag("penalty_select_home"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFFC62828))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            WhistleIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(homeTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("HOME", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .clickable {
                                selectedTeam = "AWAY"
                                step = 1
                            }
                            .testTag("penalty_select_away"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFF0D47A1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            WhistleIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(awayTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("AWAY", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5)))
                        }
                    }
                }
            }
        } else {
            // STEPS 1, 2, 3, 4 (ALL PENALTY SCREENS MATCHING IMG_8649, IMG_8650, IMG_8651)
            Column(modifier = Modifier.fillMaxSize()) {

                // TOP HEADER BAR (Exact gradient, capsule, PENALTY KICK + whistle icon or comms badge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(headerGradient)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (step == 3) {
                        // IMG_8651: Left pill says "PLAYER GETTING READY"
                        Surface(
                            modifier = Modifier.align(Alignment.CenterStart),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = "PLAYER GETTING READY",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // IMG_8651: Right side has white badge with red comms receiver icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                        ) {
                            RefereeCommsIcon(modifier = Modifier.size(34.dp))
                        }
                    } else {
                        // IMG_8649 / IMG_8650: Left pill has Team Name (e.g. "NK CELIK ZENICA" or "BORAC BANJA LUKA")
                        Surface(
                            modifier = Modifier.align(Alignment.CenterStart),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = activeTeamName.uppercase(),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Right side: "PENALTY KICK" + Whistle with sound rays icon
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PENALTY KICK",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            WhistleIcon(modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // CENTER CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (step) {
                        1 -> {
                            // SCREEN 1 (IMG_8649): "How was the penalty caused?" -> [ Foul ] [ Handball ]
                            Text(
                                text = "How was the penalty caused?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // TILE 1: Foul (Whistle with sound rays)
                                val isFoul = selectedCause == "Foul"
                                Surface(
                                    onClick = { selectedCause = "Foul" },
                                    modifier = Modifier
                                        .size(112.dp)
                                        .testTag("penalty_cause_foul"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isFoul) Color(0xFFC62828) else Color(0xFF262626),
                                    border = if (isFoul) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        WhistleIcon(modifier = Modifier.size(46.dp))
                                        Text(
                                            text = "Foul",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // TILE 2: Handball (Hand + Soccer ball)
                                val isHandball = selectedCause == "Handball"
                                Surface(
                                    onClick = { selectedCause = "Handball" },
                                    modifier = Modifier
                                        .size(112.dp)
                                        .testTag("penalty_cause_handball"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isHandball) Color(0xFFC62828) else Color(0xFF262626),
                                    border = if (isHandball) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        HandballIcon(modifier = Modifier.size(46.dp))
                                        Text(
                                            text = "Handball",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // SCREEN 2 (IMG_8650): "Was there any booking?" -> [ Yellow card ] [ Yellow/red card ] [ Red card ] [ No card ]
                            Text(
                                text = "Was there any booking?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // TILE 1: Yellow card
                                val isYellow = selectedBooking == "Yellow"
                                Surface(
                                    onClick = { selectedBooking = "Yellow" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("penalty_booking_yellow"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isYellow) BorderStroke(2.dp, Color(0xFFFFD600)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFD600)
                                        ) {}
                                        Text(
                                            text = "Yellow card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }

                                // TILE 2: Yellow/red card
                                val isYellowRed = selectedBooking == "Yellow-Red"
                                Surface(
                                    onClick = { selectedBooking = "Yellow-Red" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("penalty_booking_yellow_red"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isYellowRed) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier.size(width = 36.dp, height = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .size(width = 24.dp, height = 36.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFFFD600)
                                            ) {}
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .size(width = 24.dp, height = 36.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFFF1744)
                                            ) {}
                                        }
                                        Text(
                                            text = "Yellow/\nred card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }
                                }

                                // TILE 3: Red card
                                val isRed = selectedBooking == "Red"
                                Surface(
                                    onClick = { selectedBooking = "Red" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("penalty_booking_red"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isRed) BorderStroke(2.dp, Color(0xFFFF1744)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFF1744)
                                        ) {}
                                        Text(
                                            text = "Red card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }

                                // TILE 4: No card (Crossing out yellow & red cards)
                                val isNoCard = selectedBooking == "No card"
                                Surface(
                                    onClick = { selectedBooking = "No card" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("penalty_booking_none"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isNoCard) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        NoCardCrossedIcon(modifier = Modifier.size(width = 38.dp, height = 40.dp))
                                        Text(
                                            text = "No card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        3 -> {
                            // SCREEN 3 (IMG_8651): "Confirm when referee has whistled for\npenalty to be taken"
                            Text(
                                text = "Confirm when referee has whistled for\npenalty to be taken",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 34.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        else -> {
                            // SCREEN 4: Resolution screen -> "How was the penalty resolved?"
                            Text(
                                text = "How was the penalty resolved?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val outcomes = listOf(
                                Triple("Goal", Icons.Default.SportsSoccer, Color(0xFF2ECC71)),
                                Triple("Miss", Icons.Default.Close, Color(0xFFFF3B30)),
                                Triple("GK Save", Icons.Default.Security, Color(0xFF3498DB)),
                                Triple("Off Target", Icons.Default.Warning, Color(0xFFFF9800)),
                                Triple("Blocked", Icons.Default.Square, Color(0xFFB0BEC5))
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outcomes.forEach { (outcome, icon, tintColor) ->
                                    val isSelected = selectedResolution == outcome
                                    Surface(
                                        onClick = {
                                            selectedResolution = outcome
                                            onResolvePenalty(selectedTeam, outcome)
                                        },
                                        modifier = Modifier
                                            .size(width = 96.dp, height = 114.dp)
                                            .testTag("penalty_res_${outcome.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF262626),
                                        border = if (isSelected) BorderStroke(2.dp, tintColor) else BorderStroke(1.dp, Color(0xFF383838))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 12.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = outcome,
                                                tint = tintColor,
                                                modifier = Modifier.size(34.dp)
                                            )
                                            Text(
                                                text = outcome,
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM ACTION BAR (Matching exact design across IMG_8649, IMG_8650, IMG_8651)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: CANCEL with red ✕
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("penalty_cancel_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✕",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANCEL",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                    }

                    // Center Content:
                    if (step == 3) {
                        // IMG_8651: 4 quick incident icons (Cards, VAR, Medical, Substitution)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { /* open cards */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    YellowRedCardsIcon(modifier = Modifier.size(24.dp))
                                }
                            }
                            Surface(
                                onClick = onVideoReferee,
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    VideoRefereeTvIcon(modifier = Modifier.size(24.dp, 18.dp))
                                }
                            }
                            Surface(
                                onClick = { /* medical */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val red = Color(0xFFE53935)
                                        val arm = 6.dp.toPx()
                                        drawRoundRect(color = red, topLeft = Offset((w - arm) / 2f, 2.dp.toPx()), size = Size(arm, h - 4.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                                        drawRoundRect(color = red, topLeft = Offset(2.dp.toPx(), (h - arm) / 2f), size = Size(w - 4.dp.toPx(), arm), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                                    }
                                }
                            }
                            Surface(
                                onClick = { /* substitution */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        drawArc(
                                            color = Color(0xFF4CAF50),
                                            startAngle = 190f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                            size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = Color(0xFFE53935),
                                            startAngle = 10f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                            size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // IMG_8649 / IMG_8650: Red VIDEO REFEREE button
                        Surface(
                            onClick = onVideoReferee,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD32F2F),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag("penalty_video_referee_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VideoRefereeTvIcon(modifier = Modifier.size(22.dp, 17.dp))
                                Text(
                                    text = "VIDEO REFEREE",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }

                    // Right: CONFIRM >
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                when (step) {
                                    1 -> step = 2 // Advance to Was there any booking? (IMG_8650)
                                    2 -> {
                                        onConfirmPenalty(selectedTeam, selectedCause, selectedBooking, null)
                                        step = 3 // Advance to Confirm when referee has whistled (IMG_8651)
                                    }
                                    3 -> step = 4 // Advance to Resolution screen
                                    4 -> onResolvePenalty(selectedTeam, selectedResolution)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("penalty_confirm_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONFIRM",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "›",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. DANGEROUS FREE KICK & FREE KICK WORKFLOW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun FreeKickWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    initialTeam: String,
    isDangerousZone: Boolean,
    onConfirmFreeKick: (team: String, cause: String, isDangerous: Boolean, booking: String?) -> Unit,
    onResolveDfk: (team: String, outcome: String, isGkSave: Boolean) -> Unit,
    onVideoReferee: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTeamPreSelected = initialTeam.equals("HOME", ignoreCase = true) || initialTeam.equals("AWAY", ignoreCase = true)
    // Steps: 0: Team (if not set), 1: How was DFK caused?, 2: Was there any booking?, 3: Whistled, 4: Resolution
    var step by remember { mutableStateOf(if (isTeamPreSelected) 1 else 0) }
    var selectedTeam by remember { mutableStateOf(if (isTeamPreSelected) initialTeam.uppercase() else "HOME") }
    var selectedCause by remember { mutableStateOf("Foul") }
    var selectedBooking by remember { mutableStateOf("No card") }
    var selectedResolution by remember { mutableStateOf("Goal") }
    var isGkSave by remember { mutableStateOf(false) }

    val activeTeamName = if (selectedTeam == "HOME") homeTeamName else awayTeamName
    val headerGradient = if (selectedTeam == "HOME") {
        Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFF5B0000))) // Rich dark red
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF002171))) // Deep royal blue
    }

    val headerTitle = if (isDangerousZone) "DANGEROUS FREE KICK" else "FREE KICK"

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("free_kick_workflow_screen"),
        color = Color.Black
    ) {
        if (step == 0) {
            // STEP 0: Team selection
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDangerousZone) "DANGEROUS FREE KICK: SELECT TEAM" else "FREE KICK: SELECT TEAM",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color(0xFF212121),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .clickable {
                                selectedTeam = "HOME"
                                step = 1
                            }
                            .testTag("dfk_select_home"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFFC62828))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DangerousFreeKickIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(homeTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("HOME", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .clickable {
                                selectedTeam = "AWAY"
                                step = 1
                            }
                            .testTag("dfk_select_away"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(2.dp, Color(0xFF0D47A1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DangerousFreeKickIcon(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(awayTeamName.uppercase(), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Text("AWAY", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5)))
                        }
                    }
                }
            }
        } else {
            // STEPS 1, 2, 3, 4 (MATCHING PENALTY SCREEN DESIGN)
            Column(modifier = Modifier.fillMaxSize()) {

                // TOP HEADER BAR (Exact gradient, capsule pill, title + icon)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(headerGradient)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (step == 3) {
                        // Left capsule: "PLAYER GETTING READY"
                        Surface(
                            modifier = Modifier.align(Alignment.CenterStart),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = "PLAYER GETTING READY",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Right side: Referee comms receiver icon badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                        ) {
                            RefereeCommsIcon(modifier = Modifier.size(34.dp))
                        }
                    } else {
                        // Left capsule: Active Team Name
                        Surface(
                            modifier = Modifier.align(Alignment.CenterStart),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = activeTeamName.uppercase(),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Right side: Header label + icon
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = headerTitle,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            DangerousFreeKickIcon(modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // CENTER CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (step) {
                        1 -> {
                            // SCREEN 1: "How was the free kick caused?" -> [ Foul ] [ Handball ]
                            Text(
                                text = if (isDangerousZone) "How was the dangerous free kick caused?" else "How was the free kick caused?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // TILE 1: Foul
                                val isFoul = selectedCause == "Foul"
                                Surface(
                                    onClick = { selectedCause = "Foul" },
                                    modifier = Modifier
                                        .size(112.dp)
                                        .testTag("dfk_cause_foul"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isFoul) Color(0xFFC62828) else Color(0xFF262626),
                                    border = if (isFoul) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        WhistleIcon(modifier = Modifier.size(46.dp))
                                        Text(
                                            text = "Foul",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // TILE 2: Handball
                                val isHandball = selectedCause == "Handball"
                                Surface(
                                    onClick = { selectedCause = "Handball" },
                                    modifier = Modifier
                                        .size(112.dp)
                                        .testTag("dfk_cause_handball"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isHandball) Color(0xFFC62828) else Color(0xFF262626),
                                    border = if (isHandball) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        HandballIcon(modifier = Modifier.size(46.dp))
                                        Text(
                                            text = "Handball",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // SCREEN 2: "Was there any booking?" -> [ Yellow card ] [ Yellow/red card ] [ Red card ] [ No card ]
                            Text(
                                text = "Was there any booking?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // TILE 1: Yellow card
                                val isYellow = selectedBooking == "Yellow"
                                Surface(
                                    onClick = { selectedBooking = "Yellow" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("dfk_booking_yellow"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isYellow) BorderStroke(2.dp, Color(0xFFFFD600)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFD600)
                                        ) {}
                                        Text(
                                            text = "Yellow card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }

                                // TILE 2: Yellow/red card
                                val isYellowRed = selectedBooking == "Yellow-Red"
                                Surface(
                                    onClick = { selectedBooking = "Yellow-Red" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("dfk_booking_yellow_red"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isYellowRed) BorderStroke(2.dp, Color(0xFFFF5252)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier.size(width = 36.dp, height = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .size(width = 24.dp, height = 36.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFFFD600)
                                            ) {}
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .size(width = 24.dp, height = 36.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFFF1744)
                                            ) {}
                                        }
                                        Text(
                                            text = "Yellow/\nred card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }
                                }

                                // TILE 3: Red card
                                val isRed = selectedBooking == "Red"
                                Surface(
                                    onClick = { selectedBooking = "Red" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("dfk_booking_red"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isRed) BorderStroke(2.dp, Color(0xFFFF1744)) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(width = 28.dp, height = 40.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFF1744)
                                        ) {}
                                        Text(
                                            text = "Red card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }

                                // TILE 4: No card
                                val isNoCard = selectedBooking == "No card"
                                Surface(
                                    onClick = { selectedBooking = "No card" },
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 114.dp)
                                        .testTag("dfk_booking_none"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF262626),
                                    border = if (isNoCard) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF383838))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        NoCardCrossedIcon(modifier = Modifier.size(width = 38.dp, height = 40.dp))
                                        Text(
                                            text = "No card",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        3 -> {
                            // SCREEN 3: "Confirm when referee has whistled for free kick to be taken"
                            Text(
                                text = if (isDangerousZone) {
                                    "Confirm when referee has whistled for\ndangerous free kick to be taken"
                                } else {
                                    "Confirm when referee has whistled for\nfree kick to be taken"
                                },
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 34.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        else -> {
                            // SCREEN 4: Resolution screen -> "How was the dangerous free kick resolved?"
                            Text(
                                text = if (isDangerousZone) "How was the dangerous free kick resolved?" else "How was the free kick resolved?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val outcomes = listOf(
                                Triple("Goal", Icons.Default.SportsSoccer, Color(0xFF2ECC71)),
                                Triple("No action", Icons.Default.Close, Color(0xFFFF3B30)),
                                Triple("Corner", Icons.Default.Flag, Color(0xFF3498DB)),
                                Triple("Penalty", Icons.Default.AdsClick, Color(0xFFFFD600)),
                                Triple("Another card", Icons.Default.Square, Color(0xFFFF9800))
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outcomes.forEach { (outcome, icon, tintColor) ->
                                    val isSelected = selectedResolution == outcome
                                    Surface(
                                        onClick = {
                                            selectedResolution = outcome
                                            onResolveDfk(selectedTeam, outcome, isGkSave)
                                        },
                                        modifier = Modifier
                                            .size(width = 96.dp, height = 114.dp)
                                            .testTag("dfk_res_${outcome.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF262626),
                                        border = if (isSelected) BorderStroke(2.dp, tintColor) else BorderStroke(1.dp, Color(0xFF383838))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 12.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = outcome,
                                                tint = tintColor,
                                                modifier = Modifier.size(34.dp)
                                            )
                                            Text(
                                                text = outcome,
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Goalkeeper Save toggle
                            Surface(
                                onClick = { isGkSave = !isGkSave },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isGkSave) Color(0xFF1E3A5F) else Color(0xFF262626),
                                border = if (isGkSave) BorderStroke(1.5.dp, Color(0xFF42A5F5)) else BorderStroke(1.dp, Color(0xFF383838)),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = isGkSave,
                                        onCheckedChange = { isGkSave = it },
                                        modifier = Modifier.size(26.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF42A5F5),
                                            uncheckedColor = Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Goalkeeper Save (GK Save)",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // BOTTOM ACTION BAR (Exact design matching penalty screens)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: CANCEL with red ✕
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("dfk_cancel_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✕",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANCEL",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                    }

                    // Center Content:
                    if (step == 3) {
                        // 4 quick incident icons (Cards, VAR, Medical, Substitution)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { /* open cards */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    YellowRedCardsIcon(modifier = Modifier.size(24.dp))
                                }
                            }
                            Surface(
                                onClick = onVideoReferee,
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    VideoRefereeTvIcon(modifier = Modifier.size(24.dp, 18.dp))
                                }
                            }
                            Surface(
                                onClick = { /* medical */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val red = Color(0xFFE53935)
                                        val arm = 6.dp.toPx()
                                        drawRoundRect(color = red, topLeft = Offset((w - arm) / 2f, 2.dp.toPx()), size = Size(arm, h - 4.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                                        drawRoundRect(color = red, topLeft = Offset(2.dp.toPx(), (h - arm) / 2f), size = Size(w - 4.dp.toPx(), arm), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                                    }
                                }
                            }
                            Surface(
                                onClick = { /* substitution */ },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        drawArc(
                                            color = Color(0xFF4CAF50),
                                            startAngle = 190f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                            size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = Color(0xFFE53935),
                                            startAngle = 10f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                            size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Red VIDEO REFEREE button
                        Surface(
                            onClick = onVideoReferee,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD32F2F),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag("dfk_video_referee_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VideoRefereeTvIcon(modifier = Modifier.size(22.dp, 17.dp))
                                Text(
                                    text = "VIDEO REFEREE",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }

                    // Right: CONFIRM >
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                when (step) {
                                    1 -> step = 2 // Advance to Was there any booking?
                                    2 -> {
                                        onConfirmFreeKick(selectedTeam, selectedCause, isDangerousZone, selectedBooking.ifBlank { null })
                                        if (isDangerousZone) {
                                            step = 3 // Advance to Confirm referee whistled
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                    3 -> step = 4 // Advance to Resolution screen
                                    4 -> onResolveDfk(selectedTeam, selectedResolution, isGkSave)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("dfk_confirm_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONFIRM",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "›",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. VAR WORKFLOW SCREEN
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun VarWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    initialTeam: String,
    onConfirmOutcome: (team: String, reason: String, outcome: String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Team, 2: Review reason, 3: Outcome
    var selectedTeam by remember { mutableStateOf(if (initialTeam == "AWAY") "AWAY" else "HOME") }
    var selectedReason by remember { mutableStateOf("Goal") }
    var selectedOutcome by remember { mutableStateOf("Goal") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("var_workflow_screen"),
        color = ScoutBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LiveTv,
                        contentDescription = "VAR",
                        tint = ScoutInfo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (step) {
                            1 -> "VAR: Select Team"
                            2 -> "VIDEO ASSISTANT REFEREE TO REVIEW"
                            else -> "VAR OUTCOME?"
                        },
                        style = ScoutTypographyTitleL.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoutTextPrimary
                        )
                    )
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = ScoutSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScoutTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    // SCREEN 1 — Team selection: 3 cards 220dp each:
                    // Home (blue border) / Away (red border) / Unknown (grey #4A5568)
                    // Each with team name 28sp or "Unknown" 28sp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clickable {
                                    selectedTeam = "HOME"
                                    step = 2
                                }
                                .testTag("var_team_home"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(3.dp, ScoutPrimaryAction),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = ScoutPrimaryAction, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(homeTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                                Text("HOME TEAM", style = ScoutTypographyLabelM.copy(color = ScoutPrimaryAction, fontSize = 20.sp))
                            }
                        }

                        // Away Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clickable {
                                    selectedTeam = "AWAY"
                                    step = 2
                                }
                                .testTag("var_team_away"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(3.dp, ScoutErrorDanger),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = ScoutErrorDanger, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(awayTeamName, style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                                Text("AWAY TEAM", style = ScoutTypographyLabelM.copy(color = ScoutErrorDanger, fontSize = 20.sp))
                            }
                        }

                        // Unknown Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clickable {
                                    selectedTeam = "UNKNOWN"
                                    step = 2
                                }
                                .testTag("var_team_unknown"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                            border = BorderStroke(3.dp, Color(0xFF4A5568)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Unknown", style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                                Text("GENERAL REVIEW", style = ScoutTypographyLabelM.copy(color = Color(0xFF4A5568), fontSize = 20.sp))
                            }
                        }
                    }
                }

                2 -> {
                    // SCREEN 2 — VAR TO REVIEW:
                    // "VIDEO ASSISTANT REFEREE TO REVIEW" 28sp Bold centered
                    // 5 icon buttons in 2 rows, 140dp each:
                    // ⚽ Goal / 🎯 Penalty kick / 🟥 Red card / 👤 Mistaken identity / ⛳ Corner kick
                    // Icon 56dp + label 20sp Medium
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "VIDEO ASSISTANT REFEREE TO REVIEW",
                            style = ScoutTypographyTitleL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        val reviewOptionsRow1 = listOf(
                            Triple("Goal", Icons.Default.SportsSoccer, "⚽ Goal"),
                            Triple("Penalty kick", Icons.Default.AdsClick, "🎯 Penalty kick"),
                            Triple("Red card", Icons.Default.Square, "🟥 Red card")
                        )
                        val reviewOptionsRow2 = listOf(
                            Triple("Mistaken identity", Icons.Default.Person, "👤 Mistaken identity"),
                            Triple("Corner kick", Icons.Default.Flag, "⛳ Corner kick")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            reviewOptionsRow1.forEach { (key, icon, label) ->
                                val isSelected = selectedReason == key
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(140.dp)
                                        .clickable {
                                            selectedReason = key
                                            selectedOutcome = when (key) {
                                                "Goal" -> "Goal"
                                                "Penalty kick" -> "Penalty"
                                                "Red card" -> "Red card"
                                                "Mistaken identity" -> "Mistaken identity"
                                                "Corner kick" -> "Corner"
                                                else -> "Confirmed"
                                            }
                                            step = 3
                                        }
                                        .testTag("var_reason_${key.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) ScoutSurfaceVariant else ScoutSurface),
                                    border = BorderStroke(if (isSelected) 3.dp else 2.dp, ScoutInfo),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(icon, contentDescription = label, tint = ScoutInfo, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(label, style = ScoutTypographyLabelM.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            reviewOptionsRow2.forEach { (key, icon, label) ->
                                val isSelected = selectedReason == key
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(140.dp)
                                        .clickable {
                                            selectedReason = key
                                            selectedOutcome = when (key) {
                                                "Goal" -> "Goal"
                                                "Penalty kick" -> "Penalty"
                                                "Red card" -> "Red card"
                                                "Mistaken identity" -> "Mistaken identity"
                                                "Corner kick" -> "Corner"
                                                else -> "Confirmed"
                                            }
                                            step = 3
                                        }
                                        .testTag("var_reason_${key.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) ScoutSurfaceVariant else ScoutSurface),
                                    border = BorderStroke(if (isSelected) 3.dp else 2.dp, ScoutInfo),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(icon, contentDescription = label, tint = ScoutInfo, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(label, style = ScoutTypographyLabelM.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // SCREEN 3 — VAR OUTCOME:
                    // "VAR OUTCOME?" 32sp Bold
                    // 2 buttons 180dp tall depending on reason:
                    // - Goal → [⚽ Goal] [🚫 No Goal]
                    // - Penalty kick → [🎯 Penalty] [🚫 No Penalty]
                    // - Red card → [🟥 Red card] [🚫 No Red card]
                    // - Mistaken identity → [✅ Mistaken identity] [❌ No mistaken]
                    // - Corner kick → [⛳ Corner] [🚫 No Corner]
                    // Icons 56dp + labels 24sp Medium
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "VAR OUTCOME?",
                            style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = ScoutTextPrimary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val (option1, option2) = when (selectedReason) {
                            "Goal" -> Pair(
                                Triple("Goal", Icons.Default.SportsSoccer, "⚽ Goal"),
                                Triple("No Goal", Icons.Default.Block, "🚫 No Goal")
                            )
                            "Penalty kick" -> Pair(
                                Triple("Penalty", Icons.Default.AdsClick, "🎯 Penalty"),
                                Triple("No Penalty", Icons.Default.Block, "🚫 No Penalty")
                            )
                            "Red card" -> Pair(
                                Triple("Red card", Icons.Default.Square, "🟥 Red card"),
                                Triple("No Red card", Icons.Default.Block, "🚫 No Red card")
                            )
                            "Mistaken identity" -> Pair(
                                Triple("Mistaken identity", Icons.Default.Check, "✅ Mistaken identity"),
                                Triple("No mistaken", Icons.Default.Close, "❌ No mistaken")
                            )
                            else -> Pair(
                                Triple("Corner", Icons.Default.Flag, "⛳ Corner"),
                                Triple("No Corner", Icons.Default.Block, "🚫 No Corner")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Option 1 (Affirmative)
                            val isSel1 = selectedOutcome == option1.first
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clickable { selectedOutcome = option1.first }
                                    .testTag("var_outcome_opt1"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSel1) ScoutSuccessLive.copy(alpha = 0.15f) else ScoutSurface),
                                border = BorderStroke(if (isSel1) 4.dp else 2.dp, ScoutSuccessLive),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(option1.second, contentDescription = option1.third, tint = ScoutSuccessLive, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(option1.third, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, color = ScoutTextPrimary), textAlign = TextAlign.Center)
                                }
                            }

                            // Option 2 (Overturn / Negative)
                            val isSel2 = selectedOutcome == option2.first
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clickable { selectedOutcome = option2.first }
                                    .testTag("var_outcome_opt2"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSel2) ScoutErrorDanger.copy(alpha = 0.15f) else ScoutSurface),
                                border = BorderStroke(if (isSel2) 4.dp else 2.dp, ScoutErrorDanger),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(option2.second, contentDescription = option2.third, tint = ScoutErrorDanger, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(option2.third, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, color = ScoutTextPrimary), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons (64dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScoutButton(
                    text = if (step > 1) "BACK" else "CANCEL",
                    style = ScoutButtonStyle.SECONDARY,
                    onClick = {
                        if (step > 1) step-- else onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                if (step == 3) {
                    ScoutButton(
                        text = "CONFIRM VAR DECISION",
                        style = ScoutButtonStyle.PRIMARY,
                        onClick = {
                            onConfirmOutcome(selectedTeam, selectedReason, selectedOutcome)
                        },
                        modifier = Modifier.weight(1.5f),
                        testTag = "confirm_var_decision_btn"
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// IN-PLAY ACTION ICONS (Matching Match Official & Scouting Layout)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun ThrowInIcon(modifier: Modifier = Modifier, isLeft: Boolean = true) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val ballRadius = 6.dp.toPx()
        val ballCenter = if (isLeft) Offset(w * 0.35f, h * 0.35f) else Offset(w * 0.65f, h * 0.35f)

        // Soccer ball with outline
        drawCircle(color = Color.White, radius = ballRadius, center = ballCenter)
        drawCircle(color = Color.Black, radius = ballRadius, center = ballCenter, style = Stroke(width = 1.2.dp.toPx()))
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = ballCenter)

        // Curved arrow path
        val path = Path().apply {
            if (isLeft) {
                moveTo(w * 0.30f, h * 0.80f)
                quadraticTo(w * 0.80f, h * 0.85f, w * 0.80f, h * 0.45f)
            } else {
                moveTo(w * 0.70f, h * 0.80f)
                quadraticTo(w * 0.20f, h * 0.85f, w * 0.20f, h * 0.45f)
            }
        }
        drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Arrow head
        val headPath = Path().apply {
            if (isLeft) {
                moveTo(w * 0.18f, h * 0.80f)
                lineTo(w * 0.32f, h * 0.68f)
                lineTo(w * 0.32f, h * 0.92f)
            } else {
                moveTo(w * 0.82f, h * 0.80f)
                lineTo(w * 0.68f, h * 0.68f)
                lineTo(w * 0.68f, h * 0.92f)
            }
            close()
        }
        drawPath(headPath, color = Color.White)
    }
}

@Composable
fun FreekickIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        // 3 player silhouettes forming wall
        listOf(w * 0.28f, w * 0.50f, w * 0.72f).forEach { cx ->
            drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(cx, h * 0.24f))
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cx - 2.8.dp.toPx(), h * 0.34f),
                size = Size(5.6.dp.toPx(), 7.dp.toPx()),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
        // Soccer ball in front
        val ballCenter = Offset(w * 0.50f, h * 0.80f)
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = ballCenter)
        drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = ballCenter, style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = Color.Black, radius = 1.5.dp.toPx(), center = ballCenter)
    }
}

@Composable
fun ShotBallIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = 10.dp.toPx()
        drawCircle(color = Color.White, radius = radius, center = center)
        drawCircle(color = Color.Black, radius = radius, center = center, style = Stroke(width = 1.2.dp.toPx()))
        // Center pentagon spot
        drawCircle(color = Color.Black, radius = 3.5.dp.toPx(), center = center)
        for (i in 0 until 5) {
            val angle = (i * 72f - 90f) * (Math.PI / 180f).toFloat()
            val ex = center.x + kotlin.math.cos(angle) * radius
            val ey = center.y + kotlin.math.sin(angle) * radius
            val sx = center.x + kotlin.math.cos(angle) * 3.5.dp.toPx()
            val sy = center.y + kotlin.math.sin(angle) * 3.5.dp.toPx()
            drawLine(color = Color.Black, start = Offset(sx, sy), end = Offset(ex, ey), strokeWidth = 1.2.dp.toPx())
        }
    }
}

@Composable
fun ShotOnGoalIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF2ECC71)) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        // Goalposts outline
        drawRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.15f, h * 0.18f),
            size = Size(w * 0.70f, h * 0.72f),
            style = Stroke(width = 2.dp.toPx())
        )
        // Goal net cross-hatch lines
        drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(w * 0.38f, h * 0.18f), end = Offset(w * 0.38f, h * 0.90f), strokeWidth = 1.dp.toPx())
        drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(w * 0.62f, h * 0.18f), end = Offset(w * 0.62f, h * 0.90f), strokeWidth = 1.dp.toPx())
        drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(w * 0.15f, h * 0.54f), end = Offset(w * 0.85f, h * 0.54f), strokeWidth = 1.dp.toPx())
        // Target bullseye / soccer ball flying into the net
        drawCircle(color = color, radius = 6.dp.toPx(), center = Offset(w * 0.50f, h * 0.54f))
        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.50f, h * 0.54f), style = Stroke(width = 1.2.dp.toPx()))
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(w * 0.50f, h * 0.54f))
    }
}

@Composable
fun ShotOffGoalIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFE74C3C)) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        // Goalpost outline on left side
        drawRect(
            color = Color.White.copy(alpha = 0.75f),
            topLeft = Offset(w * 0.12f, h * 0.22f),
            size = Size(w * 0.52f, h * 0.68f),
            style = Stroke(width = 2.dp.toPx())
        )
        // Soccer ball flying wide past post (top right)
        drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = Offset(w * 0.82f, h * 0.25f))
        drawCircle(color = color, radius = 5.5.dp.toPx(), center = Offset(w * 0.82f, h * 0.25f), style = Stroke(width = 1.5.dp.toPx()))
        // Motion trajectory curve missing wide
        val path = Path().apply {
            moveTo(w * 0.38f, h * 0.75f)
            quadraticTo(w * 0.65f, h * 0.55f, w * 0.76f, h * 0.30f)
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))
    }
}

@Composable
fun ShotBlockedIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF9800)) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        // Shield path
        val shieldPath = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.26f)
            lineTo(w * 0.84f, h * 0.56f)
            quadraticTo(w * 0.50f, h * 0.88f, w * 0.50f, h * 0.90f)
            quadraticTo(w * 0.50f, h * 0.88f, w * 0.16f, h * 0.56f)
            lineTo(w * 0.16f, h * 0.26f)
            close()
        }
        drawPath(path = shieldPath, color = color.copy(alpha = 0.25f))
        drawPath(path = shieldPath, color = color, style = Stroke(width = 2.dp.toPx()))
        // Rebounding soccer ball
        drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.50f))
        drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.50f), style = Stroke(width = 1.2.dp.toPx()))
        // Block ripple lines
        drawLine(color = color, start = Offset(w * 0.34f, h * 0.40f), end = Offset(w * 0.26f, h * 0.35f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.66f, h * 0.40f), end = Offset(w * 0.74f, h * 0.35f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun GkSaveGlovesIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF3498DB)) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        // Left glove
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.36f),
            size = Size(w * 0.30f, h * 0.44f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        // Right glove
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.56f, h * 0.36f),
            size = Size(w * 0.30f, h * 0.44f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        // Ball stopped between/above hands
        drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.28f))
        drawCircle(color = Color.Black, radius = 5.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.28f), style = Stroke(width = 1.2.dp.toPx()))
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(w * 0.50f, h * 0.28f))
    }
}

@Composable
fun AnotherShotIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF9800)) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        // Ghosted first ball
        drawCircle(color = color.copy(alpha = 0.45f), radius = 4.5.dp.toPx(), center = Offset(w * 0.26f, h * 0.66f))
        // Dynamic foreground second ball
        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.68f, h * 0.34f))
        drawCircle(color = color, radius = 6.dp.toPx(), center = Offset(w * 0.68f, h * 0.34f), style = Stroke(width = 1.5.dp.toPx()))
        // Speed dashes
        drawLine(color = color, start = Offset(w * 0.40f, h * 0.54f), end = Offset(w * 0.54f, h * 0.42f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.32f, h * 0.44f), end = Offset(w * 0.48f, h * 0.32f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun GoalKickCapIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        // Goal line at bottom
        drawLine(color = Color.White.copy(alpha = 0.6f), start = Offset(0f, h * 0.90f), end = Offset(w, h * 0.90f), strokeWidth = 1.5.dp.toPx())
        // 6-yard box outline
        drawRect(
            color = Color.White,
            topLeft = Offset(w * 0.20f, h * 0.40f),
            size = Size(w * 0.60f, h * 0.50f),
            style = Stroke(width = 1.8.dp.toPx())
        )
        // Ball placed inside 6-yard box
        val ballCenter = Offset(w * 0.50f, h * 0.65f)
        drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = ballCenter)
        drawCircle(color = Color.Black, radius = 3.5.dp.toPx(), center = ballCenter, style = Stroke(width = 0.8.dp.toPx()))
        drawCircle(color = Color.Black, radius = 1.2.dp.toPx(), center = ballCenter)

        // Upward kick trajectory arrow
        drawLine(
            color = Color.White,
            start = Offset(w * 0.50f, h * 0.45f),
            end = Offset(w * 0.50f, h * 0.15f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        val headPath = Path().apply {
            moveTo(w * 0.50f, h * 0.05f)
            lineTo(w * 0.32f, h * 0.22f)
            lineTo(w * 0.68f, h * 0.22f)
            close()
        }
        drawPath(headPath, color = Color.White)
    }
}

@Composable
fun RefereeOffsideFlag(modifier: Modifier = Modifier, flipped: Boolean = false) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val poleW = 1.8.dp.toPx()
        val flagW = w * 0.72f
        val flagH = h * 0.62f

        if (!flipped) {
            // White flagpole on left
            drawLine(color = Color.White, start = Offset(2.dp.toPx(), 2.dp.toPx()), end = Offset(2.dp.toPx(), h), strokeWidth = poleW)
            val left = 2.dp.toPx() + poleW
            val halfW = flagW / 2f
            val halfH = flagH / 2f
            drawRect(color = Color(0xFFFFD700), topLeft = Offset(left, 2.dp.toPx()), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFE53935), topLeft = Offset(left + halfW, 2.dp.toPx()), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFE53935), topLeft = Offset(left, 2.dp.toPx() + halfH), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFFFD700), topLeft = Offset(left + halfW, 2.dp.toPx() + halfH), size = Size(halfW, halfH))
        } else {
            // White flagpole on right
            val poleX = w - 2.dp.toPx()
            drawLine(color = Color.White, start = Offset(poleX, 2.dp.toPx()), end = Offset(poleX, h), strokeWidth = poleW)
            val left = poleX - flagW
            val halfW = flagW / 2f
            val halfH = flagH / 2f
            drawRect(color = Color(0xFFE53935), topLeft = Offset(left, 2.dp.toPx()), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFFFD700), topLeft = Offset(left + halfW, 2.dp.toPx()), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFFFD700), topLeft = Offset(left, 2.dp.toPx() + halfH), size = Size(halfW, halfH))
            drawRect(color = Color(0xFFE53935), topLeft = Offset(left + halfW, 2.dp.toPx() + halfH), size = Size(halfW, halfH))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. SHOT WORKFLOW SCREEN (Matching IMG_8704 in color, font, and icon design)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun ShotWorkflowScreen(
    homeTeamName: String,
    awayTeamName: String,
    actionTeam: String,
    latestTapX: Float? = null,
    onShot: (team: String, category: String, header: Boolean, post: Boolean, bar: Boolean, insideBox: Boolean, outcome: String?) -> Unit,
    onVideoReferee: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val activeTeamName = if (actionTeam.equals("HOME", ignoreCase = true)) homeTeamName else awayTeamName
    val isHome = actionTeam.equals("HOME", ignoreCase = true)
    val headerGradient = if (isHome) {
        Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFF5B0000))) // Rich dark red (IMG_8704 / IMG_8649)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF002171))) // Deep royal blue (IMG_8650)
    }
    val accentColor = if (isHome) Color(0xFFE53935) else Color(0xFF42A5F5)

    // Step 1: Select Shot Type & Attributes (IMG_8704 Screen 1)
    // Step 2: Shot Resolution (IMG_8704 Screen 2 / Screen 3 / Off Goal)
    var step by remember { mutableIntStateOf(1) }
    var shotCategory by remember { mutableStateOf("Shot on goal") }
    var isHeader by remember { mutableStateOf(false) }
    var isPost by remember { mutableStateOf(false) }
    var isBar by remember { mutableStateOf(false) }

    val autoInside = if (isHome) {
        latestTapX != null && (latestTapX >= 0.85f || (latestTapX * 100f) >= 85f)
    } else {
        latestTapX != null && (latestTapX <= 0.15f || (latestTapX * 100f) <= 15f)
    }
    var isInsideBox by remember { mutableStateOf(autoInside) }

    var selectedResolution by remember { mutableStateOf("Goal") }
    var isGkSave by remember { mutableStateOf(false) }

    val headerTitle = when (step) {
        1 -> "SHOT"
        else -> when (shotCategory) {
            "Shot on goal" -> "SHOT ON GOAL"
            "Shot blocked" -> "SHOT BLOCKED"
            else -> "SHOT OFF GOAL"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shot_workflow_screen"),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ═════════════════════════════════════════════════════════════════
            // TOP HEADER BAR (Exact gradient, capsule, SHOT title + icon)
            // ═════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(headerGradient)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Left pill: Team Name
                Surface(
                    modifier = Modifier.align(Alignment.CenterStart),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.40f)
                ) {
                    Text(
                        text = activeTeamName.uppercase(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Right side: Header Title + Icon
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = headerTitle,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    when (step) {
                        1 -> ShotBallIcon(modifier = Modifier.size(26.dp))
                        else -> when (shotCategory) {
                            "Shot blocked" -> ShotBlockedIcon(modifier = Modifier.size(26.dp))
                            "Shot off goal" -> ShotOffGoalIcon(modifier = Modifier.size(26.dp))
                            else -> ShotBallIcon(modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }

            // ═════════════════════════════════════════════════════════════════
            // CENTER CONTENT
            // ═════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (step == 1) {
                    // SCREEN 1 (IMG_8704): "Select shot type:" -> [Shot on goal] [Shot off goal] [Shot blocked]
                    Text(
                        text = "Select shot type:",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val shotTypes = listOf(
                        Pair("Shot on goal", "shot_type_Shot_on_goal"),
                        Pair("Shot off goal", "shot_type_Shot_off_goal"),
                        Pair("Shot blocked", "shot_type_Shot_blocked")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        shotTypes.forEach { (type, tag) ->
                            val isSelected = shotCategory == type
                            Surface(
                                onClick = { shotCategory = type },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(136.dp)
                                    .testTag(tag),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    if (isHome) Color(0xFF3E1212) else Color(0xFF0F2B52)
                                } else {
                                    Color(0xFF262626)
                                },
                                border = if (isSelected) {
                                    BorderStroke(2.dp, if (isHome) Color(0xFFFF5252) else Color(0xFF42A5F5))
                                } else {
                                    BorderStroke(1.dp, Color(0xFF383838))
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    when (type) {
                                        "Shot on goal" -> ShotOnGoalIcon(modifier = Modifier.size(38.dp))
                                        "Shot off goal" -> ShotOffGoalIcon(modifier = Modifier.size(38.dp))
                                        else -> ShotBlockedIcon(modifier = Modifier.size(38.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = type,
                                        style = TextStyle(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ATTRIBUTE CHECKBOXES (Header, Post, Bar, Inside the box)
                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShotAttributeItem(
                            label = "Header",
                            checked = isHeader,
                            onCheckedChange = { isHeader = it },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                        ShotAttributeItem(
                            label = "Post",
                            checked = isPost,
                            onCheckedChange = { isPost = it },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                        ShotAttributeItem(
                            label = "Bar",
                            checked = isBar,
                            onCheckedChange = { isBar = it },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                        ShotAttributeItem(
                            label = "Inside the box",
                            checked = isInsideBox,
                            onCheckedChange = { isInsideBox = it },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                } else {
                    // SCREEN 2: RESOLUTION SCREEN (IMG_8704)
                    when (shotCategory) {
                        "Shot on goal" -> {
                            // "How was the shot on goal resolved?" (IMG_8704 Screen 2)
                            Text(
                                text = "How was the shot on goal resolved?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val outcomes = listOf(
                                Pair("Goal", Color(0xFF2ECC71)),
                                Pair("GK save", Color(0xFF3498DB)),
                                Pair("Another shot", Color(0xFFFF9800)),
                                Pair("Corner kick", Color(0xFFF1C40F)),
                                Pair("No action", Color(0xFFFF3B30))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outcomes.forEach { (outcome, tintColor) ->
                                    val isSelected = selectedResolution == outcome
                                    Surface(
                                        onClick = {
                                            selectedResolution = outcome
                                            if (outcome == "GK save") {
                                                isGkSave = true
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(114.dp)
                                            .testTag("shot_res_${outcome.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) tintColor.copy(alpha = 0.22f) else Color(0xFF262626),
                                        border = if (isSelected) BorderStroke(2.dp, tintColor) else BorderStroke(1.dp, Color(0xFF383838))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            when (outcome) {
                                                "Goal" -> ShotBallIcon(modifier = Modifier.size(34.dp))
                                                "GK save" -> GkSaveGlovesIcon(modifier = Modifier.size(34.dp))
                                                "Another shot" -> AnotherShotIcon(modifier = Modifier.size(34.dp))
                                                "Corner kick" -> CornerKickTopFlagIcon(modifier = Modifier.size(34.dp))
                                                else -> Icon(Icons.Default.Close, contentDescription = "No action", tint = Color(0xFFFF3B30), modifier = Modifier.size(34.dp))
                                            }
                                            Text(
                                                text = outcome,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Goalkeeper Save toggle (IMG_8704 checkbox on right side / row)
                            Surface(
                                onClick = { isGkSave = !isGkSave },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isGkSave) Color(0xFF1E3A5F) else Color(0xFF262626),
                                border = if (isGkSave) BorderStroke(1.5.dp, Color(0xFF42A5F5)) else BorderStroke(1.dp, Color(0xFF383838)),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .testTag("shot_gk_save_checkbox")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = isGkSave,
                                        onCheckedChange = { isGkSave = it },
                                        modifier = Modifier.size(26.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF42A5F5),
                                            uncheckedColor = Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Goalkeeper Save (GK save)",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        "Shot blocked" -> {
                            // "How was the blocked shot resolved?" (IMG_8704 Screen 3)
                            Text(
                                text = "How was the blocked shot resolved?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val outcomes = listOf(
                                Pair("No action", Color(0xFFFF3B30)),
                                Pair("Another shot", Color(0xFFFF9800)),
                                Pair("Corner kick", Color(0xFFF1C40F)),
                                Pair("Handball", Color(0xFFE91E63)),
                                Pair("Goal", Color(0xFF2ECC71))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outcomes.forEach { (outcome, tintColor) ->
                                    val isSelected = selectedResolution == outcome
                                    Surface(
                                        onClick = { selectedResolution = outcome },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(114.dp)
                                            .testTag("shot_res_${outcome.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) tintColor.copy(alpha = 0.22f) else Color(0xFF262626),
                                        border = if (isSelected) BorderStroke(2.dp, tintColor) else BorderStroke(1.dp, Color(0xFF383838))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            when (outcome) {
                                                "No action" -> Icon(Icons.Default.Close, contentDescription = "No action", tint = Color(0xFFFF3B30), modifier = Modifier.size(34.dp))
                                                "Another shot" -> AnotherShotIcon(modifier = Modifier.size(34.dp))
                                                "Corner kick" -> CornerKickTopFlagIcon(modifier = Modifier.size(34.dp))
                                                "Handball" -> HandballIcon(modifier = Modifier.size(34.dp))
                                                else -> ShotBallIcon(modifier = Modifier.size(34.dp))
                                            }
                                            Text(
                                                text = outcome,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // "How was the off goal shot resolved?" (Shot off goal)
                            Text(
                                text = "How was the off goal shot resolved?",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val outcomes = listOf(
                                Pair("Goal kick", Color(0xFF3498DB)),
                                Pair("Corner kick", Color(0xFFF1C40F)),
                                Pair("No action", Color(0xFFFF3B30)),
                                Pair("Another shot", Color(0xFFFF9800))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outcomes.forEach { (outcome, tintColor) ->
                                    val isSelected = selectedResolution == outcome
                                    Surface(
                                        onClick = { selectedResolution = outcome },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(114.dp)
                                            .testTag("shot_res_${outcome.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) tintColor.copy(alpha = 0.22f) else Color(0xFF262626),
                                        border = if (isSelected) BorderStroke(2.dp, tintColor) else BorderStroke(1.dp, Color(0xFF383838))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            when (outcome) {
                                                "Goal kick" -> GoalKickCapIcon(modifier = Modifier.size(34.dp))
                                                "Corner kick" -> CornerKickTopFlagIcon(modifier = Modifier.size(34.dp))
                                                "No action" -> Icon(Icons.Default.Close, contentDescription = "No action", tint = Color(0xFFFF3B30), modifier = Modifier.size(34.dp))
                                                else -> AnotherShotIcon(modifier = Modifier.size(34.dp))
                                            }
                                            Text(
                                                text = outcome,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ═════════════════════════════════════════════════════════════════
            // BOTTOM ACTION BAR (Exact design matching penalty & DFK screens)
            // ═════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (step == 1) {
                    // Left: CANCEL with red ✕
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("shot_cancel_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✕",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANCEL",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                } else {
                    // Left: ‹ BACK
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { step = 1 }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("shot_back_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BACK",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                // Center: VIDEO REFEREE button
                Surface(
                    onClick = onVideoReferee,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD32F2F),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("shot_video_referee_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VideoRefereeTvIcon(modifier = Modifier.size(22.dp, 17.dp))
                        Text(
                            text = "VIDEO REFEREE",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                // Right: CONFIRM >
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            if (step == 1) {
                                // Default selection for step 2
                                selectedResolution = when (shotCategory) {
                                    "Shot on goal" -> "Goal"
                                    "Shot blocked" -> "No action"
                                    else -> "Goal kick"
                                }
                                step = 2
                            } else {
                                val outcome = if (isGkSave && selectedResolution != "GK save") {
                                    "$selectedResolution (GK Save)"
                                } else {
                                    selectedResolution
                                }
                                onShot(actionTeam, shotCategory, isHeader, isPost, isBar, isInsideBox, outcome)
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .testTag(if (step == 1) "confirm_shot_step1_btn" else "shot_step2_confirm_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONFIRM",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "›",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun ShotAttributeItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(10.dp),
        color = if (checked) accentColor.copy(alpha = 0.20f) else Color(0xFF262626),
        border = if (checked) BorderStroke(1.5.dp, accentColor) else BorderStroke(1.dp, Color(0xFF383838)),
        modifier = modifier.height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(22.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = accentColor,
                    uncheckedColor = Color.White.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                maxLines = 1
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. IN-PLAY ACTIONS SCREEN (Double-Tap Pitch)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun InPlayActionsScreen(
    homeTeamName: String,
    awayTeamName: String,
    latestTapX: Float?,
    currentPossession: String = "HOME",
    onThrowIn: (team: String) -> Unit,
    onFreeKick: (team: String, cause: String) -> Unit,
    onShot: (team: String, category: String, header: Boolean, post: Boolean, bar: Boolean, insideBox: Boolean, outcome: String?) -> Unit,
    onGoalKick: (team: String) -> Unit,
    onOffside: (team: String) -> Unit,
    onVideoReferee: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var currentSubScreen by remember { mutableIntStateOf(0) } // 0: Main Split, 1: FK Cause, 2: Shot Step 1, 3: Shot Step 2
    var actionTeam by remember { mutableStateOf("HOME") }

    // Shot states
    var shotCategory by remember { mutableStateOf("Shot on goal") }
    var isHeader by remember { mutableStateOf(false) }
    var isPost by remember { mutableStateOf(false) }
    var isBar by remember { mutableStateOf(false) }
    var isInsideBox by remember { mutableStateOf(false) }

    if (currentSubScreen == 0) {
        // EXACT IN-PLAY ACTIONS SPLIT SCREEN (Blue vs Red)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("in_play_actions_overlay")
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // LEFT: BLUE SIDE (HOME TEAM)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF1976D2))
                ) {
                    // Header Bar (Darker Blue)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color(0xFF0D47A1))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShotBallIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = homeTeamName.uppercase(),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 1. Throw in
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onThrowIn("HOME") }
                            .padding(horizontal = 24.dp)
                            .testTag("home_throw_in_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThrowInIcon(isLeft = true)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Throw in", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 2. Freekick
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                actionTeam = "HOME"
                                currentSubScreen = 1
                            }
                            .padding(horizontal = 24.dp)
                            .testTag("home_free_kick_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FreekickIcon()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Freekick", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 3. Shot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                actionTeam = "HOME"
                                val autoInside = latestTapX != null && (latestTapX >= 0.85f || (latestTapX * 100f) >= 85f)
                                isInsideBox = autoInside
                                shotCategory = "Shot on goal"
                                isHeader = false
                                isPost = false
                                isBar = false
                                currentSubScreen = 2
                            }
                            .padding(horizontal = 24.dp)
                            .testTag("home_shot_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShotBallIcon()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Shot", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 4. Goal kick
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onGoalKick("HOME") }
                            .padding(horizontal = 24.dp)
                            .testTag("home_goal_kick_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoalKickCapIcon()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Goal kick", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 5. Offside
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onOffside("HOME") }
                            .padding(horizontal = 24.dp)
                            .testTag("home_offside_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RefereeOffsideFlag(flipped = false)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Offside", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // RIGHT: RED SIDE (AWAY TEAM)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFD32F2F))
                ) {
                    // Header Bar (Darker Red)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color(0xFFB71C1C))
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = awayTeamName.uppercase(),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 1. Throw in
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onThrowIn("AWAY") }
                            .padding(horizontal = 24.dp)
                            .testTag("away_throw_in_btn"),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Throw in", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(16.dp))
                        ThrowInIcon(isLeft = false)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 2. Freekick
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                actionTeam = "AWAY"
                                currentSubScreen = 1
                            }
                            .padding(horizontal = 24.dp)
                            .testTag("away_free_kick_btn"),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Freekick", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(16.dp))
                        FreekickIcon()
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 3. Shot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                actionTeam = "AWAY"
                                val autoInside = latestTapX != null && (latestTapX <= 0.15f || (latestTapX * 100f) <= 15f)
                                isInsideBox = autoInside
                                shotCategory = "Shot on goal"
                                isHeader = false
                                isPost = false
                                isBar = false
                                currentSubScreen = 2
                            }
                            .padding(horizontal = 24.dp)
                            .testTag("away_shot_btn"),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shot", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(16.dp))
                        ShotBallIcon()
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 4. Goal kick
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onGoalKick("AWAY") }
                            .padding(horizontal = 24.dp)
                            .testTag("away_goal_kick_btn"),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Goal kick", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(16.dp))
                        GoalKickCapIcon()
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f), thickness = 1.dp)

                    // 5. Offside
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onOffside("AWAY") }
                            .padding(horizontal = 24.dp)
                            .testTag("away_offside_btn"),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Offside", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(16.dp))
                        RefereeOffsideFlag(flipped = true)
                    }
                }
            }

            // CENTER CHEVRON NOTCH (Indicating attack/possession direction)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 24.dp, height = 38.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        if (currentPossession.equals("AWAY", ignoreCase = true)) {
                            // Red team attacking / possession: points LEFT into Blue (<)
                            moveTo(size.width, 0f)
                            lineTo(0f, size.height / 2f)
                            lineTo(size.width, size.height)
                            close()
                        } else {
                            // Blue team attacking / possession: points RIGHT into Red (>)
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height / 2f)
                            lineTo(0f, size.height)
                            close()
                        }
                    }
                    val notchColor = if (currentPossession.equals("AWAY", ignoreCase = true)) Color(0xFFD32F2F) else Color(0xFF1976D2)
                    drawPath(path, color = notchColor)
                }
            }

            // TOP CENTER CLOSE BUTTON (Between Blue and Red headers)
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFD0D5DD),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .size(width = 46.dp, height = 36.dp)
                    .testTag("in_play_close_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF374151),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else if (currentSubScreen == 2) {
        // DEDICATED SHOT WORKFLOW SCREEN (Matching IMG_8704 in color, font, and icon design)
        ShotWorkflowScreen(
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            actionTeam = actionTeam,
            latestTapX = latestTapX,
            onShot = onShot,
            onVideoReferee = onVideoReferee,
            onDismiss = { currentSubScreen = 0 }
        )
    } else {
        // FREE KICK CAUSE (Subscreen 1)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("in_play_actions_overlay"),
            color = Color.Black
        ) {
            val isHome = actionTeam.equals("HOME", ignoreCase = true)
            val activeTeamName = if (isHome) homeTeamName else awayTeamName
            val headerGradient = if (isHome) {
                Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFF5B0000)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF002171)))
            }
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(headerGradient)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterStart),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.40f)
                    ) {
                        Text(
                            text = activeTeamName.uppercase(),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FREE KICK",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        DangerousFreeKickIcon(modifier = Modifier.size(26.dp))
                    }
                }

                // Center Question & Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "How was the free kick caused?",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Foul
                        Surface(
                            onClick = { onFreeKick(actionTeam, "Foul") },
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .testTag("inplay_fk_foul_btn"),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF262626),
                            border = BorderStroke(1.5.dp, Color(0xFFC62828))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                WhistleIcon(modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Foul", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }

                        // Handball
                        Surface(
                            onClick = { onFreeKick(actionTeam, "Handball") },
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .testTag("inplay_fk_handball_btn"),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF262626),
                            border = BorderStroke(1.5.dp, Color(0xFFFF9800))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                HandballIcon(modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Handball", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }

                // Bottom bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { currentSubScreen = 0 }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("‹", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BACK", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InPlayButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = ScoutSurface,
        border = BorderStroke(2.dp, accentColor),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ScoutTextPrimary
                )
            )
        }
    }
}

@Composable
private fun InPlayCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(32.dp),
            colors = CheckboxDefaults.colors(checkedColor = ScoutPrimaryAction)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = ScoutTypographyBodyL.copy(fontSize = 20.sp, color = ScoutTextPrimary))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. FIRST LEVEL SCREEN (MATCH EVENTS) — TWO SIDES EXACT IMG_8640 DESIGN
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun WhistleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(34.dp)) {
        val w = size.width
        val h = size.height

        // Sound blast rays coming off top of whistle
        val rayCol = Color.White
        val rayW = 2.dp.toPx()
        // Left ray
        drawLine(
            color = rayCol,
            start = Offset(w * 0.32f, h * 0.18f),
            end = Offset(w * 0.22f, h * 0.04f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )
        // Middle ray
        drawLine(
            color = rayCol,
            start = Offset(w * 0.44f, h * 0.16f),
            end = Offset(w * 0.44f, h * 0.02f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )
        // Right ray
        drawLine(
            color = rayCol,
            start = Offset(w * 0.56f, h * 0.18f),
            end = Offset(w * 0.66f, h * 0.04f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )

        // Whistle chamber (circular bulb on right) and spout (cylinder on left)
        val whistlePath = Path().apply {
            // Mouthpiece tip at right
            moveTo(w * 0.84f, h * 0.48f)
            lineTo(w * 0.84f, h * 0.62f)
            // Barrel going to left
            lineTo(w * 0.54f, h * 0.62f)
            // Bottom curve of ball chamber
            cubicTo(w * 0.52f, h * 0.88f, w * 0.18f, h * 0.88f, w * 0.16f, h * 0.58f)
            // Top curve of ball chamber
            cubicTo(w * 0.16f, h * 0.32f, w * 0.48f, h * 0.30f, w * 0.52f, h * 0.46f)
            // Step up to mouthpiece top
            lineTo(w * 0.84f, h * 0.48f)
            close()
        }
        drawPath(whistlePath, color = Color.White)

        // Inner sound hole slit
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.75f),
            topLeft = Offset(w * 0.42f, h * 0.36f),
            size = Size(w * 0.10f, h * 0.18f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun CornerKickTopFlagIcon(modifier: Modifier = Modifier, flipped: Boolean = false) {
    Canvas(modifier = modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val lineW = 3.5.dp.toPx()

        if (!flipped) {
            // Left corner line: vertical line down left edge, horizontal line along top edge
            drawLine(color = Color.White, start = Offset(w * 0.20f, h * 0.15f), end = Offset(w * 0.20f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            drawLine(color = Color.White, start = Offset(w * 0.20f, h * 0.15f), end = Offset(w * 0.85f, h * 0.15f), strokeWidth = lineW, cap = StrokeCap.Square)
            // Quarter circle arc in the corner
            drawArc(
                color = Color.White,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.20f - w * 0.38f, h * 0.15f - w * 0.38f),
                size = Size(w * 0.76f, w * 0.76f),
                style = Stroke(width = lineW)
            )
        } else {
            // Right corner line: vertical line down right edge, horizontal line along top edge
            drawLine(color = Color.White, start = Offset(w * 0.80f, h * 0.15f), end = Offset(w * 0.80f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            drawLine(color = Color.White, start = Offset(w * 0.80f, h * 0.15f), end = Offset(w * 0.15f, h * 0.15f), strokeWidth = lineW, cap = StrokeCap.Square)
            // Quarter circle arc in the corner
            drawArc(
                color = Color.White,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.80f - w * 0.38f, h * 0.15f - w * 0.38f),
                size = Size(w * 0.76f, w * 0.76f),
                style = Stroke(width = lineW)
            )
        }
    }
}

@Composable
fun CornerKickBottomFlagIcon(modifier: Modifier = Modifier, flipped: Boolean = false) {
    Canvas(modifier = modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val lineW = 3.5.dp.toPx()

        if (!flipped) {
            // Bottom-left corner: vertical line up left edge, horizontal line along bottom edge
            drawLine(color = Color.White, start = Offset(w * 0.20f, h * 0.15f), end = Offset(w * 0.20f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            drawLine(color = Color.White, start = Offset(w * 0.20f, h * 0.85f), end = Offset(w * 0.85f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            // Quarter circle arc
            drawArc(
                color = Color.White,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.20f - w * 0.38f, h * 0.85f - w * 0.38f),
                size = Size(w * 0.76f, w * 0.76f),
                style = Stroke(width = lineW)
            )
        } else {
            // Bottom-right corner: vertical line up right edge, horizontal line along bottom edge
            drawLine(color = Color.White, start = Offset(w * 0.80f, h * 0.15f), end = Offset(w * 0.80f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            drawLine(color = Color.White, start = Offset(w * 0.80f, h * 0.85f), end = Offset(w * 0.15f, h * 0.85f), strokeWidth = lineW, cap = StrokeCap.Square)
            // Quarter circle arc
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.80f - w * 0.38f, h * 0.85f - w * 0.38f),
                size = Size(w * 0.76f, w * 0.76f),
                style = Stroke(width = lineW)
            )
        }
    }
}

@Composable
fun YellowRedCardsIcon(modifier: Modifier = Modifier.size(34.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cardW = w * 0.44f
        val cardH = h * 0.70f
        val cr = 3.dp.toPx()

        // Back card: Yellow card tilted slightly left
        val yellowPath = Path().apply {
            moveTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.12f + cardW, h * 0.14f)
            lineTo(w * 0.12f + cardW + 2.dp.toPx(), h * 0.14f + cardH)
            lineTo(w * 0.12f + 2.dp.toPx(), h * 0.22f + cardH)
            close()
        }
        drawPath(yellowPath, color = Color(0xFFFFD600))

        // Front card: Red card upright slightly to the right with subtle border
        drawRoundRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(w * 0.40f, h * 0.15f),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(cr, cr)
        )
    }
}

@Composable
fun HandballIcon(modifier: Modifier = Modifier.size(40.dp), color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soccer ball on the left
        val ballCenter = Offset(w * 0.32f, h * 0.58f)
        val ballR = w * 0.22f
        drawCircle(color = color, radius = ballR, center = ballCenter)
        // Pentagon/hex markings on soccer ball with dark lines
        val pentagonPath = Path().apply {
            moveTo(ballCenter.x, ballCenter.y - ballR * 0.45f)
            lineTo(ballCenter.x + ballR * 0.42f, ballCenter.y - ballR * 0.15f)
            lineTo(ballCenter.x + ballR * 0.26f, ballCenter.y + ballR * 0.42f)
            lineTo(ballCenter.x - ballR * 0.26f, ballCenter.y + ballR * 0.42f)
            lineTo(ballCenter.x - ballR * 0.42f, ballCenter.y - ballR * 0.15f)
            close()
        }
        drawPath(pentagonPath, color = Color.Black.copy(alpha = 0.85f))

        // Open hand with fingers pointing upward on the right/foreground
        val handPath = Path().apply {
            // Wrist
            moveTo(w * 0.60f, h * 0.95f)
            lineTo(w * 0.82f, h * 0.95f)
            lineTo(w * 0.85f, h * 0.65f) // Pinky edge
            // Pinky finger
            lineTo(w * 0.89f, h * 0.34f)
            cubicTo(w * 0.89f, h * 0.27f, w * 0.81f, h * 0.27f, w * 0.81f, h * 0.34f)
            lineTo(w * 0.78f, h * 0.52f)
            // Ring finger
            lineTo(w * 0.77f, h * 0.20f)
            cubicTo(w * 0.77f, h * 0.13f, w * 0.69f, h * 0.13f, w * 0.69f, h * 0.20f)
            lineTo(w * 0.67f, h * 0.50f)
            // Middle finger
            lineTo(w * 0.65f, h * 0.12f)
            cubicTo(w * 0.65f, h * 0.05f, w * 0.57f, h * 0.05f, w * 0.57f, h * 0.12f)
            lineTo(w * 0.56f, h * 0.50f)
            // Index finger
            lineTo(w * 0.54f, h * 0.22f)
            cubicTo(w * 0.54f, h * 0.15f, w * 0.46f, h * 0.15f, w * 0.46f, h * 0.22f)
            lineTo(w * 0.48f, h * 0.56f)
            // Thumb
            lineTo(w * 0.36f, h * 0.50f)
            cubicTo(w * 0.29f, h * 0.50f, w * 0.31f, h * 0.62f, w * 0.38f, h * 0.68f)
            lineTo(w * 0.54f, h * 0.78f)
            lineTo(w * 0.60f, h * 0.95f)
            close()
        }
        drawPath(handPath, color = color)
    }
}

@Composable
fun NoCardCrossedIcon(modifier: Modifier = Modifier.size(width = 38.dp, height = 40.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cardW = w * 0.44f
        val cardH = h * 0.70f
        val cr = 3.dp.toPx()

        // Yellow card (back, tilted left)
        val yellowPath = Path().apply {
            moveTo(w * 0.08f, h * 0.20f)
            lineTo(w * 0.08f + cardW, h * 0.12f)
            lineTo(w * 0.08f + cardW + 2.dp.toPx(), h * 0.12f + cardH)
            lineTo(w * 0.08f + 2.dp.toPx(), h * 0.20f + cardH)
            close()
        }
        drawPath(yellowPath, color = Color(0xFFFFD600))

        // Red card (front, right)
        drawRoundRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(w * 0.36f, h * 0.14f),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(cr, cr)
        )

        // Bold white X crossing over both cards
        val strokeW = 4.dp.toPx()
        // Draw dark shadow line for pop
        drawLine(
            color = Color.Black.copy(alpha = 0.55f),
            start = Offset(w * 0.10f, h * 0.10f),
            end = Offset(w * 0.90f, h * 0.86f),
            strokeWidth = strokeW + 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.55f),
            start = Offset(w * 0.90f, h * 0.10f),
            end = Offset(w * 0.10f, h * 0.86f),
            strokeWidth = strokeW + 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        // White X
        drawLine(
            color = Color.White,
            start = Offset(w * 0.10f, h * 0.10f),
            end = Offset(w * 0.90f, h * 0.86f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.90f, h * 0.10f),
            end = Offset(w * 0.10f, h * 0.86f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun VideoRefereeTvIcon(modifier: Modifier = Modifier.size(width = 24.dp, height = 18.dp), color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer TV outline
        val strokeW = 1.6.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.05f, h * 0.05f),
            size = Size(w * 0.90f, h * 0.70f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokeW)
        )

        // TV Stand / base
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.75f),
            end = Offset(w * 0.50f, h * 0.92f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.32f, h * 0.92f),
            end = Offset(w * 0.68f, h * 0.92f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Whistle shape inside the screen
        val whistlePath = Path().apply {
            moveTo(w * 0.68f, h * 0.34f)
            lineTo(w * 0.68f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.44f)
            cubicTo(w * 0.50f, h * 0.60f, w * 0.30f, h * 0.60f, w * 0.30f, h * 0.42f)
            cubicTo(w * 0.30f, h * 0.26f, w * 0.48f, h * 0.26f, w * 0.50f, h * 0.34f)
            close()
        }
        drawPath(whistlePath, color = color, style = Stroke(width = 1.2.dp.toPx()))

        // Sound blast tick above whistle
        drawLine(
            color = color,
            start = Offset(w * 0.44f, h * 0.22f),
            end = Offset(w * 0.44f, h * 0.14f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun RefereeCommsIcon(modifier: Modifier = Modifier.size(32.dp)) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val w = size.width
                val h = size.height
                val red = Color(0xFFC62828)

                // Phone receiver tilted at an angle
                val phonePath = Path().apply {
                    moveTo(w * 0.25f, h * 0.70f)
                    cubicTo(w * 0.20f, h * 0.85f, w * 0.35f, h * 0.95f, w * 0.45f, h * 0.90f)
                    lineTo(w * 0.55f, h * 0.78f)
                    cubicTo(w * 0.45f, h * 0.60f, w * 0.55f, h * 0.45f, w * 0.68f, h * 0.40f)
                    lineTo(w * 0.80f, h * 0.50f)
                    cubicTo(w * 0.90f, h * 0.45f, w * 0.88f, h * 0.25f, w * 0.75f, h * 0.20f)
                    cubicTo(w * 0.40f, h * 0.25f, w * 0.20f, h * 0.50f, w * 0.25f, h * 0.70f)
                    close()
                }
                drawPath(phonePath, color = red)

                // Small arrow/cord line underneath
                drawLine(
                    color = red,
                    start = Offset(w * 0.20f, h * 0.92f),
                    end = Offset(w * 0.80f, h * 0.92f),
                    strokeWidth = 1.3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun DangerousFreeKickIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(34.dp)) {
        val w = size.width
        val h = size.height

        // White mortarboard / triangle shield angle
        val capPath = Path().apply {
            moveTo(w * 0.50f, h * 0.08f)
            lineTo(w * 0.86f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.58f)
            lineTo(w * 0.14f, h * 0.44f)
            close()
        }
        drawPath(capPath, color = Color.White)

        // Lower body of graduation cap/shield
        val underPath = Path().apply {
            moveTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.64f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.68f, h * 0.62f)
            lineTo(w * 0.50f, h * 0.74f)
            lineTo(w * 0.32f, h * 0.62f)
            close()
        }
        drawPath(underPath, color = Color.White.copy(alpha = 0.9f))

        // Football in the bottom foreground
        val ballCenter = Offset(w * 0.32f, h * 0.78f)
        val r = 5.5.dp.toPx()
        drawCircle(color = Color.White, radius = r, center = ballCenter)
        drawCircle(color = Color.Black, radius = r, center = ballCenter, style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = ballCenter)
    }
}

@Composable
fun VideoRefereeWhistleTvIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(32.dp)) {
        val w = size.width
        val h = size.height

        // Monitor outline
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.10f, h * 0.12f),
            size = Size(w * 0.80f, h * 0.62f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
        // Stand base
        drawLine(
            color = Color.White,
            start = Offset(w * 0.50f, h * 0.74f),
            end = Offset(w * 0.50f, h * 0.88f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.35f, h * 0.88f),
            end = Offset(w * 0.65f, h * 0.88f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Tiny whistle outline inside TV screen
        val wp = Path().apply {
            moveTo(w * 0.68f, h * 0.38f)
            lineTo(w * 0.68f, h * 0.48f)
            lineTo(w * 0.48f, h * 0.48f)
            cubicTo(w * 0.46f, h * 0.62f, w * 0.28f, h * 0.62f, w * 0.26f, h * 0.44f)
            cubicTo(w * 0.26f, h * 0.30f, w * 0.46f, h * 0.28f, w * 0.48f, h * 0.38f)
            close()
        }
        drawPath(wp, color = Color.White)
    }
}

@Composable
fun MatchEventsFirstLevelScreen(
    homeTeamName: String,
    awayTeamName: String,
    onCornerKick: (team: String, isTop: Boolean) -> Unit,
    onCards: (team: String) -> Unit,
    onGoal: (team: String) -> Unit,
    onPenaltyKick: (team: String) -> Unit,
    onDangerousFreeKick: (team: String) -> Unit,
    onVideoReferee: () -> Unit,
    onGeneralEvents: () -> Unit,
    onClose: () -> Unit
) {
    val homeButtonColor = Color(0xFFC62828) // Deep Crimson Red from IMG_8640
    val awayButtonColor = Color(0xFF0D47A1) // Deep Royal Blue from IMG_8640

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_events_first_level_screen"),
        color = Color.Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // ═════════════════════════════════════════════════════════════════
            // LEFT SIDE: HOME TEAM (Red event tiles)
            // ═════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = homeTeamName.uppercase(),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Grid 3 rows x 2 cols
                // Row 1: Corner kick (Top) + Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Corner kick",
                        buttonColor = homeButtonColor,
                        iconContent = { CornerKickTopFlagIcon() },
                        onClick = { onCornerKick("HOME", true) },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_corner_top"
                    )
                    FirstLevelEventButton(
                        label = "Cards",
                        buttonColor = homeButtonColor,
                        iconContent = { YellowRedCardsIcon() },
                        onClick = { onCards("HOME") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_cards"
                    )
                }

                // Row 2: Goal + Penalty kick
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Goal",
                        buttonColor = homeButtonColor,
                        iconContent = { ShotBallIcon(modifier = Modifier.size(34.dp)) },
                        onClick = { onGoal("HOME") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_goal"
                    )
                    FirstLevelEventButton(
                        label = "Penalty kick",
                        buttonColor = homeButtonColor,
                        iconContent = { WhistleIcon() },
                        onClick = { onPenaltyKick("HOME") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_penalty"
                    )
                }

                // Row 3: Corner kick (Bottom) + Dangerous free kick
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Corner kick",
                        buttonColor = homeButtonColor,
                        iconContent = { CornerKickBottomFlagIcon() },
                        onClick = { onCornerKick("HOME", false) },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_corner_bottom"
                    )
                    FirstLevelEventButton(
                        label = "Dangerous\nfree kick",
                        buttonColor = homeButtonColor,
                        iconContent = { DangerousFreeKickIcon() },
                        onClick = { onDangerousFreeKick("HOME") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_home_dfk"
                    )
                }
            }

            // ═════════════════════════════════════════════════════════════════
            // CENTER DIVIDER COLUMN (Close & Tool Buttons)
            // ═════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top subtle close button
                Surface(
                    onClick = onClose,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF424242),
                    modifier = Modifier
                        .size(width = 54.dp, height = 36.dp)
                        .testTag("first_level_top_close_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Center circular RED STOP/CLOSE HAND button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onClose() }
                ) {
                    Surface(
                        onClick = onClose,
                        shape = CircleShape,
                        color = Color(0xFFD32F2F),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("first_level_hand_close_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Close Stop Hand",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CLOSE",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Middle red rectangular Video Referee (VAR) button
                Surface(
                    onClick = onVideoReferee,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(width = 56.dp, height = 44.dp)
                        .testTag("first_level_var_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        VideoRefereeWhistleTvIcon()
                    }
                }

                // Bottom grey three dots [...] button
                Surface(
                    onClick = onGeneralEvents,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF616161),
                    modifier = Modifier
                        .size(width = 54.dp, height = 36.dp)
                        .testTag("first_level_general_events_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.5.dp)
                                    .size(5.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }

            // ═════════════════════════════════════════════════════════════════
            // RIGHT SIDE: AWAY TEAM (Blue event tiles)
            // ═════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = awayTeamName.uppercase(),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Grid 3 rows x 2 cols
                // Row 1: Cards + Corner kick (Top)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Cards",
                        buttonColor = awayButtonColor,
                        iconContent = { YellowRedCardsIcon() },
                        onClick = { onCards("AWAY") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_cards"
                    )
                    FirstLevelEventButton(
                        label = "Corner kick",
                        buttonColor = awayButtonColor,
                        iconContent = { CornerKickTopFlagIcon(flipped = true) },
                        onClick = { onCornerKick("AWAY", true) },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_corner_top"
                    )
                }

                // Row 2: Penalty kick + Goal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Penalty kick",
                        buttonColor = awayButtonColor,
                        iconContent = { WhistleIcon() },
                        onClick = { onPenaltyKick("AWAY") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_penalty"
                    )
                    FirstLevelEventButton(
                        label = "Goal",
                        buttonColor = awayButtonColor,
                        iconContent = { ShotBallIcon(modifier = Modifier.size(34.dp)) },
                        onClick = { onGoal("AWAY") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_goal"
                    )
                }

                // Row 3: Dangerous free kick + Corner kick (Bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FirstLevelEventButton(
                        label = "Dangerous\nfree kick",
                        buttonColor = awayButtonColor,
                        iconContent = { DangerousFreeKickIcon() },
                        onClick = { onDangerousFreeKick("AWAY") },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_dfk"
                    )
                    FirstLevelEventButton(
                        label = "Corner kick",
                        buttonColor = awayButtonColor,
                        iconContent = { CornerKickBottomFlagIcon(flipped = true) },
                        onClick = { onCornerKick("AWAY", false) },
                        modifier = Modifier.weight(1f),
                        testTag = "first_level_away_corner_bottom"
                    )
                }
            }
        }
    }
}

@Composable
private fun FirstLevelEventButton(
    label: String,
    buttonColor: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = buttonColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2
            )
        }
    }
}

